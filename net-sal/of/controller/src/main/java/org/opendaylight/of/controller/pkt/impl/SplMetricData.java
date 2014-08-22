/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.pkt.impl;

import org.opendaylight.of.controller.pkt.SequencedPacketListenerRole;
import org.opendaylight.of.controller.pkt.SequencedPacketListener;

/**
 * Encapsulates metric data for a sequenced packet listener instance.
 *
 * @author Simon Hunt
 * @author Frank Wood
 */
class SplMetricData {
    final Class<? extends SequencedPacketListener> splClass;
    final SequencedPacketListenerRole role;
    final int altitude;
    long totalDuration = 0;
    long sampleCount = 0;

    SplMetricData(SequencedPacketListener s, SequencedPacketListenerRole r, int alt) {
        splClass = s.getClass();
        role = r;
        altitude = alt;
    }

    void addSample(long duration) {
        totalDuration += duration;
        sampleCount++;
    }
}
