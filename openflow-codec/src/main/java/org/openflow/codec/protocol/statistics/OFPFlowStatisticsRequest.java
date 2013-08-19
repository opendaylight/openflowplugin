package org.openflow.codec.protocol.statistics;

import java.io.Serializable;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFPMatch;

/**
 * Represents an ofp_flow_stats_request structure
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author AnilGujele
 */
public class OFPFlowStatisticsRequest implements OFPStatistics, Serializable {
    private static final int MINIMUM_LENGTH = 40;

    protected byte tableId;
    protected int outPort;
    private int outGroup;
    private long cookie;
    private long cookieMask;
    protected OFPMatch match;

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
     * @return the outPort
     */
    public int getOutPort() {
        return outPort;
    }

    /**
     * @param outPort
     *            the outPort to set
     */
    public void setOutPort(int outPort) {
        this.outPort = outPort;
    }

    /**
     *
     * @return
     */
    public int getOutGroup() {
        return outGroup;
    }

    /**
     *
     * @param outGroup
     */

    public void setOutGroup(int outGroup) {
        this.outGroup = outGroup;
    }

    /**
     *
     * @return
     */
    public long getCookie() {
        return cookie;
    }

    /**
     *
     * @param cookie
     */
    public void setCookie(long cookie) {
        this.cookie = cookie;
    }

    /**
     *
     * @return
     */
    public long getCookieMask() {
        return cookieMask;
    }

    /**
     *
     * @param cookieMask
     */
    public void setCookieMask(long cookieMask) {
        this.cookieMask = cookieMask;
    }

    @Override
    public int getLength() {
        return MINIMUM_LENGTH;
    }

    @Override
    public void readFrom(IDataBuffer data) {

        this.tableId = data.get();
        data.get(); // pad
        data.getShort(); // pad
        this.outPort = data.getInt();
        this.setOutGroup(data.getInt());
        data.getInt(); // pad
        this.setCookie(data.getLong());
        this.setCookieMask(data.getLong());
        if (this.match == null)
            this.match = new OFPMatch();
        this.match.readFrom(data);
    }

    @Override
    public void writeTo(IDataBuffer data) {
        data.put(this.tableId);
        data.put((byte) 0);
        data.putShort((short) 0);
        data.putInt(this.outPort);
        data.putInt(this.getOutGroup());
        data.putInt(0);
        data.putLong(this.getCookie());
        data.putLong(this.getCookieMask());
        this.match.writeTo(data);
    }

    @Override
    public int hashCode() {
        final int prime = 421;
        int result = 1;
        result = prime * result + ((match == null) ? 0 : match.hashCode());
        result = prime * result + (int) (cookie ^ (cookie >>> 32));
        result = prime * result + (int) (cookieMask ^ (cookieMask >>> 32));
        result = prime * result + outPort;
        result = prime * result + outGroup;
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
        if (!(obj instanceof OFPFlowStatisticsRequest)) {
            return false;
        }
        OFPFlowStatisticsRequest other = (OFPFlowStatisticsRequest) obj;
        if (tableId != other.tableId) {
            return false;
        }
        if (outPort != other.outPort) {
            return false;
        }
        if (getOutGroup() != other.getOutGroup()) {
            return false;
        }
        if (getCookie() != other.getCookie()) {
            return false;
        }
        if (getCookieMask() != other.getCookieMask()) {
            return false;
        }
        if (match == null) {
            if (other.match != null) {
                return false;
            }
        } else if (!match.equals(other.match)) {
            return false;
        }

        return true;
    }
}
