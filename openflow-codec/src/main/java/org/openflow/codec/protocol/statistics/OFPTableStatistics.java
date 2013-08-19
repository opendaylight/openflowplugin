package org.openflow.codec.protocol.statistics;

import java.io.Serializable;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents an ofp_table_stats structure
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author AnilGujele
 */
public class OFPTableStatistics implements OFPStatistics, Serializable {
    private static final int MINIMUM_LENGTH = 24;

    protected byte tableId;
    protected int activeCount;
    protected long lookupCount;
    protected long matchedCount;

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
     * @return the activeCount
     */
    public int getActiveCount() {
        return activeCount;
    }

    /**
     * @param activeCount
     *            the activeCount to set
     */
    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    /**
     * @return the lookupCount
     */
    public long getLookupCount() {
        return lookupCount;
    }

    /**
     * @param lookupCount
     *            the lookupCount to set
     */
    public void setLookupCount(long lookupCount) {
        this.lookupCount = lookupCount;
    }

    /**
     * @return the matchedCount
     */
    public long getMatchedCount() {
        return matchedCount;
    }

    /**
     * @param matchedCount
     *            the matchedCount to set
     */
    public void setMatchedCount(long matchedCount) {
        this.matchedCount = matchedCount;
    }

    @Override
    public int getLength() {
        return MINIMUM_LENGTH;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        this.tableId = data.get();
        data.get(); // pad
        data.get(); // pad
        data.get(); // pad
        this.activeCount = data.getInt();
        this.lookupCount = data.getLong();
        this.matchedCount = data.getLong();
    }

    @Override
    public void writeTo(IDataBuffer data) {
        data.put(this.tableId);
        data.put((byte) 0); // pad
        data.put((byte) 0); // pad
        data.put((byte) 0); // pad
        data.putInt(this.activeCount);
        data.putLong(this.lookupCount);
        data.putLong(this.matchedCount);
    }

    @Override
    public int hashCode() {
        final int prime = 449;
        int result = 1;
        result = prime * result + activeCount;
        result = prime * result + (int) (lookupCount ^ (lookupCount >>> 32));
        result = prime * result + (int) (matchedCount ^ (matchedCount >>> 32));
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
        if (!(obj instanceof OFPTableStatistics)) {
            return false;
        }
        OFPTableStatistics other = (OFPTableStatistics) obj;
        if (activeCount != other.activeCount) {
            return false;
        }
        if (lookupCount != other.lookupCount) {
            return false;
        }
        if (matchedCount != other.matchedCount) {
            return false;
        }
        if (tableId != other.tableId) {
            return false;
        }
        return true;
    }
}
