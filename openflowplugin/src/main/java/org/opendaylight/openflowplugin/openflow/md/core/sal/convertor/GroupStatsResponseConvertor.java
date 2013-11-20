package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.buckets.BucketCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.buckets.BucketCounterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc.GroupDesc;

/**
 * Class is an utility class for converting group related statistics messages coming from switch to MD-SAL
 * messages.
 * @author avishnoi@in.ibm.com
 *
 */
public class GroupStatsResponseConvertor {

    public List<GroupStats> toSALGroupStatsList(
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.GroupStats> allGroupStats){
        List<GroupStats> convertedSALGroups = new ArrayList<GroupStats>();
        for(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.GroupStats group: allGroupStats){
            convertedSALGroups.add(toSALGroupStats(group));
        }
        return convertedSALGroups;
        
    }
    /**
     * Method convert GroupStats message from library to MD SAL defined GroupStats  
     * @param groupStats GroupStats from library
     * @return GroupStats -- GroupStats defined in MD-SAL
     */
    public GroupStats toSALGroupStats(
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.GroupStats groupStats){
        
        GroupStatsBuilder salGroupStats = new GroupStatsBuilder();
        
        salGroupStats.setBuckets(toSALBuckets(groupStats.getBucketStats()));
        salGroupStats.setByteCount(new Counter64(groupStats.getByteCount()));
        
        DurationBuilder time = new DurationBuilder();
        time.setSecond(new Counter32(groupStats.getDurationSec()));
        time.setNanosecond(new Counter32(groupStats.getDurationNsec()));
        
        salGroupStats.setDuration(time.build());
        salGroupStats.setGroupId(groupStats.getGroupId().intValue());
        salGroupStats.setPacketCount(new Counter64(groupStats.getPacketCount()));
        salGroupStats.setRefCount(new Counter32(groupStats.getRefCount()));
        
        return salGroupStats.build();
    }
    
    public Buckets toSALBuckets(
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.group.stats.BucketStats> bucketStats ){
        
        BucketsBuilder salBuckets  = new BucketsBuilder();
        
        List<BucketCounter> allBucketStats = new ArrayList<BucketCounter>();
        
        for( org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.group.stats.BucketStats bucketStat : bucketStats){
            BucketCounterBuilder bucketCounter = new BucketCounterBuilder();
            bucketCounter.setByteCount(new Counter64(bucketStat.getByteCount()));
            bucketCounter.setPacketCount(new Counter64(bucketStat.getPacketCount()));
            allBucketStats.add(bucketCounter.build());
        }
        salBuckets.setBucketCounter(allBucketStats);
        return salBuckets.build();
    }
    
    
    public List<GroupDescStats> toSALGroupDescStatsList(
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.
            multipart.reply.multipart.reply.body.multipart.reply.group.desc.GroupDesc> allGroupDescStats){
        
        List<GroupDescStats> convertedSALGroupsDesc = new ArrayList<GroupDescStats>();
        for(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc.GroupDesc groupDesc: allGroupDescStats){
            convertedSALGroupsDesc.add(toSALGroupDescStats(groupDesc));
        }
        return convertedSALGroupsDesc;
        
    }
    /**
     * Method convert GroupStats message from library to MD SAL defined GroupStats  
     * @param groupDesc GroupStats from library
     * @return GroupStats -- GroupStats defined in MD-SAL
     */
    public GroupDescStats toSALGroupDescStats(GroupDesc groupDesc){
        
        GroupDescStatsBuilder salGroupDescStats = new GroupDescStatsBuilder();
        
        salGroupDescStats.setBuckets(toSALBucketsDesc(groupDesc.getBucketsList()));
        salGroupDescStats.setGroupId(new GroupId(groupDesc.getGroupId()));
        salGroupDescStats.setGroupType(GroupType.forValue(groupDesc.getType().getIntValue()));
        
        return salGroupDescStats.build();
    }
    
    public  org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets toSALBucketsDesc(
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.BucketsList> bucketDescStats ){
        
        org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder salBucketsDesc  = 
                new org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder();
        
        List<Bucket> allBuckets = new ArrayList<Bucket>();
        
        for( org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.BucketsList bucketDetails : bucketDescStats){
            BucketBuilder bucketDesc = new BucketBuilder();
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> convertedSalActions = 
                    ActionConvertor.toSALBucketActions (bucketDetails.getActionsList());
            
            List<Action> actions = new ArrayList<Action>(); 
            for (org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action : convertedSalActions){
                ActionBuilder warppedAction = new ActionBuilder();
                warppedAction.setAction(action);
                actions.add(warppedAction.build());
            }
            bucketDesc.setAction(actions);
            bucketDesc.setWeight(bucketDetails.getWeight());
            bucketDesc.setWatchPort(bucketDetails.getWatchPort().getValue());
            bucketDesc.setWatchGroup(bucketDetails.getWatchGroup());
            allBuckets.add(bucketDesc.build());
        }
        salBucketsDesc.setBucket(allBuckets);
        return salBucketsDesc.build();
    }

}
