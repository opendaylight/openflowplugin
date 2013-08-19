package org.openflow.codec.protocol.statistics;

import java.io.Serializable;
import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFPMatch;
import org.openflow.codec.protocol.factory.OFPInstructionFactory;
import org.openflow.codec.protocol.factory.OFPInstructionFactoryAware;
import org.openflow.codec.protocol.instruction.OFPInstruction;
import org.openflow.codec.util.U16;

/**
 * Represents an ofp_flow_stats structure
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author AnilGujele
 */
public class OFPFlowStatisticsReply implements OFPStatistics, OFPInstructionFactoryAware, Serializable {
    public static final int MINIMUM_LENGTH = 56;
    private static final int MIN_LENGTH_WITHOUT_MATCH = MINIMUM_LENGTH - 8;

    protected transient OFPInstructionFactory instructionFactory;
    protected short length = (short) MINIMUM_LENGTH;
    protected byte tableId;
    protected int durationSeconds;
    protected int durationNanoseconds;
    protected short priority;
    protected short idleTimeout;
    protected short hardTimeout;
    protected short flags;
    protected long cookie;
    protected long packetCount;
    protected long byteCount;
    protected OFPMatch match;
    private List<OFPInstruction> instructions;

    /**
     * @return the tableId
     */
    public byte getTableId() {
        return tableId;
    }

    /**
     * @param tableId
     *            the tableId to set
     */
    public void setTableId(byte tableId) {
        this.tableId = tableId;
    }

    /**
     * @return the match
     */
    public OFPMatch getMatch() {
        return match;
    }

    /**
     * @param match
     *            the match to set
     */
    public void setMatch(OFPMatch match) {
        this.match = match;
    }

    /**
     *
     * @return
     */
    public List<OFPInstruction> getInstructions() {
        return instructions;
    }

    /**
     *
     * @param instructions
     */
    public void setInstructions(List<OFPInstruction> instructions) {
        this.instructions = instructions;
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
     * @return the priority
     */
    public short getPriority() {
        return priority;
    }

    /**
     * @param priority
     *            the priority to set
     */
    public void setPriority(short priority) {
        this.priority = priority;
    }

    /**
     * @return the idleTimeout
     */
    public short getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * @param idleTimeout
     *            the idleTimeout to set
     */
    public void setIdleTimeout(short idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    /**
     * @return the hardTimeout
     */
    public short getHardTimeout() {
        return hardTimeout;
    }

    /**
     * @param hardTimeout
     *            the hardTimeout to set
     */
    public void setHardTimeout(short hardTimeout) {
        this.hardTimeout = hardTimeout;
    }

    /**
     *
     * @return
     */
    public short getFlags() {
        return flags;
    }

    /**
     *
     * @param flags
     */
    public void setFlags(short flags) {
        this.flags = flags;
    }

    /**
     * @return the cookie
     */
    public long getCookie() {
        return cookie;
    }

    /**
     * @param cookie
     *            the cookie to set
     */
    public void setCookie(long cookie) {
        this.cookie = cookie;
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

    /**
     * @param length
     *            the length to set
     */
    public void setLength(short length) {
        this.length = length;
    }

    @Override
    public int getLength() {
        return U16.f(length);
    }

    @Override
    public void readFrom(IDataBuffer data) {
        this.length = data.getShort();
        this.tableId = data.get();
        data.get(); // pad
        this.durationSeconds = data.getInt();
        this.durationNanoseconds = data.getInt();
        this.priority = data.getShort();
        this.idleTimeout = data.getShort();
        this.hardTimeout = data.getShort();
        this.flags = data.getShort();
        data.getInt(); // pad
        this.cookie = data.getLong();
        this.packetCount = data.getLong();
        this.byteCount = data.getLong();
        if (this.match == null)
            this.match = new OFPMatch();
        this.match.readFrom(data);
        if (null == this.instructionFactory)
            throw new RuntimeException("OFPInstructionFactory is not set");
        int instrLength = getLength() - MIN_LENGTH_WITHOUT_MATCH - match.getLengthWithPadding();
        this.instructions = this.instructionFactory.parseInstructions(data, instrLength);
    }

    @Override
    public void writeTo(IDataBuffer data) {
        data.putShort(this.length);
        data.put(this.tableId);
        data.put((byte) 0); // pad
        data.putInt(this.durationSeconds);
        data.putInt(this.durationNanoseconds);
        data.putShort(this.priority);
        data.putShort(this.idleTimeout);
        data.putShort(this.hardTimeout);
        data.putShort(this.flags);
        data.putInt(0); // pad
        data.putLong(this.cookie);
        data.putLong(this.packetCount);
        data.putLong(this.byteCount);
        this.match.writeTo(data);
        if (instructions != null) {
            for (OFPInstruction instr : instructions) {
                instr.writeTo(data);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 419;
        int result = 1;
        result = prime * result + (int) (byteCount ^ (byteCount >>> 32));
        result = prime * result + (int) (cookie ^ (cookie >>> 32));
        result = prime * result + durationNanoseconds;
        result = prime * result + durationSeconds;
        result = prime * result + flags;
        result = prime * result + hardTimeout;
        result = prime * result + idleTimeout;
        result = prime * result + length;
        result = prime * result + ((match == null) ? 0 : match.hashCode());
        result = prime * result + ((instructions == null) ? 0 : instructions.hashCode());
        result = prime * result + (int) (packetCount ^ (packetCount >>> 32));
        result = prime * result + priority;
        result = prime * result + tableId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OFPFlowStatisticsReply)) {
            return false;
        }
        OFPFlowStatisticsReply other = (OFPFlowStatisticsReply) obj;
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
        if (flags != other.flags) {
            return false;
        }
        if (hardTimeout != other.hardTimeout) {
            return false;
        }
        if (idleTimeout != other.idleTimeout) {
            return false;
        }
        if (length != other.length) {
            return false;
        }
        if (match == null) {
            if (other.match != null) {
                return false;
            }
        } else if (!match.equals(other.match)) {
            return false;
        }
        if (instructions == null) {
            if (other.instructions != null) {
                return false;
            }
        } else if (!instructions.equals(other.instructions)) {
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
        return true;
    }

    @Override
    public void setInstructionFactory(OFPInstructionFactory instructionFactory) {
        this.instructionFactory = instructionFactory;

    }
}
