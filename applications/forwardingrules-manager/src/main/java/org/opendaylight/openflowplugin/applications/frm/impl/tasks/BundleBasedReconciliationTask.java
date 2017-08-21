/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl.tasks;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.Messages;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.MessagesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.Message;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.MessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleAddFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleAddGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.add.flow._case.AddFlowCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.add.group._case.AddGroupCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.remove.flow._case.RemoveFlowCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.remove.group._case.RemoveGroupCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleControlType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleBasedReconciliationTask implements Callable<Boolean> {
    private static final Logger LOG = LoggerFactory.getLogger(BundleBasedReconciliationTask.class);
    private static final AtomicLong BUNDLE_ID = new AtomicLong();
    private static final BundleFlags BUNDLE_FLAGS = new BundleFlags(true, true);
    private static final String SEPARATOR = ":";

    private final ForwardingRulesManager provider;
    private final InstanceIdentifier<FlowCapableNode> nodeIdent;
    public BundleBasedReconciliationTask(final ForwardingRulesManager provider,
                                         final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        this.provider = provider;
        this.nodeIdent = nodeIdent;
    }

    @Override
    public Boolean call() {
        String sNode = nodeIdent.firstKeyOf(Node.class, NodeKey.class).getId().getValue();
        Optional<FlowCapableNode> flowNode = Optional.absent();
        BundleId bundleIdValue = new BundleId(BUNDLE_ID.getAndIncrement());
        BigInteger nDpId = getDpnIdFromNodeName(sNode);
        LOG.debug("Triggering bundle based tasks for device :{}", nDpId);
        ReadOnlyTransaction trans = provider.getReadTransaction();
        try {
            flowNode = trans.read(LogicalDatastoreType.CONFIGURATION, nodeIdent).get();
        } catch (Exception e) {
            LOG.error("Error occurred while reading the configuration data store for node {}", nodeIdent, e);
        }

        if (flowNode.isPresent()) {
            LOG.debug("FlowNode present for Datapath ID {}", nDpId);
            final NodeRef nodeRef = new NodeRef(nodeIdent.firstIdentifierOf(Node.class));

            final ControlBundleInput openBundleInput = new ControlBundleInputBuilder()
                    .setNode(nodeRef)
                    .setBundleId(bundleIdValue)
                    .setFlags(BUNDLE_FLAGS)
                    .setType(BundleControlType.ONFBCTOPENREQUEST)
                    .build();

            final ControlBundleInput commitBundleInput = new ControlBundleInputBuilder()
                    .setNode(nodeRef)
                    .setBundleId(bundleIdValue)
                    .setFlags(BUNDLE_FLAGS)
                    .setType(BundleControlType.ONFBCTCOMMITREQUEST)
                    .build();

            final AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                    .setNode(nodeRef)
                    .setBundleId(bundleIdValue)
                    .setFlags(BUNDLE_FLAGS)
                    .setMessages(createMessages(nodeRef, flowNode))
                    .build();

            Future<RpcResult<Void>> openBundle = provider.getSalBundleService().controlBundle(openBundleInput);

            ListenableFuture<RpcResult<Void>> addBundleMessagesFuture =
                    Futures.transformAsync(JdkFutureAdapters.listenInPoolThread(openBundle), rpcResult -> {
                        if (rpcResult.isSuccessful()) {
                            return JdkFutureAdapters.listenInPoolThread(
                                    provider.getSalBundleService().addBundleMessages(addBundleMessagesInput));
                        }
                        return Futures.immediateFuture(null);
                    });

            ListenableFuture<RpcResult<Void>> commitBundleFuture =
                    Futures.transformAsync(addBundleMessagesFuture, rpcResult -> {
                        if (rpcResult.isSuccessful()) {
                            return JdkFutureAdapters.listenInPoolThread(
                                    provider.getSalBundleService().controlBundle(commitBundleInput));
                        }
                        return Futures.immediateFuture(null);
                    });

                /* Bundles not supported for meters*/
            List<Meter> meters = flowNode.get().getMeter() != null
                    ? flowNode.get().getMeter() : Collections.emptyList();
            ListenableFuture<RpcResult<Void>> meterFuture =
                    Futures.transformAsync(commitBundleFuture, rpcResult -> {
                        if (rpcResult.isSuccessful()) {
                            for (Meter meter : meters) {
                                final KeyedInstanceIdentifier<Meter, MeterKey> meterIdent =
                                        nodeIdent.child(Meter.class, meter.getKey());
                                provider.getMeterCommiter().add(meterIdent, meter, nodeIdent);
                            }
                        }
                        return Futures.immediateFuture(null);
                    });

            trans.close();
            try {
                if(commitBundleFuture.get().isSuccessful()) {
                    LOG.debug("Completing bundle based tasks for device ID:{}", nDpId);
                    return true;
                } else {
                    return false;
                }
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Error while doing bundle based tasks for device ID:{}", nodeIdent);
                return false;
            }
        }
        LOG.error("FlowNode not present for Datapath ID {}", nDpId);
        return false;
    }


    private Messages createMessages(final NodeRef nodeRef , final Optional<FlowCapableNode> flowNode) {
        final List<Message> messages  = new ArrayList<>();
        messages.add(new MessageBuilder().setNode(nodeRef).setBundleInnerMessage(
                new BundleRemoveFlowCaseBuilder()
                        .setRemoveFlowCaseData(new RemoveFlowCaseDataBuilder(getDeleteAllFlow()).build()).build()).build());

        messages.add(new MessageBuilder().setNode(nodeRef).setBundleInnerMessage(
                new BundleRemoveGroupCaseBuilder()
                        .setRemoveGroupCaseData(new RemoveGroupCaseDataBuilder(getDeleteAllGroup()).build()).build()).build());

        if(flowNode.get().getGroup()!= null) {
            for (Group gr : flowNode.get().getGroup()) {
                messages.add(new MessageBuilder().setNode(nodeRef).setBundleInnerMessage(
                        new BundleAddGroupCaseBuilder()
                                .setAddGroupCaseData(new AddGroupCaseDataBuilder(gr).build()).build()).build());
            }
        }

        if(flowNode.get().getTable()!= null) {
            for (Table table : flowNode.get().getTable()) {
                for (Flow flow : table.getFlow()) {
                    messages.add(new MessageBuilder().setNode(nodeRef).setBundleInnerMessage(
                            new BundleAddFlowCaseBuilder()
                                    .setAddFlowCaseData(new AddFlowCaseDataBuilder(flow).build()).build()).build());
                }
            }
        }

        LOG.debug("The size of the flows and group messages created in createMessage() {}", messages.size());
        return new MessagesBuilder().setMessage(messages).build();
    }

    private Flow getDeleteAllFlow(){
        final FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setTableId(OFConstants.OFPTT_ALL);
        return flowBuilder.build();
    }

    private Group getDeleteAllGroup(){
        final GroupBuilder groupBuilder = new GroupBuilder();
        groupBuilder.setGroupType(GroupTypes.GroupAll);
        groupBuilder.setGroupId(new GroupId(OFConstants.OFPG_ALL));
        return groupBuilder.build();
    }

    private BigInteger getDpnIdFromNodeName(String nodeName) {
        String dpId = nodeName.substring(nodeName.lastIndexOf(SEPARATOR) + 1);
        return new BigInteger(dpId);
    }
}