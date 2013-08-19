/**
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com) - July 20, 2013
 */
package org.openflow.codec.protocol.action;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents an action struct ofp_action_mpls_ttl
 */
public class OFPActionMplsTimeToLive extends OFPAction {
    public static int MINIMUM_LENGTH = 8;

    protected byte mplsTimeToLive;

    public OFPActionMplsTimeToLive() {
        super.setType(OFPActionType.SET_MPLS_TTL);
        super.setLength((short) MINIMUM_LENGTH);
    }

    /**
     * @return the mplsTimeToLive
     */
    public short getMplsTimeToLive() {
        return mplsTimeToLive;
    }

    /**
     * @param mplsTimeToLive
     *            the mplsTimeToLive to set
     */
    public void setMplsTimeToLive(byte mplsTtl) {
        this.mplsTimeToLive = mplsTtl;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.mplsTimeToLive = data.get();
        data.getShort();
        data.get();
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putShort(this.mplsTimeToLive);
        data.putShort((short) 0);
        data.put((byte) 0);
    }

    @Override
    public int hashCode() {
        final int prime = 359;
        int result = super.hashCode();
        result = prime * result + mplsTimeToLive;
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
        if (!(obj instanceof OFPActionMplsTimeToLive)) {
            return false;
        }
        OFPActionMplsTimeToLive other = (OFPActionMplsTimeToLive) obj;
        if (mplsTimeToLive != other.mplsTimeToLive) {
            return false;
        }
        return true;
    }
}