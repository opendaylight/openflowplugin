/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendaylight.of.controller.ControllerStats;
import org.opendaylight.util.json.AbstractJsonCodec;

/**
 * A JSON codec capable of encoding and decoding {@link ControllerStats} objects.
 *
 * @author Shaila Shree
 */
public class ControllerStatsCodec extends AbstractJsonCodec<ControllerStats> {
    public static final String ROOTS = "controller_stats";
    private static final String DURATION_MS = "duration_ms";
    private static final String PKT_IN = "packet_in";
    private static final String BYTES = "bytes";
    private static final String PKTS = "packets";
    private static final String PKT_OUT = "packet_out";
    private static final String LOST = "lost";
    private static final String MSG_IN = "msg_in";
    private static final String MSG_OUT = "msg_out";

    public ControllerStatsCodec() {
        super(ROOTS, ROOTS);
    }

    @Override
    public ObjectNode encode(ControllerStats pojo) {
        ObjectNode stats = objectNode();

        stats.put(DURATION_MS, pojo.duration());

        ObjectNode pktIn = objectNode();
        pktIn.put(PKTS, pojo.packetInCount());
        pktIn.put(BYTES, pojo.packetInBytes());

        stats.put(PKT_IN, pktIn);

        ObjectNode pktOut = objectNode();
        pktOut.put(PKTS, pojo.packetOutCount());
        pktOut.put(BYTES, pojo.packetOutBytes());

        stats.put(PKT_OUT, pktOut);

        ObjectNode lost = objectNode();
        lost.put(PKTS, pojo.packetDropCount());
        lost.put(BYTES, pojo.packetDropBytes());

        stats.put(LOST, lost);

        stats.put(MSG_IN, pojo.msgRxCount());
        stats.put(MSG_OUT, pojo.msgTxCount());

        return stats;
    }

    @Override
    public ControllerStats decode(ObjectNode node) {
        return new JsonControllerStats(node);
    }
    
    // Json DTO
    private static class JsonControllerStats implements ControllerStats {
        private ObjectNode node;
        
        public JsonControllerStats(ObjectNode node) {
            this.node = node;
        }

        @Override
        public long duration() {
            return longVal(node, DURATION_MS);
        }

        @Override
        public long packetInCount() {
            return longVal(node.get(PKT_IN), PKTS);
        }

        @Override
        public long packetInBytes() {
            return longVal(node.get(PKT_IN), BYTES);
        }

        @Override
        public long packetOutCount() {
            return longVal(node.get(PKT_OUT), PKTS);
        }

        @Override
        public long packetOutBytes() {
            return longVal(node.get(PKT_OUT), BYTES);
        }

        @Override
        public long packetDropCount() {
            return longVal(node.get(LOST), PKTS);
        }

        @Override
        public long packetDropBytes() {
            return longVal(node.get(LOST), BYTES);
        }

        @Override
        public long msgRxCount() {
            return longVal(node, MSG_IN);
        }

        @Override
        public long msgTxCount() {
            return longVal(node, MSG_OUT);
        }
        
    }

}
