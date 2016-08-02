/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.ofpspecific;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 28.5.2015.
 */
public final class EventsTimeCounter {

    private static final Map<String, Map<String, EventTimeCounter>> DEVICES_EVENTS = new HashMap<>();

    private EventsTimeCounter() {
        // Hiding implicit constructor
    }

    public static void markStart(final EventIdentifier eventIdentifier) {
        Map<String, EventTimeCounter> deviceEvents = getOrCreateCountersForDevice(eventIdentifier.getDeviceId());
        EventTimeCounter eventTimeCounter = getOrCreateEventOfType(eventIdentifier.getEventName(), deviceEvents);
        eventTimeCounter.markStart();
    }

    public static void markEnd(final EventIdentifier eventIdentifier) {
        Map<String, EventTimeCounter> deviceEvents = getOrCreateCountersForDevice(eventIdentifier.getDeviceId());
        EventTimeCounter eventTimeCounter = getOrCreateEventOfType(eventIdentifier.getEventName(), deviceEvents);
        eventTimeCounter.markEnd();
    }

    private static EventTimeCounter getOrCreateEventOfType(final String event, final Map<String, EventTimeCounter> deviceEvents) {
        EventTimeCounter lookup = deviceEvents.get(event);
        if (null == lookup) {
            lookup = new EventTimeCounter();
            deviceEvents.put(event, lookup);
        }
        return lookup;
    }

    private static Map<String, EventTimeCounter> getOrCreateCountersForDevice(final String deviceId) {
        Map<String, EventTimeCounter> lookup = DEVICES_EVENTS.get(deviceId);
        if (null == lookup) {
            lookup = new HashMap<>();
            DEVICES_EVENTS.put(deviceId, lookup);
        }

        return lookup;
    }

    public static List<String> provideTimes() {
        List<String> dump = new ArrayList<>();
        for (Map.Entry<String, Map<String, EventTimeCounter>> deviceEntry : DEVICES_EVENTS.entrySet()) {
            Map<String, EventTimeCounter> eventsMap = deviceEntry.getValue();
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

    public static void resetAllCounters() {
        DEVICES_EVENTS.clear();
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
            if (average > 0 && delta > (average * 1.8)) {
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
