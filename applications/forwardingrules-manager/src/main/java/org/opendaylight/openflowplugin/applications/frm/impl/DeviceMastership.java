/**
 * Copyright (c) 2016, 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
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
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service (per device) for registration in singleton provider.
 */
public class DeviceMastership implements ClusterSingletonService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceMastership.class);
    private final NodeId nodeId;
    private final ServiceGroupIdentifier identifier;
    private final ClusterSingletonServiceProvider clusterSingletonServiceProvider;
    private final FlowNodeReconciliation reconcliationAgent;
    private ClusterSingletonServiceRegistration clusterSingletonServiceRegistration;
    private boolean deviceMastered;

    public DeviceMastership(final NodeId nodeId,
                            final ClusterSingletonServiceProvider clusterSingletonService,
                            final FlowNodeReconciliation reconcliationAgent) {
        this.nodeId = nodeId;
        this.identifier = ServiceGroupIdentifier.create(nodeId.getValue());
        this.deviceMastered = false;
        this.clusterSingletonServiceProvider = clusterSingletonService;
        this.reconcliationAgent = reconcliationAgent;
    }

    @Override
    public void instantiateServiceInstance() {
        LOG.info("FRM started for: {}", nodeId.getValue());
        deviceMastered = true;
        InstanceIdentifier<FlowCapableNode> fcnIID = InstanceIdentifier.create(Nodes.class).child(Node.class, new
                NodeKey(nodeId)).augmentation(FlowCapableNode.class);

        //Notify about the connect node and trigger the reconciliation.
        this.reconcliationAgent.flowNodeConnected(fcnIID, true);
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        LOG.info("FRM stopped for: {}", nodeId.getValue());
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

    public void registerClusterSingletonService() {
        LOG.info("Registering FRM as a cluster singleton service listner for service id : {}",getIdentifier());
        clusterSingletonServiceRegistration = clusterSingletonServiceProvider.registerClusterSingletonService(this);
    }
}
