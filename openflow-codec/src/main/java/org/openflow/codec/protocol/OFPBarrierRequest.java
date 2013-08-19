package org.openflow.codec.protocol;

import org.openflow.codec.util.U16;

/**
 * Represents an OFPT_BARRIER_REQUEST message
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFPBarrierRequest extends OFPMessage {
    public OFPBarrierRequest() {
        super();
        this.type = OFPType.BARRIER_REQUEST;
        this.length = U16.t(OFPMessage.MINIMUM_LENGTH);
    }
}
