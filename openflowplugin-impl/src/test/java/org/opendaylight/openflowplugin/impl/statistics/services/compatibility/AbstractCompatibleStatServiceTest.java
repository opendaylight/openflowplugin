/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.compatibility;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import java.math.BigInteger;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.impl.statistics.services.AbstractStatsServiceTest;
import org.opendaylight.openflowplugin.impl.statistics.services.AggregateFlowsInTableService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.AggregateFlowStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.get.aggregate.flow.statistics.from.flow.table._for.given.match.output.AggregatedFlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.get.aggregate.flow.statistics.from.flow.table._for.given.match.output.AggregatedFlowStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test for {@link AbstractCompatibleStatService}.
 */
public class AbstractCompatibleStatServiceTest extends AbstractStatsServiceTest {

    private static final NodeId NODE_ID = new NodeId("unit-test-node:123");
    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;
    @Mock
    private NotificationPublishService notificationPublishService;
    @Mock
    private DeviceState deviceState;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private MessageTranslator<Object, Object> translator;
    @Mock
    private GetFeaturesOutput featuresOutput;

    private AbstractRequestContext<Object> rqContext;

    private RpcResult<Object> rpcResult;

    private AbstractCompatibleStatService<GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput,
            GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput, AggregateFlowStatisticsUpdate> service;

    @Override
    public void setUp() {
        rqContext = new AbstractRequestContext<>(Uint32.valueOf(42)) {
            @Override
            public void close() {
                //NOOP
            }
        };
        final Answer closeRequestFutureAnswer = invocation -> {
            rqContext.setResult(rpcResult);
            rqContext.close();
            return null;
        };

        Mockito.lenient().when(featuresOutput.getVersion()).thenReturn(Uint8.valueOf(OFConstants.OFP_VERSION_1_3));
        Mockito.when(rqContextStack.createRequestContext()).thenReturn(rqContext);
        Mockito.lenient().when(deviceContext.getDeviceState()).thenReturn(deviceState);
        Mockito.when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        Mockito.when(deviceInfo.getNodeId()).thenReturn(NODE_ID);
        Mockito.when(deviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        Mockito.doAnswer(closeRequestFutureAnswer).when(multiMsgCollector).endCollecting(null);
        Mockito.lenient().doAnswer(closeRequestFutureAnswer).when(multiMsgCollector)
                .endCollecting(any(EventIdentifier.class));

        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(eq(42L), requestInput.capture(), any(FutureCallback.class));

        Mockito.when(translatorLibrary.lookupTranslator(any(TranslatorKey.class))).thenReturn(translator);

        service = AggregateFlowsInTableService.createWithOook(rqContextStack, deviceContext, new AtomicLong(20L));
    }

    @Test
    public void testGetOfVersion() {
        Assert.assertEquals(OFConstants.OFP_VERSION_1_3, service.getOfVersion().getVersion());
    }

    @Test
    public void testHandleAndNotify() throws Exception {
        GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput input =
                new GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder()
                        .setNode(createNodeRef("unitProt:123"))
                        .setTableId(new TableId(Uint8.ONE))
                        .build();

        rpcResult = RpcResultBuilder.<Object>success(Collections.singletonList(
                new MultipartReplyMessageBuilder()
                        .setVersion(OFConstants.OFP_VERSION_1_3)
                        .setMultipartReplyBody(new MultipartReplyAggregateCaseBuilder()
                                .setMultipartReplyAggregate(new MultipartReplyAggregateBuilder()
                                        .setByteCount(Uint64.valueOf(11))
                                        .setFlowCount(12L)
                                        .setPacketCount(Uint64.valueOf(13))
                                        .build())
                                .build())
                        .build()
        )).build();

        AggregatedFlowStatistics aggregatedStats = new AggregatedFlowStatisticsBuilder()
                .setByteCount(new Counter64(BigInteger.valueOf(11L)))
                .setFlowCount(new Counter32(12L))
                .setPacketCount(new Counter64(BigInteger.valueOf(13L)))
                .build();
        Mockito.when(translator.translate(any(MultipartReply.class), eq(deviceInfo), any()))
                .thenReturn(aggregatedStats);


        ListenableFuture<RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>> resultFuture =
                service.handleAndNotify(input, notificationPublishService);

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput> result = resultFuture.get();
        Assert.assertTrue(result.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPAGGREGATE, requestInput.getValue().getType());
        Mockito.verify(notificationPublishService, Mockito.timeout(500))
                .offerNotification(any(AggregateFlowStatisticsUpdate.class));
    }
}
