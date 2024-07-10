/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.samples.sample.bundles;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.vlan.action._case.PopVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.BucketId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessages;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundle;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInputBuilder;
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
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample DataTreeChangeListener.
 */
@Singleton
@Component(service = { })
public final class SampleFlowCapableNodeListener implements DataTreeChangeListener<FlowCapableNode>, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(SampleFlowCapableNodeListener.class);

    private static final BundleId BUNDLE_ID = new BundleId(Uint32.ONE);
    private static final BundleFlags BUNDLE_FLAGS = new BundleFlags(true, true);
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final ControlBundle controlBundle;
    private final AddBundleMessages addBundleMessages;
    private final Registration listenerReg;

    @Inject
    @Activate
    public SampleFlowCapableNodeListener(@Reference final DataBroker dataBroker,
            @Reference final RpcService rpcService) {
        controlBundle = rpcService.getRpc(ControlBundle.class);
        addBundleMessages = rpcService.getRpc(AddBundleMessages.class);
        LOG.debug("inSessionInitialized() passing");

        final InstanceIdentifier<FlowCapableNode> path = InstanceIdentifier.create(Nodes.class).child(Node.class)
                .augmentation(FlowCapableNode.class);
        final DataTreeIdentifier<FlowCapableNode> identifier =
                DataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, path);

        listenerReg = dataBroker.registerTreeChangeListener(identifier, SampleFlowCapableNodeListener.this);
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        LOG.debug("close() passing");
        listenerReg.close();
    }

    @Override
    public void onDataTreeChanged(final List<DataTreeModification<FlowCapableNode>> modifications) {
        for (var modification : modifications) {
            if (modification.getRootNode().modificationType() == ModificationType.WRITE) {
                final var nodePath = modification.getRootPath().path().firstIdentifierOf(Node.class);
                LOG.info("Node connected:  {}", nodePath);

                final var nodeRef = new NodeRef(nodePath);

                final var openBundleInput = new ControlBundleInputBuilder()
                        .setNode(nodeRef)
                        .setBundleId(BUNDLE_ID)
                        .setFlags(BUNDLE_FLAGS)
                        .setType(BundleControlType.ONFBCTOPENREQUEST)
                        .build();

                final var commitBundleInput = new ControlBundleInputBuilder()
                        .setNode(nodeRef)
                        .setBundleId(BUNDLE_ID)
                        .setFlags(BUNDLE_FLAGS)
                        .setType(BundleControlType.ONFBCTCOMMITREQUEST)
                        .build();

                final var innerMessages = createMessages(nodeRef);
                final var messages = new MessagesBuilder().setMessage(innerMessages).build();
                final var addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                        .setNode(nodeRef)
                        .setBundleId(BUNDLE_ID)
                        .setFlags(BUNDLE_FLAGS)
                        .setMessages(messages)
                        .build();

                makeCompletableFuture(controlBundle.invoke(openBundleInput))
                    .thenComposeAsync(voidRpcResult -> {
                        LOG.debug("Open successful: {}, msg: {}", voidRpcResult.isSuccessful(),
                                voidRpcResult.getErrors());
                        return makeCompletableFuture(addBundleMessages.invoke(addBundleMessagesInput));
                    }).thenComposeAsync(voidRpcResult -> {
                        LOG.debug("AddBundleMessages successful: {}, msg: {}", voidRpcResult.isSuccessful(),
                                voidRpcResult.getErrors());
                        return makeCompletableFuture(controlBundle.invoke(commitBundleInput));
                    }).thenAccept(voidRpcResult -> LOG.debug("Commit successful: {}, msg: {}",
                        voidRpcResult.isSuccessful(), voidRpcResult.getErrors()));
            }
        }
    }

    private static <T> CompletableFuture<T> makeCompletableFuture(final Future<T> future) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }, EXECUTOR);
    }

    private static List<Message> createMessages(final NodeRef nodeRef) {
        final var messages = List.of(
            new MessageBuilder()
                .setNode(nodeRef)
                .setBundleInnerMessage(new BundleAddGroupCaseBuilder()
                    .setAddGroupCaseData(new AddGroupCaseDataBuilder(createGroup(Uint32.ONE)).build()).build())
                .build(),
            new MessageBuilder()
                .setNode(nodeRef)
                .setBundleInnerMessage(new BundleAddFlowCaseBuilder()
                    .setAddFlowCaseData(
                        new AddFlowCaseDataBuilder(createFlow("42", Uint32.ONE, Uint16.ONE, Uint8.ONE)).build())
                    .build())
                .build(),
            new MessageBuilder()
                .setNode(nodeRef)
                .setBundleInnerMessage(new BundleAddFlowCaseBuilder()
                    .setAddFlowCaseData(
                        new AddFlowCaseDataBuilder(createFlow("43", Uint32.ONE, Uint16.TWO, Uint8.TWO)).build())
                    .build())
                .build());

        LOG.debug("createMessages() passing {}", messages);

        return messages;
    }

    private static Flow createFlow(final String flowId, final Uint32 groupId, final Uint16 priority,
            final Uint8 tableId) {
        return new FlowBuilder()
            .setId(new FlowId(flowId))
            .setTableId(tableId)
            .setMatch(new MatchBuilder()
                .setEthernetMatch(new EthernetMatchBuilder()
                    .setEthernetType(new EthernetTypeBuilder()
                        .setType(new EtherType(Uint32.valueOf(2048)))
                        .build())
                    .build())
                .build())
            .setInstructions(createGroupInstructions(groupId).build())
            .setPriority(priority)
            .setCookie(new FlowCookie(Uint64.valueOf(flowId + "" + priority)))
            .setHardTimeout(Uint16.ZERO)
            .setIdleTimeout(Uint16.ZERO)
            .setStrict(false)
            .setContainerName(null)
            .setFlowName("FlowWithGroupInstruction")
            .build();
    }

    private static Group createGroup(final Uint32 groupId) {
        return new GroupBuilder()
            .setGroupId(new GroupId(groupId))
            .setBarrier(false)
            .setGroupName("Foo")
            .setContainerName(null)
            .setGroupType(GroupTypes.GroupAll)
            .setBuckets(createBuckets().build())
            .build();
    }

    private static BucketsBuilder createBuckets() {
        return new BucketsBuilder()
            .setBucket(BindingMap.ordered(new BucketBuilder()
                .setBucketId(new BucketId(Uint32.valueOf(12)))
                .setAction(BindingMap.of(new ActionBuilder()
                    .setOrder(0)
                    .setAction(new PopVlanActionCaseBuilder()
                        .setPopVlanAction(new PopVlanActionBuilder().build())
                        .build())
                    .build()))
                .build(), new BucketBuilder()
                .setBucketId(new BucketId(Uint32.valueOf(13)))
                .setAction(BindingMap.of(new ActionBuilder()
                    .setAction(new SetFieldCaseBuilder()
                        .setSetField(new SetFieldBuilder()
                            .setLayer3Match(new Ipv4MatchBuilder().setIpv4Source(new Ipv4Prefix("10.0.1.0/32")).build())
                            .build())
                        .build())
                    .setOrder(0)
                    .build(), new ActionBuilder()
                    .setOrder(0)
                    .setAction(new SetFieldCaseBuilder()
                        .setSetField(new SetFieldBuilder()
                            .setLayer3Match(new Ipv4MatchBuilder()
                                .setIpv4Destination(new Ipv4Prefix("10.0.10.0/32"))
                                .build())
                            .build())
                        .build())
                    .build()))
                .build()));
    }

    private static InstructionsBuilder createGroupInstructions(final Uint32 groupId) {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new GroupActionCaseBuilder()
                                .setGroupAction(new GroupActionBuilder().setGroupId(groupId).build())
                                .build())
                            .setOrder(1)
                            .withKey(new ActionKey(0))
                            .build()))
                        .build())
                    .build())
                .withKey(new InstructionKey(0))
                .build()));
    }
}
