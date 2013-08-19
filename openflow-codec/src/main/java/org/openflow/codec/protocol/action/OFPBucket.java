package org.openflow.codec.protocol.action;

import java.io.Serializable;
import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.factory.OFPActionFactory;
import org.openflow.codec.protocol.factory.OFPActionFactoryAware;
import org.openflow.codec.util.U16;

/**
 * Represents an ofp_bucket structure
 *
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com)
 *
 */
public class OFPBucket implements OFPActionFactoryAware, Cloneable, Serializable {
    public static int MINIMUM_LENGTH = 16;

    protected OFPActionFactory actionFactory;
    protected short length;
    protected short weight;
    protected int watch_port;
    protected int watch_group;
    protected List<OFPAction> actions;

    public OFPBucket() {
        this.length = U16.t(MINIMUM_LENGTH);
    }

    /**
     * Get the length of this message
     *
     * @return
     */
    public short getLength() {
        return length;
    }

    /**
     * Get the length of this message, unsigned
     *
     * @return
     */
    public int getLengthU() {
        return U16.f(length);
    }

    /**
     * Set the length of this message
     *
     * @param length
     */
    public OFPBucket setLength(short length) {
        this.length = length;
        return this;
    }

    public short getWeight() {
        return weight;
    }

    public OFPBucket setWeight(short weight) {
        this.weight = weight;
        return this;
    }

    public int getWatch_port() {
        return watch_port;
    }

    public OFPBucket setWatch_port(int watch_port) {
        this.watch_port = watch_port;
        return this;
    }

    public int getWatch_group() {
        return watch_group;
    }

    public OFPBucket setWatch_group(int watch_group) {
        this.watch_group = watch_group;
        return this;
    }

    public List<OFPAction> getActions() {
        return actions;
    }

    public OFPBucket setActions(List<OFPAction> actions) {
        this.actions = actions;
        return this;
    }

    /**
     * Returns a summary of the message
     *
     * @return "ofmsg=v=$version;t=$type:l=$len:xid=$xid"
     */
    public String toString() {
        return "ofbucket" + ";length=" + this.getLength() + ";length=" + this.getWeight() + ";length="
                + this.getWatch_port() + ";length=" + this.getWatch_group();
    }

    /**
     * Given the output from toString(), create a new OFPBucket
     *
     * @param val
     * @return
     */
    public static OFPBucket fromString(String val) {
        String tokens[] = val.split(";");
        if (!tokens[0].equals("ofbucket"))
            throw new IllegalArgumentException("expected 'ofbucket' but got '" + tokens[0] + "'");
        String type_tokens[] = tokens[1].split("=");
        String len_tokens[] = tokens[2].split("=");
        OFPBucket bucket = new OFPBucket();
        bucket.setLength(Short.valueOf(len_tokens[1]));
        return bucket;
    }

    public void readFrom(IDataBuffer data) {
        this.length = data.getShort();
        this.weight = data.getShort();
        this.watch_port = data.getInt();
        this.watch_group = data.getInt();
        data.getInt();
        if (this.actionFactory == null)
            throw new RuntimeException("OFPActionFactory not set");
        this.actions = this.actionFactory.parseActions(data, getLengthU() - MINIMUM_LENGTH);
    }

    public void writeTo(IDataBuffer data) {
        data.putShort(length);
        data.putShort(weight);
        data.putInt(watch_port);
        data.putInt(watch_group);
        data.putInt(0);
        if (actions != null) {
            for (OFPAction action : actions) {
                action.writeTo(data);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 347;
        int result = 1;
        result = prime * result + ((actions == null) ? 0 : actions.hashCode());
        result = prime * result + length;
        result = prime * result + weight;
        result = prime * result + watch_port;
        result = prime * result + watch_group;
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
        if (!(obj instanceof OFPBucket)) {
            return false;
        }
        OFPBucket other = (OFPBucket) obj;
        if (actions == null) {
            if (other.actions != null) {
                return false;
            }
        } else if (!actions.equals(other.actions)) {
            return false;
        }
        if (length != other.length) {
            return false;
        }
        if (weight != other.weight) {
            return false;
        }
        if (watch_port != other.watch_port) {
            return false;
        }
        if (watch_group != other.watch_group) {
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
    public OFPBucket clone() throws CloneNotSupportedException {
        return (OFPBucket) super.clone();
    }

    @Override
    public void setActionFactory(OFPActionFactory actionFactory) {
        this.actionFactory = actionFactory;

    }

}
