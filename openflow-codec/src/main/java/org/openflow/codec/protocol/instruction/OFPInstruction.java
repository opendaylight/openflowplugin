package org.openflow.codec.protocol.instruction;

import java.io.Serializable;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.U16;

/**
 * correspond to struct ofp_instruction openflow structure Instruction header
 * that is common to all instructions. The length field includes the header and
 * any padding used to make the instruction 64-bit aligned.
 *
 * @author AnilGujele
 *
 */
public class OFPInstruction implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    public static final short MINIMUM_LENGTH = 4;

    protected short length;
    protected OFPInstructionType type;

    /**
     * get the length of instruction structure
     *
     * @return
     */
    public short getLength() {
        return length;
    }

    /**
     * set the length of instruction structure
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
     * get the OFInstrutionType
     *
     * @return
     */
    public OFPInstructionType getOFInstructionType() {
        return type;
    }

    /**
     * set the OFPInstructionType
     *
     * @param type
     */
    public void setOFInstructionType(OFPInstructionType type) {
        this.type = type;
    }

    /**
     * read OFPInstruction object state from buffer
     *
     * @param data
     */
    public void readFrom(IDataBuffer data) {
        this.type = OFPInstructionType.valueOf(data.getShort());
        this.length = data.getShort();
    }

    /**
     * write OFPInstruction object state to buffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        data.putShort(type.getTypeValue());
        data.putShort(this.length);

    }

    @Override
    public int hashCode() {
        final int prime = 741;
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
        if (!(obj instanceof OFPInstruction)) {
            return false;
        }
        OFPInstruction other = (OFPInstruction) obj;
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

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public OFPInstruction clone() throws CloneNotSupportedException {
        return (OFPInstruction) super.clone();
    }

}
