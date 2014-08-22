/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.mp.MBodyMeterStats;
import org.opendaylight.of.lib.mp.MBodyMutableMeterStats;
import org.opendaylight.of.lib.mp.MpBodyFactory;

import java.util.ArrayList;
import java.util.List;

import static org.opendaylight.of.json.CodecUtils.*;
import static org.opendaylight.of.lib.mp.MBodyMeterStats.MeterBandStats;
import static org.opendaylight.of.lib.mp.MultipartType.METER;

/**
 * A JSON codec capable of encoding and decoding {@link MBodyMeterStats}
 * objects.
 *
 * @author Shaila Shree
 *
 */
public class MBodyMeterStatsCodec extends OfJsonCodec<MBodyMeterStats> {

    public static final String ROOT = "meter_stats";
    public static final String ROOTS = "meter_stats";

    private static final String METER_ID = "id";
    private static final String FLOW_COUNT = "flow_count";
    private static final String DURATION_SEC = "duration_sec";
    private static final String DURATION_NSEC = "duration_nsec";
    private static final String PKT_IN_COUNT = "packet_count";
    private static final String BYTE_IN_COUNT = "byte_count";
    private static final String METER_BANDS = "band_stats";
    private static final String PACKET_BAND_COUNT = "packet_count";
    private static final String BYTE_BAND_COUNT = "byte_count";

    /**
     * Constructs a MBodyMeterStatsCodec.
     */
    protected MBodyMeterStatsCodec() {
        super(ROOT, ROOTS);
    }

    @Override
    public ObjectNode encode(MBodyMeterStats stats) {
        ObjectNode node = objectNode();
        
        node.put(METER_ID, encodeMeterId(stats.getMeterId()));
        node.put(FLOW_COUNT, stats.getFlowCount());
        node.put(PKT_IN_COUNT, stats.getPktInCount());
        node.put(BYTE_IN_COUNT, stats.getByteInCount());
        node.put(DURATION_SEC, stats.getDurationSec());
        node.put(DURATION_NSEC, stats.getDurationNSec());
        node.put(METER_BANDS, encodeMeterBands(stats.getBandStats()));

        return node;
    }
    
    @Override
    public MBodyMeterStats decode(ObjectNode node) {
        ProtocolVersion ver = decodeProtocolVersion(node.get(VERSION));
        MBodyMutableMeterStats stats = (MBodyMutableMeterStats)
                MpBodyFactory.createReplyBodyElement(ver, METER);

        stats.meterId(decodeMeterId(node.get(METER_ID)));
        stats.flowCount(node.get(FLOW_COUNT).asLong());
        stats.packetInCount(node.get(PKT_IN_COUNT).asInt());
        stats.byteInCount(node.get(BYTE_IN_COUNT).asInt());
        stats.duration(node.get(DURATION_SEC).asLong(),
                       node.get(DURATION_NSEC).asLong());
        List<MeterBandStats> mbss = decodeMeterBands(node.get(METER_BANDS));

        for (MeterBandStats mbs: mbss)
            stats.addMeterBandStat(mbs);

        return (MBodyMeterStats)stats.toImmutable();
    }

    private ArrayNode encodeMeterBands(List<MeterBandStats> mbs) {
        ArrayNode arrayNode = arrayNode();

        for (MeterBandStats mb: mbs) {
            ObjectNode node = objectNode();
            node.put(PACKET_BAND_COUNT, mb.getPacketBandCount())
                .put(BYTE_BAND_COUNT, mb.getByteBandCount());
            arrayNode.add(node);
        }

        return arrayNode;
    }

    private List<MeterBandStats> decodeMeterBands(JsonNode arrayNode) {
        List<MeterBandStats> mbss  = new ArrayList<MeterBandStats>();

        for (JsonNode node: arrayNode) {
            ObjectNode mbNode = (ObjectNode) node;
            MeterBandStats mbs = new MeterBandStats(
                                    mbNode.get(PACKET_BAND_COUNT).asLong(),
                                    mbNode.get(BYTE_BAND_COUNT).asLong());
            mbss.add(mbs);
        }

        return mbss;
    }
}
