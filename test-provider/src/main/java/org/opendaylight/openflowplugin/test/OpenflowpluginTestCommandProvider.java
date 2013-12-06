/*
 * Copyright (c) 2013 Ericsson , Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.DataModification;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpVersion;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.VlanCfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.ControllerActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.FloodActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.FloodAllActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.HwPathActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.LoopbackActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlTypeActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNextHopActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTosActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanCfiActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanPcpActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SwPathActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.config.rev130819.Flows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.config.rev130819.flows.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.config.rev130819.flows.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.config.rev130819.flows.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpSourceHardwareAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpTargetHardwareAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.MetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFieldsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TunnelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginTestCommandProvider implements CommandProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginTestCommandProvider.class);

    private DataBrokerService dataBrokerService;
    private ProviderContext pc;
    private final BundleContext ctx;
    private FlowBuilder testFlow;
    private NodeBuilder testNode;
    private final String originalFlowName = "Foo";
    private final String updatedFlowName = "Bar";

    public OpenflowpluginTestCommandProvider(BundleContext ctx) {
        this.ctx = ctx;
    }

    public void onSessionInitiated(ProviderContext session) {
        pc = session;
        dataBrokerService = session.getSALService(DataBrokerService.class);
        ctx.registerService(CommandProvider.class.getName(), this, null);
        createTestFlow(createTestNode(null), null);
    }

    private NodeBuilder createTestNode(String nodeId) {
        if (nodeId == null) {
            nodeId = OpenflowpluginTestActivator.NODE_ID;
        }
        NodeRef nodeOne = createNodeRef(nodeId);
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeId));
        builder.setKey(new NodeKey(builder.getId()));
        testNode = builder;
        return builder;
    }

    private InstanceIdentifier<Node> nodeBuilderToInstanceId(NodeBuilder node) {
        return InstanceIdentifier.builder(Nodes.class).child(Node.class, node.getKey()).toInstance();
    }

    private FlowBuilder createTestFlow(NodeBuilder nodeBuilder, String flowTypeArg) {

        FlowBuilder flow = new FlowBuilder();
        long id = 123;

        String flowType = flowTypeArg;
        if (flowType == null) {
            flowType = "f1";
        }

        switch (flowType) {
        case "f1":
            id += 1;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createDecNwTtlInstructions().build());
            break;
        case "f2":
            id += 2;
            flow.setMatch(createMatch2().build());
            flow.setInstructions(createDropInstructions().build());
            break;
        case "f3":
            id += 3;
            flow.setMatch(createMatch3().build());
            flow.setInstructions(createDropInstructions().build());
            break;
        case "f4":
            id += 4;
            flow.setMatch(createMatch4().build());
            flow.setInstructions(createDropInstructions().build());
            break;
        case "f5":
            id += 5;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction().build());
            break;
        case "f6":
            id += 6;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createGotoTableInstructions().build());
            break;
        case "f7":
            id += 7;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createMeterInstructions().build());
            break;
        default:
            LOG.warn("flow type not understood: {}", flowType);
        }

        FlowKey key = new FlowKey(id, new NodeRef(new NodeRef(nodeBuilderToInstanceId(nodeBuilder))));
        flow.setBarrier(false);
        flow.setBufferId(new Long(12));
        BigInteger value = new BigInteger("10", 10);
        flow.setCookie(value);
        flow.setCookieMask(value);
        flow.setHardTimeout(12);
        flow.setIdleTimeout(34);
        flow.setInstallHw(false);
        flow.setStrict(false);
        flow.setContainerName(null);
        flow.setFlags(new FlowModFlags(false, false, false, false, false));
        flow.setId(new Long(12));
        flow.setTableId((short) 2);
        flow.setOutGroup(new Long(2));
        flow.setOutPort(value);

        flow.setKey(key);
        flow.setPriority(2);
        flow.setFlowName(originalFlowName + "X" + flowType);
        testFlow = flow;
        return flow;
    }

    /**
     * @return
     */
    private static InstructionsBuilder createDecNwTtlInstructions() {
        DecNwTtlBuilder ta = new DecNwTtlBuilder();
        DecNwTtl decNwTtl = ta.build();
        ActionBuilder ab = new ActionBuilder();
        ab.setAction(decNwTtl);

        // Add our drop action to a list
        List<Action> actionList = new ArrayList<Action>();
        actionList.add(ab.build());

        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(aab.build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    /**
     * @return
     */
    private static InstructionsBuilder createMeterInstructions() {

        MeterBuilder aab = new MeterBuilder();
        aab.setMeter("meter");
        aab.setMeterId(new Long(1));

        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(aab.build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }


    private static InstructionsBuilder createGotoTableInstructions() {

        GoToTableBuilder aab = new GoToTableBuilder();
        aab.setTableId((short) 2);

        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(aab.build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createDropInstructions() {
        DropActionBuilder dab = new DropActionBuilder();
        DropAction dropAction = dab.build();
        ActionBuilder ab = new ActionBuilder();
        ab.setAction(dropAction);

        // Add our drop action to a list
        List<Action> actionList = new ArrayList<Action>();
        actionList.add(ab.build());

        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(aab.build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        ControllerActionBuilder controller = new ControllerActionBuilder();
        controller.setMaxLength(5);
        ab.setAction(controller.build());
        actionList.add(ab.build());

        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(56);
        Uri value = new Uri("PCEP");
        output.setOutputNodeConnector(value);

        PushMplsActionBuilder push = new PushMplsActionBuilder();
        push.setEthernetType(new Integer(0x8847));
        ab.setAction(push.build());
        actionList.add(ab.build());

        PushPbbActionBuilder pbb = new PushPbbActionBuilder();
        pbb.setEthernetType(new Integer(0x88E7));
        ab.setAction(pbb.build());
        actionList.add(ab.build());

        PushVlanActionBuilder vlan = new PushVlanActionBuilder();
        vlan.setEthernetType(new Integer(0x8100));
        ab.setAction(vlan.build());
        actionList.add(ab.build());

        SetDlDstActionBuilder setdl = new SetDlDstActionBuilder();
        setdl.setAddress(new MacAddress("00:05:b9:7c:81:5f"));
        ab.setAction(setdl.build());
        actionList.add(ab.build());

        SetDlSrcActionBuilder src = new SetDlSrcActionBuilder();
        src.setAddress(new MacAddress("00:05:b9:7c:81:5f"));
        ab.setAction(src.build());
        actionList.add(ab.build());

        SetVlanIdActionBuilder vl = new SetVlanIdActionBuilder();
        VlanId a = new VlanId(4723);
        vl.setVlanId(a);
        ab.setAction(vl.build());
        actionList.add(ab.build());

        SetVlanPcpActionBuilder pcp = new SetVlanPcpActionBuilder();
        VlanPcp pcp1 = new VlanPcp((short) 2);
        pcp.setVlanPcp(pcp1);
        ab.setAction(pcp.build());
        actionList.add(ab.build());

        CopyTtlInBuilder ttlin = new CopyTtlInBuilder();
        ab.setAction(ttlin.build());
        actionList.add(ab.build());

        CopyTtlOutBuilder ttlout = new CopyTtlOutBuilder();
        ab.setAction(ttlout.build());
        actionList.add(ab.build());

        DecMplsTtlBuilder mpls = new DecMplsTtlBuilder();
        ab.setAction(mpls.build());
        actionList.add(ab.build());

        DecNwTtlBuilder nwttl = new DecNwTtlBuilder();
        ab.setAction(nwttl.build());
        actionList.add(ab.build());

        DropActionBuilder drop = new DropActionBuilder();
        ab.setAction(drop.build());
        actionList.add(ab.build());

        FloodActionBuilder fld = new FloodActionBuilder();
        ab.setAction(fld.build());
        actionList.add(ab.build());

        FloodAllActionBuilder fldall = new FloodAllActionBuilder();
        ab.setAction(fldall.build());
        actionList.add(ab.build());

        GroupActionBuilder groupActionB = new GroupActionBuilder();
        groupActionB.setGroupId(1L);
        groupActionB.setGroup("0");
        ab.setAction(groupActionB.build());
        actionList.add(ab.build());

        HwPathActionBuilder hwPathB = new HwPathActionBuilder();
        ab.setAction(hwPathB.build());
        actionList.add(ab.build());

        LoopbackActionBuilder loopbackActionBuilder = new LoopbackActionBuilder();
        ab.setAction(loopbackActionBuilder.build());
        actionList.add(ab.build());

        PopMplsActionBuilder popMplsActionBuilder = new PopMplsActionBuilder();
        popMplsActionBuilder.setEthernetType(0XB);
        ab.setAction(popMplsActionBuilder.build());
        actionList.add(ab.build());

        PopPbbActionBuilder popPbbActionBuilder = new PopPbbActionBuilder();
        ab.setAction(popPbbActionBuilder.build());
        actionList.add(ab.build());

        PopVlanActionBuilder popVlanActionBuilder = new PopVlanActionBuilder();
        ab.setAction(popVlanActionBuilder.build());
        actionList.add(ab.build());

        SetDlTypeActionBuilder setDlTypeActionBuilder = new SetDlTypeActionBuilder();
        setDlTypeActionBuilder.setDlType(new EtherType(8L));
        ab.setAction(setDlTypeActionBuilder.build());
        actionList.add(ab.build());

        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field.MatchBuilder match = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field.MatchBuilder();
        match.setInPort(new Long(2));
        setFieldBuilder.setMatch(match.build());
        actionList.add(ab.build());

        SetMplsTtlActionBuilder setMplsTtlActionBuilder = new SetMplsTtlActionBuilder();
        setMplsTtlActionBuilder.setMplsTtl((short) 0X1);
        ab.setAction(setDlTypeActionBuilder.build());
        actionList.add(ab.build());

        SetNextHopActionBuilder setNextHopActionBuilder = new SetNextHopActionBuilder();
        Ipv4Builder ipnext = new Ipv4Builder();
        Ipv4Prefix prefix = new Ipv4Prefix("10.0.0.1/24");
        ipnext.setIpv4Address(prefix);
        setNextHopActionBuilder.setAddress(ipnext.build());
        ab.setAction(setNextHopActionBuilder.build());
        actionList.add(ab.build());

        SetNwDstActionBuilder setNwDstActionBuilder = new SetNwDstActionBuilder();
        Ipv4Builder ipdst = new Ipv4Builder();
        Ipv4Prefix prefixdst = new Ipv4Prefix("10.0.0.21/24");
        ipdst.setIpv4Address(prefixdst);
        setNwDstActionBuilder.setAddress(ipdst.build());
        ab.setAction(setNwDstActionBuilder.build());
        actionList.add(ab.build());

        SetNwSrcActionBuilder setNwsrcActionBuilder = new SetNwSrcActionBuilder();
        Ipv4Builder ipsrc = new Ipv4Builder();
        Ipv4Prefix prefixsrc = new Ipv4Prefix("10.0.23.21/24");
        ipsrc.setIpv4Address(prefixsrc);
        setNwsrcActionBuilder.setAddress(ipsrc.build());
        ab.setAction(setNwsrcActionBuilder.build());
        actionList.add(ab.build());

        SetNwTosActionBuilder setNwTosActionBuilder = new SetNwTosActionBuilder();
        setNwTosActionBuilder.setTos(1);
        ab.setAction(setNextHopActionBuilder.build());
        actionList.add(ab.build());

        SetNwTtlActionBuilder setNwTtlActionBuilder = new SetNwTtlActionBuilder();
        setNwTtlActionBuilder.setNwTtl((short) 1);
        ab.setAction(setNextHopActionBuilder.build());
        actionList.add(ab.build());

        SetQueueActionBuilder setQueueActionBuilder = new SetQueueActionBuilder();
        setQueueActionBuilder.setQueueId(1L);
        ab.setAction(setNextHopActionBuilder.build());
        actionList.add(ab.build());

        SetTpDstActionBuilder setTpDstActionBuilder = new SetTpDstActionBuilder();
        setTpDstActionBuilder.setPort(new PortNumber(109));
        ab.setAction(setNextHopActionBuilder.build());
        actionList.add(ab.build());

        SetTpSrcActionBuilder setTpSrcActionBuilder = new SetTpSrcActionBuilder();
        setTpSrcActionBuilder.setPort(new PortNumber(109));
        ab.setAction(setNextHopActionBuilder.build());
        actionList.add(ab.build());

        SetVlanCfiActionBuilder setVlanCfiActionBuilder = new SetVlanCfiActionBuilder();
        setVlanCfiActionBuilder.setVlanCfi(new VlanCfi(2));
        ab.setAction(setNextHopActionBuilder.build());
        actionList.add(ab.build());

        SwPathActionBuilder swPathAction = new SwPathActionBuilder();
        ab.setAction(swPathAction.build());
        actionList.add(ab.build());

        // Add our drop action to a list
        // actionList.add(ab.build());

        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(aab.build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    /**
     * @return
     */
    private static MatchBuilder createMatch1() {
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
        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createMatch2() {
        MatchBuilder match = new MatchBuilder();
        Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        Ipv4Prefix prefix = new Ipv4Prefix("10.0.0.1");
        ipv4Match.setIpv4Source(prefix);
        Ipv4Match i4m = ipv4Match.build();
        match.setLayer3Match(i4m);

        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x0800L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());
        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createMatch3() {
        MatchBuilder match = new MatchBuilder();
        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
        ethSourceBuilder.setAddress(new MacAddress("00:00:00:00:00:01"));
        ethernetMatch.setEthernetSource(ethSourceBuilder.build());
        match.setEthernetMatch(ethernetMatch.build());

        return match;
    }

    private static MatchBuilder createMatch4() {
        MatchBuilder match = new MatchBuilder();

        EthernetMatchBuilder ethmatch = new EthernetMatchBuilder(); // ethernettype
                                                                    // match
        EthernetTypeBuilder ethtype = new EthernetTypeBuilder();
        EtherType type = new EtherType(0x800L);
        ethmatch.setEthernetType(ethtype.setType(type).build());

        EthernetDestinationBuilder ethdest = new EthernetDestinationBuilder(); // ethernet
                                                                               // mac
                                                                               // address
                                                                               // macth
        MacAddress macdest = new MacAddress("ff:ff:ff:ff:ff:ff");
        ethdest.setAddress(macdest);
        ethmatch.setEthernetDestination(ethdest.build());

        EthernetSourceBuilder ethsrc = new EthernetSourceBuilder();
        MacAddress macsrc = new MacAddress("00:00:00:00:23:ae");
        ethsrc.setAddress(macsrc);
        ethmatch.setEthernetSource(ethsrc.build());

        VlanMatchBuilder vlanBuilder = new VlanMatchBuilder(); // vlan match
        VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
        VlanId vlanId = new VlanId(10);
        VlanPcp vpcp = new VlanPcp((short) 3);
        vlanBuilder.setVlanPcp(vpcp);
        vlanBuilder.setVlanId(vlanIdBuilder.setVlanId(vlanId).build());

        Ipv4Prefix dstip = new Ipv4Prefix("200.71.9.52/10"); // ipv4 match
        Ipv4Prefix srcip = new Ipv4Prefix("100.1.1.1/8");
        Ipv4MatchBuilder ipv4match = new Ipv4MatchBuilder();
        ipv4match.setIpv4Destination(dstip);
        ipv4match.setIpv4Source(srcip);

        IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        IpVersion ipv = IpVersion.Ipv4;
        //ipmatch.setIpProto(ipv);

        PortNumber srcport = new PortNumber(646);
        PortNumber dstport = new PortNumber(646);
        TcpMatchBuilder tcpmatch = new TcpMatchBuilder(); // tcp match
        tcpmatch.setTcpSourcePort(srcport);
        tcpmatch.setTcpDestinationPort(dstport);

        UdpMatchBuilder udpmatch = new UdpMatchBuilder(); // udp match
        udpmatch.setUdpDestinationPort(dstport);
        udpmatch.setUdpSourcePort(srcport);

        Ipv6Prefix srcip6 = new Ipv6Prefix("2001:0:0:0:0:0:0:1/56"); // ipv6
                                                                     // prefix
                                                                     // match
        Ipv6Prefix dstip6 = new Ipv6Prefix("2002::2/64");
        Ipv6MatchBuilder ipv6match = new Ipv6MatchBuilder();
        ipv6match.setIpv6Source(srcip6);
        ipv6match.setIpv6Destination(dstip6);

        IpMatchBuilder ipmatch2 = new IpMatchBuilder(); // ipv6 version
        IpVersion ipv2 = IpVersion.Ipv6;
       // ipmatch2.setIpProto(ipv);

        Icmpv4MatchBuilder icmpv4match = new Icmpv4MatchBuilder(); // icmpv4
                                                                   // match
        icmpv4match.setIcmpv4Type((short) 3);
        icmpv4match.setIcmpv4Code((short) 7);

        Icmpv6MatchBuilder icmpv6match = new Icmpv6MatchBuilder(); // icmpv6
                                                                   // match
        icmpv6match.setIcmpv6Code((short) 1);
        icmpv6match.setIcmpv6Type((short) 5);

        byte[] mask = new byte[] { (byte) -1, (byte) -1, 0, 0, 0, 0 };
        byte[] mask2 = new byte[] { (byte) -1, (byte) -1, (byte) -1, 0, 0, 0 };
        byte[] metamask = new byte[] { (byte) -1, (byte) -1, (byte) -1, 0, 0, 0, (byte) 1, (byte) 1 };

        ArpMatchBuilder arpmatch = new ArpMatchBuilder(); // arp match
        ArpSourceHardwareAddressBuilder arpsrc = new ArpSourceHardwareAddressBuilder();
        arpsrc.setAddress(macsrc);
        arpsrc.setMask(mask);
        ArpTargetHardwareAddressBuilder arpdst = new ArpTargetHardwareAddressBuilder();
        arpdst.setAddress(macdest);
        arpdst.setMask(mask2);
        arpmatch.setArpOp(2);
        arpmatch.setArpSourceHardwareAddress(arpsrc.build());
        arpmatch.setArpTargetHardwareAddress(arpdst.build());
        arpmatch.setArpSourceTransportAddress(srcip);
        arpmatch.setArpTargetTransportAddress(dstip);

        ProtocolMatchFieldsBuilder protomatch = new ProtocolMatchFieldsBuilder(); // mpls
                                                                                  // match
        protomatch.setMplsLabel((long) 36008);
        protomatch.setMplsTc((short) 4);
        protomatch.setMplsBos((short) 1);

        TunnelBuilder tunnel = new TunnelBuilder(); // tunnel id match
        tunnel.setTunnelId(BigInteger.valueOf(10668));
        match.setTunnel(tunnel.build());

        MetadataBuilder metadata = new MetadataBuilder(); // metadata match
        metadata.setMetadata(BigInteger.valueOf(500L));
        metadata.setMetadataMask(metamask);

        match.setInPort(101L);
        match.setInPhyPort(200L);
        match.setMetadata(metadata.build());
        match.setProtocolMatchFields(protomatch.build());
        match.setLayer3Match(arpmatch.build());
        match.setLayer3Match(ipv6match.build());
        match.setIpMatch(ipmatch.build());
        match.setLayer3Match(ipv4match.build());
        match.setIpMatch(ipmatch.build());
        match.setEthernetMatch(ethmatch.build());
        match.setVlanMatch(vlanBuilder.build());
        match.setLayer3Match(ipv4match.build());
        match.setIpMatch(ipmatch.build());
        match.setLayer4Match(tcpmatch.build());
        // match.setLayer4Match(udpmatch.build());
        // match.setIcmpv4Match(icmpv4match.build());
        // match.setIcmpv6Match(icmpv6match.build());
        return match;
    }

    public void _removeMDFlow(CommandInterpreter ci) {
        DataModification<InstanceIdentifier<?>, DataObject> modification = dataBrokerService.beginTransaction();
        NodeBuilder tn = createTestNode(ci.nextArgument());
        FlowBuilder tf = createTestFlow(tn, ci.nextArgument());
        InstanceIdentifier<Flow> path1 = InstanceIdentifier.builder(Flows.class).child(Flow.class, tf.getKey())
                .toInstance();
        modification.removeOperationalData(nodeBuilderToInstanceId(tn));
        modification.removeOperationalData(path1);
        modification.removeConfigurationData(nodeBuilderToInstanceId(tn));
        modification.removeConfigurationData(path1);
        Future<RpcResult<TransactionStatus>> commitFuture = modification.commit();
        try {
            RpcResult<TransactionStatus> result = commitFuture.get();
            TransactionStatus status = result.getResult();
            ci.println("Status of Flow Data Loaded Transaction: " + status);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void _addMDFlow(CommandInterpreter ci) {
        NodeBuilder tn = createTestNode(ci.nextArgument());
        FlowBuilder tf = createTestFlow(tn, ci.nextArgument());
        writeFlow(ci, tf, tn);
    }

    private void writeFlow(CommandInterpreter ci, FlowBuilder flow, NodeBuilder nodeBuilder) {
        DataModification<InstanceIdentifier<?>, DataObject> modification = dataBrokerService.beginTransaction();
        InstanceIdentifier<Flow> path1 = InstanceIdentifier.builder(Flows.class).child(Flow.class, flow.getKey())
                .toInstance();
        modification.putOperationalData(nodeBuilderToInstanceId(nodeBuilder), nodeBuilder.build());
        modification.putOperationalData(path1, flow.build());
        modification.putConfigurationData(nodeBuilderToInstanceId(nodeBuilder), nodeBuilder.build());
        modification.putConfigurationData(path1, flow.build());
        Future<RpcResult<TransactionStatus>> commitFuture = modification.commit();
        try {
            RpcResult<TransactionStatus> result = commitFuture.get();
            TransactionStatus status = result.getResult();
            ci.println("Status of Flow Data Loaded Transaction: " + status);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void _modifyMDFlow(CommandInterpreter ci) {
        NodeBuilder tn = createTestNode(ci.nextArgument());
        FlowBuilder tf = createTestFlow(tn, ci.nextArgument());
        tf.setFlowName(updatedFlowName);
        writeFlow(ci, tf, tn);
        tf.setFlowName(originalFlowName);
        writeFlow(ci, tf, tn);
    }

    private static NodeRef createNodeRef(String string) {
        NodeKey key = new NodeKey(new NodeId(string));
        InstanceIdentifier<Node> path = InstanceIdentifier.builder().node(Nodes.class).node(Node.class, key)
                .toInstance();

        return new NodeRef(path);
    }

    @Override
    public String getHelp() {
        return "No help";
    }
}
