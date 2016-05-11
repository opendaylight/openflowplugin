package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
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
import org.opendaylight.yangtools.yang.common.RpcResult;
import java.util.concurrent.Future;

/**
 *
 */
public class OpendaylightDirectStatisticsServiceImpl implements OpendaylightDirectStatisticsService {
    private final FlowDirectStatisticsService flowDirectStatisticsService;
    private final GroupDirectStatisticsService groupStatisticsService;
    private final MeterDirectStatisticsService meterDirectStatisticsService;
    private final NodeConnectorDirectStatisticsService nodeConnectorDirectStatisticsService;
    private final QueueDirectStatisticsService queueDirectStatisticsService;

    /**
     *
     * @param requestContextStack
     * @param deviceContext
     */
    public OpendaylightDirectStatisticsServiceImpl(final RequestContextStack requestContextStack,
                                                   @Deprecated final DeviceContext deviceContext) {
        flowDirectStatisticsService = new FlowDirectStatisticsService(requestContextStack, deviceContext);
        groupStatisticsService = new GroupDirectStatisticsService(requestContextStack, deviceContext);
        meterDirectStatisticsService = new MeterDirectStatisticsService(requestContextStack, deviceContext);
        nodeConnectorDirectStatisticsService = new NodeConnectorDirectStatisticsService(requestContextStack, deviceContext);
        queueDirectStatisticsService = new QueueDirectStatisticsService(requestContextStack, deviceContext);
    }

    /**
     *
     * @param input
     * @return
     */
    @Override
    public Future<RpcResult<GetGroupStatisticsOutput>> getGroupStatistics(GetGroupStatisticsInput input) {
        return groupStatisticsService.handleAndReply(input);
    }

    /**
     *
     * @param input
     * @return
     */
    @Override
    public Future<RpcResult<GetQueueStatisticsOutput>> getQueueStatistics(GetQueueStatisticsInput input) {
        return queueDirectStatisticsService.handleAndReply(input);
    }

    /**
     *
     * @param input
     * @return
     */
    @Override
    public Future<RpcResult<GetFlowStatisticsOutput>> getFlowStatistics(GetFlowStatisticsInput input) {
        return flowDirectStatisticsService.handleAndReply(input);
    }

    /**
     *
     * @param input
     * @return
     */
    @Override
    public Future<RpcResult<GetMeterStatisticsOutput>> getMeterStatistics(GetMeterStatisticsInput input) {
        return meterDirectStatisticsService.handleAndReply(input);
    }

    /**
     *
     * @param input
     * @return
     */
    @Override
    public Future<RpcResult<GetNodeConnectorStatisticsOutput>> getNodeConnectorStatistics(GetNodeConnectorStatisticsInput input) {
        return nodeConnectorDirectStatisticsService.handleAndReply(input);
    }
}
