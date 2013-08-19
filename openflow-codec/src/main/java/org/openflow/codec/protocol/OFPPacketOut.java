package org.openflow.codec.protocol;

import java.util.Arrays;
import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.action.OFPAction;
import org.openflow.codec.protocol.factory.OFPActionFactory;
import org.openflow.codec.protocol.factory.OFPActionFactoryAware;
import org.openflow.codec.util.U16;

/**
 * Represents an ofp_packet_out message
 *
 * @author David Erickson (daviderickson@cs.stanford.edu) - Mar 12, 2010
 */
public class OFPPacketOut extends OFPMessage implements OFPActionFactoryAware {
    public static int MINIMUM_LENGTH = 24;
    public static int BUFFER_ID_NONE = 0xffffffff;

    protected OFPActionFactory actionFactory;
    protected int bufferId;
    protected int inPort;
    protected short actionsLength;
    protected List<OFPAction> actions;
    protected byte[] packetData;

    public OFPPacketOut() {
        super();
        this.type = OFPType.PACKET_OUT;
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
    public OFPPacketOut setBufferId(int bufferId) {
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
     * Sets the packet data
     *
     * @param packetData
     */
    public OFPPacketOut setPacketData(byte[] packetData) {
        this.packetData = packetData;
        updateLength();
        return this;
    }

    private void updateLength() {
        short newLength = (short) ((packetData == null) ? 0 : packetData.length);
        this.length = newLength;
    }

    /**
     * Get in_port
     *
     * @return
     */
    public int getInPort() {
        return this.inPort;
    }

    /**
     * Set in_port
     *
     * @param inPort
     */
    public OFPPacketOut setInPort(int inPort) {
        this.inPort = inPort;
        return this;
    }

    /**
     * Set in_port. Convenience method using OFPPort enum.
     *
     * @param inPort
     */
    public OFPPacketOut setInPort(OFPPortNo inPort) {
        this.inPort = inPort.getValue();
        return this;
    }

    /**
     * Get actions_len
     *
     * @return
     */
    public short getActionsLength() {
        return this.actionsLength;
    }

    /**
     * Get actions_len, unsigned
     *
     * @return
     */
    public int getActionsLengthU() {
        return U16.f(this.actionsLength);
    }

    /**
     * Set actions_len
     *
     * @param actionsLength
     */
    public OFPPacketOut setActionsLength(short actionsLength) {
        this.actionsLength = actionsLength;
        return this;
    }

    /**
     * Returns the actions contained in this message
     *
     * @return a list of ordered OFPAction objects
     */
    public List<OFPAction> getActions() {
        return this.actions;
    }

    /**
     * Sets the list of actions on this message
     *
     * @param actions
     *            a list of ordered OFPAction objects
     */
    public OFPPacketOut setActions(List<OFPAction> actions) {
        this.actions = actions;
        return this;
    }

    @Override
    public void setActionFactory(OFPActionFactory actionFactory) {
        this.actionFactory = actionFactory;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.bufferId = data.getInt();
        this.inPort = data.getInt();
        this.actionsLength = data.getShort();
        data.getInt(); // pad
        data.getShort(); // pad
        if (this.actionFactory == null)
            throw new RuntimeException("ActionFactory not set");
        this.actions = this.actionFactory.parseActions(data, getActionsLengthU());
        this.packetData = new byte[getLengthU() - MINIMUM_LENGTH - getActionsLengthU()];
        data.get(this.packetData);
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putInt(bufferId);
        data.putInt(inPort);
        data.putShort(actionsLength);
        data.putInt(0); // pad
        data.putShort((short) 0); // pad
        for (OFPAction action : actions) {
            action.writeTo(data);
        }
        if (this.packetData != null)
            data.put(this.packetData);
    }

    @Override
    public int hashCode() {
        final int prime = 293;
        int result = super.hashCode();
        result = prime * result + ((actions == null) ? 0 : actions.hashCode());
        result = prime * result + actionsLength;
        result = prime * result + bufferId;
        result = prime * result + inPort;
        result = prime * result + Arrays.hashCode(packetData);
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
        if (!(obj instanceof OFPPacketOut)) {
            return false;
        }
        OFPPacketOut other = (OFPPacketOut) obj;
        if (actions == null) {
            if (other.actions != null) {
                return false;
            }
        } else if (!actions.equals(other.actions)) {
            return false;
        }
        if (actionsLength != other.actionsLength) {
            return false;
        }
        if (bufferId != other.bufferId) {
            return false;
        }
        if (inPort != other.inPort) {
            return false;
        }
        if (!Arrays.equals(packetData, other.packetData)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFPPacketOut [actionFactory=" + actionFactory + ", actions=" + actions + ", actionsLength="
                + actionsLength + ", bufferId=0x" + Integer.toHexString(bufferId) + ", inPort=" + inPort
                + ", packetData=" + Arrays.toString(packetData) + "]";
    }
}
