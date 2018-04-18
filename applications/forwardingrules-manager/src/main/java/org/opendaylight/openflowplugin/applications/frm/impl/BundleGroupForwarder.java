/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.infrautils.utils.concurrent.JdkFutures;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleGroupForwarder {

    private static final Logger LOG = LoggerFactory.getLogger(BundleGroupForwarder.class);
    private static final BundleFlags BUNDLE_FLAGS = new BundleFlags(true, true);
    private final ForwardingRulesManager forwardingRulesManager;

    public BundleGroupForwarder(final ForwardingRulesManager forwardingRulesManager) {
        this.forwardingRulesManager = Preconditions.checkNotNull(forwardingRulesManager,
                "ForwardingRulesManager can not be null!");
    }

    public void remove(final InstanceIdentifier<Group> identifier, final Group group,
            final InstanceIdentifier<FlowCapableNode> nodeIdent, final BundleId bundleId) {
        final List<Message> messages = new ArrayList<>(1);
        String node = nodeIdent.firstKeyOf(Node.class, NodeKey.class).getId().getValue();
        BundleInnerMessage bundleInnerMessage = new BundleRemoveGroupCaseBuilder()
                .setRemoveGroupCaseData(new RemoveGroupCaseDataBuilder(group).build()).build();
        Message message = new MessageBuilder().setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)))
                .setBundleInnerMessage(bundleInnerMessage).build();
        messages.add(message);
        AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class))).setBundleId(bundleId)
                .setFlags(BUNDLE_FLAGS).setMessages(new MessagesBuilder().setMessage(messages).build()).build();
        final ListenableFuture<RpcResult<AddBundleMessagesOutput>> resultFuture = forwardingRulesManager
                .getSalBundleService().addBundleMessages(addBundleMessagesInput);
        LOG.trace("Pushing group remove message {} to bundle {} for device {}", addBundleMessagesInput,
                bundleId.getValue(), node);
        JdkFutures.addErrorLogging(resultFuture, LOG, "removeBundleGroup");
    }

    public void update(final InstanceIdentifier<Group> identifier, final Group originalGroup, final Group updatedGroup,
            final InstanceIdentifier<FlowCapableNode> nodeIdent, final BundleId bundleId) {
        final List<Message> messages = new ArrayList<>(1);
        String node = nodeIdent.firstKeyOf(Node.class, NodeKey.class).getId().getValue();
        BundleInnerMessage bundleInnerMessage = new BundleUpdateGroupCaseBuilder()
                .setUpdateGroupCaseData(new UpdateGroupCaseDataBuilder(updatedGroup).build()).build();
        Message message = new MessageBuilder().setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)))
                .setBundleInnerMessage(bundleInnerMessage).build();
        messages.add(message);
        AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class))).setBundleId(bundleId)
                .setFlags(BUNDLE_FLAGS).setMessages(new MessagesBuilder().setMessage(messages).build()).build();
        final ListenableFuture<RpcResult<AddBundleMessagesOutput>> resultFuture = forwardingRulesManager
                .getSalBundleService().addBundleMessages(addBundleMessagesInput);
        LOG.trace("Pushing group update message {} to bundle {} for device {}", addBundleMessagesInput,
                bundleId.getValue(), node);
        JdkFutures.addErrorLogging(resultFuture, LOG, "updateBundleGroup");
    }

    public Future<? extends RpcResult<?>> add(final InstanceIdentifier<Group> identifier, final Group group,
            final InstanceIdentifier<FlowCapableNode> nodeIdent, final BundleId bundleId) {
        final List<Message> messages = new ArrayList<>(1);
        String node = nodeIdent.firstKeyOf(Node.class, NodeKey.class).getId().getValue();
        BundleInnerMessage bundleInnerMessage = new BundleAddGroupCaseBuilder()
                .setAddGroupCaseData(new AddGroupCaseDataBuilder(group).build()).build();
        Message message = new MessageBuilder().setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)))
                .setBundleInnerMessage(bundleInnerMessage).build();
        messages.add(message);
        AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class))).setBundleId(bundleId)
                .setFlags(BUNDLE_FLAGS).setMessages(new MessagesBuilder().setMessage(messages).build()).build();
        LOG.trace("Pushing group add message {} to bundle {} for device {}", addBundleMessagesInput,
                bundleId.getValue(), node);
        return forwardingRulesManager.getSalBundleService().addBundleMessages(addBundleMessagesInput);
    }
}
