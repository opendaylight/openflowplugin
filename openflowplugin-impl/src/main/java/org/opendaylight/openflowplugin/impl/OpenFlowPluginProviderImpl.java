/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProvider;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterProviderKeeper;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.OpenFlowPluginExtensionRegistratorProvider;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterManager;
import org.opendaylight.openflowplugin.impl.configuration.OpenFlowProviderConfigImpl;
import org.opendaylight.openflowplugin.impl.connection.ConnectionManagerImpl;
import org.opendaylight.openflowplugin.impl.device.DeviceManagerImpl;
import org.opendaylight.openflowplugin.impl.device.initialization.DeviceInitializerProvider;
import org.opendaylight.openflowplugin.impl.device.initialization.DeviceInitializerProviderFactory;
import org.opendaylight.openflowplugin.impl.lifecycle.ContextChainHolderImpl;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.DeserializerInjector;
import org.opendaylight.openflowplugin.impl.protocol.serialization.SerializerInjector;
import org.opendaylight.openflowplugin.impl.rpc.RpcManagerImpl;
import org.opendaylight.openflowplugin.impl.statistics.StatisticsManagerImpl;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.MessageIntelligenceAgencyImpl;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.MessageIntelligenceAgencyMXBean;
import org.opendaylight.openflowplugin.impl.util.TranslatorLibraryUtil;
import org.opendaylight.openflowplugin.openflow.md.core.ThreadPoolLoggingExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionConverterManagerImpl;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenFlowPluginProviderImpl implements OpenFlowPluginProvider, OpenFlowPluginExtensionRegistratorProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenFlowPluginProviderImpl.class);

    private static final int TICKS_PER_WHEEL = 500; // 0.5 sec.
    private static final long TICK_DURATION = 10;
    private static final String POOL_NAME = "ofppool";

    private static final MessageIntelligenceAgency MESSAGE_INTELLIGENCE_AGENCY = new MessageIntelligenceAgencyImpl();
    private static final String MESSAGE_INTELLIGENCE_AGENCY_MX_BEAN_NAME = String
            .format("%s:type=%s",
                    MessageIntelligenceAgencyMXBean.class.getPackage().getName(),
                    MessageIntelligenceAgencyMXBean.class.getSimpleName());

    private final HashedWheelTimer hashedWheelTimer = new HashedWheelTimer(TICK_DURATION, TimeUnit.MILLISECONDS, TICKS_PER_WHEEL);
    private final NotificationPublishService notificationPublishService;
    private final ExtensionConverterManager extensionConverterManager;
    private final DataBroker dataBroker;
    private final Collection<SwitchConnectionProvider> switchConnectionProviders;
    private final DeviceInitializerProvider deviceInitializerProvider;
    private final ConvertorManager convertorManager;
    private final RpcProviderRegistry rpcProviderRegistry;
    private final ClusterSingletonServiceProvider singletonServicesProvider;
    private final OpenflowProviderConfig config;
    private final EntityOwnershipService entityOwnershipService;
    private DeviceManager deviceManager;
    private RpcManager rpcManager;
    private StatisticsManager statisticsManager;
    private ConnectionManager connectionManager;
    private ThreadPoolExecutor threadPool;
    private ContextChainHolderImpl contextChainHolder;

    public static MessageIntelligenceAgency getMessageIntelligenceAgency() {
        return MESSAGE_INTELLIGENCE_AGENCY;
    }

    OpenFlowPluginProviderImpl(final ConfigurationService configurationService,
                               final List<SwitchConnectionProvider> switchConnectionProviders,
                               final DataBroker dataBroker,
                               final RpcProviderRegistry rpcProviderRegistry,
                               final NotificationPublishService notificationPublishService,
                               final ClusterSingletonServiceProvider singletonServiceProvider,
                               final EntityOwnershipService entityOwnershipService) {
        this.switchConnectionProviders = switchConnectionProviders;
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.notificationPublishService = notificationPublishService;
        this.singletonServicesProvider = singletonServiceProvider;
        this.entityOwnershipService = entityOwnershipService;
        convertorManager = ConvertorManagerFactory.createDefaultManager();
        extensionConverterManager = new ExtensionConverterManagerImpl();
        deviceInitializerProvider = DeviceInitializerProviderFactory.createDefaultProvider();
        config = new OpenFlowProviderConfigImpl(configurationService);
    }


    private void startSwitchConnections() {
        Futures.addCallback(Futures.allAsList(switchConnectionProviders.stream().map(switchConnectionProvider -> {
            // Inject OpenFlowPlugin custom serializers and deserializers into OpenFlowJava
            if (config.isUseSingleLayerSerialization()) {
                SerializerInjector.injectSerializers(switchConnectionProvider);
                DeserializerInjector.injectDeserializers(switchConnectionProvider);
            } else {
                DeserializerInjector.revertDeserializers(switchConnectionProvider);
            }

            // Set handler of incoming connections and start switch connection provider
            switchConnectionProvider.setSwitchConnectionHandler(connectionManager);
            return switchConnectionProvider.startup();
        }).collect(Collectors.toSet())), new FutureCallback<List<Boolean>>() {
            @Override
            public void onSuccess(final List<Boolean> result) {
                LOG.info("All switchConnectionProviders are up and running ({}).", result.size());
            }

            @Override
            public void onFailure(@Nonnull final Throwable throwable) {
                LOG.warn("Some switchConnectionProviders failed to start.", throwable);
            }
        });
    }

    private ListenableFuture<List<Boolean>> shutdownSwitchConnections() {
        final ListenableFuture<List<Boolean>> listListenableFuture = Futures.allAsList(switchConnectionProviders.stream().map(switchConnectionProvider -> {
            // Revert deserializers to their original state
            if (config.isUseSingleLayerSerialization()) {
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
            public void onFailure(@Nonnull final Throwable throwable) {
                LOG.warn("Some switchConnectionProviders failed to shutdown.", throwable);
            }
        });

        return listListenableFuture;
    }

    @Override
    public void initialize() {
        registerMXBean(MESSAGE_INTELLIGENCE_AGENCY, MESSAGE_INTELLIGENCE_AGENCY_MX_BEAN_NAME);

        // TODO: copied from OpenFlowPluginProvider (Helium) misusesing the old way of distributing extension converters
        // TODO: rewrite later!
        OFSessionUtil.getSessionManager().setExtensionConverterProvider(extensionConverterManager);

        // Creates a thread pool that creates new threads as needed, but will reuse previously
        // constructed threads when they are available.
        // Threads that have not been used for x seconds are terminated and removed from the cache.
        threadPool = new ThreadPoolLoggingExecutor(
                config.getThreadPoolMinThreads(),
                config.getThreadPoolMaxThreads().getValue(),
                config.getThreadPoolTimeout(),
                TimeUnit.SECONDS, new SynchronousQueue<>(), POOL_NAME);

        deviceManager = new DeviceManagerImpl(
                config,
                dataBroker,
                getMessageIntelligenceAgency(),
                notificationPublishService,
                hashedWheelTimer,
                convertorManager,
                deviceInitializerProvider);

        TranslatorLibraryUtil.injectBasicTranslatorLibrary(deviceManager, convertorManager);
        ((ExtensionConverterProviderKeeper) deviceManager).setExtensionConverterProvider(extensionConverterManager);

        rpcManager = new RpcManagerImpl(
                config,
                rpcProviderRegistry,
                extensionConverterManager,
                convertorManager,
                notificationPublishService);

        statisticsManager = new StatisticsManagerImpl(
                config,
                rpcProviderRegistry,
                hashedWheelTimer,
                convertorManager);

        contextChainHolder = new ContextChainHolderImpl(hashedWheelTimer, threadPool);
        contextChainHolder.changeEntityOwnershipService(entityOwnershipService);
        contextChainHolder.addSingletonServicesProvider(singletonServicesProvider);
        contextChainHolder.addManager(deviceManager);
        contextChainHolder.addManager(statisticsManager);
        contextChainHolder.addManager(rpcManager);

        connectionManager = new ConnectionManagerImpl(config, threadPool);
        connectionManager.setDeviceConnectedHandler(contextChainHolder);
        connectionManager.setDeviceDisconnectedHandler(contextChainHolder);

        deviceManager.initialize();
        startSwitchConnections();
    }

    @Override
    public ExtensionConverterRegistrator getExtensionConverterRegistrator() {
        return extensionConverterManager;
    }

    @Override
    public void close() {
        try {
            shutdownSwitchConnections().get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.warn("Failed to shut down switch connections in time {}s, error: {}", 10, e);
        }

        gracefulShutdown(contextChainHolder);
        gracefulShutdown(deviceManager);
        gracefulShutdown(rpcManager);
        gracefulShutdown(statisticsManager);
        gracefulShutdown(threadPool);
        gracefulShutdown(hashedWheelTimer);
        unregisterMXBean(MESSAGE_INTELLIGENCE_AGENCY_MX_BEAN_NAME);
    }

    private static void gracefulShutdown(final AutoCloseable closeable) {
        if (Objects.isNull(closeable)) {
            return;
        }

        try {
            closeable.close();
        } catch (Exception e) {
            LOG.warn("Failed to shutdown {} gracefully.", closeable);
        }
    }

    private static void gracefulShutdown(final Timer timer) {
        if (Objects.isNull(timer)) {
            return;
        }

        try {
            timer.stop();
        } catch (Exception e) {
            LOG.warn("Failed to shutdown {} gracefully.", timer);
        }
    }

    private static void gracefulShutdown(final ThreadPoolExecutor threadPoolExecutor) {
        if (Objects.isNull(threadPoolExecutor)) {
            return;
        }

        try {
            threadPoolExecutor.shutdownNow();
        } catch (Exception e) {
            LOG.warn("Failed to shutdown {} gracefully.", threadPoolExecutor);
        }
    }

    private static void registerMXBean(final Object bean, final String beanName) {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        try {
            mbs.registerMBean(bean, new ObjectName(beanName));
        } catch (MalformedObjectNameException
                | NotCompliantMBeanException
                | MBeanRegistrationException
                | InstanceAlreadyExistsException e) {
            LOG.warn("Error registering MBean {}", e);
        }
    }

    private static void unregisterMXBean(final String beanName) {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        try {
            mbs.unregisterMBean(new ObjectName(beanName));
        } catch (InstanceNotFoundException
                | MBeanRegistrationException
                | MalformedObjectNameException e) {
            LOG.warn("Error unregistering MBean {}", e);
        }
    }
}
