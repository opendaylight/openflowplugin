/**
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com) - July 20, 2013
 */
package org.openflow.codec.protocol.action;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents an action struct ofp_action_set_queue
 */
public class OFPActionSetQueue extends OFPAction {
    public static int MINIMUM_LENGTH = 8;

    protected int queueId;

    public OFPActionSetQueue() {
        super.setType(OFPActionType.SET_QUEUE);
        super.setLength((short) MINIMUM_LENGTH);
    }

    /**
     * @return the queueIdentifier
     */
    public int getVirtualLanIdentifier() {
        return queueId;
    }

    /**
     * @param queueIdentifier
     *            the queueIdentifier to set
     */
    public void setQueueIdentifier(int queueIdentifier) {
        this.queueId = queueIdentifier;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.queueId = data.getInt();
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putInt(this.queueId);
    }

    @Override
    public int hashCode() {
        final int prime = 349;
        int result = super.hashCode();
        result = prime * result + queueId;
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
        if (!(obj instanceof OFPActionSetQueue)) {
            return false;
        }
        OFPActionSetQueue other = (OFPActionSetQueue) obj;
        if (queueId != other.queueId) {
            return false;
        }
        return true;
    }
}