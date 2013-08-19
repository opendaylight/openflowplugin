package org.openflow.codec.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.U16;

/**
 * OXM Field in TLV format to handle Basic and Experimenter it has
 * implementation of ofp_oxm_experimenter_header
 *
 * @author AnilGujele
 *
 */
public class OXMField implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private static byte MINIMUM_LENGTH = 4;

    private OXMClass OXMClassType;
    private OFBMatchFields matchField;
    private boolean hasMask;
    private byte[] data;
    private byte length;
    private int experimenterId;
    private short experimenterField;

    /**
     * constructor
     */
    public OXMField() {
        data = new byte[0];
        length = MINIMUM_LENGTH;
    }

    /**
     * constructor with arguments
     *
     * @param classType
     * @param matchField
     * @param hasMask
     * @param data
     */
    public OXMField(OXMClass classType, OFBMatchFields matchField, boolean hasMask, byte[] data) {
        length = MINIMUM_LENGTH;
        this.setOXMClassType(classType);
        this.setMatchField(matchField);
        this.setHasMask(hasMask);
        this.setData(data);
    }

    /**
     * constructor with arguments, should use in case of experimenter
     */
    public OXMField(OXMClass classType, short expField, int expId) {
        length = MINIMUM_LENGTH;
        this.setOXMClassType(classType);
        this.setExperimenterField(expField);
        this.setExperimenterId(expId);
        // set default match field to avoid any exception in case of
        // experimenter
        this.setMatchField(OFBMatchFields.ICMPV4_TYPE);
    }

    /**
     * get the oxm match field class
     *
     * @return
     */
    public OXMClass getOXMClassType() {
        return OXMClassType;
    }

    /**
     * set the oxm match field class
     *
     * @param oXMClassType
     *            - supported class type is OXMClass.OPENFLOW_BASIC
     */
    public void setOXMClassType(OXMClass oXMClassType) {
        OXMClassType = oXMClassType;
    }

    /**
     * get match field
     *
     * @return
     */
    public OFBMatchFields getMatchField() {
        return matchField;
    }

    /**
     * set match field
     *
     * @param matchField
     */
    public void setMatchField(OFBMatchFields matchField) {
        this.matchField = matchField;
    }

    /**
     * return the match field with has mask
     */
    public byte getMatchFieldValueWithHasMask() {
        byte value = matchField.getValue();
        // 7 left most bit is used for match field identification
        value = (byte) (value << 1);
        if (this.hasMask) {
            // set the last bit as 1
            value = (byte) (value | 1);
        }
        return value;
    }

    /**
     * whether match field value has mask
     *
     * @return
     */
    public boolean isHasMask() {
        return hasMask;
    }

    /**
     * set the has mask
     *
     * @param hasMask
     */
    public void setHasMask(boolean hasMask) {
        this.hasMask = hasMask;

    }

    /**
     * get match field payload
     *
     * @return
     */
    public byte[] getData() {
        return data;
    }

    /**
     * set value for match field payload
     *
     * @param data
     */
    public void setData(byte[] data) {
        if (null == data) {
            this.data = new byte[0];
        } else {
            this.data = data;
        }
    }

    /**
     * to get the total length of match field (or TLV) including header in bytes
     *
     * @return
     */
    public byte getLength() {
        // Type + Lenghth + Value
        return (byte) (length + data.length);
    }

    /**
     * Read this field from the specified DataBuffer
     *
     * @param data
     *            - data to read to construct match object
     * @return true - reading is successful , false - failed to read as data is
     *         not proper
     */
    public boolean readFrom(IDataBuffer data) {

        boolean result = false;
        // read match field class type
        int classType = U16.f(data.getShort());
        // check for supported class type
        if (classType == OXMClass.OPENFLOW_BASIC.getValue()) {
            // read match field including hasMask
            byte field = data.get();
            boolean hasMask = ((field & 1) == 1);
            /*
             * TBD - if mask is set, then data length will be double, so driver
             * need to handle masking to set the field value or plugin or App
             * will handle
             */
            field = (byte) (field >>> 1);
            OFBMatchFields matchField = OFBMatchFields.valueOf(field);
            // read length of value
            byte dataLength = data.get();
            // if data is negative number, then return
            if (dataLength < 0) {
                return result;
            }
            if (null != matchField) {

                int matchFieldDefineSize = matchField.getLengthInBytes();
                int dataLenghtSize = hasMask ? dataLength / 2 : dataLength;
                byte[] dataValue = new byte[dataLength];
                data.get(dataValue);
                boolean isMaskingValid = hasMask ? (matchField.isHasMask() == hasMask) : true;
                // check if field is as per the open flow spec 1.3, if not then
                // only moving the data pointer is enough
                // datalength can be zero in case only header is required.
                if (0 != dataLenghtSize && (dataLenghtSize == matchFieldDefineSize) && isMaskingValid) {

                    this.setOXMClassType(OXMClass.getOXMClass(classType));
                    this.setMatchField(matchField);
                    this.setHasMask(hasMask);
                    this.setData(dataValue);

                }
            } else {
                // unknown match field, better to log and move the pointer
                // read data
                byte[] dataValue = new byte[dataLength];
                data.get(dataValue);
            }
            result = true;

        } else if (classType == OXMClass.EXPERIMENTER.getValue()) {
            this.setExperimenterField(data.getShort());
            this.setExperimenterId(data.getInt());
        }

        return result;

    }

    /**
     * Write this match binary format to the specified DataBuffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        data.putShort(U16.t(this.getOXMClassType().getValue()));
        if (this.getOXMClassType().equals(OXMClass.OPENFLOW_BASIC)) {
            data.put(this.getMatchFieldValueWithHasMask());
            data.put((byte) this.getData().length); // actual length of data
            data.put(this.getData());
        } else if (this.getOXMClassType().equals(OXMClass.EXPERIMENTER)) {
            data.putShort(this.getExperimenterField());
            data.putInt(this.getExperimenterId());

        }
    }

    /**
     * create the clone of this OXMTLVField
     *
     */
    @Override
    public OXMField clone() {
        try {
            OXMField field = (OXMField) super.clone();
            field.data = this.data.clone();
            return field;

        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * to get the object hashcode
     *
     * @return
     */
    public int hashCode() {
        final int prime = 721;
        int result = prime + ((data == null) ? 0 : Arrays.hashCode(data));
        result = prime * result + this.getLength();
        result = prime * result + this.matchField.getValue();
        result = prime * result + this.getOXMClassType().getValue();
        result = prime * result + this.getExperimenterField();
        result = prime * result + this.getExperimenterId();
        return result;
    }

    /**
     * to check object equality
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj) {
            return false;
        }
        if (!(obj instanceof OXMField)) {
            return false;
        }
        OXMField other = (OXMField) obj;
        if (0 != this.matchField.compareTo(other.matchField)) {
            return false;
        }
        if (0 != this.OXMClassType.compareTo(other.OXMClassType)) {
            return false;
        }
        if (!(this.isHasMask() == other.isHasMask())) {
            return false;
        }
        if (this.getLength() != other.getLength()) {
            return false;
        }
        if (this.getExperimenterField() != other.getExperimenterField()) {
            return false;
        }
        if (this.getExperimenterId() != other.getExperimenterId()) {
            return false;
        }

        if (!Arrays.equals(this.getData(), other.getData())) {
            return false;
        }
        return true;
    }

    /**
     * String representation of OXMTLVField Object ex:
     * Type=OPENFLOW_BASIC-IPV4_SRC, Length=8, Value=[1, 2, 3, 4]
     */

    public String toString() {
        StringBuffer buffer = new StringBuffer(30);
        buffer.append("Type=").append(this.getOXMClassType().name());
        buffer.append("-").append(this.matchField.name());
        buffer.append(", Length=").append(this.getLength());
        buffer.append(", Value=").append(Arrays.toString(data));
        return buffer.toString();
    }

    /**
     * read OXMField header from data till specified length
     *
     * @param data
     * @param length
     * @return
     */
    public static List<OXMField> readOXMFieldHeader(IDataBuffer data, int length) {
        List<OXMField> oxmList = new ArrayList<OXMField>();
        int end = data.position() + length;
        while (data.position() < end) {
            // read match field class type
            int classType = U16.f(data.getShort());
            // read match field including hasMask
            byte field = data.get();
            boolean hasMask = ((field & 1) == 1);
            field = (byte) (field >>> 1);
            OFBMatchFields matchField = OFBMatchFields.valueOf(field);
            // check for supported class type
            if (classType == OXMClass.OPENFLOW_BASIC.getValue()) {
                OXMField oxmfield = new OXMField(OXMClass.OPENFLOW_BASIC, matchField, hasMask, new byte[0]);
                oxmList.add(oxmfield);
            } else if (classType == OXMClass.EXPERIMENTER.getValue()) {
                OXMField oxmfield = new OXMField(OXMClass.OPENFLOW_BASIC, data.getShort(), data.getInt());
                oxmList.add(oxmfield);
            }
            data.get(); // ignore length field in header.
        }

        return oxmList;
    }

    /**
     * write oxmfield header in data buffer
     *
     * @param data
     * @param oxmList
     */
    public static void writeOXMFieldHeader(IDataBuffer data, List<OXMField> oxmList) {
        for (OXMField field : oxmList) {
            OXMClass clazz = field.getOXMClassType();
            if (clazz.getValue() == OXMClass.OPENFLOW_BASIC.getValue()) {
                data.putShort(U16.t(OXMClass.OPENFLOW_BASIC.getValue()));
                data.put(field.getMatchFieldValueWithHasMask());
                data.put(field.getLength());
            } else if (clazz.getValue() == OXMClass.EXPERIMENTER.getValue()) {
                data.putShort(U16.t(OXMClass.EXPERIMENTER.getValue()));
                data.putShort(field.getExperimenterField());
                data.putInt(field.getExperimenterId());
            }

        }
    }

    /**
     * get the total length in bytes for the OXMField List
     *
     * @param list
     * @return
     */
    public static int getTotalLength(List<OXMField> list) {
        int result = 0;
        for (OXMField field : list) {
            if (field.getOXMClassType().equals(OXMClass.OPENFLOW_BASIC)) {
                result = result + OXMField.MINIMUM_LENGTH;
            } else if (field.getOXMClassType().equals(OXMClass.EXPERIMENTER)) {
                // for experimenter, header is 8 byte
                result = result + OXMField.MINIMUM_LENGTH * 2;
            }
        }
        return result;

    }

    /**
     *
     * @return
     */
    public int getExperimenterId() {
        return experimenterId;
    }

    /**
     *
     * @param experimenterId
     */
    public void setExperimenterId(int experimenterId) {
        this.experimenterId = experimenterId;
    }

    public short getExperimenterField() {
        return experimenterField;
    }

    public void setExperimenterField(short experimenterField) {
        this.experimenterField = experimenterField;
    }

}
