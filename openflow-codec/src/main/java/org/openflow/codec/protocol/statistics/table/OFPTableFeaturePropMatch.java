package org.openflow.codec.protocol.statistics.table;

/**
 * Class defines OFPTFPT_MATCH type
 *
 * @author AnilGujele
 *
 */
public class OFPTableFeaturePropMatch extends OFPTableFeaturePropOXM {

    /**
     * constructor
     */
    public OFPTableFeaturePropMatch() {
        super.setOFTableFeaturePropType(OFPTableFeaturePropType.MATCH);
    }

}
