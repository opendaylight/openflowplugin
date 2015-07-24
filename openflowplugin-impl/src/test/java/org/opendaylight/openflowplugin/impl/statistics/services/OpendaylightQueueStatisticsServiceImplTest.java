/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.util.concurrent.FutureCallback;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Test for {@link OpendaylightQueueStatisticsServiceImpl}
 */
public class OpendaylightQueueStatisticsServiceImplTest extends AbstractStatsServiceTest {

    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;

    private RequestContext<Object> rqContext;

    private OpendaylightQueueStatisticsServiceImpl queueStatisticsService;

    public void setUp() {
        queueStatisticsService = new OpendaylightQueueStatisticsServiceImpl(rqContextStack, deviceContext);

        rqContext = new AbstractRequestContext<Object>(42L) {
            @Override
            public void close() {
                //NOOP
            }
        };
        Mockito.when(rqContextStack.<Object>createRequestContext()).thenReturn(rqContext);
    }

    @Test
    public void testGetAllQueuesStatisticsFromAllPorts() throws Exception {
        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(Matchers.eq(42L), requestInput.capture(), Matchers.any(FutureCallback.class));

        GetAllQueuesStatisticsFromAllPortsInputBuilder input = new GetAllQueuesStatisticsFromAllPortsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        final Future<RpcResult<GetAllQueuesStatisticsFromAllPortsOutput>> resultFuture
                = queueStatisticsService.getAllQueuesStatisticsFromAllPorts(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetAllQueuesStatisticsFromAllPortsOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPQUEUE, requestInput.getValue().getType());
    }

    @Test
    public void testGetAllQueuesStatisticsFromGivenPort() throws Exception {
        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(Matchers.eq(42L), requestInput.capture(), Matchers.any(FutureCallback.class));

        GetAllQueuesStatisticsFromGivenPortInputBuilder input = new GetAllQueuesStatisticsFromGivenPortInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setNodeConnectorId(new NodeConnectorId("unitProt:123:321"));

        final Future<RpcResult<GetAllQueuesStatisticsFromGivenPortOutput>> resultFuture
                = queueStatisticsService.getAllQueuesStatisticsFromGivenPort(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetAllQueuesStatisticsFromGivenPortOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPQUEUE, requestInput.getValue().getType());
    }

    @Test
    public void testGetQueueStatisticsFromGivenPort() throws Exception {
        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(Matchers.eq(42L), requestInput.capture(), Matchers.any(FutureCallback.class));

        GetQueueStatisticsFromGivenPortInputBuilder input = new GetQueueStatisticsFromGivenPortInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setNodeConnectorId(new NodeConnectorId("unitProt:123:321"))
                .setQueueId(new QueueId(21L));

        final Future<RpcResult<GetQueueStatisticsFromGivenPortOutput>> resultFuture
                = queueStatisticsService.getQueueStatisticsFromGivenPort(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetQueueStatisticsFromGivenPortOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPQUEUE, requestInput.getValue().getType());
    }
}