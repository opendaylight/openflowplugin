package org.openflow.codec.protocol;

import org.openflow.codec.util.U16;

/**
 * Represents an ofp_echo_reply message
 *
 * @author Rob Sherwood (rob.sherwood@stanford.edu)
 */

public class OFPEchoReply extends OFPEchoRequest {
    public static int MINIMUM_LENGTH = 8;

    public OFPEchoReply() {
        super();
        this.type = OFPType.ECHO_REPLY;
        this.length = U16.t(MINIMUM_LENGTH);
    }
}
