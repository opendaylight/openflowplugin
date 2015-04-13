/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import java.util.Map;
import java.util.Set;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;

    public class NodeChangeListenerImpl implements DataChangeListener, AutoCloseable {
        private final ListenerRegistration<DataChangeListener> dataChangeListenerRegistration;

        public NodeChangeListenerImpl(final DataBroker dataBroker) {
            dataChangeListenerRegistration = dataBroker.registerDataChangeListener(
                    LogicalDatastoreType.OPERATIONAL,
                    InstanceIdentifier.builder(Nodes.class)
                            .child(Node.class)
                            .build(),
                    this, AsyncDataBroker.DataChangeScope.BASE);
        }

        @Override
        public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
            processAddedNode(change.getCreatedData());
            processUpdatedNode(change.getUpdatedData());
            processRemovedNode(change.getRemovedPaths());
        }

        /**
         * @param removedPaths
         */
        private void processRemovedNode(Set<InstanceIdentifier<?>> removedPaths) {
            // TODO Auto-generated method stub

        }

        /**
         * @param updatedData
         */
        private void processUpdatedNode(Map<InstanceIdentifier<?>, DataObject> updatedData) {
            // TODO Auto-generated method stub

        }

        /**
         * @param createdData
         */
        private void processAddedNode(Map<InstanceIdentifier<?>, DataObject> createdData) {
            // TODO Auto-generated method stub

        }

        @Override
        public void close() throws Exception {
            dataChangeListenerRegistration.close();
        }

    }