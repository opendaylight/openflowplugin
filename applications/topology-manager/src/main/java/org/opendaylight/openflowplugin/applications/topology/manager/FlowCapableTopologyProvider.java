/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import java.util.concurrent.ExecutionException;

import com.google.common.base.Optional;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowCapableTopologyProvider implements BindingAwareProvider, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(FlowCapableTopologyProvider.class);
    private ListenerRegistration<NotificationListener> listenerRegistration;
    private Thread thread;
    private TerminationPointChangeListenerImpl terminationPointChangeListener;
    private NodeChangeListenerImpl nodeChangeListener;
    static final String TOPOLOGY_ID = "flow:1";

    /**
     * Gets called on start of a bundle.
     *
     * @param session
     */
    @Override
    public synchronized void onSessionInitiated(final ProviderContext session) {
        final DataBroker dataBroker = session.getSALService(DataBroker.class);
        final NotificationProviderService notificationService = session.getSALService(NotificationProviderService.class);

        final TopologyKey key = new TopologyKey(new TopologyId(TOPOLOGY_ID));
        final InstanceIdentifier<Topology> path = InstanceIdentifier
                .create(NetworkTopology.class)
                .child(Topology.class, key);

        final OperationProcessor processor = new OperationProcessor(dataBroker);
        final FlowCapableTopologyExporter listener = new FlowCapableTopologyExporter(processor, path);
        this.listenerRegistration = notificationService.registerNotificationListener(listener);
        this.terminationPointChangeListener = new TerminationPointChangeListenerImpl(dataBroker, processor);
        nodeChangeListener = new NodeChangeListenerImpl(dataBroker, processor);

        if(!isFlowTopologyExist(dataBroker, path)){
            final ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
            tx.put(LogicalDatastoreType.OPERATIONAL, path, new TopologyBuilder().setKey(key).build(), true);
            try {
                tx.submit().get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.warn("Initial topology export failed, continuing anyway", e);
            }
        }

        thread = new Thread(processor);
        thread.setDaemon(true);
        thread.setName("FlowCapableTopologyExporter-" + TOPOLOGY_ID);
        thread.start();
    }

    @Override
    public synchronized void close() throws InterruptedException {
        LOG.info("FlowCapableTopologyProvider stopped.");
        if (this.listenerRegistration != null) {
            try {
                this.listenerRegistration.close();
            } catch (Exception e) {
                LOG.warn("Failed to close listener registration: {}", e.getMessage());
                LOG.debug("Failed to close listener registration.. ", e);
            }
            listenerRegistration = null;
        }
        unregisterListener(terminationPointChangeListener);
        unregisterListener(nodeChangeListener);
        if (thread != null) {
            thread.interrupt();
            thread.join();
            thread = null;
        }
    }

    private static void unregisterListener(final AutoCloseable listenerToClose) {
        if (listenerToClose != null) {
            try {
                listenerToClose.close();
            } catch (Exception e) {
                LOG.warn("Failed to close listener registration: {}", e.getMessage());
                LOG.debug("Failed to close listener registration.. ", e);
            }
        }
    }

    private boolean isFlowTopologyExist(final DataBroker dataBroker,
                                        final InstanceIdentifier<Topology> path) {
        final ReadTransaction tx = dataBroker.newReadOnlyTransaction();
        try {
            Optional<Topology> ofTopology = tx.read(LogicalDatastoreType.OPERATIONAL, path).checkedGet();
            LOG.debug("OpenFlow topology exist in the operational data store at {}",path);
            if(ofTopology.isPresent()){
                return true;
            }
        } catch (ReadFailedException e) {
            LOG.warn("OpenFlow topology read operation failed!", e);
        }
        return false;
    }
}
