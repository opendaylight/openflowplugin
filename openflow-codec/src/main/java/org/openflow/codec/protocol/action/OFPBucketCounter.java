package org.openflow.codec.protocol.action;

import java.io.Serializable;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents an ofp_bucket_counter structure
 *
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com)
 *
 */
public class OFPBucketCounter implements Cloneable, Serializable {
    public static int MINIMUM_LENGTH = 16;

    protected long packet_count;
    protected long byte_count;

    public OFPBucketCounter() {

    }

    public long getPacket_count() {
        return packet_count;
    }

    public OFPBucketCounter setPacket_count(long packet_count) {
        this.packet_count = packet_count;
        return this;
    }

    public long getByte_count() {
        return byte_count;
    }

    public OFPBucketCounter setByte_count(long byte_count) {
        this.byte_count = byte_count;
        return this;
    }

    public String toString() {
        return "ofbucketcounter" + ";packet_count=" + this.getPacket_count() + ";byte_count=" + this.getByte_count();
    }

    /**
     * Given the output from toString(), create a new OFPBucketCounter
     *
     * @param val
     * @return
     */
    public static OFPBucketCounter fromString(String val) {
        String tokens[] = val.split(";");
        if (!tokens[0].equals("ofbucket"))
            throw new IllegalArgumentException("expected 'ofbucketcounter' but got '" + tokens[0] + "'");
        String packet_token[] = tokens[1].split("=");
        String byte_token[] = tokens[2].split("=");
        OFPBucketCounter bucketCounter = new OFPBucketCounter();
        bucketCounter.setPacket_count(Long.valueOf(packet_token[1]));
        bucketCounter.setByte_count(Long.valueOf(byte_token[2]));
        return bucketCounter;
    }

    public void readFrom(IDataBuffer data) {
        this.packet_count = data.getLong();
        this.byte_count = data.getLong();
    }

    public void writeTo(IDataBuffer data) {
        data.putLong(packet_count);
        data.putLong(byte_count);
    }

    @Override
    public int hashCode() {
        final int prime = 347;
        int result = 1;
        result = prime * result + (int) (packet_count ^ (packet_count >>> 32));
        result = prime * result + (int) (byte_count ^ (byte_count >>> 32));
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
        if (!(obj instanceof OFPBucketCounter)) {
            return false;
        }
        OFPBucketCounter other = (OFPBucketCounter) obj;
        if (packet_count != other.packet_count) {
            return false;
        }
        if (byte_count != other.byte_count) {
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
    public OFPBucketCounter clone() throws CloneNotSupportedException {
        return (OFPBucketCounter) super.clone();
    }

}
