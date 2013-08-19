package org.openflow.codec.protocol.queue;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.U16;

/**
 * Corresponds to the struct ofp_queue_prop_max_rate OpenFlow structure
 *
 * @author AnilGujele
 */
public class OFPQueuePropertyMaxRate extends OFPQueueProperty {
    public static int MINIMUM_LENGTH = 16;

    protected short rate;

    /**
     * constructor
     */
    public OFPQueuePropertyMaxRate() {
        super();
        this.type = OFPQueuePropertyType.MAX_RATE;
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
    public OFPQueuePropertyMaxRate setRate(short rate) {
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
        if (!(obj instanceof OFPQueuePropertyMaxRate))
            return false;
        OFPQueuePropertyMaxRate other = (OFPQueuePropertyMaxRate) obj;
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
        return "OFPQueuePropertyMaxRate [type=" + type + ", rate=" + U16.f(rate) + "]";
    }

}
