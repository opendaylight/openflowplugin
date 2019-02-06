/**
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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.frm.reconciliation.service.rev180227.FrmReconciliationService;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
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
    private final InstanceIdentifier<FlowCapableNode> fcnIID;
    private final KeyedInstanceIdentifier<Node, NodeKey> path;

    private ObjectRegistration<@NonNull FrmReconciliationService> reg;

    public DeviceMastership(final NodeId nodeId) {
        this.nodeId = nodeId;
        this.identifier = ServiceGroupIdentifier.create(nodeId.getValue());
        fcnIID = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId))
                .augmentation(FlowCapableNode.class);
        path = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId));
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

    public void registerReconciliationRpc(final RpcProviderService rpcProviderService,
            final FrmReconciliationService reconcliationService) {
        if (reg == null) {
            LOG.debug("The path is registered : {}", path);
            reg = rpcProviderService.registerRpcImplementation(FrmReconciliationService.class, reconcliationService,
                ImmutableSet.of(path));
        } else {
            LOG.debug("The path is already registered : {}", path);
        }
    }

    public void deregisterReconciliationRpc() {
        if (reg != null) {
            reg.close();
            reg = null;
            LOG.debug("The path is unregistered : {}", path);
        } else {
            LOG.debug("The path is already unregistered : {}", path);
        }
    }
}
