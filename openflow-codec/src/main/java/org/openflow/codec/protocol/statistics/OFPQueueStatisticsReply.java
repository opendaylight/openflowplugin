package org.openflow.codec.protocol.statistics;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents an ofp_queue_stats structure
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author AnilGujele
 */
public class OFPQueueStatisticsReply implements OFPStatistics {
    private final static int MINIMUM_LENGTH = 40;
    protected int portNumber;
    protected int queueId;
    protected long transmitBytes;
    protected long transmitPackets;
    protected long transmitErrors;
    protected int durationSec;
    protected int durationNanoSec;

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

    /**
     * @return the transmitBytes
     */
    public long getTransmitBytes() {
        return transmitBytes;
    }

    /**
     * @param transmitBytes
     *            the transmitBytes to set
     */
    public void setTransmitBytes(long transmitBytes) {
        this.transmitBytes = transmitBytes;
    }

    /**
     * @return the transmitPackets
     */
    public long getTransmitPackets() {
        return transmitPackets;
    }

    /**
     * @param transmitPackets
     *            the transmitPackets to set
     */
    public void setTransmitPackets(long transmitPackets) {
        this.transmitPackets = transmitPackets;
    }

    /**
     * @return the transmitErrors
     */
    public long getTransmitErrors() {
        return transmitErrors;
    }

    /**
     * @param transmitErrors
     *            the transmitErrors to set
     */
    public void setTransmitErrors(long transmitErrors) {
        this.transmitErrors = transmitErrors;
    }

    /**
     * get duration queue has been alive in seconds.
     *
     * @return
     */
    public int getDurationSec() {
        return durationSec;
    }

    /**
     * set duration queue has been alive in seconds.
     *
     * @param durationSec
     */
    public void setDurationSec(int durationSec) {
        this.durationSec = durationSec;
    }

    /**
     * get duration queue has been alive in nanoseconds beyond duration_sec.
     *
     * @return
     */
    public int getDurationNanoSec() {
        return durationNanoSec;
    }

    /**
     * set duration queue has been alive in nanoseconds beyond duration_sec.
     *
     * @param durationNanoSec
     */
    public void setDurationNanoSec(int durationNanoSec) {
        this.durationNanoSec = durationNanoSec;
    }

    @Override
    public int getLength() {
        return MINIMUM_LENGTH;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        this.portNumber = data.getInt();
        this.queueId = data.getInt();
        this.transmitBytes = data.getLong();
        this.transmitPackets = data.getLong();
        this.transmitErrors = data.getLong();
        this.durationSec = data.getInt();
        this.durationNanoSec = data.getInt();
    }

    @Override
    public void writeTo(IDataBuffer data) {
        data.putInt(this.portNumber);
        data.putInt(this.queueId);
        data.putLong(this.transmitBytes);
        data.putLong(this.transmitPackets);
        data.putLong(this.transmitErrors);
        data.putInt(this.durationSec);
        data.putInt(this.durationNanoSec);
    }

    @Override
    public int hashCode() {
        final int prime = 439;
        int result = 1;
        result = prime * result + portNumber;
        result = prime * result + queueId;
        result = prime * result + (int) (transmitBytes ^ (transmitBytes >>> 32));
        result = prime * result + (int) (transmitErrors ^ (transmitErrors >>> 32));
        result = prime * result + (int) (transmitPackets ^ (transmitPackets >>> 32));
        result = prime * result + durationSec;
        result = prime * result + durationNanoSec;
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
        if (!(obj instanceof OFPQueueStatisticsReply)) {
            return false;
        }
        OFPQueueStatisticsReply other = (OFPQueueStatisticsReply) obj;
        if (portNumber != other.portNumber) {
            return false;
        }
        if (queueId != other.queueId) {
            return false;
        }
        if (transmitBytes != other.transmitBytes) {
            return false;
        }
        if (transmitErrors != other.transmitErrors) {
            return false;
        }
        if (transmitPackets != other.transmitPackets) {
            return false;
        }
        if (durationSec != other.durationSec) {
            return false;
        }
        if (durationNanoSec != other.durationNanoSec) {
            return false;
        }
        return true;
    }
}
