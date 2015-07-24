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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Test for {@link OpendaylightGroupStatisticsServiceImpl}
 */
public class OpendaylightGroupStatisticsServiceImplTest extends AbstractStatsServiceTest {

    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;

    private RequestContext<Object> rqContext;

    private OpendaylightGroupStatisticsServiceImpl groupStatisticsService;

    public void setUp() {
        groupStatisticsService = new OpendaylightGroupStatisticsServiceImpl(rqContextStack, deviceContext);

        rqContext = new AbstractRequestContext<Object>(42L) {
            @Override
            public void close() {
                //NOOP
            }
        };
        Mockito.when(rqContextStack.<Object>createRequestContext()).thenReturn(rqContext);
    }

    @Test
    public void testGetAllGroupStatistics() throws Exception {
        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(Matchers.eq(42L), requestInput.capture(), Matchers.any(FutureCallback.class));

        GetAllGroupStatisticsInputBuilder input = new GetAllGroupStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        final Future<RpcResult<GetAllGroupStatisticsOutput>> resultFuture
                = groupStatisticsService.getAllGroupStatistics(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetAllGroupStatisticsOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPGROUP, requestInput.getValue().getType());
    }

    @Test
    public void testGetGroupDescription() throws Exception {
        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(Matchers.eq(42L), requestInput.capture(), Matchers.any(FutureCallback.class));

        GetGroupDescriptionInputBuilder input = new GetGroupDescriptionInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        final Future<RpcResult<GetGroupDescriptionOutput>> resultFuture
                = groupStatisticsService.getGroupDescription(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetGroupDescriptionOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPGROUPDESC, requestInput.getValue().getType());
    }

    @Test
    public void testGetGroupFeatures() throws Exception {
        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(Matchers.eq(42L), requestInput.capture(), Matchers.any(FutureCallback.class));

        GetGroupFeaturesInputBuilder input = new GetGroupFeaturesInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        final Future<RpcResult<GetGroupFeaturesOutput>> resultFuture
                = groupStatisticsService.getGroupFeatures(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetGroupFeaturesOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPGROUPFEATURES, requestInput.getValue().getType());
    }

    @Test
    public void testGetGroupStatistics() throws Exception {
        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(Matchers.eq(42L), requestInput.capture(), Matchers.any(FutureCallback.class));

        GetGroupStatisticsInputBuilder input = new GetGroupStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setGroupId(new GroupId(21L));

        final Future<RpcResult<GetGroupStatisticsOutput>> resultFuture
                = groupStatisticsService.getGroupStatistics(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetGroupStatisticsOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPGROUP, requestInput.getValue().getType());
    }
}