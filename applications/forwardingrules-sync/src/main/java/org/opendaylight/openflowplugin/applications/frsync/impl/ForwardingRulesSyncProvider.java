/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.applications.frsync.NodeListener;
import org.opendaylight.openflowplugin.applications.frsync.SyncPlanPushStrategy;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeCachedDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeOdlDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeSnapshotDao;
import org.opendaylight.openflowplugin.applications.frsync.impl.clustering.DeviceMastershipManager;
import org.opendaylight.openflowplugin.applications.frsync.impl.strategy.SyncPlanPushStrategyFlatBatchImpl;
import org.opendaylight.openflowplugin.applications.frsync.impl.strategy.TableForwarder;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconciliationRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.SalFlatBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Top provider of forwarding rules synchronization functionality.
 */
public class ForwardingRulesSyncProvider implements AutoCloseable, BindingAwareProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ForwardingRulesSyncProvider.class);
    private static final String FRS_EXECUTOR_PREFIX = "FRS-executor-";

    private final DataBroker dataService;
    private final ClusterSingletonServiceProvider clusterSingletonService;
    private final SalTableService salTableService;
    private final SalFlatBatchService flatBatchService;

    /** Wildcard path to flow-capable-node augmentation of inventory node. */
    private static final InstanceIdentifier<FlowCapableNode> FLOW_CAPABLE_NODE_WC_PATH =
            InstanceIdentifier.create(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class);
    /** Wildcard path to node (not flow-capable-node augmentation) of inventory node. */
    private static final InstanceIdentifier<Node> NODE_WC_PATH =
            InstanceIdentifier.create(Nodes.class).child(Node.class);

    private final DataTreeIdentifier<FlowCapableNode> nodeConfigDataTreePath;
    private final DataTreeIdentifier<Node> nodeOperationalDataTreePath;

    private ListenerRegistration<NodeListener> dataTreeConfigChangeListener;
    private ListenerRegistration<NodeListener> dataTreeOperationalChangeListener;

    private final ListeningExecutorService syncThreadPool;

    public ForwardingRulesSyncProvider(final BindingAwareBroker broker,
                                       final DataBroker dataBroker,
                                       final RpcConsumerRegistry rpcRegistry,
                                       final ClusterSingletonServiceProvider clusterSingletonService) {
        Preconditions.checkNotNull(rpcRegistry, "RpcConsumerRegistry can not be null!");
        this.dataService = Preconditions.checkNotNull(dataBroker, "DataBroker can not be null!");
        this.clusterSingletonService = Preconditions.checkNotNull(clusterSingletonService,
                "ClusterSingletonServiceProvider can not be null!");
        this.salTableService = Preconditions.checkNotNull(rpcRegistry.getRpcService(SalTableService.class),
                "RPC SalTableService not found.");
        this.flatBatchService = Preconditions.checkNotNull(rpcRegistry.getRpcService(SalFlatBatchService.class),
                "RPC SalFlatBatchService not found.");

        nodeConfigDataTreePath = new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, FLOW_CAPABLE_NODE_WC_PATH);
        nodeOperationalDataTreePath = new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, NODE_WC_PATH);

        final ExecutorService executorService= Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                .setNameFormat(FRS_EXECUTOR_PREFIX + "%d")
                .setDaemon(false)
                .setUncaughtExceptionHandler((thread, e) -> LOG.error("Uncaught exception {}", thread, e))
                .build());
        syncThreadPool = MoreExecutors.listeningDecorator(executorService);
        broker.registerProvider(this);
    }

    @Override
    public void onSessionInitiated(final ProviderContext providerContext) {
        final TableForwarder tableForwarder = new TableForwarder(salTableService);

        final SyncPlanPushStrategy syncPlanPushStrategy = new SyncPlanPushStrategyFlatBatchImpl()
                .setFlatBatchService(flatBatchService)
                .setTableForwarder(tableForwarder);

        final ReconciliationRegistry reconciliationRegistry = new ReconciliationRegistry();
        final DeviceMastershipManager deviceMastershipManager =
                new DeviceMastershipManager(clusterSingletonService, reconciliationRegistry);

        final SyncReactor syncReactorImpl = new SyncReactorImpl(syncPlanPushStrategy);
        final SyncReactor syncReactorRetry = new SyncReactorRetryDecorator(syncReactorImpl, reconciliationRegistry);
        final SyncReactor syncReactorGuard = new SyncReactorGuardDecorator(syncReactorRetry);
        final SyncReactor syncReactorFutureZip = new SyncReactorFutureZipDecorator(syncReactorGuard, syncThreadPool);

        final SyncReactor reactor = new SyncReactorClusterDecorator(syncReactorFutureZip, deviceMastershipManager);

        final FlowCapableNodeSnapshotDao configSnapshot = new FlowCapableNodeSnapshotDao();
        final FlowCapableNodeSnapshotDao operationalSnapshot = new FlowCapableNodeSnapshotDao();
        final FlowCapableNodeDao configDao = new FlowCapableNodeCachedDao(configSnapshot,
                new FlowCapableNodeOdlDao(dataService, LogicalDatastoreType.CONFIGURATION));
        final FlowCapableNodeDao operationalDao = new FlowCapableNodeCachedDao(operationalSnapshot,
                new FlowCapableNodeOdlDao(dataService, LogicalDatastoreType.OPERATIONAL));

        final NodeListener<FlowCapableNode> nodeListenerConfig =
                new SimplifiedConfigListener(reactor, configSnapshot, operationalDao);
        final NodeListener<Node> nodeListenerOperational =
                new SimplifiedOperationalListener(reactor, operationalSnapshot, configDao, reconciliationRegistry, deviceMastershipManager);

        dataTreeConfigChangeListener =
                dataService.registerDataTreeChangeListener(nodeConfigDataTreePath, nodeListenerConfig);
        dataTreeOperationalChangeListener =
                dataService.registerDataTreeChangeListener(nodeOperationalDataTreePath, nodeListenerOperational);

        LOG.info("ForwardingRulesSync has started.");
    }

    public void close() {
        if (Objects.nonNull(dataTreeConfigChangeListener)) {
            dataTreeConfigChangeListener.close();
            dataTreeConfigChangeListener = null;
        }

        if (Objects.nonNull(dataTreeOperationalChangeListener)) {
            dataTreeOperationalChangeListener.close();
            dataTreeOperationalChangeListener = null;
        }

        syncThreadPool.shutdown();
    }

}
