package org.openflow.codec.protocol.queue;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.U16;

/**
 * Corresponds to the struct ofp_queue_prop_min_rate OpenFlow structure
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFPQueuePropertyMinRate extends OFPQueueProperty {
    public static int MINIMUM_LENGTH = 16;

    protected short rate;

    /**
     *
     */
    public OFPQueuePropertyMinRate() {
        super();
        this.type = OFPQueuePropertyType.MIN_RATE;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    /**
     * @return the rate
     */
    public short getRate() {
        return rate;
    }

    /**
     * @param rate
     *            the rate to set
     */
    public OFPQueuePropertyMinRate setRate(short rate) {
        this.rate = rate;
        return this;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.rate = data.getShort();
        data.getInt(); // pad
        data.getShort(); // pad
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putShort(this.rate);
        data.putInt(0); // pad
        data.putShort((short) 0); // pad
    }

    @Override
    public int hashCode() {
        final int prime = 3259;
        int result = super.hashCode();
        result = prime * result + rate;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof OFPQueuePropertyMinRate))
            return false;
        OFPQueuePropertyMinRate other = (OFPQueuePropertyMinRate) obj;
        if (rate != other.rate)
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFPQueuePropertyMinRate [type=" + type + ", rate=" + U16.f(rate) + "]";
    }

}
