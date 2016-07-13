/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconciliationRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class DeviceManager {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceManager.class);
    // TODO provider
    private final ConcurrentHashMap<NodeId, DeviceContext> deviceContexts = new ConcurrentHashMap();
    private final ReconciliationRegistry reconciliationRegistry;

    public DeviceManager(final ReconciliationRegistry reconciliationRegistry) {
        // TODO set provider
        this.reconciliationRegistry = reconciliationRegistry;
    }

    public void onDeviceConnected(final NodeId nodeId) {
        final DeviceContext deviceContext = new DeviceContext(nodeId, reconciliationRegistry);
        deviceContexts.put(nodeId, deviceContext);
        // TODO provider.register(deviceContext);
        LOG.debug("FRS service registered for: {}", nodeId.getValue());
    }

    public void onDeviceDisconnected(final NodeId nodeId) {
        reconciliationRegistry.unregisterIfRegistered(nodeId);
        DeviceContext context = deviceContexts.remove(nodeId);
//        context.close();
        LOG.debug("FRS service unregister for: {}", nodeId.getValue());
    }

    public boolean isDeviceMastered(final NodeId nodeId) {
        return deviceContexts.get(nodeId) != null && deviceContexts.get(nodeId).isDeviceMastered();
    }

    public ReconciliationRegistry getReconciliationRegistry() {
        return reconciliationRegistry;
    }

    @VisibleForTesting
    ConcurrentMap<NodeId, DeviceContext> getDeviceContexts() {
        return deviceContexts;
    }
}
