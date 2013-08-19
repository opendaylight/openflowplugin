package org.openflow.codec.protocol.statistics.table;

/**
 * class to define OFPTFPT_NEXT_TABLES_MISS
 *
 * @author AnilGujele
 *
 */
public class OFPTableFeaturePropNextTablesMiss extends OFPTableFeaturePropNextTables {

    /**
     * constructor
     */
    public OFPTableFeaturePropNextTablesMiss() {
        super.setOFTableFeaturePropType(OFPTableFeaturePropType.NEXT_TABLES_MISS);
    }

}
