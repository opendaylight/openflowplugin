package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.ActionsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.BucketsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.BucketsListBuilder;

/**
 * This Utility class decodes the SAL - Group Mod Message and encodes into a OF
 * Library for the OFPT_GROUP_MOD Message. Input:SAL Layer Group command data.
 * Output:GroupModInput Message.
 *
 * @param <AddGroupInput>
 *
 */
public final class GroupConvertor {
    private static final  Integer DEFAULT_WEIGHT = new Integer(0); 
    private static final Long OFPP_ANY = Long.parseLong("ffffffff", 16);
    private static final Long DEFAULT_WATCH_PORT = OFPP_ANY;
    private static final Long OFPG_ANY = Long.parseLong("ffffffff", 16);
    private static final Long DEFAULT_WATCH_GROUP = OFPG_ANY;

    private GroupConvertor() {

    }

    public static GroupModInput toGroupModInput(

    org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group source, short version) {
        List<BucketsList> bucketLists = new ArrayList<BucketsList>();
        GroupModInputBuilder groupModInputBuilder = new GroupModInputBuilder();
        if (source instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput)
            groupModInputBuilder.setCommand(GroupModCommand.OFPGCADD);
        if (source instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput)
            groupModInputBuilder.setCommand(GroupModCommand.OFPGCDELETE);
        if (source instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput)
            groupModInputBuilder.setCommand(GroupModCommand.OFPGCMODIFY);

        if (source.getGroupType().getIntValue() == 0)
            groupModInputBuilder.setType(GroupType.OFPGTALL);

        if (source.getGroupType().getIntValue() == 1)
            groupModInputBuilder.setType(GroupType.OFPGTSELECT);

        if (source.getGroupType().getIntValue() == 2)
            groupModInputBuilder.setType(GroupType.OFPGTINDIRECT);

        if (source.getGroupType().getIntValue() == 3)
            groupModInputBuilder.setType(GroupType.OFPGTFF);

        groupModInputBuilder.setGroupId(source.getGroupId().getValue());

        getbucketList(source.getBuckets(), bucketLists, version);
        groupModInputBuilder.setBucketsList(bucketLists);
        groupModInputBuilder.setVersion(version);
        return groupModInputBuilder.build();

    }

    private static void getbucketList(Buckets buckets, List<BucketsList> bucketLists, short version) {

        Iterator<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket> groupBucketIterator = buckets
                .getBucket().iterator();
        org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket groupBucket;



        while (groupBucketIterator.hasNext()) {
            groupBucket = groupBucketIterator.next();
            BucketsListBuilder bucketBuilder = new BucketsListBuilder();
            
            if (null != groupBucket.getWeight()) {
                bucketBuilder.setWeight(groupBucket.getWeight().intValue());
            } else {
                bucketBuilder.setWeight(DEFAULT_WEIGHT);
            }
            
            if (null != groupBucket.getWatchGroup()) {
                bucketBuilder.setWatchGroup(groupBucket.getWatchGroup());
            } else {
                bucketBuilder.setWatchGroup(BinContent.intToUnsignedLong(DEFAULT_WATCH_GROUP.intValue()));
            }
            
            if (null != groupBucket.getWatchPort()) {
                bucketBuilder.setWatchPort(new PortNumber(groupBucket.getWatchPort()));
            } else {
                
                bucketBuilder.setWatchPort(new PortNumber(BinContent.intToUnsignedLong(DEFAULT_WATCH_PORT.intValue())));
            }

            List<ActionsList> bucketActionList = ActionConvertor.getActionList(groupBucket.getAction(), version);
            bucketBuilder.setActionsList(bucketActionList);
            BucketsList bucket = bucketBuilder.build();
            bucketLists.add(bucket);
        }

    }
    
}
