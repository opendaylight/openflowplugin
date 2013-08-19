package org.openflow.codec.protocol.queue;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.U16;

/**
 * Corresponds to the struct ofp_queue_prop_experimenter OpenFlow structure
 *
 * @author AnilGujele
 */
public class OFPQueuePropertyExperimenter extends OFPQueueProperty {
    public static int MINIMUM_LENGTH = 16;

    private int experimenterId;
    private byte[] dataArr;

    /**
     * constructor
     */
    public OFPQueuePropertyExperimenter() {
        super();
        this.type = OFPQueuePropertyType.EXPERIMENTER;
        this.length = U16.t(MINIMUM_LENGTH);
        this.dataArr = new byte[0];
    }

    /**
     * @return the rate
     */
    public int experimenterId() {
        return experimenterId;
    }

    /**
     * @param rate
     *            the rate to set
     */
    public OFPQueuePropertyExperimenter setExperimenterId(int experimenterId) {
        this.experimenterId = experimenterId;
        return this;
    }

    /**
     * get the data
     *
     * @return
     */
    public byte[] getData() {
        return dataArr;
    }

    /**
     * set the data
     *
     * @param data
     */
    public void setData(byte[] data) {
        this.dataArr = data;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.experimenterId = data.getInt();
        data.getInt(); // pad
        int dataLength = length - MINIMUM_LENGTH;
        dataArr = new byte[dataLength];
        data.get(dataArr);
    }

    @Override
    public void writeTo(IDataBuffer data) {
        this.setLength((short) (length + dataArr.length));
        super.writeTo(data);
        data.putInt(this.experimenterId);
        data.putInt(0); // pad
        data.put(dataArr);
    }

    @Override
    public int hashCode() {
        final int prime = 3259;
        int result = super.hashCode();
        result = prime * result + experimenterId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof OFPQueuePropertyExperimenter))
            return false;
        OFPQueuePropertyExperimenter other = (OFPQueuePropertyExperimenter) obj;
        if (experimenterId != other.experimenterId)
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
        return "OFPQueuePropertyExperimenter [type=" + type + ", experimenterId=" + experimenterId + "]";
    }

}
