/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.testcommon;

import java.util.Set;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * provides activation and deactivation of drop responder service - responds on packetIn
 */
public class DropTestRpcProvider implements AutoCloseable, DataChangeListener {
    private static final Logger LOG = LoggerFactory.getLogger(DropTestDsProvider.class);

    private SalFlowService flowService;
    private NotificationService notificationService;
    private DropTestRpcSender commiter = new DropTestRpcSender();
    private boolean active = false;

    /**
     * @param flowService value for setter
     */
    public void setFlowService(final SalFlowService flowService) {
        this.flowService = flowService;
    }

    /**
     * @param notificationService value for setter
     */
    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * activates drop responder
     */
    public void start() {
        commiter.setFlowService(flowService);
        commiter.setNotificationService(notificationService);
        commiter.start();
        active = true;
        LOG.debug("DropTestProvider Started.");
    }

    /**
     * @return message counts
     */
    public DropTestStats getStats() {
        if (this.commiter != null) {
            return commiter.getStats();
        } else {
            return new DropTestStats("Not initialized yet.");
        }
    }

    /**
     * reset message counts
     */
    public void clearStats() {
        if (commiter != null) {
            commiter.clearStats();
        }
    }

    @Override
    public void close() {
        LOG.debug("DropTestProvider stopped.");
        if (commiter != null) {
            commiter.close();
            active = false;
        }
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        final Set<InstanceIdentifier<?>> createdNodes = change.getCreatedData().keySet();
        final Set<InstanceIdentifier<?>> removedPaths = change.getRemovedPaths();

        if (null != createdNodes) {
            for (InstanceIdentifier<?> path : createdNodes) {
                NodeId nodeId = getNodeId(path);
                LOG.debug("Node {} was added to inventory.", nodeId);
                commiter.createExecutorForNode(nodeId);
            }
        }

        if (null != removedPaths) {
            for (InstanceIdentifier<?> path : removedPaths) {
                NodeId nodeId = getNodeId(path);
                LOG.debug("Node {} was removed from inventory.", nodeId);
                commiter.removeNodesExecutor(nodeId);
            }
        }


    }

    private NodeId getNodeId(final InstanceIdentifier<?> path) {
        NodeKey nodeKey = path.firstKeyOf(Node.class, NodeKey.class);
        return nodeKey.getId();
    }
}
