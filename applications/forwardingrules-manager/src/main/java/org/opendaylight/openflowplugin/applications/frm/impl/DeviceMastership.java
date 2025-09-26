/*
 * Copyright (c) 2016, 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.api.ServiceGroupIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.frm.reconciliation.service.rev180227.ReconcileNode;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service (per device) for registration in singleton provider.
 */
public class DeviceMastership implements ClusterSingletonService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceMastership.class);
    private final NodeId nodeId;
    private final ServiceGroupIdentifier identifier;
    private final AtomicBoolean deviceMastered = new AtomicBoolean(false);
    private final AtomicBoolean isDeviceInOperDS = new AtomicBoolean(false);
    private final DataObjectIdentifier.WithKey<Node, NodeKey> path;
    private final DataObjectIdentifier<FlowCapableNode> fcnIID;

    private Registration reg;

    public DeviceMastership(final NodeId nodeId) {
        this.nodeId = nodeId;
        identifier = new ServiceGroupIdentifier(nodeId.getValue());
        path = DataObjectIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(nodeId)).build();
        fcnIID = path.toBuilder().augmentation(FlowCapableNode.class).build();
    }

    @Override
    public void instantiateServiceInstance() {
        LOG.info("FRM started for: {}", nodeId.getValue());
        deviceMastered.set(true);
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

    public void setDeviceOperationalStatus(final boolean inOperDS) {
        isDeviceInOperDS.set(inOperDS);
    }

    public void reconcile() {
        deviceMastered.set(true);
    }

    public void registerReconcileNode(final RpcProviderService rpcProviderService, final ReconcileNode reconcileNode) {
        if (reg == null) {
            LOG.debug("The path is registered : {}", path);
            reg = rpcProviderService.registerRpcImplementation(reconcileNode, ImmutableSet.of(path));
        } else {
            LOG.debug("The path is already registered : {}", path);
        }
    }

    public void deregisterReconcileNode() {
        if (reg != null) {
            reg.close();
            reg = null;
            LOG.debug("The path is unregistered : {}", path);
        } else {
            LOG.debug("The path is already unregistered : {}", path);
        }
    }
}
