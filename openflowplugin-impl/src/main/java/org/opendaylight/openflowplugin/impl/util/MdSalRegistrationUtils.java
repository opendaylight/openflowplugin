/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableClassToInstanceMap;
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
import org.opendaylight.openflowplugin.impl.services.sal.SalBundleRpcs;
import org.opendaylight.openflowplugin.impl.services.sal.SalEchoRpc;
import org.opendaylight.openflowplugin.impl.services.sal.SalExperimenterMessageRpc;
import org.opendaylight.openflowplugin.impl.services.sal.SalExperimenterMpMessageRpc;
import org.opendaylight.openflowplugin.impl.services.sal.SalFlatBatchRpc;
import org.opendaylight.openflowplugin.impl.services.sal.SalFlowRpcs;
import org.opendaylight.openflowplugin.impl.services.sal.SalFlowsBatchRpcs;
import org.opendaylight.openflowplugin.impl.services.sal.SalGroupRpcs;
import org.opendaylight.openflowplugin.impl.services.sal.SalGroupsBatchRpcs;
import org.opendaylight.openflowplugin.impl.services.sal.SalMeterRpcs;
import org.opendaylight.openflowplugin.impl.services.sal.SalMetersBatchRpcs;
import org.opendaylight.openflowplugin.impl.services.sal.SalPortRpc;
import org.opendaylight.openflowplugin.impl.services.sal.SalTableRpc;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightFlowStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightFlowTableStatisticsRpc;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightGroupStatisticsRpcs;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightMeterStatisticsRpcs;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightPortStatisticsRpcs;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightQueueStatisticsRpcs;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.OpendaylightFlowStatisticsServiceDelegateImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.OpendaylightDirectStatisticsRpcs;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.multilayer.MultiLayerDirectStatisticsProviderInitializer;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.singlelayer.SingleLayerDirectStatisticsProviderInitializer;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEcho;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SendExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTable;

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
        final SalFlowRpcs salFlowRpcs = new SalFlowRpcs(rpcContext, deviceContext, convertorExecutor);
        final FlowCapableTransactionRpc flowCapableTransactionRpc =
                new FlowCapableTransactionRpc(rpcContext, deviceContext);
        final SalAsyncConfigRpcs salAsyncConfigRpcs = new SalAsyncConfigRpcs(rpcContext, deviceContext);
        final SalGroupRpcs salGroupRpcs = new SalGroupRpcs(rpcContext, deviceContext, convertorExecutor);
        final SalMeterRpcs salMeterRpcs = new SalMeterRpcs(rpcContext, deviceContext, convertorExecutor);
        final SalEchoRpc salEchoRpc = new SalEchoRpc(rpcContext, deviceContext);
        final NodeConfigRpc nodeConfigRpc = new NodeConfigRpc(rpcContext, deviceContext);
        final PacketProcessingRpc packetProcessingRpc =
                new PacketProcessingRpc(rpcContext, deviceContext, convertorExecutor);
        final SalPortRpc salPortRpc = new SalPortRpc(rpcContext, deviceContext, convertorExecutor);
        final SalTableRpc salTableRpc =
                new SalTableRpc(rpcContext, deviceContext, convertorExecutor, multipartWriterProvider);

        // register routed service instances
        rpcContext.registerRpcServiceImplementations(salEchoRpc,
            ImmutableClassToInstanceMap.of(SendEcho.class, salEchoRpc::sendEcho));
        rpcContext.registerRpcServiceImplementations(flowCapableTransactionRpc,
            ImmutableClassToInstanceMap.of(SendBarrier.class, flowCapableTransactionRpc::sendBarrier));
        rpcContext.registerRpcServiceImplementations(salFlowRpcs, salFlowRpcs.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(salGroupRpcs, salGroupRpcs.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(salMeterRpcs, salMeterRpcs.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(salAsyncConfigRpcs, salAsyncConfigRpcs.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(salTableRpc,
            ImmutableClassToInstanceMap.of(UpdateTable.class, salTableRpc::updateTable));
        rpcContext.registerRpcServiceImplementations(salPortRpc,
            ImmutableClassToInstanceMap.of(UpdatePort.class, salPortRpc::updatePort));
        rpcContext.registerRpcServiceImplementations(packetProcessingRpc,
            ImmutableClassToInstanceMap.of(TransmitPacket.class, packetProcessingRpc::transmitPacket));
        rpcContext.registerRpcServiceImplementations(nodeConfigRpc,
            ImmutableClassToInstanceMap.of(SetConfig.class, nodeConfigRpc::setConfig));
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
        final var flatBatchService = new SalFlatBatchRpc(
            new SalFlowsBatchRpcs(salFlowRpcs, flowCapableTransactionRpc),
            new SalGroupsBatchRpcs(salGroupRpcs, flowCapableTransactionRpc),
            new SalMetersBatchRpcs(salMeterRpcs, flowCapableTransactionRpc));
        rpcContext.registerRpcServiceImplementations(flatBatchService,
            ImmutableClassToInstanceMap.of(ProcessFlatBatch.class, flatBatchService::processFlatBatch));

        // register experimenter services
        final SalExperimenterMessageRpc salExperimenterMessageRpc =
                new SalExperimenterMessageRpc(rpcContext, deviceContext, extensionConverterProvider);
        final SalExperimenterMpMessageRpc salExperimenterMpMessageRpc =
                new SalExperimenterMpMessageRpc(rpcContext, deviceContext, extensionConverterProvider);
        rpcContext.registerRpcServiceImplementations(salExperimenterMessageRpc,
            ImmutableClassToInstanceMap.of(SendExperimenter.class, salExperimenterMessageRpc::sendExperimenter));
        rpcContext.registerRpcServiceImplementations(salExperimenterMpMessageRpc, ImmutableClassToInstanceMap.of(
            SendExperimenterMpRequest.class, salExperimenterMpMessageRpc::sendExperimenterMpRequest));

        //register onf extension bundles
        final SalBundleRpcs salBundleRpcs = new SalBundleRpcs(new SalExperimenterMessageRpc(rpcContext, deviceContext,
            extensionConverterProvider));
        rpcContext.registerRpcServiceImplementations(salBundleRpcs, salBundleRpcs.getRpcClassToInstanceMap());
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
        final OpendaylightGroupStatisticsRpcs opendaylightGroupStatisticsRpcs =
                new OpendaylightGroupStatisticsRpcs(rpcContext, deviceContext, compatibilityXidSeed,
                    notificationPublishService, convertorExecutor);
        final OpendaylightMeterStatisticsRpcs opendaylightMeterStatisticsRpcs =
            new OpendaylightMeterStatisticsRpcs(rpcContext, deviceContext,
                compatibilityXidSeed, notificationPublishService, convertorExecutor);
        final OpendaylightPortStatisticsRpcs opendaylightPortStatisticsRpcs =
            new OpendaylightPortStatisticsRpcs(rpcContext, deviceContext, compatibilityXidSeed,
                notificationPublishService);
        final OpendaylightQueueStatisticsRpcs opendaylightQueueStatisticsRpcs =
            new OpendaylightQueueStatisticsRpcs(rpcContext, deviceContext,
                compatibilityXidSeed, notificationPublishService);
        rpcContext.registerRpcServiceImplementations(opendaylightFlowTableStatisticsRpc,
            ImmutableClassToInstanceMap.of(GetFlowTablesStatistics.class,
                opendaylightFlowTableStatisticsRpc::getFlowTablesStatistics));
        rpcContext.registerRpcServiceImplementations(opendaylightGroupStatisticsRpcs,
            opendaylightGroupStatisticsRpcs.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(opendaylightMeterStatisticsRpcs,
            opendaylightMeterStatisticsRpcs.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(opendaylightQueueStatisticsRpcs,
            opendaylightQueueStatisticsRpcs.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(opendaylightPortStatisticsRpcs,
            opendaylightPortStatisticsRpcs.getRpcClassToInstanceMap());
    }
}
