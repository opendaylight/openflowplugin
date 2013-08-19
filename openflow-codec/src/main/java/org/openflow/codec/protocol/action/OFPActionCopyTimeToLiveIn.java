/**
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com) - July 20, 2013
 */
package org.openflow.codec.protocol.action;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents an action OFPAT_COPY_TTL_IN
 */
public class OFPActionCopyTimeToLiveIn extends OFPAction {
    public static int MINIMUM_LENGTH = 8;

    public OFPActionCopyTimeToLiveIn() {
        super();
        super.setType(OFPActionType.COPY_TTL_IN);
        super.setLength((short) MINIMUM_LENGTH);
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        // PAD
        data.getInt();
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        // PAD
        data.putInt(0);
    }
}