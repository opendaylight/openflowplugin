package org.openflow.codec.protocol.action;

/**
 * Represents an action type OFPAT_PUSH_PBB
 *
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com)
 */
public class OFPActionPushPbb extends OFPActionPush {
    public OFPActionPushPbb() {
        super();
        super.setType(OFPActionType.PUSH_PBB);
        super.setLength((short) OFPActionPushPbb.MINIMUM_LENGTH);
    }
}