package org.openflow.codec.protocol.statistics;

import java.util.Arrays;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents ofp_experimenter_multipart_header
 *
 * @author AnilGujele
 */
public class OFPExperimenterMultipartHeader implements OFPExtStatistics {
    public static int MINIMUM_LENGTH = 8;

    private int experimenter;
    private int expType;
    private byte[] data;

    // non-message field
    private int length;

    /**
     * constructor
     */
    public OFPExperimenterMultipartHeader() {
        this.length = MINIMUM_LENGTH;
    }

    /**
     * get experimenter id
     *
     * @return
     */
    public int getExperimenter() {
        return experimenter;
    }

    /**
     * set experimenter id
     *
     * @param experimenter
     */
    public void setExperimenter(int experimenter) {
        this.experimenter = experimenter;
    }

    /**
     * get experimenter type
     *
     * @return
     */
    public int getExpType() {
        return expType;
    }

    /**
     * set experimenter type
     *
     * @param expType
     */
    public void setExpType(int expType) {
        this.expType = expType;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        this.experimenter = data.getInt();
        this.expType = data.getInt();
        if (this.length > MINIMUM_LENGTH) {
            this.data = new byte[this.length - MINIMUM_LENGTH];
            data.get(this.data);
            updateLength();
        }
    }

    @Override
    public void writeTo(IDataBuffer data) {
        data.putInt(this.experimenter);
        data.putInt(this.expType);
        if (this.data != null)
            data.put(this.data);
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @param data
     *            the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
        updateLength();
    }

    /**
     * update length
     */
    private void updateLength() {
        int dataLength = (null == data) ? 0 : data.length;
        this.length = OFPExperimenterMultipartHeader.MINIMUM_LENGTH + dataLength;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 337;
        int result = 0;
        result = prime * result + Arrays.hashCode(data);
        result = prime * result + experimenter;
        result = prime * result + expType;
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (!(obj instanceof OFPExperimenterMultipartHeader))
            return false;
        OFPExperimenterMultipartHeader other = (OFPExperimenterMultipartHeader) obj;
        if (experimenter != other.experimenter)
            return false;
        if (expType != other.expType)
            return false;
        if (!Arrays.equals(data, other.data))
            return false;
        return true;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public void setLength(int length) {
        this.length = length;

    }
}
