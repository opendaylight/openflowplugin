/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * message counter (by type)
 */
public class MessageSpyCounterImpl implements MessageObservatory<DataContainer> {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(MessageSpyCounterImpl.class);
    
    private Map<STATISTIC_GROUP, Map<Class<? extends DataContainer>, AtomicLong[]>> inputStats = new ConcurrentHashMap<>();
    
    @Override
    public void spyIn(DataContainer message) {
        AtomicLong[] counters = getCounters(message, STATISTIC_GROUP.FROM_SWITCH_TRANSLATE_IN_SUCCESS);
        counters[0].incrementAndGet();  
    }

    /**
     * @param message
     * @param statGroup TODO
     * @return
     */
    private AtomicLong[] getCounters(DataContainer message, STATISTIC_GROUP statGroup) {
        Class<? extends DataContainer> msgType = message.getImplementedInterface();
        Map<Class<? extends DataContainer>, AtomicLong[]> groupData = getOrCreateGroupData(statGroup);
        AtomicLong[] counters = getOrCreateCountersPair(msgType, groupData);
        return counters;
    }
    
    private static AtomicLong[] getOrCreateCountersPair(Class<? extends DataContainer> msgType, Map<Class<? extends DataContainer>, AtomicLong[]> groupData) {
        AtomicLong[] counters = groupData.get(msgType);
        synchronized(groupData) {
            if (counters == null) {
                counters = new AtomicLong[] {new AtomicLong(), new AtomicLong()};
                groupData.put(msgType, counters);
            } 
        }
        return counters;
    }

    private Map<Class<? extends DataContainer>, AtomicLong[]> getOrCreateGroupData(STATISTIC_GROUP statGroup) {
        Map<Class<? extends DataContainer>, AtomicLong[]> groupData = null;
        synchronized(inputStats) {
            groupData = inputStats.get(statGroup);
            if (groupData == null) {
                groupData = new HashMap<>();
                inputStats.put(statGroup, groupData);
            }
        }
        return groupData;
    }

    @Override
    public void spyOut(DataContainer message) {
        AtomicLong[] counters = getCounters(message, STATISTIC_GROUP.FROM_SWITCH_TRANSLATE_OUT_SUCCESS);
        counters[0].incrementAndGet();  
    }

    @Override
    public void run() {
        // log current counters and cleans it
        if (LOG.isDebugEnabled()) {
            for (String counterItem : dumpMessageCounts()) {
                LOG.debug(counterItem);
            }
        }
    }

    @Override
    public List<String> dumpMessageCounts() {
        List<String> dump = new ArrayList<>();
        for (STATISTIC_GROUP statGroup : STATISTIC_GROUP.values()) {
            Map<Class<? extends DataContainer>, AtomicLong[]> groupData = inputStats.get(statGroup);
            if (groupData != null) {
                for (Entry<Class<? extends DataContainer>, AtomicLong[]> statEntry : groupData.entrySet()) {
                    long amountPerInterval = statEntry.getValue()[0].getAndSet(0);
                    long cumulativeAmount = statEntry.getValue()[1].addAndGet(amountPerInterval);
                    dump.add(String.format("%s: MSG[%s] -> +%d | %d",
                            statGroup,
                            statEntry.getKey().getSimpleName(), amountPerInterval, cumulativeAmount));
                }
                
            } else {
                dump.add(String.format("%s: no activity detected", statGroup));
            }
        }
        return dump;
    }

    @Override
    public void spyMessage(DataContainer message, STATISTIC_GROUP statGroup) {
        AtomicLong[] counters = getCounters(message, statGroup);
        counters[0].incrementAndGet();
    }
}
