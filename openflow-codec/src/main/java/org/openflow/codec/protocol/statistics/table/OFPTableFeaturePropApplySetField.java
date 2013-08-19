package org.openflow.codec.protocol.statistics.table;

/**
 * Class defines OFPTFPT_APPLY_SETFIELD type
 *
 * @author AnilGujele
 *
 */
public class OFPTableFeaturePropApplySetField extends OFPTableFeaturePropOXM {

    /**
     * constructor
     */
    public OFPTableFeaturePropApplySetField() {
        super.setOFTableFeaturePropType(OFPTableFeaturePropType.APPLY_SETFIELD);
    }

}
