/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.compatibility.Delegator;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.impl.services.sal.FlowCapableTransactionRpc;
import org.opendaylight.openflowplugin.impl.services.sal.NodeConfigRpc;
import org.opendaylight.openflowplugin.impl.services.sal.PacketProcessingRpc;
import org.opendaylight.openflowplugin.impl.services.sal.SalAsyncConfigRpcs;
import org.opendaylight.openflowplugin.impl.services.sal.SalBundleServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalEchoRpc;
import org.opendaylight.openflowplugin.impl.services.sal.SalExperimenterMessageServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalExperimenterMpMessageServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalFlatBatchRpc;
import org.opendaylight.openflowplugin.impl.services.sal.SalFlowRpcs;
import org.opendaylight.openflowplugin.impl.services.sal.SalFlowsBatchRpcs;
import org.opendaylight.openflowplugin.impl.services.sal.SalGroupRpcs;
import org.opendaylight.openflowplugin.impl.services.sal.SalGroupsBatchRpcs;
import org.opendaylight.openflowplugin.impl.services.sal.SalMeterRpcs;
import org.opendaylight.openflowplugin.impl.services.sal.SalMetersBatchRpcs;
import org.opendaylight.openflowplugin.impl.services.sal.SalPortRpc;
import org.opendaylight.openflowplugin.impl.services.sal.SalTableServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightFlowStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightFlowTableStatisticsRpc;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightGroupStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightMeterStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightPortStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightQueueStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.OpendaylightFlowStatisticsServiceDelegateImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.OpendaylightDirectStatisticsRpcs;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.multilayer.MultiLayerDirectStatisticsProviderInitializer;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.singlelayer.SingleLayerDirectStatisticsProviderInitializer;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SalExperimenterMessageService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SalExperimenterMpMessageService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.OpendaylightGroupStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.OpendaylightMeterStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.SalBundleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.OpendaylightPortStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.OpendaylightQueueStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;

public final class MdSalRegistrationUtils {

    //TODO: Make one register and one unregister method for all services
    private MdSalRegistrationUtils() {
        // Hidden on purpose
    }

    /**
     * Method registers all OF services for role {@link OfpRole#BECOMEMASTER}.
     *
     * @param rpcContext    - registration processing is implemented in
     *        {@link org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext}
     *
     * @param deviceContext - every service needs
     *        {@link org.opendaylight.openflowplugin.api.openflow.device.DeviceContext} as input parameter
     *
     * @param convertorExecutor convertor executor
     */
    public static void registerServices(@NonNull final RpcContext rpcContext,
                                        @NonNull final DeviceContext deviceContext,
                                        final ExtensionConverterProvider extensionConverterProvider,
                                        final ConvertorExecutor convertorExecutor) {
        // TODO: Use multipart writer provider from device context
        final MultipartWriterProvider multipartWriterProvider = MultipartWriterProviderFactory
            .createDefaultProvider(deviceContext);

        // create service instances
        final SalFlowRpcs salFlowService = new SalFlowRpcs(rpcContext, deviceContext, convertorExecutor);
        final FlowCapableTransactionRpc flowCapableTransactionService =
                new FlowCapableTransactionRpc(rpcContext, deviceContext);
        final SalAsyncConfigRpcs salAsyncConfigService = new SalAsyncConfigRpcs(rpcContext, deviceContext);
        final SalGroupRpcs salGroupService = new SalGroupRpcs(rpcContext, deviceContext, convertorExecutor);
        final SalMeterRpcs salMeterService = new SalMeterRpcs(rpcContext, deviceContext, convertorExecutor);
        final SalEchoRpc salEchoService = new SalEchoRpc(rpcContext, deviceContext);
        final NodeConfigRpc nodeConfigService = new NodeConfigRpc(rpcContext, deviceContext);
        final PacketProcessingRpc packetProcessingRpc =
                new PacketProcessingRpc(rpcContext, deviceContext, convertorExecutor);
        final SalPortRpc salPortService = new SalPortRpc(rpcContext, deviceContext, convertorExecutor);


        // register routed service instances
        rpcContext.registerRpcServiceImplementations(salEchoService, salEchoService.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(salFlowService, salFlowService.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(flowCapableTransactionService,
            flowCapableTransactionService.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(salAsyncConfigService,
            salAsyncConfigService.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(salMeterService, salMeterService.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(salGroupService, salGroupService.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementation(SalTableService.class,
                new SalTableServiceImpl(rpcContext, deviceContext, convertorExecutor, multipartWriterProvider));
        rpcContext.registerRpcServiceImplementations(salPortService, salPortService.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(packetProcessingRpc,
            packetProcessingRpc.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(nodeConfigService, nodeConfigService.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementation(OpendaylightFlowStatisticsService.class,
                OpendaylightFlowStatisticsServiceImpl.createWithOook(rpcContext, deviceContext, convertorExecutor));

        // register direct statistics gathering services
        final OpendaylightDirectStatisticsRpcs opendaylightDirectStatisticsRpcs =
            new OpendaylightDirectStatisticsRpcs(deviceContext.canUseSingleLayerSerialization()
                ? SingleLayerDirectStatisticsProviderInitializer
                .createProvider(rpcContext, deviceContext, convertorExecutor, multipartWriterProvider)
                : MultiLayerDirectStatisticsProviderInitializer
                .createProvider(rpcContext, deviceContext, convertorExecutor, multipartWriterProvider));
        rpcContext.registerRpcServiceImplementations(opendaylightDirectStatisticsRpcs,
            opendaylightDirectStatisticsRpcs.getRpcClassToInstanceMap());

        // register flat batch services
        final SalFlatBatchRpc salFlatBatchRpc = new SalFlatBatchRpc(
            new SalFlowsBatchRpcs(salFlowService, flowCapableTransactionService),
            new SalGroupsBatchRpcs(salGroupService, flowCapableTransactionService),
            new SalMetersBatchRpcs(salMeterService, flowCapableTransactionService));
        rpcContext.registerRpcServiceImplementations(salFlatBatchRpc, salFlatBatchRpc.getRpcClassToInstanceMap());

        // register experimenter services
        rpcContext.registerRpcServiceImplementation(SalExperimenterMessageService.class,
                new SalExperimenterMessageServiceImpl(rpcContext, deviceContext, extensionConverterProvider));
        rpcContext.registerRpcServiceImplementation(SalExperimenterMpMessageService.class,
                new SalExperimenterMpMessageServiceImpl(rpcContext, deviceContext, extensionConverterProvider));

        //register onf extension bundles
        rpcContext.registerRpcServiceImplementation(SalBundleService.class,
                new SalBundleServiceImpl(new SalExperimenterMessageServiceImpl(
                        rpcContext, deviceContext, extensionConverterProvider)));
    }

    /**
     * Support deprecated statistic related services for backward compatibility. The only exception from deprecation is
     * the aggregated flow statistic with match criteria input.
     *
     * @param rpcContext    - registration processing is implemented in
     *        {@link org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext}
     *
     * @param deviceContext - every service needs
     *        {@link org.opendaylight.openflowplugin.api.openflow.device.DeviceContext} as input parameter
     *
     * @param notificationPublishService - notification service
     *
     * @param convertorExecutor - convertor executor
     */
    public static void registerStatCompatibilityServices(final RpcContext rpcContext, final DeviceContext deviceContext,
                                                         final NotificationPublishService notificationPublishService,
                                                         final ConvertorExecutor convertorExecutor) {

        AtomicLong compatibilityXidSeed = new AtomicLong();
        // pickup low statistics service
        final OpendaylightFlowStatisticsService flowStatisticsService = requireNonNull(
                rpcContext.lookupRpcService(OpendaylightFlowStatisticsService.class));

        // attach delegate to flow statistics service (to cover all but aggregated stats with match filter input)
        final OpendaylightFlowStatisticsServiceDelegateImpl flowStatisticsDelegate =
                new OpendaylightFlowStatisticsServiceDelegateImpl(rpcContext, deviceContext, notificationPublishService,
                        new AtomicLong(), convertorExecutor);
        ((Delegator<OpendaylightFlowStatisticsService>) flowStatisticsService).setDelegate(flowStatisticsDelegate);

        final OpendaylightFlowTableStatisticsRpc opendaylightFlowTableStatisticsRpc =
            new OpendaylightFlowTableStatisticsRpc(rpcContext, deviceContext, compatibilityXidSeed,
                notificationPublishService);
        // register all statistics (deprecated) services
        rpcContext.registerRpcServiceImplementations(opendaylightFlowTableStatisticsRpc,
            opendaylightFlowTableStatisticsRpc.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementation(OpendaylightGroupStatisticsService.class,
                new OpendaylightGroupStatisticsServiceImpl(rpcContext, deviceContext,
                        compatibilityXidSeed, notificationPublishService, convertorExecutor));
        rpcContext.registerRpcServiceImplementation(OpendaylightMeterStatisticsService.class,
                new OpendaylightMeterStatisticsServiceImpl(rpcContext, deviceContext,
                        compatibilityXidSeed, notificationPublishService, convertorExecutor));
        rpcContext.registerRpcServiceImplementation(OpendaylightQueueStatisticsService.class,
                new OpendaylightQueueStatisticsServiceImpl(rpcContext, deviceContext,
                        compatibilityXidSeed, notificationPublishService));
        rpcContext.registerRpcServiceImplementation(OpendaylightPortStatisticsService.class,
                new OpendaylightPortStatisticsServiceImpl(rpcContext, deviceContext,
                        compatibilityXidSeed, notificationPublishService));
    }
}
