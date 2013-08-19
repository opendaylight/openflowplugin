package org.openflow.codec.protocol.statistics.table;

/**
 * Class defines OFPTFPT_WRITE_ACTIONS type
 *
 * @author AnilGujele
 *
 */
public class OFPTableFeaturePropWriteActions extends OFPTableFeaturePropActions {

    /**
     * constructor
     */
    public OFPTableFeaturePropWriteActions() {
        super.setOFTableFeaturePropType(OFPTableFeaturePropType.WRITE_ACTIONS);
    }

}
