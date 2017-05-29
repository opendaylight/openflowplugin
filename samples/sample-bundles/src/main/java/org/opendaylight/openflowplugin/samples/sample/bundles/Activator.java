/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.samples.sample.bundles;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.AbstractBrokerAwareActivator;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.vlan.action._case.PopVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.BucketId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.SalBundleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.Messages;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.MessagesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.Message;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.MessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleAddFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleAddGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.add.flow._case.AddFlowCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.add.group._case.AddGroupCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleControlType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample bundles activator
 */
public class Activator extends AbstractBrokerAwareActivator implements BindingAwareConsumer, ClusteredDataTreeChangeListener<FlowCapableNode>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private static final BundleId BUNDLE_ID = new BundleId(1L);
    private static final BundleFlags BUNDLE_FLAGS = new BundleFlags(true, true);
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private DataBroker dataBroker;
    private SalBundleService bundleService;

    @Override
    public void close() throws Exception {
        LOG.debug("close() passing");
    }

    @Override
    public void onSessionInitialized(ConsumerContext consumerContext) {
        LOG.debug("inSessionInitialized() passing");
        dataBroker = consumerContext.getSALService(DataBroker.class);

        final InstanceIdentifier<FlowCapableNode> path = InstanceIdentifier.create(Nodes.class).child(Node.class).
                augmentation(FlowCapableNode.class);
        final DataTreeIdentifier<FlowCapableNode> identifier = new DataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, path);

        dataBroker.registerDataTreeChangeListener(identifier, Activator.this);
        bundleService = consumerContext.getRpcService(SalBundleService.class);
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<FlowCapableNode>> modifications) {
        for (DataTreeModification modification : modifications) {
            if (modification.getRootNode().getModificationType() == ModificationType.WRITE) {
                LOG.info("Node connected:  {}", modification.getRootPath().getRootIdentifier().firstIdentifierOf(Node.class));

                final NodeRef nodeRef = new NodeRef(modification.getRootPath().getRootIdentifier().firstIdentifierOf(Node.class));

                final ControlBundleInput openBundleInput = new ControlBundleInputBuilder()
                        .setNode(nodeRef)
                        .setBundleId(BUNDLE_ID)
                        .setFlags(BUNDLE_FLAGS)
                        .setType(BundleControlType.ONFBCTOPENREQUEST)
                        .build();

                final ControlBundleInput commitBundleInput = new ControlBundleInputBuilder()
                        .setNode(nodeRef)
                        .setBundleId(BUNDLE_ID)
                        .setFlags(BUNDLE_FLAGS)
                        .setType(BundleControlType.ONFBCTCOMMITREQUEST)
                        .build();

                final List<Message> innerMessages = createMessages(nodeRef);
                final Messages messages = new MessagesBuilder().setMessage(innerMessages).build();
                final AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                        .setNode(nodeRef)
                        .setBundleId(BUNDLE_ID)
                        .setFlags(BUNDLE_FLAGS)
                        .setMessages(messages)
                        .build();

                makeCompletableFuture(bundleService.controlBundle(openBundleInput))
                        .thenComposeAsync(voidRpcResult -> {
                            LOG.debug("Open successful: {}, msg: {}", voidRpcResult.isSuccessful(), voidRpcResult.getErrors());

                            final CompletableFuture<RpcResult<Void>> addFuture =
                                    makeCompletableFuture(bundleService.addBundleMessages(addBundleMessagesInput));

                            return addFuture;
                        }).thenComposeAsync(voidRpcResult -> {
                            LOG.debug("AddBundleMessages successful: {}, msg: {}", voidRpcResult.isSuccessful(), voidRpcResult.getErrors());

                            final CompletableFuture<RpcResult<Void>> controlCommitFuture =
                                    makeCompletableFuture(bundleService.controlBundle(commitBundleInput));

                            return controlCommitFuture;
                        }).thenAccept(voidRpcResult -> {
                            LOG.debug("Commit successful: {}, msg: {}", voidRpcResult.isSuccessful(), voidRpcResult.getErrors());
                });
            }
        }
    }

    private static <T> CompletableFuture<T> makeCompletableFuture(Future<T> future) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }, EXECUTOR);
    }

    @Override
    protected void onBrokerAvailable(BindingAwareBroker bindingAwareBroker, BundleContext bundleContext) {
        LOG.debug("onBrokerAvailable() passing");
        bindingAwareBroker.registerConsumer(this);
    }

    private static List<Message> createMessages(NodeRef nodeRef) {
        List<Message> messages  = new ArrayList<>();


        messages.add(new MessageBuilder().setNode(nodeRef).setBundleInnerMessage(
                new BundleAddGroupCaseBuilder()
                        .setAddGroupCaseData(new AddGroupCaseDataBuilder(createGroup(1l)).build()).build()).build());

        messages.add(new MessageBuilder().setNode(nodeRef).setBundleInnerMessage(
                new BundleAddFlowCaseBuilder()
                        .setAddFlowCaseData(new AddFlowCaseDataBuilder(createFlow("42", 1l, 1, (short) 1)).build()).build()).build());

        messages.add(new MessageBuilder().setNode(nodeRef).setBundleInnerMessage(
                new BundleAddFlowCaseBuilder()
                        .setAddFlowCaseData(new AddFlowCaseDataBuilder(createFlow("43", 1l, 2, (short) 2)).build()).build()).build());

        LOG.debug("createMessages() passing {}", messages);

        return messages;
    }

    private static Flow createFlow(String flowId, long groupId, int priority, short tableId) {
        MatchBuilder matchBuilder = new MatchBuilder();
        matchBuilder.setEthernetMatch(new EthernetMatchBuilder()
                .setEthernetType(new EthernetTypeBuilder()
                        .setType(new EtherType(2048l)).build()).build());

        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setMatch(matchBuilder.build());
        flowBuilder.setInstructions(createGroupInstructions(groupId).build());
        flowBuilder.setPriority(priority);
        flowBuilder.setCookie(new FlowCookie(new BigInteger(flowId + "" + priority)));

        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);
        flowBuilder.setStrict(false);
        flowBuilder.setContainerName(null);
        flowBuilder.setId(new FlowId(flowId));
        flowBuilder.setTableId(tableId);
        flowBuilder.setKey(key);
        flowBuilder.setFlowName("FlowWithGroupInstruction");

        return flowBuilder.build();
    }

    private static Group createGroup(long groupId) {
        GroupBuilder groupBuilder = new GroupBuilder();
        GroupKey groupKey = new GroupKey(new GroupId(groupId));
        groupBuilder.setKey(groupKey);
        groupBuilder.setGroupId(groupKey.getGroupId());
        groupBuilder.setBarrier(false);
        groupBuilder.setGroupName("Foo");
        groupBuilder.setContainerName(null);
        groupBuilder.setGroupType(GroupTypes.GroupAll);
        groupBuilder.setBuckets(createBuckets().build());

        return groupBuilder.build();
    }

    private static BucketsBuilder createBuckets() {
        List<Bucket> bucketList = new ArrayList<>();
        List<Action> actionList = new ArrayList<>();

        actionList.add(new ActionBuilder()
                .setOrder(0)
                .setAction(new PopVlanActionCaseBuilder()
                        .setPopVlanAction(new PopVlanActionBuilder().build())
                        .build()).build());

        BucketBuilder bucketBuilder = new BucketBuilder();
        bucketBuilder.setBucketId(new BucketId(12l));
        bucketBuilder.setAction(actionList);

        bucketList.add(bucketBuilder.build());

        actionList = new ArrayList<>();
        SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
        setFieldCaseBuilder.setSetField(new SetFieldBuilder()
                .setLayer3Match(new Ipv4MatchBuilder()
                        .setIpv4Source(new Ipv4Prefix("10.0.1.0/32"))
                        .build())
                .build());

        actionList.add(new ActionBuilder()
                .setAction(setFieldCaseBuilder.build())
                .setOrder(0)
                .build());

        setFieldCaseBuilder = new SetFieldCaseBuilder();
        setFieldCaseBuilder.setSetField(new SetFieldBuilder()
                .setLayer3Match(new Ipv4MatchBuilder()
                        .setIpv4Destination(new Ipv4Prefix("10.0.10.0/32"))
                        .build())
                .build());

        actionList.add(new ActionBuilder()
                .setAction(setFieldCaseBuilder.build())
                .setOrder(0)
                .build());

        bucketBuilder = new BucketBuilder();
        bucketBuilder.setBucketId(new BucketId(13l));
        bucketBuilder.setAction(actionList);

        bucketList.add(bucketBuilder.build());

        BucketsBuilder bucketsBuilder = new BucketsBuilder();
        bucketsBuilder.setBucket(bucketList);

        return bucketsBuilder;
    }

    private static InstructionsBuilder createGroupInstructions(long groupId) {
        List<Action> actionList = new ArrayList<>();
        ActionBuilder actionBuilder = new ActionBuilder();

        GroupActionBuilder groupActionBuilder = new GroupActionBuilder();
        groupActionBuilder.setGroupId(groupId);

        actionBuilder.setAction(new GroupActionCaseBuilder().setGroupAction(groupActionBuilder.build()).build());
        actionBuilder.setOrder(1);
        actionBuilder.setKey(new ActionKey(0));
        actionList.add(actionBuilder.build());

        ApplyActionsBuilder applyActionsBuilder = new ApplyActionsBuilder();
        applyActionsBuilder.setAction(actionList);

        InstructionBuilder instructionBuilder = new InstructionBuilder();
        instructionBuilder.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(applyActionsBuilder.build()).build());
        instructionBuilder.setOrder(0);
        instructionBuilder.setKey(new InstructionKey(0));

        InstructionsBuilder instructionsBuilder = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<>();
        instructions.add(instructionBuilder.build());
        instructionsBuilder.setInstruction(instructions);

        return instructionsBuilder;
    }
}
