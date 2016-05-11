package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.OpendaylightDirectStatisticsService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class OpendaylightDirectStatisticsServiceImplTest {
    @Mock
    FlowDirectStatisticsService flowDirectStatisticsService;

    @Mock
    GroupDirectStatisticsService groupDirectStatisticsService;

    @Mock
    MeterDirectStatisticsService meterDirectStatisticsService;

    @Mock
    NodeConnectorDirectStatisticsService nodeConnectorDirectStatisticsService;

    @Mock
    QueueDirectStatisticsService queueDirectStatisticsService;

    private OpendaylightDirectStatisticsService service;

    @Before
    public void setUp() throws Exception {
        final OpendaylightDirectStatisticsServiceProvider provider = new OpendaylightDirectStatisticsServiceProvider();
        provider.register(FlowDirectStatisticsService.class, flowDirectStatisticsService);
        provider.register(GroupDirectStatisticsService.class, groupDirectStatisticsService);
        provider.register(MeterDirectStatisticsService.class, meterDirectStatisticsService);
        provider.register(NodeConnectorDirectStatisticsService.class, nodeConnectorDirectStatisticsService);
        provider.register(QueueDirectStatisticsService.class, queueDirectStatisticsService);
        service = new OpendaylightDirectStatisticsServiceImpl(provider);
    }

    @Test
    public void testGetGroupStatistics() throws Exception {
        GetGroupStatisticsInput input = mock(GetGroupStatisticsInput.class);
        service.getGroupStatistics(input);
        verify(groupDirectStatisticsService).handleAndReply(input);
    }

    @Test
    public void testGetQueueStatistics() throws Exception {
        GetQueueStatisticsInput input = mock(GetQueueStatisticsInput.class);
        service.getQueueStatistics(input);
        verify(queueDirectStatisticsService).handleAndReply(input);
    }

    @Test
    public void testGetFlowStatistics() throws Exception {
        GetFlowStatisticsInput input = mock(GetFlowStatisticsInput.class);
        service.getFlowStatistics(input);
        verify(flowDirectStatisticsService).handleAndReply(input);
    }

    @Test
    public void testGetMeterStatistics() throws Exception {
        GetMeterStatisticsInput input = mock(GetMeterStatisticsInput.class);
        service.getMeterStatistics(input);
        verify(meterDirectStatisticsService).handleAndReply(input);
    }

    @Test
    public void testGetNodeConnectorStatistics() throws Exception {
        GetNodeConnectorStatisticsInput input = mock(GetNodeConnectorStatisticsInput.class);
        service.getNodeConnectorStatistics(input);
        verify(nodeConnectorDirectStatisticsService).handleAndReply(input);
    }
}