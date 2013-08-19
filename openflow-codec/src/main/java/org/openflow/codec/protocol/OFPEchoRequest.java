package org.openflow.codec.protocol;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.U16;

/**
 * Represents an ofp_echo_request message
 *
 * @author Rob Sherwood (rob.sherwood@stanford.edu)
 */

public class OFPEchoRequest extends OFPMessage {
    public static int MINIMUM_LENGTH = 8;
    byte[] payload;

    public OFPEchoRequest() {
        super();
        this.type = OFPType.ECHO_REQUEST;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    @Override
    public void readFrom(IDataBuffer bb) {
        super.readFrom(bb);
        int datalen = this.getLengthU() - MINIMUM_LENGTH;
        if (datalen > 0) {
            this.payload = new byte[datalen];
            bb.get(payload);
        }
    }

    /**
     * @return the payload
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * @param payload
     *            the payload to set
     */
    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    @Override
    public void writeTo(IDataBuffer bb) {
        super.writeTo(bb);
        if (payload != null)
            bb.put(payload);
    }
}
