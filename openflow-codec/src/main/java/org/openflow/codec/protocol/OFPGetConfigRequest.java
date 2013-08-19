package org.openflow.codec.protocol;

import org.openflow.codec.util.U16;

/**
 * Represents an OFPT_GET_CONFIG_REQUEST type message
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFPGetConfigRequest extends OFPMessage {
    public OFPGetConfigRequest() {
        super();
        this.type = OFPType.GET_CONFIG_REQUEST;
        this.length = U16.t(OFPMessage.MINIMUM_LENGTH);
    }
}
