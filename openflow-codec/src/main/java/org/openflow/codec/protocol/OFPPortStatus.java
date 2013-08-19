package org.openflow.codec.protocol;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.U16;

/**
 * Represents an ofp_port_status message
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFPPortStatus extends OFPMessage {
    public static int MINIMUM_LENGTH = 80;

    public enum OFPortReason {
        OFPPR_ADD, OFPPR_DELETE, OFPPR_MODIFY
    }

    protected byte reason;
    protected OFPPort desc;

    /**
     * @return the reason
     */
    public byte getReason() {
        return reason;
    }

    /**
     * @param reason
     *            the reason to set
     */
    public void setReason(byte reason) {
        this.reason = reason;
    }

    /**
     * @return the desc
     */
    public OFPPort getDesc() {
        return desc;
    }

    /**
     * @param desc
     *            the desc to set
     */
    public void setDesc(OFPPort desc) {
        this.desc = desc;
    }

    public OFPPortStatus() {
        super();
        this.type = OFPType.PORT_STATUS;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.reason = data.get();
        data.position(data.position() + 7); // skip 7 bytes of padding
        if (this.desc == null)
            this.desc = new OFPPort();
        this.desc.readFrom(data);
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.put(this.reason);
        for (int i = 0; i < 7; ++i)
            data.put((byte) 0);
        this.desc.writeTo(data);
    }

    @Override
    public int hashCode() {
        final int prime = 313;
        int result = super.hashCode();
        result = prime * result + ((desc == null) ? 0 : desc.hashCode());
        result = prime * result + reason;
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
        if (!(obj instanceof OFPPortStatus)) {
            return false;
        }
        OFPPortStatus other = (OFPPortStatus) obj;
        if (desc == null) {
            if (other.desc != null) {
                return false;
            }
        } else if (!desc.equals(other.desc)) {
            return false;
        }
        if (reason != other.reason) {
            return false;
        }
        return true;
    }
}
