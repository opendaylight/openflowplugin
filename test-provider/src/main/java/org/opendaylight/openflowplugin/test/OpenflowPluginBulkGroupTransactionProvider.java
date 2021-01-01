/*
 * Copyright (c) 2014, 2015 Ericsson, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Map;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtlBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.BucketId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowPluginBulkGroupTransactionProvider implements CommandProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowPluginBulkGroupTransactionProvider.class);
    private final DataBroker dataBroker;
    private final BundleContext ctx;
    private final String originalFlowName = "Foo";
    private final NodeErrorListener nodeErrorListener = new NodeErrorListenerLoggingImpl();
    private Node testNode12;
    private final String originalGroupName = "Foo";
    private final NotificationService notificationService;

    public OpenflowPluginBulkGroupTransactionProvider(final DataBroker dataBroker,
            final NotificationService notificationService, final BundleContext ctx) {
        this.dataBroker = dataBroker;
        this.notificationService = notificationService;
        this.ctx = ctx;
    }

    public void init() {
        notificationService.registerNotificationListener(nodeErrorListener);
        ctx.registerService(CommandProvider.class.getName(), this, null);
        createTestFlow(createTestNode(null), null, null);
    }

    private static NodeBuilder createTestNode(String nodeId) {
        if (nodeId == null) {
            nodeId = OpenflowpluginTestActivator.NODE_ID;
        }
        return new NodeBuilder().setId(new NodeId(nodeId));
    }

    private void createTestNode() {
        testNode12 = new NodeBuilder().setId(new NodeId(OpenflowpluginTestActivator.NODE_ID)).build();
    }

    @Override
    public String getHelp() {
        return "No help";
    }

    private static MatchBuilder createMatch1() {
        MatchBuilder match = new MatchBuilder();
        Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        Ipv4Prefix prefix = new Ipv4Prefix("10.0.0.1/24");
        ipv4Match.setIpv4Destination(prefix);
        Ipv4Match i4m = ipv4Match.build();
        match.setLayer3Match(i4m);

        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(Uint32.valueOf(0x0800)));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());
        return match;
    }

    private static InstructionsBuilder createDecNwTtlInstructions() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new DecNwTtlCaseBuilder().setDecNwTtl(new DecNwTtlBuilder().build()).build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static MatchBuilder createMatch2() {
        return new MatchBuilder()
            .setLayer3Match(new Ipv4MatchBuilder().setIpv4Source(new Ipv4Prefix("10.0.0.1")).build())
            .setEthernetMatch(new EthernetMatchBuilder()
                .setEthernetType(new EthernetTypeBuilder().setType(new EtherType(Uint32.valueOf(0x0800))).build())
                .build());
    }

    private static MatchBuilder createMatch3() {
        return new MatchBuilder()
            .setEthernetMatch(new EthernetMatchBuilder()
                .setEthernetSource(new EthernetSourceBuilder().setAddress(new MacAddress("00:00:00:00:00:01")).build())
                .build());
    }

    private static InstructionsBuilder createDropInstructions() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new DropActionCaseBuilder()
                                .setDropAction(new DropActionBuilder().build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static MatchBuilder createEthernetMatch() {
        return new MatchBuilder()
            .setEthernetMatch(new EthernetMatchBuilder()
                .setEthernetType(new EthernetTypeBuilder().setType(new EtherType(Uint32.valueOf(0x0800))).build())
                .setEthernetDestination(new EthernetDestinationBuilder()
                    .setAddress(new MacAddress("ff:ff:ff:ff:ff:ff"))
                    // .setMask(mask1);
                    .build())
                .setEthernetSource(new EthernetSourceBuilder()
                    .setAddress(new MacAddress("00:00:00:00:23:ae"))
                    // .setMask(mask2);
                    .build())
                .build());
    }

    private static InstructionsBuilder createMeterInstructions() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setInstruction(new MeterCaseBuilder()
                    .setMeter(new MeterBuilder().setMeterId(new MeterId(Uint32.ONE)).build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new ControllerActionCaseBuilder()
                                .setControllerAction(new ControllerActionBuilder()
                                    .setMaxLength(Uint16.valueOf(5))
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction7() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new SetVlanIdActionCaseBuilder()
                                .setSetVlanIdAction(new SetVlanIdActionBuilder()
                                    .setVlanId(new VlanId(Uint16.valueOf(4012)))
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction21() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new PopVlanActionCaseBuilder()
                                .setPopVlanAction(new PopVlanActionBuilder().build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction2() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new PushMplsActionCaseBuilder()
                                .setPushMplsAction(new PushMplsActionBuilder()
                                    .setEthernetType(Uint16.valueOf(0x8847))
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction3() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new PushPbbActionCaseBuilder()
                                .setPushPbbAction(new PushPbbActionBuilder()
                                    .setEthernetType(Uint16.valueOf(0x88E7))
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createGotoTableInstructions() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setInstruction(new GoToTableCaseBuilder()
                    .setGoToTable(new GoToTableBuilder().setTableId(Uint8.TWO).build())
                    .build())
                .build()));
    }

    private FlowBuilder createTestFlow(final NodeBuilder nodeBuilder, final String flowTypeArg, final String tableId) {

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

        final FlowKey key = new FlowKey(new FlowId(Long.toString(id)));
        if (null == flow.getBarrier()) {
            flow.setBarrier(Boolean.FALSE);
        }
        // flow.setBufferId(12L);
        flow.setCookie(new FlowCookie(Uint64.TEN));
        flow.setCookieMask(new FlowCookie(Uint64.TEN));
        flow.setHardTimeout(Uint16.ZERO);
        flow.setIdleTimeout(Uint16.ZERO);
        flow.setInstallHw(false);
        flow.setStrict(false);
        flow.setContainerName(null);
        flow.setFlags(new FlowModFlags(false, false, false, false, true));
        flow.setId(new FlowId("12"));
        flow.setTableId(getTableId(tableId));
        flow.setOutGroup(Uint32.MAX_VALUE);
        // set outport to OFPP_NONE (65535) to disable remove restriction for
        // flow
        flow.setOutPort(Uint64.valueOf(4294967295L));

        flow.withKey(key);
        flow.setPriority(Uint16.TWO);
        flow.setFlowName(originalFlowName + "X" + flowType);
        return flow;
    }

    private static Uint8 getTableId(final String tableId) {
        Uint8 table = Uint8.TWO;
        if (tableId == null) {
            return table;
        }

        try {
            table = Uint8.valueOf(tableId);
        } catch (IllegalArgumentException ex) {
            // ignore exception and continue with default value
        }

        return table;

    }

    @SuppressWarnings("checkstyle:MethodName")
    public void _addGroups(final CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        int count = Integer.parseInt(ci.nextArgument());
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
            default:
                break;
        }
    }

    private void createUserNode(final String nodeRef) {
        testNode12 = new NodeBuilder().setId(new NodeId(nodeRef)).build();
    }

    @SuppressWarnings("checkstyle:MethodName")
    public void _modifyGroups(final CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        int count = Integer.parseInt(ci.nextArgument());
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
            default:
                break;
        }
    }

    private static InstanceIdentifier<Node> nodeToInstanceId(final Node node) {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, node.key());
    }

    @SuppressWarnings("checkstyle:MethodName")
    public void _removeGroups(final CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }

        int count = Integer.parseInt(ci.nextArgument());
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
            default:
                break;
        }
    }

    private void writeGroup(final CommandInterpreter ci, final Group group, final Group group1) {
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();

        InstanceIdentifier<Group> path1 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, testNode12.key()).augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(group.getGroupId()));
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, nodeToInstanceId(testNode12),
                testNode12);
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, path1, group);

        InstanceIdentifier<Group> path2 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, testNode12.key()).augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(group1.getGroupId()));
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, nodeToInstanceId(testNode12),
                testNode12);
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, path2, group1);
        modification.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(final CommitInfo notUsed) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(final Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        }, MoreExecutors.directExecutor());
    }

    private void deleteGroup(final CommandInterpreter ci, final Group group, final Group group1) {
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        InstanceIdentifier<Group> path1 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, testNode12.key()).augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(group.getGroupId()));
        modification.delete(LogicalDatastoreType.OPERATIONAL, path1);
        modification.delete(LogicalDatastoreType.CONFIGURATION, path1);
        InstanceIdentifier<Group> path2 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, testNode12.key()).augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(group1.getGroupId()));
        modification.delete(LogicalDatastoreType.OPERATIONAL, path2);
        modification.delete(LogicalDatastoreType.CONFIGURATION, path2);
        modification.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(final CommitInfo notUsed) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(final Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        }, MoreExecutors.directExecutor());
    }

    private GroupBuilder createTestGroup(String actionType, String groupType, final String groupmod,
            final String strId) {
        // Sample data , committing to DataStore

        GroupBuilder group = new GroupBuilder();
        BucketBuilder bucket = new BucketBuilder();
        bucket.withKey(new BucketKey(new BucketId(Uint32.valueOf(12))));

        if (groupType == null) {
            groupType = "g1";
        }
        if (actionType == null) {
            actionType = "a1";
        }

        switch (groupType) {
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
            default:
                break;
        }

        switch (actionType) {
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
            default:
                break;
        }

        if ("add".equals(groupmod)) {
            bucket.setWatchGroup(Uint32.valueOf(14));
            bucket.setWatchPort(Uint32.valueOf(1234));
            bucket.setWeight(Uint16.valueOf(50));
        } else {
            bucket.setWatchGroup(Uint32.valueOf(13));
            bucket.setWatchPort(Uint32.valueOf(134));
            bucket.setWeight(Uint16.valueOf(30));
        }

        return group.withKey(new GroupKey(new GroupId(Uint32.valueOf(strId))))
            // .group.setInstall(false)
            .setGroupName(originalGroupName)
            .setBarrier(false)
            .setBuckets(new BucketsBuilder().setBucket(BindingMap.of(bucket.build())).build());
    }

    private static Map<ActionKey, Action> createPopVlanAction() {
        return BindingMap.of(new ActionBuilder()
            .setOrder(0)
            .setAction(new PopVlanActionCaseBuilder().setPopVlanAction(new PopVlanActionBuilder().build()).build())
            .build());
    }

    private static Map<ActionKey, Action> createPushVlanAction() {
        return BindingMap.of(new ActionBuilder()
            .setOrder(0)
            .setAction(new PushVlanActionCaseBuilder()
                .setPushVlanAction(new PushVlanActionBuilder()
                    .setEthernetType(Uint16.valueOf(0x8100))
                    .setVlanId(new VlanId(Uint16.TWO))
                    .build())
                .build())
            .build());
    }

    private static Map<ActionKey, Action> createPushMplsAction() {
        return BindingMap.of(new ActionBuilder()
            .setOrder(0)
            .setAction(new PushMplsActionCaseBuilder()
                .setPushMplsAction(new PushMplsActionBuilder().setEthernetType(Uint16.valueOf(0x8847)).build())
                .build())
            .build());
    }

    private static Map<ActionKey, Action> createPopMplsAction() {
        return BindingMap.of(new ActionBuilder()
            .setOrder(0)
            .setAction(new PopMplsActionCaseBuilder()
                .setPopMplsAction(new PopMplsActionBuilder().setEthernetType(Uint16.valueOf(0xB)).build())
                .build())
            .build());
    }

    private static Map<ActionKey, Action> createPopPbbAction() {
        return BindingMap.of(new ActionBuilder()
            .setOrder(0)
            .setAction(new PopPbbActionCaseBuilder().setPopPbbAction(new PopPbbActionBuilder().build()).build())
            .build());
    }

    private static Map<ActionKey, Action> createPushPbbAction() {
        return BindingMap.of(new ActionBuilder()
            .setOrder(0)
            .setAction(new PushPbbActionCaseBuilder()
                .setPushPbbAction(new PushPbbActionBuilder().setEthernetType(Uint16.valueOf(0x88E7)).build())
                .build())
            .build());
    }

    private static Map<ActionKey, Action> createCopyTtlInAction() {
        return BindingMap.of(new ActionBuilder()
            .setOrder(0)
            .setAction(new CopyTtlInCaseBuilder().setCopyTtlIn(new CopyTtlInBuilder().build()).build())
            .build());
    }

    private static Map<ActionKey, Action> createCopyTtlOutAction() {
        return BindingMap.of(new ActionBuilder()
            .setOrder(0)
            .setAction(new CopyTtlOutCaseBuilder().setCopyTtlOut(new CopyTtlOutBuilder().build()).build())
            .build());
    }

    private static Map<ActionKey, Action> createDecMplsTtlAction() {
        return BindingMap.of(new ActionBuilder()
            .setOrder(0)
            .setAction(new DecMplsTtlCaseBuilder().setDecMplsTtl(new DecMplsTtlBuilder().build()).build())
            .build());
    }

    private static Map<ActionKey, Action> createGroupAction() {
        return BindingMap.of(new ActionBuilder()
            .setOrder(0)
            .setAction(new GroupActionCaseBuilder()
                .setGroupAction(new GroupActionBuilder().setGroupId(Uint32.ONE).setGroup("0").build())
                .build())
            .build());
    }

    private static Map<ActionKey, Action> createNonAppyPushVlanAction() {
        return BindingMap.of(new ActionBuilder()
            .setOrder(0)
            .setAction(new GroupActionCaseBuilder()
                .setGroupAction(new GroupActionBuilder().setGroupId(Uint32.ONE).setGroup("0").build())
                .build())
            .build());
    }
}
