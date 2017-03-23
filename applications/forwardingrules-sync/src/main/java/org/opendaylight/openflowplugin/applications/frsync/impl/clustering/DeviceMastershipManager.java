/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl.clustering;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.Futures;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconciliationRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for clustering service registrations of {@link DeviceMastership}.
 */
public class DeviceMastershipManager implements MastershipChangeService {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceMastershipManager.class);
    private final List<NodeId> deviceMasterships = Collections.synchronizedList(new ArrayList<>());
    private final ReconciliationRegistry reconciliationRegistry;

    public DeviceMastershipManager(final ReconciliationRegistry reconciliationRegistry) {
        this.reconciliationRegistry = reconciliationRegistry;
    }

    public boolean isDeviceMastered(final NodeId nodeId) {
        return deviceMasterships.contains(nodeId);
    }

    @VisibleForTesting
    List<NodeId> getDeviceMasterships() {
        return deviceMasterships;
    }

    @Override
    public Future<Void> onBecomeOwner(@Nonnull final DeviceInfo deviceInfo) {
        LOG.debug("FRS service registered and started for: {}", deviceInfo.getLOGValue());
        deviceMasterships.add(deviceInfo.getNodeId());
        reconciliationRegistry.register(deviceInfo.getNodeId());
        return Futures.immediateFuture(null);
    }

    @Override
    public Future<Void> onLoseOwnership(@Nonnull final DeviceInfo deviceInfo) {
        LOG.debug("FRS service unregistered and stopped for: {}", deviceInfo.getLOGValue());
        deviceMasterships.remove(deviceInfo.getNodeId());
        reconciliationRegistry.unregisterIfRegistered(deviceInfo.getNodeId());
        return Futures.immediateFuture(null);
    }

    @Override
    public void close() throws Exception {
        deviceMasterships.forEach(reconciliationRegistry::unregisterIfRegistered);
        deviceMasterships.clear();
    }

}
