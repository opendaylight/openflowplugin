package org.openflow.codec.protocol.action;

/**
 * Represents an action type OFPAT_PUSH_MPLS
 *
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com)
 */
public class OFPActionPushMpls extends OFPActionPush {
    public OFPActionPushMpls() {
        super();
        super.setType(OFPActionType.PUSH_MPLS);
        super.setLength((short) OFPActionPushMpls.MINIMUM_LENGTH);
    }
}