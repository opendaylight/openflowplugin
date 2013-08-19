package org.openflow.codec.protocol.statistics.table;

import java.util.Arrays;

import org.openflow.codec.io.IDataBuffer;

/**
 * correspond to struct ofp_table_feature_prop_next_tables
 *
 * @author AnilGujele
 *
 */
public class OFPTableFeaturePropNextTables extends OFPTableFeaturePropHeader {
    private byte[] nextTableIds = new byte[0];

    /**
     * constructor
     */
    public OFPTableFeaturePropNextTables() {
        super.setOFTableFeaturePropType(OFPTableFeaturePropType.NEXT_TABLES);
        super.setLength(MINIMUM_LENGTH);
    }

    /**
     *
     * @return
     */
    public byte[] getNextTableIds() {
        return nextTableIds;
    }

    /**
     *
     * @param nextTableIds
     */
    public void setNextTableIds(byte[] nextTableIds) {
        this.nextTableIds = nextTableIds;
        updateLength();
    }

    /**
     * update the length
     *
     */
    private void updateLength() {
        int length = this.getLength() + nextTableIds.length;
        this.setLength((short) length);

    }

    /**
     * read OFPTableFeaturePropNextTables from buffer
     *
     * @param data
     */
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        int idDataLength = this.getLengthU() - OFPTableFeaturePropHeader.MINIMUM_LENGTH;
        nextTableIds = new byte[idDataLength];
        data.get(nextTableIds);

        /*
         * Read the padding, if any and move the data pointer position to next
         * element
         */
        int paddingLength = ((this.getLengthU() % MULTIPLE_OF_EIGHT) == 0) ? 0 : (MULTIPLE_OF_EIGHT - (this
                .getLengthU() % MULTIPLE_OF_EIGHT));
        data.position(data.position() + paddingLength);

    }

    /**
     * write OFPTableFeaturePropNextTables to buffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.put(nextTableIds);

        /* Add padding if structure is not 8 byte aligned */
        int paddingLength = ((this.getLengthU() % MULTIPLE_OF_EIGHT) == 0) ? 0 : (MULTIPLE_OF_EIGHT - (this
                .getLengthU() % MULTIPLE_OF_EIGHT));
        byte[] padding = new byte[paddingLength];
        data.put(padding);

    }

    @Override
    public int hashCode() {
        final int prime = 743;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(nextTableIds);
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
        if (!(obj instanceof OFPTableFeaturePropNextTables)) {
            return false;
        }
        OFPTableFeaturePropNextTables other = (OFPTableFeaturePropNextTables) obj;
        if (!Arrays.equals(nextTableIds, other.nextTableIds)) {
            return false;
        }
        return true;
    }

}
