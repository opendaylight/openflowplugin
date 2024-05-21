/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.infrautils.ready.SystemReadyListener;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupInfoHistories;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupInfoHistory;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.OpenFlowPluginExtensionRegistratorProvider;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterManager;
import org.opendaylight.openflowplugin.impl.connection.ConnectionManagerImpl;
import org.opendaylight.openflowplugin.impl.device.DeviceManagerImpl;
import org.opendaylight.openflowplugin.impl.device.initialization.DeviceInitializerProvider;
import org.opendaylight.openflowplugin.impl.device.initialization.DeviceInitializerProviderFactory;
import org.opendaylight.openflowplugin.impl.lifecycle.ContextChainHolderImpl;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.DeserializerInjector;
import org.opendaylight.openflowplugin.impl.protocol.serialization.SerializerInjector;
import org.opendaylight.openflowplugin.impl.role.RoleManagerImpl;
import org.opendaylight.openflowplugin.impl.rpc.RpcManagerImpl;
import org.opendaylight.openflowplugin.impl.statistics.StatisticsManagerImpl;
import org.opendaylight.openflowplugin.impl.util.ThreadPoolLoggingExecutor;
import org.opendaylight.openflowplugin.impl.util.TranslatorLibraryUtil;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionConverterManagerImpl;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(immediate = true, service = {
    OpenFlowPluginExtensionRegistratorProvider.class,
    FlowGroupInfoHistories.class
})
public final class OpenFlowPluginProviderImpl
        implements OpenFlowPluginExtensionRegistratorProvider, FlowGroupInfoHistories, SystemReadyListener,
                   AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(OpenFlowPluginProviderImpl.class);

    private static final int TICKS_PER_WHEEL = 500; // 0.5 sec.
    private static final long TICK_DURATION = 10;
    private static final String POOL_NAME = "ofppool";

    // TODO: Split this out into a separate component, which requires proper timer cancellation from all users. But is
    //       that worth the complications?
    private final HashedWheelTimer hashedWheelTimer =
            new HashedWheelTimer(TICK_DURATION, TimeUnit.MILLISECONDS, TICKS_PER_WHEEL);
    private final ExtensionConverterManager extensionConverterManager;
    private final List<SwitchConnectionProvider> switchConnectionProviders;
    private final DeviceInitializerProvider deviceInitializerProvider;
    private final ConvertorManager convertorManager;
    private final OpenflowProviderConfig config;
    private final DeviceManager deviceManager;
    private final RpcManager rpcManager;
    private final StatisticsManager statisticsManager;
    private final RoleManager roleManager;
    private final ExecutorService executorService;
    private final ContextChainHolderImpl contextChainHolder;
    private final DiagStatusProvider diagStatusProvider;
    private final SettableFuture<Void> fullyStarted = SettableFuture.create();

    private ConnectionManager connectionManager;

    @Inject
    @Activate
    public OpenFlowPluginProviderImpl(@Reference final ConfigurationService configurationService,
            @Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policyOption = ReferencePolicyOption.GREEDY)
            final List<SwitchConnectionProvider> switchConnectionProviders,
            @Reference final DataBroker dataBroker, @Reference final RpcProviderService rpcProviderRegistry,
            @Reference final NotificationPublishService notificationPublishService,
            @Reference final ClusterSingletonServiceProvider singletonServiceProvider,
            @Reference final EntityOwnershipService entityOwnershipService,
            @Reference final MastershipChangeServiceManager mastershipChangeServiceManager,
            @Reference final MessageIntelligenceAgency messageIntelligenceAgency,
            @Reference final DiagStatusProvider diagStatusProvider,
            @Reference final SystemReadyMonitor systemReadyMonitor) {
        config = configurationService.toProviderConfig();
        this.switchConnectionProviders = List.copyOf(switchConnectionProviders);
        final var ppdb = new PingPongDataBroker(dataBroker);
        this.diagStatusProvider = requireNonNull(diagStatusProvider);

        convertorManager = ConvertorManagerFactory.createDefaultManager();
        extensionConverterManager = new ExtensionConverterManagerImpl();
        deviceInitializerProvider = DeviceInitializerProviderFactory.createDefaultProvider();

        // TODO: copied from OpenFlowPluginProvider (Helium) misusesing the old way of distributing extension converters
        // TODO: rewrite later!
        OFSessionUtil.getSessionManager().setExtensionConverterProvider(extensionConverterManager);

        // Creates a thread pool that creates new threads as needed, but will reuse previously
        // constructed threads when they are available.
        // Threads that have not been used for x seconds are terminated and removed from the cache.
        executorService = new ThreadPoolLoggingExecutor(
                config.getThreadPoolMinThreads().toJava(),
                config.getThreadPoolMaxThreads().getValue().toJava(),
                config.getThreadPoolTimeout().toJava(),
                TimeUnit.SECONDS, new SynchronousQueue<>(), POOL_NAME);

        final var devMgr = new DeviceManagerImpl(
                config,
                ppdb,
                messageIntelligenceAgency,
                notificationPublishService,
                hashedWheelTimer,
                convertorManager,
                deviceInitializerProvider,
                executorService);
        deviceManager = devMgr;

        TranslatorLibraryUtil.injectBasicTranslatorLibrary(deviceManager, convertorManager);
        devMgr.setExtensionConverterProvider(extensionConverterManager);

        rpcManager = new RpcManagerImpl(
                config,
                rpcProviderRegistry,
                extensionConverterManager,
                convertorManager,
                notificationPublishService);

        statisticsManager = new StatisticsManagerImpl(
                config,
                rpcProviderRegistry,
                convertorManager,
                executorService);

        roleManager = new RoleManagerImpl(hashedWheelTimer, config, executorService);

        contextChainHolder = new ContextChainHolderImpl(
                executorService,
                singletonServiceProvider,
                entityOwnershipService,
                mastershipChangeServiceManager,
                config);

        contextChainHolder.addManager(deviceManager);
        contextChainHolder.addManager(statisticsManager);
        contextChainHolder.addManager(rpcManager);
        contextChainHolder.addManager(roleManager);

        connectionManager = new ConnectionManagerImpl(config, executorService, ppdb, notificationPublishService);
        connectionManager.setDeviceConnectedHandler(contextChainHolder);
        connectionManager.setDeviceDisconnectedHandler(contextChainHolder);

        deviceManager.setContextChainHolder(contextChainHolder);
        deviceManager.initialize();
        systemReadyMonitor.registerListener(this);
        LOG.info("registered onSystemBootReady() listener for OpenFlowPluginProvider");
    }

    @Override
    public void onSystemBootReady() {
        LOG.info("onSystemBootReady() received, starting the switch connections");
        Futures.addCallback(Futures.allAsList(switchConnectionProviders.stream().map(switchConnectionProvider -> {
            // Inject OpenFlowPlugin custom serializers and deserializers into OpenFlowJava
            if (config.getUseSingleLayerSerialization()) {
                SerializerInjector.injectSerializers(switchConnectionProvider,
                        switchConnectionProvider.getConfiguration().isGroupAddModEnabled());
                DeserializerInjector.injectDeserializers(switchConnectionProvider);
            } else {
                DeserializerInjector.revertDeserializers(switchConnectionProvider);
            }

            // Set handler of incoming connections and start switch connection provider
            switchConnectionProvider.setSwitchConnectionHandler(connectionManager);
            return switchConnectionProvider.startup();
        }).collect(Collectors.toSet())), new FutureCallback<List<Void>>() {
            @Override
            public void onSuccess(final List<Void> result) {
                LOG.info("All switchConnectionProviders are up and running ({}).", result.size());
                diagStatusProvider.reportStatus(ServiceState.OPERATIONAL);
                fullyStarted.set(null);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("Some switchConnectionProviders failed to start.", throwable);
                diagStatusProvider.reportStatus(ServiceState.ERROR, throwable);
                fullyStarted.setException(throwable);
            }
        }, MoreExecutors.directExecutor());
    }

    @VisibleForTesting
    public Future<Void> getFullyStarted() {
        return fullyStarted;
    }

    private ListenableFuture<List<Boolean>> shutdownSwitchConnections() {
        final ListenableFuture<List<Boolean>> listListenableFuture =
                Futures.allAsList(switchConnectionProviders.stream().map(switchConnectionProvider -> {
                    // Revert deserializers to their original state
                    if (config.getUseSingleLayerSerialization()) {
                        DeserializerInjector.revertDeserializers(switchConnectionProvider);
                    }

                    // Shutdown switch connection provider
                    return switchConnectionProvider.shutdown();
                }).collect(Collectors.toSet()));

        Futures.addCallback(listListenableFuture, new FutureCallback<List<Boolean>>() {
            @Override
            public void onSuccess(final List<Boolean> result) {
                LOG.info("All switchConnectionProviders were successfully shut down ({}).", result.size());
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("Some switchConnectionProviders failed to shutdown.", throwable);
            }
        }, MoreExecutors.directExecutor());

        return listListenableFuture;
    }

    @Override
    public ExtensionConverterRegistrator getExtensionConverterRegistrator() {
        return extensionConverterManager;
    }

    @Override
    public Map<NodeId, FlowGroupInfoHistory> getAllFlowGroupHistories() {
        return deviceManager.getAllFlowGroupHistories();
    }

    @Override
    public FlowGroupInfoHistory getFlowGroupHistory(final NodeId nodeId) {
        return deviceManager.getFlowGroupHistory(nodeId);
    }

    @Override
    @PreDestroy
    @Deactivate
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void close() {
        try {
            shutdownSwitchConnections().get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.warn("Failed to shut down switch connections in time {}s", 10, e);
        }

        gracefulShutdown(contextChainHolder);
        gracefulShutdown(connectionManager);
        gracefulShutdown(deviceManager);
        gracefulShutdown(rpcManager);
        gracefulShutdown(statisticsManager);
        gracefulShutdown(roleManager);
        gracefulShutdown(executorService);
        gracefulShutdown(hashedWheelTimer);
        diagStatusProvider.reportStatus(ServiceState.UNREGISTERED);
        try {
            if (connectionManager != null) {
                connectionManager.close();
                connectionManager = null;
            }
        } catch (Exception e) {
            LOG.error("Failed to close ConnectionManager", e);
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private static void gracefulShutdown(final AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                LOG.warn("Failed to shutdown {} gracefully.", closeable);
            }
        }
    }

    private static void gracefulShutdown(final Timer timer) {
        if (timer != null) {
            try {
                timer.stop();
            } catch (IllegalStateException e) {
                LOG.warn("Failed to shutdown {} gracefully.", timer);
            }
        }
    }

    private static void gracefulShutdown(final ExecutorService executorService) {
        if (executorService != null) {
            try {
                executorService.shutdownNow();
            } catch (SecurityException e) {
                LOG.warn("Failed to shutdown {} gracefully.", executorService);
            }
        }
    }


}
