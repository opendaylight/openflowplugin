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
import org.opendaylight.of.controller.pkt.SplMetric;
import org.opendaylight.util.StringUtils;

/**
 * Default implementation of {@link SplMetric}.
 *
 * @author Simon Hunt
 * @author Frank Wood
 */
class SplMetricSnapshot implements SplMetric {
    private static final double MILLION = 1000000.0;
    private static final double UNDEF = -1.0;

    private static final String FMT_6F = "%.6f";
    private static final String NA = "n/a";
    private static final String FMT = "{} [alt={}] {}: av = {} ms, #samples = {}";

    private final Class<? extends SequencedPacketListener> splClass;
    private final SequencedPacketListenerRole role;
    private final int altitude;
    private final long td;
    private final long sc;


    SplMetricSnapshot(SplMetricData metric) {
        splClass = metric.splClass;
        role = metric.role;
        altitude = metric.altitude;
        td = metric.totalDuration;
        sc = metric.sampleCount;
    }

    @Override
    public Class<? extends SequencedPacketListener> splClass() {
        return splClass;
    }

    @Override
    public SequencedPacketListenerRole role() {
        return role;
    }

    @Override
    public int altitude() {
        return altitude;
    }

    @Override
    public long totalDuration() {
        return td;
    }

    @Override
    public long sampleCount() {
        return sc;
    }

    @Override
    public double averageDurationNanos() {
        return sc == 0 ? UNDEF : (double) td / (double) sc;
    }

    @Override
    public double averageDurationMs() {
        double nanos = averageDurationNanos();
        return nanos < 0 ? UNDEF : nanos / MILLION;
    }

    @Override
    public String averageMs() {
        double avd = averageDurationMs();
        return avd < 0 ? NA : String.format(FMT_6F, avd);
    }

    @Override
    public String toString() {
        return StringUtils.format(FMT, role, altitude, splClass.getName(),
                averageMs(), sc);
    }
}
