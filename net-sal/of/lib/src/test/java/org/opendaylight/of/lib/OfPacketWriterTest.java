/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import org.junit.Test;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.util.net.U16Id;
import org.opendaylight.util.net.U32Id;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.opendaylight.util.ByteUtils.toHexArrayString;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link OfPacketWriter}.
 *
 * @author Simon Hunt
 */
public class OfPacketWriterTest extends OfPacketBufferTest {

    private OfPacketWriter pkt;

    private void printByteArray(String s, Object v) {
        print("{}: {} --> {}", s, v, toHexArrayString(pkt.array()));
    }

    @Test
    public void wrGroupId() {
        print(EOL + "wrGroupId()");
        pkt = new OfPacketWriter(U32Id.LENGTH_IN_BYTES);
        pkt.write(EXP_GID);
        printByteArray("GroupId", EXP_GID);
        assertArrayEquals(AM_NEQ, EXP_GID_ARRAY, pkt.array());
    }

    @Test
    public void wrBufferId() {
        print(EOL + "wrBufferId()");
        pkt = new OfPacketWriter(U32Id.LENGTH_IN_BYTES);
        pkt.write(EXP_BID);
        printByteArray("BufferId", EXP_BID);
        assertArrayEquals(AM_NEQ, EXP_BID_ARRAY, pkt.array());
    }

    @Test
    public void wrQueueId() {
        print(EOL + "wrQueueId()");
        pkt = new OfPacketWriter(U32Id.LENGTH_IN_BYTES);
        pkt.write(EXP_QID);
        printByteArray("QueueId", EXP_QID);
        assertArrayEquals(AM_NEQ, EXP_QID_ARRAY, pkt.array());
    }

    @Test
    public void wrMeterId() {
        print(EOL + "wrMeterId()");
        pkt = new OfPacketWriter(U32Id.LENGTH_IN_BYTES);
        pkt.write(EXP_MID);
        printByteArray("MeterId", EXP_MID);
        assertArrayEquals(AM_NEQ, EXP_MID_ARRAY, pkt.array());
    }

    @Test
    public void wrTableId() {
        print(EOL + "wrTableId()");
        pkt = new OfPacketWriter(1);
        pkt.write(EXP_TID);
        printByteArray("TableId", EXP_TID);
        assertArrayEquals(AM_NEQ, EXP_TID_ARRAY, pkt.array());
    }

    @Test
    public void wrVId() {
        print(EOL + "wrVId()");
        pkt = new OfPacketWriter(U16Id.LENGTH_IN_BYTES);
        pkt.write(EXP_VID);
        printByteArray("VId", EXP_VID);
        assertArrayEquals(AM_NEQ, EXP_VID_ARRAY, pkt.array());
    }

    @Test
    public void wrDataPathId() {
        print(EOL + "wrDataPathId()");
        pkt = new OfPacketWriter(DataPathId.LENGTH_IN_BYTES);
        pkt.write(EXP_DPID);
        printByteArray("DataPathId", EXP_DPID);
        assertArrayEquals(AM_NEQ, EXP_DPID_ARRAY, pkt.array());
    }

    @Test
    public void duplicatePseudoPacket() {
        print(EOL + "duplicatePseudoPacket()");
        byte[] expData = slurpedBytes(TF_OF_PACKET);

        pkt = new OfPacketWriter(expData.length);

        pkt.writeU16(EXP_MAGIC);
        pkt.write(EXP_GID);
        pkt.write(EXP_VID);
        pkt.write(EXP_DPID);
        pkt.write(EXP_BID);
        pkt.write(EXP_QID);
        pkt.write(EXP_MID);
        pkt.write(EXP_TID);
        pkt.writeZeros(3); // padding
        // We should be done
        print("Pseudo Packet: {}", toHexArrayString(pkt.array()));

        assertEquals(AM_NEQ, 0, pkt.writableBytes());
        assertArrayEquals(AM_NEQ, expData, pkt.array());
    }

}
