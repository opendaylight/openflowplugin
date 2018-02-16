package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.applications.frm.UpgradeReconciliation;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.*;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.upgrade.state.rev180215.OpenflowUpgradeState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class UpgradeReconciliationImpl implements ClusteredDataTreeChangeListener<OpenflowUpgradeState>, UpgradeReconciliation {

        private static final Logger LOG = LoggerFactory.getLogger(UpgradeReconciliationImpl.class);

        private final DataBroker dataBroker;
        private final ForwardingRulesManager provider;
        private final String serviceName;
        private final int priority;
        private final ResultState resultState;
        private final SalBundleService salBundleService;
        private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        private static final String SEPARATOR = ":";
        private static final int THREAD_POOL_SIZE = 4;

        private ListenerRegistration<UpgradeReconciliationImpl> registration;

        private OpenflowUpgradeState.UpgradeState isUpgrade;
        private final Map<InstanceIdentifier<FlowCapableNode>, BundleId> bundleIdMap = new HashMap<>();

        private static final AtomicLong BUNDLE_ID = new AtomicLong();

        private static final BundleFlags BUNDLE_FLAGS = new BundleFlags(true, true);

        public UpgradeReconciliationImpl(ForwardingRulesManager forwardingRulesManager, DataBroker dataService,
                String upgrade, int i, ResultState donothing) {
                this.provider = Preconditions.checkNotNull(forwardingRulesManager, "ForwardingRulesManager can not be null!");
                this.dataBroker = Preconditions.checkNotNull(dataService, "DataBroker can not be null!");
                this.serviceName = upgrade;
                this.priority = i;
                this.resultState = donothing;
                salBundleService = Preconditions.checkNotNull(provider.getSalBundleService(),
                        "salBundleService can not be null!");
                registerListener();

        }

        private void registerListener() {
                final DataTreeIdentifier<OpenflowUpgradeState> treeId =
                        new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, getWildcardPath());
                LOG.trace("Registering on path: {}", treeId);
                registration = dataBroker.registerDataTreeChangeListener(treeId, UpgradeReconciliationImpl.this);
        }

        protected InstanceIdentifier<OpenflowUpgradeState> getWildcardPath() {
                return InstanceIdentifier.create(OpenflowUpgradeState.class);
        }

        @Override public ListenableFuture<Boolean> startReconciliation(DeviceInfo node) {
                InstanceIdentifier<FlowCapableNode> connectedNode = node.getNodeInstanceIdentifier()
                        .augmentation(FlowCapableNode.class);
                return  reconcileConfiguration(connectedNode);        }

        private ListenableFuture<Boolean> reconcileConfiguration(InstanceIdentifier<FlowCapableNode> connectedNode) {
                LOG.info("Triggering upgradereconciliation for device {}", connectedNode.firstKeyOf(Node.class));
                if (provider.isStaleMarkingEnabled()) {
                        LOG.info("Stale-Marking is ENABLED and proceeding with deletion of " + "stale-marked entities on switch {}",
                                connectedNode.toString());
                }
                UpgradeReconciliationTask UpgradeReconTask = new UpgradeReconciliationTask(connectedNode);
                return JdkFutureAdapters.listenInPoolThread(executor.submit(UpgradeReconTask));

        }
        private BigInteger getDpnIdFromNodeName(String nodeName) {

                String dpId = nodeName.substring(nodeName.lastIndexOf(SEPARATOR) + 1);
                return new BigInteger(dpId);
        }

        public Map<InstanceIdentifier<FlowCapableNode>, BundleId> getBundleIdMap() {
                return bundleIdMap;
        }

        @Override public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<OpenflowUpgradeState>> changes) {
                {
                        Preconditions.checkNotNull(changes, "Changes may not be null!");

                        for (DataTreeModification<OpenflowUpgradeState> change : changes) {
                                final InstanceIdentifier<OpenflowUpgradeState> key = change.getRootPath().getRootIdentifier();
                                final DataObjectModification<OpenflowUpgradeState> mod = change.getRootNode();

                                switch (mod.getModificationType()) {
                                case DELETE:
                                        LOG.info("delete is called");
                                        isUpgrade = OpenflowUpgradeState.UpgradeState.ENDUPGRADE;
                                        break;
                                case SUBTREE_MODIFIED:
                                        break;
                                case WRITE:
                                        LOG.info("write is called");
                                        isUpgrade = mod.getDataAfter().getUpgradeState();
                                        if (isUpgrade == OpenflowUpgradeState.UpgradeState.ENDUPGRADE) {
                                                for (Map.Entry<InstanceIdentifier<FlowCapableNode>, BundleId> entry : bundleIdMap.entrySet()) {
                                                        InstanceIdentifier<FlowCapableNode> nodeIdentity = entry.getKey();
                                                        String node = nodeIdentity.firstKeyOf(Node.class, NodeKey.class).getId().getValue();
                                                        BigInteger dpnId = getDpnIdFromNodeName(node);
                                                        final NodeRef nodeRef = new NodeRef(nodeIdentity.firstIdentifierOf(Node.class));
                                                        BundleId bundleId = entry.getValue();
                                                        final ControlBundleInput commitBundleInput = new ControlBundleInputBuilder().setNode(nodeRef)
                                                                .setBundleId(bundleId).setFlags(BUNDLE_FLAGS)
                                                                .setType(BundleControlType.ONFBCTCOMMITREQUEST).build();
                                                        ListenableFuture<RpcResult<Void>> commitBundleFuture = JdkFutureAdapters
                                                                                        .listenInPoolThread(salBundleService.controlBundle(commitBundleInput));
                                                        try {
                                                                if (commitBundleFuture.get().isSuccessful()) {
                                                                        LOG.debug("Completing bundle based reconciliation for device ID:{}", dpnId);
                                                                } else {
                                                                }
                                                        } catch (InterruptedException | ExecutionException e) {
                                                                LOG.error("Error while doing bundle based reconciliation for device ID:{}", nodeIdentity);
                                                        }
                                                }

                                                }

                                        break;
                                default:
                                        throw new IllegalArgumentException("Unhandled modification type " + mod.getModificationType());
                                }
                        }
                }
        }

        private Messages createMessages(final NodeRef nodeRef, final Optional<FlowCapableNode> flowNode) {
                final List<Message> messages = new ArrayList<>();
                messages.add(new MessageBuilder().setNode(nodeRef)
                        .setBundleInnerMessage(new BundleRemoveFlowCaseBuilder()
                                .setRemoveFlowCaseData(new RemoveFlowCaseDataBuilder(getDeleteAllFlow()).build()).build())
                        .build());

                messages.add(new MessageBuilder().setNode(nodeRef)
                        .setBundleInnerMessage(new BundleRemoveGroupCaseBuilder()
                                .setRemoveGroupCaseData(new RemoveGroupCaseDataBuilder(getDeleteAllGroup()).build()).build())
                        .build());
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

                @Override public Boolean call() {
                        String node = nodeIdentity.firstKeyOf(Node.class, NodeKey.class).getId().getValue();
                        Optional<FlowCapableNode> flowNode = Optional.absent();
                        BundleId bundleIdValue = new BundleId(BUNDLE_ID.getAndIncrement());
                        BigInteger dpnId = getDpnIdFromNodeName(node);
                        LOG.debug("Triggering upgrade based reconciliation for device :{}", dpnId);
                        ReadOnlyTransaction trans = provider.getReadTranaction();
                        try {
                                flowNode = trans.read(LogicalDatastoreType.CONFIGURATION, nodeIdentity).get();
                        } catch (ExecutionException | InterruptedException e) {
                                LOG.error("Error occurred while reading the configuration data store for node {}",
                                        nodeIdentity, e);
                        }

                        final NodeRef nodeRef = new NodeRef(nodeIdentity.firstIdentifierOf(Node.class));

                        final ControlBundleInput openBundleInput = new ControlBundleInputBuilder().setNode(nodeRef)
                                .setBundleId(bundleIdValue).setFlags(BUNDLE_FLAGS).setType(BundleControlType.ONFBCTOPENREQUEST).build();

                        final AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                                .setNode(nodeRef).setBundleId(bundleIdValue).setFlags(BUNDLE_FLAGS)
                                .setMessages(createMessages(nodeRef, flowNode)).build();

                        Future<RpcResult<Void>> openBundle = salBundleService.controlBundle(openBundleInput);

                        ListenableFuture<RpcResult<Void>> addBundleMessagesFuture = Futures
                                .transformAsync(JdkFutureAdapters.listenInPoolThread(openBundle), rpcResult -> {
                                        if (rpcResult.isSuccessful()) {
                                                return JdkFutureAdapters.listenInPoolThread(salBundleService.addBundleMessages(addBundleMessagesInput));
                                        }
                                        return Futures.immediateFuture(null);
                                }, MoreExecutors.directExecutor());

                        trans.close();
                        try {
                                if (addBundleMessagesFuture.get().isSuccessful()) {
                                        LOG.debug("Completing bundle based reconciliation for device ID:{}", dpnId);
                                        bundleIdMap.put(nodeIdentity, bundleIdValue);
                                        return true;
                                } else {
                                        return false;
                                }
                        } catch (InterruptedException | ExecutionException e) {
                                LOG.error("Error while doing bundle based reconciliation for device ID:{}", nodeIdentity);
                                return false;
                        }
                }
        }

        @Override public ListenableFuture<Boolean> endReconciliation(DeviceInfo node) {
                return null;
        }

        @Override public int getPriority() {
                return priority;
        }

        @Override public String getName() {
                return serviceName;
        }

        @Override public ResultState getResultState() {
                return resultState;
        }

        @Override public void close() throws Exception {
                if(this.registration != null) {
                        this.registration.close();
                        this.registration = null;
                }
        }
}
