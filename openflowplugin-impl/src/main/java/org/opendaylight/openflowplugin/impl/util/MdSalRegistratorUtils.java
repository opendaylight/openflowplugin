/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.compatibility.Delegator;
import org.opendaylight.openflowplugin.impl.services.FlowCapableTransactionServiceImpl;
import org.opendaylight.openflowplugin.impl.services.NodeConfigServiceImpl;
import org.opendaylight.openflowplugin.impl.services.PacketProcessingServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalEchoServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalExperimenterMessageServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalFlowServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalGroupServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalMeterServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalPortServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalTableServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightFlowStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightFlowTableStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightGroupStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightMeterStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightPortStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightQueueStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.OpendaylightFlowStatisticsServiceDelegateImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SalEchoService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SalExperimenterMessageService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.OpendaylightFlowTableStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.OpendaylightGroupStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.OpendaylightMeterStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.NodeConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.SalPortService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.OpendaylightPortStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.OpendaylightQueueStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;

public class MdSalRegistratorUtils {

    private static final TypeToken<Delegator<OpendaylightFlowStatisticsService>> COMPOSITE_SERVICE_TYPE_TOKEN =
            new TypeToken<Delegator<OpendaylightFlowStatisticsService>>() {
                private static final long serialVersionUID = 1L;
                //NOBODY
            };

    private MdSalRegistratorUtils() {
        throw new IllegalStateException();
    }


    public static void registerServices(final RpcContext rpcContext, final DeviceContext deviceContext) {
        rpcContext.registerRpcServiceImplementation(SalFlowService.class, new SalFlowServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(SalEchoService.class, new SalEchoServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(FlowCapableTransactionService.class, new FlowCapableTransactionServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(SalMeterService.class, new SalMeterServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(SalGroupService.class, new SalGroupServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(SalTableService.class, new SalTableServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(SalPortService.class, new SalPortServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(PacketProcessingService.class, new PacketProcessingServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(NodeConfigService.class, new NodeConfigServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(OpendaylightFlowStatisticsService.class, new OpendaylightFlowStatisticsServiceImpl(rpcContext, deviceContext));
        // TODO: experimenter symmetric and multipart message services
        rpcContext.registerRpcServiceImplementation(SalExperimenterMessageService.class,
                new SalExperimenterMessageServiceImpl(rpcContext, deviceContext));
    }

    public static void unregisterServices(final RpcContext rpcContext) throws Exception {
        rpcContext.close();
    }

    /**
     * Support deprecated statistic related services for backward compatibility. The only exception from deprecation is
     * the aggregated flow statistic with match criteria input.
     *
     * @param rpcContext
     * @param deviceContext
     * @param notificationPublishService
     * @param compatibilityXidSeed
     */
    public static void registerStatCompatibilityServices(final RpcContext rpcContext, final DeviceContext deviceContext,
                                                         final NotificationPublishService notificationPublishService,
                                                         final AtomicLong compatibilityXidSeed) {
        // pickup low statistics service
        final OpendaylightFlowStatisticsService flowStatisticsService = Preconditions.checkNotNull(
                rpcContext.lookupRpcService(OpendaylightFlowStatisticsService.class));
        Preconditions.checkArgument(COMPOSITE_SERVICE_TYPE_TOKEN.isAssignableFrom(flowStatisticsService.getClass()));
        // attach delegate to flow statistics service (to cover all but aggregated stats with match filter input)
        OpendaylightFlowStatisticsServiceDelegateImpl flowStatisticsDelegate =
                new OpendaylightFlowStatisticsServiceDelegateImpl(rpcContext, deviceContext, notificationPublishService, new AtomicLong());
        ((Delegator<OpendaylightFlowStatisticsService>) flowStatisticsService).setDelegate(flowStatisticsDelegate);

        // register all statistics (deprecated) services
        rpcContext.registerRpcServiceImplementation(OpendaylightFlowTableStatisticsService.class,
                new OpendaylightFlowTableStatisticsServiceImpl(rpcContext, deviceContext, compatibilityXidSeed, notificationPublishService));
        rpcContext.registerRpcServiceImplementation(OpendaylightGroupStatisticsService.class,
                new OpendaylightGroupStatisticsServiceImpl(rpcContext, deviceContext, compatibilityXidSeed, notificationPublishService));
        rpcContext.registerRpcServiceImplementation(OpendaylightMeterStatisticsService.class,
                new OpendaylightMeterStatisticsServiceImpl(rpcContext, deviceContext, compatibilityXidSeed, notificationPublishService));
        rpcContext.registerRpcServiceImplementation(OpendaylightQueueStatisticsService.class,
                new OpendaylightQueueStatisticsServiceImpl(rpcContext, deviceContext, compatibilityXidSeed, notificationPublishService));
        rpcContext.registerRpcServiceImplementation(OpendaylightPortStatisticsService.class,
                new OpendaylightPortStatisticsServiceImpl(rpcContext, deviceContext, compatibilityXidSeed, notificationPublishService));
    }
}
