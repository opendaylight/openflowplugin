package org.openflow.codec.protocol;

import java.util.LinkedList;
import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.factory.OFPInstructionFactory;
import org.openflow.codec.protocol.factory.OFPInstructionFactoryAware;
import org.openflow.codec.protocol.instruction.OFPInstruction;
import org.openflow.codec.util.U16;

/**
 * Represents an ofp_flow_mod message
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 *
 */
public class OFPFlowMod extends OFPMessage implements OFPInstructionFactoryAware, Cloneable {
    public static int MINIMUM_LENGTH = 56;
    public static int MIN_LENGTH_WITHOUT_MATCH = MINIMUM_LENGTH - 8;

    public static final short OFPFC_ADD = 0; /* New flow. */
    public static final short OFPFC_MODIFY = 1; /* Modify all matching flows. */
    public static final short OFPFC_MODIFY_STRICT = 2; /*
                                                        * Modify entry strictly
                                                        * matching wildcards
                                                        */
    public static final short OFPFC_DELETE = 3; /* Delete all matching flows. */
    public static final short OFPFC_DELETE_STRICT = 4; /*
                                                        * Strictly match
                                                        * wildcards and
                                                        * priority.
                                                        */

    private OFPInstructionFactory instructionFactory;
    private long cookie;
    private long cookieMask;
    private byte tableId;
    private OFPFlowModCommand command;
    private short idleTimeout;
    private short hardTimeout;
    private short priority;
    private int bufferId;
    private int outPort;
    private int outGroup;
    private short flags;
    private OFPMatch match;
    private List<OFPInstruction> instructions;

    public OFPFlowMod() {
        super();
        this.type = OFPType.FLOW_MOD;
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
    public OFPFlowMod setBufferId(int bufferId) {
        this.bufferId = bufferId;
        return this;
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
    public OFPFlowMod setCookie(long cookie) {
        this.cookie = cookie;
        return this;
    }

    public long getCookieMask() {
        return cookieMask;
    }

    public void setCookieMask(long cookieMask) {
        this.cookieMask = cookieMask;
    }

    public byte getTableId() {
        return tableId;
    }

    public void setTableId(byte tableId) {
        this.tableId = tableId;
    }

    /**
     * Get command
     *
     * @return
     */
    public OFPFlowModCommand getCommand() {
        return this.command;
    }

    /**
     * Set command
     *
     * @param command
     */
    public OFPFlowMod setCommand(OFPFlowModCommand command) {
        this.command = command;
        return this;
    }

    /**
     * Get flags
     *
     * @return
     */
    public short getFlags() {
        return this.flags;
    }

    /**
     * Set flags
     *
     * @param flags
     */
    public OFPFlowMod setFlags(short flags) {
        this.flags = flags;
        return this;
    }

    /**
     * Get hard_timeout
     *
     * @return
     */
    public short getHardTimeout() {
        return this.hardTimeout;
    }

    /**
     * Set hard_timeout
     *
     * @param hardTimeout
     */
    public OFPFlowMod setHardTimeout(short hardTimeout) {
        this.hardTimeout = hardTimeout;
        return this;
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
    public OFPFlowMod setIdleTimeout(short idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
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
    public OFPFlowMod setMatch(OFPMatch match) {
        this.match = match;
        return this;
    }

    /**
     * Get out_port
     *
     * @return
     */
    public int getOutPort() {
        return this.outPort;
    }

    /**
     * Set out_port
     *
     * @param outPort
     */
    public OFPFlowMod setOutPort(int outPort) {
        this.outPort = outPort;
        return this;
    }

    /**
     * Set out_port
     *
     * @param port
     */
    public OFPFlowMod setOutPort(OFPPortNo port) {
        this.outPort = port.getValue();
        return this;
    }

    public int getOutGroup() {
        return outGroup;
    }

    public void setOutGroup(int outGroup) {
        this.outGroup = outGroup;
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
    public OFPFlowMod setPriority(short priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Returns instructions contained in this Flow Mod
     *
     * @return a list of ordered OFPInstruction objects
     */
    public List<OFPInstruction> getInstructions() {
        return this.instructions;
    }

    /**
     * Sets the list of instruction this Flow Mod contains
     *
     * @param instruction
     *            a list of ordered OFPInstruction objects
     */
    public OFPFlowMod setInstructions(List<OFPInstruction> instructions) {
        this.instructions = instructions;
        return this;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.cookie = data.getLong();
        this.cookieMask = data.getLong();
        this.tableId = data.get();
        this.command = OFPFlowModCommand.valueOf(data.get());
        this.idleTimeout = data.getShort();
        this.hardTimeout = data.getShort();
        this.priority = data.getShort();
        this.bufferId = data.getInt();
        this.outPort = data.getInt();
        this.outGroup = data.getInt();
        this.flags = data.getShort();
        data.getShort(); // pad
        if (this.match == null)
            this.match = new OFPMatch();
        this.match.readFrom(data);
        if (this.instructionFactory == null)
            throw new RuntimeException("OFPInstructionFactory is not set");
        int instructionLength = getLengthU() - MIN_LENGTH_WITHOUT_MATCH - match.getLengthWithPadding();
        this.instructions = this.instructionFactory.parseInstructions(data, instructionLength);
    }

    @Override
    public void writeTo(IDataBuffer data) {
        length = getLength();
        super.writeTo(data);
        data.putLong(cookie);
        data.putLong(cookieMask);
        data.put(tableId);
        data.put(command.getValue());
        data.putShort(idleTimeout);
        data.putShort(hardTimeout);
        data.putShort(priority);
        data.putInt(bufferId);
        data.putInt(outPort);
        data.putInt(outGroup);
        data.putShort(flags);
        data.putShort((short) 0);
        this.match.writeTo(data);
        if (instructions != null) {
            for (OFPInstruction instr : instructions) {
                instr.writeTo(data);
            }
        }
    }

    /**
     * get length based on match and instruction length
     */
    public short getLength() {
        int totalLength = MIN_LENGTH_WITHOUT_MATCH;
        totalLength += this.match.getLengthWithPadding();
        if (instructions != null) {
            for (OFPInstruction instr : instructions) {
                totalLength += instr.getLength();
            }
        }
        length = U16.t(totalLength);
        return length;
    }

    @Override
    public int hashCode() {
        final int prime = 227;
        int result = super.hashCode();
        result = prime * result + ((instructions == null) ? 0 : instructions.hashCode());
        result = prime * result + bufferId;
        result = prime * result + command.getValue();
        result = prime * result + (int) (cookie ^ (cookie >>> 32));
        result = prime * result + (int) (cookieMask ^ (cookieMask >>> 32));
        result = prime * result + tableId;
        result = prime * result + flags;
        result = prime * result + hardTimeout;
        result = prime * result + idleTimeout;
        result = prime * result + ((match == null) ? 0 : match.hashCode());
        result = prime * result + outPort;
        result = prime * result + outGroup;
        result = prime * result + priority;
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
        if (!(obj instanceof OFPFlowMod)) {
            return false;
        }
        OFPFlowMod other = (OFPFlowMod) obj;
        if (instructions == null) {
            if (other.instructions != null) {
                return false;
            }
        } else if (!instructions.equals(other.instructions)) {
            return false;
        }
        if (bufferId != other.bufferId) {
            return false;
        }
        if (command != other.command) {
            return false;
        }
        if (cookie != other.cookie) {
            return false;
        }
        if (cookieMask != other.cookieMask) {
            return false;
        }
        if (tableId != other.tableId) {
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
        if (match == null) {
            if (other.match != null) {
                return false;
            }
        } else if (!match.equals(other.match)) {
            return false;
        }
        if (outPort != other.outPort) {
            return false;
        }
        if (outGroup != other.outGroup) {
            return false;
        }
        if (priority != other.priority) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public OFPFlowMod clone() {
        try {
            OFPMatch neoMatch = match.clone();
            OFPFlowMod flowMod = (OFPFlowMod) super.clone();
            flowMod.setMatch(neoMatch);
            List<OFPInstruction> instrList = new LinkedList<OFPInstruction>();
            for (OFPInstruction instr : this.instructions)
                instrList.add((OFPInstruction) instr.clone());
            flowMod.setInstructions(instrList);
            return flowMod;
        } catch (CloneNotSupportedException e) {
            // Won't happen
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFPFlowMod [instructionFactory=" + instructionFactory + ", instructions=" + instructions
                + ", bufferId=" + bufferId + ", command=" + command + ", cookie=" + cookie + ", cookieMask="
                + cookieMask + ", tableId=" + tableId + ", flags=" + flags + ", hardTimeout=" + hardTimeout
                + ", idleTimeout=" + idleTimeout + ", match=" + match + ", outPort=" + outPort + ", outGroup="
                + outGroup + ", priority=" + priority + ", length=" + length + ", type=" + type + ", version="
                + version + ", xid=" + xid + "]";
    }

    @Override
    public void setInstructionFactory(OFPInstructionFactory instructionFactory) {
        this.instructionFactory = instructionFactory;

    }
}
