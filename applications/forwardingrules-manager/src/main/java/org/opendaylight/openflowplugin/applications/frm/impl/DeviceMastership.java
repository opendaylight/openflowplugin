/**
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service (per device) for registration in singleton provider.
 */
public class DeviceMastership implements ClusterSingletonService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceMastership.class);
    private final NodeId nodeId;
    private final ServiceGroupIdentifier identifier;
    private final ClusterSingletonServiceRegistration clusterSingletonServiceRegistration;
    private boolean deviceMastered;

    public DeviceMastership(final NodeId nodeId, final ClusterSingletonServiceProvider clusterSingletonService) {
        this.nodeId = nodeId;
        this.identifier = ServiceGroupIdentifier.create(nodeId.getValue());
        this.deviceMastered = false;
        clusterSingletonServiceRegistration = clusterSingletonService.registerClusterSingletonService(this);
    }

    @Override
    public void instantiateServiceInstance() {
        LOG.debug("FRM started for: {}", nodeId.getValue());
        deviceMastered = true;
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        LOG.debug("FRM stopped for: {}", nodeId.getValue());
        deviceMastered = false;
        return Futures.immediateFuture(null);
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public void close() {
        if (clusterSingletonServiceRegistration != null) {
            try {
                clusterSingletonServiceRegistration.close();
            } catch (Exception e) {
                LOG.error("FRM cluster service close fail: {} {}", nodeId.getValue(), e);
            }
        }
    }

    public boolean isDeviceMastered() {
        return deviceMastered;
    }

}
