/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.common.txchain.TransactionChainManager;
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

@Singleton
public class FlowCapableTopologyProvider implements ClusterSingletonService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(FlowCapableTopologyProvider.class);
    private static final String TOPOLOGY_PROVIDER = "ofp-topology-manager";
    static final String TOPOLOGY_ID = "flow:1";

    private final DataBroker dataBroker;
    private final NotificationService notificationService;
    private final OperationProcessor processor;
    private final ClusterSingletonServiceProvider clusterSingletonServiceProvider;
    private InstanceIdentifier<Topology> topologyPathIID;
    private TransactionChainManager transactionChainManager;
    private ListenerRegistration<NotificationListener> listenerRegistration;
    private ClusterSingletonServiceRegistration singletonServiceRegistration;

    @Inject
    public FlowCapableTopologyProvider(@Reference final DataBroker dataBroker,
                                       @Reference final NotificationService notificationService,
                                       final OperationProcessor processor,
                                       @Reference final ClusterSingletonServiceProvider
                                               clusterSingletonServiceProvider) {
        this.dataBroker = dataBroker;
        this.notificationService = notificationService;
        this.processor = processor;
        this.clusterSingletonServiceProvider = clusterSingletonServiceProvider;
    }

    /**
     * Gets called on start of a bundle.
     */
    @PostConstruct
    public void start() {
        final TopologyKey key = new TopologyKey(new TopologyId(TOPOLOGY_ID));
        this.topologyPathIID = InstanceIdentifier.create(NetworkTopology.class).child(Topology.class, key);

        final FlowCapableTopologyExporter listener = new FlowCapableTopologyExporter(processor, topologyPathIID);
        this.listenerRegistration = notificationService.registerNotificationListener(listener);
        this.transactionChainManager = new TransactionChainManager(dataBroker, TOPOLOGY_PROVIDER);
        this.transactionChainManager.activateTransactionManager();
        this.transactionChainManager.initialSubmitWriteTransaction();
        this.singletonServiceRegistration = this.clusterSingletonServiceProvider.registerClusterSingletonService(this);
        LOG.info("Topology Manager service started.");
    }

    @Override
    @PreDestroy
    public void close() {
        this.transactionChainManager.close();
        if (this.listenerRegistration != null) {
            LOG.info("Closing notification listener registration.");
            this.listenerRegistration.close();
            this.listenerRegistration = null;
        }

        if (this.singletonServiceRegistration != null) {
            LOG.info("Closing clustering singleton service registration.");
            this.singletonServiceRegistration.close();
            this.singletonServiceRegistration = null;
        }
        LOG.debug("Topology Manager instance is stopped.");
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

    @Nonnull
    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return ServiceGroupIdentifier.create(TOPOLOGY_PROVIDER);
    }

    private boolean isFlowTopologyExist(final InstanceIdentifier<Topology> path) {
        try {
            Optional<Topology> ofTopology = this.transactionChainManager
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
