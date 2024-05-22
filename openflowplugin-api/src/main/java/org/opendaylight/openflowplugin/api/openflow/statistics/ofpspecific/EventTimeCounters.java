/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Counters of events occurring on devices.
 */
@NonNullByDefault
public interface EventTimeCounters {

    void markStart(EventIdentifier eventIdentifier);

    void markEnd(EventIdentifier eventIdentifier);

    List<String> provideTimes();

    /**
     * Reset all counters for all devices.
     */
    void resetAllCounters();

    /**
     * Reset counters for a particular device.
     *
     * @param deviceName device name
     * @return {@code true} if there were counters for the specified device, {@code false} otherwise
     */
    boolean resetCounters(String deviceName);
}
