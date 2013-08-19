package org.openflow.codec.protocol.statistics.table;

/**
 * Class defines OFPTFPT_EXPERIMENTER_MISS.
 *
 * @author AnilGujele
 *
 */
public class OFPTableFeaturePropExperimenterMiss extends OFPTableFeaturePropExperimenter {

    /**
     * constructor
     */
    public OFPTableFeaturePropExperimenterMiss() {
        super.setOFTableFeaturePropType(OFPTableFeaturePropType.EXPERIMENTER_MISS);
    }

}
