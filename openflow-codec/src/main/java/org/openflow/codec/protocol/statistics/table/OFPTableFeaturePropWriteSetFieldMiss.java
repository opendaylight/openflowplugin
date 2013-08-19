package org.openflow.codec.protocol.statistics.table;

/**
 * Class defines OFPTFPT_WRITE_SETFIELD_MISS type
 *
 * @author AnilGujele
 *
 */
public class OFPTableFeaturePropWriteSetFieldMiss extends OFPTableFeaturePropOXM {

    /**
     * constructor
     */
    public OFPTableFeaturePropWriteSetFieldMiss() {
        super.setOFTableFeaturePropType(OFPTableFeaturePropType.WRITE_SETFIELD_MISS);
    }

}
