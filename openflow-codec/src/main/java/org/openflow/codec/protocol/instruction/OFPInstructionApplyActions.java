package org.openflow.codec.protocol.instruction;

/**
 * apply_actions instruction class
 *
 * @author AnilGujele
 *
 */
public class OFPInstructionApplyActions extends OFPInstructionActions {

    public OFPInstructionApplyActions() {
        super();
        super.setOFInstructionType(OFPInstructionType.APPLY_ACTIONS);
    }
}
