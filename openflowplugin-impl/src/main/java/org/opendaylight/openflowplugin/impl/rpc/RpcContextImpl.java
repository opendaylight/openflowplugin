/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipWatcher;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.impl.services.SendEchoImpl;
import org.opendaylight.openflowplugin.impl.services.sal.AddFlowImpl;
import org.opendaylight.openflowplugin.impl.services.sal.AddGroupImpl;
import org.opendaylight.openflowplugin.impl.services.sal.AddMeterImpl;
import org.opendaylight.openflowplugin.impl.services.sal.RemoveFlowImpl;
import org.opendaylight.openflowplugin.impl.services.sal.RemoveGroupImpl;
import org.opendaylight.openflowplugin.impl.services.sal.RemoveMeterImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalBundleServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalExperimenterMessageServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalExperimenterMpMessageServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalFlatBatchServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalFlowsBatchServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalGroupsBatchServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalMetersBatchServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalPortServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SendBarrierImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SetConfigImpl;
import org.opendaylight.openflowplugin.impl.services.sal.TransmitPacketImpl;
import org.opendaylight.openflowplugin.impl.services.sal.UpdateFlowImpl;
import org.opendaylight.openflowplugin.impl.services.sal.UpdateGroupImpl;
import org.opendaylight.openflowplugin.impl.services.sal.UpdateMeterImpl;
import org.opendaylight.openflowplugin.impl.services.sal.UpdateTableImpl;
import org.opendaylight.openflowplugin.impl.services.singlelayer.GetAsyncImpl;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SetAsyncImpl;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsync;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsync;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.OpendaylightDirectStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEcho;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SalExperimenterMessageService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SalExperimenterMpMessageService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.SalFlatBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.OpendaylightFlowTableStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.OpendaylightGroupStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.OpendaylightMeterStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.SalBundleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.SalPortService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.OpendaylightPortStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.OpendaylightQueueStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTable;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class RpcContextImpl implements RpcContext {
    private static final Logger LOG = LoggerFactory.getLogger(RpcContextImpl.class);

    private final RpcProviderService rpcProviderRegistry;
    private final MessageSpy messageSpy;
    private final Semaphore tracker;
    private final boolean isStatisticsRpcEnabled;

    // TODO: add private Sal salBroker
    private final ConcurrentMap<Class<?>, ObjectRegistration<? extends RpcService>> rpcRegistrations =
            new ConcurrentHashMap<>();
    private final KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier;
    private final DeviceInfo deviceInfo;
    private final DeviceContext deviceContext;
    private final ExtensionConverterProvider extensionConverterProvider;
    private final ConvertorExecutor convertorExecutor;
    private final NotificationPublishService notificationPublishService;

    private ContextChainMastershipWatcher contextChainMastershipWatcher = null;

    RpcContextImpl(@NonNull final RpcProviderService rpcProviderRegistry,
                   final int maxRequests,
                   @NonNull final DeviceContext deviceContext,
                   @NonNull final ExtensionConverterProvider extensionConverterProvider,
                   @NonNull final ConvertorExecutor convertorExecutor,
                   @NonNull final NotificationPublishService notificationPublishService,
                   final boolean statisticsRpcEnabled) {
        this.deviceContext = deviceContext;
        deviceInfo = deviceContext.getDeviceInfo();
        nodeInstanceIdentifier = deviceContext.getDeviceInfo().getNodeInstanceIdentifier();
        messageSpy = deviceContext.getMessageSpy();
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.extensionConverterProvider = extensionConverterProvider;
        this.notificationPublishService = notificationPublishService;
        this.convertorExecutor = convertorExecutor;
        isStatisticsRpcEnabled = statisticsRpcEnabled;
        tracker = new Semaphore(maxRequests, true);
    }

    private <S extends RpcService> void registerRpcServiceImplementation(final Class<S> serviceClass,
            final S serviceInstance) {
        if (!rpcRegistrations.containsKey(serviceClass)) {
            final ObjectRegistration<S> routedRpcReg = rpcProviderRegistry.registerRpcImplementation(serviceClass,
                serviceInstance, ImmutableSet.of(nodeInstanceIdentifier));
            rpcRegistrations.put(serviceClass, routedRpcReg);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Registration of service {} for device {}.",
                        serviceClass.getSimpleName(),
                        nodeInstanceIdentifier.getKey().getId().getValue());
            }
        }
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return deviceInfo.getServiceIdentifier();
    }

    @Override
    public void close() {
        unregisterRPCs();
    }

    private void unregisterRPCs() {
        for (var iterator = Iterators.consumingIterator(rpcRegistrations.entrySet().iterator()); iterator.hasNext(); ) {
            final var rpcRegistration = iterator.next().getValue();
            rpcRegistration.close();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Closing RPC Registration of service {} for device {}.",
                        rpcRegistration.getInstance().getClass().getSimpleName(),
                        nodeInstanceIdentifier.getKey().getId().getValue());
            }
        }
    }

    @Override
    public <T> RequestContext<T> createRequestContext() {
        if (!tracker.tryAcquire()) {
            LOG.trace("Device queue {} at capacity", this);
            return null;
        }

        LOG.trace("Acquired semaphore for {}, available permits:{} ",
            nodeInstanceIdentifier.getKey().getId().getValue(), tracker.availablePermits());

        final var xid = deviceInfo.reserveXidForDeviceMessage();
        if (xid == null) {
            LOG.warn("Xid cannot be reserved for new RequestContext, node:{}",
                    nodeInstanceIdentifier.getKey().getId().getValue());
            tracker.release();
            return null;
        }

        return new AbstractRequestContext<>(xid) {
            @Override
            public void close() {
                tracker.release();
                LOG.trace("Removed request context with xid {}", getXid().getValue());
                messageSpy.spyMessage(RpcContextImpl.class, MessageSpy.StatisticsGroup.REQUEST_STACK_FREED);
            }
        };
    }

    @VisibleForTesting
    boolean isEmptyRpcRegistrations() {
        return rpcRegistrations.isEmpty();
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public void registerMastershipWatcher(final ContextChainMastershipWatcher newWatcher) {
        contextChainMastershipWatcher = newWatcher;
    }

    @Override
    public ListenableFuture<?> closeServiceInstance() {
        unregisterRPCs();
        return Futures.immediateVoidFuture();
    }

    @Override
    public void instantiateServiceInstance() {
        // FIXME:: Use multipart writer provider from device context
        final var multipartWriterProvider = MultipartWriterProviderFactory.createDefaultProvider(deviceContext);

        // flow-capable-transaction.yang
        final var sendBarrier = new SendBarrierImpl(this, deviceContext);

        // node-config.yang
        final var setConfig = new SetConfigImpl(this, deviceContext);

        // packet-processing.yang
        final var transmitPacket = new TransmitPacketImpl(this, deviceContext, convertorExecutor);

        // sal-async-config.yang
        final var getAsync = new GetAsyncImpl(this, deviceContext);
        final var setAsync = new SetAsyncImpl(this, deviceContext);

        // sal-echo.yang
        final var sendEcho = new SendEchoImpl(this, deviceContext);

        // sal-flow.yang
        final var addFlow = new AddFlowImpl(this, deviceContext, convertorExecutor);
        final var removeFlow = new RemoveFlowImpl(this, deviceContext, convertorExecutor);
        final var updateFlow = new UpdateFlowImpl(this, deviceContext, convertorExecutor);

        // sal-group.yang
        final var addGroup = new AddGroupImpl(this, deviceContext, convertorExecutor);
        final var removeGroup = new RemoveGroupImpl(this, deviceContext, convertorExecutor);
        final var updateGroup = new UpdateGroupImpl(this, deviceContext, convertorExecutor);

        // sal-meter.yang
        final var addMeter = new AddMeterImpl(this, deviceContext, convertorExecutor);
        final var removeMeter = new RemoveMeterImpl(this, deviceContext, convertorExecutor);
        final var updateMeter = new UpdateMeterImpl(this, deviceContext, convertorExecutor);

        // sal-table.yang
        final var updateTable = new UpdateTableImpl(this, deviceContext, convertorExecutor, multipartWriterProvider);

        final var reg = rpcProviderRegistry.registerRpcImplementations(ImmutableClassToInstanceMap.<Rpc<?, ?>>builder()
            .put(SendBarrier.class, sendBarrier)
            .put(SetConfig.class, setConfig)
            .put(TransmitPacket.class, transmitPacket)
            .put(GetAsync.class, getAsync)
            .put(SetAsync.class, setAsync)
            .put(SendEcho.class, sendEcho)
            .put(AddFlow.class, addFlow)
            .put(RemoveFlow.class, removeFlow)
            .put(UpdateFlow.class, updateFlow)
            .put(AddGroup.class, addGroup)
            .put(RemoveGroup.class, removeGroup)
            .put(UpdateGroup.class, updateGroup)
            .put(AddMeter.class, addMeter)
            .put(RemoveMeter.class, removeMeter)
            .put(UpdateMeter.class, updateMeter)
            .put(UpdateTable.class, updateTable)
            .build(), Set.of(nodeInstanceIdentifier));

        final var flowStatisticsService = OpendaylightFlowStatisticsServiceImpl.createWithOook(this, deviceContext,
            convertorExecutor);

        // register routed service instances
        registerRpcServiceImplementation(SalPortService.class,
            new SalPortServiceImpl(this, deviceContext, convertorExecutor));
        registerRpcServiceImplementation(OpendaylightFlowStatisticsService.class, flowStatisticsService);

        // register direct statistics gathering services
        registerRpcServiceImplementation(OpendaylightDirectStatisticsService.class,
            new OpendaylightDirectStatisticsServiceImpl(deviceContext.canUseSingleLayerSerialization()
                ? SingleLayerDirectStatisticsProviderInitializer.createProvider(this, deviceContext, convertorExecutor,
                    multipartWriterProvider)
                : MultiLayerDirectStatisticsProviderInitializer.createProvider(this, deviceContext, convertorExecutor,
                    multipartWriterProvider)));

        // register flat batch services
        registerRpcServiceImplementation(SalFlatBatchService.class, new SalFlatBatchServiceImpl(
            new SalFlowsBatchServiceImpl(salFlowService, sendBarrier),
            new SalGroupsBatchServiceImpl(salGroupService, sendBarrier),
            new SalMetersBatchServiceImpl(salMeterService, sendBarrier)));

        // register experimenter services
        registerRpcServiceImplementation(SalExperimenterMessageService.class,
            new SalExperimenterMessageServiceImpl(this, deviceContext, extensionConverterProvider));
        registerRpcServiceImplementation(SalExperimenterMpMessageService.class,
            new SalExperimenterMpMessageServiceImpl(this, deviceContext, extensionConverterProvider));

        //register onf extension bundles
        registerRpcServiceImplementation(SalBundleService.class, new SalBundleServiceImpl(
            new SalExperimenterMessageServiceImpl(this, deviceContext, extensionConverterProvider)));

        // Support deprecated statistic related services for backward compatibility. The only exception from deprecation
        // is the aggregated flow statistic with match criteria input.
        if (isStatisticsRpcEnabled && !deviceContext.canUseSingleLayerSerialization()) {
            final var compatibilityXidSeed = new AtomicLong();

            // attach delegate to flow statistics service (to cover all but aggregated stats with match filter input)
            final var flowStatisticsDelegate = new OpendaylightFlowStatisticsServiceDelegateImpl(this, deviceContext,
                notificationPublishService, new AtomicLong(), convertorExecutor);
            flowStatisticsService.setDelegate(flowStatisticsDelegate);

            // register all statistics (deprecated) services
            registerRpcServiceImplementation(OpendaylightFlowTableStatisticsService.class,
                new OpendaylightFlowTableStatisticsServiceImpl(this, deviceContext,
                    compatibilityXidSeed, notificationPublishService));
            registerRpcServiceImplementation(OpendaylightGroupStatisticsService.class,
                new OpendaylightGroupStatisticsServiceImpl(this, deviceContext,
                    compatibilityXidSeed, notificationPublishService, convertorExecutor));
            registerRpcServiceImplementation(OpendaylightMeterStatisticsService.class,
                new OpendaylightMeterStatisticsServiceImpl(this, deviceContext,
                    compatibilityXidSeed, notificationPublishService, convertorExecutor));
            registerRpcServiceImplementation(OpendaylightQueueStatisticsService.class,
                new OpendaylightQueueStatisticsServiceImpl(this, deviceContext,
                    compatibilityXidSeed, notificationPublishService));
            registerRpcServiceImplementation(OpendaylightPortStatisticsService.class,
                new OpendaylightPortStatisticsServiceImpl(this, deviceContext,
                    compatibilityXidSeed, notificationPublishService));
        }

        final var local = contextChainMastershipWatcher;
        if (local != null) {
            local.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.RPC_REGISTRATION);
        }
    }
}
