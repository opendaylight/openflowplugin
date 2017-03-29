/*
 * Copyright (c) 2016, 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for clustering service registrations of {@link DeviceMastership}.
 */
public class DeviceMastershipManager implements AutoCloseable, MastershipChangeService {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceMastershipManager.class);

    private final Object lockObj = new Object();
    private final FlowNodeReconciliation reconcliationAgent;
    private final ConcurrentHashMap<NodeId, DeviceMastership> deviceMasterships = new ConcurrentHashMap<>();
    private Set<NodeId> activeNodes = Collections.emptySet();

    DeviceMastershipManager(final FlowNodeReconciliation reconcliationAgent) {
        this.reconcliationAgent = reconcliationAgent;
    }

    public boolean isDeviceMastered(final NodeId nodeId) {
        return deviceMasterships.get(nodeId) != null && deviceMasterships.get(nodeId).isDeviceMastered();
    }

    boolean isNodeActive(final NodeId nodeId) {
        return activeNodes.contains(nodeId);

    }

    @VisibleForTesting
    ConcurrentHashMap<NodeId, DeviceMastership> getDeviceMasterships() {
        return deviceMasterships;
    }

    @Override
    public void close() {
        //noop
    }

    @Override
    public Future<Void> onBecomeOwner(@Nonnull DeviceInfo deviceInfo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("FRM: Node {} become master. Starting reconciliation if possible.", deviceInfo.getLOGValue());
        }
        DeviceMastership deviceMastership = deviceMasterships.computeIfAbsent(deviceInfo.getNodeId(), device ->
                new DeviceMastership(deviceInfo.getNodeId(), reconcliationAgent));
        deviceMastership.changeDeviceMastered(true);
        synchronized (lockObj) {
            Set<NodeId> set = Sets.newHashSet(activeNodes);
            set.add(deviceInfo.getNodeId());
            activeNodes = Collections.unmodifiableSet(set);
        }
        return Futures.immediateFuture(null);
    }

    @Override
    public Future<Void> onLoseOwnership(@Nonnull DeviceInfo deviceInfo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("FRM: Node {} become slave or was disconnected.", deviceInfo.getLOGValue());
        }
        synchronized (lockObj) {
            Set<NodeId> set = Sets.newHashSet(activeNodes);
            set.remove(deviceInfo.getNodeId());
            activeNodes = Collections.unmodifiableSet(set);
        }
        final DeviceMastership mastership = deviceMasterships.remove(deviceInfo.getNodeId());
        if (mastership != null) {
            mastership.close();
        }
        return Futures.immediateFuture(null);
    }
}
