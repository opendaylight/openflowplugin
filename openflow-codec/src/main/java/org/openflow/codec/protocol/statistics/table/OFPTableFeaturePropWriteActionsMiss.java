package org.openflow.codec.protocol.statistics.table;

/**
 * Class defines OFPTFPT_WRITE_ACTIONS_MISS type
 *
 * @author AnilGujele
 *
 */
public class OFPTableFeaturePropWriteActionsMiss extends OFPTableFeaturePropActions {

    /**
     * constructor
     */
    public OFPTableFeaturePropWriteActionsMiss() {
        super.setOFTableFeaturePropType(OFPTableFeaturePropType.WRITE_ACTIONS_MISS);
    }

}
