/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.mp.MBodyGroupStats;
import org.opendaylight.of.lib.mp.MBodyGroupStats.BucketCounter;
import org.opendaylight.of.lib.mp.MBodyMutableGroupStats;

import java.util.List;

import static org.opendaylight.of.json.CodecUtils.decodeProtocolVersion;

/**
 * A JSON codec capable of encoding and decoding {@link MBodyGroupStats} 
 * objects.
 *
 * @author Prashant Nayak
 *
 */
public class MBodyGroupStatsCodec extends OfJsonCodec<MBodyGroupStats> {
    
    public static final String ROOT = "group_stats";
    public static final String ROOTS = "group_stats";
   
    private static final String GROUP_ID = "id";
    private static final String REF_COUNT = "ref_count";
    private static final String DURATION_SEC = "duration_sec";
    private static final String DURATION_NSEC = "duration_nsec";
    private static final String PACKET_COUNT = "packet_count";
    private static final String BYTE_COUNT = "byte_count";
    
    /** JSON key for bucket stats */
    private static final String BUCKET_STATS = "bucket_stats";
    

    protected MBodyGroupStatsCodec() {
        super(ROOT, ROOTS);
    }

    @Override
    public ObjectNode encode(MBodyGroupStats mbgs) {
        ObjectNode groupNode = objectNode();
        
        groupNode.put(GROUP_ID, mbgs.getGroupId().toLong());
        groupNode.put(REF_COUNT, mbgs.getRefCount());
        groupNode.put(PACKET_COUNT, mbgs.getPacketCount());
        groupNode.put(BYTE_COUNT, mbgs.getByteCount());
        groupNode.put(DURATION_SEC, mbgs.getDurationSec());
        groupNode.put(DURATION_NSEC, mbgs.getDurationNsec());
        groupNode.put(BUCKET_STATS, encodeBuckets(mbgs));
        
        return groupNode;
    }
    
    private ArrayNode encodeBuckets(MBodyGroupStats groupStat) {      
        List<BucketCounter> bktCounter = groupStat.getBucketStats();
        ArrayNode bucketsNode = mapper.createArrayNode();
        if(bktCounter != null){
            for(BucketCounter bucket : bktCounter){
                ObjectNode bucketNode = mapper.createObjectNode();
                bucketNode.put(PACKET_COUNT, bucket.getPacketCount());
                bucketNode.put(BYTE_COUNT, bucket.getByteCount());
                
                bucketsNode.add(bucketNode);
            }
        }
        return bucketsNode;
    }


    @Override
    public MBodyGroupStats decode(ObjectNode groupNode) {
        ProtocolVersion ver = decodeProtocolVersion(groupNode.get(VERSION));
        
        MBodyMutableGroupStats groupStats = new MBodyMutableGroupStats(ver);
        
        groupStats.groupId(GroupId.valueOf 
                               (groupNode.get(GROUP_ID).longValue()));
        groupStats.refCount(groupNode.get(REF_COUNT).asLong());
        groupStats.packetCount(groupNode.get(PACKET_COUNT).asLong());
        groupStats.byteCount(groupNode.get(BYTE_COUNT).asLong());
        groupStats.duration(groupNode.get(DURATION_SEC).asLong(), 
                            groupNode.get(DURATION_NSEC).asLong());
        
        JsonNode node = groupNode.get(BUCKET_STATS);
        for(JsonNode bktNode: node) {
            ObjectNode bucketNode = (ObjectNode) bktNode;
            groupStats.addBucketStats(bucketNode.get(PACKET_COUNT).asLong(), 
                                      bucketNode.get(BYTE_COUNT).asLong());
        }
        
        return (MBodyGroupStats)groupStats.toImmutable();
    }

}
