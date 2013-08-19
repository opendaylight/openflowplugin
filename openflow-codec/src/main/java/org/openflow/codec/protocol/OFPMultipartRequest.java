package org.openflow.codec.protocol;

import org.openflow.codec.util.U16;

/**
 * Represents an ofp_multipart_request message
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFPMultipartRequest extends OFPMultipartMessageBase {

    /**
     * Represents an ofp_multipart_request_flags
     *
     * @author AnilGujele
     *
     */
    public enum OFMultipartRequestFlags {
        REQ_MORE(1 << 0);

        protected short type;

        OFMultipartRequestFlags(int type) {
            this.type = (short) type;
        }

        public short getTypeValue() {
            return type;
        }
    }

    public OFPMultipartRequest() {
        super();
        this.type = OFPType.MULTIPART_REQUEST;
        this.length = U16.t(OFPMultipartMessageBase.MINIMUM_LENGTH);
    }
}
