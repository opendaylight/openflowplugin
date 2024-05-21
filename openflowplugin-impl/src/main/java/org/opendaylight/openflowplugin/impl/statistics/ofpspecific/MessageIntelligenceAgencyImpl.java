/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.ofpspecific;

import static java.util.Objects.requireNonNull;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of
 * {@link org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency}.
 * Class counts message of {@link StatisticsGroup} type and provides info as debug log.
 */
@Singleton
@Component(immediate = true, service = MessageIntelligenceAgency.class)
public final class MessageIntelligenceAgencyImpl implements MessageIntelligenceAgency, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(MessageIntelligenceAgencyImpl.class);

    private static final ObjectName MXBEAN_OBJECT_NAME;

    static {
        try {
            MXBEAN_OBJECT_NAME = new ObjectName("%s:type=%s".formatted(
                    MessageIntelligenceAgencyMXBean.class.getPackage().getName(),
                    MessageIntelligenceAgencyMXBean.class.getSimpleName()));
        } catch (MalformedObjectNameException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final class MessageCounters {
        private static final AtomicLongFieldUpdater<MessageCounters> UPDATER =
                AtomicLongFieldUpdater.newUpdater(MessageCounters.class, "current");
        @SuppressWarnings("unused")
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

    private final Map<StatisticsGroup, Map<Class<?>, MessageCounters>> inputStats = new ConcurrentHashMap<>();

    private boolean runUnreg;

    @Inject
    @Activate
    public MessageIntelligenceAgencyImpl() {
        try {
            ManagementFactory.getPlatformMBeanServer()
                .registerMBean((MessageIntelligenceAgencyMXBean) this::provideIntelligence, MXBEAN_OBJECT_NAME);
            runUnreg = true;
            LOG.info("Registered MBean {}", MXBEAN_OBJECT_NAME);
        } catch (NotCompliantMBeanException | MBeanRegistrationException | InstanceAlreadyExistsException e) {
            LOG.warn("Error registering MBean {}", MXBEAN_OBJECT_NAME, e);
            runUnreg = false;
        }
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        if (runUnreg) {
            runUnreg = false;
            try {
                ManagementFactory.getPlatformMBeanServer().unregisterMBean(MXBEAN_OBJECT_NAME);
                LOG.info("Unregistered MBean {}", MXBEAN_OBJECT_NAME);
            } catch (InstanceNotFoundException | MBeanRegistrationException e) {
                LOG.warn("Error unregistering MBean {}", MXBEAN_OBJECT_NAME, e);
            }
        }
    }

    @Override
    public void spyMessage(final Class<?> message, final StatisticsGroup statGroup) {
        requireNonNull(message, "Message can't be null.");
        getCounters(message, statGroup).increment();
    }

    /**
     * Get counters.
     * @param message counted element
     * @param statGroup statistic counter group
     * @return corresponding counter
     */
    private MessageCounters getCounters(final Class<?> message, final StatisticsGroup statGroup) {
        return inputStats
            .computeIfAbsent(statGroup, key -> new ConcurrentHashMap<>())
            .computeIfAbsent(message, key -> new MessageCounters());
    }

    @Override
    public void run() {
        // log current counters and cleans it
        if (LOG.isDebugEnabled()) {
            for (var counterItem : provideIntelligence()) {
                LOG.debug("Counter: {}", counterItem);
            }
        }
    }

    @Override
    public List<String> provideIntelligence() {
        final var dump = new ArrayList<String>();

        for (var statGroup : StatisticsGroup.values()) {
            final var groupData = inputStats.get(statGroup);
            if (groupData != null) {
                for (var statEntry : groupData.entrySet()) {
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

    @Override
    public void resetStatistics() {
        inputStats.clear();
    }
}
