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
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightFlowStatisticsRpcs;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightFlowTableStatisticsRpc;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightGroupStatisticsRpcs;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightMeterStatisticsRpcs;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightPortStatisticsRpcs;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightQueueStatisticsRpcs;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.OpendaylightFlowStatisticsDelegateRpcs;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.OpendaylightDirectStatisticsRpcs;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.multilayer.MultiLayerDirectStatisticsProviderInitializer;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.singlelayer.SingleLayerDirectStatisticsProviderInitializer;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;

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
        final SalFlowRpcs salFlowRpcs = new SalFlowRpcs(rpcContext, deviceContext,
                convertorExecutor);
        final FlowCapableTransactionRpc flowCapableTransactionRpc =
                new FlowCapableTransactionRpc(rpcContext, deviceContext);
        final SalAsyncConfigRpcs salAsyncConfigRpcs =
                new SalAsyncConfigRpcs(rpcContext, deviceContext);
        final SalGroupRpcs salGroupRpcs =
                new SalGroupRpcs(rpcContext, deviceContext, convertorExecutor);
        final SalMeterRpcs salMeterRpcs =
                new SalMeterRpcs(rpcContext, deviceContext, convertorExecutor);

        // register routed service instances
        rpcContext.registerRpcServiceImplementations(new SalEchoRpc(rpcContext, deviceContext)
            .getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(salFlowRpcs.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(flowCapableTransactionRpc.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(salAsyncConfigRpcs.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(salMeterRpcs.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(salGroupRpcs.getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(new SalTableRpc(rpcContext, deviceContext, convertorExecutor,
            multipartWriterProvider).getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(new SalPortRpc(rpcContext, deviceContext, convertorExecutor)
            .getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(new PacketProcessingRpc(rpcContext, deviceContext,
            convertorExecutor).getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(new NodeConfigRpc(rpcContext, deviceContext)
            .getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(OpendaylightFlowStatisticsRpcs
            .createWithOook(rpcContext, deviceContext, convertorExecutor).getRpcClassToInstanceMap());

        // register direct statistics gathering services
        rpcContext.registerRpcServiceImplementations(new OpendaylightDirectStatisticsRpcs(
            deviceContext.canUseSingleLayerSerialization()
                ? SingleLayerDirectStatisticsProviderInitializer
                    .createProvider(rpcContext, deviceContext, convertorExecutor, multipartWriterProvider)
                : MultiLayerDirectStatisticsProviderInitializer
                    .createProvider(rpcContext, deviceContext, convertorExecutor, multipartWriterProvider))
            .getRpcClassToInstanceMap());

        // register flat batch services
        rpcContext.registerRpcServiceImplementations(new SalFlatBatchRpc(
                new SalFlowsBatchRpcs(salFlowRpcs, flowCapableTransactionRpc),
                new SalGroupsBatchRpcs(salGroupRpcs, flowCapableTransactionRpc),
                new SalMetersBatchRpcs(salMeterRpcs, flowCapableTransactionRpc)
        ).getRpcClassToInstanceMap());

        // register experimenter services
        rpcContext.registerRpcServiceImplementations(new SalExperimenterMessageRpc(rpcContext, deviceContext,
            extensionConverterProvider).getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(new SalExperimenterMpMessageRpc(rpcContext, deviceContext,
            extensionConverterProvider).getRpcClassToInstanceMap());

        //register onf extension bundles
        rpcContext.registerRpcServiceImplementations(new SalBundleRpcs(new SalExperimenterMessageRpc(rpcContext,
            deviceContext, extensionConverterProvider)).getRpcClassToInstanceMap());
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
        final OpendaylightFlowStatisticsRpcs flowStatisticsService = requireNonNull(
                rpcContext.lookupRpcServices(new OpendaylightFlowStatisticsRpcs()));

        // attach delegate to flow statistics service (to cover all but aggregated stats with match filter input)
        final OpendaylightFlowStatisticsDelegateRpcs flowStatisticsDelegate =
                new OpendaylightFlowStatisticsDelegateRpcs(rpcContext, deviceContext, notificationPublishService,
                        new AtomicLong(), convertorExecutor);
        ((Delegator<OpendaylightFlowStatisticsRpcs>) flowStatisticsService).setDelegate(flowStatisticsDelegate);

        // register all statistics (deprecated) services
        rpcContext.registerRpcServiceImplementations(new OpendaylightFlowTableStatisticsRpc(rpcContext,
            deviceContext, compatibilityXidSeed, notificationPublishService).getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(new OpendaylightGroupStatisticsRpcs(rpcContext, deviceContext,
            compatibilityXidSeed, notificationPublishService, convertorExecutor).getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(new OpendaylightMeterStatisticsRpcs(rpcContext, deviceContext,
            compatibilityXidSeed, notificationPublishService, convertorExecutor).getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(new OpendaylightQueueStatisticsRpcs(rpcContext, deviceContext,
            compatibilityXidSeed, notificationPublishService).getRpcClassToInstanceMap());
        rpcContext.registerRpcServiceImplementations(new OpendaylightPortStatisticsRpcs(rpcContext, deviceContext,
            compatibilityXidSeed, notificationPublishService).getRpcClassToInstanceMap());
    }
}
