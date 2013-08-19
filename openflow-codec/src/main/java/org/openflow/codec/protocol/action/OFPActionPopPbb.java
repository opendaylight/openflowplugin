/**
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com) - July 20, 2013
 */
package org.openflow.codec.protocol.action;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents an action type OFPAT_POP_PBB
 */
public class OFPActionPopPbb extends OFPAction {
    public static int MINIMUM_LENGTH = 8;

    public OFPActionPopPbb() {
        super();
        super.setType(OFPActionType.POP_PBB);
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