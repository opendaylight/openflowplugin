/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.outputtest

import java.math.BigInteger
import java.util.ArrayList
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext
import java.util.concurrent.ExecutionException
import org.apache.commons.lang.ArrayUtils

class OutputTestUtil {
    
    private new() {
        throw new UnsupportedOperationException("Utility class. Instantiation is not allowed.");
    }
    
    public static def buildTransmitInputPacket(String nodeId, byte[] packValue, String outPort, String inPort) {
        
        var list = new ArrayList<Byte>(40);
        var msg = (new String("sendOutputMsg_TEST")).getBytes();
        var index =0;
        for (byte b : msg) {
            list.add(b);
            if(index < 7) {index = index+1} else {index = 0}
        }
        while(index < 8) {
            list.add(new Byte("0"));
            index = index+1;
        }
        
        var ref = createNodeRef(nodeId);
        var nEgressConfRef = new NodeConnectorRef(createNodeConnRef(nodeId, outPort));
        var nIngressConRef = new NodeConnectorRef(createNodeConnRef(nodeId, inPort));
        var tPackBuilder = new TransmitPacketInputBuilder
        tPackBuilder.setPayload(ArrayUtils.toPrimitive(list));
        tPackBuilder.setNode(ref);
        // TODO VD P2 missing cookies in Test
        tPackBuilder.setCookie(null);
        tPackBuilder.setEgress(nEgressConfRef)
        tPackBuilder.setIngress(nIngressConRef)
        return tPackBuilder.build
    }

    public static def makePingFlowForNode(String nodeId, ProviderContext pc) {
        var nodeBuilder = createNodeBuilder(nodeId)
        var flowBuilder = createFlowBuilder(1235, null, "ping")
        
        var dataBrokerService = pc.getSALService(DataBrokerService)
        var modif = dataBrokerService.beginTransaction
        
        var path = InstanceIdentifier
                    .builder(Nodes)
                    .child(Node, nodeBuilder.getKey)
                    .augmentation(FlowCapableNode)
                    .child(Table, new TableKey(flowBuilder.getTableId))
                    .child(Flow, flowBuilder.getKey()).build;
        
        modif.putConfigurationData(path, flowBuilder.build)
        var commitFuture = modif.commit
        
        try {
            var resutl = commitFuture.get
            var status = resutl.result
            return "Status of Flow Data Loaded Transaction: " + status
        } catch (InterruptedException e) {
            e.printStackTrace();
            return e.class.name
        } catch (ExecutionException e) {
            e.printStackTrace();
            return e.class.name
        }
    }
    
    public static def createNodeRef(String nodeId) {
        var key = new NodeKey(new NodeId(nodeId));
        var path = InstanceIdentifier.builder(Nodes).child(Node, key).toInstance
        return new NodeRef(path)
    }
    
    public static def createNodeConnRef(String nodeId, String port) {
        var sBuild = new StringBuilder(nodeId).append(":").append(port);
        var nConKey = new NodeConnectorKey(new NodeConnectorId(sBuild.toString));
        var path = InstanceIdentifier.builder(Nodes)
                .child(Node, new NodeKey(new NodeId(nodeId)))
                .child(NodeConnector, nConKey).toInstance()
        return new NodeConnectorRef(path)
    }
    
    
    
    private static def createNodeBuilder(String nodeId) {
        var builder = new NodeBuilder()
        builder.setId(new NodeId(nodeId))
        builder.setKey(new NodeKey(builder.getId()))
        return builder
    }
    
    private static def createFlowBuilder(long flowId, String tableId, String flowName) {
        var fBuild = new FlowBuilder();
        fBuild.setMatch(new MatchBuilder().build)
        fBuild.setInstructions(createPingInstructionsBuilder().build)
        
        var key = new FlowKey(new FlowId(Long.toString(flowId)));
        fBuild.setBarrier(false);
        // flow.setBufferId(new Long(12));
        var value = new BigInteger("10", 10);
        fBuild.setCookie(value);
        fBuild.setCookieMask(value);
        fBuild.setHardTimeout(0);
        fBuild.setIdleTimeout(0);
        fBuild.setInstallHw(false);
        fBuild.setStrict(false);
        fBuild.setContainerName(null);
        fBuild.setFlags(new FlowModFlags(false, false, false, false, false));
        fBuild.setId(new FlowId("12"));
        fBuild.setTableId(checkTableId(tableId));
        fBuild.setOutGroup(new Long(2));
        fBuild.setOutPort(value);

        fBuild.setKey(key);
        fBuild.setPriority(2);
        fBuild.setFlowName(flowName);
        return fBuild
    }

    private static def createPingInstructionsBuilder() {
        var aList = new ArrayList<Action>
        var aBuild = new ActionBuilder

        var output = new OutputActionBuilder
        output.setMaxLength(56)
        output.setOutputNodeConnector(new Uri("CONTROLLER"))
        aBuild.setAction(new OutputActionCaseBuilder().setOutputAction(output.build).build)
        aBuild.setOrder(0)
        aBuild.setKey(new ActionKey(0))
        aList.add(aBuild.build)
        var asBuild = new ApplyActionsBuilder(); asBuild.setAction(aList)

        var iBuild = new InstructionBuilder
        iBuild.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(asBuild.build).build);
        iBuild.setOrder(0);
        iBuild.setKey(new InstructionKey(0));
        
        var instr = new ArrayList<Instruction>(); instr.add(iBuild.build)
        return new InstructionsBuilder().setInstruction(instr) 
    }
    
    private static def checkTableId(String tableId) {
        try {
            return Short.parseShort(tableId)
        } catch (Exception ex) {
            return Short.parseShort("2")
        }
    }
}