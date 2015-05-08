/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import java.util.concurrent.Callable;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DataChangeListenerImpl implements DataChangeListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DataChangeListenerImpl.class);
    private static final long STARTUP_LOOP_TICK = 500L;
    private static final int STARTUP_LOOP_MAX_RETRIES = 8;
    protected final ListenerRegistration<DataChangeListener> dataChangeListenerRegistration;
    protected OperationProcessor operationProcessor;

    /**
     * instance identifier to Node in network topology model (yangtools)
     */
    protected static final InstanceIdentifier<Topology> II_TO_TOPOLOGY =
            InstanceIdentifier
            .builder(NetworkTopology.class)
            .child(Topology.class, new TopologyKey(new TopologyId(FlowCapableTopologyProvider.TOPOLOGY_ID)))
            .build();


    /**
     *
     */
    public DataChangeListenerImpl(final OperationProcessor operationProcessor, final DataBroker dataBroker,
            final InstanceIdentifier<?> ii) {
        SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(STARTUP_LOOP_TICK, STARTUP_LOOP_MAX_RETRIES);
        try {
            dataChangeListenerRegistration = looper.loopUntilNoException(new Callable<ListenerRegistration<DataChangeListener>>() {
                @Override
                public ListenerRegistration<DataChangeListener> call() throws Exception {
                    return dataBroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, ii,
                            DataChangeListenerImpl.this, AsyncDataBroker.DataChangeScope.BASE);

                }
            });
        } catch (Exception e) {
            LOG.warn("data listener registration failed: {}", e.getMessage());
            LOG.debug("data listener registration failed.. ", e);
            throw new IllegalStateException("TopologyManager startup fail! TM bundle needs restart.", e);
        }

        this.operationProcessor = operationProcessor;
    }

    @Override
    public void close() throws Exception {
        dataChangeListenerRegistration.close();
    }

    protected <T extends DataObject> void sendToTransactionChain(final T node,
            final InstanceIdentifier<T> iiToTopologyNode) {
        operationProcessor.enqueueOperation(new TopologyOperation() {

            @Override
            public void applyOperation(ReadWriteTransaction transaction) {
                transaction.merge(LogicalDatastoreType.OPERATIONAL, iiToTopologyNode, node, true);
            }
        });
    }

    protected InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> provideIIToTopologyNode(
            final NodeId nodeIdInTopology) {
        org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey nodeKeyInTopology = new org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey(
                nodeIdInTopology);
        return II_TO_TOPOLOGY
                .builder()
                .child(org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node.class,
                        nodeKeyInTopology).build();
    }

    protected NodeId provideTopologyNodeId(InstanceIdentifier<?> iiToNodeInInventory) {
        final NodeKey inventoryNodeKey = iiToNodeInInventory.firstKeyOf(Node.class, NodeKey.class);
        if (inventoryNodeKey != null) {
            return new NodeId(inventoryNodeKey.getId().getValue());
        }
        return null;
    }

}
