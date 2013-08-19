package org.openflow.codec.protocol.statistics.table;

import java.util.Arrays;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents struct ofp_table_feature_prop_experimenter
 *
 * @author AnilGujele
 *
 */
public class OFPTableFeaturePropExperimenter extends OFPTableFeaturePropHeader {
    private static final short MINIMUM_LENGTH = 12;
    private int expId;
    private int expType;
    private int[] expData = new int[0];

    /**
     * constructor
     */
    public OFPTableFeaturePropExperimenter() {
        super.setOFTableFeaturePropType(OFPTableFeaturePropType.EXPERIMENTER);
        super.setLength(OFPTableFeaturePropExperimenter.MINIMUM_LENGTH);
    }

    public int getExpId() {
        return expId;
    }

    public void setExpId(int expId) {
        this.expId = expId;
    }

    public int getExpType() {
        return expType;
    }

    public void setExpType(int expType) {
        this.expType = expType;
    }

    public int[] getExpData() {
        return expData;
    }

    public void setExpData(int[] expData) {
        this.expData = expData;
        updateLength();
    }

    /**
     * update the length
     *
     */
    private void updateLength() {
        int length = this.getLength() + (expData.length * 4);
        this.setLength((short) length);

    }

    /**
     * read OFPTableFeaturePropExperimenter from buffer
     *
     * @param data
     */
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.expId = data.getInt();
        this.expType = data.getInt();
        int dataLength = this.getLengthU() - OFPTableFeaturePropExperimenter.MINIMUM_LENGTH;
        int intDataLength = dataLength / 4;
        expData = new int[intDataLength];
        for (int i = 0; i < intDataLength; i++) {
            expData[i] = data.getInt();
        }

        /* Read the padding, if any */
        int paddingLength = ((this.getLengthU() % MULTIPLE_OF_EIGHT) == 0) ? 0 : (MULTIPLE_OF_EIGHT - (this
                .getLengthU() % MULTIPLE_OF_EIGHT));
        data.position(data.position() + paddingLength);

    }

    /**
     * write OFPTableFeaturePropExperimenter to buffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putInt(this.expId);
        data.putInt(this.expType);
        for (int value : expData) {
            data.putInt(value);
        }

        /* Add padding if structure is not 8 byte aligned */
        int paddingLength = ((this.getLengthU() % MULTIPLE_OF_EIGHT) == 0) ? 0 : (MULTIPLE_OF_EIGHT - (this
                .getLengthU() % MULTIPLE_OF_EIGHT));
        byte[] padding = new byte[paddingLength];
        data.put(padding);

    }

    @Override
    public int hashCode() {
        final int prime = 746;
        int result = super.hashCode();
        result = prime * result + expId;
        result = prime * result + expType;
        result = prime * result + Arrays.hashCode(expData);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OFPTableFeaturePropExperimenter)) {
            return false;
        }
        OFPTableFeaturePropExperimenter other = (OFPTableFeaturePropExperimenter) obj;
        if (this.expId != other.expId) {
            return false;
        }
        if (this.expType != other.expType) {
            return false;
        }
        if (!Arrays.equals(expData, other.expData)) {
            return false;
        }
        return true;
    }

}
