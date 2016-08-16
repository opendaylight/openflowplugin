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
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 5.6.2015.
 */
public class SessionStatistics {

    private static final Map<String, Map<ConnectionStatus, EventCounter>> SESSION_EVENTS = new HashMap<>();

    public static void countEvent(final String sessionId, final ConnectionStatus connectionStatus) {
        Map<ConnectionStatus, EventCounter> sessionsConnectionEvents = getConnectionEvents(sessionId);
        EventCounter connectionEvent = getConnectionEvent(sessionsConnectionEvents, connectionStatus);
        connectionEvent.increment();
    }

    private static EventCounter getConnectionEvent(final Map<ConnectionStatus, EventCounter> sessionsConnectionEvents,
                                                   final ConnectionStatus connectionStatus) {
        EventCounter eventCounter = sessionsConnectionEvents.get(connectionStatus);
        if (null == eventCounter) {
            eventCounter = new EventCounter();
            sessionsConnectionEvents.put(connectionStatus, eventCounter);
        }
        return eventCounter;
    }

    private static Map<ConnectionStatus, EventCounter> getConnectionEvents(final String sessionId) {
        Map<ConnectionStatus, EventCounter> sessionConnectionEvents = SESSION_EVENTS.get(sessionId);
        if (null == sessionConnectionEvents) {
            sessionConnectionEvents = new HashMap<>();
            SESSION_EVENTS.put(sessionId, sessionConnectionEvents);
        }
        return sessionConnectionEvents;
    }


    public static List<String> provideStatistics() {
        List<String> dump = new ArrayList<>();
        for (Map.Entry<String, Map<ConnectionStatus, EventCounter>> sessionEntries : SESSION_EVENTS.entrySet()) {
            Map<ConnectionStatus, EventCounter> sessionEvents = sessionEntries.getValue();
            dump.add(String.format("SESSION : %s", sessionEntries.getKey()));
            for (Map.Entry<ConnectionStatus, EventCounter> sessionEvent : sessionEvents.entrySet()) {
                dump.add(String.format(" %s : %d", sessionEvent.getKey().toString(), sessionEvent.getValue().getCount()));
            }
        }
        return dump;

    }

    public enum ConnectionStatus {
        CONNECTION_CREATED, CONNECTION_DISCONNECTED_BY_DEVICE, CONNECTION_DISCONNECTED_BY_OFP;
    }

    private static final class EventCounter {
        private final AtomicLongFieldUpdater<EventCounter> updater = AtomicLongFieldUpdater.newUpdater(EventCounter.class, "count");
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
