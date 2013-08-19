package org.openflow.codec.protocol.instruction;

import org.openflow.codec.io.IDataBuffer;

/**
 * Instruction structure correspond to struct ofp_instruction_experimenter
 *
 * @author AnilGujele
 *
 */
public class OFPInstructionExperimenter extends OFPInstruction {
    private static final short MINIMUM_LENGTH = 8;

    private int experimenterId;

    /**
     * constructor
     */
    public OFPInstructionExperimenter() {
        super.setOFInstructionType(OFPInstructionType.EXPERIMENTER);
        super.setLength(MINIMUM_LENGTH);
    }

    /**
     * get experimenter id
     *
     * @return
     */
    public int getExperimenterId() {
        return experimenterId;
    }

    /**
     * set experimenter id
     *
     * @param experimenterId
     */
    public void setExperimenterId(int experimenterId) {
        this.experimenterId = experimenterId;
    }

    /**
     * read OFPInstruction from buffer
     *
     * @param data
     */
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.experimenterId = data.getInt();

    }

    /**
     * write OFPInstruction to buffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putInt(this.experimenterId);
    }

    @Override
    public int hashCode() {
        final int prime = 744;
        int result = super.hashCode();
        result = prime * result + this.experimenterId;
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
        if (!(obj instanceof OFPInstructionExperimenter)) {
            return false;
        }
        OFPInstructionExperimenter other = (OFPInstructionExperimenter) obj;
        if (this.experimenterId != other.experimenterId) {
            return false;
        }
        return true;
    }

    /**
     * Returns a string representation of the instruction
     */
    public String toString() {
        return "OFPInstruction[" + "type=" + this.getOFInstructionType() + ", length=" + this.getLength()
                + ", experimenterId=" + experimenterId + "]";
    }

}
