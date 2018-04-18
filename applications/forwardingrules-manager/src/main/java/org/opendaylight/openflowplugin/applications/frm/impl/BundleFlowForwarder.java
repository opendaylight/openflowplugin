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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleAddFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleUpdateFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.add.flow._case.AddFlowCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.remove.flow._case.RemoveFlowCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.update.flow._case.UpdateFlowCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleFlowForwarder {

    private static final Logger LOG = LoggerFactory.getLogger(BundleFlowForwarder.class);
    private static final BundleFlags BUNDLE_FLAGS = new BundleFlags(true, true);
    private final ForwardingRulesManager forwardingRulesManager;

    public BundleFlowForwarder(ForwardingRulesManager forwardingRulesManager) {
        this.forwardingRulesManager = Preconditions.checkNotNull(forwardingRulesManager,
                "ForwardingRulesManager can not be null!");
    }

    public void remove(final InstanceIdentifier<Flow> identifier, final Flow flow,
            final InstanceIdentifier<FlowCapableNode> nodeIdent, final BundleId bundleId) {
        final List<Message> messages = new ArrayList<>(1);
        String node = nodeIdent.firstKeyOf(Node.class, NodeKey.class).getId().getValue();
        BundleInnerMessage bundleInnerMessage = new BundleRemoveFlowCaseBuilder()
                .setRemoveFlowCaseData(new RemoveFlowCaseDataBuilder(flow).build()).build();
        Message message = new MessageBuilder().setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)))
                .setBundleInnerMessage(bundleInnerMessage).build();
        messages.add(message);
        AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class))).setBundleId(bundleId)
                .setFlags(BUNDLE_FLAGS).setMessages(new MessagesBuilder().setMessage(messages).build()).build();
        final ListenableFuture<RpcResult<AddBundleMessagesOutput>> resultFuture = forwardingRulesManager
                .getSalBundleService().addBundleMessages(addBundleMessagesInput);
        LOG.trace("Pushing flow remove message {} to bundle {} for device {}", addBundleMessagesInput,
                bundleId.getValue(), node);
        JdkFutures.addErrorLogging(resultFuture, LOG, "removeBundleFlow");
    }

    public void update(final InstanceIdentifier<Flow> identifier, final Flow originalFlow, final Flow updatedFlow,
            final InstanceIdentifier<FlowCapableNode> nodeIdent, final BundleId bundleId) {
        final List<Message> messages = new ArrayList<>(1);
        String node = nodeIdent.firstKeyOf(Node.class, NodeKey.class).getId().getValue();
        BundleInnerMessage bundleInnerMessage = new BundleUpdateFlowCaseBuilder()
                .setUpdateFlowCaseData(new UpdateFlowCaseDataBuilder(updatedFlow).build()).build();
        Message message = new MessageBuilder().setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)))
                .setBundleInnerMessage(bundleInnerMessage).build();
        messages.add(message);
        AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class))).setBundleId(bundleId)
                .setFlags(BUNDLE_FLAGS).setMessages(new MessagesBuilder().setMessage(messages).build()).build();
        final ListenableFuture<RpcResult<AddBundleMessagesOutput>> resultFuture = forwardingRulesManager
                .getSalBundleService().addBundleMessages(addBundleMessagesInput);
        LOG.trace("Pushing flow update message {} to bundle {} for device {}", addBundleMessagesInput,
                bundleId.getValue(), node);
        JdkFutures.addErrorLogging(resultFuture, LOG, "updateBundleFlow");
    }

    public Future<? extends RpcResult<?>> add(final InstanceIdentifier<Flow> identifier, final Flow flow,
            final InstanceIdentifier<FlowCapableNode> nodeIdent, final BundleId bundleId) {
        final List<Message> messages = new ArrayList<>(1);
        String node = nodeIdent.firstKeyOf(Node.class, NodeKey.class).getId().getValue();
        BundleInnerMessage bundleInnerMessage = new BundleAddFlowCaseBuilder()
                .setAddFlowCaseData(new AddFlowCaseDataBuilder(flow).build()).build();
        Message message = new MessageBuilder().setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)))
                .setBundleInnerMessage(bundleInnerMessage).build();
        messages.add(message);
        AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class))).setBundleId(bundleId)
                .setFlags(BUNDLE_FLAGS).setMessages(new MessagesBuilder().setMessage(messages).build()).build();
        LOG.trace("Pushing flow add message {} to bundle {} for device {}", addBundleMessagesInput,
                bundleId.getValue(), node);
        return forwardingRulesManager.getSalBundleService().addBundleMessages(addBundleMessagesInput);
    }
}
