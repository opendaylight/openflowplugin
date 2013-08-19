package org.openflow.codec.protocol.statistics.table;

/**
 * Class defines OFPTFPT_INSTRUCTIONS_MISS type
 *
 * @author AnilGujele
 *
 */
public class OFPTableFeaturePropInstructionsMiss extends OFPTableFeaturePropInstructions {

    /**
     * constructor
     */
    public OFPTableFeaturePropInstructionsMiss() {
        super.setOFTableFeaturePropType(OFPTableFeaturePropType.INSTRUCTIONS_MISS);
    }
}
