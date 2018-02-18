/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.openflowplugin.extension.api.AugmentTuple;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg0;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxRegCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNodesNodeTableFlowApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.NxRegLoadBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.nx.reg.load.DstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev130819.TestFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev130819.TestService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import com.google.common.util.concurrent.Futures;

/**
 * @author msunal
 *
 */
public class Test implements TestService {
    
    private SalFlowService flowService;


    @Override
    public Future<RpcResult<Void>> testFlow(TestFlowInput input) {
        AddFlowInputBuilder flow = new AddFlowInputBuilder();
        flow.setPriority(2);
        flow.setMatch(createMatchBld().build());
        flow.setInstructions(createDecNwTtlInstructionsBld().build());
        flow.setBarrier(Boolean.FALSE);
        BigInteger value = BigInteger.valueOf(10L);
        flow.setCookie(new FlowCookie(value));
        flow.setCookieMask(new FlowCookie(value));
        flow.setHardTimeout(0);
        flow.setIdleTimeout(0);
        flow.setInstallHw(false);
        flow.setStrict(false);
        flow.setContainerName(null);
        flow.setFlags(new FlowModFlags(false, false, false, false, true));
        flow.setTableId((short) 0);

        flow.setFlowName("NiciraFLOW");
        
        // Construct the flow instance id
        final InstanceIdentifier<Node> flowInstanceId = InstanceIdentifier
                .builder(Nodes.class) // File under nodes
                .child(Node.class, new NodeKey(new NodeId("openflow:1"))).build(); // A particular node identified by nodeKey
        flow.setNode(new NodeRef(flowInstanceId));
        
        pushFlowViaRpc(flow.build());
        
        return Futures.immediateFuture(RpcResultBuilder.<Void>status(true).build());
    }
    
    /**
     * @param addFlowInput
     */
    private void pushFlowViaRpc(AddFlowInput addFlowInput) {
        flowService.addFlow(addFlowInput);
    }

    /**
     * @param flowService the flowService to set
     */
    public void setFlowService(SalFlowService flowService) {
        this.flowService = flowService;
    }
    
    private static MatchBuilder createMatchBld() {
        MatchBuilder match = new MatchBuilder();
        Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        Ipv4Prefix prefix = new Ipv4Prefix("10.0.0.1/24");
        ipv4Match.setIpv4Destination(prefix);
        Ipv4Match i4m = ipv4Match.build();
        match.setLayer3Match(i4m);

        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x0800L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());
        
//        AugmentTuple<Match> extAugmentWrapper = createNxMatchAugment();
//        match.addAugmentation(extAugmentWrapper.getAugmentationClass(), extAugmentWrapper.getAugmentationObject());
        
        return match;
    }
    
    private static AugmentTuple<Match> createNxMatchAugment() {
        // TODO add example
        return null;
    }

    private static InstructionsBuilder createDecNwTtlInstructionsBld() {
        // Add our drop action to a list
        List<Action> actionList = new ArrayList<Action>();
        actionList.add(createOFAction(0).build());
        actionList.add(createNxActionBld(1).build());
        
        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);
       
        // Wrap our Apply Action in an Instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setKey(new InstructionKey(0));
        ib.setOrder(0);

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        ib.setKey(new InstructionKey(0));
        isb.setInstruction(instructions);
        return isb;
    }

    /**
     * @param actionKeyVal 
     * @return
     */
    private static ActionBuilder createOFAction(int actionKeyVal) {
        DecNwTtlBuilder ta = new DecNwTtlBuilder();
        DecNwTtl decNwTtl = ta.build();
        ActionBuilder ab = new ActionBuilder();
        ab.setAction(new DecNwTtlCaseBuilder().setDecNwTtl(decNwTtl).build());
        ab.setKey(new ActionKey(actionKeyVal));
        return ab;
    }

    /**
     * @param actionKeyVal TODO
     * @return
     */
    private static ActionBuilder createNxActionBld(int actionKeyVal) {
        // vendor part
        DstNxRegCaseBuilder nxRegCaseBld = new DstNxRegCaseBuilder().setNxReg(NxmNxReg0.class);
        DstBuilder dstBld = new DstBuilder()
            .setDstChoice(nxRegCaseBld.build())
            .setStart(0)
            .setEnd(5);
        NxRegLoadBuilder nxRegLoadBuilder = new NxRegLoadBuilder();
        nxRegLoadBuilder.setDst(dstBld.build());
        nxRegLoadBuilder.setValue(BigInteger.valueOf(55L));
        NxActionRegLoadNodesNodeTableFlowApplyActionsCaseBuilder topNxActionCaseBld = new NxActionRegLoadNodesNodeTableFlowApplyActionsCaseBuilder();
        topNxActionCaseBld.setNxRegLoad(nxRegLoadBuilder.build());
        
        // base part
        ActionBuilder abExt = new ActionBuilder();
        abExt.setKey(new ActionKey(actionKeyVal));
        abExt.setAction(topNxActionCaseBld.build());
        return abExt;
    }
    

}
