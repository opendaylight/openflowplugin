/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.DeviceContextChangeListener;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleConductor;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.RoleChangeListener;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ServiceChangeListener;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public final class LifecycleConductorImpl implements LifecycleConductor, RoleChangeListener, DeviceContextChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(LifecycleConductorImpl.class);
    private static final int TICKS_PER_WHEEL = 500;
    private static final long TICK_DURATION = 10; // 0.5 sec.

    private final HashedWheelTimer hashedWheelTimer = new HashedWheelTimer(TICK_DURATION, TimeUnit.MILLISECONDS, TICKS_PER_WHEEL);
    private DeviceManager deviceManager;
    private final MessageIntelligenceAgency messageIntelligenceAgency;
    private ConcurrentHashMap<NodeId, ServiceChangeListener> serviceChangeListeners = new ConcurrentHashMap<>();
    private StatisticsManager statisticsManager;

    public LifecycleConductorImpl(final MessageIntelligenceAgency messageIntelligenceAgency) {
        Preconditions.checkNotNull(messageIntelligenceAgency);
        this.messageIntelligenceAgency = messageIntelligenceAgency;
    }

    public void setSafelyDeviceManager(final DeviceManager deviceManager) {
        if (this.deviceManager == null) {
            this.deviceManager = deviceManager;
        }
    }

    public void setSafelyStatisticsManager(final StatisticsManager statisticsManager) {
        if (this.statisticsManager == null) {
            this.statisticsManager = statisticsManager;
        }
    }

    public void addOneTimeListenerWhenServicesChangesDone(final ServiceChangeListener manager, final NodeId nodeId){
        LOG.debug("Listener {} for service change for node {} registered.", manager, nodeId);
        serviceChangeListeners.put(nodeId, manager);
    }

    @VisibleForTesting
    void notifyServiceChangeListeners(final NodeId nodeId, final boolean success){
        if (serviceChangeListeners.size() == 0) {
            return;
        }
        LOG.debug("Notifying registered listeners for service change, no. of listeners {}", serviceChangeListeners.size());
        for (final Map.Entry<NodeId, ServiceChangeListener> nodeIdServiceChangeListenerEntry : serviceChangeListeners.entrySet()) {
            if (nodeIdServiceChangeListenerEntry.getKey().equals(nodeId)) {
                LOG.debug("Listener {} for service change for node {} was notified. Success was set on {}", nodeIdServiceChangeListenerEntry.getValue(), nodeId, success);
                nodeIdServiceChangeListenerEntry.getValue().servicesChangeDone(nodeId, success);
                serviceChangeListeners.remove(nodeId);
            }
        }
    }

    @Override
    public void roleInitializationDone(final NodeId nodeId, final boolean success) {
        if (!success) {
            LOG.warn("Initialization phase for node {} in role context was NOT successful, closing connection.", nodeId);
            closeConnection(nodeId);
        } else {
            LOG.info("initialization phase for node {} in role context was successful, continuing to next context.", nodeId);
        }
    }

    public void closeConnection(final NodeId nodeId) {
        LOG.debug("Close connection called for node {}", nodeId);
        final DeviceContext deviceContext = getDeviceContext(nodeId);
        if (null != deviceContext) {
            deviceContext.shutdownConnection();
        }
    }

    @Override
    public void roleChangeOnDevice(final NodeId nodeId, final boolean success, final OfpRole newRole, final boolean initializationPhase) {

        final DeviceContext deviceContext = getDeviceContext(nodeId);

        if (null == deviceContext) {
            LOG.warn("Something went wrong, device context for nodeId: {} doesn't exists");
            return;
        }
        if (!success) {
            LOG.warn("Role change to {} in role context for node {} was NOT successful, closing connection", newRole, nodeId);
            closeConnection(nodeId);
        } else {
            if (initializationPhase) {
                LOG.debug("Initialization phase skipping starting services.");
                return;
            }

            LOG.info("Role change to {} in role context for node {} was successful, starting/stopping services.", newRole, nodeId);

            if (OfpRole.BECOMEMASTER.equals(newRole)) {
                statisticsManager.startScheduling(deviceContext.getPrimaryConnectionContext().getDeviceInfo());
            } else {
                statisticsManager.stopScheduling(deviceContext.getPrimaryConnectionContext().getDeviceInfo());
            }

            final ListenableFuture<Void> onClusterRoleChange = deviceContext.onClusterRoleChange(null, newRole);
            Futures.addCallback(onClusterRoleChange, new FutureCallback<Void>() {
                @Override
                public void onSuccess(@Nullable final Void aVoid) {
                    LOG.info("Starting/Stopping services for node {} was successful", nodeId);
                    if (newRole.equals(OfpRole.BECOMESLAVE)) notifyServiceChangeListeners(nodeId, true);
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    LOG.warn("Starting/Stopping services for node {} was NOT successful, closing connection", nodeId);
                    closeConnection(nodeId);
                }
            });
        }
    }

    public MessageIntelligenceAgency getMessageIntelligenceAgency() {
        return messageIntelligenceAgency;
    }

    @Override
    public DeviceContext getDeviceContext(final NodeId nodeId){
         return deviceManager.getDeviceContextFromNodeId(nodeId);
    }

    public Short gainVersionSafely(final NodeId nodeId) {
        return (null != getDeviceContext(nodeId)) ? getDeviceContext(nodeId).getPrimaryConnectionContext().getFeatures().getVersion() : null;
    }

    public Timeout newTimeout(@Nonnull TimerTask task, long delay, @Nonnull TimeUnit unit) {
        return hashedWheelTimer.newTimeout(task, delay, unit);
    }

    public ConnectionContext.CONNECTION_STATE gainConnectionStateSafely(final NodeId nodeId){
        return (null != getDeviceContext(nodeId)) ? getDeviceContext(nodeId).getPrimaryConnectionContext().getConnectionState() : null;
    }

    public Long reserveXidForDeviceMessage(final NodeId nodeId){
        return null != getDeviceContext(nodeId) ? getDeviceContext(nodeId).reserveXidForDeviceMessage() : null;
    }

    @Override
    public void deviceStartInitializationDone(final NodeId nodeId, final boolean success) {
        if (!success) {
            LOG.warn("Initialization phase for node {} in device context was NOT successful, closing connection.", nodeId);
            closeConnection(nodeId);
        } else {
            LOG.info("initialization phase for node {} in device context was successful. Continuing to next context.", nodeId);
        }
    }

    @Override
    public void deviceInitializationDone(final NodeId nodeId, final boolean success) {
        if (!success) {
            LOG.warn("Initialization phase for node {} in device context was NOT successful, closing connection.", nodeId);
            closeConnection(nodeId);
        } else {
            LOG.info("initialization phase for node {} in device context was successful. All phases initialized OK.", nodeId);
        }
    }

    @VisibleForTesting
    public boolean isServiceChangeListenersEmpty() {
        return this.serviceChangeListeners.isEmpty();
    }

}
