/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;


import org.opendaylight.of.lib.dt.*;
import org.opendaylight.util.packet.PacketReader;

import java.nio.ByteBuffer;


/**
 * Extends the functionality of {@link PacketReader} to include the
 * OpenFlow datatypes from {@code org.opendaylight.of.lib.dt}.
 *
 * @author Simon Hunt
 */
public class OfPacketReader extends PacketReader {

    private int startIndex;
    private int targetIndex;

    /**
     * Constructs a packet reader backed by the specified byte buffer.
     *
     * @param bb the byte buffer to read from
     */
    public OfPacketReader(ByteBuffer bb) {
        super(bb);
    }

    /**
     * Annotates this packet reader with the start index.
     *
     * @param index the start index
     */
    public void startIndex(int index) {
        startIndex = index;
    }

    /**
     * Returns the annotated start index.
     *
     * @return the start index
     */
    public int startIndex() {
        return startIndex;
    }

    /**
     * Annotates this packet reader with the target index.
     *
     * @param index the target index
     */
    public void targetIndex(int index) {
        targetIndex = index;
    }

    /**
     * Returns the annotated target index.
     *
     * @return the target index
     */
    public int targetIndex() {
        return targetIndex;
    }

    /**
     * Constructs a packet reader backed by the specified byte buffer.
     *
     * @param bytes the bytes to wrap
     */
    public OfPacketReader(byte[] bytes) {
        super(ByteBuffer.wrap(bytes));
    }

    /** Reads a (u32) group id from the buffer.
     *
     * @return a group id
     */
    public GroupId readGroupId() {
        long id = readU32();
        return GroupId.valueOf(id);
    }

    /** Reads a (u32) buffer id from the buffer.
     *
     * @return a buffer id
     */
    public BufferId readBufferId() {
        long id = readU32();
        return BufferId.valueOf(id);
    }

    /** Reads a (u32) queue id from the buffer.
     *
     * @return a queue id
     */
    public QueueId readQueueId() {
        long id = readU32();
        return QueueId.valueOf(id);
    }

    /** Reads a (u32) meter id from the buffer.
     *
     * @return a meter id
     */
    public MeterId readMeterId() {
        long id = readU32();
        return MeterId.valueOf(id);
    }

    /** Reads a (u8) table id from the buffer.
     *
     * @return a table id
     */
    public TableId readTableId() {
        short id = readU8();
        return TableId.valueOf(id);
    }

    /** Reads a virtual identifier (u16) from the buffer.
     *
     * @return a virtual identifier
     */
    public VId readVId() {
        int value = readU16();
        return VId.valueOf(value);
    }

    /** Reads a (u32) datapath id from the buffer.
     *
     * @return a datapath id
     */
    public DataPathId readDataPathId() {
        // TODO: Optimize later (valueFrom like MacAddress).
        byte[] bytes = readBytes(DataPathId.LENGTH_IN_BYTES);
        return DataPathId.valueOf(bytes);
    }

}
