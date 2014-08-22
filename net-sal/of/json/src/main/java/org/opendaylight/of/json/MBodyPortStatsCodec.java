/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.mp.MBodyMutablePortStats;
import org.opendaylight.of.lib.mp.MBodyPortStats;
import org.opendaylight.util.net.BigPortNumber;

/**
 * A JSON codec capable of encoding and decoding {@link MBodyPortStats}
 * objects.
 * 
 * @author Jesse Hummer
 */
public class MBodyPortStatsCodec extends OfJsonCodec<MBodyPortStats> {

    /** JSON key for meter */
    public static final String ROOT = "port_stats";
    /** JSON key for meters */
    public static final String ROOTS = "port_stats";

    /** JSON key for port ID */
    private static final String ID = "port_id";
    /** JSON key for rx packets */
    private static final String RX_PACKETS = "rx_packets";
    /** JSON key for tx packets */
    private static final String TX_PACKETS = "tx_packets";
    /** JSON key for rx bytes */
    private static final String RX_BYTES = "rx_bytes";
    /** JSON key for tx bytes */
    private static final String TX_BYTES = "tx_bytes";
    /** JSON key for rx dropped */
    private static final String RX_DROPPED = "rx_dropped";
    /** JSON key for tx dropped */
    private static final String TX_DROPPED = "tx_dropped";
    /** JSON key for rx errors */
    private static final String RX_ERRORS = "rx_errors";
    /** JSON key for tx errors */
    private static final String TX_ERRORS = "tx_errors";
    /** JSON key for collisions */
    private static final String COLLISIONS = "collisions";
    /** JSON key for duration in seconds */
    private static final String DURATION_SEC = "duration_sec";
    /** JSON key for duration in nanoseconds beyond duration in seconds */
    private static final String DURATION_NSEC = "duration_nsec";
    /** JSON key for received CRC errors */
    private static final String RX_CRC_ERR = "rx_crc_err";
    /** JSON key for received frame errors */
    private static final String RX_FRAME_ERR = "rx_frame_err";
    /** JSON key for received overrun errors */
    private static final String RX_OVER_ERR = "rx_over_err";

    public MBodyPortStatsCodec() {
        super(ROOT, ROOTS);
    }

    @Override
    public String encode(MBodyPortStats ps, boolean prettyPrint) {
        throw new UnsupportedOperationException(
                "Port statistics is always in plural form");
    }

    @Override
    public ObjectNode encode(MBodyPortStats ps) {
        ObjectNode node = mapper.createObjectNode();

        node.put(ID, ps.getPort().toLong());
        node.put(RX_PACKETS, ps.getRxPackets());
        node.put(TX_PACKETS, ps.getTxPackets());
        node.put(RX_BYTES, ps.getRxBytes());
        node.put(TX_BYTES, ps.getTxBytes());
        node.put(RX_DROPPED, ps.getRxDropped());
        node.put(TX_DROPPED, ps.getTxDropped());
        node.put(RX_ERRORS, ps.getRxErrors());
        node.put(TX_ERRORS, ps.getTxErrors());
        node.put(COLLISIONS, ps.getCollisions());
        node.put(DURATION_SEC, ps.getDurationSec()); // 1.3
        node.put(DURATION_NSEC, ps.getDurationNsec()); // 1.3
        node.put(RX_CRC_ERR, ps.getRxCRCErr());
        node.put(RX_FRAME_ERR, ps.getRxFrameErr());
        node.put(RX_OVER_ERR, ps.getRxOverErr());

        return node;
    }

    @Override
    public MBodyPortStats decode(ObjectNode node) {
        // note that the following will fail if decode(String) was not called!
        ProtocolVersion pv = version(node);
        MBodyMutablePortStats pojo = new MBodyMutablePortStats(pv);

        pojo.port(BigPortNumber.valueOf(node.get(ID).longValue()));
        pojo.rxPackets(node.get(RX_PACKETS).longValue());
        pojo.txPackets(node.get(TX_PACKETS).longValue());
        pojo.rxBytes(node.get(RX_BYTES).longValue());
        pojo.txBytes(node.get(TX_BYTES).longValue());
        pojo.rxDropped(node.get(RX_DROPPED).longValue());
        pojo.txDropped(node.get(TX_DROPPED).longValue());
        pojo.rxErrors(node.get(RX_ERRORS).longValue());
        pojo.txErrors(node.get(TX_ERRORS).longValue());
        pojo.collisions(node.get(COLLISIONS).longValue());
        pojo.duration(node.get(DURATION_SEC).longValue(),
                node.get(DURATION_NSEC).longValue()); // 1.3
        pojo.rxCrcErr(node.get(RX_CRC_ERR).longValue());
        pojo.rxFrameErr(node.get(RX_FRAME_ERR).longValue());
        pojo.rxOverErr(node.get(RX_OVER_ERR).longValue());

        return (MBodyPortStats) pojo.toImmutable();
    }
}
