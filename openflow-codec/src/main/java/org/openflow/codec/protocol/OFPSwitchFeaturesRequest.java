package org.openflow.codec.protocol;

import org.openflow.codec.util.U16;

/**
 * Represents a features request message
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 *
 */
public class OFPSwitchFeaturesRequest extends OFPMessage {
    public static int MINIMUM_LENGTH = 8;

    public OFPSwitchFeaturesRequest() {
        super();
        this.type = OFPType.FEATURES_REQUEST;
        this.length = U16.t(MINIMUM_LENGTH);
    }
}
