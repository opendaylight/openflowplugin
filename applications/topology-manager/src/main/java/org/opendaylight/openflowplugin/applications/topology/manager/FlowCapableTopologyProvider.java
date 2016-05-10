/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import com.google.common.base.Optional;
import java.util.concurrent.ExecutionException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
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

public class FlowCapableTopologyProvider implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(FlowCapableTopologyProvider.class);
    static final String TOPOLOGY_ID = "flow:1";

    private final DataBroker dataBroker;
    private final NotificationProviderService notificationService;
    private final OperationProcessor processor;
    private ListenerRegistration<NotificationListener> listenerRegistration;

    public FlowCapableTopologyProvider(DataBroker dataBroker, NotificationProviderService notificationService,
            OperationProcessor processor) {
        this.dataBroker = dataBroker;
        this.notificationService = notificationService;
        this.processor = processor;
    }

    /**
     * Gets called on start of a bundle.
     */
    public void start() {
        final TopologyKey key = new TopologyKey(new TopologyId(TOPOLOGY_ID));
        final InstanceIdentifier<Topology> path = InstanceIdentifier
                .create(NetworkTopology.class)
                .child(Topology.class, key);

        final FlowCapableTopologyExporter listener = new FlowCapableTopologyExporter(processor, path);
        this.listenerRegistration = notificationService.registerNotificationListener(listener);

        if(!isFlowTopologyExist(dataBroker, path)){
            final ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
            tx.put(LogicalDatastoreType.OPERATIONAL, path, new TopologyBuilder().setKey(key).build(), true);
            try {
                tx.submit().get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.warn("Initial topology export failed, continuing anyway", e);
            }
        }

        LOG.info("FlowCapableTopologyProvider started");
    }

    @Override
    public void close() {
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
