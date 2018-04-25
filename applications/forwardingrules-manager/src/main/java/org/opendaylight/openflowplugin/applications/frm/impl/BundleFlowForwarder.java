/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.MessagesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.Message;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.MessageBuilder;
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

    public void remove(InstanceIdentifier<Flow> identifier, Flow del, InstanceIdentifier<FlowCapableNode> nodeIdent,
            ForwardingRulesManager forwardingRulesManager, BundleId bundleId) {
        final List<Message> messages = new ArrayList<>(1);
        String node = nodeIdent.firstKeyOf(Node.class, NodeKey.class).getId().getValue();
        messages.add(
                new MessageBuilder().setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)))
                .setBundleInnerMessage(new BundleRemoveFlowCaseBuilder()
                        .setRemoveFlowCaseData(new RemoveFlowCaseDataBuilder(del).build()).build())
                .build());
        AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class))).setBundleId(bundleId)
                .setFlags(BUNDLE_FLAGS).setMessages(new MessagesBuilder().setMessage(messages).build()).build();
        final Future<RpcResult<Void>> resultFuture = forwardingRulesManager.getSalBundleService()
                .addBundleMessages(addBundleMessagesInput);
        LOG.trace("Pushing removeBundleFlow message {} for device {}", addBundleMessagesInput, node);
        JdkFutures.addErrorLogging(resultFuture, LOG, "removeBundleFlow");
    }

    public void update(InstanceIdentifier<Flow> identifier, Flow original, Flow update,
            InstanceIdentifier<FlowCapableNode> nodeIdent, ForwardingRulesManager forwardingRulesManager,
            BundleId bundleId) {
        final List<Message> messages = new ArrayList<>(1);
        String node = nodeIdent.firstKeyOf(Node.class, NodeKey.class).getId().getValue();
        messages.add(
                new MessageBuilder().setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)))
                .setBundleInnerMessage(new BundleUpdateFlowCaseBuilder()
                        .setUpdateFlowCaseData(new UpdateFlowCaseDataBuilder(update).build()).build())
                .build());
        AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class))).setBundleId(bundleId)
                .setFlags(BUNDLE_FLAGS).setMessages(new MessagesBuilder().setMessage(messages).build()).build();
        final Future<RpcResult<Void>> resultFuture = forwardingRulesManager.getSalBundleService()
                .addBundleMessages(addBundleMessagesInput);
        LOG.trace("Pushing updateBundleFlow message {} for device {}", addBundleMessagesInput, node);
        JdkFutures.addErrorLogging(resultFuture, LOG, "updateBundleFlow");
    }

    public Future<? extends RpcResult<?>> add(InstanceIdentifier<Flow> identifier, Flow add,
            InstanceIdentifier<FlowCapableNode> nodeIdent, ForwardingRulesManager forwardingRulesManager,
            BundleId bundleId) {
        final List<Message> messages = new ArrayList<>(1);
        String node = nodeIdent.firstKeyOf(Node.class, NodeKey.class).getId().getValue();
        messages.add(
                new MessageBuilder().setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)))
                .setBundleInnerMessage(new BundleAddFlowCaseBuilder()
                        .setAddFlowCaseData(new AddFlowCaseDataBuilder(add).build()).build())
                .build());
        AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class))).setBundleId(bundleId)
                .setFlags(BUNDLE_FLAGS).setMessages(new MessagesBuilder().setMessage(messages).build()).build();
        LOG.trace("Pushing addBundleFlow message {} for device {}", addBundleMessagesInput, node);
        return forwardingRulesManager.getSalBundleService().addBundleMessages(addBundleMessagesInput);
    }
}
