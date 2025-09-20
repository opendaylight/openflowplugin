/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.applications.frsync.NodeListener;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeCachedDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeOdlDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeSnapshotDao;
import org.opendaylight.openflowplugin.applications.frsync.impl.clustering.DeviceMastershipManager;
import org.opendaylight.openflowplugin.applications.frsync.impl.strategy.SyncPlanPushStrategyFlatBatchImpl;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconciliationRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTable;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Top provider of forwarding rules synchronization functionality.
 */
@Singleton
@Component(service = { })
public class ForwardingRulesSyncProvider implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ForwardingRulesSyncProvider.class);
    private static final String FRS_EXECUTOR_PREFIX = "FRS-executor-";

    private final DataBroker dataService;
    private final ClusterSingletonServiceProvider clusterSingletonService;
    private final UpdateTable updateTable;
    private final ProcessFlatBatch processFlatBatch;

    /** Wildcard path to flow-capable-node augmentation of inventory node. */
    private static final DataObjectReference<FlowCapableNode> FLOW_CAPABLE_NODE_WC_PATH =
        DataObjectReference.builder(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class).build();
    /** Wildcard path to node (not flow-capable-node augmentation) of inventory node. */
    private static final DataObjectReference<Node> NODE_WC_PATH =
        DataObjectReference.builder(Nodes.class).child(Node.class).build();

    private Registration dataTreeConfigChangeListener;
    private Registration dataTreeOperationalChangeListener;

    private final ExecutorService syncThreadPool;

    @Inject
    @Activate
    public ForwardingRulesSyncProvider(@Reference final DataBroker dataBroker,
            @Reference final RpcService rpcRegistry,
            @Reference final ClusterSingletonServiceProvider clusterSingletonService) {
        requireNonNull(rpcRegistry, "RpcService can not be null!");
        dataService = requireNonNull(dataBroker, "DataBroker can not be null!");
        this.clusterSingletonService = requireNonNull(clusterSingletonService,
                "ClusterSingletonServiceProvider can not be null!");
        updateTable = requireNonNull(rpcRegistry.getRpc(UpdateTable.class),
                "RPC UpdateTable not found.");
        processFlatBatch = requireNonNull(rpcRegistry.getRpc(ProcessFlatBatch.class),
                "RPC SalFlatBatchService not found.");

        syncThreadPool = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setNameFormat(FRS_EXECUTOR_PREFIX + "%d")
            .setDaemon(false)
            .setUncaughtExceptionHandler((thread, ex) -> LOG.error("Uncaught exception {}", thread, ex))
            .build());

        final var syncPlanPushStrategy = new SyncPlanPushStrategyFlatBatchImpl(processFlatBatch);

        final var reconciliationRegistry = new ReconciliationRegistry();
        final var deviceMastershipManager = new DeviceMastershipManager(clusterSingletonService,
            reconciliationRegistry);

        final var syncReactorImpl = new SyncReactorImpl(syncPlanPushStrategy);
        final var syncReactorRetry = new SyncReactorRetryDecorator(syncReactorImpl, reconciliationRegistry);
        final var syncReactorGuard = new SyncReactorGuardDecorator(syncReactorRetry);
        final var syncReactorFutureZip = new SyncReactorFutureZipDecorator(syncReactorGuard, syncThreadPool);

        final var reactor = new SyncReactorClusterDecorator(syncReactorFutureZip, deviceMastershipManager);

        final var configSnapshot = new FlowCapableNodeSnapshotDao();
        final var operationalSnapshot = new FlowCapableNodeSnapshotDao();
        final var configDao = new FlowCapableNodeCachedDao(configSnapshot,
                new FlowCapableNodeOdlDao(dataService, LogicalDatastoreType.CONFIGURATION));
        final var operationalDao = new FlowCapableNodeCachedDao(operationalSnapshot,
                new FlowCapableNodeOdlDao(dataService, LogicalDatastoreType.OPERATIONAL));

        final NodeListener<FlowCapableNode> nodeListenerConfig =
                new SimplifiedConfigListener(reactor, configSnapshot, operationalDao);
        final NodeListener<Node> nodeListenerOperational = new SimplifiedOperationalListener(reactor,
                operationalSnapshot, configDao, reconciliationRegistry, deviceMastershipManager);

        dataTreeConfigChangeListener = dataService.registerTreeChangeListener(LogicalDatastoreType.CONFIGURATION,
            FLOW_CAPABLE_NODE_WC_PATH, nodeListenerConfig);
        dataTreeOperationalChangeListener = dataService.registerTreeChangeListener(LogicalDatastoreType.OPERATIONAL,
            NODE_WC_PATH, nodeListenerOperational);
        LOG.info("ForwardingRulesSync started");
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        if (dataTreeConfigChangeListener != null) {
            dataTreeConfigChangeListener.close();
            dataTreeConfigChangeListener = null;
        }

        if (dataTreeOperationalChangeListener != null) {
            dataTreeOperationalChangeListener.close();
            dataTreeOperationalChangeListener = null;
        }

        syncThreadPool.shutdown();
        LOG.info("ForwardingRulesSync stopped");
    }
}
