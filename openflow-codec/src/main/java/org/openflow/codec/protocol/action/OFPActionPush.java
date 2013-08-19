/**
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com) - July 20, 2013
 */
package org.openflow.codec.protocol.action;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents an action struct ofp_action_push
 */
public class OFPActionPush extends OFPAction {
    public static int MINIMUM_LENGTH = 8;

    protected short etherType;

    /**
     * @return the etherType
     */
    public short getEtherType() {
        return etherType;
    }

    /**
     * @param etherType
     *            the etherType to set
     */
    public void setEtherType(short etherType) {
        this.etherType = etherType;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.etherType = data.getShort();
        data.getShort();
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putShort(this.etherType);
        data.putShort((short) 0);
    }

    @Override
    public int hashCode() {
        final int prime = 389;
        int result = super.hashCode();
        result = prime * result + etherType;
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
        if (!(obj instanceof OFPActionPush)) {
            return false;
        }
        OFPActionPush other = (OFPActionPush) obj;
        if (etherType != other.etherType) {
            return false;
        }
        return true;
    }
}