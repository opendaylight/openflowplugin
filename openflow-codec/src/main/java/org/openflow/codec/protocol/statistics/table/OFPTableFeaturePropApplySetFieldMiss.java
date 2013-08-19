package org.openflow.codec.protocol.statistics.table;

/**
 * Class defines OFPTFPT_APPLY_SETFIELD_MISS type
 *
 * @author AnilGujele
 *
 */
public class OFPTableFeaturePropApplySetFieldMiss extends OFPTableFeaturePropOXM {

    /**
     * constructor
     */
    public OFPTableFeaturePropApplySetFieldMiss() {
        super.setOFTableFeaturePropType(OFPTableFeaturePropType.APPLY_SETFIELD_MISS);
    }

}
