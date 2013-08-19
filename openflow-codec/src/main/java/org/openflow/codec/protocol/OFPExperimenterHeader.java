package org.openflow.codec.protocol;

import java.util.Arrays;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.U16;

/**
 * Represents ofp_experimenter_header
 *
 * @author AnilGujele
 */
public class OFPExperimenterHeader extends OFPMessage {
    public static int MINIMUM_LENGTH = 16;

    private int experimenter;
    private int expType;
    private byte[] data;

    /**
     * constructor
     */
    public OFPExperimenterHeader() {
        super();
        this.type = OFPType.EXPERIMENTER;
        this.length = U16.t(MINIMUM_LENGTH);
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
        super.readFrom(data);
        this.experimenter = data.getInt();
        this.expType = data.getInt();
        if (this.length > MINIMUM_LENGTH) {
            this.data = new byte[this.length - MINIMUM_LENGTH];
            data.get(this.data);
        }
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
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
        this.length = (short) (OFPExperimenterHeader.MINIMUM_LENGTH + dataLength);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 337;
        int result = super.hashCode();
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
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        OFPExperimenterHeader other = (OFPExperimenterHeader) obj;
        if (experimenter != other.experimenter)
            return false;
        if (expType != other.expType)
            return false;
        if (!Arrays.equals(data, other.data))
            return false;
        return true;
    }
}
