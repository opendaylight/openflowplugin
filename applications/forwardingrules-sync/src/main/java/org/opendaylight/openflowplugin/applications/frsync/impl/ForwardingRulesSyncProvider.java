/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Callable;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.openflowplugin.applications.frsync.NodeListener;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeCachedDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeOdlDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeSnapshotDao;
import org.opendaylight.openflowplugin.applications.frsync.util.SemaphoreKeeperGuavaImpl;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * top provider of forwarding rules synchronization functionality
 */
@SuppressWarnings("deprecation")
public class ForwardingRulesSyncProvider implements AutoCloseable, BindingAwareProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ForwardingRulesSyncProvider.class);
    public static final int STARTUP_LOOP_TICK = 500;
    public static final int STARTUP_LOOP_MAX_RETRIES = 8;

    private final DataBroker dataService;
    private final SalFlowService salFlowService;
    private final SalGroupService salGroupService;
    private final SalMeterService salMeterService;
    private final SalTableService salTableService;
    private final FlowCapableTransactionService transactionService;

    /** wildcard path to flow-capable-node augmentation of inventory node */
    private static final InstanceIdentifier<FlowCapableNode> FLOW_CAPABLE_NODE_WC_PATH =
            InstanceIdentifier.create(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class);
    /** wildcard path to node (not flow-capable-node augmentation) of inventory node */
    private static final InstanceIdentifier<Node> NODE_WC_PATH =
            InstanceIdentifier.create(Nodes.class).child(Node.class);


    private final DataTreeIdentifier<FlowCapableNode> nodeConfigDataTreePath;
    private final DataTreeIdentifier<Node> nodeOperationalDataTreePath;

    private ListenerRegistration<NodeListener> dataTreeConfigChangeListener;
    private ListenerRegistration<NodeListener> dataTreeOperationalChangeListener;


    public ForwardingRulesSyncProvider(final BindingAwareBroker broker,
                                       final DataBroker dataBroker,
                                       final RpcConsumerRegistry rpcRegistry) {
        this.dataService = Preconditions.checkNotNull(dataBroker, "DataBroker can not be null!");

        Preconditions.checkArgument(rpcRegistry != null, "RpcConsumerRegistry can not be null !");

        this.salFlowService = Preconditions.checkNotNull(rpcRegistry.getRpcService(SalFlowService.class),
                "RPC SalFlowService not found.");
        this.salGroupService = Preconditions.checkNotNull(rpcRegistry.getRpcService(SalGroupService.class),
                "RPC SalGroupService not found.");
        this.salMeterService = Preconditions.checkNotNull(rpcRegistry.getRpcService(SalMeterService.class),
                "RPC SalMeterService not found.");
        this.salTableService = Preconditions.checkNotNull(rpcRegistry.getRpcService(SalTableService.class),
                "RPC SalTableService not found.");
        this.transactionService =
                Preconditions.checkNotNull(rpcRegistry.getRpcService(FlowCapableTransactionService.class),
                        "RPC SalTableService not found.");

        nodeConfigDataTreePath =
                new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, FLOW_CAPABLE_NODE_WC_PATH);
        nodeOperationalDataTreePath = new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, NODE_WC_PATH);

        broker.registerProvider(this);
    }

    private final ListeningExecutorService syncThreadPool = FrmExecutors.instance()
            // TODO improve log in ThreadPoolExecutor.afterExecute
            // TODO max bloking queue size
            // TODO core/min pool size
            .newFixedThreadPool(6, new ThreadFactoryBuilder()
                    .setNameFormat(SyncReactorFutureDecorator.FRM_RPC_CLIENT_PREFIX + "%d")
                    .setDaemon(false)
                    .setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                        @Override
                        public void uncaughtException(Thread thread, Throwable e) {
                            LOG.error("uncaught exception {}", thread, e);
                        }
                    })
                    .build());
    @Override
    public void onSessionInitiated(final BindingAwareBroker.ProviderContext providerContext) {
        final FlowForwarder flowForwarder = new FlowForwarder(salFlowService);
        final GroupForwarder groupForwarder = new GroupForwarder(salGroupService);
        final MeterForwarder meterForwarder = new MeterForwarder(salMeterService);
        final TableForwarder tableForwarder = new TableForwarder(salTableService);

        {
            final SyncReactorImpl syncReactorImpl = new SyncReactorImpl();
            final SyncReactor syncReactorGuard = new SyncReactorGuardDecorator(syncReactorImpl
                    .setFlowForwarder(flowForwarder)
                    .setGroupForwarder(groupForwarder)
                    .setMeterForwarder(meterForwarder)
                    .setTableForwarder(tableForwarder)
                    .setTransactionService(transactionService),
                    new SemaphoreKeeperGuavaImpl<InstanceIdentifier<FlowCapableNode>>(1, true));

            final SyncReactor cfgReactor = new SyncReactorFutureWithCompressionDecorator(syncReactorGuard, syncThreadPool);
            final SyncReactor operReactor = new SyncReactorFutureWithCompressionDecorator(syncReactorGuard, syncThreadPool);

            final FlowCapableNodeSnapshotDao configSnapshot = new FlowCapableNodeSnapshotDao();
            final FlowCapableNodeSnapshotDao operationalSnapshot = new FlowCapableNodeSnapshotDao();
            final FlowCapableNodeDao configDao = new FlowCapableNodeCachedDao(configSnapshot,
                    new FlowCapableNodeOdlDao(dataService, LogicalDatastoreType.CONFIGURATION));
            final FlowCapableNodeDao operationalDao = new FlowCapableNodeCachedDao(operationalSnapshot,
                    new FlowCapableNodeOdlDao(dataService, LogicalDatastoreType.OPERATIONAL));

            final NodeListener<FlowCapableNode> nodeListenerConfig =
                    new SimplifiedConfigListener(
                            cfgReactor,
                            configSnapshot, operationalDao);
            final NodeListener<Node> nodeListenerOperational =
                    new SimplifiedOperationalListener(operReactor, operationalSnapshot, configDao);

            try {
                SimpleTaskRetryLooper looper1 = new SimpleTaskRetryLooper(STARTUP_LOOP_TICK, STARTUP_LOOP_MAX_RETRIES);
                dataTreeConfigChangeListener = looper1.loopUntilNoException(
                        new Callable<ListenerRegistration<NodeListener>>() {
                            @Override
                            public ListenerRegistration<NodeListener> call() throws Exception {
                                return dataService.registerDataTreeChangeListener(
                                        nodeConfigDataTreePath, nodeListenerConfig);
                            }
                        });

                SimpleTaskRetryLooper looper2 = new SimpleTaskRetryLooper(STARTUP_LOOP_TICK, STARTUP_LOOP_MAX_RETRIES);
                dataTreeOperationalChangeListener = looper2.loopUntilNoException(
                        new Callable<ListenerRegistration<NodeListener>>() {
                            @Override
                            public ListenerRegistration<NodeListener> call() throws Exception {
                                return dataService.registerDataTreeChangeListener(
                                        nodeOperationalDataTreePath, nodeListenerOperational);
                            }
                        });
            } catch (final Exception e) {
                LOG.warn("FR-Sync node DataChange listener registration fail!", e);
                throw new IllegalStateException("FR-Sync startup fail!", e);
            }
        }
        LOG.info("ForwardingRulesSync has started.");
    }

    public void close() throws Exception {
        if (dataTreeConfigChangeListener != null) {
            dataTreeConfigChangeListener.close();
            dataTreeConfigChangeListener = null;
        }
        if (dataTreeOperationalChangeListener != null) {
            dataTreeOperationalChangeListener.close();
            dataTreeOperationalChangeListener = null;
        }

        syncThreadPool.shutdown();
    }
}
