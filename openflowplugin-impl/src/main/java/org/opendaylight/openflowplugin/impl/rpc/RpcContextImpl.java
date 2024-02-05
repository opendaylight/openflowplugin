/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
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
import org.opendaylight.openflowplugin.api.openflow.statistics.compatibility.Delegator;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.impl.services.sal.FlowCapableTransactionServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.NodeConfigServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.PacketProcessingServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalAsyncConfigServiceImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalBundleServiceImpl;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SalAsyncConfigService;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.OpendaylightMeterStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.NodeConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.SalBundleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.SalPortService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.OpendaylightPortStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.OpendaylightQueueStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.Uint32;
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

    private <S extends RpcService> S lookupRpcService(final Class<S> serviceClass) {
        final var registration = rpcRegistrations.get(serviceClass);
        return serviceClass.cast(registration.getInstance());
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
        } else {
            LOG.trace("Acquired semaphore for {}, available permits:{} ",
                    nodeInstanceIdentifier.getKey().getId().getValue(), tracker.availablePermits());
        }

        final Uint32 xid = deviceInfo.reserveXidForDeviceMessage();
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

    @Override
    public <S extends RpcService> void unregisterRpcServiceImplementation(final Class<S> serviceClass) {
        LOG.trace("Try to unregister serviceClass {} for Node {}",
                serviceClass, nodeInstanceIdentifier.getKey().getId());
        final ObjectRegistration<? extends RpcService> rpcRegistration = rpcRegistrations.remove(serviceClass);
        if (rpcRegistration != null) {
            rpcRegistration.close();
            LOG.debug("Un-registration serviceClass {} for Node {}", serviceClass.getSimpleName(),
                    nodeInstanceIdentifier.getKey().getId().getValue());
        }
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
    public void registerMastershipWatcher(@NonNull final ContextChainMastershipWatcher newWatcher) {
        contextChainMastershipWatcher = newWatcher;
    }

    @Override
    public ListenableFuture<?> closeServiceInstance() {
        unregisterRPCs();
        return Futures.immediateVoidFuture();
    }

    @Override
    public void instantiateServiceInstance() {
        // TODO: Use multipart writer provider from device context
        final var multipartWriterProvider = MultipartWriterProviderFactory.createDefaultProvider(deviceContext);

        // create service instances
        final var salFlowService = new SalFlowServiceImpl(this, deviceContext, convertorExecutor);
        final var flowCapableTransactionService = new FlowCapableTransactionServiceImpl(this, deviceContext);
        final var salAsyncConfigService = new SalAsyncConfigServiceImpl(this, deviceContext);
        final var salGroupService = new SalGroupServiceImpl(this, deviceContext, convertorExecutor);
        final var salMeterService = new SalMeterServiceImpl(this, deviceContext, convertorExecutor);

        // register routed service instances
        registerRpcServiceImplementation(SalEchoService.class, new SalEchoServiceImpl(this, deviceContext));
        registerRpcServiceImplementation(SalFlowService.class, salFlowService);
        registerRpcServiceImplementation(FlowCapableTransactionService.class, flowCapableTransactionService);
        registerRpcServiceImplementation(SalAsyncConfigService.class, salAsyncConfigService);
        registerRpcServiceImplementation(SalMeterService.class, salMeterService);
        registerRpcServiceImplementation(SalGroupService.class, salGroupService);
        registerRpcServiceImplementation(SalTableService.class,
            new SalTableServiceImpl(this, deviceContext, convertorExecutor, multipartWriterProvider));
        registerRpcServiceImplementation(SalPortService.class,
            new SalPortServiceImpl(this, deviceContext, convertorExecutor));
        registerRpcServiceImplementation(PacketProcessingService.class,
            new PacketProcessingServiceImpl(this, deviceContext, convertorExecutor));
        registerRpcServiceImplementation(NodeConfigService.class, new NodeConfigServiceImpl(this, deviceContext));
        registerRpcServiceImplementation(OpendaylightFlowStatisticsService.class,
            OpendaylightFlowStatisticsServiceImpl.createWithOook(this, deviceContext, convertorExecutor));

        // register direct statistics gathering services
        registerRpcServiceImplementation(OpendaylightDirectStatisticsService.class,
            new OpendaylightDirectStatisticsServiceImpl(deviceContext.canUseSingleLayerSerialization()
                ? SingleLayerDirectStatisticsProviderInitializer.createProvider(this, deviceContext, convertorExecutor,
                    multipartWriterProvider)
                : MultiLayerDirectStatisticsProviderInitializer.createProvider(this, deviceContext, convertorExecutor,
                    multipartWriterProvider)));

        // register flat batch services
        registerRpcServiceImplementation(SalFlatBatchService.class, new SalFlatBatchServiceImpl(
            new SalFlowsBatchServiceImpl(salFlowService, flowCapableTransactionService),
            new SalGroupsBatchServiceImpl(salGroupService, flowCapableTransactionService),
            new SalMetersBatchServiceImpl(salMeterService, flowCapableTransactionService)));

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
            // pickup low statistics service
            final var flowStatisticsService = requireNonNull(lookupRpcService(OpendaylightFlowStatisticsService.class));

            // attach delegate to flow statistics service (to cover all but aggregated stats with match filter input)
            final var flowStatisticsDelegate = new OpendaylightFlowStatisticsServiceDelegateImpl(this, deviceContext,
                notificationPublishService, new AtomicLong(), convertorExecutor);
            ((Delegator<OpendaylightFlowStatisticsService>) flowStatisticsService).setDelegate(flowStatisticsDelegate);

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

        contextChainMastershipWatcher.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.RPC_REGISTRATION);
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return deviceInfo.getServiceIdentifier();
    }
}
