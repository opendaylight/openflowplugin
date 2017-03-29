/**
 * Copyright (c) 2016, 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import java.util.concurrent.atomic.AtomicBoolean;
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
public class DeviceMastership implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceMastership.class);
    private final NodeId nodeId;
    private final FlowNodeReconciliation reconcliationAgent;
    private final AtomicBoolean deviceMastered = new AtomicBoolean(false);
    private final AtomicBoolean isDeviceInOperDS = new AtomicBoolean(false);
    private final InstanceIdentifier<FlowCapableNode> fcnIID;

    DeviceMastership(final NodeId nodeId,
                            final FlowNodeReconciliation reconcliationAgent) {
        this.nodeId = nodeId;
        this.reconcliationAgent = reconcliationAgent;
        fcnIID = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId)).augmentation
                (FlowCapableNode.class);
    }

    void changeDeviceMastered(final boolean mastered) {
        if (mastered) {
            LOG.info("FRM started for: {}", nodeId.getValue());
            deviceMastered.set(true);
            if(canReconcile()) {
                LOG.info("Triggering reconciliation for device {}", nodeId.getValue());
                reconcliationAgent.reconcileConfiguration(fcnIID);
            }
        } else {
            LOG.info("FRM stopped for: {}", nodeId.getValue());
            deviceMastered.set(false);
        }
    }

    boolean isDeviceMastered() {
        return deviceMastered.get();
    }

    void setDeviceOperationalStatus(boolean inOperDS) {
        isDeviceInOperDS.set(inOperDS);
        if(canReconcile()) {
            LOG.info("Triggering reconciliation for device {}", nodeId.getValue());
            reconcliationAgent.reconcileConfiguration(fcnIID);
        }
    }

    private boolean canReconcile() {
        return (deviceMastered.get() && isDeviceInOperDS.get());
    }

    @Override
    public void close() {
        //NOOP
    }
}
