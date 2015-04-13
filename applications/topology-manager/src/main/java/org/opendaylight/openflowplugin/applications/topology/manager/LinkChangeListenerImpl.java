/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import java.util.Map;
import java.util.Set;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LinkChangeListenerImpl implements DataChangeListener, AutoCloseable {
    private final ListenerRegistration<DataChangeListener> dataChangeListenerRegistration;
    private OperationProcessor operationProcessor;

    public LinkChangeListenerImpl(final DataBroker dataBroker, final OperationProcessor operationProcessor) {
        dataChangeListenerRegistration = dataBroker.registerDataChangeListener(
                LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.builder(Nodes.class)
                        .child(Node.class)
                        .child(NodeConnector.class)
                        .augmentation(FlowCapableNodeConnector.class)
                        .build(),
                this, AsyncDataBroker.DataChangeScope.BASE);
        this.operationProcessor = operationProcessor;
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        processAddedNodeConnectors(change.getCreatedData());
        processUpdatedNodeConnectors(change.getUpdatedData());
        processRemovedNodeConnectors(change.getRemovedPaths());
    }

    /**
     * @param removedPaths
     */
    private void processRemovedNodeConnectors(Set<InstanceIdentifier<?>> removedPaths) {
        // TODO Auto-generated method stub

    }

    /**
     * @param updatedData
     */
    private void processUpdatedNodeConnectors(Map<InstanceIdentifier<?>, DataObject> updatedData) {
        // TODO Auto-generated method stub

    }

    /**
     * @param createdData
     */
    private void processAddedNodeConnectors(Map<InstanceIdentifier<?>, DataObject> createdData) {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() throws Exception {
        dataChangeListenerRegistration.close();
    }

}
