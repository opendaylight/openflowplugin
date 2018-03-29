/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.arbitratorreconciliation.impl;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;

import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationListener;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.applications.arbitratorreconciliation.api.ArbitraryReconciliationProperty;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationNotificationListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.SalBundleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.Messages;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.MessagesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.Message;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.MessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.remove.flow._case.RemoveFlowCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.remove.group._case.RemoveGroupCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleControlType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.ArbitratorConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.ArbitratorConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.ArbitratorReconcileService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.CommitActiveBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.CommitActiveBundleOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.CommitActiveBundleOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.GetActiveBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.GetActiveBundleOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.GetActiveBundleOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArbitratorReconciliationManagerImpl implements ClusteredDataTreeChangeListener<ArbitratorConfig>,
    ArbitratorReconcileService, ReconciliationNotificationListener, ConfigurationListener,
    AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ArbitratorReconciliationManagerImpl.class);
    private static final int THREAD_POOL_SIZE = 4;
    private static final AtomicLong BUNDLE_ID = new AtomicLong();
    private static final BundleFlags BUNDLE_FLAGS = new BundleFlags(true, true);
    private static final int ARBITRATOR_RECONCILIATION_PRIORITY = Integer
            .getInteger("arbitrator.reconciliation.manager.priority", 1/*default*/);
    private static final String SERVICE_NAME = "ArbitratorReconciliationManager";
    private static final String SEPARATOR = ":";

    private final SalBundleService salBundleService;
    private final DataBroker dataBroker;
    private final ReconciliationManager reconciliationManager;
    private final RoutedRpcRegistration routedRpcReg;
    private final AutoCloseable configurationServiceRegistration;
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private final Map<BigInteger, BundleDetails> bundleIdMap = new HashMap<>();
    private ListenerRegistration<ArbitratorReconciliationManagerImpl> registration;
    private AtomicBoolean isArbitratorReconEnabled = new AtomicBoolean(false);

    public ArbitratorReconciliationManagerImpl(final DataBroker dataService, final RpcProviderRegistry rpcRegistry,
            final ReconciliationManager reconciliationManager, final ConfigurationService configurationService) {
        this.dataBroker = Preconditions.checkNotNull(dataService, "DataBroker cannot be null!");
        Preconditions.checkArgument(rpcRegistry != null, "RpcConsumerRegistry cannot be null !");
        this.configurationServiceRegistration = configurationService.registerListener(this);
        this.reconciliationManager = Preconditions.checkNotNull(reconciliationManager,
                "ReconciliationManager cannot be null!");
        this.salBundleService = Preconditions.checkNotNull(rpcRegistry.getRpcService(SalBundleService.class),
                "RPC SalBundlService not found.");
        this.routedRpcReg = rpcRegistry.addRoutedRpcImplementation(ArbitratorReconcileService.class,
                this);
    }

    public void start() {
        registerListener();
        reconciliationManager.registerService(this);
        LOG.info("ArbitratorReconciliationManager has started successfully.");
    }

    private void registerListener() {
        final DataTreeIdentifier<ArbitratorConfig> treeId = new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, getWildcardPath());
        // When this config value is set from a file it is not accessible via the yang tree...
        // so we just write it once here just in case.
        setIsArbitratorReconEnabled(isArbitratorReconEnabled.get());
        LOG.trace("Registering on path: {}", treeId);
        registration = dataBroker.registerDataTreeChangeListener(treeId, ArbitratorReconciliationManagerImpl.this);
    }

    private InstanceIdentifier<ArbitratorConfig> getWildcardPath() {
        return InstanceIdentifier.create(ArbitratorConfig.class);
    }

    @Override
    public void close() throws Exception {
        if (this.registration != null) {
            this.registration.close();
            this.registration = null;
        }
        if (this.configurationServiceRegistration != null) {
            this.configurationServiceRegistration.close();
        }
        executor.shutdown();
    }

    @Override
    public ListenableFuture<RpcResult<CommitActiveBundleOutput>> commitActiveBundle(
            CommitActiveBundleInput input) {
        BigInteger nodeId = input.getNodeId();
        if (bundleIdMap.containsKey(nodeId)) {
            BundleId bundleId = bundleIdMap.get(nodeId).getBundleId();
            if (bundleId != null) {
                final ControlBundleInput commitBundleInput = new ControlBundleInputBuilder()
                        .setNode(input.getNode()).setBundleId(bundleId)
                        .setFlags(BUNDLE_FLAGS)
                        .setType(BundleControlType.ONFBCTCOMMITREQUEST).build();
                ListenableFuture<RpcResult<ControlBundleOutput>> rpcResult = JdkFutureAdapters
                        .listenInPoolThread(salBundleService.controlBundle(commitBundleInput));
                bundleIdMap.put(input.getNodeId(), new BundleDetails(bundleId, rpcResult));
                Futures.addCallback(rpcResult, new CommitActiveBundleCallback(nodeId),
                        MoreExecutors.directExecutor());
                return Futures.transform(
                        rpcResult,
                        this.<ControlBundleOutput>createRpcResultCondenser("committed active bundle"),
                        MoreExecutors.directExecutor());
            }
        }
        return RpcResultBuilder.success((new CommitActiveBundleOutputBuilder()
                .setResult(null).build()))
                .withRpcErrors(Collections.singleton(RpcResultBuilder.newError(RpcError.ErrorType.APPLICATION,
                null, "No active bundle found for the node" + nodeId.toString()))).buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<GetActiveBundleOutput>> getActiveBundle(GetActiveBundleInput input) {
        BigInteger nodeId = input.getNodeId();
        BundleDetails bundleDetails = bundleIdMap.get(nodeId);
        if (bundleDetails != null) {
            try {
                //This blocking call is used to prevent the applications from pushing flows and groups via the default
                // pipeline when the commit bundle is ongoing.
                bundleDetails.getResult().get();
                return RpcResultBuilder.success((new GetActiveBundleOutputBuilder()
                        .setResult(bundleDetails.getBundleId()).build())).buildFuture();
            } catch (InterruptedException | ExecutionException | NullPointerException e) {
                return RpcResultBuilder.<GetActiveBundleOutput>failed()
                        .withRpcErrors(Collections.singleton(RpcResultBuilder.newError(RpcError.ErrorType.APPLICATION,
                                null, e.getMessage()))).buildFuture();
            }
        }
        return RpcResultBuilder.success((new GetActiveBundleOutputBuilder()
                .setResult(null).build())).buildFuture();
    }

    @Override
    public ListenableFuture<Boolean> startReconciliation(DeviceInfo node) {
        KeyedInstanceIdentifier<Node, NodeKey> path = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(node.getNodeId()));
        registerRpc(routedRpcReg, path);
        if (isArbitratorReconEnabled.get()) {
            LOG.trace("Starting arbitrator reconciliation for node {}", node.getDatapathId());
            return reconcileConfiguration(node);
        }
        LOG.trace("arbitrator reconciliation is disabled");
        return Futures.immediateFuture(true);
    }

    @Override
    public ListenableFuture<Boolean> endReconciliation(DeviceInfo node) {
        LOG.trace("Stopping arbitrator reconciliation for node {}", node.getDatapathId());
        InstanceIdentifier<FlowCapableNode> connectedNode = node.getNodeInstanceIdentifier()
                .augmentation(FlowCapableNode.class);
        bundleIdMap.remove(connectedNode);
        KeyedInstanceIdentifier<Node, NodeKey> path = InstanceIdentifier.create(Nodes.class).child(Node.class,
                new NodeKey(node.getNodeId()));
        deregisterRpc(routedRpcReg, path);
        return Futures.immediateFuture(true);
    }

    @Override
    public int getPriority() {
        return ARBITRATOR_RECONCILIATION_PRIORITY;
    }

    @Override
    public String getName() {
        return SERVICE_NAME;
    }

    @Override
    public ResultState getResultState() {
        return ResultState.DONOTHING;
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<ArbitratorConfig>> changes) {
        Preconditions.checkNotNull(changes, "Changes may not be null!");
        for (DataTreeModification<ArbitratorConfig> change : changes) {
            final DataObjectModification<ArbitratorConfig> mod = change.getRootNode();
            switch (mod.getModificationType()) {
                case DELETE:
                    LOG.trace("Delete: {}", mod.getDataBefore());
                    remove(mod.getDataBefore());
                    break;
                case SUBTREE_MODIFIED:
                    LOG.trace("Modify: before: {}, after: {}", mod.getDataBefore(), mod.getDataAfter());
                    update(mod.getDataBefore(), mod.getDataAfter());
                    break;
                case WRITE:
                    LOG.trace("write: {}", mod.getDataAfter());
                    add(mod.getDataAfter());
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type " + mod.getModificationType());
            }
        }
    }

    private ListenableFuture<Boolean> reconcileConfiguration(DeviceInfo node) {
        LOG.info("Triggering arbitrator reconciliation for device {}", node.getDatapathId());
        ArbitratorReconciliationTask upgradeReconTask = new ArbitratorReconciliationTask(node);
        return JdkFutureAdapters.listenInPoolThread(executor.submit(upgradeReconTask));
    }

    private Messages createMessages(final NodeRef nodeRef) {
        final List<Message> messages = new ArrayList<>();
        messages.add(new MessageBuilder().setNode(nodeRef).setBundleInnerMessage(
                new BundleRemoveFlowCaseBuilder()
                .setRemoveFlowCaseData(new RemoveFlowCaseDataBuilder(getDeleteAllFlow()).build())
                .build()).build());

        messages.add(new MessageBuilder().setNode(nodeRef).setBundleInnerMessage(
                new BundleRemoveGroupCaseBuilder()
                .setRemoveGroupCaseData(new RemoveGroupCaseDataBuilder(getDeleteAllGroup()).build())
                .build()).build());
        LOG.debug("The size of the flows and group messages created in createMessage() {}", messages.size());
        return new MessagesBuilder().setMessage(messages).build();
    }

    private Flow getDeleteAllFlow() {
        final FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setTableId(OFConstants.OFPTT_ALL);
        return flowBuilder.build();
    }

    private Group getDeleteAllGroup() {
        final GroupBuilder groupBuilder = new GroupBuilder();
        groupBuilder.setGroupType(GroupTypes.GroupAll);
        groupBuilder.setGroupId(new GroupId(OFConstants.OFPG_ALL));
        return groupBuilder.build();
    }

    @Override
    public void onPropertyChanged(@Nonnull String propertyName, @Nonnull String propertyValue) {
        Optional.ofNullable(ArbitraryReconciliationProperty.forValue(propertyName))
            .ifPresent(arbitraryReconciliationProperty -> {
                switch (arbitraryReconciliationProperty) {
                    case ARBITRARY_RECONCILIATION_ENABLED:
                        isArbitratorReconEnabled.set(Boolean.valueOf(propertyValue));
                        setIsArbitratorReconEnabled(isArbitratorReconEnabled.get());
                        break;
                    default:
                        LOG.warn("No arbitrary reconciliation property found.");
                        break;
                }
            });
    }

    private class ArbitratorReconciliationTask implements Callable<Boolean> {
        final DeviceInfo deviceInfo;

        ArbitratorReconciliationTask(final DeviceInfo deviceInfo) {
            this.deviceInfo = deviceInfo;
        }

        @Override
        public Boolean call() {
            InstanceIdentifier<FlowCapableNode> nodeIdentity = deviceInfo.getNodeInstanceIdentifier()
                    .augmentation(FlowCapableNode.class);
            String node = nodeIdentity.firstKeyOf(Node.class, NodeKey.class).getId().getValue();
            BundleId bundleIdValue = new BundleId(BUNDLE_ID.getAndIncrement());
            LOG.debug("Triggering arbitrator reconciliation for device :{}", node);
            final NodeRef nodeRef = new NodeRef(nodeIdentity.firstIdentifierOf(Node.class));

            final ControlBundleInput closeBundleInput = new ControlBundleInputBuilder().setNode(nodeRef)
                    .setBundleId(bundleIdValue).setFlags(BUNDLE_FLAGS)
                    .setType(BundleControlType.ONFBCTCLOSEREQUEST).build();

            final ControlBundleInput openBundleInput = new ControlBundleInputBuilder().setNode(nodeRef)
                    .setBundleId(bundleIdValue).setFlags(BUNDLE_FLAGS)
                    .setType(BundleControlType.ONFBCTOPENREQUEST).build();

            final AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                    .setNode(nodeRef).setBundleId(bundleIdValue).setFlags(BUNDLE_FLAGS)
                    .setMessages(createMessages(nodeRef)).build();

            ListenableFuture<RpcResult<ControlBundleOutput>> closeBundle = salBundleService
                    .controlBundle(closeBundleInput);

            ListenableFuture<RpcResult<ControlBundleOutput>> openBundleMessagesFuture = Futures
                    .transformAsync(closeBundle, rpcResult -> salBundleService
                            .controlBundle(openBundleInput), MoreExecutors.directExecutor());

            ListenableFuture<RpcResult<AddBundleMessagesOutput>> addBundleMessagesFuture = Futures
                    .transformAsync(openBundleMessagesFuture, rpcResult -> {
                        if (rpcResult.isSuccessful()) {
                            return salBundleService
                                    .addBundleMessages(addBundleMessagesInput);
                        }
                        return Futures.immediateFuture(null);
                    }, MoreExecutors.directExecutor());
            BigInteger nodeId = getDpnIdFromNodeName(node);
            try {
                if (addBundleMessagesFuture.get().isSuccessful()) {
                    bundleIdMap.put(nodeId, new BundleDetails(bundleIdValue,
                            Futures.immediateFuture(null)));
                    LOG.debug("Arbitrator reconciliation initial task has been completed for node {} and open up"
                            + " for application programming.", nodeId);
                    return true;
                } else {
                    LOG.error("Error while performing arbitrator reconciliation for device:{}", nodeId);
                    return false;
                }
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Error while performing arbitrator reconciliation for device:{}", nodeId, e);
                return false;
            }
        }
    }

    public final class CommitActiveBundleCallback implements FutureCallback<RpcResult<?>> {
        private final BigInteger nodeId;

        private CommitActiveBundleCallback(final BigInteger nodeId) {
            this.nodeId = nodeId;
        }

        @Override
        public void onSuccess(RpcResult<?> rpcResult) {
            LOG.debug("Completed arbitrator reconciliation for device:{}", nodeId);
            closeActiveBundle(nodeId);
        }

        @Override
        public void onFailure(Throwable throwable) {
            LOG.error("Error while performing arbitrator reconciliation for device {}",
                    nodeId, throwable);
        }
    }

    private void add(@Nonnull ArbitratorConfig newDataObject) {
        isArbitratorReconEnabled.set(newDataObject.isArbitratorReconcileEnabled());
    }

    private void remove(@Nonnull ArbitratorConfig removedDataObject) {
        isArbitratorReconEnabled.set(false);
    }

    private void update(@Nonnull ArbitratorConfig originalDataObject, ArbitratorConfig updatedDataObject) {
        isArbitratorReconEnabled.set(updatedDataObject.isArbitratorReconcileEnabled());
    }

    private BundleDetails closeActiveBundle(BigInteger nodeId) {
        return bundleIdMap.remove(nodeId);
    }

    public static <D> Function<RpcResult<D>,
            RpcResult<CommitActiveBundleOutput>> createRpcResultCondenser(final String action) {
        return input -> {
            final RpcResultBuilder<CommitActiveBundleOutput> resultSink;
            if (input != null) {
                List<RpcError> errors = new ArrayList<>();
                if (!input.isSuccessful()) {
                    errors.addAll(input.getErrors());
                    resultSink = RpcResultBuilder.<CommitActiveBundleOutput>failed().withRpcErrors(errors);
                } else {
                    resultSink = RpcResultBuilder.success();
                }
            } else {
                resultSink = RpcResultBuilder.<CommitActiveBundleOutput>failed()
                        .withError(RpcError.ErrorType.APPLICATION, "action of " + action + " failed");
            }
            return resultSink.build();
        };
    }

    private static void registerRpc(RoutedRpcRegistration routedRpcReg, KeyedInstanceIdentifier<Node, NodeKey> path) {
        LOG.debug("The path is registered : {}", path);
        routedRpcReg.registerPath(NodeContext.class, path);
    }

    private static void deregisterRpc(RoutedRpcRegistration routedRpcReg, KeyedInstanceIdentifier<Node, NodeKey> path) {
        LOG.debug("The path is unregistered : {}", path);
        routedRpcReg.unregisterPath(NodeContext.class, path);
    }

    private static class BundleDetails {
        private final BundleId bundleId;
        private final ListenableFuture<RpcResult<ControlBundleOutput>> result;

        BundleDetails(BundleId bundleId, ListenableFuture<RpcResult<ControlBundleOutput>> result) {
            this.bundleId = bundleId;
            this.result = result;
        }

        public BundleId getBundleId() {
            return bundleId;
        }

        public ListenableFuture<RpcResult<ControlBundleOutput>> getResult() {
            return result;
        }
    }

    private BigInteger getDpnIdFromNodeName(String nodeName) {
        String dpId = nodeName.substring(nodeName.lastIndexOf(SEPARATOR) + 1);
        return new BigInteger(dpId);
    }

    private void setIsArbitratorReconEnabled(boolean isArbitratorReconEnabled) {
        WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.CONFIGURATION, getWildcardPath(),
                new ArbitratorConfigBuilder().setArbitratorReconcileEnabled(isArbitratorReconEnabled).build());
        tx.submit();
    }
}
