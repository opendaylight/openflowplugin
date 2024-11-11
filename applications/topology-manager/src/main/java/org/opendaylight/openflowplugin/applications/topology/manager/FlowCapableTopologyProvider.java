/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.common.txchain.TransactionChainManager;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = { })
public final class FlowCapableTopologyProvider implements ClusterSingletonService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(FlowCapableTopologyProvider.class);
    private static final String TOPOLOGY_PROVIDER = "ofp-topology-manager";
    static final String TOPOLOGY_ID = "flow:1";

    private final DataObjectIdentifier.WithKey<Topology, TopologyKey> topologyPathIID;
    private final TransactionChainManager transactionChainManager;
    private final OperationProcessor processor;

    private Registration listenerRegistration;
    private Registration singletonServiceRegistration;

    @Inject
    @Activate
    public FlowCapableTopologyProvider(@Reference final DataBroker dataBroker,
            @Reference final NotificationService notificationService,
            @Reference final ClusterSingletonServiceProvider clusterSingletonServiceProvider,
            @Reference final OperationProcessor processor) {
        this.processor = requireNonNull(processor);
        final TopologyKey key = new TopologyKey(new TopologyId(TOPOLOGY_ID));
        topologyPathIID = DataObjectIdentifier.builder(NetworkTopology.class)
            .child(Topology.class, key)
            .build();

        listenerRegistration = notificationService.registerCompositeListener(
            new FlowCapableTopologyExporter(processor, topologyPathIID).toListener());
        transactionChainManager = new TransactionChainManager(dataBroker, TOPOLOGY_PROVIDER);
        transactionChainManager.activateTransactionManager();
        transactionChainManager.initialSubmitWriteTransaction();
        singletonServiceRegistration = clusterSingletonServiceProvider.registerClusterSingletonService(this);
        LOG.info("Topology Manager service started.");
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        transactionChainManager.close();
        if (listenerRegistration != null) {
            LOG.info("Closing notification listener registration.");
            listenerRegistration.close();
            listenerRegistration = null;
        }

        if (singletonServiceRegistration != null) {
            LOG.info("Closing clustering singleton service registration.");
            singletonServiceRegistration.close();
            singletonServiceRegistration = null;
        }
        LOG.info("Topology Manager instance is stopped.");
    }

    @Override
    public void instantiateServiceInstance() {
        LOG.debug("Topology Manager instance is elected as an active instance.");
        if (!isFlowTopologyExist(topologyPathIID)) {
            transactionChainManager.writeToTransaction(LogicalDatastoreType.OPERATIONAL, topologyPathIID,
                    new TopologyBuilder().withKey(new TopologyKey(new TopologyId(TOPOLOGY_ID))).build(), true);
            transactionChainManager.submitTransaction();
            LOG.info("Topology node {} is successfully written to the operational datastore.", TOPOLOGY_ID);
        }
    }

    @Override
    public ListenableFuture<? extends Object> closeServiceInstance() {
        return Futures.immediateFuture(null);
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return new ServiceGroupIdentifier(TOPOLOGY_PROVIDER);
    }

    private boolean isFlowTopologyExist(final WithKey<Topology, TopologyKey> path) {
        try {
            // FIXME: expose exists() from manager
            Optional<Topology> ofTopology = transactionChainManager
                    .readFromTransaction(LogicalDatastoreType.OPERATIONAL, path).get();
            LOG.debug("OpenFlow topology exist in the operational data store at {}", path);
            if (ofTopology.isPresent()) {
                return true;
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("OpenFlow topology read operation failed!", e);
        }
        return false;
    }
}
