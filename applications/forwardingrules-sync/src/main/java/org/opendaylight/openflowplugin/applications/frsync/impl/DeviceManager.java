/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.annotations.VisibleForTesting;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class DeviceManager {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceManager.class);
    // TODO provider
    private final ConcurrentMap<NodeId, DeviceContext> deviceContexts = new ConcurrentHashMap();
    private final Map<NodeId, Date> reconcileRegistrations = new ConcurrentHashMap<>();

    public DeviceManager() {
        // TODO set provider
    }

    public void onDeviceConnected(final NodeId nodeId) {
        final DeviceContext deviceContext = new DeviceContext(nodeId, this);
        deviceContexts.put(nodeId, deviceContext);
        // TODO provider.register(deviceContext);
        LOG.debug("FRS service registered for: {}", nodeId.getValue());
    }

    public void onDeviceDisconnected(final NodeId nodeId) {
        unregisterReconcileIfRegistered(nodeId);
        DeviceContext context = deviceContexts.remove(nodeId);
//        context.close();
        LOG.debug("FRS service unregister for: {}", nodeId.getValue());
    }

    public boolean isDeviceMastered(final NodeId nodeId) {
        if (deviceContexts.get(nodeId) == null) {
            return false;
        } else {
            return deviceContexts.get(nodeId).isDeviceMastered();
        }
    }

    public Date registerReconcile(NodeId nodeId) {
        Date timestamp = new Date();
        reconcileRegistrations.put(nodeId, timestamp);
        LOG.debug("Registered for next consistent operational: {}", nodeId.getValue());
        return timestamp;
    }

    public Date unregisterReconcileIfRegistered(NodeId nodeId) {
        Date timestamp = reconcileRegistrations.remove(nodeId);
        if (timestamp != null) {
            LOG.debug("Unregistered for next consistent operational: {}", nodeId.getValue());
        }
        return timestamp;
    }

    public boolean isRegisteredForReconcile(NodeId nodeId) {
        return reconcileRegistrations.get(nodeId) != null;
    }

    public Date getReconcileRegistrationTimestamp(NodeId nodeId) {
        return reconcileRegistrations.get(nodeId);
    }

    @VisibleForTesting
    public ConcurrentMap<NodeId, DeviceContext> getDeviceContexts() {
        return deviceContexts;
    }
}
