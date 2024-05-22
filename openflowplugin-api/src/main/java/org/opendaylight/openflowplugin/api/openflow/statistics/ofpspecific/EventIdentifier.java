/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 29.5.2015.
 */
@NonNullByDefault
public record EventIdentifier(String eventName, String deviceId) {
    public EventIdentifier {
        requireNonNull(eventName);
        requireNonNull(deviceId);
    }

    @Deprecated(forRemoval = true)
    public String getEventName() {
        return eventName;
    }

    @Deprecated(forRemoval = true)
    public String getDeviceId() {
        return deviceId;
    }
}
