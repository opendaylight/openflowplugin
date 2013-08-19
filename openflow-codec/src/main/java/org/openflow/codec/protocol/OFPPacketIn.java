package org.openflow.codec.protocol;

import java.util.Arrays;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.U16;
import org.openflow.codec.util.U8;

/**
 * Represents an ofp_packet_in
 *
 * @author David Erickson (daviderickson@cs.stanford.edu) - Feb 8, 2010
 * @author AnilGujele
 */
public class OFPPacketIn extends OFPMessage {
    public static int MINIMUM_LENGTH = 32;
    public static int MINIMUM_LENGTH_WITHOUT_MATCH = MINIMUM_LENGTH - 8;

    public enum OFPacketInReason {
        NO_MATCH, ACTION, INVALID_TTL
    }

    protected int bufferId;
    protected short totalLength;
    protected OFPacketInReason reason;
    protected byte tableId;
    protected long cookie;
    protected OFPMatch match;
    protected byte[] packetData;

    public OFPPacketIn() {
        super();
        this.type = OFPType.PACKET_IN;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    /**
     * Get buffer_id
     *
     * @return
     */
    public int getBufferId() {
        return this.bufferId;
    }

    /**
     * Set buffer_id
     *
     * @param bufferId
     */
    public OFPPacketIn setBufferId(int bufferId) {
        this.bufferId = bufferId;
        return this;
    }

    /**
     * Returns the packet data
     *
     * @return
     */
    public byte[] getPacketData() {
        return this.packetData;
    }

    /**
     * Sets the packet data, and updates the length of this message
     *
     * @param packetData
     */
    public OFPPacketIn setPacketData(byte[] packetData) {
        this.packetData = packetData;
        updateLength();
        return this;
    }

    private void updateLength() {
        short matchLength = (null == match) ? 0 : match.getLengthWithPadding();
        int packetDataLength = (null == packetData) ? 0 : packetData.length;
        int len = OFPPacketIn.MINIMUM_LENGTH_WITHOUT_MATCH + matchLength + packetDataLength;
        this.length = (short) len;
    }

    /**
     * Get reason
     *
     * @return
     */
    public OFPacketInReason getReason() {
        return this.reason;
    }

    /**
     * Set reason
     *
     * @param reason
     */
    public OFPPacketIn setReason(OFPacketInReason reason) {
        this.reason = reason;
        return this;
    }

    /**
     * get table id
     *
     * @return
     */
    public byte getTableId() {
        return tableId;
    }

    /**
     * set table id
     *
     * @param tableId
     */
    public void setTableId(byte tableId) {
        this.tableId = tableId;
    }

    /**
     * get cookie
     *
     * @return
     */
    public long getCookie() {
        return cookie;
    }

    /**
     * set cookie
     *
     * @param cookie
     */
    public void setCookie(long cookie) {
        this.cookie = cookie;
    }

    /**
     * get match
     *
     * @return
     */
    public OFPMatch getMatch() {
        return match;
    }

    /**
     * set match
     *
     * @param match
     */
    public void setMatch(OFPMatch match) {
        this.match = match;
        updateLength();
    }

    /**
     * Get total_len
     *
     * @return
     */
    public short getTotalLength() {
        return this.totalLength;
    }

    /**
     * Set total_len
     *
     * @param totalLength
     */
    public OFPPacketIn setTotalLength(short totalLength) {
        this.totalLength = totalLength;
        return this;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.bufferId = data.getInt();
        this.totalLength = data.getShort();
        this.reason = OFPacketInReason.values()[U8.f(data.get())];
        this.tableId = data.get();
        this.cookie = data.getLong();
        this.match = new OFPMatch();
        match.readFrom(data);
        // TBD - how to handle the padding here
        data.getShort(); // pad
        this.packetData = new byte[getLengthU() - OFPPacketIn.MINIMUM_LENGTH_WITHOUT_MATCH
                - match.getLengthWithPadding()];
        data.get(this.packetData);
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putInt(bufferId);
        data.putShort(totalLength);
        data.put((byte) reason.ordinal());
        data.put(tableId);
        data.putLong(cookie);
        if (null != match) {
            match.writeTo(data);
        }
        data.putShort((short) 0); // pad
        data.put(this.packetData);
    }

    @Override
    public int hashCode() {
        final int prime = 283;
        int result = super.hashCode();
        result = prime * result + bufferId;
        result = prime * result + tableId;
        result = prime * result + (int) (cookie ^ (cookie >>> 32));
        result = prime * result + ((null == match) ? 0 : match.hashCode());
        result = prime * result + Arrays.hashCode(packetData);
        result = prime * result + ((reason == null) ? 0 : reason.hashCode());
        result = prime * result + totalLength;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OFPPacketIn)) {
            return false;
        }
        OFPPacketIn other = (OFPPacketIn) obj;
        if (bufferId != other.bufferId) {
            return false;
        }
        if (tableId != other.tableId) {
            return false;
        }
        if (match == null) {
            if (other.match != null) {
                return false;
            }
        } else if (!match.equals(other.match)) {
            return false;
        }
        if (!Arrays.equals(packetData, other.packetData)) {
            return false;
        }
        if (reason == null) {
            if (other.reason != null) {
                return false;
            }
        } else if (!reason.equals(other.reason)) {
            return false;
        }
        if (totalLength != other.totalLength) {
            return false;
        }
        return true;
    }
}
