package org.openflow.codec.protocol.statistics.table;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.U16;

/**
 * Represents struct ofp_table_feature_prop_header that is common to all table
 * feature properties.
 *
 * @author AnilGujele
 *
 */
public class OFPTableFeaturePropHeader {
    public static final short MINIMUM_LENGTH = 4;

    private OFPTableFeaturePropType type;
    private short length;

    /**
     * Match size should be multiple of eight, so padding will be done based on
     * length of match.
     */
    protected static final int MULTIPLE_OF_EIGHT = 8;

    /**
     * get the length of TableFeatureProp structure
     *
     * @return
     */
    public short getLength() {
        return length;
    }

    /**
     * set the length of TableFeatureProp structure
     *
     * @param length
     */
    public void setLength(short length) {
        this.length = length;
    }

    /**
     * get the unsigned length of TableFeatureProp structure
     *
     * @return
     */
    public int getLengthU() {
        return U16.f(length);
    }

    /**
     * get the OFPTableFeaturePropType
     *
     * @return
     */
    public OFPTableFeaturePropType getOFTableFeaturePropType() {
        return type;
    }

    /**
     * set the OFPTableFeaturePropType
     *
     * @param type
     */
    public void setOFTableFeaturePropType(OFPTableFeaturePropType type) {
        this.type = type;
    }

    /**
     * read OFPTableFeaturePropHeader object state from buffer
     *
     * @param data
     */
    public void readFrom(IDataBuffer data) {
        this.type = OFPTableFeaturePropType.valueOf(data.getShort());
        this.length = data.getShort();
    }

    /**
     * write OFPTableFeaturePropHeader object state to buffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        data.putShort(type.getTypeValue());
        data.putShort(this.length);

    }

    @Override
    public int hashCode() {
        final int prime = 751;
        int result = 1;
        result = prime * result + length;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OFPTableFeaturePropHeader)) {
            return false;
        }
        OFPTableFeaturePropHeader other = (OFPTableFeaturePropHeader) obj;
        if (length != other.length) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

}
