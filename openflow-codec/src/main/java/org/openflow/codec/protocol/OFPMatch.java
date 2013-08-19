package org.openflow.codec.protocol;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents an ofp_match structure
 *
 * @author AnilGujele
 *
 */
public class OFPMatch implements Cloneable, Serializable {
    /**
     * serialization id
     */
    private static final long serialVersionUID = 1L;
    private static final short MINIMUM_LENGTH = 4;
    /**
     * Match size should be multiple of eight, so padding will be done based on
     * length of match.
     */
    private static final int MULTIPLE_OF_EIGHT = 8;

    private OFPMatchType matchType;
    private short length;
    private Map<String, OXMField> fieldMap;

    /**
     * constructor
     */
    public OFPMatch() {
        matchType = OFPMatchType.OFPMT_OXM;
        setLength(MINIMUM_LENGTH); // all match fields are wild card
        fieldMap = new HashMap<String, OXMField>();
    }

    /**
     * add match field
     *
     * @param matchField
     */
    public void addMatchField(OXMField matchField) {
        fieldMap.put(matchField.getMatchField().name(), matchField);

    }

    /**
     * remove match field
     *
     * @param matchField
     */
    public void removeMatchField(OXMField matchField) {
        fieldMap.remove(matchField.getMatchField().name());
    }

    /**
     * get the length OFPMatch TLV
     *
     * @return
     */
    public short getLength() {
        length = OFPMatch.MINIMUM_LENGTH;
        for (Map.Entry<String, OXMField> entry : fieldMap.entrySet()) {
            length += entry.getValue().getLength();
        }
        return length;
    }

    /**
     * get the length OFPMatch TLV
     *
     * @return
     */
    public short getLengthWithPadding() {
        int matchLength = getLength();
        int paddingLength = ((matchLength % MULTIPLE_OF_EIGHT) == 0) ? 0
                : (MULTIPLE_OF_EIGHT - (matchLength % MULTIPLE_OF_EIGHT));
        length = (short) (matchLength + paddingLength);
        return length;
    }

    /**
     * set the length of OFPMatch TLV
     *
     * @param length
     */
    public void setLength(short length) {
        this.length = length;
    }

    /**
     * get the matchfield map
     *
     * @return
     */
    public Map<String, OXMField> getMatchFieldMap() {
        return fieldMap;
    }

    /**
     * set the matchfield map
     *
     * @param fieldMap
     */
    public void setMatchFieldMap(Map<String, OXMField> fieldMap) {
        this.fieldMap = fieldMap;
    }

    /**
     * Read this match from the specified DataBuffer
     *
     * @param data
     *            - data to read to construct match object
     * @return true - reading is successful , false - failed to read as data is
     *         not proper or prerequisite is not matched
     */
    public boolean readFrom(IDataBuffer data) {
        boolean result = false;
        this.matchType = OFPMatchType.valueOf(data.getShort());
        // check for valid match type
        // if not match then either match type is deprecated or not supported
        if (matchType == OFPMatchType.OFPMT_OXM) {
            int length = data.getShort();
            if (length >= MINIMUM_LENGTH) {
                // match size will be multiple of 8, so need to handle padding
                // if any
                int paddingLength = ((length % MULTIPLE_OF_EIGHT) == 0) ? 0
                        : (MULTIPLE_OF_EIGHT - (length % MULTIPLE_OF_EIGHT));
                // length = length - MINIMUM_LENGTH - paddingLength;
                /* length of ofp_match, is excluding the padding */
                length = length - MINIMUM_LENGTH;
                // read match field
                while (length > 0) {
                    OXMField oxmField = new OXMField();
                    if (!oxmField.readFrom(data)) {
                        // data is not valid, so log, move the data pointer and
                        // return;
                        byte[] junkData = new byte[length + paddingLength];
                        data.get(junkData);
                        return false;
                    }

                    length = length - oxmField.getLength();
                    this.addMatchField(oxmField);
                    this.length += oxmField.getLength();
                    result = true;

                }
                // remove padding bytes
                byte[] pad = new byte[paddingLength];
                data.get(pad);
                // result = MatchUtil.hasPrerequisite(this);
            }
        }
        return result;

    }

    /**
     * Write this match binary format to the specified DataBuffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        // TBD - we should proceed if prerequisite matching
        // MatchUtil.hasPrerequisite(this);
        data.putShort(matchType.getMatchTypeValue());
        data.putShort(getLength());
        for (Map.Entry<String, OXMField> entry : fieldMap.entrySet()) {
            OXMField field = entry.getValue();
            field.writeTo(data);
        }
        // match size should be multiple of 8, so need to handle padding if any
        int paddingLength = ((getLength() % MULTIPLE_OF_EIGHT) == 0) ? 0
                : (MULTIPLE_OF_EIGHT - (getLength() % MULTIPLE_OF_EIGHT));
        // add padding bytes
        byte[] pad = new byte[paddingLength];
        data.put(pad);

    }

    /**
     * create the clone of this OFPMatch
     *
     */
    @Override
    public OFPMatch clone() {
        try {
            OFPMatch clonedOFMatch = (OFPMatch) super.clone();
            clonedOFMatch.fieldMap = new HashMap<String, OXMField>();
            for (Map.Entry<String, OXMField> entry : fieldMap.entrySet()) {
                clonedOFMatch.fieldMap.put(entry.getKey(), entry.getValue().clone());
            }
            return clonedOFMatch;
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
        final int prime = 722;
        int result = prime + this.getLength();
        result = prime * result + this.matchType.getMatchTypeValue();
        result = prime * result + this.fieldMap.hashCode();
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
        if (!(obj instanceof OFPMatch)) {
            return false;
        }
        OFPMatch other = (OFPMatch) obj;
        if (this.matchType != other.matchType) {
            return false;
        }
        if (this.getLength() != other.getLength()) {
            return false;
        }

        if (!this.fieldMap.equals(other.fieldMap)) {
            return false;
        }

        return true;
    }

    /**
     * String representation of Match object ex: OFPMatch[MatchType=OFPMT_OXM,
     * Length=12, Match Fields[Type=OPENFLOW_BASIC-IPV4_SRC, Length=8, Value=[1,
     * 2, 3, 4]]]
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("OFPMatch[");
        buffer.append("MatchType=").append(matchType);
        buffer.append(", Length=").append(getLength());
        buffer.append(", Match Fields[");
        int index = fieldMap.size();
        for (Map.Entry<String, OXMField> entry : fieldMap.entrySet()) {
            OXMField field = entry.getValue();
            buffer.append(field.toString());
            if (0 != --index) {
                buffer.append(",");
            }
        }
        buffer.append("]]");
        return buffer.toString();
    }

}
