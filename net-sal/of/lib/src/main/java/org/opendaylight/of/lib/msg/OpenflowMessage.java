/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;


import org.opendaylight.util.nbio.AbstractMessage;
import org.opendaylight.of.lib.*;
import org.opendaylight.util.ResourceUtils;

import java.util.ResourceBundle;

/**
 * Base class for all OpenFlow messages.
 *
 * @author Simon Hunt
 */
public abstract class OpenflowMessage extends AbstractMessage implements Message {

    /** Minimum length (in bytes) of an OpenFlow message. */
    static final int OFM_HEADER_LEN = 8;

    /** The header for this message. */
    final Header header;

    /**
     * Constructs an OpenFlow message.
     *
     * @param header the message header
     */
    OpenflowMessage(Header header) {
        this.header = header;
    }

    @Override
    public ProtocolVersion getVersion() {
        return header.version;
    }

    @Override
    public MessageType getType() {
        return header.type;
    }

    @Override
    public int length() {
        return header.length;
    }

    @Override
    public long getXid() {
        return header.xid;
    }

    @Override
    public String toString() {
        return "{ofm:" + header + "}";
    }

    /** Returns a string representation useful for debugging.
     * This default implementation delegates to {@link #toString()}, but
     * subclasses are free to override this behavior.
     *
     * @return a (possibly multi-line) string representation of this message
     */
    @Override
    public String toDebugString() {
        return toString();
    }

    /** Validates this message for completeness and throws an exception
     * if the message is considered "not complete".
     * <p>
     * This default implementation does nothing, i.e. default behavior is
     * that messages are considered complete.
     * <p>
     * Subclasses should override this method to check that mandatory
     * fields or other internal state is present, throwing an exception
     * if it is not.
     *
     * @throws IncompleteMessageException if the message is not complete
     */
    public void validate() throws IncompleteMessageException { }

    //========================================================================

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            OpenflowMessage.class, "openflowMessage");

    private static final String E_NULL_PARAM = RES.getString("e_null_param");
    private static final String E_TOO_FEW_BYTES = RES
            .getString("e_too_few_bytes");
    private static final String CANNOT_DECODE_HEADER = "(Cannot decode header)";

    /** A utility method that will attempt to decode the given bytes as an
     * openflow message header, and return a string representation of the
     * result. The array should be at least 8 bytes in length.
     *
     * @param bytes the bytes to decode
     * @return a string representation of the header
     * @throws NullPointerException if bytes is null
     * @throws IllegalArgumentException if the array is too short
     */
    public static String decodeHeader(byte[] bytes) {
        if (bytes == null)
            throw new NullPointerException(E_NULL_PARAM);
        if (bytes.length < OFM_HEADER_LEN)
            throw new IllegalArgumentException(E_TOO_FEW_BYTES);

        OfPacketReader pkt = new OfPacketReader(bytes);
        Header hdr = null;
        try {
            hdr = parseHeader(pkt);
        } catch (DecodeException e) {
            // do nothing
        }
        return hdr != null ? hdr.toString() : CANNOT_DECODE_HEADER;
    }

    /** Parses the header structure from the given data buffer.
     * Note that this method will advance the reader index of the buffer
     * by the length of an OpenFlow header (8 bytes).
     *
     * @param pkt the data buffer
     * @return a parsed header
     * @throws DecodeException if header fields cannot be decoded
     */
    static Header parseHeader(OfPacketReader pkt)
            throws DecodeException {
        Header hdr = new Header();
        byte versionByte = pkt.readByte();
        hdr.version = ProtocolVersion.decode(versionByte);
        hdr.type = MessageType.decode(pkt.readU8(), hdr.version);
        hdr.length = pkt.readU16();
        hdr.xid = pkt.readU32();
        return hdr;
    }

    /** Writes the header structure into the given packet writer.
     * Note that this method will advance the writer index of the buffer
     * by the length of an OpenFlow header (8 bytes).
     *
     * @param hdr the header to write
     * @param pkt the buffer in which the header is to be written
     */
    static void writeHeader(Header hdr, OfPacketWriter pkt) {
        pkt.writeByte(hdr.version.code());
        pkt.writeU8(hdr.type.getCode(hdr.version));
        pkt.writeU16(hdr.length);
        pkt.writeU32(hdr.xid);
    }

    /** Represents the header common to all OpenFlow messages. */
    static class Header {
        /** The version. (u8) */
        ProtocolVersion version;
        /** The message type. (u8) */
        MessageType type;
        /** The message length (in bytes) including the header. (u16) */
        int length;
        /** The transaction id. (u32) */
        long xid;

        // no args constructor (for parsed headers)
        Header() { }

        // constructor (for created headers)
        Header(ProtocolVersion pv, MessageType type, long xid) {
            this.version = pv;
            this.type = type;
            this.length = OFM_HEADER_LEN;
            this.xid = xid;
        }

        // copy constructor
        Header(Header copyMe) {
            this.version = copyMe.version;
            this.type = copyMe.type;
            this.length = copyMe.length;
            this.xid = copyMe.xid;
        }

        @Override
        public String toString() {
            return "[" + version.name() + "," + type + "," + length +
                    "," + xid + "]";
        }
    }


    /** Returns the given long as a string in hex form.
     *
     * @param value the value
     * @return the value in hex form
     */
    protected static String hex(long value) {
        return "0x" + Long.toHexString(value);
    }

    /** Returns the given int as a string in hex form.
     *
     * @param value the value
     * @return the value in hex form
     */
    protected static String hex(int value) {
        return "0x" + Integer.toHexString(value);
    }
}