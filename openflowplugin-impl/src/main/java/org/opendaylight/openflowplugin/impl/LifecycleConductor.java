/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.RoleChangeListener;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ServiceChangeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * This class/singleton is a binder between all managers
 */
public final class LifecycleConductor implements RoleChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(LifecycleConductor.class);
    private static final int TICKS_PER_WHEEL = 500;
    private static final long TICK_DURATION = 10; // 0.5 sec.

    private static LifecycleConductor INSTANCE = new LifecycleConductor();

    private final HashedWheelTimer hashedWheelTimer;
    private DeviceManager deviceManager = null;
    private ConcurrentHashMap<NodeId, ServiceChangeListener> serviceChangeListeners = new ConcurrentHashMap<>();

    private LifecycleConductor() {
        hashedWheelTimer = new HashedWheelTimer(TICK_DURATION, TimeUnit.MILLISECONDS, TICKS_PER_WHEEL);
    }

    public static LifecycleConductor getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new LifecycleConductor();
        }
        return INSTANCE;
    }

    public void addOneTimeListenerWhenServicesChangesDone(ServiceChangeListener manager, NodeId nodeId){
        LOG.debug("Listener {} for service change for node {} registered.", manager, nodeId);
        serviceChangeListeners.put(nodeId, manager);
    }

    private void notifyServiceChangeListeners(NodeId nodeId, boolean success){
        if (serviceChangeListeners.size() == 0) {
            return;
        }
        LOG.debug("Notifying registered listeners for service change, no. of listeners {}", serviceChangeListeners.size());
        for (Map.Entry<NodeId, ServiceChangeListener> nodeIdServiceChangeListenerEntry : serviceChangeListeners.entrySet()) {
            if (nodeIdServiceChangeListenerEntry.getKey().equals(nodeId)) {
                LOG.debug("Listener {} for service change for node {} was notified. Success was set on {}", nodeIdServiceChangeListenerEntry.getValue(), nodeId, success);
                nodeIdServiceChangeListenerEntry.getValue().servicesChangeDone(nodeId, success);
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

    private void closeConnection(NodeId nodeId) {
        LOG.debug("Close connection called for node {}", nodeId);
        if (null != getDeviceContext(nodeId)) {
            getDeviceContext(nodeId).shutdownConnection();
        }
    }

    @Override
    public void roleChangeOnDevice(final NodeId nodeId, final boolean success, final OfpRole newRole, final boolean initializationPhase) {
        if (!success) {
            LOG.warn("Role change to {} in role context for node {} was NOT successful, closing connection", newRole, nodeId);
            notifyServiceChangeListeners(nodeId, false);
            closeConnection(nodeId);
        } else {
            if (initializationPhase) {
                LOG.debug("Initialization phase skipping starting services.");
                return;
            }
            LOG.info("Role change to {} in role context for node {} was successful, staring/stopping services.", newRole, nodeId);

            //TODO: This is old way to check if statistics is running, remove after statistics changes implemented
            if (OfpRole.BECOMEMASTER.equals(newRole) && (getDeviceContext(nodeId) != null)) {
                getDeviceContext(nodeId).getDeviceState().setRole(OfpRole.BECOMEMASTER);
            } else {
                getDeviceContext(nodeId).getDeviceState().setRole(OfpRole.BECOMESLAVE);
            }

            final ListenableFuture<Void> onClusterRoleChange = getDeviceContext(nodeId).onClusterRoleChange(null, newRole);
            if (null != getDeviceContext(nodeId)) {
                Futures.addCallback(onClusterRoleChange, new FutureCallback<Void>() {
                    @Override
                    public void onSuccess(@Nullable Void aVoid) {
                        LOG.info("Starting/Stopping services for node {} was successful", nodeId);
                        notifyServiceChangeListeners(nodeId, true);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        LOG.warn("Starting/Stopping services for node {} was NOT successful, closing connection", nodeId);
                        notifyServiceChangeListeners(nodeId, false);
                        closeConnection(nodeId);
                    }
                });
            }
        }
    }

    public void setDeviceManager(final DeviceManager deviceManager){
        LOG.info("Device manager was set.");
        this.deviceManager = deviceManager;
    }

    private DeviceContext getDeviceContext(NodeId nodeId){
         return (null != deviceManager) ? deviceManager.getDeviceContextFromNodeId(nodeId) : null;
    }

    public Short gainVersionSafely(NodeId nodeId) {
        return (null != getDeviceContext(nodeId)) ? getDeviceContext(nodeId).getPrimaryConnectionContext().getFeatures().getVersion() : null;
    }

    public HashedWheelTimer getTimer() {
        return hashedWheelTimer;
    }

    public ConnectionContext.CONNECTION_STATE gainConnectionStateSafely(NodeId nodeId){
        return (null != getDeviceContext(nodeId)) ? getDeviceContext(nodeId).getPrimaryConnectionContext().getConnectionState() : null;
    }

    public Long reserveXidForDeviceMessage(NodeId nodeId){
        return null != getDeviceContext(nodeId) ? getDeviceContext(nodeId).reservedXidForDeviceMessage() : null;
    }
}
