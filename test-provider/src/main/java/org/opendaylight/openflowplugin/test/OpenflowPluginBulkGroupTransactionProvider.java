/*
 * Copyright (c) 2014, 2015 Ericsson, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.test;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.ControllerActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.controller.action._case.ControllerActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.in._case.CopyTtlInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.out._case.CopyTtlOutBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.mpls.ttl._case.DecMplsTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.mpls.action._case.PopMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.pbb.action._case.PopPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.vlan.action._case.PopVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.mpls.action._case.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.pbb.action._case.PushPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.id.action._case.SetVlanIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.go.to.table._case.GoToTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.meter._case.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.BucketId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.NodeErrorListener;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.M

public class OpenflowPluginBulkGroupTransactionProvider implements CommandProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowPluginBulkGroupTransactionProvider.class);
    private NodeBuilder testNode;
    private DataBroker dataBroker;
    private final BundleContext ctx;
    private ProviderContext pc;
    private FlowBuilder testFlow;
    private final String originalFlowName = "Foo";
    private final NodeErrorListener nodeErrorListener = new NodeErrorListenerLoggingImpl();
    private Registration listener1Reg;
    private Registration listener2Reg;
    private Group testGroup;
    private Group testGroup2;
    private Node testNode12;
    private final String originalGroupName = "Foo";
    private static NotificationService notificationService;

    public OpenflowPluginBulkGroupTransactionProvider(BundleContext ctx) {
        this.ctx = ctx;
    }

    public void onSessionInitiated(ProviderContext session) {
        pc = session;
        notificationService = session.getSALService(NotificationService.class);
        listener2Reg = notificationService.registerNotificationListener(nodeErrorListener);
        dataBroker = session.getSALService(DataBroker.class);
        ctx.registerService(CommandProvider.class.getName(), this, null);
        createTestFlow(createTestNode(null), null, null);
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

    private static NodeRef createNodeRef(String string) {
        NodeKey key = new NodeKey(new NodeId(string));
        InstanceIdentifier<Node> path = InstanceIdentifier.create(Nodes.class).child(Node.class, key);
        return new NodeRef(path);
    }

    @Override
    public String getHelp() {
        return "No help";
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
    private static InstructionsBuilder createDecNwTtlInstructions() {
        DecNwTtlBuilder ta = new DecNwTtlBuilder();
        DecNwTtl decNwTtl = ta.build();
        ActionBuilder ab = new ActionBuilder();
        ab.setAction(new DecNwTtlCaseBuilder().setDecNwTtl(decNwTtl).build());

        // Add our drop action to a list
        List<Action> actionList = new ArrayList<Action>();
        actionList.add(ab.build());

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
        isb.setInstruction(instructions);
        return isb;
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

    private static InstructionsBuilder createDropInstructions() {
        DropActionBuilder dab = new DropActionBuilder();
        DropAction dropAction = dab.build();
        ActionBuilder ab = new ActionBuilder();
        ab.setAction(new DropActionCaseBuilder().setDropAction(dropAction).build());

        // Add our drop action to a list
        List<Action> actionList = new ArrayList<Action>();
        actionList.add(ab.build());

        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static MatchBuilder createEthernetMatch() {
        MatchBuilder match = new MatchBuilder();

        byte[] mask1 = new byte[]{(byte) -1, (byte) -1, 0, 0, 0, 0};
        byte[] mask2 = new byte[]{(byte) -1, (byte) -1, (byte) -1, 0, 0, 0};

        EthernetMatchBuilder ethmatch = new EthernetMatchBuilder(); // ethernettype
        // match
        EthernetTypeBuilder ethtype = new EthernetTypeBuilder();
        EtherType type = new EtherType(0x0800L);
        ethmatch.setEthernetType(ethtype.setType(type).build());

        EthernetDestinationBuilder ethdest = new EthernetDestinationBuilder(); // ethernet
        // macaddress
        // match
        MacAddress macdest = new MacAddress("ff:ff:ff:ff:ff:ff");
        ethdest.setAddress(macdest);
        // ethdest.setMask(mask1);

        ethmatch.setEthernetDestination(ethdest.build());

        EthernetSourceBuilder ethsrc = new EthernetSourceBuilder();
        MacAddress macsrc = new MacAddress("00:00:00:00:23:ae");
        ethsrc.setAddress(macsrc);
        // ethsrc.setMask(mask2);

        ethmatch.setEthernetSource(ethsrc.build());
        match.setEthernetMatch(ethmatch.build());
        return match;

    }

    /**
     * @return
     */
    private static InstructionsBuilder createMeterInstructions() {

        MeterBuilder aab = new MeterBuilder();
        aab.setMeterId(new MeterId(1L));

        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new MeterCaseBuilder().setMeter(aab.build()).build());

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
        ab.setAction(new ControllerActionCaseBuilder().setControllerAction(controller.build()).build());
        actionList.add(ab.build());
        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction7() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetVlanIdActionBuilder vl = new SetVlanIdActionBuilder();
        VlanId a = new VlanId(4012);
        vl.setVlanId(a);
        ab.setAction(new SetVlanIdActionCaseBuilder().setSetVlanIdAction(vl.build()).build());
        actionList.add(ab.build());
        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction21() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        PopVlanActionBuilder popVlanActionBuilder = new PopVlanActionBuilder();
        ab.setAction(new PopVlanActionCaseBuilder().setPopVlanAction(popVlanActionBuilder.build()).build());
        actionList.add(ab.build());

        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction2() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        PushMplsActionBuilder push = new PushMplsActionBuilder();
        push.setEthernetType(0x8847);
        ab.setAction(new PushMplsActionCaseBuilder().setPushMplsAction(push.build()).build());
        actionList.add(ab.build());
        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction3() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        PushPbbActionBuilder pbb = new PushPbbActionBuilder();
        pbb.setEthernetType(0x88E7);
        ab.setAction(new PushPbbActionCaseBuilder().setPushPbbAction(pbb.build()).build());
        actionList.add(ab.build());
        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

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
        ib.setInstruction(new GoToTableCaseBuilder().setGoToTable(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private FlowBuilder createTestFlow(NodeBuilder nodeBuilder, String flowTypeArg, String tableId) {

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
                flow.setMatch(createEthernetMatch().build());
                flow.setInstructions(createDropInstructions().build());
                break;
            case "f82":
                id += 1;
                flow.setMatch(createMatch1().build());
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
            case "f8":
                id += 8;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createAppyActionInstruction7().build());
                break;
            case "f9":
                id += 9;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createAppyActionInstruction2().build());
                break;
            case "f10":
                id += 10;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createAppyActionInstruction3().build());
                break;
            case "f14":
                id += 14;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createAppyActionInstruction7().build());
                break;
            case "f29":
                id += 29;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createAppyActionInstruction21().build());
                break;

            default:
                LOG.warn("flow type not understood: {}", flowType);
        }

        FlowKey key = new FlowKey(new FlowId(Long.toString(id)));
        if (null == flow.isBarrier()) {
            flow.setBarrier(Boolean.FALSE);
        }
        // flow.setBufferId(12L);
        BigInteger value = BigInteger.valueOf(10);
        BigInteger outputPort = BigInteger.valueOf(4294967295L);
        flow.setCookie(new FlowCookie(value));
        flow.setCookieMask(new FlowCookie(value));
        flow.setHardTimeout(0);
        flow.setIdleTimeout(0);
        flow.setInstallHw(false);
        flow.setStrict(false);
        flow.setContainerName(null);
        flow.setFlags(new FlowModFlags(false, false, false, false, true));
        flow.setId(new FlowId("12"));
        flow.setTableId(getTableId(tableId));
        flow.setOutGroup(4294967295L);
        // set outport to OFPP_NONE (65535) to disable remove restriction for
        // flow
        flow.setOutPort(outputPort);

        flow.setKey(key);
        flow.setPriority(2);
        flow.setFlowName(originalFlowName + "X" + flowType);
        testFlow = flow;
        return flow;
    }

    private short getTableId(String tableId) {
        short table = 2;
        try {
            table = Short.parseShort(tableId);
        } catch (Exception ex) {
            // ignore exception and continue with default value
        }

        return table;

    }

    public void _addGroups(CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        Integer count = Integer.parseInt(ci.nextArgument());
        switch (count) {
            case 1:
                GroupBuilder group = createTestGroup("a7", "g1", "add", "1");
                GroupBuilder group1 = createTestGroup("a3", "g1", "add", "2");
                writeGroup(ci, group.build(), group1.build());
                break;
            case 2:
                GroupBuilder group2 = createTestGroup("a4", "g1", "add", "4");
                GroupBuilder group3 = createTestGroup("a5", "g1", "add", "5");
                writeGroup(ci, group2.build(), group3.build());
                break;
            case 3:
                GroupBuilder group4 = createTestGroup("a6", "g1", "add", "6");
                GroupBuilder group5 = createTestGroup("a7", "g1", "add", "7");
                writeGroup(ci, group4.build(), group5.build());
                break;
            case 4:
                // -ve
                GroupBuilder group6 = createTestGroup("a14", "g1", "add", "5");
                GroupBuilder group7 = createTestGroup("a3", "g1", "add", "6");
                writeGroup(ci, group6.build(), group7.build());
                break;

        }

    }

    private void createUserNode(String nodeRef) {
        NodeRef nodeOne = createNodeRef(nodeRef);
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeRef));
        builder.setKey(new NodeKey(builder.getId()));
        testNode12 = builder.build();
    }

    public void _modifyGroups(CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        Integer count = Integer.parseInt(ci.nextArgument());
        switch (count) {
            case 1:
                GroupBuilder group = createTestGroup("a4", "g1", "modify", "1");
                GroupBuilder group1 = createTestGroup("a5", "g1", "modify", "2");
                writeGroup(ci, group.build(), group1.build());
                break;
            case 2:
                GroupBuilder group2 = createTestGroup("a1", "g1", "modify", "4");
                GroupBuilder group3 = createTestGroup("a2", "g1", "modify", "5");
                writeGroup(ci, group2.build(), group3.build());
                break;
            case 3:
                GroupBuilder group4 = createTestGroup("a9", "g1", "modify", "6");
                GroupBuilder group5 = createTestGroup("a10", "g1", "modify", "7");
                writeGroup(ci, group4.build(), group5.build());
                break;

            case 4:
                GroupBuilder group6 = createTestGroup("a6", "g1", "modify", "5");
                GroupBuilder group7 = createTestGroup("a29", "g1", "modify", "6");
                writeGroup(ci, group6.build(), group7.build());
                break;
        }
    }

    private InstanceIdentifier<Node> nodeToInstanceId(Node node) {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, node.getKey());
    }

    private void createTestNode() {
        NodeRef nodeOne = createNodeRef(OpenflowpluginTestActivator.NODE_ID);
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(OpenflowpluginTestActivator.NODE_ID));
        builder.setKey(new NodeKey(builder.getId()));
        testNode12 = builder.build();
    }

    public void _removeGroups(CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }

        Integer count = Integer.parseInt(ci.nextArgument());
        switch (count) {
            case 1:
                GroupBuilder group = createTestGroup("a2", "g1", "remove", "1");
                GroupBuilder group1 = createTestGroup("a3", "g1", "remove", "2");
                deleteGroup(ci, group.build(), group1.build());
                break;
            case 2:
                GroupBuilder group2 = createTestGroup("a4", "g1", "remove", "4");
                GroupBuilder group3 = createTestGroup("a5", "g1", "remove", "5");
                deleteGroup(ci, group2.build(), group3.build());
                break;
            case 3:
                GroupBuilder group4 = createTestGroup("a6", "g1", "remove", "6");
                GroupBuilder group5 = createTestGroup("a7", "g1", "remove", "7");
                deleteGroup(ci, group4.build(), group5.build());
                break;
            case 4:
                GroupBuilder group6 = createTestGroup("a14", "g1", "remove", "5");
                GroupBuilder group7 = createTestGroup("a3", "g1", "remove", "6");
                deleteGroup(ci, group6.build(), group7.build());
                break;
            case 5:
                GroupBuilder group8 = createTestGroup("a4", "g1", "modify", "1");
                GroupBuilder group9 = createTestGroup("a5", "g1", "modify", "2");
                writeGroup(ci, group8.build(), group9.build());
                break;
            case 6:
                GroupBuilder group10 = createTestGroup("a1", "g1", "modify", "4");
                GroupBuilder group11 = createTestGroup("a2", "g1", "modify", "5");
                writeGroup(ci, group10.build(), group11.build());
                break;
            case 7:
                GroupBuilder group12 = createTestGroup("a9", "g1", "modify", "6");
                GroupBuilder group13 = createTestGroup("a10", "g1", "modify", "7");
                writeGroup(ci, group12.build(), group13.build());
                break;

            case 8:
                GroupBuilder group14 = createTestGroup("a6", "g1", "modify", "5");
                GroupBuilder group15 = createTestGroup("a29", "g1", "modify", "6");
                writeGroup(ci, group14.build(), group15.build());
                break;

        }

    }

    private void writeGroup(final CommandInterpreter ci, Group group, Group group1) {
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();

        InstanceIdentifier<Group> path1 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, testNode12.getKey()).augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(group.getGroupId()));
        modification.merge(LogicalDatastoreType.CONFIGURATION, nodeToInstanceId(testNode12), testNode12, true);
        modification.merge(LogicalDatastoreType.CONFIGURATION, path1, group, true);

        InstanceIdentifier<Group> path2 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, testNode12.getKey()).augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(group1.getGroupId()));
        modification.merge(LogicalDatastoreType.CONFIGURATION, nodeToInstanceId(testNode12), testNode12, true);
        modification.merge(LogicalDatastoreType.CONFIGURATION, path2, group1, true);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        Futures.addCallback(commitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        });
    }

    private void deleteGroup(final CommandInterpreter ci, Group group, Group group1) {
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        InstanceIdentifier<Group> path1 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, testNode12.getKey()).augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(group.getGroupId()));
        modification.delete(LogicalDatastoreType.OPERATIONAL, path1);
        modification.delete(LogicalDatastoreType.CONFIGURATION, path1);
        InstanceIdentifier<Group> path2 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, testNode12.getKey()).augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(group1.getGroupId()));
        modification.delete(LogicalDatastoreType.OPERATIONAL, path2);
        modification.delete(LogicalDatastoreType.CONFIGURATION, path2);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        Futures.addCallback(commitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        });
    }

    private GroupBuilder createTestGroup(String actiontype, String type, String mod, String iD) {
        // Sample data , committing to DataStore

        String GroupType = type;
        String ActionType = actiontype;
        String Groupmod = mod;

        long id = Long.parseLong(iD);
        GroupKey key = new GroupKey(new GroupId(id));
        GroupBuilder group = new GroupBuilder();
        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 12));
        bucket.setKey(new BucketKey(new BucketId((long) 12)));

        if (GroupType == null) {
            GroupType = "g1";
        }
        if (ActionType == null) {
            ActionType = "a1";
        }

        switch (GroupType) {
            case "g1":
                group.setGroupType(GroupTypes.GroupSelect);
                break;
            case "g2":
                group.setGroupType(GroupTypes.GroupAll);
                break;
            case "g3":
                group.setGroupType(GroupTypes.GroupIndirect);
                break;
            case "g4":
                group.setGroupType(GroupTypes.GroupFf);
                break;
        }

        switch (ActionType) {
            case "a1":
                bucket.setAction(createPopVlanAction());
                break;
            case "a2":
                bucket.setAction(createPushVlanAction());
                break;
            case "a3":
                bucket.setAction(createPushMplsAction());
                break;
            case "a4":
                bucket.setAction(createPopMplsAction());
                break;
            case "a5":
                bucket.setAction(createPopPbbAction());
                break;
            case "a6":
                bucket.setAction(createPushPbbAction());
                break;
            case "a7":
                bucket.setAction(createPushPbbAction());
                break;
            case "a8":
                bucket.setAction(createCopyTtlInAction());
                break;
            case "a9":
                bucket.setAction(createCopyTtlOutAction());
                break;
            case "a10":
                bucket.setAction(createDecMplsTtlAction());
                break;
            case "a14":
                bucket.setAction(createGroupAction());
                break;
            case "a29":
                bucket.setAction(createNonAppyPushVlanAction());
                break;

        }

        if (Groupmod == "add") {
            bucket.setWatchGroup((long) 14);
            bucket.setWatchPort((long) 1234);
            bucket.setWeight(50);
        } else {
            bucket.setWatchGroup((long) 13);
            bucket.setWatchPort((long) 134);
            bucket.setWeight(30);
        }
        group.setKey(key);
        // group.setInstall(false);
        group.setGroupId(new GroupId(id));
        group.setGroupName(originalGroupName);
        group.setBarrier(false);
        BucketsBuilder value = new BucketsBuilder();
        List<Bucket> value1 = new ArrayList<Bucket>();
        value1.add(bucket.build());
        value.setBucket(value1);
        group.setBuckets(value.build());
        testGroup = group.build();
        return group;
    }

    private List<Action> createPopVlanAction() {
        PopVlanActionBuilder vlanAction = new PopVlanActionBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PopVlanActionCaseBuilder().setPopVlanAction(vlanAction.build()).build());
        action.setKey(new ActionKey(0));
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createPushVlanAction() {
        PushVlanActionBuilder vlan = new PushVlanActionBuilder();
        vlan.setEthernetType(0x8100);
        VlanId v = new VlanId(2);
        vlan.setVlanId(v);
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PushVlanActionCaseBuilder().setPushVlanAction(vlan.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createPushMplsAction() {
        PushMplsActionBuilder push = new PushMplsActionBuilder();
        push.setEthernetType(0x8847);
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PushMplsActionCaseBuilder().setPushMplsAction(push.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createPopMplsAction() {
        PopMplsActionBuilder popMplsActionBuilder = new PopMplsActionBuilder();
        popMplsActionBuilder.setEthernetType(0XB);
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PopMplsActionCaseBuilder().setPopMplsAction(popMplsActionBuilder.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createPopPbbAction() {
        PopPbbActionBuilder popPbbActionBuilder = new PopPbbActionBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PopPbbActionCaseBuilder().setPopPbbAction(popPbbActionBuilder.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createPushPbbAction() {
        PushPbbActionBuilder pbb = new PushPbbActionBuilder();
        pbb.setEthernetType(0x88E7);
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PushPbbActionCaseBuilder().setPushPbbAction(pbb.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createCopyTtlInAction() {
        CopyTtlInBuilder ttlin = new CopyTtlInBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new CopyTtlInCaseBuilder().setCopyTtlIn(ttlin.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createCopyTtlOutAction() {
        CopyTtlOutBuilder ttlout = new CopyTtlOutBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new CopyTtlOutCaseBuilder().setCopyTtlOut(ttlout.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createDecMplsTtlAction() {
        DecMplsTtlBuilder mpls = new DecMplsTtlBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new DecMplsTtlCaseBuilder().setDecMplsTtl(mpls.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createGroupAction() {

        GroupActionBuilder groupActionB = new GroupActionBuilder();
        groupActionB.setGroupId(1L);
        groupActionB.setGroup("0");
        ActionBuilder action = new ActionBuilder();
        action.setAction(new GroupActionCaseBuilder().setGroupAction(groupActionB.build()).build());
        action.setKey(new ActionKey(0));
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private static List<Action> createNonAppyPushVlanAction() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        GroupActionBuilder groupActionB = new GroupActionBuilder();
        groupActionB.setGroupId(1L);
        groupActionB.setGroup("0");
        ab.setAction(new GroupActionCaseBuilder().setGroupAction(groupActionB.build()).build());
        actionList.add(ab.build());

        return actionList;
    }

}
