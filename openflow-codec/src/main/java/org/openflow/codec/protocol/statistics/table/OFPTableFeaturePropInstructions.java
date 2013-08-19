package org.openflow.codec.protocol.statistics.table;

import java.util.ArrayList;
import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.factory.OFPInstructionFactory;
import org.openflow.codec.protocol.factory.OFPInstructionFactoryAware;
import org.openflow.codec.protocol.instruction.OFPInstruction;
import org.openflow.codec.util.U16;

/**
 * Represents struct ofp_table_feature_prop_instructions
 *
 * @author AnilGujele
 *
 */
public class OFPTableFeaturePropInstructions extends OFPTableFeaturePropHeader implements OFPInstructionFactoryAware {
    private List<OFPInstruction> instructionIds;
    private OFPInstructionFactory instructionFactory;

    /**
     * constructor
     */
    public OFPTableFeaturePropInstructions() {
        super.setOFTableFeaturePropType(OFPTableFeaturePropType.INSTRUCTIONS);
        super.setLength(MINIMUM_LENGTH);
        setInstructionIds(new ArrayList<OFPInstruction>());
    }

    /**
     * get list of instruction id
     *
     * @return
     */
    public List<OFPInstruction> getInstructionIds() {
        return instructionIds;
    }

    /**
     * set list of instruction id
     *
     * @param instructionIds
     */
    public void setInstructionIds(List<OFPInstruction> instructionIds) {
        this.instructionIds = instructionIds;
        updateLength();
    }

    /**
     * update the length
     *
     * @return
     */
    private void updateLength() {
        int length = this.getLength();
        for (OFPInstruction ofInstruction : instructionIds) {
            length += ofInstruction.getLengthU();
        }
        this.setLength((short) length);

    }

    /**
     * read OFPTableFeaturePropInstructions from buffer
     *
     * @param data
     */
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        int instructionIdsLength = U16.f(this.getLength()) - MINIMUM_LENGTH;
        if (null == instructionFactory) {
            throw new RuntimeException("OFPInstructionFactory is not set.");
        }
        instructionIds = instructionFactory.parseInstructions(data, instructionIdsLength);
        /* Read the padding, if any */
        int paddingLength = ((this.getLength() % MULTIPLE_OF_EIGHT) == 0) ? 0
                : (MULTIPLE_OF_EIGHT - (this.getLength() % MULTIPLE_OF_EIGHT));
        data.position(data.position() + paddingLength);

    }

    /**
     * write OFPTableFeaturePropInstructions to buffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        // write instruction ids
        for (OFPInstruction ofInstruction : instructionIds) {
            ofInstruction.writeTo(data);
        }

        /* Add padding if structure is not 8 byte aligned */
        int paddingLength = ((this.getLength() % MULTIPLE_OF_EIGHT) == 0) ? 0
                : (MULTIPLE_OF_EIGHT - (this.getLength() % MULTIPLE_OF_EIGHT));
        byte[] padding = new byte[paddingLength];
        data.put(padding);

    }

    @Override
    public int hashCode() {
        final int prime = 742;
        int result = super.hashCode();
        result = prime * result + instructionIds.hashCode();
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
        if (!(obj instanceof OFPTableFeaturePropInstructions)) {
            return false;
        }
        OFPTableFeaturePropInstructions other = (OFPTableFeaturePropInstructions) obj;
        if (!this.instructionIds.equals(other.instructionIds)) {
            return false;
        }
        return true;
    }

    @Override
    public void setInstructionFactory(OFPInstructionFactory instructionFactory) {
        this.instructionFactory = instructionFactory;

    }

}
