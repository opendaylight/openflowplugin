/**
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com) - Jul 20, 2013
 */
package org.openflow.codec.protocol.action;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents an ofp_action_group
 */
public class OFPActionGroup extends OFPAction {
    public static int MINIMUM_LENGTH = 8;

    protected int groupId;

    public OFPActionGroup() {
        super.setType(OFPActionType.GROUP);
        super.setLength((short) MINIMUM_LENGTH);
    }

    /**
     * @return the groupId
     */
    public int getGroupId() {
        return groupId;
    }

    /**
     * @param groupId
     *            the groupId to set
     */
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.groupId = data.getInt();
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putInt(this.groupId);
    }

    @Override
    public int hashCode() {
        final int prime = 353;
        int result = super.hashCode();
        result = prime * result + groupId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OFPActionGroup)) {
            return false;
        }
        OFPActionGroup other = (OFPActionGroup) obj;
        if (groupId != other.groupId) {
            return false;
        }
        return true;
    }
}