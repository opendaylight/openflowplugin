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
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.OpendaylightDirectStatisticsService;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;

@RunWith(MockitoJUnitRunner.class)
public class OpendaylightDirectStatisticsServiceImplTest {
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

    private OpendaylightDirectStatisticsService service;
    private OpendaylightDirectStatisticsService emptyService;

    @Before
    public void setUp() throws Exception {
        final OpendaylightDirectStatisticsServiceProvider provider = new OpendaylightDirectStatisticsServiceProvider();
        provider.register(AbstractFlowDirectStatisticsService.class, flowDirectStatisticsService);
        provider.register(AbstractGroupDirectStatisticsService.class, groupDirectStatisticsService);
        provider.register(AbstractMeterDirectStatisticsService.class, meterDirectStatisticsService);
        provider.register(AbstractPortDirectStatisticsService.class, nodeConnectorDirectStatisticsService);
        provider.register(AbstractQueueDirectStatisticsService.class, queueDirectStatisticsService);

        service = new OpendaylightDirectStatisticsServiceImpl(provider);
        emptyService = new OpendaylightDirectStatisticsServiceImpl(new OpendaylightDirectStatisticsServiceProvider());
    }

    @Test
    public void testGetGroupStatistics() throws Exception {
        service.getGroupStatistics(getGroupStatisticsInput);
        verify(groupDirectStatisticsService).handleAndReply(getGroupStatisticsInput);
    }

    @Test
    public void testGetGroupStatisticsFail() throws Exception {
        RpcResult<GetGroupStatisticsOutput> result = emptyService
                .getGroupStatistics(getGroupStatisticsInput)
                .get();

        assertFalse(result.isSuccessful());

        for (RpcError error : result.getErrors()) {
            assertTrue(error.getMessage().contains(AbstractGroupDirectStatisticsService.class.getSimpleName()));
        }

        verify(groupDirectStatisticsService, times(0)).handleAndReply(getGroupStatisticsInput);
    }

    @Test
    public void testGetQueueStatistics() throws Exception {
        service.getQueueStatistics(getQueueStatisticsInput);
        verify(queueDirectStatisticsService).handleAndReply(getQueueStatisticsInput);
    }

    @Test
    public void testGetQueueStatisticsFail() throws Exception {
        RpcResult<GetQueueStatisticsOutput> result = emptyService
                .getQueueStatistics(getQueueStatisticsInput)
                .get();

        assertFalse(result.isSuccessful());

        for (RpcError error : result.getErrors()) {
            assertTrue(error.getMessage().contains(AbstractQueueDirectStatisticsService.class.getSimpleName()));
        }

        verify(queueDirectStatisticsService, times(0)).handleAndReply(getQueueStatisticsInput);
    }

    @Test
    public void testGetFlowStatistics() throws Exception {
        service.getFlowStatistics(getFlowStatisticsInput);
        verify(flowDirectStatisticsService).handleAndReply(getFlowStatisticsInput);
    }

    @Test
    public void testGetFlowStatisticsFail() throws Exception {
        RpcResult<GetFlowStatisticsOutput> result = emptyService
                .getFlowStatistics(getFlowStatisticsInput)
                .get();

        assertFalse(result.isSuccessful());

        for (RpcError error : result.getErrors()) {
            assertTrue(error.getMessage().contains(AbstractFlowDirectStatisticsService.class.getSimpleName()));
        }

        verify(flowDirectStatisticsService, times(0)).handleAndReply(getFlowStatisticsInput);
    }

    @Test
    public void testGetMeterStatistics() throws Exception {
        service.getMeterStatistics(getMeterStatisticsInput);
        verify(meterDirectStatisticsService).handleAndReply(getMeterStatisticsInput);
    }

    @Test
    public void testGetMeterStatisticsFail() throws Exception {
        RpcResult<GetMeterStatisticsOutput> result = emptyService
                .getMeterStatistics(getMeterStatisticsInput)
                .get();

        assertFalse(result.isSuccessful());

        for (RpcError error : result.getErrors()) {
            assertTrue(error.getMessage().contains(AbstractMeterDirectStatisticsService.class.getSimpleName()));
        }

        verify(meterDirectStatisticsService, times(0)).handleAndReply(getMeterStatisticsInput);
    }

    @Test
    public void testGetNodeConnectorStatistics() throws Exception {
        service.getNodeConnectorStatistics(getNodeConnectorStatisticsInput);
        verify(nodeConnectorDirectStatisticsService).handleAndReply(getNodeConnectorStatisticsInput);
    }

    @Test
    public void testGetNodeConnectorStatisticsFail() throws Exception {
        RpcResult<GetNodeConnectorStatisticsOutput> result = emptyService
                .getNodeConnectorStatistics(getNodeConnectorStatisticsInput)
                .get();

        assertFalse(result.isSuccessful());

        for (RpcError error : result.getErrors()) {
            assertTrue(error.getMessage().contains(AbstractPortDirectStatisticsService.class.getSimpleName()));
        }

        verify(nodeConnectorDirectStatisticsService, times(0)).handleAndReply(getNodeConnectorStatisticsInput);
    }
}
