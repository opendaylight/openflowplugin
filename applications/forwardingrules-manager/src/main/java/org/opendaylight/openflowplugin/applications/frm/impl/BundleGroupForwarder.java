package org.opendaylight.openflowplugin.applications.frm.impl;

import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.MessagesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.Message;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.MessageBuilder;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class BundleGroupForwarder {
        private static final BundleFlags BUNDLE_FLAGS = new BundleFlags(true, true);

        public void remove(InstanceIdentifier<Group> identifier, Group del,
                InstanceIdentifier<FlowCapableNode> nodeIdent, ForwardingRulesManager forwardingRulesManager,
                BundleId bundleId) {
                final List<Message> messages = new ArrayList<>(1);
                messages.add(new MessageBuilder().setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)))
                        .setBundleInnerMessage(new BundleRemoveGroupCaseBuilder()
                                .setRemoveGroupCaseData(new RemoveGroupCaseDataBuilder(del).build()).build()).build());
                AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                        .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class))).setBundleId(bundleId)
                        .setFlags(BUNDLE_FLAGS).setMessages(new MessagesBuilder().setMessage(messages).build()).build();

                forwardingRulesManager.getSalBundleService().addBundleMessages(addBundleMessagesInput);
        }

        public void update(InstanceIdentifier<Group> identifier, Group original, Group update,
                InstanceIdentifier<FlowCapableNode> nodeIdent, ForwardingRulesManager forwardingRulesManager,
                BundleId bundleId) {
                final List<Message> messages = new ArrayList<>(1);
                messages.add(new MessageBuilder().setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)))
                        .setBundleInnerMessage(new BundleUpdateGroupCaseBuilder()
                                .setUpdateGroupCaseData(new UpdateGroupCaseDataBuilder(update).build()).build())
                        .build());
                AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                        .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class))).setBundleId(bundleId)
                        .setFlags(BUNDLE_FLAGS).setMessages(new MessagesBuilder().setMessage(messages).build()).build();

                forwardingRulesManager.getSalBundleService().addBundleMessages(addBundleMessagesInput);
        }

        public Future<? extends RpcResult<?>> add(InstanceIdentifier<Group> identifier, Group add,
                InstanceIdentifier<FlowCapableNode> nodeIdent, ForwardingRulesManager forwardingRulesManager,
                BundleId bundleId) {
                final List<Message> messages = new ArrayList<>(1);
                //retrieve bundle id from rf framework

                messages.add(new MessageBuilder().setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)))
                        .setBundleInnerMessage(new BundleAddGroupCaseBuilder()
                                .setAddGroupCaseData(new AddGroupCaseDataBuilder(add).build()).build()).build());
                AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                        .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class))).setBundleId(bundleId)
                        .setFlags(BUNDLE_FLAGS).setMessages(new MessagesBuilder().setMessage(messages).build()).build();

                return forwardingRulesManager.getSalBundleService().addBundleMessages(addBundleMessagesInput);
        }

}
