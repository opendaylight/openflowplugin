package org.openflow.codec.protocol;

import java.io.Serializable;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.U16;

/**
 * Represents a features reply message struct ofp_switch_features
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 *
 */
public class OFPSwitchFeaturesReply extends OFPMessage implements Serializable {
    public static int MINIMUM_LENGTH = 32;

    /**
     * Corresponds to bits on the capabilities field enum ofp_capabilities
     */
    public enum OFCapabilities {
        OFPC_FLOW_STATS(1 << 0), OFPC_TABLE_STATS(1 << 1), OFPC_PORT_STATS(1 << 2), OFPC_GROUP_STATS(1 << 3), OFPC_IP_REASM(
                1 << 5), OFPC_QUEUE_STATS(1 << 6), OFPC_PORT_BLOCKED(1 << 8);

        protected int value;

        private OFCapabilities(int value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }

    protected long datapathId;
    protected int buffers;
    protected byte tables;
    protected byte auxiliaryId;
    protected int capabilities;
    protected int reserved;

    public OFPSwitchFeaturesReply() {
        super();
        this.type = OFPType.FEATURES_REPLY;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    /**
     * @return the datapathId
     */
    public long getDatapathId() {
        return datapathId;
    }

    /**
     * @param datapathId
     *            the datapathId to set
     */
    public void setDatapathId(long datapathId) {
        this.datapathId = datapathId;
    }

    /**
     * @return the buffers
     */
    public int getBuffers() {
        return buffers;
    }

    /**
     * @param buffers
     *            the buffers to set
     */
    public void setBuffers(int buffers) {
        this.buffers = buffers;
    }

    /**
     * @return the tables
     */
    public byte getTables() {
        return tables;
    }

    /**
     * @param tables
     *            the tables to set
     */
    public void setTables(byte tables) {
        this.tables = tables;
    }

    /**
     * type of connection with switch.
     *
     * @return 0 - main connection , non zero - auxiliary connection
     */
    public byte getAuxiliaryId() {
        return auxiliaryId;
    }

    /**
     *
     * @param auxiliaryId
     */
    public void setAuxiliaryId(byte auxiliaryId) {
        this.auxiliaryId = auxiliaryId;
    }

    /**
     * @return the capabilities
     */
    public int getCapabilities() {
        return capabilities;
    }

    /**
     * @param capabilities
     *            the capabilities to set
     */
    public void setCapabilities(int capabilities) {
        this.capabilities = capabilities;
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(int reserved) {
        this.reserved = reserved;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.datapathId = data.getLong();
        this.buffers = data.getInt();
        this.tables = data.get();
        this.auxiliaryId = data.get();
        data.getShort(); // pad
        this.capabilities = data.getInt();
        this.reserved = data.getInt();
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putLong(this.datapathId);
        data.putInt(this.buffers);
        data.put(this.tables);
        data.put(this.auxiliaryId);
        data.putShort((short) 0); // pad
        data.putInt(this.capabilities);
        data.putInt(this.reserved);
    }

    @Override
    public int hashCode() {
        final int prime = 139;
        int result = super.hashCode();
        result = prime * result + auxiliaryId;
        result = prime * result + buffers;
        result = prime * result + capabilities;
        result = prime * result + reserved;
        result = prime * result + (int) (datapathId ^ (datapathId >>> 32));
        result = prime * result + tables;
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
        if (!(obj instanceof OFPSwitchFeaturesReply)) {
            return false;
        }
        OFPSwitchFeaturesReply other = (OFPSwitchFeaturesReply) obj;
        if (auxiliaryId != other.auxiliaryId) {
            return false;
        }
        if (reserved != other.reserved) {
            return false;
        }
        if (buffers != other.buffers) {
            return false;
        }
        if (capabilities != other.capabilities) {
            return false;
        }
        if (datapathId != other.datapathId) {
            return false;
        }
        if (tables != other.tables) {
            return false;
        }
        return true;
    }
}
