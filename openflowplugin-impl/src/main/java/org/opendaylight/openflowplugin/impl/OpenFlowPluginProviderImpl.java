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
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProvider;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
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

    private final HashedWheelTimer hashedWheelTimer = new HashedWheelTimer(TICK_DURATION, TimeUnit.MILLISECONDS, TICKS_PER_WHEEL);

    private int rpcRequestsQuota;
    private long globalNotificationQuota;
    private final ConvertorManager convertorManager;
    private long barrierInterval;
    private int barrierCountLimit;
    private long echoReplyTimeout;
    private DeviceManager deviceManager;
    private RpcManager rpcManager;
    private RpcProviderRegistry rpcProviderRegistry;
    private StatisticsManager statisticsManager;
    private ConnectionManager connectionManager;
    private final NotificationService notificationProviderService;
    private final NotificationPublishService notificationPublishService;
    private final ExtensionConverterManager extensionConverterManager;
    private final DataBroker dataBroker;
    private final Collection<SwitchConnectionProvider> switchConnectionProviders;
    private boolean switchFeaturesMandatory;
    private boolean isStatisticsPollingOn;
    private boolean isStatisticsRpcEnabled;
    private boolean isFlowRemovedNotificationOn;
    private boolean skipTableFeatures;
    private long basicTimerDelay;
    private long maximumTimerDelay;
    private boolean useSingleLayerSerialization;
    private final DeviceInitializerProvider deviceInitializerProvider;

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
                                      final ClusterSingletonServiceProvider singletonServiceProvider) {
        this.switchConnectionProviders = switchConnectionProviders;
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.notificationProviderService = notificationProviderService;
        this.notificationPublishService = notificationPublishService;
        this.singletonServicesProvider = singletonServiceProvider;
        convertorManager = ConvertorManagerFactory.createDefaultManager();
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

    @Override
    public void updateIsStatisticsPollingOn(final boolean isStatisticsPollingOn) {
        LOG.info("is-statistics-polling-on update ({} -> {})", this.isStatisticsPollingOn, isStatisticsPollingOn);
        this.isStatisticsPollingOn = isStatisticsPollingOn;

        if (initialized) {
            statisticsManager.setIsStatisticsPollingOn(isStatisticsPollingOn);
        }
    }

    @Override
    public void updateBarrierCountLimit(final int barrierCountLimit) {
        LOG.info("barrier-count-limit update ({} -> {})", this.barrierCountLimit, barrierCountLimit);
        this.barrierCountLimit = barrierCountLimit;

        if (initialized) {
            deviceManager.setBarrierCountLimit(barrierCountLimit);
        }
    }

    @Override
    public void updateBarrierIntervalTimeoutLimit(final long barrierInterval) {
        LOG.info("barrier-interval-timeout-limit update ({} -> {})", this.barrierInterval, barrierInterval);
        this.barrierInterval = barrierInterval;

        if (initialized) {
            deviceManager.setBarrierInterval(barrierInterval);
        }
    }

    @Override
    public void updateEchoReplyTimeout(final long echoReplyTimeout) {
        LOG.info("echo-reply-timeout update ({} -> {})", this.echoReplyTimeout, echoReplyTimeout);
        this.echoReplyTimeout = echoReplyTimeout;

        if (initialized) {
            connectionManager.setEchoReplyTimeout(echoReplyTimeout);
        }
    }

    @Override
    public void updateEnableFlowRemovedNotification(boolean isFlowRemovedNotificationOn) {
        LOG.info("enable-flow-removed-notification update ({} -> {})", this.isFlowRemovedNotificationOn, isFlowRemovedNotificationOn);
        this.isFlowRemovedNotificationOn = isFlowRemovedNotificationOn;

        if (initialized) {
            deviceManager.setFlowRemovedNotificationOn(isFlowRemovedNotificationOn);
        }
    }

    @Override
    public void updateSkipTableFeatures(final boolean skipTableFeatures){
        LOG.info("skip-table-features update ({} -> {})", this.skipTableFeatures, skipTableFeatures);
        this.skipTableFeatures = skipTableFeatures;

        if (initialized) {
            deviceManager.setSkipTableFeatures(skipTableFeatures);
        }
    }

    @Override
    public void updateBasicTimerDelay(long basicTimerDelay) {
        LOG.info("basic-timer-delay update ({} -> {})", this.basicTimerDelay, basicTimerDelay);
        this.basicTimerDelay = basicTimerDelay;

        if (initialized) {
            statisticsManager.setBasicTimerDelay(basicTimerDelay);
        }
    }

    @Override
    public void updateMaximumTimerDelay(long maximumTimerDelay) {
        LOG.info("maximum-timer-delay update ({} -> {})", this.maximumTimerDelay, maximumTimerDelay);
        this.maximumTimerDelay = maximumTimerDelay;

        if (initialized) {
            statisticsManager.setMaximumTimerDelay(maximumTimerDelay);
        }
    }

    @Override
    public void updateSwitchFeaturesMandatory(final boolean switchFeaturesMandatory) {
        LOG.info("switch-features-mandatory update ({} -> {})", this.switchFeaturesMandatory, switchFeaturesMandatory);
        this.switchFeaturesMandatory = switchFeaturesMandatory;

        if (initialized) {
            deviceManager.setSwitchFeaturesMandatory(switchFeaturesMandatory);
        }
    }

    @Override
    public void updateIsStatisticsRpcEnabled(final boolean isStatisticsRpcEnabled) {
        LOG.info("is-statistics-rpc-enabled update ({} -> {})", this.isStatisticsRpcEnabled, isStatisticsRpcEnabled);
        this.isStatisticsRpcEnabled = isStatisticsRpcEnabled;

        if (initialized) {
            rpcManager.setStatisticsRpcEnabled(isStatisticsRpcEnabled);
        }
    }

    @Override
    public void updateUseSingleLayerSerialization(boolean useSingleLayer) {
        LOG.info("use-single-layer-serialization update ({} -> {})", this.useSingleLayerSerialization, useSingleLayer);

        if (useSingleLayer != useSingleLayerSerialization) {
            useSingleLayerSerialization = useSingleLayer;

            if (useSingleLayer) {
                switchConnectionProviders.forEach(switchConnectionProvider -> {
                    SerializerInjector.injectSerializers(switchConnectionProvider);
                    DeserializerInjector.injectDeserializers(switchConnectionProvider);
                });
            } else {
                switchConnectionProviders.forEach(DeserializerInjector::revertDeserializers);
            }
        }
    }

    @Override
    public void updateRpcRequestsQuota(final int rpcRequestsQuota) {
        LOG.info("rpc-requests-quota update ({} -> {})", this.rpcRequestsQuota, rpcRequestsQuota);
        this.rpcRequestsQuota = rpcRequestsQuota;

        if (initialized) {
            rpcManager.setRpcRequestQuota(rpcRequestsQuota);
        }
    }

    @Override
    public void updateGlobalNotificationQuota(final long globalNotificationQuota) {
        LOG.info("global-notification-quota update ({} -> {})", this.globalNotificationQuota, globalNotificationQuota);
        this.globalNotificationQuota = globalNotificationQuota;

        if (initialized) {
            deviceManager.setGlobalNotificationQuota(globalNotificationQuota);
        }
    }

    @Override
    public void updateThreadPoolMinThreads(final int threadPoolMinThreads) {
        if (initialized) {
            LOG.warn("thread-pool-min-threads update ({} -> {}) is not permitted on the fly", this.threadPoolMinThreads, threadPoolMinThreads);
            return;
        }

        LOG.info("thread-pool-min-threads update ({} -> {})", this.threadPoolMinThreads, threadPoolMaxThreads);
        this.threadPoolMinThreads = threadPoolMinThreads;
    }

    @Override
    public void updateThreadPoolMaxThreads(final int threadPoolMaxThreads) {
        if (initialized) {
            LOG.warn("thread-pool-max-threads update ({} -> {}) is not permitted on the fly", this.threadPoolMaxThreads, threadPoolMaxThreads);
            return;
        }

        LOG.info("thread-pool-max-threads update ({} -> {})", this.threadPoolMaxThreads, threadPoolMaxThreads);
        this.threadPoolMaxThreads = threadPoolMaxThreads;
    }

    @Override
    public void updateThreadPoolTimeout(final long threadPoolTimeout) {
        if (initialized) {
            LOG.warn("thread-pool-timeout update ({} -> {}) is not permitted on the fly", this.threadPoolTimeout, threadPoolTimeout);
            return;
        }

        LOG.info("thread-pool-timeout update ({} -> {})", this.threadPoolTimeout, threadPoolTimeout);
        this.threadPoolTimeout = threadPoolTimeout;
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

        deviceManager = new DeviceManagerImpl(
            dataBroker,
            getMessageIntelligenceAgency(),
            singletonServicesProvider,
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

        /* Initialization Phase ordering - OFP Device Context suite */
        // CM -> DM -> SM -> RPC -> Role -> DM
        connectionManager.setDeviceConnectedHandler(deviceManager);
        deviceManager.setDeviceInitializationPhaseHandler(statisticsManager);
        statisticsManager.setDeviceInitializationPhaseHandler(rpcManager);
        rpcManager.setDeviceInitializationPhaseHandler(deviceManager);

        /* Termination Phase ordering - OFP Device Context suite */
        deviceManager.setDeviceTerminationPhaseHandler(rpcManager);
        rpcManager.setDeviceTerminationPhaseHandler(statisticsManager);
        statisticsManager.setDeviceTerminationPhaseHandler(deviceManager);

        rpcManager.setStatisticsRpcEnabled(isStatisticsRpcEnabled);

        TranslatorLibraryUtil.injectBasicTranslatorLibrary(deviceManager, convertorManager);
        deviceManager.initialize();

        startSwitchConnections();
        initialized = true;
    }

    @Override
    public void update(final Map<String, Object> properties) {
        properties.forEach((key, value) -> {
            final String sValue = value.toString();

            try {
                switch (key) {
                    case "rpc-requests-quota":
                        updateRpcRequestsQuota(Integer.valueOf(sValue));
                        break;
                    case "switch-features-mandatory":
                        updateSwitchFeaturesMandatory(Boolean.valueOf(sValue));
                        break;
                    case "global-notification-quota":
                        updateGlobalNotificationQuota(Long.valueOf(sValue));
                        break;
                    case "is-statistics-polling-on":
                        updateIsStatisticsPollingOn(Boolean.valueOf(sValue));
                        break;
                    case "is-statistics-rpc-enabled":
                        updateIsStatisticsRpcEnabled(Boolean.valueOf(sValue));
                        break;
                    case "barrier-interval-timeout-limit":
                        updateBarrierIntervalTimeoutLimit(Long.valueOf(sValue));
                        break;
                    case "barrier-count-limit":
                        updateBarrierCountLimit(Integer.valueOf(sValue));
                        break;
                    case "echo-reply-timeout":
                        updateEchoReplyTimeout(Long.valueOf(sValue));
                        break;
                    case "thread-pool-min-threads":
                        updateThreadPoolMinThreads(Integer.valueOf(sValue));
                        break;
                    case "thread-pool-max-threads":
                        updateThreadPoolMaxThreads(Integer.valueOf(sValue));
                        break;
                    case "thread-pool-timeout":
                        updateThreadPoolTimeout(Long.valueOf(sValue));
                        break;
                    case "enable-flow-removed-notification":
                        updateEnableFlowRemovedNotification(Boolean.valueOf(sValue));
                        break;
                    case "skip-table-features":
                        updateSkipTableFeatures(Boolean.valueOf(sValue));
                        break;
                    case "basic-timer-delay":
                        updateBasicTimerDelay(Long.valueOf(sValue));
                        break;
                    case "maximum-timer-delay":
                        updateMaximumTimerDelay(Long.valueOf(sValue));
                        break;
                    case "use-single-layer-serialization":
                        updateUseSingleLayerSerialization(Boolean.valueOf(sValue));
                        break;
                    default:
                        LOG.warn("Unsupported configuration property '{}={}'", key, sValue);
                }
            } catch (final Exception ex) {
                LOG.warn("Failed to read configuration property '{}={}', error: {}", key, sValue, ex);
            }
        });
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
}
