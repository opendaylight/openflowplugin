package org.openflow.codec.protocol.instruction;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents meter instruction struct ofp_instruction_meter
 *
 * @author AnilGujele
 *
 */
public class OFPInstructionMeter extends OFPInstruction {
    private static final short MINIMUM_LENGTH = 8;

    private int meterId;

    /**
     * constructor
     */
    public OFPInstructionMeter() {
        super.setOFInstructionType(OFPInstructionType.METER);
        super.setLength(MINIMUM_LENGTH);
    }

    /**
     * get meter id
     *
     * @return
     */
    public int getMeterId() {
        return meterId;
    }

    /**
     * set meter id
     *
     * @param meterId
     */
    public void setMeterId(int meterId) {
        this.meterId = meterId;
    }

    /**
     * read OFPInstruction from buffer
     *
     * @param data
     */
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.meterId = data.getInt();

    }

    /**
     * write OFPInstruction to buffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putInt(this.meterId);
    }

    @Override
    public int hashCode() {
        final int prime = 745;
        int result = super.hashCode();
        result = prime * result + this.meterId;
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
        if (!(obj instanceof OFPInstructionMeter)) {
            return false;
        }
        OFPInstructionMeter other = (OFPInstructionMeter) obj;
        if (this.meterId != other.meterId) {
            return false;
        }
        return true;
    }

    /**
     * Returns a string representation of the instruction
     */
    public String toString() {
        return "OFPInstruction[" + "type=" + this.getOFInstructionType() + ", length=" + this.getLength()
                + ", meterId=" + meterId + "]";
    }

}
