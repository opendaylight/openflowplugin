/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsOutput;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;

@RunWith(MockitoJUnitRunner.class)
public class OpendaylightDirectStatisticsRpcsTest {
    @Mock
    AbstractFlowDirectStatisticsService flowDirectStatisticsService;
    @Mock
    AbstractGroupDirectStatisticsService groupDirectStatisticsService;
    @Mock
    AbstractMeterDirectStatisticsService meterDirectStatisticsService;
    @Mock
    AbstractPortDirectStatisticsService nodeConnectorDirectStatisticsService;
    @Mock
    AbstractQueueDirectStatisticsService queueDirectStatisticsService;

    @Mock
    GetGroupStatisticsInput getGroupStatisticsInput;
    @Mock
    GetQueueStatisticsInput getQueueStatisticsInput;
    @Mock
    GetFlowStatisticsInput getFlowStatisticsInput;
    @Mock
    GetMeterStatisticsInput getMeterStatisticsInput;
    @Mock
    GetNodeConnectorStatisticsInput getNodeConnectorStatisticsInput;

    private OpendaylightDirectStatisticsRpcs rpc;
    private OpendaylightDirectStatisticsRpcs emptyRpc;

    @Before
    public void setUp() {
        final OpendaylightDirectStatisticsServiceProvider provider = new OpendaylightDirectStatisticsServiceProvider();
        provider.register(AbstractFlowDirectStatisticsService.class, flowDirectStatisticsService);
        provider.register(AbstractGroupDirectStatisticsService.class, groupDirectStatisticsService);
        provider.register(AbstractMeterDirectStatisticsService.class, meterDirectStatisticsService);
        provider.register(AbstractPortDirectStatisticsService.class, nodeConnectorDirectStatisticsService);
        provider.register(AbstractQueueDirectStatisticsService.class, queueDirectStatisticsService);

        rpc = new OpendaylightDirectStatisticsRpcs(provider);
        emptyRpc = new OpendaylightDirectStatisticsRpcs(new OpendaylightDirectStatisticsServiceProvider());
    }

    @Test
    public void testGetGroupStatistics() {
        rpc.getRpcClassToInstanceMap().getInstance(GetGroupStatistics.class).invoke(getGroupStatisticsInput);
        verify(groupDirectStatisticsService).handleAndReply(getGroupStatisticsInput);
    }

    @Test
    public void testGetGroupStatisticsFail() throws Exception {
        RpcResult<GetGroupStatisticsOutput> result = emptyRpc.getRpcClassToInstanceMap()
            .getInstance(GetGroupStatistics.class).invoke(getGroupStatisticsInput).get();

        assertFalse(result.isSuccessful());

        for (RpcError error : result.getErrors()) {
            assertTrue(error.getMessage().contains(AbstractGroupDirectStatisticsService.class.getSimpleName()));
        }

        verify(groupDirectStatisticsService, times(0)).handleAndReply(getGroupStatisticsInput);
    }

    @Test
    public void testGetQueueStatistics() {
        rpc.getRpcClassToInstanceMap().getInstance(GetQueueStatistics.class).invoke(getQueueStatisticsInput);
        verify(queueDirectStatisticsService).handleAndReply(getQueueStatisticsInput);
    }

    @Test
    public void testGetQueueStatisticsFail() throws Exception {
        RpcResult<GetQueueStatisticsOutput> result = emptyRpc.getRpcClassToInstanceMap()
            .getInstance(GetQueueStatistics.class).invoke(getQueueStatisticsInput).get();

        assertFalse(result.isSuccessful());

        for (RpcError error : result.getErrors()) {
            assertTrue(error.getMessage().contains(AbstractQueueDirectStatisticsService.class.getSimpleName()));
        }

        verify(queueDirectStatisticsService, times(0)).handleAndReply(getQueueStatisticsInput);
    }

    @Test
    public void testGetFlowStatistics() {
        rpc.getRpcClassToInstanceMap().getInstance(GetFlowStatistics.class).invoke(getFlowStatisticsInput);
        verify(flowDirectStatisticsService).handleAndReply(getFlowStatisticsInput);
    }

    @Test
    public void testGetFlowStatisticsFail() throws Exception {
        RpcResult<GetFlowStatisticsOutput> result = emptyRpc.getRpcClassToInstanceMap()
            .getInstance(GetFlowStatistics.class).invoke(getFlowStatisticsInput).get();

        assertFalse(result.isSuccessful());

        for (RpcError error : result.getErrors()) {
            assertTrue(error.getMessage().contains(AbstractFlowDirectStatisticsService.class.getSimpleName()));
        }

        verify(flowDirectStatisticsService, times(0)).handleAndReply(getFlowStatisticsInput);
    }

    @Test
    public void testGetMeterStatistics() {
        rpc.getRpcClassToInstanceMap().getInstance(GetMeterStatistics.class).invoke(getMeterStatisticsInput);
        verify(meterDirectStatisticsService).handleAndReply(getMeterStatisticsInput);
    }

    @Test
    public void testGetMeterStatisticsFail() throws Exception {
        RpcResult<GetMeterStatisticsOutput> result = emptyRpc.getRpcClassToInstanceMap()
            .getInstance(GetMeterStatistics.class).invoke(getMeterStatisticsInput).get();

        assertFalse(result.isSuccessful());

        for (RpcError error : result.getErrors()) {
            assertTrue(error.getMessage().contains(AbstractMeterDirectStatisticsService.class.getSimpleName()));
        }

        verify(meterDirectStatisticsService, times(0)).handleAndReply(getMeterStatisticsInput);
    }

    @Test
    public void testGetNodeConnectorStatistics() {
        rpc.getRpcClassToInstanceMap().getInstance(GetNodeConnectorStatistics.class)
            .invoke(getNodeConnectorStatisticsInput);
        verify(nodeConnectorDirectStatisticsService).handleAndReply(getNodeConnectorStatisticsInput);
    }

    @Test
    public void testGetNodeConnectorStatisticsFail() throws Exception {
        RpcResult<GetNodeConnectorStatisticsOutput> result = emptyRpc.getRpcClassToInstanceMap()
            .getInstance(GetNodeConnectorStatistics.class).invoke(getNodeConnectorStatisticsInput).get();

        assertFalse(result.isSuccessful());

        for (RpcError error : result.getErrors()) {
            assertTrue(error.getMessage().contains(AbstractPortDirectStatisticsService.class.getSimpleName()));
        }

        verify(nodeConnectorDirectStatisticsService, times(0)).handleAndReply(getNodeConnectorStatisticsInput);
    }
}
