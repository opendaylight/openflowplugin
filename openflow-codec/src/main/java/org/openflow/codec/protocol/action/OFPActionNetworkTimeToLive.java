/**
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com) - July 20, 2013
 */
package org.openflow.codec.protocol.action;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents an action struct ofp_action_nw_ttl
 */
public class OFPActionNetworkTimeToLive extends OFPAction {
    public static int MINIMUM_LENGTH = 8;

    protected byte networkTimeToLive;

    public OFPActionNetworkTimeToLive() {
        super.setType(OFPActionType.SET_NW_TTL);
        super.setLength((short) MINIMUM_LENGTH);
    }

    /**
     * @return the networkTimeToLive
     */
    public short getNetworkTimeToLive() {
        return networkTimeToLive;
    }

    /**
     * @param networkTimeToLive
     *            the networkTimeToLive to set
     */
    public void setNetworkTimeToLive(byte networkTtl) {
        this.networkTimeToLive = networkTtl;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.networkTimeToLive = data.get();
        data.getShort();
        data.get();
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putShort(this.networkTimeToLive);
        data.putShort((short) 0);
        data.put((byte) 0);
    }

    @Override
    public int hashCode() {
        final int prime = 373;
        int result = super.hashCode();
        result = prime * result + networkTimeToLive;
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
        if (!(obj instanceof OFPActionNetworkTimeToLive)) {
            return false;
        }
        OFPActionNetworkTimeToLive other = (OFPActionNetworkTimeToLive) obj;
        if (networkTimeToLive != other.networkTimeToLive) {
            return false;
        }
        return true;
    }
}