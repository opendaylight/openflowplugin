/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import org.opendaylight.of.lib.dt.*;
import org.opendaylight.util.packet.PacketWriter;

import java.nio.ByteBuffer;


/**
 * Extends the functionality of {@link PacketWriter} to include the
 * OpenFlow datatypes from {@code org.opendaylight.of.lib.dt}.
 *
 * @author Simon Hunt
 */
public class OfPacketWriter extends PacketWriter {

    /**
     * Constructs the packet write backed by the supplied byte buffer.
     * 
     * @param bb the backing byte buffer buffer
     */
    public OfPacketWriter(ByteBuffer bb) {
        super(bb);
    }

    /** Constructs a packet writer wrapping a newly allocated byte array.
     * Note that the writer index starts at 0.
     *
     * @param length the length of the byte array to create
     */
    public OfPacketWriter(int length) {
        super(length);
    }
    
    /** Writes a group id (u32) to the buffer.
     *
     * @param id the group id to write
     */
    public void write(GroupId id) {
        writeU32(id.toLong());
    }

    /** Writes a buffer id (u32) to the buffer.
     *
     * @param id the buffer id to write
     */
    public void write(BufferId id) {
        writeU32(id.toLong());
    }

    /** Writes a queue id (u32) to the buffer.
     *
     * @param id the queue id to write
     */
    public void write(QueueId id) {
        writeU32(id.toLong());
    }

    /** Writes a meter id (u32) to the buffer.
     *
     * @param id the meter id to write
     */
    public void write(MeterId id) {
        writeU32(id.toLong());
    }

    /** Writes a table id (u8) to the buffer.
     *
     * @param id the table id to write
     */
    public void write(TableId id) {
        writeByte(id.toByte());
    }

    /** Writes a virtual id (u16) to the buffer.
     *
     * @param id the virtual id to write
     */
    public void write(VId id) {
        writeU16(id.toInt());
    }

    /** Writes a datapath id (u64) to the buffer.
     *
     * @param dpid the datapath id to write
     */
    public void write(DataPathId dpid) {
        // TODO: optimize later (intoBuffer like MacAddress).
        writeBytes(dpid.toByteArray());
    }

}
