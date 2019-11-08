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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.applications.reconciliation.NotificationRegistration;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationNotificationListener;
import org.opendaylight.serviceutils.upgrade.UpgradeState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.remove.flow._case.RemoveFlowCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.remove.group._case.RemoveGroupCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleControlType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.ArbitratorReconcileService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.CommitActiveBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.CommitActiveBundleOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.CommitActiveBundleOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.GetActiveBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.GetActiveBundleOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.GetActiveBundleOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ArbitratorReconciliationManagerImpl implements ArbitratorReconcileService,
        ReconciliationNotificationListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ArbitratorReconciliationManagerImpl.class);
    private static final int THREAD_POOL_SIZE = 4;
    private static final AtomicLong BUNDLE_ID = new AtomicLong();
    private static final BundleFlags BUNDLE_FLAGS = new BundleFlags(true, true);
    private static final int ARBITRATOR_RECONCILIATION_PRIORITY = Integer
            .getInteger("arbitrator.reconciliation.manager.priority", 0/*default*/);
    private static final String SERVICE_NAME = "ArbitratorReconciliationManager";
    private static final String SEPARATOR = ":";

    private static final BundleRemoveFlowCase DELETE_ALL_FLOW = new BundleRemoveFlowCaseBuilder()
            .setRemoveFlowCaseData(
                new RemoveFlowCaseDataBuilder(new FlowBuilder().setTableId(OFConstants.OFPTT_ALL).build()).build())
            .build();
    private static final BundleRemoveGroupCase DELETE_ALL_GROUP = new BundleRemoveGroupCaseBuilder()
            .setRemoveGroupCaseData(new RemoveGroupCaseDataBuilder(new GroupBuilder()
                .setGroupType(GroupTypes.GroupAll)
                .setGroupId(new GroupId(OFConstants.OFPG_ALL))
                .build()).build())
            .build();

    private final SalBundleService salBundleService;
    private final ReconciliationManager reconciliationManager;
    private final RoutedRpcRegistration routedRpcReg;
    private final UpgradeState upgradeState;
    private NotificationRegistration registration;
    private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(
            Executors.newFixedThreadPool(THREAD_POOL_SIZE));
    private final Map<Uint64, BundleDetails> bundleIdMap = new ConcurrentHashMap<>();

    @Inject
    public ArbitratorReconciliationManagerImpl(@Reference RpcProviderRegistry rpcRegistry,
            @Reference ReconciliationManager reconciliationManager, @Reference UpgradeState upgradeState) {
        Preconditions.checkArgument(rpcRegistry != null, "RpcConsumerRegistry cannot be null !");
        this.reconciliationManager = Preconditions.checkNotNull(reconciliationManager,
                "ReconciliationManager cannot be null!");
        this.salBundleService = Preconditions.checkNotNull(rpcRegistry.getRpcService(SalBundleService.class),
                "RPC SalBundlService not found.");
        this.routedRpcReg = rpcRegistry.addRoutedRpcImplementation(ArbitratorReconcileService.class,
                this);
        this.upgradeState = Preconditions.checkNotNull(upgradeState, "UpgradeState cannot be null!");
    }

    @PostConstruct
    public void start() {
        registration = reconciliationManager.registerService(this);
        LOG.info("ArbitratorReconciliationManager has started successfully.");
    }

    @Override
    @PreDestroy
    public void close() throws Exception {
        executor.shutdown();
        if (registration != null) {
            registration.close();
            registration = null;
        }
    }

    @Override
    public ListenableFuture<RpcResult<CommitActiveBundleOutput>> commitActiveBundle(
            CommitActiveBundleInput input) {
        Uint64 nodeId = input.getNodeId();
        if (bundleIdMap.containsKey(nodeId)) {
            BundleId bundleId = bundleIdMap.get(nodeId).getBundleId();
            if (bundleId != null) {
                final ControlBundleInput commitBundleInput = new ControlBundleInputBuilder()
                        .setNode(input.getNode()).setBundleId(bundleId)
                        .setFlags(BUNDLE_FLAGS)
                        .setType(BundleControlType.ONFBCTCOMMITREQUEST).build();
                ListenableFuture<RpcResult<ControlBundleOutput>> rpcResult = salBundleService
                        .controlBundle(commitBundleInput);
                bundleIdMap.put(nodeId, new BundleDetails(bundleId, rpcResult));
                Futures.addCallback(rpcResult, new CommitActiveBundleCallback(nodeId),
                        MoreExecutors.directExecutor());
                return Futures.transform(
                        rpcResult,
                        this.createRpcResultCondenser("committed active bundle"),
                        MoreExecutors.directExecutor());
            }
        }
        return RpcResultBuilder.success(new CommitActiveBundleOutputBuilder()
                .setResult(null).build())
                .withRpcErrors(Collections.singleton(RpcResultBuilder.newError(RpcError.ErrorType.APPLICATION,
                null, "No active bundle found for the node" + nodeId.toString()))).buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<GetActiveBundleOutput>> getActiveBundle(GetActiveBundleInput input) {
        Uint64 nodeId = input.getNodeId();
        BundleDetails bundleDetails = bundleIdMap.get(nodeId);
        if (bundleDetails != null) {
            try {
                //This blocking call is used to prevent the applications from pushing flows and groups via the default
                // pipeline when the commit bundle is ongoing.
                bundleDetails.getResult().get();
                return RpcResultBuilder.success(new GetActiveBundleOutputBuilder()
                        .setResult(bundleDetails.getBundleId()).build()).buildFuture();
            } catch (InterruptedException | ExecutionException | NullPointerException e) {
                return RpcResultBuilder.<GetActiveBundleOutput>failed()
                        .withRpcErrors(Collections.singleton(RpcResultBuilder.newError(RpcError.ErrorType.APPLICATION,
                                null, e.getMessage()))).buildFuture();
            }
        }
        return RpcResultBuilder.success(new GetActiveBundleOutputBuilder()
                .setResult(null).build()).buildFuture();
    }

    @Override
    public ListenableFuture<Boolean> startReconciliation(DeviceInfo node) {
        registerRpc(node);
        if (upgradeState.isUpgradeInProgress()) {
            LOG.trace("Starting arbitrator reconciliation for node {}", node.getDatapathId());
            return reconcileConfiguration(node);
        }
        LOG.trace("arbitrator reconciliation is disabled");
        return FluentFutures.immediateTrueFluentFuture();
    }

    @Override
    public ListenableFuture<Boolean> endReconciliation(DeviceInfo node) {
        Uint64 datapathId = node.getDatapathId();
        LOG.trace("Stopping arbitrator reconciliation for node {}", datapathId);
        bundleIdMap.remove(datapathId);
        deregisterRpc(node);
        return FluentFutures.immediateTrueFluentFuture();
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

    private ListenableFuture<Boolean> reconcileConfiguration(DeviceInfo node) {
        LOG.info("Triggering arbitrator reconciliation for device {}", node.getDatapathId());
        ArbitratorReconciliationTask upgradeReconTask = new ArbitratorReconciliationTask(node);
        return executor.submit(upgradeReconTask);
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "https://github.com/spotbugs/spotbugs/issues/811")
    private Messages createMessages(final NodeRef nodeRef) {
        final List<Message> messages = new ArrayList<>();
        messages.add(new MessageBuilder().setNode(nodeRef).setBundleInnerMessage(DELETE_ALL_FLOW).build());
        messages.add(new MessageBuilder().setNode(nodeRef).setBundleInnerMessage(DELETE_ALL_GROUP).build());
        LOG.debug("The size of the flows and group messages created in createMessage() {}", messages.size());
        return new MessagesBuilder().setMessage(messages).build();
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
            String node = nodeIdentity.firstKeyOf(Node.class).getId().getValue();
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
                        return FluentFutures.immediateNullFluentFuture();
                    }, MoreExecutors.directExecutor());
            Uint64 nodeId = getDpnIdFromNodeName(node);
            try {
                if (addBundleMessagesFuture.get().isSuccessful()) {
                    bundleIdMap.put(nodeId, new BundleDetails(bundleIdValue,
                        FluentFutures.immediateNullFluentFuture()));
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
        private final Uint64 nodeId;

        private CommitActiveBundleCallback(final Uint64 nodeId) {
            this.nodeId = nodeId;
        }

        @Override
        public void onSuccess(RpcResult<?> rpcResult) {
            LOG.debug("Completed arbitrator reconciliation for device:{}", nodeId);
            bundleIdMap.remove(nodeId);
        }

        @Override
        public void onFailure(Throwable throwable) {
            LOG.error("Error while performing arbitrator reconciliation for device {}", nodeId, throwable);
        }
    }

    private <D> Function<RpcResult<D>,
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

    private void registerRpc(DeviceInfo node) {
        KeyedInstanceIdentifier<Node, NodeKey> path = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(node.getNodeId()));
        LOG.debug("The path is registered : {}", path);
        routedRpcReg.registerPath(NodeContext.class, path);
    }

    private void deregisterRpc(DeviceInfo node) {
        KeyedInstanceIdentifier<Node, NodeKey> path = InstanceIdentifier.create(Nodes.class).child(Node.class,
                new NodeKey(node.getNodeId()));
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

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "https://github.com/spotbugs/spotbugs/issues/811")
    private Uint64 getDpnIdFromNodeName(String nodeName) {
        String dpnId = nodeName.substring(nodeName.lastIndexOf(SEPARATOR) + 1);
        return Uint64.valueOf(dpnId);
    }
}
