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
import java.util.concurrent.atomic.AtomicBoolean;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
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
    private final FlowNodeReconciliation reconcliationAgent;
    private final AtomicBoolean deviceMastered = new AtomicBoolean(false);
    private final AtomicBoolean isDeviceInOperDS = new AtomicBoolean(false);
    private final InstanceIdentifier<FlowCapableNode> fcnIID;

    public DeviceMastership(final NodeId nodeId,
                            final FlowNodeReconciliation reconcliationAgent) {
        this.nodeId = nodeId;
        this.identifier = ServiceGroupIdentifier.create(nodeId.getValue());
        this.reconcliationAgent = reconcliationAgent;
        fcnIID = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId)).augmentation
                (FlowCapableNode.class);
    }

    @Override
    public void instantiateServiceInstance() {
        LOG.info("FRM started for: {}", nodeId.getValue());
        deviceMastered.set(true);
        if(canReconcile()) {
            LOG.info("Triggering reconciliation for device {}", nodeId.getValue());
            reconcliationAgent.reconcileConfiguration(fcnIID);
        }
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        LOG.info("FRM stopped for: {}", nodeId.getValue());
        deviceMastered.set(false);
        return Futures.immediateFuture(null);
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public void close() {
    }

    public boolean isDeviceMastered() {
        return deviceMastered.get();
    }

    public void setDeviceOperationalStatus(boolean inOperDS) {
        isDeviceInOperDS.set(inOperDS);
        if(canReconcile()) {
            reconcliationAgent.reconcileConfiguration(fcnIID);
        }
    }

    private boolean canReconcile() {
        return (deviceMastered.get() && isDeviceInOperDS.get());
    }

    public void reconcile() {
        deviceMastered.set(true);
        LOG.info("Triggering reconciliation for device {}", nodeId.getValue());
        reconcliationAgent.reconcileConfiguration(fcnIID);
    }
}
