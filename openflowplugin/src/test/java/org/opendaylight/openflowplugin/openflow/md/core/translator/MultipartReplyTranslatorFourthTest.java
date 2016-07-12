/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.translator;

import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupDescStatsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopPbbCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.group.stats.BucketStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.group.stats.BucketStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.MultipartReplyGroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.multipart.reply.group.desc.GroupDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.multipart.reply.group.desc.GroupDescBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * @author michal.polkorab
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MultipartReplyTranslatorFourthTest {

    @Mock SwitchConnectionDistinguisher cookie;
    @Mock SessionContext sc;
    @Mock ConnectionConductor conductor;
    @Mock GetFeaturesOutput features;

    MultipartReplyTranslator translator;

    /**
     * Initializes mocks
     */
    @Before
    public void startUp() {
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        translator = new MultipartReplyTranslator(convertorManager);
        when(sc.getPrimaryConductor()).thenReturn(conductor);
        when(conductor.getVersion()).thenReturn((short) EncodeConstants.OF13_VERSION_ID);
        when(sc.getFeatures()).thenReturn(features);
        when(features.getDatapathId()).thenReturn(new BigInteger("42"));
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with empty group stats
     */
    @Test
    public void testEmptyGroupStats() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPGROUP);

        MultipartReplyGroupCaseBuilder caseBuilder = new MultipartReplyGroupCaseBuilder();
        MultipartReplyGroupBuilder groupBuilder = new MultipartReplyGroupBuilder();
        List<GroupStats> groupStats = new ArrayList<>();
        groupBuilder.setGroupStats(groupStats);
        caseBuilder.setMultipartReplyGroup(groupBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();

        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        GroupStatisticsUpdated statUpdate = (GroupStatisticsUpdated) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", statUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, statUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, statUpdate.getTransactionId().getValue().intValue());
        Assert.assertEquals("Wrong group stats size", 0, statUpdate.getGroupStats().size());
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with group stats
     */
    @Test
    public void testGroupStats() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPGROUP);

        MultipartReplyGroupCaseBuilder caseBuilder = new MultipartReplyGroupCaseBuilder();
        MultipartReplyGroupBuilder groupBuilder = new MultipartReplyGroupBuilder();
        List<GroupStats> groupStats = new ArrayList<>();
        GroupStatsBuilder builder = new GroupStatsBuilder();
        builder.setGroupId(new GroupId(55L));
        builder.setRefCount(56L);
        builder.setPacketCount(new BigInteger("57"));
        builder.setByteCount(new BigInteger("58"));
        builder.setDurationSec(59L);
        builder.setDurationNsec(60L);
        List<BucketStats> bucketStats = new ArrayList<>();
        BucketStatsBuilder bucketBuilder = new BucketStatsBuilder();
        bucketBuilder.setByteCount(new BigInteger("61"));
        bucketBuilder.setPacketCount(new BigInteger("62"));
        bucketStats.add(bucketBuilder.build());
        bucketBuilder = new BucketStatsBuilder();
        bucketBuilder.setByteCount(new BigInteger("63"));
        bucketBuilder.setPacketCount(new BigInteger("64"));
        bucketStats.add(bucketBuilder.build());
        builder.setBucketStats(bucketStats);
        groupStats.add(builder.build());
        builder = new GroupStatsBuilder();
        builder.setGroupId(new GroupId(550L));
        builder.setRefCount(560L);
        builder.setPacketCount(new BigInteger("570"));
        builder.setByteCount(new BigInteger("580"));
        builder.setDurationSec(590L);
        builder.setDurationNsec(600L);
        bucketStats = new ArrayList<>();
        builder.setBucketStats(bucketStats);
        groupStats.add(builder.build());
        groupBuilder.setGroupStats(groupStats);
        caseBuilder.setMultipartReplyGroup(groupBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();

        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        GroupStatisticsUpdated statUpdate = (GroupStatisticsUpdated) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", statUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, statUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, statUpdate.getTransactionId().getValue().intValue());
        Assert.assertEquals("Wrong group stats size", 2, statUpdate.getGroupStats().size());
        org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
        .GroupStats stat = statUpdate.getGroupStats().get(0);
        Assert.assertEquals("Wrong group-id", 55, stat.getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong ref count", 56, stat.getRefCount().getValue().intValue());
        Assert.assertEquals("Wrong packet count", 57, stat.getPacketCount().getValue().intValue());
        Assert.assertEquals("Wrong byte count", 58, stat.getByteCount().getValue().intValue());
        Assert.assertEquals("Wrong duration sec", 59, stat.getDuration().getSecond().getValue().intValue());
        Assert.assertEquals("Wrong duration n sec", 60, stat.getDuration().getNanosecond().getValue().intValue());
        Assert.assertEquals("Wrong bucket stats size", 2, stat.getBuckets().getBucketCounter().size());
        Assert.assertEquals("Wrong bucket-id", 0, stat.getBuckets().getBucketCounter().get(0).getBucketId().getValue().intValue());
        Assert.assertEquals("Wrong byte count", 61, stat.getBuckets().getBucketCounter().get(0).getByteCount().getValue().intValue());
        Assert.assertEquals("Wrong packet count", 62, stat.getBuckets().getBucketCounter().get(0).getPacketCount().getValue().intValue());
        Assert.assertEquals("Wrong bucket-id", 1, stat.getBuckets().getBucketCounter().get(1).getBucketId().getValue().intValue());
        Assert.assertEquals("Wrong byte count", 63, stat.getBuckets().getBucketCounter().get(1).getByteCount().getValue().intValue());
        Assert.assertEquals("Wrong packet count", 64, stat.getBuckets().getBucketCounter().get(1).getPacketCount().getValue().intValue());
        stat = statUpdate.getGroupStats().get(1);
        Assert.assertEquals("Wrong group-id", 550, stat.getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong ref count", 560, stat.getRefCount().getValue().intValue());
        Assert.assertEquals("Wrong packet count", 570, stat.getPacketCount().getValue().intValue());
        Assert.assertEquals("Wrong byte count", 580, stat.getByteCount().getValue().intValue());
        Assert.assertEquals("Wrong duration sec", 590, stat.getDuration().getSecond().getValue().intValue());
        Assert.assertEquals("Wrong duration n sec", 600, stat.getDuration().getNanosecond().getValue().intValue());
        Assert.assertEquals("Wrong bucket stats size", 0, stat.getBuckets().getBucketCounter().size());
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with empty group desc stats
     */
    @Test
    public void testEmptyGroupDescStats() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPGROUPDESC);

        MultipartReplyGroupDescCaseBuilder caseBuilder = new MultipartReplyGroupDescCaseBuilder();
        MultipartReplyGroupDescBuilder groupBuilder = new MultipartReplyGroupDescBuilder();
        List<GroupDesc> groupStats = new ArrayList<>();
        groupBuilder.setGroupDesc(groupStats);
        caseBuilder.setMultipartReplyGroupDesc(groupBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();

        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        GroupDescStatsUpdated statUpdate = (GroupDescStatsUpdated) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", statUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, statUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, statUpdate.getTransactionId().getValue().intValue());
        Assert.assertEquals("Wrong group stats size", 0, statUpdate.getGroupDescStats().size());
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with group desc stats
     */
    @Test
    public void testGroupDescStats() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPGROUPDESC);

        MultipartReplyGroupDescCaseBuilder caseBuilder = new MultipartReplyGroupDescCaseBuilder();
        MultipartReplyGroupDescBuilder groupBuilder = new MultipartReplyGroupDescBuilder();
        List<GroupDesc> groupStats = new ArrayList<>();

        GroupDescBuilder builder = new GroupDescBuilder();
        builder.setType(GroupType.OFPGTALL);
        builder.setGroupId(new GroupId(1L));
        List<BucketsList> buckets = new ArrayList<>();
        BucketsListBuilder bucketBuilder = new BucketsListBuilder();
        bucketBuilder.setWeight(28);
        bucketBuilder.setWatchPort(new PortNumber(56L));
        bucketBuilder.setWatchGroup(112L);

        List<Action> actions = new ArrayList<>();
        ActionBuilder actionBuilder = new ActionBuilder();
        DecMplsTtlCaseBuilder decMplsTtlCaseBuilder = new DecMplsTtlCaseBuilder();
        actionBuilder.setActionChoice(decMplsTtlCaseBuilder.build());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        PopPbbCaseBuilder popPbbCaseBuilder = new PopPbbCaseBuilder();
        actionBuilder.setActionChoice(popPbbCaseBuilder.build());
        actions.add(actionBuilder.build());

        bucketBuilder.setAction(actions);
        buckets.add(bucketBuilder.build());
        bucketBuilder = new BucketsListBuilder();
        bucketBuilder.setWeight(280);
        bucketBuilder.setWatchPort(new PortNumber(560L));
        bucketBuilder.setWatchGroup(1120L);
        actions = new ArrayList<>();
        bucketBuilder.setAction(actions);
        buckets.add(bucketBuilder.build());
        builder.setBucketsList(buckets);
        groupStats.add(builder.build());
        groupBuilder.setGroupDesc(groupStats);
        caseBuilder.setMultipartReplyGroupDesc(groupBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();

        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        GroupDescStatsUpdated statUpdate = (GroupDescStatsUpdated) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", statUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, statUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, statUpdate.getTransactionId().getValue().intValue());
        Assert.assertEquals("Wrong group stats size", 1, statUpdate.getGroupDescStats().size());
        GroupDescStats stat = statUpdate.getGroupDescStats().get(0);
        Assert.assertEquals("Wrong group-id", 1, stat.getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong group type", GroupTypes.GroupAll, stat.getGroupType());
        Assert.assertEquals("Wrong buckets size", 2, stat.getBuckets().getBucket().size());
        Bucket bucket = stat.getBuckets().getBucket().get(0);
        Assert.assertEquals("Wrong weight size", 28, bucket.getWeight().intValue());
        Assert.assertEquals("Wrong watch port size", 56, bucket.getWatchPort().intValue());
        Assert.assertEquals("Wrong watch group size", 112, bucket.getWatchGroup().intValue());
        Assert.assertEquals("Wrong actions size", 2, bucket.getAction().size());
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112"
                + ".action.action.DecMplsTtlCase", bucket.getAction().get(0).getAction().getImplementedInterface().getName());
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112"
                + ".action.action.PopPbbActionCase", bucket.getAction().get(1).getAction().getImplementedInterface().getName());
        bucket = stat.getBuckets().getBucket().get(1);
        Assert.assertEquals("Wrong weight size", 280, bucket.getWeight().intValue());
        Assert.assertEquals("Wrong watch port size", 560, bucket.getWatchPort().intValue());
        Assert.assertEquals("Wrong watch group size", 1120, bucket.getWatchGroup().intValue());
        Assert.assertEquals("Wrong actions size", 0, bucket.getAction().size());
    }
}