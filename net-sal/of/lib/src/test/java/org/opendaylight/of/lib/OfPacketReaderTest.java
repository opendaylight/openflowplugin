/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.of.lib.dt.*;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link OfPacketReader}.
 *
 * @author Simon Hunt
 */
public class OfPacketReaderTest extends OfPacketBufferTest {

    private OfPacketReader pkt;

    private OfPacketReader createPacketReader() {
        return getPacketReader(TF_OF_PACKET);
    }

    @Before
    public void setUp() {
        pkt = createPacketReader();
        assertEquals(AM_NEQ, 0, pkt.ri());
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        print(pkt);
        int magic = pkt.readU16();
        print("Magic : {}", hex(magic));
        assertEquals(AM_NEQ, EXP_MAGIC, magic);

        GroupId gid = pkt.readGroupId();
        print("GrpID: {}", gid);
        assertEquals(AM_NEQ, EXP_GID, gid);

        VId vid = pkt.readVId();
        print("  VID: {}", vid);
        assertEquals(AM_NEQ, EXP_VID, vid);

        DataPathId dpid = pkt.readDataPathId();
        print("  DataPathId: {}", dpid);
        assertEquals(AM_NEQ, EXP_DPID, dpid);

        BufferId bid = pkt.readBufferId();
        print("  Bid: {}", bid);
        assertEquals(AM_NEQ, EXP_BID, bid);

        QueueId qid = pkt.readQueueId();
        print("  Qid: {}", qid);
        assertEquals(AM_NEQ, EXP_QID, qid);

        MeterId mid = pkt.readMeterId();
        print("  Mid: {}", mid);
        assertEquals(AM_NEQ, EXP_MID, mid);

        TableId tid = pkt.readTableId();
        print("  Tid: {}", tid);
        assertEquals(AM_NEQ, EXP_TID, tid);

        pkt.skip(3); // zero fill

        // should be at the end of the buffer
        assertEquals("bytes left over", 0, pkt.readableBytes());
    }

    @Test
    public void basicBuffer() {
        pkt = new OfPacketReader(ByteBuffer.wrap(slurpedBytes(TF_OF_PACKET)));
        basic();
    }

    private static final int SOME_INDEX = 37;

    @Test
    public void indices() {
        pkt = new OfPacketReader(ByteBuffer.wrap(slurpedBytes(TF_OF_PACKET)));
        assertEquals(AM_NEQ, 0, pkt.startIndex());
        assertEquals(AM_NEQ, 0, pkt.targetIndex());

        pkt.startIndex(SOME_INDEX);
        assertEquals(AM_NEQ, SOME_INDEX, pkt.startIndex());

        pkt.targetIndex(SOME_INDEX);
        assertEquals(AM_NEQ, SOME_INDEX, pkt.targetIndex());
    }

}
