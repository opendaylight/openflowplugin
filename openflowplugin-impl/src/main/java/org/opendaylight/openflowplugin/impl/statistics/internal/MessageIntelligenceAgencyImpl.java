/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency}.
 * Class counts message of {@link org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy.STATISTIC_GROUP} type
 * and provides info as debug log.
 */
public class MessageIntelligenceAgencyImpl implements MessageIntelligenceAgency<Class> {

    private static final Logger LOG = LoggerFactory.getLogger(MessageIntelligenceAgencyImpl.class);

    private static final class MessageCounters {
        private static final AtomicLongFieldUpdater<MessageCounters> UPDATER = AtomicLongFieldUpdater.newUpdater(MessageCounters.class, "current");
        private volatile long current;
        private long cumulative;

        public synchronized long accumulate() {
            final long inc = UPDATER.getAndSet(this, 0);
            cumulative += inc;
            return inc;
        }

        public synchronized long getCumulative() {
            return cumulative;
        }

        public long increment() {
            return UPDATER.incrementAndGet(this);
        }
    }

    private final ConcurrentMap<STATISTIC_GROUP, ConcurrentMap<Class, MessageCounters>> inputStats = new ConcurrentHashMap<>();

    @Override
    public void spyIn(final Class message) {
        getCounters(message, STATISTIC_GROUP.FROM_SWITCH_TRANSLATE_IN_SUCCESS).increment();
    }

    @Override
    public void spyOut(final Class message) {
        getCounters(message, STATISTIC_GROUP.FROM_SWITCH_TRANSLATE_OUT_SUCCESS).increment();
    }

    @Override
    public void spyMessage(final Class message, final STATISTIC_GROUP statGroup) {
        getCounters(message, statGroup).increment();
    }

    /**
     * @param message
     * @param statGroup TODO
     * @return
     */
    private MessageCounters getCounters(final Class message, final STATISTIC_GROUP statGroup) {
        ConcurrentMap<Class, MessageCounters> groupData = getOrCreateGroupData(statGroup);
        MessageCounters counters = getOrCreateCountersPair(message, groupData);
        return counters;
    }

    private static MessageCounters getOrCreateCountersPair(final Class msgType, final ConcurrentMap<Class, MessageCounters> groupData) {
        final MessageCounters lookup = groupData.get(msgType);
        if (lookup != null) {
            return lookup;
        }

        final MessageCounters newCounters = new MessageCounters();
        final MessageCounters check = groupData.putIfAbsent(msgType, newCounters);
        return check == null ? newCounters : check;

    }

    private ConcurrentMap<Class, MessageCounters> getOrCreateGroupData(final STATISTIC_GROUP statGroup) {
        final ConcurrentMap<Class, MessageCounters> lookup = inputStats.get(statGroup);
        if (lookup != null) {
            return lookup;
        }

        final ConcurrentMap<Class, MessageCounters> newmap = new ConcurrentHashMap<>();
        final ConcurrentMap<Class, MessageCounters> check = inputStats.putIfAbsent(statGroup, newmap);

        return check == null ? newmap : check;
    }

    @Override
    public void run() {
        // log current counters and cleans it
        if (LOG.isDebugEnabled()) {
            for (String counterItem : provideIntelligence()) {
                LOG.debug(counterItem);
            }
        }
    }

    @Override
    public List<String> provideIntelligence() {
        List<String> dump = new ArrayList<>();

        for (STATISTIC_GROUP statGroup : STATISTIC_GROUP.values()) {
            Map<Class, MessageCounters> groupData = inputStats.get(statGroup);
            if (groupData != null) {
                for (Entry<Class, MessageCounters> statEntry : groupData.entrySet()) {
                    long amountPerInterval = statEntry.getValue().accumulate();
                    long cumulativeAmount = statEntry.getValue().getCumulative();
                    dump.add(String.format("%s: MSG[%s] -> +%d | %d",
                            statGroup,
                            statEntry.getKey().getSimpleName(),
                            amountPerInterval, cumulativeAmount));
                }
            } else {
                dump.add(String.format("%s: no activity detected", statGroup));
            }
        }
        return dump;
    }
}
