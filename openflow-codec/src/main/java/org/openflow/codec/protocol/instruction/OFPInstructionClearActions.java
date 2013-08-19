package org.openflow.codec.protocol.instruction;

import org.openflow.codec.io.IDataBuffer;

/**
 * clear_actions instruction class Note:For the Clear-Actions instruction, the
 * structure does not contain any actions.
 *
 * @author AnilGujele
 *
 */
public class OFPInstructionClearActions extends OFPInstruction {
    private static final long serialVersionUID = 1L;

    private final static short MINIMUM_LENGTH = 8;

    public OFPInstructionClearActions() {
        super();
        super.setLength(MINIMUM_LENGTH);
        super.setOFInstructionType(OFPInstructionType.CLEAR_ACTIONS);
    }

    /**
     * read OFPInstruction from buffer
     *
     * @param data
     */
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        data.getInt(); // pad

    }

    /**
     * write OFPInstruction to buffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putInt(0); // pad
    }

}
