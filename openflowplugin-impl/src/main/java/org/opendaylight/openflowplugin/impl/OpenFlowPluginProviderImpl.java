/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import io.netty.util.HashedWheelTimer;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProvider;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainHolder;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterProviderKeeper;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.OpenFlowPluginExtensionRegistratorProvider;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterManager;
import org.opendaylight.openflowplugin.impl.connection.ConnectionManagerImpl;
import org.opendaylight.openflowplugin.impl.device.DeviceManagerImpl;
import org.opendaylight.openflowplugin.impl.device.initialization.DeviceInitializerProvider;
import org.opendaylight.openflowplugin.impl.device.initialization.DeviceInitializerProviderFactory;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.DeserializerInjector;
import org.opendaylight.openflowplugin.impl.protocol.serialization.SerializerInjector;
import org.opendaylight.openflowplugin.impl.lifecycle.ContextChainHolderImpl;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenFlowPluginProviderImpl implements OpenFlowPluginProvider, OpenFlowPluginConfigurationService, OpenFlowPluginExtensionRegistratorProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenFlowPluginProviderImpl.class);
    private static final MessageIntelligenceAgency messageIntelligenceAgency = new MessageIntelligenceAgencyImpl();
    private static final int TICKS_PER_WHEEL = 500; // 0.5 sec.
    private static final long TICK_DURATION = 10;
    private static final String POOL_NAME = "ofppool";

    private final HashedWheelTimer hashedWheelTimer = new HashedWheelTimer(TICK_DURATION, TimeUnit.MILLISECONDS, TICKS_PER_WHEEL);

    private final NotificationService notificationProviderService;
    private final NotificationPublishService notificationPublishService;
    private final ExtensionConverterManager extensionConverterManager;
    private final DataBroker dataBroker;
    private final Collection<SwitchConnectionProvider> switchConnectionProviders;
    private final DeviceInitializerProvider deviceInitializerProvider;
    private final ConvertorManager convertorManager;
    private final ContextChainHolder contextChainHolder;private int rpcRequestsQuota;
    private long globalNotificationQuota;
    private long barrierInterval;
    private int barrierCountLimit;
    private long echoReplyTimeout;
    private DeviceManager deviceManager;
    private RpcManager rpcManager;
    private RpcProviderRegistry rpcProviderRegistry;
    private StatisticsManager statisticsManager;
    private ConnectionManager connectionManager;
    private boolean switchFeaturesMandatory;
    private boolean isStatisticsPollingOn;
    private boolean isStatisticsRpcEnabled;
    private boolean isFlowRemovedNotificationOn;
    private boolean skipTableFeatures;
    private long basicTimerDelay;
    private long maximumTimerDelay;
    private boolean useSingleLayerSerialization;
    private ThreadPoolExecutor threadPool;
    private ClusterSingletonServiceProvider singletonServicesProvider;
    private int threadPoolMinThreads;
    private int threadPoolMaxThreads;
    private long threadPoolTimeout;
    private boolean initialized = false;

    public OpenFlowPluginProviderImpl(final List<SwitchConnectionProvider> switchConnectionProviders,
                                      final DataBroker dataBroker,
                                      final RpcProviderRegistry rpcProviderRegistry,
                                      final NotificationService notificationProviderService,
                                      final NotificationPublishService notificationPublishService,
                                      final ClusterSingletonServiceProvider singletonServiceProvider,
                                      final EntityOwnershipService entityOwnershipService) {
        this.switchConnectionProviders = switchConnectionProviders;
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.notificationProviderService = notificationProviderService;
        this.notificationPublishService = notificationPublishService;
        this.singletonServicesProvider = singletonServiceProvider;
        convertorManager = ConvertorManagerFactory.createDefaultManager();
        contextChainHolder = new ContextChainHolderImpl(hashedWheelTimer);
        contextChainHolder.changeEntityOwnershipService(entityOwnershipService);
        extensionConverterManager = new ExtensionConverterManagerImpl();
        deviceInitializerProvider = DeviceInitializerProviderFactory.createDefaultProvider();
    }


    private void startSwitchConnections() {
        Futures.addCallback(Futures.allAsList(switchConnectionProviders.stream().map(switchConnectionProvider -> {
            // Inject OpenflowPlugin custom serializers and deserializers into OpenflowJava
            if (useSingleLayerSerialization) {
                SerializerInjector.injectSerializers(switchConnectionProvider);
                DeserializerInjector.injectDeserializers(switchConnectionProvider);
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
            public void onFailure(@Nonnull final Throwable t) {
                LOG.warn("Some switchConnectionProviders failed to start.", t);
            }
        });
    }

    public static MessageIntelligenceAgency getMessageIntelligenceAgency() {
        return OpenFlowPluginProviderImpl.messageIntelligenceAgency;
    }

    @Override
    public void initialize() {
        Preconditions.checkNotNull(dataBroker, "missing data broker");
        Preconditions.checkNotNull(rpcProviderRegistry, "missing RPC provider registry");
        Preconditions.checkNotNull(notificationProviderService, "missing notification provider service");
        Preconditions.checkNotNull(singletonServicesProvider, "missing singleton services provider");

        // TODO: copied from OpenFlowPluginProvider (Helium) misusesing the old way of distributing extension converters
        // TODO: rewrite later!
        OFSessionUtil.getSessionManager().setExtensionConverterProvider(extensionConverterManager);

        // Creates a thread pool that creates new threads as needed, but will reuse previously
        // constructed threads when they are available.
        // Threads that have not been used for x seconds are terminated and removed from the cache.
        threadPool = new ThreadPoolLoggingExecutor(
                Preconditions.checkNotNull(threadPoolMinThreads),
                Preconditions.checkNotNull(threadPoolMaxThreads),
                Preconditions.checkNotNull(threadPoolTimeout), TimeUnit.SECONDS,
                new SynchronousQueue<>(), "ofppool");

        connectionManager = new ConnectionManagerImpl(threadPool);
        connectionManager.setEchoReplyTimeout(echoReplyTimeout);

        registerMXBean(messageIntelligenceAgency);

        contextChainHolder.addSingletonServicesProvider(singletonServicesProvider);

        deviceManager = new DeviceManagerImpl(
                dataBroker,
                getMessageIntelligenceAgency(),
                notificationPublishService,
                hashedWheelTimer,
                convertorManager,
                deviceInitializerProvider);

        deviceManager.setGlobalNotificationQuota(globalNotificationQuota);
        deviceManager.setSwitchFeaturesMandatory(switchFeaturesMandatory);
        deviceManager.setBarrierInterval(barrierInterval);
        deviceManager.setBarrierCountLimit(barrierCountLimit);
        deviceManager.setFlowRemovedNotificationOn(isFlowRemovedNotificationOn);
        deviceManager.setSkipTableFeatures(skipTableFeatures);
        deviceManager.setUseSingleLayerSerialization(useSingleLayerSerialization);

        ((ExtensionConverterProviderKeeper) deviceManager).setExtensionConverterProvider(extensionConverterManager);

        rpcManager = new RpcManagerImpl(rpcProviderRegistry, extensionConverterManager, convertorManager, notificationPublishService);
        rpcManager.setRpcRequestQuota(rpcRequestsQuota);

        statisticsManager = new StatisticsManagerImpl(rpcProviderRegistry, hashedWheelTimer, convertorManager);
        statisticsManager.setBasicTimerDelay(basicTimerDelay);
        statisticsManager.setMaximumTimerDelay(maximumTimerDelay);
        statisticsManager.setIsStatisticsPollingOn(isStatisticsPollingOn);

        // Device connection handler moved from device manager to context holder
        connectionManager.setDeviceConnectedHandler(contextChainHolder);

        /* Termination Phase ordering - OFP Device Context suite */
        connectionManager.setDeviceDisconnectedHandler(contextChainHolder);

        rpcManager.setStatisticsRpcEnabled(isStatisticsRpcEnabled);

        TranslatorLibraryUtil.injectBasicTranslatorLibrary(deviceManager, convertorManager);
        deviceManager.initialize();

        contextChainHolder.addManager(deviceManager);
        contextChainHolder.addManager(statisticsManager);
        contextChainHolder.addManager(rpcManager);

        startSwitchConnections();
        initialized = true;
    }


    @Override
    public ExtensionConverterRegistrator getExtensionConverterRegistrator() {
        return extensionConverterManager;
    }

    @Override
    public void close() throws Exception {
        initialized = false;
        //TODO: consider wrapping each manager into try-catch
        deviceManager.close();
        rpcManager.close();
        statisticsManager.close();

        // Manually shutdown all remaining running threads in pool
        threadPool.shutdown();
    }

    @Override
    public void update(final Map<String, Object> properties) {
        properties.forEach((key, value) -> {
            final PropertyType propertyType = PropertyType.forValue(key);

            if (Objects.nonNull(propertyType)) {
                updateProperty(propertyType, value.toString());
            } else if (!key.equals("service.pid") && !key.equals("felix.fileinstall.filename")) {
                LOG.warn("Unsupported configuration property '{}={}'", key, value);
            }
        });
    }

    @Override
    public void updateProperty(final PropertyType key, final Object value) {
        try {
            final String sValue = value.toString();

            switch (key) {
                case RPC_REQUESTS_QUOTA:
                    updateRpcRequestsQuota(Integer.valueOf(sValue));
                    break;
                case SWITCH_FEATURES_MANDATORY:
                    updateSwitchFeaturesMandatory(Boolean.valueOf(sValue));
                    break;
                case GLOBAL_NOTIFICATION_QUOTA:
                    updateGlobalNotificationQuota(Long.valueOf(sValue));
                    break;
                case IS_STATISTICS_POLLING_ON:
                    updateIsStatisticsPollingOn(Boolean.valueOf(sValue));
                    break;
                case IS_STATISTICS_RPC_ENABLED:
                    updateIsStatisticsRpcEnabled(Boolean.valueOf(sValue));
                    break;
                case BARRIER_INTERVAL_TIMEOUT:
                    updateBarrierIntervalTimeoutLimit(Long.valueOf(sValue));
                    break;
                case BARRIER_COUNT_LIMIT:
                    updateBarrierCountLimit(Integer.valueOf(sValue));
                    break;
                case ECHO_REPLY_TIMEOUT:
                    updateEchoReplyTimeout(Long.valueOf(sValue));
                    break;
                case THREAD_POOL_MIN_THREADS:
                    updateThreadPoolMinThreads(Integer.valueOf(sValue));
                    break;
                case THREAD_POOL_MAX_THREADS:
                    updateThreadPoolMaxThreads(Integer.valueOf(sValue));
                    break;
                case THREAD_POOL_TIMEOUT:
                    updateThreadPoolTimeout(Long.valueOf(sValue));
                    break;
                case ENABLE_FLOW_REMOVED_NOTIFICATION:
                    updateEnableFlowRemovedNotification(Boolean.valueOf(sValue));
                    break;
                case SKIP_TABLE_FEATURES:
                    updateSkipTableFeatures(Boolean.valueOf(sValue));
                    break;
                case BASIC_TIMER_DELAY:
                    updateBasicTimerDelay(Long.valueOf(sValue));
                    break;
                case MAXIMUM_TIMER_DELAY:
                    updateMaximumTimerDelay(Long.valueOf(sValue));
                    break;
                case USE_SINGLE_LAYER_SERIALIZATION:
                    updateUseSingleLayerSerialization(Boolean.valueOf(sValue));
                    break;
                default:
                    LOG.warn("Unsupported configuration property '{}={}'", key, sValue);
            }
        } catch (final Exception ex) {
            LOG.warn("Failed to read configuration property '{}={}', error: {}", key, value, ex);
        }
    }

    private void updateIsStatisticsPollingOn(final boolean isStatisticsPollingOn) {
        if (initialized && this.isStatisticsPollingOn == isStatisticsPollingOn) {
            LOG.debug("is-statistics-polling-on config parameter is already set to {})", isStatisticsPollingOn);
            return;
        }

        LOG.info("is-statistics-polling-on config parameter is updated ({} -> {})", this.isStatisticsPollingOn, isStatisticsPollingOn);
        this.isStatisticsPollingOn = isStatisticsPollingOn;

        if (initialized) {
            statisticsManager.setIsStatisticsPollingOn(isStatisticsPollingOn);
        }
    }

    private void updateBarrierCountLimit(final int barrierCountLimit) {
        if (initialized && this.barrierCountLimit == barrierCountLimit) {
            LOG.debug("barrier-count-limit config parameter is already set to {})", barrierCountLimit);
            return;
        }

        LOG.info("barrier-count-limit config parameter is updated ({} -> {})", this.barrierCountLimit, barrierCountLimit);
        this.barrierCountLimit = barrierCountLimit;

        if (initialized) {
            deviceManager.setBarrierCountLimit(barrierCountLimit);
        }
    }

    private void updateBarrierIntervalTimeoutLimit(final long barrierInterval) {
        if (initialized && this.barrierInterval == barrierInterval) {
            LOG.debug("barrier-interval-timeout-limit config parameter is already set to {})", barrierInterval);
            return;
        }

        LOG.info("barrier-interval-timeout-limit config parameter is updated ({} -> {})", this.barrierInterval, barrierInterval);
        this.barrierInterval = barrierInterval;

        if (initialized) {
            deviceManager.setBarrierInterval(barrierInterval);
        }
    }

    private void updateEchoReplyTimeout(final long echoReplyTimeout) {
        if (initialized && this.echoReplyTimeout == echoReplyTimeout) {
            LOG.debug("echo-reply-timeout config parameter is already set to {})", echoReplyTimeout);
            return;
        }

        LOG.info("echo-reply-timeout config parameter is updated ({} -> {})", this.echoReplyTimeout, echoReplyTimeout);
        this.echoReplyTimeout = echoReplyTimeout;

        if (initialized) {
            connectionManager.setEchoReplyTimeout(echoReplyTimeout);
        }
    }

    private void updateEnableFlowRemovedNotification(boolean isFlowRemovedNotificationOn) {
        if (initialized && this.isFlowRemovedNotificationOn == isFlowRemovedNotificationOn) {
            LOG.debug("enable-flow-removed-notification config parameter is already set to {})", isFlowRemovedNotificationOn);
            return;
        }

        LOG.info("enable-flow-removed-notification config parameter is updated ({} -> {})", this.isFlowRemovedNotificationOn, isFlowRemovedNotificationOn);
        this.isFlowRemovedNotificationOn = isFlowRemovedNotificationOn;

        if (initialized) {
            deviceManager.setFlowRemovedNotificationOn(isFlowRemovedNotificationOn);
        }
    }

    private void updateSkipTableFeatures(final boolean skipTableFeatures){
        if (initialized && this.skipTableFeatures == skipTableFeatures) {
            LOG.debug("skip-table-features config parameter is already set to {})", skipTableFeatures);
            return;
        }

        LOG.info("skip-table-features config parameter is updated ({} -> {})", this.skipTableFeatures, skipTableFeatures);
        this.skipTableFeatures = skipTableFeatures;

        if (initialized) {
            deviceManager.setSkipTableFeatures(skipTableFeatures);
        }
    }

    private void updateBasicTimerDelay(long basicTimerDelay) {
        if (initialized && this.basicTimerDelay == basicTimerDelay) {
            LOG.debug("basic-timer-delay config parameter is already set to {})", basicTimerDelay);
            return;
        }

        LOG.info("basic-timer-delay config parameter is updated ({} -> {})", this.basicTimerDelay, basicTimerDelay);
        this.basicTimerDelay = basicTimerDelay;

        if (initialized) {
            statisticsManager.setBasicTimerDelay(basicTimerDelay);
        }
    }

    private void updateMaximumTimerDelay(long maximumTimerDelay) {
        if (initialized && this.maximumTimerDelay == maximumTimerDelay) {
            LOG.debug("maximum-timer-delay config parameter is already set to {})", maximumTimerDelay);
            return;
        }

        LOG.info("maximum-timer-delay config parameter is updated ({} -> {})", this.maximumTimerDelay, maximumTimerDelay);
        this.maximumTimerDelay = maximumTimerDelay;

        if (initialized) {
            statisticsManager.setMaximumTimerDelay(maximumTimerDelay);
        }
    }

    private void updateSwitchFeaturesMandatory(final boolean switchFeaturesMandatory) {
        if (initialized && this.switchFeaturesMandatory == switchFeaturesMandatory) {
            LOG.debug("switch-features-mandatory config parameter is already set to {})", switchFeaturesMandatory);
            return;
        }

        LOG.info("switch-features-mandatory config parameter is updated ({} -> {})", this.switchFeaturesMandatory, switchFeaturesMandatory);
        this.switchFeaturesMandatory = switchFeaturesMandatory;

        if (initialized) {
            deviceManager.setSwitchFeaturesMandatory(switchFeaturesMandatory);
        }
    }

    private void updateIsStatisticsRpcEnabled(final boolean isStatisticsRpcEnabled) {
        if (initialized && this.isStatisticsRpcEnabled == isStatisticsRpcEnabled) {
            LOG.debug("is-statistics-rpc-enabled config parameter is already set to {})", isStatisticsRpcEnabled);
            return;
        }

        LOG.info("is-statistics-rpc-enabled config parameter is updated ({} -> {})", this.isStatisticsRpcEnabled, isStatisticsRpcEnabled);
        this.isStatisticsRpcEnabled = isStatisticsRpcEnabled;

        if (initialized) {
            rpcManager.setStatisticsRpcEnabled(isStatisticsRpcEnabled);
        }
    }

    private void updateUseSingleLayerSerialization(boolean useSingleLayerSerialization) {
        if (initialized && this.useSingleLayerSerialization == useSingleLayerSerialization) {
            LOG.debug("use-single-layer-serialization config parameter is already set to {})", useSingleLayerSerialization);
            return;
        }

        LOG.info("use-single-layer-serialization config parameter is updated ({} -> {})", this.useSingleLayerSerialization, useSingleLayerSerialization);

        if (this.useSingleLayerSerialization != useSingleLayerSerialization) {
            this.useSingleLayerSerialization = useSingleLayerSerialization;

            switchConnectionProviders.forEach(switchConnectionProvider -> {
                if (useSingleLayerSerialization) {
                    SerializerInjector.injectSerializers(switchConnectionProvider);
                    DeserializerInjector.injectDeserializers(switchConnectionProvider);
                } else {
                    DeserializerInjector.revertDeserializers(switchConnectionProvider);
                }
            });
        }
    }

    private void updateRpcRequestsQuota(final int rpcRequestsQuota) {
        if (initialized && this.rpcRequestsQuota == rpcRequestsQuota) {
            LOG.debug("rpc-requests-quota config parameter is already set to {})", rpcRequestsQuota);
            return;
        }

        LOG.info("rpc-requests-quota config parameter is updated ({} -> {})", this.rpcRequestsQuota, rpcRequestsQuota);
        this.rpcRequestsQuota = rpcRequestsQuota;

        if (initialized) {
            rpcManager.setRpcRequestQuota(rpcRequestsQuota);
        }
    }

    private void updateGlobalNotificationQuota(final long globalNotificationQuota) {
        if (initialized && this.globalNotificationQuota == globalNotificationQuota) {
            LOG.debug("global-notification-quota config parameter is already set to {})", globalNotificationQuota);
            return;
        }

        LOG.info("global-notification-quota config parameter is updated ({} -> {})", this.globalNotificationQuota, globalNotificationQuota);
        this.globalNotificationQuota = globalNotificationQuota;

        if (initialized) {
            deviceManager.setGlobalNotificationQuota(globalNotificationQuota);
        }
    }

    private void updateThreadPoolMinThreads(final int threadPoolMinThreads) {
        if (initialized && this.threadPoolMinThreads == threadPoolMinThreads) {
            LOG.debug("thread-pool-min-threads config parameter is already set to {})", threadPoolMinThreads);
            return;
        }

        if (initialized) {
            LOG.warn("thread-pool-min-threads update ({} -> {}) is not allowed after controller start", this.threadPoolMinThreads, threadPoolMinThreads);
            return;
        }

        LOG.info("thread-pool-min-threads config parameter is updated ({} -> {})", this.threadPoolMinThreads, threadPoolMaxThreads);
        this.threadPoolMinThreads = threadPoolMinThreads;
    }

    private void updateThreadPoolMaxThreads(final int threadPoolMaxThreads) {
        if (initialized && this.threadPoolMaxThreads == threadPoolMaxThreads) {
            LOG.debug("thread-pool-max-threads config parameter is already set to {})", threadPoolMaxThreads);
            return;
        }

        if (initialized) {
            LOG.warn("thread-pool-max-threads update ({} -> {}) is not allowed after controller start", this.threadPoolMaxThreads, threadPoolMaxThreads);
            return;
        }

        LOG.info("thread-pool-max-threads config parameter is updated ({} -> {})", this.threadPoolMaxThreads, threadPoolMaxThreads);
        this.threadPoolMaxThreads = threadPoolMaxThreads;
    }

    private void updateThreadPoolTimeout(final long threadPoolTimeout) {
        if (initialized && this.threadPoolTimeout == threadPoolTimeout) {
            LOG.debug("thread-pool-timeout config parameter is already set to {})", threadPoolTimeout);
            return;
        }

        if (initialized) {
            LOG.warn("thread-pool-timeout update ({} -> {}) is not allowed after controller start", this.threadPoolTimeout, threadPoolTimeout);
            return;
        }

        LOG.info("thread-pool-timeout config parameter is updated ({} -> {})", this.threadPoolTimeout, threadPoolTimeout);
        this.threadPoolTimeout = threadPoolTimeout;
    }

    private static void registerMXBean(final MessageIntelligenceAgency messageIntelligenceAgency) {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            final String pathToMxBean = String.format("%s:type=%s",
                    MessageIntelligenceAgencyMXBean.class.getPackage().getName(),
                    MessageIntelligenceAgencyMXBean.class.getSimpleName());
            final ObjectName name = new ObjectName(pathToMxBean);
            mbs.registerMBean(messageIntelligenceAgency, name);
        } catch (MalformedObjectNameException
                | NotCompliantMBeanException
                | MBeanRegistrationException
                | InstanceAlreadyExistsException e) {
            LOG.warn("Error registering MBean {}", e);
        }
    }
}
