/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
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
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProvider;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
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
import org.opendaylight.openflowplugin.impl.role.RoleManagerImpl;
import org.opendaylight.openflowplugin.impl.rpc.RpcManagerImpl;
import org.opendaylight.openflowplugin.impl.statistics.StatisticsManagerImpl;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.MessageIntelligenceAgencyImpl;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.MessageIntelligenceAgencyMXBean;
import org.opendaylight.openflowplugin.impl.util.ThreadPoolLoggingExecutor;
import org.opendaylight.openflowplugin.impl.util.TranslatorLibraryUtil;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionConverterManagerImpl;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OpenFlowPluginProviderImpl implements
        OpenFlowPluginProvider,
        OpenFlowPluginExtensionRegistratorProvider,
        FlowGroupInfoHistories,
        SystemReadyListener {
    @ObjectClassDefinition
    public @interface Configuration {
        @AttributeDefinition(description = "Quota for maximum number of RPC requests")
        int rpc$_$requests$_$quota() default 20_000;

        @AttributeDefinition(description = """
            This parameter indicates whether it is mandatory for switch to support OF1.3 features: \
            table, flow, meter, group. If this is set to true and switch does not support these features its connection
            will be denied""")
        boolean switch$_$features$_$mandatory() default false;

        @AttributeDefinition(description = "Global notification quota")
        int global$_$notification$_$quota() default 64_000;

        @AttributeDefinition(description = "If enabled, periodic statistics gathering will be turned on")
        boolean is$_$statistics$_$polling$_$on() default true;

        @AttributeDefinition(description = "If enabled, periodic table statistics gathering will be turned on")
        boolean is$_$table$_$statistics$_$polling$_$on() default true;

        @AttributeDefinition(description = "If enabled, periodic flow statistics gathering will be turned on")
        boolean is$_$flow$_$statistics$_$polling$_$on() default true;

        @AttributeDefinition(description = "If enabled, periodic group statistics gathering will be turned on")
        boolean is$_$group$_$statistics$_$polling$_$on() default true;

        @AttributeDefinition(description = "If enabled, periodic meter statistics gathering will be turned on")
        boolean is$_$meter$_$statistics$_$polling$_$on() default true;

        @AttributeDefinition(description = "If enabled, periodic port statistics gathering will be turned on")
        boolean is$_$port$_$statistics$_$polling$_$on() default true;

        @AttributeDefinition(description = "If enabled, periodic queue statistics gathering will be turned on")
        boolean is$_$queue$_$statistics$_$polling$_$on() default true;

        @Deprecated
        @AttributeDefinition(description = """
            Expose backward compatible statistics RPCs providing result in form of asynchronous notification. This is
            deprecated, use direct statistics instead.""")
        boolean is$_$statistics$_$rpc$_$enabled() default false;

//        #
//        # Barrier timeout
//        #
//        # barrier-interval-timeout-limit=500
//
//        #
//        # Barrier limit
//        #
//        # barrier-count-limit=25600
//
//        #
//        # How long we should wait for echo reply (value is in milliseconds)
//        #
//        # echo-reply-timeout=2000
//
//        #
//        # Minimum (starting) number of threads in thread pool
//        #
//        # thread-pool-min-threads=1
//
//        #
//        # Maximum number of threads in thread pool
//        #
//        # thread-pool-max-threads=32000
//
//        #
//        # After how much time (in seconds) of inactivity will be threads in pool
//        # terminated
//        #
//        # thread-pool-timeout=60
//
//        #
//        # Turning on flow removed notification
//        #
//        # enable-flow-removed-notification=true
//
//        #
//        # Ability to skip pulling and storing of large table features. These features
//        # are still available via rpc but if set to true then maintenance in DS will be
//        # omitted
//        #
//        # skip-table-features=true
//
//        #
//        # Initial delay used in polling the statistics, value is in milliseconds
//        #
//        # basic-timer-delay=3000
//
//        #
//        # Maximum timer delay is the wait time to collect next statistics used in
//        # polling the statistics, value is in milliseconds
//        #
//        # maximum-timer-delay=900000
//
//        #
//        # When true, openflowplugin won't send any specific role
//        # request down to the switch after plugin internally decides the
//        # ownership of the device using Entity Ownership Service. In this
//        # scenario, controller connection for the device will be in equal
//        # role. The behavior will be same for single node setup and clustered
//        # setup. In clustered scenario, all the controller will be in equal
//        # role for the device. In this case device will send all asynchronous
//        # event messages (e.g packet_in) to all the controllers, but openflowplugin
//        # will drop these events for the controller instances that is internally
//        # not owning the device.
//        #
//        # enable-equal-role=false
//
//        #
//        # When true, Yang models are serialized and deserialized directly to and from
//        # format supported by device, so serialization and deserialization is faster.
//        # Otherwise, models are first serialized to Openflow specification models and
//        # then to format supported by device, and reversed when deserializing.
//        #
//        # use-single-layer-serialization=true
//
//        #
//        # To limit the number of datapath nodes to be connected to the controller instance
//        # per minute. When the default value of zero is set, then the device connection rate
//        # limitter will be disabled. If it is set to any value, then only those many
//        # number of datapath nodes are allowed to connect to the controller in a minute
//        #
//        # device-connection-rate-limit-per-min=0
//
//        #
//        # Device connection hold time is the least time delay in seconds a device has
//        # to maintain between its consecutive connection attempts. If time delay between
//        # the previous connection and the current connection is within device connection
//        # hold time, the device will not be allowed to connect to the controller.
//        # Default value of the device connection hold time is 0 second
//        #
//        # device-connection-hold-time-in-seconds=0
//
//        #
//        # Delay (in milliseconds) before device is removed from the operational data
//        # store in the event of device disconnection from the controller.
//        #
//        # device-datastore-removal-delay=500
//        #############################################################################
//        #                                                                           #
//        #            Forwarding Rule Manager Application Configuration              #
//        #                                                                           #
//        #############################################################################
//
//        #
//        # Disable the default switch reconciliation mechanism
//        #
//        # disable-reconciliation=false
//
//        #
//        # Enable stale marking for switch reconciliation. Once user enable this feature
//        # forwarding rule manager will keep track of any change to the config data store
//        # while the switch is disconnected from controller. Once switch reconnect to the
//        # controller it will apply those changes to the switch and do the reconciliation
//        # of other configuration as well.
//        # NOTE: This option will be effective only if disable_reconciliation=false.
//        #
//        # stale-marking-enabled=false
//
//        #
//        # Number of time forwarding rules manager should retry to reconcile any specific
//        # configuration.
//        #
//        # reconciliation-retry-count=5
//
//        #
//        # Bundle reconciliation can be enabled by making this flag to true.
//        # By default bundle reconciliation is disabled and reconciliation happens
//        # via normal flow/group mods.
//        # NOTE: This option will be effective with disable_reconciliation=false.
//        #
//        # bundle-based-reconciliation-enabled=false
//
//        #############################################################################
//        #                                                                           #
//        #            Topology Lldp Discovery Configuration                          #
//        #                                                                           #
//        #############################################################################
//
//        # Periodic interval for sending LLDP packet for link discovery
//        # topology-lldp-interval=5000
//
//        # Timeout duration for LLDP response message
//        # topology-lldp-expiration-interval=60000

    }


    private static final Logger LOG = LoggerFactory.getLogger(OpenFlowPluginProviderImpl.class);

    private static final int TICKS_PER_WHEEL = 500; // 0.5 sec.
    private static final long TICK_DURATION = 10;
    private static final String POOL_NAME = "ofppool";

    private static final MessageIntelligenceAgency MESSAGE_INTELLIGENCE_AGENCY = new MessageIntelligenceAgencyImpl();
    private static final String MESSAGE_INTELLIGENCE_AGENCY_MX_BEAN_NAME = String
            .format("%s:type=%s",
                    MessageIntelligenceAgencyMXBean.class.getPackage().getName(),
                    MessageIntelligenceAgencyMXBean.class.getSimpleName());

    private final HashedWheelTimer hashedWheelTimer =
            new HashedWheelTimer(TICK_DURATION, TimeUnit.MILLISECONDS, TICKS_PER_WHEEL);
    private final NotificationPublishService notificationPublishService;
    private final ExtensionConverterManager extensionConverterManager;
    private final DataBroker dataBroker;
    private final Collection<SwitchConnectionProvider> switchConnectionProviders;
    private final DeviceInitializerProvider deviceInitializerProvider;
    private final ConvertorManager convertorManager;
    private final RpcProviderService rpcProviderRegistry;
    private final ClusterSingletonServiceProvider singletonServicesProvider;
    private final OpenflowProviderConfig config;
    private final EntityOwnershipService entityOwnershipService;
    private final MastershipChangeServiceManager mastershipChangeServiceManager;
    private DeviceManager deviceManager;
    private RpcManager rpcManager;
    private StatisticsManager statisticsManager;
    private RoleManager roleManager;
    private ConnectionManager connectionManager;
    private ExecutorService executorService;
    private ContextChainHolderImpl contextChainHolder;
    private final DiagStatusProvider diagStatusProvider;
    private final SystemReadyMonitor systemReadyMonitor;
    private final SettableFuture<Void> fullyStarted = SettableFuture.create();
    private static final String OPENFLOW_SERVICE_NAME = "OPENFLOW";

    public static MessageIntelligenceAgency getMessageIntelligenceAgency() {
        return MESSAGE_INTELLIGENCE_AGENCY;
    }

    @Inject
    public OpenFlowPluginProviderImpl(final ConfigurationService configurationService,
                               final List<SwitchConnectionProvider> switchConnectionProviders,
                               final DataBroker dataBroker,
                               final RpcProviderService rpcProviderRegistry,
                               final NotificationPublishService notificationPublishService,
                               final ClusterSingletonServiceProvider singletonServiceProvider,
                               final EntityOwnershipService entityOwnershipService,
                               final MastershipChangeServiceManager mastershipChangeServiceManager,
                               final DiagStatusProvider diagStatusProvider,
                               final SystemReadyMonitor systemReadyMonitor) {
        this.switchConnectionProviders = switchConnectionProviders;
        this.dataBroker = new PingPongDataBroker(dataBroker);
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.notificationPublishService = notificationPublishService;
        singletonServicesProvider = singletonServiceProvider;
        this.entityOwnershipService = entityOwnershipService;
        convertorManager = ConvertorManagerFactory.createDefaultManager();
        extensionConverterManager = new ExtensionConverterManagerImpl();
        deviceInitializerProvider = DeviceInitializerProviderFactory.createDefaultProvider();
        config = new OpenFlowProviderConfigImpl(configurationService);
        this.mastershipChangeServiceManager = mastershipChangeServiceManager;
        this.diagStatusProvider = diagStatusProvider;
        this.systemReadyMonitor = systemReadyMonitor;
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
        }).collect(Collectors.toSet())), new FutureCallback<List<Boolean>>() {
            @Override
            public void onSuccess(final List<Boolean> result) {
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
    @PostConstruct
    public void initialize() {
        registerMXBean(MESSAGE_INTELLIGENCE_AGENCY, MESSAGE_INTELLIGENCE_AGENCY_MX_BEAN_NAME);

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

        deviceManager = new DeviceManagerImpl(
                config,
                dataBroker,
                getMessageIntelligenceAgency(),
                notificationPublishService,
                hashedWheelTimer,
                convertorManager,
                deviceInitializerProvider,
                executorService);

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
                convertorManager,
                executorService);

        roleManager = new RoleManagerImpl(hashedWheelTimer, config, executorService);

        contextChainHolder = new ContextChainHolderImpl(
                executorService,
                singletonServicesProvider,
                entityOwnershipService,
                mastershipChangeServiceManager,
                config);

        contextChainHolder.addManager(deviceManager);
        contextChainHolder.addManager(statisticsManager);
        contextChainHolder.addManager(rpcManager);
        contextChainHolder.addManager(roleManager);

        connectionManager = new ConnectionManagerImpl(config, executorService, dataBroker, notificationPublishService);
        connectionManager.setDeviceConnectedHandler(contextChainHolder);
        connectionManager.setDeviceDisconnectedHandler(contextChainHolder);

        deviceManager.setContextChainHolder(contextChainHolder);
        deviceManager.initialize();
        systemReadyMonitor.registerListener(this);
        LOG.info("registered onSystemBootReady() listener for OpenFlowPluginProvider");
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
        unregisterMXBean(MESSAGE_INTELLIGENCE_AGENCY_MX_BEAN_NAME);
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

    private static void registerMXBean(final Object bean, final String beanName) {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        try {
            mbs.registerMBean(bean, new ObjectName(beanName));
        } catch (MalformedObjectNameException
                | NotCompliantMBeanException
                | MBeanRegistrationException
                | InstanceAlreadyExistsException e) {
            LOG.warn("Error registering MBean {}", beanName, e);
        }
    }

    private static void unregisterMXBean(final String beanName) {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        try {
            mbs.unregisterMBean(new ObjectName(beanName));
        } catch (InstanceNotFoundException
                | MBeanRegistrationException
                | MalformedObjectNameException e) {
            LOG.warn("Error unregistering MBean {}", beanName, e);
        }
    }
}
