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
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProvider;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleConductor;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.RoleChangeListener;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterProviderKeeper;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.OpenFlowPluginExtensionRegistratorProvider;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterManager;
import org.opendaylight.openflowplugin.impl.connection.ConnectionManagerImpl;
import org.opendaylight.openflowplugin.impl.device.DeviceManagerImpl;
import org.opendaylight.openflowplugin.impl.role.RoleManagerImpl;
import org.opendaylight.openflowplugin.impl.rpc.RpcManagerImpl;
import org.opendaylight.openflowplugin.impl.statistics.StatisticsManagerImpl;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.MessageIntelligenceAgencyImpl;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.MessageIntelligenceAgencyMXBean;
import org.opendaylight.openflowplugin.impl.util.TranslatorLibraryUtil;
import org.opendaylight.openflowplugin.openflow.md.core.ThreadPoolLoggingExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionConverterManagerImpl;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenFlowPluginProviderImpl implements OpenFlowPluginProvider, OpenFlowPluginExtensionRegistratorProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenFlowPluginProviderImpl.class);
    private static final MessageIntelligenceAgency messageIntelligenceAgency = new MessageIntelligenceAgencyImpl();

    private final int rpcRequestsQuota;
    private final long globalNotificationQuota;
    private long barrierInterval;
    private int barrierCountLimit;
    private long echoReplyTimeout;
    private DeviceManager deviceManager;
    private RoleManager roleManager;
    private RpcManager rpcManager;
    private RpcProviderRegistry rpcProviderRegistry;
    private StatisticsManager statisticsManager;
    private ConnectionManager connectionManager;
    private NotificationService notificationProviderService;
    private NotificationPublishService notificationPublishService;
    private EntityOwnershipService entityOwnershipService;

    private ExtensionConverterManager extensionConverterManager;

    private DataBroker dataBroker;
    private Collection<SwitchConnectionProvider> switchConnectionProviders;
    private boolean switchFeaturesMandatory = false;
    private boolean isStatisticsPollingOff = false;
    private boolean isStatisticsRpcEnabled;
    private boolean skipTableFeatures = true;

    private final LifecycleConductor conductor;
    private final ThreadPoolExecutor threadPool;

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
                new SynchronousQueue<Runnable>(), "ofppool");

        conductor = new LifecycleConductorImpl(messageIntelligenceAgency);
    }

    @Override
    public boolean isStatisticsPollingOff() {
        return isStatisticsPollingOff;
    }

    @Override
    public void setIsStatisticsPollingOff(final boolean isStatisticsPollingOff) {
        this.isStatisticsPollingOff = isStatisticsPollingOff;
    }

    private void startSwitchConnections() {
        final List<ListenableFuture<Boolean>> starterChain = new ArrayList<>(switchConnectionProviders.size());
        for (final SwitchConnectionProvider switchConnectionPrv : switchConnectionProviders) {
            switchConnectionPrv.setSwitchConnectionHandler(connectionManager);
            final ListenableFuture<Boolean> isOnlineFuture = switchConnectionPrv.startup();
            starterChain.add(isOnlineFuture);
        }

        final ListenableFuture<List<Boolean>> srvStarted = Futures.allAsList(starterChain);
        Futures.addCallback(srvStarted, new FutureCallback<List<Boolean>>() {
            @Override
            public void onSuccess(final List<Boolean> result) {
                LOG.info("All switchConnectionProviders are up and running ({}).",
                        result.size());
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
    public void setEntityOwnershipService(final EntityOwnershipService entityOwnershipService) {
        this.entityOwnershipService = entityOwnershipService;
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
    public void setSkipTableFeatures(final boolean skipTableFeatures) {
        this.skipTableFeatures = skipTableFeatures;
    }

    @Override
    public void initialize() {
        Preconditions.checkNotNull(dataBroker, "missing data broker");
        Preconditions.checkNotNull(rpcProviderRegistry, "missing RPC provider registry");
        Preconditions.checkNotNull(notificationProviderService, "missing notification provider service");

        extensionConverterManager = new ExtensionConverterManagerImpl();
        // TODO: copied from OpenFlowPluginProvider (Helium) misusesing the old way of distributing extension converters
        // TODO: rewrite later!
        OFSessionUtil.getSessionManager().setExtensionConverterProvider(extensionConverterManager);

        connectionManager = new ConnectionManagerImpl(echoReplyTimeout, threadPool);

        registerMXBean(messageIntelligenceAgency);

        deviceManager = new DeviceManagerImpl(dataBroker,
                globalNotificationQuota,
                switchFeaturesMandatory,
                barrierInterval,
                barrierCountLimit,
                conductor,
                skipTableFeatures);
        ((ExtensionConverterProviderKeeper) deviceManager).setExtensionConverterProvider(extensionConverterManager);

        conductor.setSafelyDeviceManager(deviceManager);

        roleManager = new RoleManagerImpl(entityOwnershipService, dataBroker, conductor);
        statisticsManager = new StatisticsManagerImpl(rpcProviderRegistry, isStatisticsPollingOff, conductor);
        rpcManager = new RpcManagerImpl(rpcProviderRegistry, rpcRequestsQuota, conductor);

        roleManager.addRoleChangeListener((RoleChangeListener) conductor);


        /* Initialization Phase ordering - OFP Device Context suite */
        // CM -> DM -> SM -> RPC -> Role -> DM
        connectionManager.setDeviceConnectedHandler(deviceManager);
        deviceManager.setDeviceInitializationPhaseHandler(statisticsManager);
        statisticsManager.setDeviceInitializationPhaseHandler(rpcManager);
        rpcManager.setDeviceInitializationPhaseHandler(roleManager);
        roleManager.setDeviceInitializationPhaseHandler(deviceManager);

        /* Termination Phase ordering - OFP Device Context suite */
        deviceManager.setDeviceTerminationPhaseHandler(rpcManager);
        rpcManager.setDeviceTerminationPhaseHandler(statisticsManager);
        statisticsManager.setDeviceTerminationPhaseHandler(roleManager);
        roleManager.setDeviceTerminationPhaseHandler(deviceManager);

        rpcManager.setStatisticsRpcEnabled(isStatisticsRpcEnabled);
        rpcManager.setNotificationPublishService(notificationPublishService);

        deviceManager.setNotificationPublishService(this.notificationPublishService);

        TranslatorLibraryUtil.setBasicTranslatorLibrary(deviceManager);
        deviceManager.initialize();

        startSwitchConnections();
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

        // TODO: needs to close org.opendaylight.openflowplugin.impl.role.OpenflowOwnershipListener after RoleContexts are down
        // TODO: must not be executed prior to all living RoleContexts have been closed (via closing living DeviceContexts)
        roleManager.close();

        // Manually shutdown all remaining running threads in pool
        threadPool.shutdown();
    }
}
