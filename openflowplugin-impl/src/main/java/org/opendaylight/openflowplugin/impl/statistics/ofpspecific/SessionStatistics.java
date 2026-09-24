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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public final class SessionStatistics {

    private SessionStatistics() {
    }

    private static final Map<String, Map<ConnectionStatus, EventCounter>> SESSION_EVENTS = new ConcurrentHashMap<>();

    public static void countEvent(final String sessionId, final ConnectionStatus connectionStatus) {
        Map<ConnectionStatus, EventCounter> sessionsConnectionEvents = getConnectionEvents(sessionId);
        EventCounter connectionEvent = getConnectionEvent(sessionsConnectionEvents, connectionStatus);
        connectionEvent.increment();
    }

    private static EventCounter getConnectionEvent(final Map<ConnectionStatus, EventCounter> sessionsConnectionEvents,
                                                   final ConnectionStatus connectionStatus) {
        return sessionsConnectionEvents.computeIfAbsent(connectionStatus, k -> new EventCounter());
    }

    private static Map<ConnectionStatus, EventCounter> getConnectionEvents(final String sessionId) {
        return SESSION_EVENTS.computeIfAbsent(sessionId, k -> new HashMap<>());
    }


    public static List<String> provideStatistics() {
        var dump = new ArrayList<String>();
        for (var sessionEntries : SESSION_EVENTS.entrySet()) {
            var sessionEvents = sessionEntries.getValue();
            dump.add("SESSION : %s".formatted(sessionEntries.getKey()));
            for (var sessionEvent : sessionEvents.entrySet()) {
                dump.add(" %s : %d".formatted(sessionEvent.getKey(), sessionEvent.getValue().getCount()));
            }
        }
        return dump;

    }

    public enum ConnectionStatus {
        CONNECTION_CREATED, CONNECTION_DISCONNECTED_BY_DEVICE, CONNECTION_DISCONNECTED_BY_OFP
    }

    private static final class EventCounter {
        private final AtomicLongFieldUpdater<EventCounter> updater =
                AtomicLongFieldUpdater.newUpdater(EventCounter.class, "count");
        private volatile long count;

        public long getCount() {
            return count;
        }

        public void increment() {
            count = updater.incrementAndGet(this);
        }
    }

    public static void resetAllCounters() {
        SESSION_EVENTS.clear();
    }

}
