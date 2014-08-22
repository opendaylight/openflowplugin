/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.json;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendaylight.of.controller.pkt.SplMetric;
import org.opendaylight.util.json.AbstractJsonCodec;
import org.opendaylight.util.json.JsonCodec;

/**
 * A JSON codec capable of encoding and decoding {@link SplMetric} entities.
 *
 * @author Simon Hunt
 * @author Frank Wood
 */
public class SplMetricCodec extends AbstractJsonCodec<SplMetric>
        implements JsonCodec<SplMetric> {

    private static final String ROOTS = "metrics";
    private static final String ROOT = "metric";

    private static final String CLASS = "class";
    private static final String ROLE = "role";
    private static final String ALTITUDE = "altitude";
    private static final String SAMPLE_COUNT = "sample_count";
    private static final String TOTAL_DURATION = "total_duration";
    private static final String AV_DURATION_NANOS = "av_duration_nanos";
    private static final String AV_DURATION_MS = "av_duration_ms";
    private static final String AV_MS_STRING = "av_ms_string";

    /**
     * Creates the SPL metric codec.
     */
    protected SplMetricCodec() {
        super(ROOT, ROOTS);
    }

    @Override
    public ObjectNode encode(SplMetric m) {
        ObjectNode node = objectNode();
        node.put(CLASS, m.splClass().getName());
        node.put(ROLE, m.role().toString());
        node.put(ALTITUDE, m.altitude());
        node.put(SAMPLE_COUNT, m.sampleCount());
        node.put(TOTAL_DURATION, m.totalDuration());
        node.put(AV_DURATION_NANOS, m.averageDurationNanos());
        node.put(AV_DURATION_MS, m.averageDurationMs());
        node.put(AV_MS_STRING, m.averageMs());
        return node;
    }

    @Override
    public SplMetric decode(ObjectNode jsonNodes) {
        throw new UnsupportedOperationException("so there!");
    }
}
