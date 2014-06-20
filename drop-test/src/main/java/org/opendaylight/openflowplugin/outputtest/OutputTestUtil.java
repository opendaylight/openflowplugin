/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.outputtest;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang.ArrayUtils;
import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.opendaylight.controller.sal.binding.api.data.DataModificationTransaction;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
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
import org.opendaylight.yangtools.yang.common.RpcResult;

public class OutputTestUtil {

    private OutputTestUtil() {
        throw new UnsupportedOperationException("Utility class. Instantiation is not allowed.");
    }

    public static TransmitPacketInput buildTransmitInputPacket(final String nodeId, final byte[] packValue,
            final String outPort, final String inPort) {
        ArrayList<Byte> list = new ArrayList<Byte>(40);
        byte[] msg = new String("sendOutputMsg_TEST").getBytes();

        int index = 0;
        for (byte b : msg) {
            list.add(b);
            index = index < 7 ? index + 1 : 0;
        }

        while (index < 8) {
            list.add((byte)0);
            index++;
        }
        NodeRef ref = createNodeRef(nodeId);
        NodeConnectorRef nEgressConfRef = new NodeConnectorRef(createNodeConnRef(nodeId, outPort));

        NodeConnectorRef nIngressConRef = new NodeConnectorRef(createNodeConnRef(nodeId, inPort));

        TransmitPacketInputBuilder tPackBuilder = new TransmitPacketInputBuilder();

        final ArrayList<Byte> _converted_list = list;
        byte[] _primitive = ArrayUtils.toPrimitive(_converted_list.toArray(new Byte[0]));
        tPackBuilder.setPayload(_primitive);

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

        DataBrokerService dataBrokerService = pc.<DataBrokerService>getSALService(DataBrokerService.class);
        DataModificationTransaction modif = dataBrokerService.beginTransaction();

        InstanceIdentifier<Flow> path = InstanceIdentifier.<Nodes>builder(Nodes.class)
                .<Node, NodeKey>child(Node.class, nodeBuilder.getKey())
                .<FlowCapableNode>augmentation(FlowCapableNode.class)
                .<Table, TableKey>child(Table.class, new TableKey(flowBuilder.getTableId()))
                .<Flow, FlowKey>child(Flow.class, flowBuilder.getKey())
                .build();

        modif.putConfigurationData(path, flowBuilder.build());
        Future<RpcResult<TransactionStatus>> commitFuture = modif.commit();

        try {
            RpcResult<TransactionStatus> resutl = commitFuture.get();
            TransactionStatus status = resutl.getResult();
            return "Status of Flow Data Loaded Transaction: " + status;

        } catch (InterruptedException e) {
            e.printStackTrace();
            return e.getClass().getName();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return e.getClass().getName();
        }
    }

    public static NodeRef createNodeRef(final String nodeId) {
        NodeKey key = new NodeKey(new NodeId(nodeId));
        InstanceIdentifier<Node> path = InstanceIdentifier.<Nodes>builder(Nodes.class)
                .<Node, NodeKey>child(Node.class, key)
                .toInstance();
        return new NodeRef(path);
    }

    public static NodeConnectorRef createNodeConnRef(final String nodeId, final String port) {
        StringBuilder sBuild = new StringBuilder(nodeId).append(':').append(port);

        NodeConnectorKey nConKey = new NodeConnectorKey(new NodeConnectorId(sBuild.toString()));

        InstanceIdentifier<NodeConnector> path = InstanceIdentifier.<Nodes>builder(Nodes.class)
                .<Node, NodeKey>child(Node.class, new NodeKey(new NodeId(nodeId)))
                .<NodeConnector, NodeConnectorKey>child(NodeConnector.class, nConKey)
                .toInstance();

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
        // flow.setBufferId(new Long(12));
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
        ArrayList<Action> aList = new ArrayList<Action>();
        ActionBuilder aBuild =  new ActionBuilder();

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

        ArrayList<Instruction> instr = new ArrayList<Instruction>();
        instr.add(iBuild.build());
        return new InstructionsBuilder().setInstruction(instr);
    }

    private static short checkTableId(final String tableId) {
        try {
            return Short.parseShort(tableId);
        } catch (Exception ex) {
            return 2;
        }
    }
}
