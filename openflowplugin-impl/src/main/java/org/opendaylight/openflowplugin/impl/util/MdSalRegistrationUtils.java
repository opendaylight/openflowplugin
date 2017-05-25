/*
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
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.compatibility.Delegator;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.services.sal.FlowCapableTransactionServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.NodeConfigServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.PacketProcessingServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalEchoServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalExperimenterMessageServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalExperimenterMpMessageServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalFlatBatchServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalFlowServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalFlowsBatchServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalGroupServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalGroupsBatchServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalMeterServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalMetersBatchServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalPortServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalTableServiceImpl;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightFlowStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightFlowTableStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightGroupStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightMeterStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightPortStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightQueueStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.OpendaylightFlowStatisticsServiceDelegateImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.OpendaylightDirectStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.multilayer.MultiLayerDirectStatisticsProviderInitializer;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.singlelayer.SingleLayerDirectStatisticsProviderInitializer;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.OpendaylightDirectStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SalEchoService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SalExperimenterMessageService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SalExperimenterMpMessageService;
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
     *  @param rpcContext    - registration processing is implemented in {@link org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext}
     * @param deviceContext - every service needs {@link org.opendaylight.openflowplugin.api.openflow.device.DeviceContext} as input parameter
     * @param convertorExecutor convertor executor
     */
    public static void registerServices(@Nonnull final RpcContext rpcContext,
                                        @Nonnull final DeviceContext deviceContext,
                                        final ExtensionConverterProvider extensionConverterProvider,
                                        final ConvertorExecutor convertorExecutor) {
        // TODO: Use multipart writer provider from device context
        final MultipartWriterProvider multipartWriterProvider = MultipartWriterProviderFactory
            .createDefaultProvider(deviceContext);

        // create service instances
        final SalFlowServiceImpl salFlowService = new SalFlowServiceImpl(rpcContext, deviceContext, convertorExecutor);
        final FlowCapableTransactionServiceImpl flowCapableTransactionService = new FlowCapableTransactionServiceImpl(rpcContext, deviceContext);
        final SalGroupServiceImpl salGroupService = new SalGroupServiceImpl(rpcContext, deviceContext, convertorExecutor);
        final SalMeterServiceImpl salMeterService = new SalMeterServiceImpl(rpcContext, deviceContext, convertorExecutor);

        // register routed service instances
        rpcContext.registerRpcServiceImplementation(SalEchoService.class, new SalEchoServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(SalFlowService.class, salFlowService);
        rpcContext.registerRpcServiceImplementation(FlowCapableTransactionService.class, flowCapableTransactionService);
        rpcContext.registerRpcServiceImplementation(SalMeterService.class, salMeterService);
        rpcContext.registerRpcServiceImplementation(SalGroupService.class, salGroupService);
        rpcContext.registerRpcServiceImplementation(SalTableService.class, new SalTableServiceImpl(rpcContext, deviceContext, convertorExecutor, multipartWriterProvider));
        rpcContext.registerRpcServiceImplementation(SalPortService.class, new SalPortServiceImpl(rpcContext, deviceContext, convertorExecutor));
        rpcContext.registerRpcServiceImplementation(PacketProcessingService.class, new PacketProcessingServiceImpl(rpcContext, deviceContext, convertorExecutor));
        rpcContext.registerRpcServiceImplementation(NodeConfigService.class, new NodeConfigServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(OpendaylightFlowStatisticsService.class, OpendaylightFlowStatisticsServiceImpl.createWithOook(rpcContext, deviceContext, convertorExecutor));

        // register direct statistics gathering services
        rpcContext.registerRpcServiceImplementation(OpendaylightDirectStatisticsService.class,
            new OpendaylightDirectStatisticsServiceImpl(deviceContext.canUseSingleLayerSerialization()
                ? SingleLayerDirectStatisticsProviderInitializer
                    .createProvider(rpcContext, deviceContext, convertorExecutor, multipartWriterProvider)
                : MultiLayerDirectStatisticsProviderInitializer
                    .createProvider(rpcContext, deviceContext, convertorExecutor, multipartWriterProvider)));

        // register flat batch services
        rpcContext.registerRpcServiceImplementation(SalFlatBatchService.class, new SalFlatBatchServiceImpl(
                new SalFlowsBatchServiceImpl(salFlowService, flowCapableTransactionService),
                new SalGroupsBatchServiceImpl(salGroupService, flowCapableTransactionService),
                new SalMetersBatchServiceImpl(salMeterService, flowCapableTransactionService)
        ));

        // register experimenter services
        rpcContext.registerRpcServiceImplementation(SalExperimenterMessageService.class,
                new SalExperimenterMessageServiceImpl(rpcContext, deviceContext, extensionConverterProvider));
        rpcContext.registerRpcServiceImplementation(SalExperimenterMpMessageService.class,
                new SalExperimenterMpMessageServiceImpl(rpcContext, deviceContext, extensionConverterProvider));
    }

    /**
     * Support deprecated statistic related services for backward compatibility. The only exception from deprecation is
     * the aggregated flow statistic with match criteria input.
     * @param rpcContext
     * @param deviceContext
     * @param notificationPublishService
     * @param convertorExecutor
     */
    public static void registerStatCompatibilityServices(final RpcContext rpcContext, final DeviceContext deviceContext,
                                                         final NotificationPublishService notificationPublishService,
                                                         final ConvertorExecutor convertorExecutor) {

        AtomicLong compatibilityXidSeed = new AtomicLong();
        // pickup low statistics service
        final OpendaylightFlowStatisticsService flowStatisticsService = Preconditions.checkNotNull(
                rpcContext.lookupRpcService(OpendaylightFlowStatisticsService.class));
        Preconditions.checkArgument(COMPOSITE_SERVICE_TYPE_TOKEN.isAssignableFrom(flowStatisticsService.getClass()));
        // attach delegate to flow statistics service (to cover all but aggregated stats with match filter input)
        final OpendaylightFlowStatisticsServiceDelegateImpl flowStatisticsDelegate =
                new OpendaylightFlowStatisticsServiceDelegateImpl(rpcContext, deviceContext, notificationPublishService, new AtomicLong(), convertorExecutor);
        ((Delegator<OpendaylightFlowStatisticsService>) flowStatisticsService).setDelegate(flowStatisticsDelegate);

        // register all statistics (deprecated) services
        rpcContext.registerRpcServiceImplementation(OpendaylightFlowTableStatisticsService.class,
                new OpendaylightFlowTableStatisticsServiceImpl(rpcContext, deviceContext, compatibilityXidSeed, notificationPublishService));
        rpcContext.registerRpcServiceImplementation(OpendaylightGroupStatisticsService.class,
                new OpendaylightGroupStatisticsServiceImpl(rpcContext, deviceContext, compatibilityXidSeed, notificationPublishService, convertorExecutor));
        rpcContext.registerRpcServiceImplementation(OpendaylightMeterStatisticsService.class,
                new OpendaylightMeterStatisticsServiceImpl(rpcContext, deviceContext, compatibilityXidSeed, notificationPublishService, convertorExecutor));
        rpcContext.registerRpcServiceImplementation(OpendaylightQueueStatisticsService.class,
                new OpendaylightQueueStatisticsServiceImpl(rpcContext, deviceContext, compatibilityXidSeed, notificationPublishService));
        rpcContext.registerRpcServiceImplementation(OpendaylightPortStatisticsService.class,
                new OpendaylightPortStatisticsServiceImpl(rpcContext, deviceContext, compatibilityXidSeed, notificationPublishService));
    }
}
