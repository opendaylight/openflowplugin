/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.FutureCallback;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCase;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test for {@link GetFlowTablesStatisticsImpl}.
 */
@Deprecated
public class GetFlowTablesStatisticsImplTest extends AbstractSingleStatsServiceTest {
    private static final Uint8 TABLE_ID = Uint8.valueOf(123);

    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;

    private GetFlowTablesStatisticsImpl getFlowTablesStatistics;

    @Override
    public void setUp() {
        getFlowTablesStatistics = new GetFlowTablesStatisticsImpl(rqContextStack, deviceContext, new AtomicLong(),
            notificationPublishService);
    }

    @Test
    public void testGetFlowTablesStatistics() throws Exception {
        doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(eq(Uint32.valueOf(42)), requestInput.capture(), any(FutureCallback.class));

        var  input = new GetFlowTablesStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = RpcResultBuilder.<Object>success(List.of(
                new MultipartReplyMessageBuilder()
                        .setVersion(EncodeConstants.OF_VERSION_1_3)
                        .setMultipartReplyBody(new MultipartReplyTableCaseBuilder()
                                .setMultipartReplyTable(new MultipartReplyTableBuilder()
                                        .setTableStats(List.of(new TableStatsBuilder()
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

        final var resultFuture = getFlowTablesStatistics.invoke(input.build());

        assertTrue(resultFuture.isDone());
        final var rpcResult = resultFuture.get();
        assertTrue(rpcResult.isSuccessful());
        assertEquals(MultipartType.OFPMPTABLE, requestInput.getValue().getType());

        verify(notificationPublishService).offerNotification(any());
    }

    @Test
    public void testBuildRequest() {
        final var xid = new Xid(Uint32.valueOf(42L));
        final var input = new GetFlowTablesStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));
        final var request = getFlowTablesStatistics.buildRequest(xid, input.build());
        assertTrue(request instanceof MultipartRequestInput);
        final var mpRequest = (MultipartRequestInput) request;
        assertEquals(MultipartType.OFPMPTABLE, mpRequest.getType());
        assertTrue(mpRequest.getMultipartRequestBody() instanceof MultipartRequestTableCase);
        final var mpRequestBody = (MultipartRequestTableCase) mpRequest.getMultipartRequestBody();
        assertNotNull(mpRequestBody.getMultipartRequestTable().getEmpty());
    }
}
