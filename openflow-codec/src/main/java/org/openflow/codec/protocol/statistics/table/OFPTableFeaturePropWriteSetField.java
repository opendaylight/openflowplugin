package org.openflow.codec.protocol.statistics.table;

/**
 * Class defines OFPTFPT_WRITE_SETFIELD type
 *
 * @author AnilGujele
 *
 */
public class OFPTableFeaturePropWriteSetField extends OFPTableFeaturePropOXM {

    /**
     * constructor
     */
    public OFPTableFeaturePropWriteSetField() {
        super.setOFTableFeaturePropType(OFPTableFeaturePropType.WRITE_SETFIELD);
    }

}
