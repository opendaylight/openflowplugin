/*
 * Copyright (c) 2016, 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.applications.frm.DeviceMastershipManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceMastershipManagerImpl implements DeviceMastershipManager {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceMastershipManagerImpl.class);
    private List<NodeId> activeNodes = Collections.synchronizedList(new ArrayList<>());
    private final AutoCloseable mastershipChangeServiceRegistration;

    DeviceMastershipManagerImpl(final MastershipChangeServiceManager mastershipChangeServiceManager) {
        mastershipChangeServiceRegistration = mastershipChangeServiceManager.register(this);
    }

    @Override
    public boolean isDeviceMastered(final NodeId nodeId) {
        return activeNodes.contains(nodeId);
    }

    @Override
    public void close() throws Exception {
        mastershipChangeServiceRegistration.close();
    }

    @Override
    public void onBecomeOwner(@Nonnull final DeviceInfo deviceInfo) {
        activeNodes.add(deviceInfo.getNodeId());
        LOG.info("Registered FRM cluster singleton service for service id : {}", deviceInfo);
    }

    @Override
    public void onLoseOwnership(@Nonnull final DeviceInfo deviceInfo) {
        activeNodes.remove(deviceInfo.getNodeId());
        LOG.info("Unregistered FRM cluster singleton service for service id : {}", deviceInfo);
    }
}