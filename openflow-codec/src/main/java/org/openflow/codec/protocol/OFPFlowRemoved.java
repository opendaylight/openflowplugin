package org.openflow.codec.protocol;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.U16;

/**
 * Represents an ofp_flow_removed message
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author AnilGujele
 *
 */
public class OFPFlowRemoved extends OFPMessage {
    public static final int MINIMUM_LENGTH = 56;
    public static final int MATCH_DEFAULT_LENGTH = 8;
    public static final int MINIMUM_LENGTH_WITHOUT_MATCH = MINIMUM_LENGTH - MATCH_DEFAULT_LENGTH;

    /**
     * correspond to enum ofp_flow_removed_reason
     *
     * @author AnilGujele
     *
     */
    public enum OFFlowRemovedReason {
        OFPRR_IDLE_TIMEOUT, OFPRR_HARD_TIMEOUT, OFPRR_DELETE, OFPRR_GROUP_DELETE
    }

    protected long cookie;
    protected short priority;
    protected OFFlowRemovedReason reason;
    protected byte tableId;
    protected int durationSeconds;
    protected int durationNanoseconds;
    protected short idleTimeout;
    protected short hardTimeout;
    protected long packetCount;
    protected long byteCount;
    protected OFPMatch match;

    public OFPFlowRemoved() {
        super();
        this.type = OFPType.FLOW_REMOVED;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    /**
     * Get cookie
     *
     * @return
     */
    public long getCookie() {
        return this.cookie;
    }

    /**
     * Set cookie
     *
     * @param cookie
     */
    public void setCookie(long cookie) {
        this.cookie = cookie;
    }

    /**
     * Get idle_timeout
     *
     * @return
     */
    public short getIdleTimeout() {
        return this.idleTimeout;
    }

    /**
     * Set idle_timeout
     *
     * @param idleTimeout
     */
    public void setIdleTimeout(short idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    /**
     * get hard timeout
     *
     * @return
     */
    public short getHardTimeout() {
        return hardTimeout;
    }

    /**
     * set hard timeout
     *
     * @param hardTimeout
     */
    public void setHardTimeout(short hardTimeout) {
        this.hardTimeout = hardTimeout;
    }

    /**
     * Gets a copy of the OFPMatch object for this FlowMod, changes to this
     * object do not modify the FlowMod
     *
     * @return
     */
    public OFPMatch getMatch() {
        return this.match;
    }

    /**
     * Set match
     *
     * @param match
     */
    public void setMatch(OFPMatch match) {
        this.match = match;
        updateLength();
    }

    private void updateLength() {
        int matchLength = (null == this.match) ? MATCH_DEFAULT_LENGTH : this.match.getLengthWithPadding();
        this.length = (short) (OFPFlowRemoved.MINIMUM_LENGTH_WITHOUT_MATCH + matchLength);
    }

    /**
     * Get priority
     *
     * @return
     */
    public short getPriority() {
        return this.priority;
    }

    /**
     * Set priority
     *
     * @param priority
     */
    public void setPriority(short priority) {
        this.priority = priority;
    }

    /**
     * @return the reason
     */
    public OFFlowRemovedReason getReason() {
        return reason;
    }

    /**
     * @param reason
     *            the reason to set
     */
    public void setReason(OFFlowRemovedReason reason) {
        this.reason = reason;
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
     * @return the durationSeconds
     */
    public int getDurationSeconds() {
        return durationSeconds;
    }

    /**
     * @param durationSeconds
     *            the durationSeconds to set
     */
    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    /**
     * @return the durationNanoseconds
     */
    public int getDurationNanoseconds() {
        return durationNanoseconds;
    }

    /**
     * @param durationNanoseconds
     *            the durationNanoseconds to set
     */
    public void setDurationNanoseconds(int durationNanoseconds) {
        this.durationNanoseconds = durationNanoseconds;
    }

    /**
     * @return the packetCount
     */
    public long getPacketCount() {
        return packetCount;
    }

    /**
     * @param packetCount
     *            the packetCount to set
     */
    public void setPacketCount(long packetCount) {
        this.packetCount = packetCount;
    }

    /**
     * @return the byteCount
     */
    public long getByteCount() {
        return byteCount;
    }

    /**
     * @param byteCount
     *            the byteCount to set
     */
    public void setByteCount(long byteCount) {
        this.byteCount = byteCount;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.cookie = data.getLong();
        this.priority = data.getShort();
        this.reason = OFFlowRemovedReason.values()[(0xff & data.get())];
        this.tableId = data.get();
        this.durationSeconds = data.getInt();
        this.durationNanoseconds = data.getInt();
        this.idleTimeout = data.getShort();
        this.hardTimeout = data.getShort();
        this.packetCount = data.getLong();
        this.byteCount = data.getLong();
        if (this.match == null)
            this.match = new OFPMatch();
        this.match.readFrom(data);
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putLong(cookie);
        data.putShort(priority);
        data.put((byte) this.reason.ordinal());
        data.put((byte) tableId);
        data.putInt(this.durationSeconds);
        data.putInt(this.durationNanoseconds);
        data.putShort(idleTimeout);
        data.putShort(hardTimeout);
        data.putLong(this.packetCount);
        data.putLong(this.byteCount);
        this.match.writeTo(data);
    }

    @Override
    public int hashCode() {
        final int prime = 271;
        int result = super.hashCode();
        result = prime * result + (int) (byteCount ^ (byteCount >>> 32));
        result = prime * result + (int) (cookie ^ (cookie >>> 32));
        result = prime * result + durationNanoseconds;
        result = prime * result + durationSeconds;
        result = prime * result + idleTimeout;
        result = prime * result + hardTimeout;
        result = prime * result + ((match == null) ? 0 : match.hashCode());
        result = prime * result + (int) (packetCount ^ (packetCount >>> 32));
        result = prime * result + priority;
        result = prime * result + tableId;
        result = prime * result + ((reason == null) ? 0 : reason.hashCode());
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
        if (!(obj instanceof OFPFlowRemoved)) {
            return false;
        }
        OFPFlowRemoved other = (OFPFlowRemoved) obj;
        if (byteCount != other.byteCount) {
            return false;
        }
        if (cookie != other.cookie) {
            return false;
        }
        if (durationNanoseconds != other.durationNanoseconds) {
            return false;
        }
        if (durationSeconds != other.durationSeconds) {
            return false;
        }
        if (idleTimeout != other.idleTimeout) {
            return false;
        }
        if (hardTimeout != other.hardTimeout) {
            return false;
        }

        if (match == null) {
            if (other.match != null) {
                return false;
            }
        } else if (!match.equals(other.match)) {
            return false;
        }
        if (packetCount != other.packetCount) {
            return false;
        }
        if (priority != other.priority) {
            return false;
        }
        if (tableId != other.tableId) {
            return false;
        }
        if (reason == null) {
            if (other.reason != null) {
                return false;
            }
        } else if (!reason.equals(other.reason)) {
            return false;
        }
        return true;
    }
}
