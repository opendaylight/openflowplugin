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
import io.netty.util.HashedWheelTimer;

public class OpenFlowPluginProviderImpl implements OpenFlowPluginProvider, OpenFlowPluginExtensionRegistratorProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenFlowPluginProviderImpl.class);
    private static final MessageIntelligenceAgency messageIntelligenceAgency = new MessageIntelligenceAgencyImpl();
    private static final int TICKS_PER_WHEEL = 500;
    // 0.5 sec.
    private static final long TICK_DURATION = 10;
    private static final Integer DEFAULT_BARRIER_COUNT = 25600;
    private static final Long DEFAULT_ECHO_TIMEOUT = 2000L;
    private static final Long DEFAULT_BARRIER_TIMEOUT = 500L;
    private static final String POOL_NAME = "ofppool";

    private final HashedWheelTimer hashedWheelTimer = new HashedWheelTimer(TICK_DURATION, TimeUnit.MILLISECONDS, TICKS_PER_WHEEL);

    private final int rpcRequestsQuota;
    private final long globalNotificationQuota;
    private final ConvertorManager convertorManager;
    private final ContextChainHolder contextChainHolder;
    private long barrierInterval;
    private int barrierCountLimit;
    private long echoReplyTimeout;
    private DeviceManager deviceManager;
    private RpcManager rpcManager;
    private RpcProviderRegistry rpcProviderRegistry;
    private StatisticsManager statisticsManager;
    private ConnectionManager connectionManager;
    private NotificationService notificationProviderService;
    private NotificationPublishService notificationPublishService;
    private ExtensionConverterManager extensionConverterManager;
    private DataBroker dataBroker;
    private Collection<SwitchConnectionProvider> switchConnectionProviders;
    private boolean switchFeaturesMandatory = false;
    private boolean isStatisticsPollingOn = true;
    private boolean isStatisticsRpcEnabled;
    private boolean isFlowRemovedNotificationOn = true;
    private boolean skipTableFeatures = true;
    private long basicTimerDelay;
    private long maximumTimerDelay;
    private boolean useSingleLayerSerialization = false;
    private final DeviceInitializerProvider deviceInitializerProvider;

    private final ThreadPoolExecutor threadPool;
    private ClusterSingletonServiceProvider singletonServicesProvider;

    public OpenFlowPluginProviderImpl(final long rpcRequestsQuota,
                                      final long globalNotificationQuota,
                                      final int threadPoolMinThreads,
                                      final int threadPoolMaxThreads,
                                      final long threadPoolTimeout) {
        Preconditions.checkArgument(rpcRequestsQuota > 0 && rpcRequestsQuota <= Integer.MAX_VALUE, "rpcRequestQuota has to be in range <1,%s>", Integer.MAX_VALUE);
        this.rpcRequestsQuota = (int) rpcRequestsQuota;
        this.globalNotificationQuota = Preconditions.checkNotNull(globalNotificationQuota);

        // Creates a thread pool that creates new threads as needed, but will reuse previously
        // constructed threads when they are available.
        // Threads that have not been used for x seconds are terminated and removed from the cache.
        threadPool = new ThreadPoolLoggingExecutor(
                Preconditions.checkNotNull(threadPoolMinThreads),
                Preconditions.checkNotNull(threadPoolMaxThreads),
                Preconditions.checkNotNull(threadPoolTimeout), TimeUnit.SECONDS,
                new SynchronousQueue<>(), POOL_NAME);
        deviceInitializerProvider = DeviceInitializerProviderFactory.createDefaultProvider();		
        convertorManager = ConvertorManagerFactory.createDefaultManager();
        contextChainHolder = new ContextChainHolderImpl(hashedWheelTimer);
    }

    @Override
    public boolean isStatisticsPollingOn() {
        return isStatisticsPollingOn;
    }

    @Override
    public void setStatisticsPollingOn(final boolean isStatisticsPollingOn) {
        this.isStatisticsPollingOn = isStatisticsPollingOn;
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
    public boolean isSwitchFeaturesMandatory() {
        return switchFeaturesMandatory;
    }

    @Override
    public void setBarrierCountLimit(final int barrierCountLimit) {
        this.barrierCountLimit = barrierCountLimit;
    }

    @Override
    public void setBarrierInterval(final long barrierTimeoutLimit) {
        this.barrierInterval = barrierTimeoutLimit;
    }

    @Override
    public void setEchoReplyTimeout(final long echoReplyTimeout) {
        this.echoReplyTimeout = echoReplyTimeout;
    }

    @Override
    public void setFlowRemovedNotification(boolean isFlowRemovedNotificationOn) {
        this.isFlowRemovedNotificationOn = isFlowRemovedNotificationOn;
    }

    @Override
    public void setClusteringSingletonServicesProvider(ClusterSingletonServiceProvider singletonServicesProvider) {
        this.singletonServicesProvider = singletonServicesProvider;
    }

    @Override
    public void setSkipTableFeatures(final boolean skipTableFeatures){
        this.skipTableFeatures = skipTableFeatures;
    }

    @Override
    public void setBasicTimerDelay(long basicTimerDelay) {
        this.basicTimerDelay = basicTimerDelay;
    }

    @Override
    public void setMaximumTimerDelay(long maximumTimerDelay) {
        this.maximumTimerDelay = maximumTimerDelay;
    }

    @Override
    public void setSwitchFeaturesMandatory(final boolean switchFeaturesMandatory) {
        this.switchFeaturesMandatory = switchFeaturesMandatory;
    }

    public static MessageIntelligenceAgency getMessageIntelligenceAgency() {
        return OpenFlowPluginProviderImpl.messageIntelligenceAgency;
    }

    @Override
    public void setSwitchConnectionProviders(final Collection<SwitchConnectionProvider> switchConnectionProviders) {
        this.switchConnectionProviders = switchConnectionProviders;
    }

    @Override
    public void setDataBroker(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public void setRpcProviderRegistry(final RpcProviderRegistry rpcProviderRegistry) {
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

    @Override
    public void initialize() {
        Preconditions.checkNotNull(dataBroker, "missing data broker");
        Preconditions.checkNotNull(rpcProviderRegistry, "missing RPC provider registry");
        Preconditions.checkNotNull(notificationProviderService, "missing notification provider service");
        Preconditions.checkNotNull(singletonServicesProvider, "missing singleton services provider");

        extensionConverterManager = new ExtensionConverterManagerImpl();
        // TODO: copied from OpenFlowPluginProvider (Helium) misusesing the old way of distributing extension converters
        // TODO: rewrite later!
        OFSessionUtil.getSessionManager().setExtensionConverterProvider(extensionConverterManager);

        connectionManager = new ConnectionManagerImpl(echoReplyTimeout, threadPool);

        registerMXBean(messageIntelligenceAgency);

        contextChainHolder.addSingletonServicesProvider(singletonServicesProvider);

        deviceManager = new DeviceManagerImpl(dataBroker,
                globalNotificationQuota,
                switchFeaturesMandatory,
                barrierInterval,
                barrierCountLimit,
                getMessageIntelligenceAgency(),
                isFlowRemovedNotificationOn,
                singletonServicesProvider,
                notificationPublishService,
                hashedWheelTimer,
                convertorManager,
                skipTableFeatures,
                useSingleLayerSerialization,
                deviceInitializerProvider);

        ((ExtensionConverterProviderKeeper) deviceManager).setExtensionConverterProvider(extensionConverterManager);

        rpcManager = new RpcManagerImpl(rpcProviderRegistry, rpcRequestsQuota, extensionConverterManager, convertorManager, notificationPublishService);
        statisticsManager = new StatisticsManagerImpl(rpcProviderRegistry, isStatisticsPollingOn, hashedWheelTimer,
                convertorManager,basicTimerDelay,maximumTimerDelay);

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
    }

    @Override
    public void update(Map<String,Object> props) {
        LOG.debug("Update managed properties = {}", props.toString());

        final boolean containsUseSingleLayer = props.containsKey("use-single-layer-serialization");

        if (containsUseSingleLayer) {
            final Boolean useSingleLayer = Boolean.valueOf(props.get("use-single-layer-serialization").toString());

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

        if(deviceManager != null) {
            if (containsUseSingleLayer) {
                deviceManager.setUseSingleLayerSerialization(Boolean.valueOf(props.get("use-single-layer-serialization").toString()));
            }

            if (props.containsKey("notification-flow-removed-off")) {
                deviceManager.setFlowRemovedNotificationOn(Boolean.valueOf(props.get("enable-flow-removed-notification").toString()));
            }
            if (props.containsKey("skip-table-features")) {
                deviceManager.setSkipTableFeatures(Boolean.valueOf(props.get("skip-table-features").toString()));
            }
            if (props.containsKey("barrier-count-limit")) {
                try {
                    deviceManager.setBarrierCountLimit(Integer.valueOf(props.get("barrier-count-limit").toString()));
                } catch (NumberFormatException ex) {
                    deviceManager.setBarrierCountLimit(DEFAULT_BARRIER_COUNT);
                }
            }
            if (props.containsKey("barrier-interval-timeout-limit")){
                try {
                    deviceManager.setBarrierInterval(Long.valueOf(props.get("barrier-interval-timeout-limit").toString()));
                } catch (NumberFormatException ex) {
                    deviceManager.setBarrierInterval(DEFAULT_BARRIER_TIMEOUT);
                }
            }
        }

        if(rpcManager != null && props.containsKey("is-statistics-rpc-enabled")){
            rpcManager.setStatisticsRpcEnabled(Boolean.valueOf((props.get("is-statistics-rpc-enabled").toString())));
        }

        if (connectionManager != null && props.containsKey("echo-reply-timeout") ){
            try {
                connectionManager.setEchoReplyTimeout(Long.valueOf(props.get("echo-reply-timeout").toString()));
            }catch (NumberFormatException ex){
                connectionManager.setEchoReplyTimeout(DEFAULT_ECHO_TIMEOUT);
            }
        }

        if(statisticsManager != null && props.containsKey("is-statistics-polling-on")){
            statisticsManager.setIsStatisticsPollingOn(Boolean.valueOf(props.get("is-statistics-polling-on").toString()));
        }

        if(statisticsManager != null && props.containsKey("basic-timer-delay")){
            statisticsManager.setBasicTimerDelay(Long.valueOf(props.get("basic-timer-delay").toString()));
        }

        if(statisticsManager != null && props.containsKey("maximum-timer-delay")){
            statisticsManager.setMaximumTimerDelay(Long.valueOf(props.get("maximum-timer-delay").toString()));
        }
        if (props.containsKey("ttl-before-drop")) {
            contextChainHolder.setTtlBeforeDrop(Long.valueOf(props.get("ttl-before-drop").toString()));
        }

        if (props.containsKey("ttl-step")) {
            contextChainHolder.setTtlStep(Long.valueOf(props.get("ttl-step").toString()));
        }

        if (props.containsKey("never-drop-contexts-on")) {
            contextChainHolder.setNeverDropContextChain(Boolean.valueOf(props.get("never-drop-contexts-on").toString()));
        }

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
    public void setNotificationProviderService(final NotificationService notificationProviderService) {
        this.notificationProviderService = notificationProviderService;
    }

    @Override
    public void setNotificationPublishService(final NotificationPublishService notificationPublishProviderService) {
        this.notificationPublishService = notificationPublishProviderService;
    }

    @Override
    public ExtensionConverterRegistrator getExtensionConverterRegistrator() {
        return extensionConverterManager;
    }

    @Override
    public void setIsStatisticsRpcEnabled(final boolean isStatisticsRpcEnabled) {
        this.isStatisticsRpcEnabled = isStatisticsRpcEnabled;
    }

    @Override
    public void close() throws Exception {
        //TODO: consider wrapping each manager into try-catch
        deviceManager.close();
        rpcManager.close();
        statisticsManager.close();

        // Manually shutdown all remaining running threads in pool
        threadPool.shutdown();
    }

    @Override
    public void setIsUseSingleLayerSerialization(Boolean useSingleLayerSerialization) {
        this.useSingleLayerSerialization = useSingleLayerSerialization;
    }

    @Override
    public void updateTtlBeforeDropInContextChainHolder(final Long ttlBeforeDrop) {
        this.contextChainHolder.setTtlBeforeDrop(ttlBeforeDrop);
    }

    @Override
    public void updateTtlStepInContextChainHolder(final Long ttlStep) {
        this.contextChainHolder.setTtlStep(ttlStep);
    }

    @Override
    public void updateNeverDropContextChains(final Boolean neverDropChain) {
        this.contextChainHolder.setNeverDropContextChain(neverDropChain);
    }

}