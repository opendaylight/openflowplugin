/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.util.concurrent.FutureCallback;
import java.math.BigInteger;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.group.stats.BucketStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.MultipartReplyGroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.multipart.reply.group.desc.GroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeaturesBuilder;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link OpendaylightGroupStatisticsServiceImpl}
 */
public class OpendaylightGroupStatisticsServiceImplTest extends AbstractSingleStatsServiceTest {

    private static final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId GROUP_ID =
            new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId(123L);
    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;

    private OpendaylightGroupStatisticsServiceImpl groupStatisticsService;

    public void setUp() {
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        groupStatisticsService = new OpendaylightGroupStatisticsServiceImpl(rqContextStack, deviceContext,
                new AtomicLong(), notificationPublishService, convertorManager);

        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(Matchers.eq(42L), requestInput.capture(), Matchers.any(FutureCallback.class));
    }

    @After
    public void tearDown() throws Exception {
        Mockito.verify(notificationPublishService).offerNotification(Matchers.<Notification>any());
    }

    @Test
    public void testGetAllGroupStatistics() throws Exception {
        GetAllGroupStatisticsInputBuilder input = new GetAllGroupStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = buildGroupStatsResponse();

        final Future<RpcResult<GetAllGroupStatisticsOutput>> resultFuture
                = groupStatisticsService.getAllGroupStatistics(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetAllGroupStatisticsOutput> rpcResultCompatible = resultFuture.get();
        Assert.assertTrue(rpcResultCompatible.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPGROUP, requestInput.getValue().getType());
    }

    @Test
    public void testGetGroupDescription() throws Exception {
        GetGroupDescriptionInputBuilder input = new GetGroupDescriptionInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = RpcResultBuilder.<Object>success(Collections.singletonList(
                new MultipartReplyMessageBuilder()
                        .setVersion(OFConstants.OFP_VERSION_1_3)
                        .setMultipartReplyBody(new MultipartReplyGroupDescCaseBuilder()
                                .setMultipartReplyGroupDesc(new MultipartReplyGroupDescBuilder()
                                        .setGroupDesc(Collections.singletonList(new GroupDescBuilder()
                                                .setGroupId(GROUP_ID)
                                                .setBucketsList(Collections.singletonList(new BucketsListBuilder()
                                                        .setWatchGroup(51L)
                                                        .setWatchPort(new PortNumber(52L))
                                                        .setWeight(53)
                                                        .build()))
                                                .setType(GroupType.OFPGTALL)
                                                .build()))
                                        .build())
                                .build())
                        .build()
        )).build();

        final Future<RpcResult<GetGroupDescriptionOutput>> resultFuture
                = groupStatisticsService.getGroupDescription(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetGroupDescriptionOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPGROUPDESC, requestInput.getValue().getType());
    }

    @Test
    public void testGetGroupFeatures() throws Exception {
        GetGroupFeaturesInputBuilder input = new GetGroupFeaturesInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = RpcResultBuilder.<Object>success(Collections.singletonList(
                new MultipartReplyMessageBuilder()
                        .setVersion(OFConstants.OFP_VERSION_1_3)
                        .setMultipartReplyBody(new MultipartReplyGroupFeaturesCaseBuilder()
                                .setMultipartReplyGroupFeatures(new MultipartReplyGroupFeaturesBuilder()
                                        .setActionsBitmap(Collections.singletonList(new ActionType(true,
                                                false, false, false, false, false, false, false, false, false, false,
                                                false, false, false, false, false, false)))
                                        .setCapabilities(new GroupCapabilities(true, false, false, false))
                                        .setTypes(new GroupTypes(true, false, false, false))
                                        .setMaxGroups(Collections.singletonList(5L))
                                        .build())
                                .build())
                        .build()
        )).build();

        final Future<RpcResult<GetGroupFeaturesOutput>> resultFuture
                = groupStatisticsService.getGroupFeatures(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetGroupFeaturesOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPGROUPFEATURES, requestInput.getValue().getType());
    }

    @Test
    public void testGetGroupStatistics() throws Exception {
        GetGroupStatisticsInputBuilder input = new GetGroupStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setGroupId(new GroupId(21L));

        rpcResult = buildGroupStatsResponse();

        final Future<RpcResult<GetGroupStatisticsOutput>> resultFuture
                = groupStatisticsService.getGroupStatistics(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetGroupStatisticsOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPGROUP, requestInput.getValue().getType());
    }

    private static RpcResult<Object> buildGroupStatsResponse() {
        return RpcResultBuilder.<Object>success(Collections.singletonList(
                new MultipartReplyMessageBuilder()
                        .setVersion(OFConstants.OFP_VERSION_1_3)
                        .setMultipartReplyBody(new MultipartReplyGroupCaseBuilder()
                                .setMultipartReplyGroup(new MultipartReplyGroupBuilder()
                                        .setGroupStats(Collections.singletonList(new GroupStatsBuilder()
                                                .setByteCount(BigInteger.valueOf(21L))
                                                .setPacketCount(BigInteger.valueOf(22L))
                                                .setRefCount(23L)
                                                .setDurationSec(24L)
                                                .setDurationNsec(25L)
                                                .setGroupId(GROUP_ID)
                                                .setBucketStats(Collections.singletonList(new BucketStatsBuilder()
                                                        .setByteCount(BigInteger.valueOf(26L))
                                                        .setPacketCount(BigInteger.valueOf(27L))
                                                        .build()))
                                                .build()))
                                        .build())
                                .build())
                        .build()
        )).build();
    }
}