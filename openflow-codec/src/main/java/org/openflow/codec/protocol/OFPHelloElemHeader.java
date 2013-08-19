package org.openflow.codec.protocol;

import java.io.Serializable;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.U16;

/**
 * Represents struct ofp_hello_elem_header
 *
 * @author AnilGujele
 *
 */
public class OFPHelloElemHeader implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final short MINIMUM_LENGTH = 4;

    protected OFPHelloElemType type;
    protected short length;

    /**
     * get the length
     *
     * @return
     */
    public short getLength() {
        return length;
    }

    /**
     * set the length
     *
     * @param length
     */
    public void setLength(short length) {
        this.length = length;
    }

    /**
     * get the unsigned length of instruction structure
     *
     * @return
     */
    public int getLengthU() {
        return U16.f(length);
    }

    /**
     * get the OFPHelloElemType
     *
     * @return
     */
    public OFPHelloElemType getOFHelloElemType() {
        return type;
    }

    /**
     * set the OFPHelloElemType
     *
     * @param type
     */
    public void setOFHelloElemType(OFPHelloElemType type) {
        this.type = type;
    }

    /**
     * read OFPHelloElemHeader object state from buffer
     *
     * @param data
     */
    public void readFrom(IDataBuffer data) {
        this.type = OFPHelloElemType.valueOf(data.getShort());
        this.length = data.getShort();
    }

    /**
     * write OFPHelloElemHeader object state to buffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        data.putShort(type.getTypeValue());
        data.putShort(this.length);

    }

    @Override
    public int hashCode() {
        final int prime = 761;
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
        if (!(obj instanceof OFPHelloElemHeader)) {
            return false;
        }
        OFPHelloElemHeader other = (OFPHelloElemHeader) obj;
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
