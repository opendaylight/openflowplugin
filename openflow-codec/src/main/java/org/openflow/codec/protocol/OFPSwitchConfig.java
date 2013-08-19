package org.openflow.codec.protocol;

import org.openflow.codec.io.IDataBuffer;

/**
 * Base class representing ofp_switch_config based messages
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public abstract class OFPSwitchConfig extends OFPMessage {
    public static int MINIMUM_LENGTH = 12;

    public enum OFConfigFlags {
        OFPC_FRAG_NORMAL, OFPC_FRAG_DROP, OFPC_FRAG_REASM, OFPC_FRAG_MASK
    }

    protected short flags;
    protected short missSendLength;

    public OFPSwitchConfig() {
        super();
        super.setLengthU(MINIMUM_LENGTH);
    }

    /**
     * @return the flags
     */
    public short getFlags() {
        return flags;
    }

    /**
     * @param flags
     *            the flags to set
     */
    public OFPSwitchConfig setFlags(short flags) {
        this.flags = flags;
        return this;
    }

    /**
     * @return the missSendLength
     */
    public short getMissSendLength() {
        return missSendLength;
    }

    /**
     * @param missSendLength
     *            the missSendLength to set
     */
    public OFPSwitchConfig setMissSendLength(short missSendLength) {
        this.missSendLength = missSendLength;
        return this;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.flags = data.getShort();
        this.missSendLength = data.getShort();
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putShort(this.flags);
        data.putShort(this.missSendLength);
    }

    @Override
    public int hashCode() {
        final int prime = 331;
        int result = super.hashCode();
        result = prime * result + flags;
        result = prime * result + missSendLength;
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
        if (!(obj instanceof OFPSwitchConfig)) {
            return false;
        }
        OFPSwitchConfig other = (OFPSwitchConfig) obj;
        if (flags != other.flags) {
            return false;
        }
        if (missSendLength != other.missSendLength) {
            return false;
        }
        return true;
    }
}
