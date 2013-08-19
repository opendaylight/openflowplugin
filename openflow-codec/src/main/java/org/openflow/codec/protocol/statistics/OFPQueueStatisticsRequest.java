package org.openflow.codec.protocol.statistics;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents an ofp_queue_stats_request structure
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author AnilGujele
 */
public class OFPQueueStatisticsRequest implements OFPStatistics {
    private static final int MINIMUM_LENGTH = 8;
    protected int portNumber;
    protected int queueId;

    /**
     * @return the portNumber
     */
    public int getPortNumber() {
        return portNumber;
    }

    /**
     * @param portNumber
     *            the portNumber to set
     */
    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    /**
     * @return the queueId
     */
    public int getQueueId() {
        return queueId;
    }

    /**
     * @param queueId
     *            the queueId to set
     */
    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    @Override
    public int getLength() {
        return MINIMUM_LENGTH;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        this.portNumber = data.getInt();
        this.queueId = data.getInt();
    }

    @Override
    public void writeTo(IDataBuffer data) {
        data.putInt(this.portNumber);
        data.putInt(this.queueId);
    }

    @Override
    public int hashCode() {
        final int prime = 443;
        int result = 1;
        result = prime * result + portNumber;
        result = prime * result + queueId;
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
        if (!(obj instanceof OFPQueueStatisticsRequest)) {
            return false;
        }
        OFPQueueStatisticsRequest other = (OFPQueueStatisticsRequest) obj;
        if (portNumber != other.portNumber) {
            return false;
        }
        if (queueId != other.queueId) {
            return false;
        }
        return true;
    }
}
