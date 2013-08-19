package org.openflow.codec.protocol.action;

/**
 * Represents an action type OFPAT_PUSH_VLAN
 *
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com)
 */
public class OFPActionPushVLAN extends OFPActionPush {
    public OFPActionPushVLAN() {
        super();
        super.setType(OFPActionType.PUSH_VLAN);
        super.setLength((short) OFPActionPushVLAN.MINIMUM_LENGTH);
    }
}