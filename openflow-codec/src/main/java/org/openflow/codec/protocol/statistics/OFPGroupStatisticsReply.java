package org.openflow.codec.protocol.statistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.action.OFPBucketCounter;
import org.openflow.codec.util.U16;

/**
 * Represents an ofp_group_stats structure
 *
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com)
 */
public class OFPGroupStatisticsReply implements OFPStatistics, Serializable {
    public static int MINIMUM_LENGTH = 40;

    protected short length = (short) MINIMUM_LENGTH;
    protected int group_id;
    protected int ref_count;
    protected long packet_count;
    protected long byte_count;
    protected int duration_sec;
    protected int duration_nsec;
    protected List<OFPBucketCounter> bucket_stats;

    public void setLength(short length) {
        this.length = length;
    }

    @Override
    public int getLength() {
        return U16.f(length);
    }

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    public int getRef_count() {
        return ref_count;
    }

    public void setRef_count(int ref_count) {
        this.ref_count = ref_count;
    }

    public long getPacket_count() {
        return packet_count;
    }

    public void setPacket_count(long packet_count) {
        this.packet_count = packet_count;
    }

    public long getByte_count() {
        return byte_count;
    }

    public void setByte_count(long byte_count) {
        this.byte_count = byte_count;
    }

    public int getDuration_sec() {
        return duration_sec;
    }

    public void setDuration_sec(int duration_sec) {
        this.duration_sec = duration_sec;
    }

    public int getDuration_nsec() {
        return duration_nsec;
    }

    public void setDuration_nsec(int duration_nsec) {
        this.duration_nsec = duration_nsec;
    }

    public List<OFPBucketCounter> getBucket_stats() {
        return bucket_stats;
    }

    public void setBucket_stats(List<OFPBucketCounter> bucket_stats) {
        this.bucket_stats = bucket_stats;
        if (bucket_stats == null) {
            this.setLength((short) MINIMUM_LENGTH);
        } else {
            this.setLength((short) (MINIMUM_LENGTH + bucket_stats.size() * OFPBucketCounter.MINIMUM_LENGTH));
        }
    }

    @Override
    public void readFrom(IDataBuffer data) {
        this.length = data.getShort();
        data.getShort();
        this.group_id = data.getInt();
        this.ref_count = data.getInt();
        data.getInt();
        this.packet_count = data.getLong();
        this.byte_count = data.getLong();
        this.duration_sec = data.getInt();
        this.duration_nsec = data.getInt();
        if (this.bucket_stats == null) {
            this.bucket_stats = new ArrayList<OFPBucketCounter>();
        } else {
            this.bucket_stats.clear();
        }
        int bucketCounterCount = (this.getLength() - 38) / OFPBucketCounter.MINIMUM_LENGTH;
        OFPBucketCounter bucketCounter;
        for (int i = 0; i < bucketCounterCount; ++i) {
            bucketCounter = new OFPBucketCounter();
            bucketCounter.readFrom(data);
            this.bucket_stats.add(bucketCounter);
        }
    }

    @Override
    public void writeTo(IDataBuffer data) {
        data.putShort(length);
        data.putShort((short) 0);
        data.putInt(group_id);
        data.putInt(ref_count);
        data.putInt(0);
        data.putLong(packet_count);
        data.putLong(byte_count);
        data.putInt(duration_sec);
        data.putInt(duration_nsec);
        if (bucket_stats != null) {
            for (OFPBucketCounter bucketCounter : bucket_stats) {
                bucketCounter.writeTo(data);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 419;
        int result = 1;
        result = prime * result + ((bucket_stats == null) ? 0 : bucket_stats.hashCode());
        result = prime * result + length;
        result = prime * result + group_id;
        result = prime * result + ref_count;
        result = prime * result + (int) (packet_count ^ (packet_count >>> 32));
        result = prime * result + (int) (byte_count ^ (byte_count >>> 32));
        result = prime * result + duration_sec;
        result = prime * result + duration_nsec;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OFPGroupStatisticsReply)) {
            return false;
        }
        OFPGroupStatisticsReply other = (OFPGroupStatisticsReply) obj;
        if (bucket_stats == null) {
            if (other.bucket_stats != null) {
                return false;
            }
        } else if (!bucket_stats.equals(other.bucket_stats)) {
            return false;
        }
        if (length != other.length) {
            return false;
        }
        if (group_id != other.group_id) {
            return false;
        }
        if (ref_count != other.ref_count) {
            return false;
        }
        if (packet_count != other.packet_count) {
            return false;
        }
        if (byte_count != other.byte_count) {
            return false;
        }
        if (duration_sec != other.duration_sec) {
            return false;
        }
        if (duration_nsec != other.duration_nsec) {
            return false;
        }
        return true;
    }
}
