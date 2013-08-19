package org.openflow.codec.protocol;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.action.OFPBucket;
import org.openflow.codec.util.U16;

/**
 * Represents an ofp_group_mod message
 *
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com)
 *
 */
public class OFPGroupMod extends OFPMessage implements Cloneable {
    public static int MINIMUM_LENGTH = 16;

    public static final short OFPGC_ADD = 0; /* New group. */
    public static final short OFPGC_MODIFY = 1; /* Modify all matching groups. */
    public static final short OFPGC_DELETE = 2; /* Delete all matching groups. */

    public static final short OFPGT_ALL = 0; /* All (multicast/broadcast) group. */
    public static final short OFPGT_SELECT = 1; /* Select group. */
    public static final short OFPGT_INDIRECT = 2; /* Indirect group. */
    public static final short OFPGT_FF = 3; /* Fast failover group. */

    protected short groupCommand;
    protected byte groupType;
    protected int group_id;
    protected List<OFPBucket> buckets;

    public OFPGroupMod() {
        super();
        this.type = OFPType.GROUP_MOD;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    public short getGroupCommand() {
        return groupCommand;
    }

    public OFPGroupMod setGroupCommand(short groupCommand) {
        this.groupCommand = groupCommand;
        return this;
    }

    public byte getGroupType() {
        return groupType;
    }

    public OFPGroupMod setGroupType(byte groupType) {
        this.groupType = groupType;
        return this;
    }

    public int getGroup_id() {
        return group_id;
    }

    public OFPGroupMod setGroup_id(int group_id) {
        this.group_id = group_id;
        return this;
    }

    public List<OFPBucket> getBuckets() {
        return buckets;
    }

    /**
     * @param buckets
     *            the buckets to set
     */
    public OFPGroupMod setBuckets(List<OFPBucket> buckets) {
        this.buckets = buckets;
        if (buckets == null) {
            this.setLengthU(MINIMUM_LENGTH);
        } else {
            this.setLengthU(MINIMUM_LENGTH + buckets.size() * OFPBucket.MINIMUM_LENGTH);
        }
        return this;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.groupCommand = data.getShort();
        this.groupType = data.get();
        data.get();
        this.group_id = data.getInt();
        if (this.buckets == null) {
            this.buckets = new ArrayList<OFPBucket>();
        } else {
            this.buckets.clear();
        }
        int bucketCount = (super.getLengthU() - 16) / OFPBucket.MINIMUM_LENGTH;
        OFPBucket bucket;
        for (int i = 0; i < bucketCount; ++i) {
            bucket = new OFPBucket();
            bucket.readFrom(data);
            this.buckets.add(bucket);
        }
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putShort(groupCommand);
        data.put(groupType);
        data.put((byte) 0);
        data.putInt(group_id);
        if (buckets != null) {
            for (OFPBucket bucket : buckets) {
                bucket.writeTo(data);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 227;
        int result = super.hashCode();
        result = prime * result + ((buckets == null) ? 0 : buckets.hashCode());
        result = prime * result + groupCommand;
        result = prime * result + groupType;
        result = prime * result + group_id;
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
        if (!(obj instanceof OFPGroupMod)) {
            return false;
        }
        OFPGroupMod other = (OFPGroupMod) obj;
        if (buckets == null) {
            if (other.buckets != null) {
                return false;
            }
        } else if (!buckets.equals(other.buckets)) {
            return false;
        }
        if (groupCommand != other.groupCommand) {
            return false;
        }
        if (groupType != other.groupType) {
            return false;
        }
        if (group_id != other.group_id) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public OFPGroupMod clone() {
        try {
            OFPGroupMod groupMod = (OFPGroupMod) super.clone();
            List<OFPBucket> neoBuckets = new LinkedList<OFPBucket>();
            for (OFPBucket bucket : this.buckets)
                neoBuckets.add((OFPBucket) bucket.clone());
            groupMod.setBuckets(neoBuckets);
            return groupMod;
        } catch (CloneNotSupportedException e) {
            // Won't happen
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFPGroupMod [ buckets=" + buckets + ", groupCommand=" + groupCommand + ", groupType=" + groupType
                + ", group_id=" + group_id + "]";
    }
}
