package org.openflow.codec.protocol;

import org.openflow.codec.util.U16;

/**
 * Represents an ofp_multipart_reply message
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFPMultipartReply extends OFPMultipartMessageBase {

    /**
     * Represents an ofp_multipart_reply_flags
     *
     * @author AnilGujele
     *
     */
    public enum OFMultipartReplyFlags {
        REPLY_MORE(1 << 0);

        protected short type;

        OFMultipartReplyFlags(int type) {
            this.type = (short) type;
        }

        public short getTypeValue() {
            return type;
        }
    }

    public OFPMultipartReply() {
        super();
        this.type = OFPType.MULTIPART_REPLY;
        this.length = U16.t(OFPMultipartMessageBase.MINIMUM_LENGTH);
    }
}
