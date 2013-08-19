package org.openflow.codec.protocol.statistics.table;

/**
 * Class defines OFPTFPT_APPLY_ACTIONS_MISS type
 *
 * @author AnilGujele
 *
 */
public class OFPTableFeaturePropApplyActionsMiss extends OFPTableFeaturePropActions {

    /**
     * constructor
     */
    public OFPTableFeaturePropApplyActionsMiss() {
        super.setOFTableFeaturePropType(OFPTableFeaturePropType.APPLY_ACTIONS_MISS);
    }

}
