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
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6FlowLabel;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtlBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6LabelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.MetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
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

public class OpenflowPluginBulkTransactionProvider implements CommandProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowPluginBulkTransactionProvider.class);

    private final DataBroker dataBroker;
    private final BundleContext ctx;
    private final String originalFlowName = "Foo";
    private final NodeErrorListener nodeErrorListener = new NodeErrorListenerLoggingImpl();
    private final NotificationService notificationService;

    public OpenflowPluginBulkTransactionProvider(final DataBroker dataBroker,
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

    @Override
    public String getHelp() {
        return "No help";
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

        FlowKey key = new FlowKey(new FlowId(Long.toString(id)));
        flow.withKey(key);
        flow.setPriority(Uint16.TWO);
        flow.setFlowName(originalFlowName + "X" + flowType);
        return flow;
    }

    private static Uint8 getTableId(final String tableId) {
        if (tableId != null) {
            try {
                return Uint8.valueOf(tableId);
            } catch (IllegalArgumentException ex) {
                // ignore exception and continue with default value
            }
        }
        return Uint8.TWO;
    }

    @SuppressWarnings("checkstyle:MethodName")
    public void _addFlows(final CommandInterpreter ci) {
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

    private static InstanceIdentifier<Node> nodeBuilderToInstanceId(final NodeBuilder node) {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, node.key());
    }

    @SuppressWarnings("checkstyle:MethodName")
    public void _modifyFlows(final CommandInterpreter ci) {
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

    @SuppressWarnings("checkstyle:MethodName")
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
            default:
                throw new IllegalArgumentException("Invalid flowtype: " + flowtype);
        }

        InstanceIdentifier<Flow> path1 = InstanceIdentifier.create(Nodes.class).child(Node.class, tn.key())
                .augmentation(FlowCapableNode.class).child(Table.class, new TableKey(tf.getTableId()))
                .child(Flow.class, tf.key());
        modification.delete(LogicalDatastoreType.OPERATIONAL, path1);
        modification.delete(LogicalDatastoreType.CONFIGURATION, nodeBuilderToInstanceId(tn));
        modification.delete(LogicalDatastoreType.CONFIGURATION, path1);
        InstanceIdentifier<Flow> path2 = InstanceIdentifier.create(Nodes.class).child(Node.class, tn.key())
                .augmentation(FlowCapableNode.class).child(Table.class, new TableKey(tf1.getTableId()))
                .child(Flow.class, tf1.key());
        modification.delete(LogicalDatastoreType.OPERATIONAL, path2);
        modification.delete(LogicalDatastoreType.CONFIGURATION, nodeBuilderToInstanceId(tn));
        modification.delete(LogicalDatastoreType.CONFIGURATION, path2);

        InstanceIdentifier<Flow> path3 = InstanceIdentifier.create(Nodes.class).child(Node.class, tn.key())
                .augmentation(FlowCapableNode.class).child(Table.class, new TableKey(tf2.getTableId()))
                .child(Flow.class, tf2.key());
        modification.delete(LogicalDatastoreType.OPERATIONAL, path3);
        modification.delete(LogicalDatastoreType.CONFIGURATION, nodeBuilderToInstanceId(tn));
        modification.delete(LogicalDatastoreType.CONFIGURATION, path3);
        InstanceIdentifier<Flow> path4 = InstanceIdentifier.create(Nodes.class).child(Node.class, tn.key())
                .augmentation(FlowCapableNode.class).child(Table.class, new TableKey(tf3.getTableId()))
                .child(Flow.class, tf3.key());
        modification.delete(LogicalDatastoreType.OPERATIONAL, path4);
        modification.delete(LogicalDatastoreType.CONFIGURATION, nodeBuilderToInstanceId(tn));
        modification.delete(LogicalDatastoreType.CONFIGURATION, path4);
        modification.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(final CommitInfo notUsed) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("Status of Group Data Loaded Transaction : failure.", throwable);
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        }, MoreExecutors.directExecutor());

    }

    private void writeFlow(final CommandInterpreter ci, final FlowBuilder flow, final FlowBuilder flow1,
                           final FlowBuilder flow2, final FlowBuilder flow3, final NodeBuilder nodeBuilder) {
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        InstanceIdentifier<Flow> path1 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, nodeBuilder.key()).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flow.getTableId())).child(Flow.class, flow.key());
        modification.mergeParentStructureMerge(LogicalDatastoreType.OPERATIONAL, nodeBuilderToInstanceId(nodeBuilder),
                nodeBuilder.build());
        modification.mergeParentStructureMerge(LogicalDatastoreType.OPERATIONAL, path1, flow.build());
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, nodeBuilderToInstanceId(nodeBuilder),
                nodeBuilder.build());
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, path1, flow.build());
        InstanceIdentifier<Flow> path2 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, nodeBuilder.key()).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flow1.getTableId())).child(Flow.class, flow1.key());
        modification.mergeParentStructureMerge(LogicalDatastoreType.OPERATIONAL, nodeBuilderToInstanceId(nodeBuilder),
                nodeBuilder.build());
        modification.mergeParentStructureMerge(LogicalDatastoreType.OPERATIONAL, path2, flow1.build());
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, nodeBuilderToInstanceId(nodeBuilder),
                nodeBuilder.build());
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, path2, flow1.build());

        InstanceIdentifier<Flow> path3 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, nodeBuilder.key()).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flow2.getTableId())).child(Flow.class, flow2.key());
        modification.mergeParentStructureMerge(LogicalDatastoreType.OPERATIONAL, nodeBuilderToInstanceId(nodeBuilder),
                nodeBuilder.build());
        modification.mergeParentStructureMerge(LogicalDatastoreType.OPERATIONAL, path3, flow2.build());
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, nodeBuilderToInstanceId(nodeBuilder),
                nodeBuilder.build());
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, path3, flow2.build());

        InstanceIdentifier<Flow> path4 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, nodeBuilder.key()).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flow3.getTableId())).child(Flow.class, flow3.key());
        modification.mergeParentStructureMerge(LogicalDatastoreType.OPERATIONAL, nodeBuilderToInstanceId(nodeBuilder),
                nodeBuilder.build());
        modification.mergeParentStructureMerge(LogicalDatastoreType.OPERATIONAL, path4, flow3.build());
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, nodeBuilderToInstanceId(nodeBuilder),
                nodeBuilder.build());
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, path4, flow3.build());
        modification.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(final CommitInfo notUsed) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("Status of Group Data Loaded Transaction : failure.", throwable);
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        }, MoreExecutors.directExecutor());
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

    private static InstructionsBuilder createMeterInstructions() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new MeterCaseBuilder()
                    .setMeter(new MeterBuilder().setMeterId(new MeterId(Uint32.ONE)).build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createGotoTableInstructions() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new GoToTableCaseBuilder()
                    .setGoToTable(new GoToTableBuilder().setTableId(Uint8.TWO).build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createDropInstructions() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
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

    private static InstructionsBuilder createAppyActionInstruction() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
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

    private static InstructionsBuilder createSentToControllerInstructions() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new OutputActionCaseBuilder()
                                .setOutputAction(new OutputActionBuilder()
                                    .setMaxLength(Uint16.MAX_VALUE)
                                    .setOutputNodeConnector(new Uri(OutputPortValues.CONTROLLER.toString()))
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction2() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
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
                .setOrder(0)
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

    private static InstructionsBuilder createAppyActionInstruction6() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new SetDlSrcActionCaseBuilder()
                                .setSetDlSrcAction(new SetDlSrcActionBuilder()
                                    .setAddress(new MacAddress("00:05:b9:7c:81:5f"))
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
                .setOrder(0)
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

    private static InstructionsBuilder createAppyActionInstruction8() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new SetVlanPcpActionCaseBuilder()
                                .setSetVlanPcpAction(new SetVlanPcpActionBuilder()
                                    .setVlanPcp(new VlanPcp(Uint8.TWO))
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction9() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new CopyTtlInCaseBuilder().setCopyTtlIn(new CopyTtlInBuilder().build()).build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction16() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new GroupActionCaseBuilder()
                                .setGroupAction(new GroupActionBuilder().setGroupId(Uint32.ONE).setGroup("0").build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction160() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new FloodAllActionCaseBuilder()
                                .setFloodAllAction(new FloodAllActionBuilder().build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction26() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new SetNwDstActionCaseBuilder()
                                .setSetNwDstAction(new SetNwDstActionBuilder()
                                    .setAddress(new Ipv4Builder()
                                        .setIpv4Address(new Ipv4Prefix("10.0.0.21/24"))
                                        .build())
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction27() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new SetNwSrcActionCaseBuilder()
                                .setSetNwSrcAction(new SetNwSrcActionBuilder()
                                    .setAddress(new Ipv4Builder()
                                        .setIpv4Address(new Ipv4Prefix("10.0.23.21/24"))
                                        .build())
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction28() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new SetNwTosActionCaseBuilder()
                                .setSetNwTosAction(new SetNwTosActionBuilder().setTos(8).build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction34() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new SwPathActionCaseBuilder()
                                .setSwPathAction(new SwPathActionBuilder().build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static MatchBuilder createLLDPMatch() {
        return new MatchBuilder()
            .setEthernetMatch(new EthernetMatchBuilder()
                .setEthernetType(new EthernetTypeBuilder().setType(new EtherType(Uint32.valueOf(0x88cc))).build())
                .build());
    }

    private static MatchBuilder createMatch1() {
        return new MatchBuilder()
            .setLayer3Match(new Ipv4MatchBuilder().setIpv4Destination(new Ipv4Prefix("10.0.0.1/24")).build())
            .setEthernetMatch(new EthernetMatchBuilder()
                .setEthernetType(new EthernetTypeBuilder().setType(new EtherType(Uint32.valueOf(0x0800))).build())
                .build());
    }

    private static MatchBuilder createMatch1000() {
        return new MatchBuilder()
            .setLayer3Match(new Ipv4MatchBuilder().setIpv4Destination(new Ipv4Prefix("10.1.1.1/24")).build())
            .setEthernetMatch(new EthernetMatchBuilder()
                .setEthernetType(new EthernetTypeBuilder().setType(new EtherType(Uint32.valueOf(0x0800))).build())
                .build());
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

    private static MatchBuilder createInphyportMatch(final NodeId nodeId) {
        return new MatchBuilder()
            .setInPort(new NodeConnectorId(nodeId + ":202"))
            .setInPhyPort(new NodeConnectorId(nodeId + ":10122"));
    }

    private static MatchBuilder createEthernetMatch() {
        return new MatchBuilder()
            .setEthernetMatch(new EthernetMatchBuilder()
                .setEthernetType(new EthernetTypeBuilder().setType(new EtherType(Uint32.valueOf(0x0800))).build())
                .setEthernetDestination(new EthernetDestinationBuilder()
                    .setAddress(new MacAddress("ff:ff:ff:ff:ff:ff"))
                    // .setMask(mask1)
                    .build())
                .setEthernetSource(new EthernetSourceBuilder()
                    .setAddress(new MacAddress("00:00:00:00:23:ae"))
                    // .setMask(mask2)
                    .build())
                .build());
    }

    private static MatchBuilder createL3IPv6Match() {
        return new MatchBuilder()
            .setEthernetMatch(new EthernetMatchBuilder()
                .setEthernetType(new EthernetTypeBuilder().setType(new EtherType(Uint32.valueOf(0x86dd))).build())
                .build())
            // icmpv6
            .setIcmpv6Match(new Icmpv6MatchBuilder()
                .setIcmpv6Type(Uint8.valueOf(135))
                .setIcmpv6Code(Uint8.ZERO)
                .build())
            .setLayer3Match(new Ipv6MatchBuilder()
                // .setIpv6Source(srcip6)
                // .setIpv6Destination(dstip6)
                // .setIpv6ExtHeader(nextheader.build())
                .setIpv6NdSll(new MacAddress("c2:00:54:f5:00:00"))
                .setIpv6NdTll(new MacAddress("00:0c:29:0e:4c:67"))
                // .setIpv6NdTarget(ndtarget)
                .setIpv6Label(new Ipv6LabelBuilder()
                    .setIpv6Flabel(new Ipv6FlowLabel(Uint32.valueOf(10028)))
                    // .setFlabelMask(new byte[] { 0, 1, -1, -1 })
                    .build())
                .build());
    }

    private static MatchBuilder createICMPv6Match() {
        return new MatchBuilder()
            .setEthernetMatch(new EthernetMatchBuilder()
                .setEthernetType(new EthernetTypeBuilder().setType(new EtherType(Uint32.valueOf(0x86dd))).build())
                .build())
            // ipv4 version
            .setIpMatch(new IpMatchBuilder().setIpProtocol(Uint8.valueOf(58)).build())
            // icmpv6
            .setIcmpv6Match(new Icmpv6MatchBuilder()
                .setIcmpv6Type(Uint8.valueOf(135))
                .setIcmpv6Code(Uint8.ONE)
                .build());
    }

    private static MatchBuilder createMetadataMatch() {
        return new MatchBuilder()
            .setMetadata(new MetadataBuilder()
                .setMetadata(Uint64.valueOf(500))
                // .setMetadataMask(metamask)
                .build());
    }
}
