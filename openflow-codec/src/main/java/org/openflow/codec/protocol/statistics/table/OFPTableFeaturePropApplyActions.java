package org.openflow.codec.protocol.statistics.table;

/**
 * Class defines OFPTFPT_APPLY_ACTIONS type
 *
 * @author AnilGujele
 *
 */
public class OFPTableFeaturePropApplyActions extends OFPTableFeaturePropActions {

    /**
     * constructor
     */
    public OFPTableFeaturePropApplyActions() {
        super.setOFTableFeaturePropType(OFPTableFeaturePropType.APPLY_ACTIONS);
    }

}
