/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.outputtest;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OutputTestUtil {
    private static final String OUTPUT_MSG = "sendOutputMsg_TEST";
    private static final Logger LOG = LoggerFactory
            .getLogger(OutputTestUtil.class);
    
    private OutputTestUtil() {
        throw new UnsupportedOperationException("Utility class. Instantiation is not allowed.");
    }

    public static TransmitPacketInput buildTransmitInputPacket(final String nodeId, final String outPort,
                                                               final String inPort) {
        List<Byte> list = new ArrayList<Byte>(40);
        byte[] msg = OUTPUT_MSG.getBytes();

        int index = 0;
        for (byte b : msg) {
            list.add(b);
            index = index < 7 ? index + 1 : 0;
        }

        while (index < 8) {
            list.add((byte) 0);
            index++;
        }
        NodeRef ref = createNodeRef(nodeId);
        NodeConnectorRef nEgressConfRef = new NodeConnectorRef(createNodeConnRef(nodeId, outPort));

        NodeConnectorRef nIngressConRef = new NodeConnectorRef(createNodeConnRef(nodeId, inPort));

        TransmitPacketInputBuilder tPackBuilder = new TransmitPacketInputBuilder();

        final List<Byte> convertedList = list;
        byte[] primitive = ArrayUtils.toPrimitive(convertedList.toArray(new Byte[0]));
        tPackBuilder.setPayload(primitive);

        tPackBuilder.setNode(ref);
        // TODO VD P2 missing cookies in Test
        tPackBuilder.setConnectionCookie(null);
        tPackBuilder.setEgress(nEgressConfRef);
        tPackBuilder.setIngress(nIngressConRef);
        return tPackBuilder.build();
    }

    public static String makePingFlowForNode(final String nodeId, final ProviderContext pc) {
        NodeBuilder nodeBuilder = createNodeBuilder(nodeId);
        FlowBuilder flowBuilder = createFlowBuilder(1235, null, "ping");

        DataBroker dataBroker = pc.<DataBroker>getSALService(DataBroker.class);
        ReadWriteTransaction modif = dataBroker.newReadWriteTransaction();

        InstanceIdentifier<Flow> path = InstanceIdentifier.<Nodes>builder(Nodes.class)
                .<Node, NodeKey>child(Node.class, nodeBuilder.getKey())
                .<FlowCapableNode>augmentation(FlowCapableNode.class)
                .<Table, TableKey>child(Table.class, new TableKey(flowBuilder.getTableId()))
                .<Flow, FlowKey>child(Flow.class, flowBuilder.getKey())
                .build();

        modif.put(LogicalDatastoreType.CONFIGURATION, path, flowBuilder.build());
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modif.submit();
        final StringBuilder aggregator = new StringBuilder();
        Futures.addCallback(commitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                aggregator.append("Status of Flow Data Loaded Transaction: succes ");
            }

            @Override
            public void onFailure(Throwable throwable) {
                aggregator.append(throwable.getClass().getName());
            }
        });
        return aggregator.toString();
    }

    public static NodeRef createNodeRef(final String nodeId) {
        NodeKey key = new NodeKey(new NodeId(nodeId));
        InstanceIdentifier<Node> path = InstanceIdentifier.<Nodes>builder(Nodes.class)
                .<Node, NodeKey>child(Node.class, key)
                .build();
        return new NodeRef(path);
    }

    public static NodeConnectorRef createNodeConnRef(final String nodeId, final String port) {
        StringBuilder sBuild = new StringBuilder(nodeId).append(':').append(port);

        NodeConnectorKey nConKey = new NodeConnectorKey(new NodeConnectorId(sBuild.toString()));

        InstanceIdentifier<NodeConnector> path = InstanceIdentifier.<Nodes>builder(Nodes.class)
                .<Node, NodeKey>child(Node.class, new NodeKey(new NodeId(nodeId)))
                .<NodeConnector, NodeConnectorKey>child(NodeConnector.class, nConKey)
                .build();

        return new NodeConnectorRef(path);
    }

    private static NodeBuilder createNodeBuilder(final String nodeId) {
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeId));
        builder.setKey(new NodeKey(builder.getId()));
        return builder;
    }

    private static FlowBuilder createFlowBuilder(final long flowId, final String tableId, final String flowName) {

        FlowBuilder fBuild = new FlowBuilder();
        fBuild.setMatch(new MatchBuilder().build());
        fBuild.setInstructions(createPingInstructionsBuilder().build());

        FlowKey key = new FlowKey(new FlowId(Long.toString(flowId)));
        fBuild.setBarrier(false);
        final BigInteger value = BigInteger.valueOf(10);
        fBuild.setCookie(new FlowCookie(value));
        fBuild.setCookieMask(new FlowCookie(value));
        fBuild.setHardTimeout(0);
        fBuild.setIdleTimeout(0);
        fBuild.setInstallHw(false);
        fBuild.setStrict(false);
        fBuild.setContainerName(null);
        fBuild.setFlags(new FlowModFlags(false, false, false, false, false));
        fBuild.setId(new FlowId("12"));
        fBuild.setTableId(checkTableId(tableId));
        fBuild.setOutGroup(2L);
        fBuild.setOutPort(value);

        fBuild.setKey(key);
        fBuild.setPriority(2);
        fBuild.setFlowName(flowName);
        return fBuild;

    }

    private static InstructionsBuilder createPingInstructionsBuilder() {
        List<Action> aList = new ArrayList<Action>();
        ActionBuilder aBuild = new ActionBuilder();

        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(56);
        output.setOutputNodeConnector(new Uri("CONTROLLER"));
        aBuild.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        aBuild.setOrder(0);
        aBuild.setKey(new ActionKey(0));
        aList.add(aBuild.build());
        ApplyActionsBuilder asBuild = new ApplyActionsBuilder();
        asBuild.setAction(aList);

        InstructionBuilder iBuild = new InstructionBuilder();
        iBuild.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(asBuild.build()).build());
        iBuild.setOrder(0);
        iBuild.setKey(new InstructionKey(0));

        List<Instruction> instr = new ArrayList<Instruction>();
        instr.add(iBuild.build());
        return new InstructionsBuilder().setInstruction(instr);
    }

    private static short checkTableId(final String tableId) {
        try {
            return Short.parseShort(tableId);
        } catch (Exception ex) {
            LOG.debug("TableId problem: ",ex);
            return 2;
        }
    }
}
