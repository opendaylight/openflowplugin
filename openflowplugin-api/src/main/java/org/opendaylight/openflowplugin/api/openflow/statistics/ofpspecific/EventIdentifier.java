/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 29.5.2015.
 */
public final class EventIdentifier {

    private final String eventName;
    private final String deviceId;

    public EventIdentifier(final String eventName, final String deviceId) {
        this.eventName = eventName;
        this.deviceId = deviceId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
