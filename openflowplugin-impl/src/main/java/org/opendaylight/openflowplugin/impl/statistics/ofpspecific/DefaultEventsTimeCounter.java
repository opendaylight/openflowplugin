/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.ofpspecific;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Singleton
@Component(immediate = true)
public final class DefaultEventsTimeCounter {
    private final Map<String, Map<String, EventTimeCounter>> deviceEvents = new ConcurrentHashMap<>();

    @Inject
    @Activate
    private DefaultEventsTimeCounter() {
        // Expose for DI
    }

    public void markStart(final EventIdentifier eventIdentifier) {
        getCounter(eventIdentifier).markStart();
    }

    public void markEnd(final EventIdentifier eventIdentifier) {
        getCounter(eventIdentifier).markEnd();
    }

    private EventTimeCounter getCounter(final EventIdentifier eventIdentifier) {
        return deviceEvents
            .computeIfAbsent(eventIdentifier.deviceId(), k -> new ConcurrentHashMap<>())
            .computeIfAbsent(eventIdentifier.eventName(), k -> new EventTimeCounter());
    }

    public List<String> provideTimes() {
        final var dump = new ArrayList<String>();
        for (var deviceEntry : deviceEvents.entrySet()) {
            var eventsMap = deviceEntry.getValue();
            dump.add("================================================");
            dump.add(String.format("DEVICE : %s", deviceEntry.getKey()));
            for (Map.Entry<String, EventTimeCounter> eventEntry : eventsMap.entrySet()) {
                final String eventName = eventEntry.getKey();
                final EventTimeCounter eventTimeCounter = eventEntry.getValue();
                dump.add(String.format("%s", eventName));
                dump.add(String.format("    MIN TIME (ms):  %d",
                        TimeUnit.MILLISECONDS.convert(eventTimeCounter.getMinimum(), TimeUnit.NANOSECONDS)));
                dump.add(String.format("    MAX TIME (ms):  %d",
                        TimeUnit.MILLISECONDS.convert(eventTimeCounter.getMaximum(), TimeUnit.NANOSECONDS)));
                dump.add(String.format("    AVG TIME (ms):  %d",
                        TimeUnit.MILLISECONDS.convert(eventTimeCounter.getAverage(), TimeUnit.NANOSECONDS)));

            }
        }
        return dump;
    }

    public void resetAllCounters() {
        deviceEvents.clear();
    }

    private static final class EventTimeCounter {

        private volatile long delta = 0;
        private volatile long average = 0;
        private volatile long minimum = 0;
        private volatile long maximum = 0;
        private volatile long summary = 0;
        private volatile int counter = 0;

        public synchronized void markStart() {
            delta = System.nanoTime();
        }

        @SuppressFBWarnings("VO_VOLATILE_INCREMENT") // counter++ is volatile, but this is synchronized, so OK
        public synchronized void markEnd() {
            if (0 == delta) {
                return;
            }
            counter++;
            delta = System.nanoTime() - delta;

            if (delta < minimum || minimum == 0) {
                minimum = delta;
            }
            if (delta > maximum) {
                maximum = delta;
            }
            if (average > 0 && delta > average * 1.8) {
                summary += average;
            } else {
                summary += delta;
            }
            average = summary / counter;
        }

        public synchronized void resetCounters() {
            delta = 0;
            average = 0;
            minimum = 0;
            maximum = 0;
            summary = 0;
            counter = 0;

        }

        public synchronized long getAverage() {
            return average;
        }

        public synchronized long getMinimum() {
            return minimum;
        }

        public synchronized long getMaximum() {
            return maximum;
        }
    }
}
