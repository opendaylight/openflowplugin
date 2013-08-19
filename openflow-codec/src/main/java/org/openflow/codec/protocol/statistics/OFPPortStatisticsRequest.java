package org.openflow.codec.protocol.statistics;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents an ofp_port_stats_request structure
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author AnilGujele
 */
public class OFPPortStatisticsRequest implements OFPStatistics {
    private final static int MINIMUM_LENGTH = 8;
    protected int portNumber;

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

    @Override
    public int getLength() {
        return MINIMUM_LENGTH;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        this.portNumber = data.getInt();
        data.getInt(); // pad
    }

    @Override
    public void writeTo(IDataBuffer data) {
        data.putInt(this.portNumber);
        data.putInt(0); // pad
    }

    @Override
    public int hashCode() {
        final int prime = 433;
        int result = 1;
        result = prime * result + portNumber;
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
        if (!(obj instanceof OFPPortStatisticsRequest)) {
            return false;
        }
        OFPPortStatisticsRequest other = (OFPPortStatisticsRequest) obj;
        if (portNumber != other.portNumber) {
            return false;
        }
        return true;
    }
}
