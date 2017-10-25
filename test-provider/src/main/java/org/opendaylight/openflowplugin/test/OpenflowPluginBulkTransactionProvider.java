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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.ControllerActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.FloodAllActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTosActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanPcpActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SwPathActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.controller.action._case.ControllerActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.in._case.CopyTtlInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.flood.all.action._case.FloodAllActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.mpls.action._case.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.pbb.action._case.PushPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.src.action._case.SetDlSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.dst.action._case.SetNwDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.src.action._case.SetNwSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.tos.action._case.SetNwTosActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.id.action._case.SetVlanIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.pcp.action._case.SetVlanPcpActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.sw.path.action._case.SwPathActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6ExtHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6LabelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.MetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.NodeErrorListener;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.M

public class OpenflowPluginBulkTransactionProvider implements CommandProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowPluginBulkTransactionProvider.class);
    private DataBroker dataBroker;
    private final BundleContext ctx;
    private NodeBuilder testNode;
    private ProviderContext pc;
    private FlowBuilder testFlow;
    private final String originalFlowName = "Foo";
    private final NodeErrorListener nodeErrorListener = new NodeErrorListenerLoggingImpl();
    private Registration listener1Reg;
    private Registration listener2Reg;
    private Node testNode12;
    private final String originalGroupName = "Foo";
    private static NotificationService notificationService;

    public OpenflowPluginBulkTransactionProvider(BundleContext ctx) {
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
            case "f23":
                id += 23;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createAppyActionInstruction16().build());
                break;
            case "f230":
                id += 23;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createAppyActionInstruction160().build());
                break;
            case "f34":
                id += 34;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createAppyActionInstruction26().build());
                break;
            case "f35":
                id += 35;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createAppyActionInstruction27().build());
                break;
            case "f36":
                id += 36;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createAppyActionInstruction28().build());
                break;
            case "f42":
                id += 42;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createAppyActionInstruction34().build());
                break;
            case "f43":
                id += 43;
                flow.setMatch(createICMPv6Match().build());
                flow.setInstructions(createDecNwTtlInstructions().build());
                break;
            case "f44":
                id += 44;
                flow.setMatch(createInphyportMatch(nodeBuilder.getId()).build());
                flow.setInstructions(createDropInstructions().build());
                break;
            case "f45":
                id += 45;
                flow.setMatch(createMetadataMatch().build());
                flow.setInstructions(createDropInstructions().build());
                break;
            case "f46":
                id += 46;
                flow.setMatch(createL3IPv6Match().build());
                flow.setInstructions(createDecNwTtlInstructions().build());
                break;
            case "f81":
                id += 81;
                flow.setMatch(createLLDPMatch().build());
                flow.setInstructions(createSentToControllerInstructions().build());
                break;

            case "f82":
                id += 1;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createDropInstructions().build());
                break;
            case "f83":
                id += 2;
                flow.setMatch(createMatch2().build());
                flow.setInstructions(createDecNwTtlInstructions().build());
                break;
            case "f84":
                id += 3;
                flow.setMatch(createMatch3().build());
                flow.setInstructions(createDecNwTtlInstructions().build());
                break;
            case "f85":
                id += 4;
                flow.setMatch(createEthernetMatch().build());
                flow.setInstructions(createMeterInstructions().build());
                break;
            case "f86":
                id += 6;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createDecNwTtlInstructions().build());
                break;
            case "f87":
                id += 12;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createAppyActionInstruction7().build());
                break;
            case "f88":
                id += 13;
                flow.setMatch(createEthernetMatch().build());
                flow.setInstructions(createAppyActionInstruction6().build());
                break;
            case "f89":
                id += 14;
                flow.setMatch(createEthernetMatch().build());
                flow.setInstructions(createAppyActionInstruction7().build());
                break;
            case "f90":
                id += 15;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createAppyActionInstruction9().build());
                break;
            case "f91":
                id += 7;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createAppyActionInstruction9().build());
                break;
            case "f92":
                id += 8;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createAppyActionInstruction6().build());
                break;
            case "f93":
                id += 9;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createDecNwTtlInstructions().build());
                break;
            case "f94":
                id += 10;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createDecNwTtlInstructions().build());
                break;
            case "f95":
                id += 42;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createDecNwTtlInstructions().build());
                break;
            case "f96":
                id += 43;
                flow.setMatch(createICMPv6Match().build());
                flow.setInstructions(createDropInstructions().build());
                break;
            case "f97":
                id += 44;
                flow.setMatch(createInphyportMatch(nodeBuilder.getId()).build());
                flow.setInstructions(createMeterInstructions().build());
                break;
            case "f98":
                id += 45;
                flow.setMatch(createMetadataMatch().build());
                flow.setInstructions(createAppyActionInstruction6().build());
                break;
            case "f99":
                id += 34;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createAppyActionInstruction6().build());
                break;
            case "f100":
                id += 35;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createAppyActionInstruction7().build());
                break;
            case "f101":
                id += 36;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createAppyActionInstruction8().build());
                break;
            case "f700":
                id += 3;
                flow.setMatch(createMatch3().build());
                flow.setInstructions(createMeterInstructions().build());
                break;
            case "f800":
                id += 8;
                flow.setMatch(createMatch1000().build());
                flow.setInstructions(createAppyActionInstruction6().build());
                break;
            case "f900":
                id += 5;
                flow.setMatch(createMatch1000().build());
                flow.setInstructions(createAppyActionInstruction2().build());
                break;
            case "f1000":
                id += 10;
                flow.setMatch(createMatch1000().build());
                flow.setInstructions(createAppyActionInstruction3().build());
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

    private void createTestNode() {
        NodeRef nodeOne = createNodeRef(OpenflowpluginTestActivator.NODE_ID);
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(OpenflowpluginTestActivator.NODE_ID));
        builder.setKey(new NodeKey(builder.getId()));
        testNode12 = builder.build();
    }

    public void _addFlows(CommandInterpreter ci) {
        NodeBuilder tn = createTestNode(ci.nextArgument());
        String flowtype = ci.nextArgument();
        Integer flowcnt = Integer.parseInt(flowtype);
        FlowBuilder tf;
        FlowBuilder tf1;
        FlowBuilder tf2;
        FlowBuilder tf3;
        switch (flowcnt) {
            case 1:
                tf = createTestFlow(tn, "f1", "10");
                tf1 = createTestFlow(tn, "f2", "11");
                tf2 = createTestFlow(tn, "f3", "12");
                tf3 = createTestFlow(tn, "f4", "13");
                break;
            case 2:
                tf = createTestFlow(tn, "f3", "3");
                tf1 = createTestFlow(tn, "f4", "4");
                tf2 = createTestFlow(tn, "f5", "5");
                tf3 = createTestFlow(tn, "f6", "6");
                break;
            case 3:
                tf = createTestFlow(tn, "f7", "7");
                tf1 = createTestFlow(tn, "f8", "8");
                tf2 = createTestFlow(tn, "f9", "9");
                tf3 = createTestFlow(tn, "f10", "10");
                break;
            case 4:
                // -ve scenario
                tf = createTestFlow(tn, "f23", "3");
                tf1 = createTestFlow(tn, "f34", "4");
                tf2 = createTestFlow(tn, "f35", "5");
                tf3 = createTestFlow(tn, "f36", "6");
                break;
            case 5:
                // +ve scenario
                // modify case 6 -ve
                tf = createTestFlow(tn, "f230", "3");
                tf1 = createTestFlow(tn, "f34", "4");
                tf2 = createTestFlow(tn, "f35", "5");
                tf3 = createTestFlow(tn, "f36", "6");
                break;

            default:
                tf = createTestFlow(tn, "f42", "42");
                tf1 = createTestFlow(tn, "f43", "43");
                tf2 = createTestFlow(tn, "f44", "44");
                tf3 = createTestFlow(tn, "f45", "45");

        }
        writeFlow(ci, tf, tf1, tf2, tf3, tn);
    }

    private InstanceIdentifier<Node> nodeBuilderToInstanceId(NodeBuilder node) {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, node.getKey());
    }

    public void _modifyFlows(CommandInterpreter ci) {
        NodeBuilder tn = createTestNode(ci.nextArgument());
        String flowtype = ci.nextArgument();
        Integer flowcnt = Integer.parseInt(flowtype);
        FlowBuilder tf;
        FlowBuilder tf1;
        FlowBuilder tf2;
        FlowBuilder tf3;
        switch (flowcnt) {
            case 1:
                tf = createTestFlow(tn, "f82", "10");
                tf1 = createTestFlow(tn, "f83", "11");
                tf2 = createTestFlow(tn, "f84", "12");
                tf3 = createTestFlow(tn, "f85", "13");
                break;
            case 2:
                tf = createTestFlow(tn, "f700", "3");
                tf1 = createTestFlow(tn, "f4", "4");
                tf2 = createTestFlow(tn, "f900", "5");
                tf3 = createTestFlow(tn, "f86", "6");
                break;
            case 3:
                // +
                tf = createTestFlow(tn, "f91", "7");
                tf1 = createTestFlow(tn, "f92", "8");
                tf2 = createTestFlow(tn, "f93", "9");
                tf3 = createTestFlow(tn, "f94", "10");
                break;
            case 4:
                // +ve scenario
                tf = createTestFlow(tn, "f230", "3");
                tf1 = createTestFlow(tn, "f99", "4");
                tf2 = createTestFlow(tn, "f100", "5");
                tf3 = createTestFlow(tn, "f101", "6");
                break;
            case 5:
                // -
                tf = createTestFlow(tn, "f23", "3");
                tf1 = createTestFlow(tn, "f99", "4");
                tf2 = createTestFlow(tn, "f100", "5");
                tf3 = createTestFlow(tn, "f101", "6");
                break;

            default:
                tf = createTestFlow(tn, "f87", "12");
                tf1 = createTestFlow(tn, "f88", "13");
                tf2 = createTestFlow(tn, "f89", "14");
                tf3 = createTestFlow(tn, "f90", "15");

        }

        writeFlow(ci, tf, tf1, tf2, tf3, tn);

    }

    public void _removeFlows(final CommandInterpreter ci) {
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        NodeBuilder tn = createTestNode(ci.nextArgument());
        String flowtype = ci.nextArgument();
        Integer flowcnt = Integer.parseInt(flowtype);
        FlowBuilder tf = null;
        FlowBuilder tf1 = null;
        FlowBuilder tf2 = null;
        FlowBuilder tf3 = null;
        switch (flowcnt) {
            case 1:
                // add case 1
                tf = createTestFlow(tn, "f1", "10");
                tf1 = createTestFlow(tn, "f2", "11");
                tf2 = createTestFlow(tn, "f3", "12");
                tf3 = createTestFlow(tn, "f4", "13");
                break;
            case 2:
                // modify case 1
                tf = createTestFlow(tn, "f82", "10");
                tf1 = createTestFlow(tn, "f83", "11");
                tf2 = createTestFlow(tn, "f84", "12");
                tf3 = createTestFlow(tn, "f85", "13");
                break;
            case 3:
                // add case 2
                tf = createTestFlow(tn, "f3", "3");
                tf1 = createTestFlow(tn, "f4", "4");
                tf2 = createTestFlow(tn, "f5", "5");
                tf3 = createTestFlow(tn, "f6", "6");
                break;
            case 4:
                // modify case 2
                tf = createTestFlow(tn, "f700", "3");
                tf1 = createTestFlow(tn, "f4", "4");
                tf2 = createTestFlow(tn, "f900", "5");
                tf3 = createTestFlow(tn, "f86", "6");
                break;
            case 5:
                // add case 3
                tf = createTestFlow(tn, "f7", "7");
                tf1 = createTestFlow(tn, "f8", "8");
                tf2 = createTestFlow(tn, "f9", "9");
                tf3 = createTestFlow(tn, "f10", "10");
                break;
            case 6:
                // modify case 3
                tf = createTestFlow(tn, "f91", "7");
                tf1 = createTestFlow(tn, "f92", "8");
                tf2 = createTestFlow(tn, "f93", "9");
                tf3 = createTestFlow(tn, "f94", "10");
                break;
            case 7:
                // -ve scenario
                tf = createTestFlow(tn, "f23", "3");
                tf1 = createTestFlow(tn, "f34", "4");
                tf2 = createTestFlow(tn, "f35", "5");
                tf3 = createTestFlow(tn, "f36", "6");
                break;
            case 8:
                // +ve scenario
                // modify case 6 -ve
                tf = createTestFlow(tn, "f23", "3");
                tf1 = createTestFlow(tn, "f99", "4");
                tf2 = createTestFlow(tn, "f100", "5");
                tf3 = createTestFlow(tn, "f101", "6");
                break;
            case 9:
                // modify case 6
                tf = createTestFlow(tn, "f700", "7");
                tf1 = createTestFlow(tn, "f230", "23");
                tf2 = createTestFlow(tn, "f900", "9");
                tf3 = createTestFlow(tn, "f1000", "10");
                break;
        }

        InstanceIdentifier<Flow> path1 = InstanceIdentifier.create(Nodes.class).child(Node.class, tn.getKey())
                .augmentation(FlowCapableNode.class).child(Table.class, new TableKey(tf.getTableId()))
                .child(Flow.class, tf.getKey());
        modification.delete(LogicalDatastoreType.OPERATIONAL, path1);
        modification.delete(LogicalDatastoreType.CONFIGURATION, nodeBuilderToInstanceId(tn));
        modification.delete(LogicalDatastoreType.CONFIGURATION, path1);
        InstanceIdentifier<Flow> path2 = InstanceIdentifier.create(Nodes.class).child(Node.class, tn.getKey())
                .augmentation(FlowCapableNode.class).child(Table.class, new TableKey(tf1.getTableId()))
                .child(Flow.class, tf1.getKey());
        modification.delete(LogicalDatastoreType.OPERATIONAL, path2);
        modification.delete(LogicalDatastoreType.CONFIGURATION, nodeBuilderToInstanceId(tn));
        modification.delete(LogicalDatastoreType.CONFIGURATION, path2);

        InstanceIdentifier<Flow> path3 = InstanceIdentifier.create(Nodes.class).child(Node.class, tn.getKey())
                .augmentation(FlowCapableNode.class).child(Table.class, new TableKey(tf2.getTableId()))
                .child(Flow.class, tf2.getKey());
        modification.delete(LogicalDatastoreType.OPERATIONAL, path3);
        modification.delete(LogicalDatastoreType.CONFIGURATION, nodeBuilderToInstanceId(tn));
        modification.delete(LogicalDatastoreType.CONFIGURATION, path3);
        InstanceIdentifier<Flow> path4 = InstanceIdentifier.create(Nodes.class).child(Node.class, tn.getKey())
                .augmentation(FlowCapableNode.class).child(Table.class, new TableKey(tf3.getTableId()))
                .child(Flow.class, tf3.getKey());
        modification.delete(LogicalDatastoreType.OPERATIONAL, path4);
        modification.delete(LogicalDatastoreType.CONFIGURATION, nodeBuilderToInstanceId(tn));
        modification.delete(LogicalDatastoreType.CONFIGURATION, path4);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        Futures.addCallback(commitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.error(throwable.getMessage(), throwable);
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        });

    }

    private void writeFlow(final CommandInterpreter ci, FlowBuilder flow, FlowBuilder flow1, FlowBuilder flow2,
                           FlowBuilder flow3, NodeBuilder nodeBuilder) {
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        InstanceIdentifier<Flow> path1 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, nodeBuilder.getKey()).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flow.getTableId())).child(Flow.class, flow.getKey());
        modification.merge(LogicalDatastoreType.OPERATIONAL, nodeBuilderToInstanceId(nodeBuilder), nodeBuilder.build(), true);
        modification.merge(LogicalDatastoreType.OPERATIONAL, path1, flow.build(), true);
        modification.merge(LogicalDatastoreType.CONFIGURATION, nodeBuilderToInstanceId(nodeBuilder), nodeBuilder.build(), true);
        modification.merge(LogicalDatastoreType.CONFIGURATION, path1, flow.build(), true);
        InstanceIdentifier<Flow> path2 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, nodeBuilder.getKey()).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flow1.getTableId())).child(Flow.class, flow1.getKey());
        modification.merge(LogicalDatastoreType.OPERATIONAL, nodeBuilderToInstanceId(nodeBuilder), nodeBuilder.build(), true);
        modification.merge(LogicalDatastoreType.OPERATIONAL, path2, flow1.build(), true);
        modification.merge(LogicalDatastoreType.CONFIGURATION, nodeBuilderToInstanceId(nodeBuilder), nodeBuilder.build(), true);
        modification.merge(LogicalDatastoreType.CONFIGURATION, path2, flow1.build(), true);

        InstanceIdentifier<Flow> path3 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, nodeBuilder.getKey()).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flow2.getTableId())).child(Flow.class, flow2.getKey());
        modification.merge(LogicalDatastoreType.OPERATIONAL, nodeBuilderToInstanceId(nodeBuilder), nodeBuilder.build(), true);
        modification.merge(LogicalDatastoreType.OPERATIONAL, path3, flow2.build(), true);
        modification.merge(LogicalDatastoreType.CONFIGURATION, nodeBuilderToInstanceId(nodeBuilder), nodeBuilder.build(), true);
        modification.merge(LogicalDatastoreType.CONFIGURATION, path3, flow2.build(), true);

        InstanceIdentifier<Flow> path4 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, nodeBuilder.getKey()).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flow3.getTableId())).child(Flow.class, flow3.getKey());
        modification.merge(LogicalDatastoreType.OPERATIONAL, nodeBuilderToInstanceId(nodeBuilder), nodeBuilder.build(), true);
        modification.merge(LogicalDatastoreType.OPERATIONAL, path4, flow3.build(), true);
        modification.merge(LogicalDatastoreType.CONFIGURATION, nodeBuilderToInstanceId(nodeBuilder), nodeBuilder.build(), true);
        modification.merge(LogicalDatastoreType.CONFIGURATION, path4, flow3.build(), true);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        Futures.addCallback(commitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.error(throwable.getMessage(), throwable);
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        });
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

    private static InstructionsBuilder createSentToControllerInstructions() {
        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(0xffff);
        Uri value = new Uri(OutputPortValues.CONTROLLER.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));

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

    private static InstructionsBuilder createAppyActionInstruction6() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetDlSrcActionBuilder src = new SetDlSrcActionBuilder();
        src.setAddress(new MacAddress("00:05:b9:7c:81:5f"));
        ab.setAction(new SetDlSrcActionCaseBuilder().setSetDlSrcAction(src.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction8() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetVlanPcpActionBuilder pcp = new SetVlanPcpActionBuilder();
        VlanPcp pcp1 = new VlanPcp((short) 2);
        pcp.setVlanPcp(pcp1);
        ab.setAction(new SetVlanPcpActionCaseBuilder().setSetVlanPcpAction(pcp.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction9() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        CopyTtlInBuilder ttlin = new CopyTtlInBuilder();
        ab.setAction(new CopyTtlInCaseBuilder().setCopyTtlIn(ttlin.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction16() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        GroupActionBuilder groupActionB = new GroupActionBuilder();
        groupActionB.setGroupId(1L);
        groupActionB.setGroup("0");
        ab.setAction(new GroupActionCaseBuilder().setGroupAction(groupActionB.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction160() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        FloodAllActionBuilder fldall = new FloodAllActionBuilder();
        ab.setAction(new FloodAllActionCaseBuilder().setFloodAllAction(fldall.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction26() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetNwDstActionBuilder setNwDstActionBuilder = new SetNwDstActionBuilder();
        Ipv4Builder ipdst = new Ipv4Builder();
        Ipv4Prefix prefixdst = new Ipv4Prefix("10.0.0.21/24");
        ipdst.setIpv4Address(prefixdst);
        setNwDstActionBuilder.setAddress(ipdst.build());
        ab.setAction(new SetNwDstActionCaseBuilder().setSetNwDstAction(setNwDstActionBuilder.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction27() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetNwSrcActionBuilder setNwsrcActionBuilder = new SetNwSrcActionBuilder();
        Ipv4Builder ipsrc = new Ipv4Builder();
        Ipv4Prefix prefixsrc = new Ipv4Prefix("10.0.23.21/24");
        ipsrc.setIpv4Address(prefixsrc);
        setNwsrcActionBuilder.setAddress(ipsrc.build());
        ab.setAction(new SetNwSrcActionCaseBuilder().setSetNwSrcAction(setNwsrcActionBuilder.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction28() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetNwTosActionBuilder setNwTosActionBuilder = new SetNwTosActionBuilder();
        setNwTosActionBuilder.setTos(8);
        ab.setAction(new SetNwTosActionCaseBuilder().setSetNwTosAction(setNwTosActionBuilder.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction34() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SwPathActionBuilder swPathAction = new SwPathActionBuilder();
        ab.setAction(new SwPathActionCaseBuilder().setSwPathAction(swPathAction.build()).build());
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

    private static MatchBuilder createLLDPMatch() {
        MatchBuilder match = new MatchBuilder();
        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x88ccL));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());
        return match;
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

    private static MatchBuilder createMatch1000() {
        MatchBuilder match = new MatchBuilder();
        Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        Ipv4Prefix prefix = new Ipv4Prefix("10.1.1.1/24");
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

    private static MatchBuilder createMatch33() {

        MatchBuilder match = new MatchBuilder();
        Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        Ipv4Prefix prefix = new Ipv4Prefix("10.0.0.10");
        ipv4Match.setIpv4Source(prefix);
        Ipv4Match i4m = ipv4Match.build();
        match.setLayer3Match(i4m);

        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0xfffeL));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());
        return match;
    }

    private static MatchBuilder createInphyportMatch(NodeId nodeId) {
        MatchBuilder match = new MatchBuilder();
        match.setInPort(new NodeConnectorId(nodeId + ":202"));
        match.setInPhyPort(new NodeConnectorId(nodeId + ":10122"));
        return match;
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
    private static MatchBuilder createL3IPv6Match() {
        MatchBuilder match = new MatchBuilder();

        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x86ddL));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        Ipv6Prefix dstip6 = new Ipv6Prefix("2002::2/64");
        Ipv6Prefix srcip6 = new Ipv6Prefix("2001:0:0:0:0:0:0:1/56");
        Ipv6Address ndtarget = new Ipv6Address("2001:db8:0:1:fd97:f9f0:a810:782e");
        MacAddress ndsll = new MacAddress("c2:00:54:f5:00:00");
        MacAddress ndtll = new MacAddress("00:0c:29:0e:4c:67");
        Ipv6ExtHeaderBuilder nextheader = new Ipv6ExtHeaderBuilder();
        nextheader.setIpv6Exthdr(58);
        Ipv6LabelBuilder ipv6label = new Ipv6LabelBuilder();
        Ipv6FlowLabel label = new Ipv6FlowLabel(10028L);
        ipv6label.setIpv6Flabel(label);
        // ipv6label.setFlabelMask(new byte[] { 0, 1, -1, -1 });

        Icmpv6MatchBuilder icmpv6match = new Icmpv6MatchBuilder(); // icmpv6
        // match
        icmpv6match.setIcmpv6Type((short) 135);
        icmpv6match.setIcmpv6Code((short) 0);
        match.setIcmpv6Match(icmpv6match.build());

        Ipv6MatchBuilder ipv6match = new Ipv6MatchBuilder();
        // ipv6match.setIpv6Source(srcip6);
        // ipv6match.setIpv6Destination(dstip6);
        // ipv6match.setIpv6ExtHeader(nextheader.build());
        ipv6match.setIpv6NdSll(ndsll);
        ipv6match.setIpv6NdTll(ndtll);
        // ipv6match.setIpv6NdTarget(ndtarget);
        ipv6match.setIpv6Label(ipv6label.build());

        match.setLayer3Match(ipv6match.build());

        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createICMPv6Match() {

        MatchBuilder match = new MatchBuilder();
        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x86ddL));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol((short) 58);
        match.setIpMatch(ipmatch.build());

        Icmpv6MatchBuilder icmpv6match = new Icmpv6MatchBuilder(); // icmpv6
        // match
        icmpv6match.setIcmpv6Type((short) 135);
        icmpv6match.setIcmpv6Code((short) 1);
        match.setIcmpv6Match(icmpv6match.build());

        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createMetadataMatch() {
        MatchBuilder match = new MatchBuilder();
        byte[] metamask = new byte[]{(byte) -1, (byte) -1, (byte) -1, 0, 0, 0, (byte) 1, (byte) 1};
        MetadataBuilder metadata = new MetadataBuilder(); // metadata match
        metadata.setMetadata(BigInteger.valueOf(500L));
        // metadata.setMetadataMask(metamask);
        match.setMetadata(metadata.build());

        return match;
    }

}
