/**
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com) - July 21, 2013
 */
package org.openflow.codec.protocol.action;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFBMatchFields;
import org.openflow.codec.protocol.OXMClass;
import org.openflow.codec.protocol.OXMField;

/**
 * Represents an action struct ofp_action_set_field
 */
public class OFPActionSetField extends OFPAction {
    public static int MINIMUM_LENGTH = 8;

    // OXM TLV
    OXMField oxmtlvField;

    // Contructors
    public OFPActionSetField() {
        super.setType(OFPActionType.SET_FIELD);
        super.setLength((short) MINIMUM_LENGTH);
        oxmtlvField = new OXMField();
    }

    public OFPActionSetField(OXMClass classType, OFBMatchFields matchField, boolean hasMask, byte[] data) {
        oxmtlvField = new OXMField(classType, matchField, hasMask, data);
    }

    // Get Set Methods
    public OXMClass getOXMClassType() {
        return this.oxmtlvField.getOXMClassType();
    }

    public void setOXMClassType(OXMClass OXMClassType) {
        this.oxmtlvField.setOXMClassType(OXMClassType);
    }

    public OFBMatchFields getMatchField() {
        return this.oxmtlvField.getMatchField();
    }

    public void setMatchField(OFBMatchFields matchField) {
        this.oxmtlvField.setMatchField(matchField);
    }

    public boolean isHasMask() {
        return this.oxmtlvField.isHasMask();
    }

    public void setHasMask(boolean hasMask) {
        this.oxmtlvField.setHasMask(hasMask);
    }

    /**
     * to get the total length of match field (or TLV) including header in bytes
     *
     * @return
     */
    public byte getFieldLength() {
        // Type + Lenghth + Value
        return this.oxmtlvField.getLength();
    }

    public byte[] getData() {
        return this.oxmtlvField.getData();
    }

    public void setData(byte[] data) {
        this.oxmtlvField.setData(data);
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        if (oxmtlvField == null)
            oxmtlvField = new OXMField();
        oxmtlvField.readFrom(data);
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        oxmtlvField.writeTo(data);
    }

    @Override
    public int hashCode() {
        final int prime = 353;
        int result = super.hashCode();
        result = prime * result + (oxmtlvField == null ? 0 : oxmtlvField.hashCode());
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
        if (!(obj instanceof OFPActionSetField)) {
            return false;
        }
        OFPActionSetField other = (OFPActionSetField) obj;
        if (this.oxmtlvField.getMatchField() != other.oxmtlvField.getMatchField()) {
            return false;
        }
        return true;
    }

}