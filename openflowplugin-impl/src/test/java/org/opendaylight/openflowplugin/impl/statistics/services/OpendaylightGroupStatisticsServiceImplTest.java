/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.FutureCallback;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsInputBuilder;
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
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

@Deprecated
public class OpendaylightGroupStatisticsServiceImplTest extends AbstractSingleStatsServiceTest {
    private static final org.opendaylight.yang.gen.v1.urn
            .opendaylight.openflow.common.types.rev130731.GroupId GROUP_ID = new org.opendaylight.yang.gen.v1.urn
                .opendaylight.openflow.common.types.rev130731.GroupId(Uint32.valueOf(123));
    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;

    private GetAllGroupStatisticsImpl getAllGroupStatistics;
    private GetGroupStatisticsImpl getGroupStatistics;
    private GetGroupDescriptionImpl getGroupDescription;
    private GetGroupFeaturesImpl getGroupFeatures;

    @Override
    public void setUp() {
        final var convertorManager = ConvertorManagerFactory.createDefaultManager();
        final var xid = new AtomicLong();
        getAllGroupStatistics = new GetAllGroupStatisticsImpl(rqContextStack, deviceContext, xid,
            notificationPublishService, convertorManager);
        getGroupStatistics = new GetGroupStatisticsImpl(rqContextStack, deviceContext, xid, notificationPublishService,
            convertorManager);
        getGroupDescription = new GetGroupDescriptionImpl(rqContextStack, deviceContext, xid,
            notificationPublishService, convertorManager);
        getGroupFeatures = new GetGroupFeaturesImpl(rqContextStack, deviceContext, xid, notificationPublishService,
            convertorManager);

        doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(eq(Uint32.valueOf(42)), requestInput.capture(), any(FutureCallback.class));
    }

    @After
    public void tearDown() {
        verify(notificationPublishService).offerNotification(any());
    }

    @Test
    public void testGetAllGroupStatistics() throws Exception {
        GetAllGroupStatisticsInputBuilder input = new GetAllGroupStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = buildGroupStatsResponse();

        final var resultFuture = getAllGroupStatistics.invoke(input.build());

        assertTrue(resultFuture.isDone());
        final var rpcResultCompatible = resultFuture.get();
        assertTrue(rpcResultCompatible.isSuccessful());
        assertEquals(MultipartType.OFPMPGROUP, requestInput.getValue().getType());
    }

    @Test
    public void testGetGroupDescription() throws Exception {
        GetGroupDescriptionInputBuilder input = new GetGroupDescriptionInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = RpcResultBuilder.<Object>success(List.of(new MultipartReplyMessageBuilder()
            .setVersion(EncodeConstants.OF_VERSION_1_3)
            .setMultipartReplyBody(new MultipartReplyGroupDescCaseBuilder()
                .setMultipartReplyGroupDesc(new MultipartReplyGroupDescBuilder()
                    .setGroupDesc(List.of(new GroupDescBuilder()
                        .setGroupId(GROUP_ID)
                        .setBucketsList(List.of(new BucketsListBuilder()
                            .setWatchGroup(Uint32.valueOf(51))
                            .setWatchPort(new PortNumber(Uint32.valueOf(52)))
                            .setWeight(Uint16.valueOf(53))
                            .build()))
                        .setType(GroupType.OFPGTALL)
                        .build()))
                    .build())
                .build())
            .build()
        )).build();

        final var resultFuture = getGroupDescription.invoke(input.build());

        assertTrue(resultFuture.isDone());
        final var rpcResult = resultFuture.get();
        assertTrue(rpcResult.isSuccessful());
        assertEquals(MultipartType.OFPMPGROUPDESC, requestInput.getValue().getType());
    }

    @Test
    public void testGetGroupFeatures() throws Exception {
        GetGroupFeaturesInputBuilder input = new GetGroupFeaturesInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = RpcResultBuilder.<Object>success(List.of(new MultipartReplyMessageBuilder()
            .setVersion(EncodeConstants.OF_VERSION_1_3)
            .setMultipartReplyBody(new MultipartReplyGroupFeaturesCaseBuilder()
                .setMultipartReplyGroupFeatures(new MultipartReplyGroupFeaturesBuilder()
                    .setActionsBitmap(List.of(new ActionType(true, false, false, false, false, false, false, false,
                        false, false, false, false, false, false, false, false, false)))
                    .setCapabilities(new GroupCapabilities(true, false, false, false))
                    .setTypes(new GroupTypes(true, false, false, false))
                    .setMaxGroups(List.of(Uint32.valueOf(5L)))
                    .build())
                .build())
            .build()
        )).build();

        final var resultFuture = getGroupFeatures.invoke(input.build());

        assertTrue(resultFuture.isDone());
        final var rpcResult = resultFuture.get();
        assertTrue(rpcResult.isSuccessful());
        assertEquals(MultipartType.OFPMPGROUPFEATURES, requestInput.getValue().getType());
    }

    @Test
    public void testGetGroupStatistics() throws Exception {
        GetGroupStatisticsInputBuilder input = new GetGroupStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setGroupId(new GroupId(Uint32.valueOf(21)));

        rpcResult = buildGroupStatsResponse();

        final var resultFuture = getGroupStatistics.invoke(input.build());

        assertTrue(resultFuture.isDone());
        final var rpcResult = resultFuture.get();
        assertTrue(rpcResult.isSuccessful());
        assertEquals(MultipartType.OFPMPGROUP, requestInput.getValue().getType());
    }

    private static RpcResult<Object> buildGroupStatsResponse() {
        return RpcResultBuilder.<Object>success(List.of(new MultipartReplyMessageBuilder()
            .setVersion(EncodeConstants.OF_VERSION_1_3)
            .setMultipartReplyBody(new MultipartReplyGroupCaseBuilder()
                .setMultipartReplyGroup(new MultipartReplyGroupBuilder()
                    .setGroupStats(List.of(new GroupStatsBuilder()
                        .setByteCount(Uint64.valueOf(21))
                        .setPacketCount(Uint64.valueOf(22))
                        .setRefCount(Uint32.valueOf(23))
                        .setDurationSec(Uint32.valueOf(24))
                        .setDurationNsec(Uint32.valueOf(25))
                        .setGroupId(GROUP_ID)
                        .setBucketStats(List.of(new BucketStatsBuilder()
                            .setByteCount(Uint64.valueOf(26))
                            .setPacketCount(Uint64.valueOf(27))
                            .build()))
                        .build()))
                    .build())
                .build())
            .build()
        )).build();
    }
}