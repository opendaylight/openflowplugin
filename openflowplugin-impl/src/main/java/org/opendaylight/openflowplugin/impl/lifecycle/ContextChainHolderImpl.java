/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.base.Verify;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionStatus;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChain;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainHolder;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.ContextChainState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextChainHolderImpl implements ContextChainHolder {

    private static final Logger LOG = LoggerFactory.getLogger(ContextChainHolderImpl.class);
    private static final String NOT_ALL_MANAGER_WERE_SET = "Not all manager were set.";
    private static final String MANAGER_WAS_SET = " manager was set";
    private static final String CONTEXT_CREATED_FOR_CONNECTION = " context created for connection: {}";
    private static final String SINGLETON_SERVICE_PROVIDER_WAS_NOT_SET_YET
            = "Singleton service provider was not set yet.";
    private static final long DEFAULT_TTL_STEP = 1000L;
    private static final long DEFAULT_TTL_BEFORE_DROP = 5000L;

    private DeviceManager deviceManager;
    private RpcManager rpcManager;
    private StatisticsManager statisticsManager;
    private volatile ConcurrentHashMap<DeviceInfo, ContextChain> contextChainMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<DeviceInfo, ContextChain> sleepingChains = new ConcurrentHashMap<>();
    private ConcurrentHashMap<DeviceInfo, Long> timeToLive = new ConcurrentHashMap<>();
    private ClusterSingletonServiceProvider singletonServicesProvider;
    private boolean timerIsRunning;
    private final HashedWheelTimer timer;
    private Long ttlBeforeDrop;
    private Long ttlStep;
    private Boolean neverDropChain;

    public ContextChainHolderImpl(final HashedWheelTimer timer) {
        this.timerIsRunning = false;
        this.timer = timer;
        this.ttlBeforeDrop = DEFAULT_TTL_BEFORE_DROP;
        this.ttlStep = DEFAULT_TTL_STEP;
    }

    @Override
    public <T extends OFPManager> void addManager(final T manager) {
        if (Objects.isNull(deviceManager) && manager instanceof DeviceManager) {
            LOG.info("Device" + MANAGER_WAS_SET);
            deviceManager = (DeviceManager) manager;
        } else if (Objects.isNull(rpcManager) && manager instanceof RpcManager) {
            LOG.info("RPC" + MANAGER_WAS_SET);
            rpcManager = (RpcManager) manager;
        } else if (Objects.isNull(statisticsManager) && manager instanceof StatisticsManager) {
            LOG.info("Statistics" + MANAGER_WAS_SET);
            statisticsManager = (StatisticsManager) manager;
        }
    }

    @Override
    public ContextChain createContextChain(final ConnectionContext connectionContext) {

        final DeviceInfo deviceInfo = connectionContext.getDeviceInfo();
        final String deviceInfoLOGValue = deviceInfo.getLOGValue();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating a new chain" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfoLOGValue);
        }

        final ContextChain contextChain = new ContextChainImpl();
        final LifecycleService lifecycleService = new LifecycleServiceImpl(this);
        lifecycleService.registerDeviceRemovedHandler(deviceManager);
        lifecycleService.registerDeviceRemovedHandler(rpcManager);
        lifecycleService.registerDeviceRemovedHandler(statisticsManager);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Lifecycle services" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfoLOGValue);
        }

        final DeviceContext deviceContext = deviceManager.createContext(connectionContext);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Device" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfoLOGValue);
        }

        final RpcContext rpcContext = rpcManager.createContext(connectionContext.getDeviceInfo(), deviceContext);

        if (LOG.isDebugEnabled()) {
            LOG.debug("RPC" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfoLOGValue);
        }

        final StatisticsContext statisticsContext
                = statisticsManager.createContext(deviceContext);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Statistics" + CONTEXT_CREATED_FOR_CONNECTION, deviceInfoLOGValue);
        }

        deviceContext.setLifecycleInitializationPhaseHandler(statisticsContext);
        statisticsContext.setLifecycleInitializationPhaseHandler(rpcContext);
        statisticsContext.setInitialSubmitHandler(deviceContext);

        contextChain.addLifecycleService(lifecycleService);
        contextChain.addContext(deviceContext);
        contextChain.addContext(rpcContext);
        contextChain.addContext(statisticsContext);
        contextChain.makeDeviceSlave();
        deviceContext.onPublished();

        return contextChain;

    }

    @Override
    public void destroyContextChain(final DeviceInfo deviceInfo) {
        removeFromSleepingChainsMap(deviceInfo);
        ContextChain chain = contextChainMap.get(deviceInfo);
        if (Objects.nonNull(chain)) {
            chain.close();
            contextChainMap.remove(deviceInfo);
            try {
                LOG.info("Removing device: {} from DS", deviceInfo.getLOGValue());
                deviceManager.removeDeviceFromOperationalDS(deviceInfo).checkedGet(5L, TimeUnit.SECONDS);
            } catch (TimeoutException | TransactionCommitFailedException e) {
                LOG.warn("Not able to remove device {} from DS", deviceInfo.getLOGValue());
            }
        }
    }

    @Override
    public void pairConnection(final ConnectionContext connectionContext) {
        DeviceInfo deviceInfo = connectionContext.getDeviceInfo();
        ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (Objects.nonNull(contextChain)) {
            contextChain.changePrimaryConnection(connectionContext);
            contextChain.makeDeviceSlave();
        }
    }

    @Override
    public ConnectionStatus deviceConnected(final ConnectionContext connectionContext) throws Exception {

        Verify.verify(this.checkAllManagers(), NOT_ALL_MANAGER_WERE_SET);
        Verify.verifyNotNull(this.singletonServicesProvider, SINGLETON_SERVICE_PROVIDER_WAS_NOT_SET_YET);

        DeviceInfo deviceInfo = connectionContext.getDeviceInfo();

        LOG.info("Device {} connected.", deviceInfo.getLOGValue());
        ContextChain chain = contextChainMap.get(deviceInfo);

        if (Objects.isNull(chain)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No context chain found for device: {}, creating new.", deviceInfo.getLOGValue());
            }
            contextChainMap.put(deviceInfo, createContextChain(connectionContext));
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Found context chain for device: {}, pairing.", deviceInfo.getLOGValue());
            }
            this.pairConnection(connectionContext);
        }

        return ConnectionStatus.MAY_CONTINUE;
    }

    @Override
    public void addSingletonServicesProvider(final ClusterSingletonServiceProvider singletonServicesProvider) {
        this.singletonServicesProvider = singletonServicesProvider;
    }

    @Override
    public void onNotAbleToStartMastership(final DeviceInfo deviceInfo) {
        ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (Objects.nonNull(contextChain)) {
            LOG.warn("Not able to set MASTER role on device {}", deviceInfo.getLOGValue());
            if (contextChain.getContextChainState().equals(ContextChainState.INITIALIZED)) {
                contextChain.closePrimaryConnection();
            } else {
                contextChain.sleepTheChainAndDropConnection();
                addToSleepingChainsMap(deviceInfo, contextChain);
            }
        }
    }

    @Override
    public void onMasterRoleAcquired(final DeviceInfo deviceInfo) {
        ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (Objects.nonNull(contextChain)) {
            if (contextChain.getContextChainState().equals(ContextChainState.WORKINGMASTER)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Device {} already working as MASTER no changes need to be done.",
                            deviceInfo.getLOGValue());
                }
            } else {
                if (contextChain.getContextChainState().equals(ContextChainState.INITIALIZED)) {
                    LOG.info("Device {} has not finish initial gathering yet.",
                            deviceInfo.getLOGValue());
                }
                Futures.addCallback(contextChain.startChain(),
                        new StartStopChainCallback(deviceInfo, false));
            }
        }
    }

    @Override
    public void onSlaveRoleAcquired(final DeviceInfo deviceInfo) {
        ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (Objects.nonNull(contextChain)) {
            contextChain.registerServices(this.singletonServicesProvider);
        }
    }

    @Override
    public void onSlaveRoleNotAcquired(final DeviceInfo deviceInfo) {
        ContextChain contextChain = contextChainMap.get(deviceInfo);
        if (Objects.nonNull(contextChain)) {
            contextChain.sleepTheChainAndDropConnection();
            addToSleepingChainsMap(deviceInfo, contextChain);
        }
    }

    @Override
    public void onDeviceDisconnected(final ConnectionContext connectionContext) {

        final DeviceInfo deviceInfo = connectionContext.getDeviceInfo();

        if (Objects.isNull(deviceInfo)) {
            LOG.info("Non existing device info. Cannot close context chain.");
        } else {
            LOG.info("Device {} disconnected.", deviceInfo.getLOGValue());
            ContextChain chain = contextChainMap.get(deviceInfo);
            if (Objects.isNull(chain)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("There was no context chain created yet for the disconnected device {}",
                            deviceInfo.getLOGValue());
                }
            } else {
                Futures.addCallback(chain.connectionDropped(),
                        new StartStopChainCallback(deviceInfo, true));
            }
        }
    }

    @Override
    public void setTtlBeforeDrop(final Long ttlBeforeDrop) {
        this.ttlBeforeDrop = ttlBeforeDrop;
    }

    @Override
    public void setTtlStep(final Long ttlStep) {
        this.ttlStep = ttlStep;
    }

    @Override
    public void setNeverDropContextChain(final Boolean neverDropChain) {
        this.neverDropChain = neverDropChain;
    }

    private void addToSleepingChainsMap(@Nonnull final DeviceInfo deviceInfo, final ContextChain contextChain) {
        sleepingChains.put(deviceInfo, contextChain);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Put context chain on mattress to sleep for device {}", deviceInfo.getLOGValue());
        }
        if (!this.neverDropChain) {
            timeToLive.put(deviceInfo, this.ttlBeforeDrop);
            if (!this.timerIsRunning) {
                startTimer();
            }
        }
    }

    private void removeFromSleepingChainsMap(@Nonnull final DeviceInfo deviceInfo) {
        sleepingChains.remove(deviceInfo);
        if (!this.neverDropChain) {
            timeToLive.remove(deviceInfo);
            if (sleepingChains.isEmpty() && this.timerIsRunning) {
                stopTimer();
            }
        }
    }

    private void startTimer() {
        this.timerIsRunning = true;
        if (LOG.isDebugEnabled()) {
            LOG.debug("There is at least one context chains sleeping, starting timer.");
        }
        timer.newTimeout(new SleepingChainsTimerTask(), this.ttlStep, TimeUnit.MILLISECONDS);
    }

    private void stopTimer() {
        this.timerIsRunning = false;
        if (LOG.isDebugEnabled()) {
            LOG.debug("There are no context chains sleeping, stopping timer.");
        }
    }

    private void timerTick() {
        if (sleepingChains.isEmpty()) {
            this.stopTimer();
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Context chain holder timer tick. There are {} context chains sleeping.",
                        sleepingChains.size());
            }
            if (timeToLive.isEmpty()) {
                LOG.warn("TTL map is empty but not sleeping chains map. Providing clean up.");
                sleepingChains.clear();
            }
            final ArrayList<DeviceInfo> deviceInfos = new ArrayList<>();
            for (Map.Entry<DeviceInfo, Long> deviceInfoLongEntry : timeToLive.entrySet()) {
                Long newValue = deviceInfoLongEntry.getValue() - this.ttlStep;
                deviceInfoLongEntry.setValue(newValue);
                DeviceInfo deviceInfo = deviceInfoLongEntry.getKey();
                ContextChain chain = sleepingChains.get(deviceInfo);
                if (Objects.isNull(chain)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("There is no sleeping context chain for device {}", deviceInfo.getLOGValue());
                    }
                    deviceInfos.add(deviceInfo);
                    continue;
                }
                if (!ContextChainState.SLEEPING.equals(chain.getContextChainState())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("There is timer registered for device: {} " +
                                        "but device is in state: {} Removing from timer.",
                                deviceInfo.getLOGValue(),
                                chain.getContextChainState().getName());
                    }
                    deviceInfos.add(deviceInfo);
                }
                if (newValue <= 0) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Dear device: {} your time to wake up is up. Its time to destroy you.",
                                deviceInfo.getLOGValue());
                    }
                    destroyContextChain(deviceInfo);
                }
            }

            deviceInfos.forEach(deviceInfo -> timeToLive.remove(deviceInfo));

            if (!timeToLive.isEmpty()) {
                timer.newTimeout(new SleepingChainsTimerTask(), this.ttlStep, TimeUnit.MILLISECONDS);
            } else {
                this.stopTimer();
            }
        }
    }

    private boolean checkAllManagers() {
        return Objects.nonNull(deviceManager) && Objects.nonNull(rpcManager) && Objects.nonNull(statisticsManager);
    }

    private class StartStopChainCallback implements FutureCallback<Void> {

        private final String deviceInfoString;
        private final String stopString;
        private final String stoppedString;
        private final boolean stop;
        private final DeviceInfo deviceInfo;

        StartStopChainCallback(final DeviceInfo deviceInfo, final boolean stop) {

            this.deviceInfoString = Objects.nonNull(deviceInfo) ? deviceInfo.getLOGValue() : "null";
            this.stopString = stop ? "stop" : "start";
            this.stoppedString = stop ? "stopped" : "started";
            this.stop = stop;
            this.deviceInfo = deviceInfo;
        }

        @Override
        public void onSuccess(@Nullable Void nothing) {
            LOG.info("Context chain for device {} successfully {}", deviceInfoString, stoppedString);
            if (this.stop && Objects.nonNull(deviceInfo)) {
                addToSleepingChainsMap(deviceInfo, contextChainMap.get(deviceInfo));
            }
        }

        @Override
        public void onFailure(@Nonnull final Throwable throwable) {
            LOG.warn("Not able to {} the context chain for device {}", stopString, deviceInfoString);
            if (this.stop && Objects.nonNull(deviceInfo)) {
                addToSleepingChainsMap(deviceInfo, contextChainMap.get(deviceInfo));
            }
        }
    }

    private class SleepingChainsTimerTask implements TimerTask {

        @Override
        public void run(Timeout timeout) throws Exception {
            timerTick();
        }

    }

}

