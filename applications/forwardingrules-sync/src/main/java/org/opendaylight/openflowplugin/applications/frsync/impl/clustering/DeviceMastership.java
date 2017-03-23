/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl.clustering;

import com.google.common.util.concurrent.Futures;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeRegistration;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconciliationRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ClusterSingletonService} mastershipChangeRegistration per connected device.
 */
public class DeviceMastership implements MastershipChangeService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceMastership.class);
    private final NodeId nodeId;
    private final ReconciliationRegistry reconciliationRegistry;
    private MastershipChangeRegistration mastershipChangeRegistration;
    private boolean deviceMastered;

    public DeviceMastership(final NodeId nodeId,
                            final ReconciliationRegistry reconciliationRegistry,
                            final MastershipChangeServiceManager mastershipChangeServiceManager) {
        this.nodeId = nodeId;
        this.reconciliationRegistry = reconciliationRegistry;
        this.deviceMastered = false;
        mastershipChangeRegistration = mastershipChangeServiceManager.register(this);
    }

    @Override
    public Future<Void> onBecomeOwner(@Nonnull final DeviceInfo deviceInfo) {
        LOG.debug("FRS started for: {}", nodeId.getValue());
        deviceMastered = true;
        reconciliationRegistry.register(nodeId);
        return Futures.immediateFuture(null);
    }

    @Override
    public Future<Void> onLoseOwnership(@Nonnull final DeviceInfo deviceInfo) {
        LOG.debug("FRS stopped for: {}", nodeId.getValue());
        deviceMastered = false;
        reconciliationRegistry.unregisterIfRegistered(nodeId);
        return Futures.immediateFuture(null);
    }

    @Override
    public void close() {
        if (mastershipChangeRegistration != null) {
            try {
                mastershipChangeRegistration.close();
                mastershipChangeRegistration = null;
            } catch (Exception e) {
                LOG.error("FRS cluster service close fail: {} {}", nodeId.getValue(), e);
            }
        }
    }

    public boolean isDeviceMastered() {
        return deviceMastered;
    }

}
