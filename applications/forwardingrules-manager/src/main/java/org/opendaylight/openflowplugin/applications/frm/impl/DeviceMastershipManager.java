/**
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for clustering service registrations of {@link DeviceMastership}.
 */
public class DeviceMastershipManager {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceMastershipManager.class);
    private final ClusterSingletonServiceProvider clusterSingletonService;
    private final ConcurrentHashMap<NodeId, DeviceMastership> deviceMasterships = new ConcurrentHashMap();

    public DeviceMastershipManager(final ClusterSingletonServiceProvider clusterSingletonService) {
        this.clusterSingletonService = clusterSingletonService;
    }

    public void onDeviceConnected(final NodeId nodeId) {
        final DeviceMastership mastership = new DeviceMastership(nodeId);
        final ClusterSingletonServiceRegistration registration = clusterSingletonService.registerClusterSingletonService(mastership);
        mastership.setClusterSingletonServiceRegistration(registration);
        deviceMasterships.put(nodeId, mastership);
        LOG.debug("FRS service registered for: {}", nodeId.getValue());
    }


    public void onDeviceDisconnected(final NodeId nodeId) {
        final DeviceMastership mastership = deviceMasterships.remove(nodeId);
        final ClusterSingletonServiceRegistration registration = mastership.getClusterSingletonServiceRegistration();
        if (registration != null) {
            try {
                registration.close();
            } catch (Exception e) {
                LOG.error("FRS cluster service close fail: {}", nodeId.getValue());
            }
        }
        LOG.debug("FRS service unregistered for: {}", nodeId.getValue());
    }

    public boolean isDeviceMastered(final NodeId nodeId) {
        return deviceMasterships.get(nodeId) != null && deviceMasterships.get(nodeId).isDeviceMastered();
    }

}
