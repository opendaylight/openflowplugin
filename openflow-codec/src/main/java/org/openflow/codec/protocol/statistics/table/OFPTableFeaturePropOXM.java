package org.openflow.codec.protocol.statistics.table;

import java.util.ArrayList;
import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OXMField;

/**
 * Represents struct ofp_table_feature_prop_oxm
 *
 * @author AnilGujele
 *
 */
public class OFPTableFeaturePropOXM extends OFPTableFeaturePropHeader {
    private List<OXMField> oxmIds = new ArrayList<OXMField>();

    /**
     * constructor
     */
    public OFPTableFeaturePropOXM() {
        super.setLength(MINIMUM_LENGTH);
    }

    /**
     *
     * @return
     */
    public List<OXMField> getOXMIds() {
        return oxmIds;
    }

    /**
     *
     * @param oxmIds
     */
    public void setOXMIds(List<OXMField> oxmIds) {
        this.oxmIds = oxmIds;
        updateLength();
    }

    /**
     * update the length
     *
     */
    private void updateLength() {
        int length = this.getLength() + OXMField.getTotalLength(oxmIds);
        this.setLength((short) length);

    }

    /**
     * read OFPTableFeaturePropOXM from buffer
     *
     * @param data
     */
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        int idDataLength = this.getLengthU() - OFPTableFeaturePropHeader.MINIMUM_LENGTH;
        oxmIds = OXMField.readOXMFieldHeader(data, idDataLength);
        /* Read the padding, if any */
        int paddingLength = ((this.getLengthU() % MULTIPLE_OF_EIGHT) == 0) ? 0 : (MULTIPLE_OF_EIGHT - (this
                .getLengthU() % MULTIPLE_OF_EIGHT));
        data.position(data.position() + paddingLength);

    }

    /**
     * write OFPTableFeaturePropOXM to buffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        OXMField.writeOXMFieldHeader(data, oxmIds);

        /* Add padding if structure is not 8 byte aligned */
        int paddingLength = ((this.getLengthU() % MULTIPLE_OF_EIGHT) == 0) ? 0 : (MULTIPLE_OF_EIGHT - (this
                .getLengthU() % MULTIPLE_OF_EIGHT));
        byte[] padding = new byte[paddingLength];
        data.put(padding);

    }

    @Override
    public int hashCode() {
        final int prime = 745;
        int result = super.hashCode();
        result = prime * result + oxmIds.hashCode();
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
        if (!(obj instanceof OFPTableFeaturePropOXM)) {
            return false;
        }
        OFPTableFeaturePropOXM other = (OFPTableFeaturePropOXM) obj;
        if (!oxmIds.equals(other.oxmIds)) {
            return false;
        }
        return true;
    }

}
