/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.reflect.TypeToken;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.CheckForNull;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.compatibility.Delegator;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.services.FlowCapableTransactionServiceImpl;
import org.opendaylight.openflowplugin.impl.services.NodeConfigServiceImpl;
import org.opendaylight.openflowplugin.impl.services.PacketProcessingServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalEchoServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalExperimenterMessageServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalFlatBatchServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalFlowServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalFlowsBatchServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalGroupServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalGroupsBatchServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalMeterServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalMetersBatchServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalPortServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalTableServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightFlowStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightFlowTableStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightGroupStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightMeterStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightPortStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightQueueStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.OpendaylightFlowStatisticsServiceDelegateImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.FlowDirectStatisticsService;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.GroupDirectStatisticsService;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.MeterDirectStatisticsService;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.NodeConnectorDirectStatisticsService;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.OpendaylightDirectStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.OpendaylightDirectStatisticsServiceProvider;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.QueueDirectStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.OpendaylightDirectStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SalEchoService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SalExperimenterMessageService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.SalFlatBatchService;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yangtools.yang.binding.RpcService;

public class MdSalRegistrationUtils {

    //TODO: Make one register and one unregister method for all services

    private static final TypeToken<Delegator<OpendaylightFlowStatisticsService>> COMPOSITE_SERVICE_TYPE_TOKEN =
            new TypeToken<Delegator<OpendaylightFlowStatisticsService>>() {
                //NOBODY
            };

    private MdSalRegistrationUtils() {
        throw new IllegalStateException();
    }

    /**
     * Method registers all OF services for role {@link OfpRole#BECOMEMASTER}
     *
     * @param rpcContext    - registration processing is implemented in {@link RpcContext}
     * @param deviceContext - every service needs {@link DeviceContext} as input parameter
     */
    public static void registerServices(@CheckForNull final RpcContext rpcContext,
                                        @CheckForNull final DeviceContext deviceContext,
                                        final ExtensionConverterProvider extensionConverterProvider) {
        Preconditions.checkArgument(rpcContext != null);
        Preconditions.checkArgument(deviceContext != null);

        // create service instances
        final SalFlowServiceImpl salFlowService = new SalFlowServiceImpl(rpcContext, deviceContext);
        final FlowCapableTransactionServiceImpl flowCapableTransactionService = new FlowCapableTransactionServiceImpl(rpcContext, deviceContext);
        final SalGroupServiceImpl salGroupService = new SalGroupServiceImpl(rpcContext, deviceContext);
        final SalMeterServiceImpl salMeterService = new SalMeterServiceImpl(rpcContext, deviceContext);

        // register routed service instances
        rpcContext.registerRpcServiceImplementation(SalEchoService.class, new SalEchoServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(SalFlowService.class, salFlowService);
        //TODO: add constructors with rcpContext and deviceContext to meter, group, table constructors
        rpcContext.registerRpcServiceImplementation(FlowCapableTransactionService.class, flowCapableTransactionService);
        rpcContext.registerRpcServiceImplementation(SalMeterService.class, salMeterService);
        rpcContext.registerRpcServiceImplementation(SalGroupService.class, salGroupService);
        rpcContext.registerRpcServiceImplementation(SalTableService.class, new SalTableServiceImpl(rpcContext, deviceContext, deviceContext.getPrimaryConnectionContext().getNodeId()));
        rpcContext.registerRpcServiceImplementation(SalPortService.class, new SalPortServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(PacketProcessingService.class, new PacketProcessingServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(NodeConfigService.class, new NodeConfigServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(OpendaylightFlowStatisticsService.class, OpendaylightFlowStatisticsServiceImpl.createWithOook(rpcContext, deviceContext));

        // Direct statistics gathering
        final OpendaylightDirectStatisticsServiceProvider statisticsProvider = new OpendaylightDirectStatisticsServiceProvider();
        statisticsProvider.register(FlowDirectStatisticsService.class, new FlowDirectStatisticsService(rpcContext, deviceContext));
        statisticsProvider.register(GroupDirectStatisticsService.class, new GroupDirectStatisticsService(rpcContext, deviceContext));
        statisticsProvider.register(MeterDirectStatisticsService.class, new MeterDirectStatisticsService(rpcContext, deviceContext));
        statisticsProvider.register(NodeConnectorDirectStatisticsService.class, new NodeConnectorDirectStatisticsService(rpcContext, deviceContext));
        statisticsProvider.register(QueueDirectStatisticsService.class, new QueueDirectStatisticsService(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(OpendaylightDirectStatisticsService.class, new OpendaylightDirectStatisticsServiceImpl(statisticsProvider));

        final SalFlatBatchServiceImpl salFlatBatchService = new SalFlatBatchServiceImpl(
                new SalFlowsBatchServiceImpl(salFlowService, flowCapableTransactionService),
                new SalGroupsBatchServiceImpl(salGroupService, flowCapableTransactionService),
                new SalMetersBatchServiceImpl(salMeterService, flowCapableTransactionService)
        );
        rpcContext.registerRpcServiceImplementation(SalFlatBatchService.class, salFlatBatchService);

        // TODO: experimenter symmetric and multipart message services
        rpcContext.registerRpcServiceImplementation(SalExperimenterMessageService.class,
                new SalExperimenterMessageServiceImpl(rpcContext, deviceContext, extensionConverterProvider));
    }

    /**
     * Method unregisters all OF services.
     *
     * @param rpcContext - unregistration processing is implemented in {@link RpcContext}
     */
    public static void unregisterServices(@CheckForNull final RpcContext rpcContext) {
        Preconditions.checkArgument(rpcContext != null);

        rpcContext.unregisterRpcServiceImplementation(SalEchoService.class);
        rpcContext.unregisterRpcServiceImplementation(SalFlowService.class);
        //TODO: add constructors with rcpContext and deviceContext to meter, group, table constructors
        rpcContext.unregisterRpcServiceImplementation(FlowCapableTransactionService.class);
        rpcContext.unregisterRpcServiceImplementation(SalMeterService.class);
        rpcContext.unregisterRpcServiceImplementation(SalGroupService.class);
        rpcContext.unregisterRpcServiceImplementation(SalTableService.class);
        rpcContext.unregisterRpcServiceImplementation(SalPortService.class);
        rpcContext.unregisterRpcServiceImplementation(PacketProcessingService.class);
        rpcContext.unregisterRpcServiceImplementation(NodeConfigService.class);
        rpcContext.unregisterRpcServiceImplementation(OpendaylightFlowStatisticsService.class);
        rpcContext.unregisterRpcServiceImplementation(SalFlatBatchService.class);
        // TODO: experimenter symmetric and multipart message services
        rpcContext.unregisterRpcServiceImplementation(SalExperimenterMessageService.class);
        rpcContext.unregisterRpcServiceImplementation(OpendaylightDirectStatisticsService.class);
    }

    /**
     * Support deprecated statistic related services for backward compatibility. The only exception from deprecation is
     * the aggregated flow statistic with match criteria input.
     *
     * @param rpcContext
     * @param deviceContext
     * @param notificationPublishService
     */
    public static void registerStatCompatibilityServices(final RpcContext rpcContext, final DeviceContext deviceContext,
                                                         final NotificationPublishService notificationPublishService) {

        AtomicLong compatibilityXidSeed = new AtomicLong();
        // pickup low statistics service
        final OpendaylightFlowStatisticsService flowStatisticsService = Preconditions.checkNotNull(
                rpcContext.lookupRpcService(OpendaylightFlowStatisticsService.class));
        Preconditions.checkArgument(COMPOSITE_SERVICE_TYPE_TOKEN.isAssignableFrom(flowStatisticsService.getClass()));
        // attach delegate to flow statistics service (to cover all but aggregated stats with match filter input)
        final OpendaylightFlowStatisticsServiceDelegateImpl flowStatisticsDelegate =
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
