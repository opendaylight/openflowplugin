/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.openflowplugin.applications.frm.util.FrmUtil.getNodeIdValueFromNodeIdentifier;
import static org.opendaylight.openflowplugin.applications.frm.util.FrmUtil.isGroupExistsOnDevice;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.infrautils.utils.concurrent.LoggingFutures;
import org.opendaylight.openflowplugin.applications.frm.BundleMessagesCommiter;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.applications.frm.NodeConfigurator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.MessagesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.Message;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.MessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.BundleInnerMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleAddGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleUpdateGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.add.group._case.AddGroupCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.remove.group._case.RemoveGroupCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.update.group._case.UpdateGroupCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleGroupForwarder implements BundleMessagesCommiter<Group> {

    private static final Logger LOG = LoggerFactory.getLogger(BundleGroupForwarder.class);
    private static final BundleFlags BUNDLE_FLAGS = new BundleFlags(true, true);
    private final ForwardingRulesManager forwardingRulesManager;
    private final NodeConfigurator nodeConfigurator;

    public BundleGroupForwarder(final ForwardingRulesManager forwardingRulesManager) {
        this.forwardingRulesManager = requireNonNull(forwardingRulesManager, "ForwardingRulesManager can not be null!");
        this.nodeConfigurator = requireNonNull(forwardingRulesManager.getNodeConfigurator(),
                "NodeConfigurator can not be null!");
    }

    @Override
    public void remove(final InstanceIdentifier<Group> identifier, final Group group,
            final InstanceIdentifier<FlowCapableNode> nodeIdent, final BundleId bundleId) {
        final String nodeId = getNodeIdValueFromNodeIdentifier(nodeIdent);
        nodeConfigurator.enqueueJob(nodeId, () -> {
            final List<Message> messages = new ArrayList<>(1);
            BundleInnerMessage bundleInnerMessage = new BundleRemoveGroupCaseBuilder()
                    .setRemoveGroupCaseData(new RemoveGroupCaseDataBuilder(group).build())
                    .build();
            Message message = new MessageBuilder()
                    .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)))
                    .setBundleInnerMessage(bundleInnerMessage)
                    .build();
            messages.add(message);
            AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                    .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)))
                    .setBundleId(bundleId)
                    .setFlags(BUNDLE_FLAGS)
                    .setMessages(new MessagesBuilder().setMessage(messages).build())
                    .build();
            LOG.trace("Pushing group remove message {} to bundle {} for device {}", addBundleMessagesInput,
                    bundleId.getValue(), nodeId);
            final ListenableFuture<RpcResult<AddBundleMessagesOutput>> resultFuture = forwardingRulesManager
                    .getSalBundleService().addBundleMessages(addBundleMessagesInput);
            Futures.addCallback(resultFuture,
                    new BundleRemoveGroupCallBack(group.getGroupId().getValue(), nodeId),
                    MoreExecutors.directExecutor());
            LoggingFutures.addErrorLogging(resultFuture, LOG, "removeBundleGroup");
            return resultFuture;
        });

    }

    @Override
    public void update(final InstanceIdentifier<Group> identifier,
                       final Group originalGroup,
                       final Group updatedGroup,
                        final InstanceIdentifier<FlowCapableNode> nodeIdent,
                       final BundleId bundleId) {
        final String nodeId = getNodeIdValueFromNodeIdentifier(nodeIdent);
        nodeConfigurator.enqueueJob(nodeId, () -> {
            final List<Message> messages = new ArrayList<>(1);
            BundleInnerMessage bundleInnerMessage = new BundleUpdateGroupCaseBuilder()
                    .setUpdateGroupCaseData(new UpdateGroupCaseDataBuilder(updatedGroup).build())
                    .build();
            Message message = new MessageBuilder()
                    .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)))
                    .setBundleInnerMessage(bundleInnerMessage)
                    .build();
            messages.add(message);
            AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                    .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)))
                    .setBundleId(bundleId)
                    .setFlags(BUNDLE_FLAGS)
                    .setMessages(new MessagesBuilder().setMessage(messages).build())
                    .build();
            LOG.trace("Pushing group update message {} to bundle {} for device {}", addBundleMessagesInput,
                    bundleId.getValue(), nodeId);
            final ListenableFuture<RpcResult<AddBundleMessagesOutput>> resultFuture = forwardingRulesManager
                    .getSalBundleService()
                    .addBundleMessages(addBundleMessagesInput);
            Futures.addCallback(resultFuture,
                    new BundleUpdateGroupCallBack(originalGroup.getGroupId().getValue(), nodeId),
                    MoreExecutors.directExecutor());
            LoggingFutures.addErrorLogging(resultFuture, LOG, "updateBundleGroup");
            return resultFuture;
        });
    }

    @Override
    public ListenableFuture<RpcResult<AddBundleMessagesOutput>> add(final InstanceIdentifier<Group> identifier,
                                                                    final Group group,
                                                                    final InstanceIdentifier<FlowCapableNode> nodeIdent,
                                                                    final BundleId bundleId) {
        final String nodeId = getNodeIdValueFromNodeIdentifier(nodeIdent);
        final Uint32 groupId = group.getGroupId().getValue();
        return nodeConfigurator.enqueueJob(nodeId, () -> {
            if (isGroupExistsOnDevice(nodeIdent, groupId, forwardingRulesManager)) {
                LOG.debug("Group {} already exists in the device. Ignoring the add DTCN", groupId);
                return Futures.immediateFuture(RpcResultBuilder.<AddBundleMessagesOutput>success().build());
            }
            final List<Message> messages = new ArrayList<>(1);
            BundleInnerMessage bundleInnerMessage = new BundleAddGroupCaseBuilder()
                    .setAddGroupCaseData(new AddGroupCaseDataBuilder(group).build())
                    .build();
            Message message = new MessageBuilder()
                    .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)))
                    .setBundleInnerMessage(bundleInnerMessage)
                    .build();
            messages.add(message);
            AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                    .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)))
                    .setBundleId(bundleId)
                    .setFlags(BUNDLE_FLAGS)
                    .setMessages(new MessagesBuilder()
                            .setMessage(messages).build())
                    .build();
            LOG.trace("Pushing group add message {} to bundle {} for device {}", addBundleMessagesInput,
                    bundleId.getValue(), nodeId);
            ListenableFuture<RpcResult<AddBundleMessagesOutput>> resultFuture = forwardingRulesManager
                    .getSalBundleService()
                    .addBundleMessages(addBundleMessagesInput);
            Futures.addCallback(resultFuture,
                    new BundleAddGroupCallBack(groupId, nodeId),
                    MoreExecutors.directExecutor());
            return resultFuture;
        });
    }

    private final class BundleAddGroupCallBack implements FutureCallback<RpcResult<AddBundleMessagesOutput>> {
        private final Uint32 groupId;
        private final String nodeId;

        private BundleAddGroupCallBack(final Uint32 groupId, final String nodeId) {
            this.groupId = groupId;
            this.nodeId = nodeId;
        }

        @Override
        public void onSuccess(final RpcResult<AddBundleMessagesOutput> result) {
            if (result.isSuccessful()) {
                forwardingRulesManager.getDevicesGroupRegistry().storeGroup(nodeId, groupId);
                LOG.debug("Group add with id {} finished without error for node {}", groupId, nodeId);
            } else {
                LOG.error("Group add with id {} failed for node {} with error: {}", groupId, nodeId,
                        result.getErrors().toString());
            }
        }

        @Override
        public void onFailure(final Throwable throwable) {
            LOG.error("Service call for adding group {} failed for node {} with error ", groupId, nodeId, throwable);
        }
    }

    private final class BundleUpdateGroupCallBack implements FutureCallback<RpcResult<AddBundleMessagesOutput>> {
        private final Uint32 groupId;
        private final String nodeId;

        private BundleUpdateGroupCallBack(final Uint32 groupId, final String nodeId) {
            this.groupId = groupId;
            this.nodeId = nodeId;
        }

        @Override
        public void onSuccess(final RpcResult<AddBundleMessagesOutput> result) {
            if (result.isSuccessful()) {
                forwardingRulesManager.getDevicesGroupRegistry().storeGroup(nodeId, groupId);
                LOG.debug("Group update with id {} finished without error for node {}", groupId, nodeId);
            } else {
                LOG.error("Group update with id {} failed for node {} with error: {}", groupId, nodeId,
                        result.getErrors().toString());
            }
        }

        @Override
        public void onFailure(final Throwable throwable) {
            LOG.error("Service call for updating group {} failed for node {}", groupId, nodeId, throwable);
        }
    }

    private final class BundleRemoveGroupCallBack implements FutureCallback<RpcResult<AddBundleMessagesOutput>> {
        private final Uint32 groupId;
        private final String nodeId;

        private BundleRemoveGroupCallBack(final Uint32 groupId, final String nodeId) {
            this.groupId = groupId;
            this.nodeId = nodeId;
        }

        @Override
        public void onSuccess(final RpcResult<AddBundleMessagesOutput> result) {
            if (result.isSuccessful()) {
                LOG.debug("Group remove with id {} finished without error for node {}", groupId, nodeId);
                forwardingRulesManager.getDevicesGroupRegistry().removeGroup(nodeId, groupId);
            } else {
                LOG.error("Group remove with id {} failed for node {} with error {}", groupId, nodeId,
                        result.getErrors().toString());
            }
        }

        @Override
        public void onFailure(final Throwable throwable) {
            LOG.error("Service call for removing group {} failed for node {} with error", groupId, nodeId, throwable);
        }
    }
}
