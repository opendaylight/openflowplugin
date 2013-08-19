package org.openflow.codec.protocol;

import org.openflow.codec.util.U16;

/**
 * Represents an OFPT_BARRIER_REPLY message
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFPBarrierReply extends OFPMessage {
    public OFPBarrierReply() {
        super();
        this.type = OFPType.BARRIER_REPLY;
        this.length = U16.t(OFPMessage.MINIMUM_LENGTH);
    }
}
