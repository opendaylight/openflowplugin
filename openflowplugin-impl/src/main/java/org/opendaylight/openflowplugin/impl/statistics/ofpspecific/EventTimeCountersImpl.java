/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.ofpspecific;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventTimeCounters;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Singleton
@Component(immediate = true)
public final class EventTimeCountersImpl implements EventTimeCounters {
    private final Map<String, Map<String, EventTimeCounter>> events = new ConcurrentHashMap<>();

    @Singleton
    @Activate
    public EventTimeCountersImpl() {
        // Exposed for DI
    }

    @Override
    public void markStart(final EventIdentifier eventIdentifier) {
        getCounter(eventIdentifier).markStart();
    }

    @Override
    public void markEnd(final EventIdentifier eventIdentifier) {
        getCounter(eventIdentifier).markEnd();
    }

    @Override
    public List<String> provideTimes() {
        final var dump = new ArrayList<String>();
        for (var deviceEntry : events.entrySet()) {
            var eventsMap = deviceEntry.getValue();
            dump.add("================================================");
            dump.add(String.format("DEVICE : %s", deviceEntry.getKey()));
            for (var eventEntry : eventsMap.entrySet()) {
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

    @Override
    public void resetAllCounters() {
        events.clear();
    }

    @Override
    public boolean resetCounters(final String deviceName) {
        return events.remove(requireNonNull(deviceName)) != null;
    }

    private EventTimeCounter getCounter(final EventIdentifier eventIdentifier) {
        return events.computeIfAbsent(eventIdentifier.getDeviceId(), k -> new ConcurrentHashMap<>())
            .computeIfAbsent(eventIdentifier.getEventName(), k -> new EventTimeCounter());
    }
}
