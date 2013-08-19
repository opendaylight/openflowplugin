package org.openflow.codec.protocol.instruction;

/**
 * Represents write_actions instruction class
 *
 * @author AnilGujele
 *
 */
public class OFPInstructionWriteActions extends OFPInstructionActions {

    public OFPInstructionWriteActions() {
        super();
        super.setOFInstructionType(OFPInstructionType.WRITE_ACTIONS);
    }
}
