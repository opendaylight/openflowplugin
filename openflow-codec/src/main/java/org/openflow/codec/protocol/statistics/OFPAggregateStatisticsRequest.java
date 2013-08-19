package org.openflow.codec.protocol.statistics;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFPMatch;

/**
 * Represents an ofp_aggregate_stats_request structure
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author AnilGujele
 */
public class OFPAggregateStatisticsRequest implements OFPStatistics {
    private final static int MINIMUM_LENGTH = 40;

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
     * get the output group
     *
     * @return
     */
    public int getOutGroup() {
        return outGroup;
    }

    /**
     * set output group
     *
     * @param outGroup
     */
    public void setOutGroup(int outGroup) {
        this.outGroup = outGroup;
    }

    /**
     * get the cookie
     *
     * @return
     */
    public long getCookie() {
        return cookie;
    }

    /**
     * set cookie
     *
     * @param cookie
     */
    public void setCookie(long cookie) {
        this.cookie = cookie;
    }

    /**
     * get the cookie mask
     *
     * @return
     */
    public long getCookieMask() {
        return cookieMask;
    }

    /**
     * set mask for cookie
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
        this.outGroup = data.getInt();
        data.getInt(); // pad
        this.cookie = data.getLong();
        this.cookieMask = data.getLong();
        if (this.match == null)
            this.match = new OFPMatch();
        this.match.readFrom(data);
    }

    @Override
    public void writeTo(IDataBuffer data) {
        data.put(this.tableId);
        data.put((byte) 0); // pad
        data.putShort((short) 0); // pad
        data.putInt(this.outPort);
        data.putInt(this.outGroup);
        data.putInt(0); // pad
        data.putLong(cookie);
        data.putLong(cookieMask);
        this.match.writeTo(data);
    }

    @Override
    public int hashCode() {
        final int prime = 401;
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
        if (!(obj instanceof OFPAggregateStatisticsRequest)) {
            return false;
        }
        OFPAggregateStatisticsRequest other = (OFPAggregateStatisticsRequest) obj;
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
