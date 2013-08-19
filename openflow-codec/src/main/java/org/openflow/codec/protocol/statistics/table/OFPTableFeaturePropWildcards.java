package org.openflow.codec.protocol.statistics.table;

/**
 * Class defines OFPTFPT_WILDCARDS type
 *
 * @author AnilGujele
 *
 */
public class OFPTableFeaturePropWildcards extends OFPTableFeaturePropOXM {

    /**
     * constructor
     */
    public OFPTableFeaturePropWildcards() {
        super.setOFTableFeaturePropType(OFPTableFeaturePropType.WILDCARDS);
    }

}
