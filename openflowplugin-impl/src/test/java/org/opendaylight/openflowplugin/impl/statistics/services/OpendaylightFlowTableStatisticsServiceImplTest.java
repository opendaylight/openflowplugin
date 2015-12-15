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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCase;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link OpendaylightFlowTableStatisticsServiceImpl}
 */
public class OpendaylightFlowTableStatisticsServiceImplTest extends AbstractSingleStatsServiceTest {

    private static final Short TABLE_ID = (short) 123;
    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;

    private OpendaylightFlowTableStatisticsServiceImpl flowTableStatisticsService;

    public void setUp() {
        flowTableStatisticsService = new OpendaylightFlowTableStatisticsServiceImpl(rqContextStack, deviceContext,
                new AtomicLong(), notificationPublishService);
    }

    @Test
    public void testGetFlowTablesStatistics() throws Exception {
        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(Matchers.eq(42L), requestInput.capture(), Matchers.any(FutureCallback.class));

        GetFlowTablesStatisticsInputBuilder input = new GetFlowTablesStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = RpcResultBuilder.<Object>success(Collections.singletonList(
                new MultipartReplyMessageBuilder()
                        .setVersion(OFConstants.OFP_VERSION_1_3)
                        .setMultipartReplyBody(new MultipartReplyTableCaseBuilder()
                                .setMultipartReplyTable(new MultipartReplyTableBuilder()
                                        .setTableStats(Collections.singletonList(new TableStatsBuilder()
                                                .setActiveCount(31L)
                                                .setLookupCount(BigInteger.valueOf(32L))
                                                .setMatchedCount(BigInteger.valueOf(33L))
                                                .setMaxEntries(34L)
                                                .setName("test-table")
                                                .setNwDstMask((short) 35)
                                                .setNwSrcMask((short) 36)
                                                .setTableId(TABLE_ID)
                                                .build()))
                                        .build())
                                .build())
                        .build()
        )).build();

        final Future<RpcResult<GetFlowTablesStatisticsOutput>> resultFuture
                = flowTableStatisticsService.getFlowTablesStatistics(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetFlowTablesStatisticsOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPTABLE, requestInput.getValue().getType());

        Mockito.verify(notificationPublishService).offerNotification(Matchers.<Notification>any());
    }

    @Test
    public void testBuildRequest() throws Exception {
        Xid xid = new Xid(42L);
        GetFlowTablesStatisticsInputBuilder input = new GetFlowTablesStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));
        final OfHeader request = flowTableStatisticsService.buildRequest(xid, input.build());
        Assert.assertTrue(request instanceof MultipartRequestInput);
        final MultipartRequestInput mpRequest = (MultipartRequestInput) request;
        Assert.assertEquals(MultipartType.OFPMPTABLE, mpRequest.getType());
        Assert.assertTrue(mpRequest.getMultipartRequestBody() instanceof MultipartRequestTableCase);
        final MultipartRequestTableCase mpRequestBody = (MultipartRequestTableCase) (mpRequest.getMultipartRequestBody());
        Assert.assertTrue(mpRequestBody.getMultipartRequestTable().isEmpty());
    }
}