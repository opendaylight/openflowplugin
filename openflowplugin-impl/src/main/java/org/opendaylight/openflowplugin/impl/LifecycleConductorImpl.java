/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.DeviceContextChangeListener;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleConductor;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.RoleChangeListener;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterProviderKeeper;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.util.MdSalRegistrationUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
final class LifecycleConductorImpl implements LifecycleConductor, DeviceContextChangeListener, ExtensionConverterProviderKeeper {

    private static final Logger LOG = LoggerFactory.getLogger(LifecycleConductorImpl.class);
    private static final int TICKS_PER_WHEEL = 500;
    private static final long TICK_DURATION = 10; // 0.5 sec.

    private final HashedWheelTimer hashedWheelTimer = new HashedWheelTimer(TICK_DURATION, TimeUnit.MILLISECONDS, TICKS_PER_WHEEL);
    private ExtensionConverterProvider extensionConverterProvider;
    private DeviceManager deviceManager;
    private StatisticsManager statisticsManager;
    private RpcManager rpcManager;
    private final MessageIntelligenceAgency messageIntelligenceAgency;
    private NotificationPublishService notificationPublishService;

    LifecycleConductorImpl(final MessageIntelligenceAgency messageIntelligenceAgency) {
        this.messageIntelligenceAgency = Preconditions.checkNotNull(messageIntelligenceAgency);
    }

    @Override
    public ExtensionConverterProvider getExtensionConverterProvider() {
        return extensionConverterProvider;
    }

    @Override
    public void setExtensionConverterProvider(ExtensionConverterProvider extensionConverterProvider) {
        this.extensionConverterProvider = extensionConverterProvider;
    }

    @Override
    public void setSafelyManager(final OFPManager manager){
        if (manager instanceof RpcManager) {
            if (rpcManager != null) {
                LOG.info("RPC manager {} is already defined in conductor. ", manager);
                return;
            }
            this.rpcManager = (RpcManager) manager;
        } else {
            if (manager instanceof StatisticsManager) {
                if (statisticsManager != null) {
                    LOG.info("Statistics manager {} is already defined in conductor. ", manager);
                    return;
                }
                this.statisticsManager = (StatisticsManager) manager;
            } else {
                if (manager instanceof DeviceManager) {
                    if (deviceManager != null) {
                        LOG.info("Device manager {} is already defined in conductor. ", manager);
                        return;
                    }
                    this.deviceManager = (DeviceManager) manager;
                }
            }
        }
    }

    public void closeConnection(final DeviceInfo deviceInfo) {
        LOG.debug("Close connection called for node {}", deviceInfo);
        final DeviceContext deviceContext = getDeviceContext(deviceInfo);
        if (null != deviceContext) {
            deviceContext.shutdownConnection();
        }
    }

    public MessageIntelligenceAgency getMessageIntelligenceAgency() {
        return messageIntelligenceAgency;
    }

    @Override
    public DeviceContext getDeviceContext(DeviceInfo deviceInfo){
         return deviceManager.gainContext(deviceInfo);
    }

    public Timeout newTimeout(@Nonnull TimerTask task, long delay, @Nonnull TimeUnit unit) {
        return hashedWheelTimer.newTimeout(task, delay, unit);
    }

    @Override
    public ConnectionContext.CONNECTION_STATE gainConnectionStateSafely(final DeviceInfo deviceInfo){
        return (null != getDeviceContext(deviceInfo)) ? getDeviceContext(deviceInfo).getPrimaryConnectionContext().getConnectionState() : null;
    }

    @Override
    public Long reserveXidForDeviceMessage(final DeviceInfo deviceInfo){
        return null != getDeviceContext(deviceInfo) ? getDeviceContext(deviceInfo).reserveXidForDeviceMessage() : null;
    }

    @Override
    public void deviceStartInitializationDone(final DeviceInfo deviceInfo, final boolean success) {
        if (!success) {
            LOG.warn("Initialization phase for node {} in device context was NOT successful, closing connection.", deviceInfo);
            closeConnection(deviceInfo);
        } else {
            LOG.info("initialization phase for node {} in device context was successful. Continuing to next context.", deviceInfo);
        }
    }

    @Override
    public void deviceInitializationDone(final DeviceInfo deviceInfo, final boolean success) {
        if (!success) {
            LOG.warn("Initialization phase for node {} in device context was NOT successful, closing connection.", deviceInfo);
            closeConnection(deviceInfo);
        } else {
            LOG.info("initialization phase for node {} in device context was successful. All phases initialized OK.", deviceInfo);
        }
    }

    @Override
    public NotificationPublishService getNotificationPublishService() {
        return notificationPublishService;
    }

    @Override
    public void setNotificationPublishService(NotificationPublishService notificationPublishService) {
        this.notificationPublishService = notificationPublishService;
    }
}
