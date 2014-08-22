/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.controller.ControllerStats;
import org.opendaylight.util.json.JSON;
import org.opendaylight.util.json.JsonFactory;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.json.ControllerStatsCodec.ROOTS;
import static org.opendaylight.util.StringUtils.normalizeEOL;
import static org.opendaylight.util.json.JsonValidator.validate;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;

/**
 * Unit tests for {@link ControllerStatsCodec}.
 *
 * @author Shaila Shree
 */
public class ControllerStatsCodecTest extends AbstractCodecTest {

    private static final String JSON_STATS = "v13/controllerStats";
    private static final String STATS = getJsonContents(JSON_STATS);

    @BeforeClass
    public static void init() {
        JsonFactory factory = new OfJsonFactory();
        JSON.registerFactory(factory);
    }

    @AfterClass
    public static void afterClass() {
        JSON.unregisterFactory(OfJsonFactory.instance());
    }

    @Test
    public void decode() {
        String actual = JSON.toJson(new TestControllerStats(), true);
        assertEquals(AM_NEQ, normalizeEOL(STATS), normalizeEOL(actual));

        // Schema validate
        validate(actual, ROOTS);
    }

    // === Inner ControllerStats class
    private static class TestControllerStats implements ControllerStats {
        private final long duration = 1377194517223L;
        private final long pktIns = 5000;
        private final long pktInBytes = 6000;
        private final long pktOuts = 3000;
        private final long pktOutBytes = 4000;
        private final long pktDrop = 1000;
        private final long pktDropBytes = 6000;
        private final long msgIn = 2000;
        private final long msgOut = 1500;

        @Override public long duration() { return duration; }
        @Override public long packetInCount() { return pktIns; }
        @Override public long packetInBytes() { return pktInBytes; }
        @Override public long packetOutCount() { return pktOuts; }
        @Override public long packetOutBytes() { return pktOutBytes; }
        @Override public long packetDropCount() { return pktDrop; }
        @Override public long packetDropBytes() { return pktDropBytes; }
        @Override public long msgRxCount() { return msgIn; }
        @Override public long msgTxCount() { return msgOut; }

        @Override
        public String toString() {
            return "{ControllerStats:dur=" + duration +
                    "ms,#pktIn=" + pktIns + ",#pktOut=" + pktOuts +
                    ",#pktDrop=" + pktDrop + ",#msgRx=" + msgIn +
                    ",#msgTx=" + msgOut + "}";
        }
    }
}
