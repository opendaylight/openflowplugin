/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import com.google.common.util.concurrent.FutureCallback;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
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
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test for {@link OpendaylightFlowTableStatisticsServiceImpl}.
 */
public class OpendaylightFlowTableStatisticsServiceImplTest extends AbstractSingleStatsServiceTest {
    private static final Uint8 TABLE_ID = Uint8.valueOf(123);

    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;

    private OpendaylightFlowTableStatisticsServiceImpl flowTableStatisticsService;

    @Override
    public void setUp() {
        flowTableStatisticsService = new OpendaylightFlowTableStatisticsServiceImpl(rqContextStack, deviceContext,
                new AtomicLong(), notificationPublishService);
    }

    @Test
    public void testGetFlowTablesStatistics() throws Exception {
        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(eq(42L), requestInput.capture(), any(FutureCallback.class));

        GetFlowTablesStatisticsInputBuilder input = new GetFlowTablesStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = RpcResultBuilder.<Object>success(Collections.singletonList(
                new MultipartReplyMessageBuilder()
                        .setVersion(OFConstants.OFP_VERSION_1_3)
                        .setMultipartReplyBody(new MultipartReplyTableCaseBuilder()
                                .setMultipartReplyTable(new MultipartReplyTableBuilder()
                                        .setTableStats(Collections.singletonList(new TableStatsBuilder()
                                                .setActiveCount(Uint32.valueOf(31))
                                                .setLookupCount(Uint64.valueOf(32))
                                                .setMatchedCount(Uint64.valueOf(33))
                                                .setMaxEntries(Uint32.valueOf(34))
                                                .setName("test-table")
                                                .setNwDstMask(Uint8.valueOf(35))
                                                .setNwSrcMask(Uint8.valueOf(36))
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

        Mockito.verify(notificationPublishService).offerNotification(ArgumentMatchers.any());
    }

    @Test
    public void testBuildRequest() {
        Xid xid = new Xid(Uint32.valueOf(42L));
        GetFlowTablesStatisticsInputBuilder input = new GetFlowTablesStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));
        final OfHeader request = flowTableStatisticsService.buildRequest(xid, input.build());
        Assert.assertTrue(request instanceof MultipartRequestInput);
        final MultipartRequestInput mpRequest = (MultipartRequestInput) request;
        Assert.assertEquals(MultipartType.OFPMPTABLE, mpRequest.getType());
        Assert.assertTrue(mpRequest.getMultipartRequestBody() instanceof MultipartRequestTableCase);
        final MultipartRequestTableCase mpRequestBody =
                (MultipartRequestTableCase) mpRequest.getMultipartRequestBody();
        Assert.assertNotNull(mpRequestBody.getMultipartRequestTable().getEmpty());
    }
}
