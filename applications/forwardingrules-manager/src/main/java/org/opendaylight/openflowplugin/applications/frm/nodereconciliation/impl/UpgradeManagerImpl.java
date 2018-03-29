/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.nodereconciliation.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.applications.frm.nodereconciliation.api.UpgradeManager;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInputBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.upgrade.rev180328.UpgradeProgress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpgradeManagerImpl implements ClusteredDataTreeChangeListener<UpgradeProgress>, UpgradeManager {

    private static final Logger LOG = LoggerFactory.getLogger(UpgradeManagerImpl.class);
    private static final String SEPARATOR = ":";
    private static final int THREAD_POOL_SIZE = 4;
    private static final AtomicLong BUNDLE_ID = new AtomicLong();
    private static final BundleFlags BUNDLE_FLAGS = new BundleFlags(true, true);
    private static final int UPGRADEMANAGER_RECONCILIATION_PRIORITY = Integer
            .getInteger("upgrademgr.reconciliation.priority", 1/*default*/);
    private static final String SERVICE_NAME = "UpradeManager";

    private final SalBundleService salBundleService;
    private final DataBroker dataBroker;
    private final ReconciliationManager reconciliationManager;
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private final Map<InstanceIdentifier<FlowCapableNode>, BundleId> bundleIdMap = new HashMap<>();
    private ListenerRegistration<UpgradeManagerImpl> registration;
    private boolean upgradeInProgress = false;

    public UpgradeManagerImpl(final DataBroker dataService, final RpcConsumerRegistry rpcRegistry,
            final ReconciliationManager reconciliationManager) {
        this.dataBroker = Preconditions.checkNotNull(dataService, "DataBroker cannot be null!");
        Preconditions.checkArgument(rpcRegistry != null, "RpcConsumerRegistry cannot be null !");
        this.reconciliationManager = Preconditions.checkNotNull(reconciliationManager,
                "ReconciliationManager cannot be null!");
        this.salBundleService = Preconditions.checkNotNull(rpcRegistry.getRpcService(SalBundleService.class),
                "RPC SalBundlService not found.");
    }

    @Override
    public void start() {
        registerListener();
        reconciliationManager.registerService(this);
        LOG.info("UpgradeManager has started successfully.");
    }

    @Override
    public void close() throws Exception {
        if (this.registration != null) {
            this.registration.close();
            this.registration = null;
        }
    }

    @Override
    public BundleId getActiveBundle(InstanceIdentifier<FlowCapableNode> node) {
        return bundleIdMap.get(node);
    }

    @Override
    public Future<RpcResult<?>> commitActiveBundle(InstanceIdentifier<FlowCapableNode> node) {
        BundleId bundleId = bundleIdMap.get(node);
        final NodeRef nodeRef = new NodeRef(node.firstIdentifierOf(Node.class));
        final ControlBundleInput commitBundleInput = new ControlBundleInputBuilder()
                .setNode(nodeRef).setBundleId(bundleId)
                .setFlags(BUNDLE_FLAGS)
                .setType(BundleControlType.ONFBCTCOMMITREQUEST).build();
        Futures.addCallback(JdkFutureAdapters
                .listenInPoolThread(salBundleService.controlBundle(commitBundleInput)),
                new UpgradeManagerCallback(node));
        return null;
    }

    @Override
    public ListenableFuture<Boolean> startReconciliation(DeviceInfo node) {
        InstanceIdentifier<FlowCapableNode> connectedNode = node.getNodeInstanceIdentifier()
                .augmentation(FlowCapableNode.class);
        return reconcileConfiguration(connectedNode);
    }

    @Override
    public ListenableFuture<Boolean> endReconciliation(DeviceInfo node) {
        InstanceIdentifier<FlowCapableNode> connectedNode = node.getNodeInstanceIdentifier()
                .augmentation(FlowCapableNode.class);
        bundleIdMap.remove(connectedNode);
        return Futures.immediateFuture(true);
    }

    @Override
    public int getPriority() {
        return UPGRADEMANAGER_RECONCILIATION_PRIORITY;
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
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<UpgradeProgress>> changes) {
        Preconditions.checkNotNull(changes, "Changes may not be null!");
        for (DataTreeModification<UpgradeProgress> change : changes) {
            final DataObjectModification<UpgradeProgress> mod = change.getRootNode();
            switch (mod.getModificationType()) {
                case DELETE:
                    break;
                case SUBTREE_MODIFIED:
                    break;
                case WRITE:
                    upgradeInProgress = mod.getDataAfter().isUpgradeInProgress();
                    break;
                default:
                    throw new IllegalArgumentException(
                        "Unhandled modification type " + mod.getModificationType());
            }
        }
    }

    private void registerListener() {
        final DataTreeIdentifier<UpgradeProgress> treeId = new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, getWildcardPath());
        LOG.trace("Registering on path: {}", treeId);
        registration = dataBroker.registerDataTreeChangeListener(treeId, UpgradeManagerImpl.this);
    }

    private InstanceIdentifier<UpgradeProgress> getWildcardPath() {
        return InstanceIdentifier.create(UpgradeProgress.class);
    }

    private ListenableFuture<Boolean> reconcileConfiguration(InstanceIdentifier<FlowCapableNode> connectedNode) {
        LOG.info("Triggering upgradeReconciliation for device {}", connectedNode.firstKeyOf(Node.class));
        UpgradeReconciliationTask upgradeReconTask = new UpgradeReconciliationTask(connectedNode);
        return JdkFutureAdapters.listenInPoolThread(executor.submit(upgradeReconTask));
    }

    private Messages createMessages(final NodeRef nodeRef, final Optional<FlowCapableNode> flowNode) {
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

    private class UpgradeReconciliationTask implements Callable<Boolean> {
        final InstanceIdentifier<FlowCapableNode> nodeIdentity;

        UpgradeReconciliationTask(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
            nodeIdentity = nodeIdent;
        }

        @Override
        public Boolean call() {
            if (upgradeInProgress == false) {
                return true;
            }
            String node = nodeIdentity.firstKeyOf(Node.class, NodeKey.class).getId().getValue();
            Optional<FlowCapableNode> flowNode = Optional.absent();
            Long dpnId = getDpnIdFromNodeName(node);
            BundleId bundleIdValue = new BundleId(BUNDLE_ID.getAndIncrement());
            LOG.debug("Triggering upgrade based reconciliation for device :{}", dpnId);
            ReadOnlyTransaction trans = dataBroker.newReadOnlyTransaction();
            try {
                flowNode = trans.read(LogicalDatastoreType.CONFIGURATION, nodeIdentity).get();
            } catch (ExecutionException | InterruptedException e) {
                LOG.error("Error occurred while reading the configuration data store for node {}",
                        nodeIdentity, e);
            }

            final NodeRef nodeRef = new NodeRef(nodeIdentity.firstIdentifierOf(Node.class));

            final ControlBundleInput closeBundleInput = new ControlBundleInputBuilder().setNode(nodeRef)
                    .setBundleId(bundleIdValue).setFlags(BUNDLE_FLAGS)
                    .setType(BundleControlType.ONFBCTCLOSEREQUEST).build();

            final ControlBundleInput openBundleInput = new ControlBundleInputBuilder().setNode(nodeRef)
                    .setBundleId(bundleIdValue).setFlags(BUNDLE_FLAGS)
                    .setType(BundleControlType.ONFBCTOPENREQUEST).build();

            final AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                    .setNode(nodeRef).setBundleId(bundleIdValue).setFlags(BUNDLE_FLAGS)
                    .setMessages(createMessages(nodeRef, flowNode)).build();

            Future<RpcResult<Void>> closeBundle = salBundleService.controlBundle(closeBundleInput);

            ListenableFuture<RpcResult<Void>> openBundleMessagesFuture = Futures
                    .transformAsync(JdkFutureAdapters.listenInPoolThread(closeBundle), rpcResult -> {
                        return JdkFutureAdapters.listenInPoolThread(
                                salBundleService.controlBundle(openBundleInput));
                    }, MoreExecutors.directExecutor());


            ListenableFuture<RpcResult<Void>> addBundleMessagesFuture = Futures
                    .transformAsync(JdkFutureAdapters.listenInPoolThread(openBundleMessagesFuture), rpcResult -> {
                        if (rpcResult.isSuccessful()) {
                            return JdkFutureAdapters.listenInPoolThread(
                                    salBundleService.addBundleMessages(addBundleMessagesInput));
                        }
                        return Futures.immediateFuture(null);
                    }, MoreExecutors.directExecutor());

            trans.close();
            try {
                if (addBundleMessagesFuture.get().isSuccessful()) {
                    LOG.debug("Performing Upgrade reconciliation for device:{}", dpnId);
                    bundleIdMap.put(nodeIdentity, bundleIdValue);
                    return true;
                } else {
                    return false;
                }
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Error while performing Upgrade reconciliation for device:{}",
                        nodeIdentity);
                return false;
            }
        }
    }

    private final class UpgradeManagerCallback implements FutureCallback<RpcResult<?>> {
        private final InstanceIdentifier<FlowCapableNode> nodeIdentity;

        private UpgradeManagerCallback(final InstanceIdentifier<FlowCapableNode> nodeIdentity) {
            this.nodeIdentity = nodeIdentity;
        }

        @Override
        public void onSuccess(RpcResult<?> rpcResult) {
            LOG.debug("Completed upgrade reconciliation for device:{}", getDpnIdFromFlowCapableNode(nodeIdentity));
            bundleIdMap.remove(nodeIdentity);
        }

        @Override
        public void onFailure(Throwable throwable) {
            LOG.error("Error {} while performing Upgrade reconciliation for device {}", throwable,
                    getDpnIdFromFlowCapableNode(nodeIdentity));
        }
    }

    private Long getDpnIdFromNodeName(String nodeName) {
        String dpId = nodeName.substring(nodeName.lastIndexOf(SEPARATOR) + 1);
        return Long.valueOf(dpId);
    }

    private Long getDpnIdFromFlowCapableNode(InstanceIdentifier<FlowCapableNode> nodeIdentity) {
        return getDpnIdFromNodeName(nodeIdentity.firstKeyOf(Node.class, NodeKey.class).getId().getValue());
    }
}
