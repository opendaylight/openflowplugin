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
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.VlanCfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.ControllerActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.FloodActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.FloodAllActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.HwPathActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.LoopbackActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlTypeActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNextHopActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTosActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanCfiActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanPcpActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.StripVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SwPathActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.controller.action._case.ControllerActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.in._case.CopyTtlInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.out._case.CopyTtlOutBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.mpls.ttl._case.DecMplsTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.flood.action._case.FloodActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.flood.all.action._case.FloodAllActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.hw.path.action._case.HwPathActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.loopback.action._case.LoopbackActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.mpls.action._case.PopMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.pbb.action._case.PopPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.vlan.action._case.PopVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.mpls.action._case.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.pbb.action._case.PushPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.dst.action._case.SetDlDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.src.action._case.SetDlSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.type.action._case.SetDlTypeActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.mpls.ttl.action._case.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.next.hop.action._case.SetNextHopActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.dst.action._case.SetNwDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.src.action._case.SetNwSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.tos.action._case.SetNwTosActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.ttl.action._case.SetNwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.dst.action._case.SetTpDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.src.action._case.SetTpSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.cfi.action._case.SetVlanCfiActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.id.action._case.SetVlanIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.pcp.action._case.SetVlanPcpActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.strip.vlan.action._case.StripVlanActionBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.go.to.table._case.GoToTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.meter._case.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.write.metadata._case.WriteMetadataBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpSourceHardwareAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpTargetHardwareAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6ExtHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6LabelBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.protocol.match.fields.PbbBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yangtools.concepts.Registration;
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
    private final SalFlowListener flowEventListener = new FlowEventListenerLoggingImpl();
    private static NotificationService notificationService;
    private Registration<org.opendaylight.yangtools.yang.binding.NotificationListener> listener1Reg;

    public OpenflowpluginTestCommandProvider(BundleContext ctx) {
        this.ctx = ctx;
    }

    public void onSessionInitiated(ProviderContext session) {
        pc = session;
        notificationService = session.getSALService(NotificationService.class);
        // For switch events
        listener1Reg = notificationService.registerNotificationListener(flowEventListener);
        dataBrokerService = session.getSALService(DataBrokerService.class);
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

    private InstanceIdentifier<Node> nodeBuilderToInstanceId(NodeBuilder node) {
        return InstanceIdentifier.builder(Nodes.class).child(Node.class, node.getKey()).toInstance();
    }

    private FlowBuilder createTestFlow(NodeBuilder nodeBuilder, String flowTypeArg, String tableId) {

        FlowBuilder flow = new FlowBuilder();
        long id = 123;

        String flowType = flowTypeArg;
        if (flowType == null) {
            flowType = "f1";
        }
        
        flow.setPriority(2);

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
            flow.setInstructions(createAppyActionInstruction1().build());
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
        case "f11":
            id += 11;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction4().build());
            break;
        case "f12":
            id += 12;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction5().build());
            break;
        case "f13":
            id += 13;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction6().build());
            break;
        case "f14":
            id += 14;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction7().build());
            break;
        case "f15":
            id += 15;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction8().build());
            break;
        case "f16":
            id += 16;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction9().build());
            break;
        case "f17":
            id += 17;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction10().build());
            break;
        case "f18":
            id += 18;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction11().build());
            break;
        case "f19":
            id += 19;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction12().build());
            break;
        case "f20":
            id += 20;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction13().build());
            break;
        case "f21":
            id += 21;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction14().build());
            break;
        case "f22":
            id += 22;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction15().build());
            break;
        case "f23":
            id += 23;
            // f23 can be used as test-case for generating error notification
            // if the particular group is not configured - tested
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction16().build());
            break;
        case "f24":
            id += 24;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction17().build());
            break;
        case "f25":
            id += 25;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction18().build());
            break;
        case "f26":
            id += 26;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction19().build());
            break;
        case "f27":
            id += 27;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createMetadataInstructions().build());
            break;
        case "f28":
            id += 28;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction20().build());
            break;
        case "f29":
            id += 29;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction21().build());
            break;
        case "f30":
            id += 30;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction22().build());
            break;
        case "f31":
            id += 31;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction23(nodeBuilder.getId()).build());
            break;
        case "f32":
            id += 32;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction24().build());
            break;
        case "f33":
            id += 33;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction25().build());
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
        case "f37":
            id += 37;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction29().build());
            break;
        case "f38":
            id += 38;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction30().build());
            break;
        case "f39":
            id += 39;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction31().build());
            break;
        case "f40":
            id += 40;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction32().build());
            break;
        case "f41":
            id += 41;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction33().build());
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
        case "f47":
            id += 47;
            flow.setMatch(createL4SCTPMatch().build());
            flow.setInstructions(createAppyActionInstruction().build());
            break;
        case "f48":
            id += 48;
            flow.setMatch(createTunnelIDMatch().build());
            flow.setInstructions(createGotoTableInstructions().build());
            break;
        case "f49":
            id += 49;
            flow.setMatch(createVlanMatch().build());
            flow.setInstructions(createMeterInstructions().build());
            break;
        case "f50":
            id += 50;
            flow.setMatch(createPbbMatch().build());
            flow.setInstructions(createMeterInstructions().build());
            break;
        case "f51":
            id += 51;
            flow.setMatch(createVlanMatch().build());
            flow.setInstructions(createDropInstructions().build());
            break;
        case "f52":
            id += 52;
            flow.setMatch(createL4TCPMatch().build());
            flow.setInstructions(createDropInstructions().build());
            break;

        case "f53":
            id += 53;
            flow.setMatch(createL4UDPMatch().build());
            flow.setInstructions(createDropInstructions().build());
            break;
        case "f54":
            id += 54;
            flow.setMatch(new MatchBuilder().build());
            flow.setInstructions(createSentToControllerInstructions().build());
            flow.setPriority(0);
            break;
        case "f55":
            id += 55;
            flow.setMatch(createToSMatch().build());
            flow.setInstructions(createDropInstructions().build());
            break;
        case "f56":
            id += 56;
            flow.setMatch(createToSMatch().build());
            flow.setInstructions(createOutputInstructions("INPORT", 10).build());
            break;
        case "f57":
            id += 57;
            flow.setMatch(createToSMatch().build());
            flow.setInstructions(createOutputInstructions("FLOOD", 20).build());
            break;
        case "f58":
            id += 58;
            flow.setMatch(createToSMatch().build());
            flow.setInstructions(createOutputInstructions("ALL", 30).build());
            break;
        case "f59":
            id += 59;
            flow.setMatch(createToSMatch().build());
            flow.setInstructions(createOutputInstructions("NORMAL", 40).build());
            break;
        case "f60":
            id += 60;
            flow.setMatch(createToSMatch().build());
            flow.setInstructions(createOutputInstructions("LOCAL", 50).build());
            break;
        case "f61":
            id += 61;
            flow.setMatch(createToSMatch().build());
            flow.setInstructions(createOutputInstructions("TABLE", 60).build());
            break;
        case "f62":
            id += 62;
            flow.setMatch(createToSMatch().build());
            flow.setInstructions(createOutputInstructions("NONE", 70).build());
            break;
        case "f63":
            id += 63;
            flow.setMatch(createToSMatch().build());
            flow.setInstructions(createStripVlanInstructions().build());
            flow.setBarrier(Boolean.TRUE);
            break;
        case "f64":
            id += 64;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction35().build());
            break;
        case "f65":
            id += 65;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction36().build());
            break;
        case "f66":
            id += 66;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction37().build());
            break;
        case "f67":
            id += 67;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction38().build());
            break;
        case "f68":
            id += 68;
            flow.setMatch(createL4TCPMatch().build());
            flow.setInstructions(createAppyActionInstruction39().build());
            break;
        case "f69":
            id += 69;
            flow.setMatch(createL4UDPMatch().build());
            flow.setInstructions(createAppyActionInstruction40().build());
            break;
        case "f70":
            id += 70;
            flow.setMatch(createL4SCTPMatch().build());
            flow.setInstructions(createAppyActionInstruction41().build());
            break;
        case "f71":
            id += 71;
            flow.setMatch(createICMPv4Match().build());
            flow.setInstructions(createAppyActionInstruction42().build());
            break;
        case "f72":
            id += 72;
            flow.setMatch(createArpMatch().build());
            flow.setInstructions(createAppyActionInstruction43().build());
            break;
        case "f73":
            id += 73;
            flow.setMatch(createL3IPv6Match().build());
            flow.setInstructions(createAppyActionInstruction44().build());
            break;
        case "f74":
            id += 74;
            flow.setMatch(createICMPv6Match().build());
            flow.setInstructions(createAppyActionInstruction45().build());
            break;
        case "f75":
            id += 75;
            flow.setMatch(createMplsMatch().build());
            flow.setInstructions(createAppyActionInstruction46().build());
            break;
        case "f76":
            id += 76;
            flow.setMatch(createPbbMatch().build());
            flow.setInstructions(createAppyActionInstruction47().build());
            break;
        case "f77":
            id += 77;
            flow.setMatch(createTunnelIDMatch().build());
            flow.setInstructions(createAppyActionInstruction48().build());
            break;
        case "f78":
            id += 78;
            flow.setMatch(createMatch33().build());
            flow.setInstructions(createDropInstructions().build());
            break;
        case "f79":
            id += 79;
            flow.setMatch(createICMPv6Match1().build());
            flow.setInstructions(createDecNwTtlInstructions().build());
            break;
        case "f80":
            id += 80;
            flow.setMatch(createMatch1().build());
            flow.setInstructions(createAppyActionInstruction88().build());
            break;
        case "f81":
            id += 81;
            flow.setMatch(createLLDPMatch().build());
            flow.setInstructions(createSentToControllerInstructions().build());
            break;
        case "f82":
            id += 82;
            flow.setMatch(createToSMatch().build());
            flow.setInstructions(createOutputInstructions().build());
            break;
        default:
            LOG.warn("flow type not understood: {}", flowType);
        }

        FlowKey key = new FlowKey(new FlowId(Long.toString(id)));
        if (null == flow.isBarrier()) {
            flow.setBarrier(Boolean.FALSE);
        }
        // flow.setBufferId(new Long(12));
        BigInteger value = new BigInteger("10", 10);
        // BigInteger outputPort = new BigInteger("65535", 10);
        flow.setCookie(value);
        flow.setCookieMask(value);
        flow.setHardTimeout(0);
        flow.setIdleTimeout(0);
        flow.setInstallHw(false);
        flow.setStrict(false);
        flow.setContainerName(null);
        flow.setFlags(new FlowModFlags(false, false, false, false, true));
        flow.setId(new FlowId("12"));
        flow.setTableId(getTableId(tableId));
        // commenting setOutGroup and setOutPort, as by default
        // OFPG_ANY is send
        // enable setOutGroup and setOutPort to enable output filtering
        // flow.setOutGroup(new Long(2));
        // set outport to OFPP_NONE (65535) to disable remove restriction for
        // flow
        // flow.setOutPort(outputPort);

        flow.setKey(key);
        flow.setFlowName(originalFlowName + "X" + flowType);
        testFlow = flow;
        return flow;
    }

    private FlowBuilder createtablemiss(NodeBuilder nodeBuilder, String flowTypeArg, String tableId) {
        FlowBuilder flow = new FlowBuilder();
        long id = 456;
        MatchBuilder matchBuilder = new MatchBuilder();
        flow.setMatch(matchBuilder.build());
        flow.setInstructions(createSentToControllerInstructions().build());
        flow.setPriority(0);
        flow.setTableId((short) 0);
        FlowKey key = new FlowKey(new FlowId(Long.toString(id)));
        flow.setKey(key);
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
        aab.setMeterId(new MeterId(new Long(1)));

        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new MeterCaseBuilder().setMeter(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createMetadataInstructions() {

        WriteMetadataBuilder aab = new WriteMetadataBuilder();
        aab.setMetadata(new BigInteger("10", 10));
        aab.setMetadataMask(new BigInteger("12", 10));

        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new WriteMetadataCaseBuilder().setWriteMetadata(aab.build()).build());

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

    private static InstructionsBuilder createAppyActionInstruction1() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(56);
        Uri value = new Uri("PCEP");
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
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

    private static InstructionsBuilder createOutputInstructions() {

        // test case for Output Port works if the particular port exists
        // this particular test-case is for Port : 1
        // tested as (addMDFlow openflow:<dpid> f82)
        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();

        Uri value = new Uri("1");
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

    private static InstructionsBuilder createSentToControllerInstructions() {
        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(new Integer(0xffff));
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

    private static InstructionsBuilder createOutputInstructions(String outputType, int outputValue) {
        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(outputValue);
        Uri value = new Uri(outputType);
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

    private static InstructionsBuilder createStripVlanInstructions() {
        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        StripVlanActionBuilder stripActionBuilder = new StripVlanActionBuilder();
        ab.setAction(new StripVlanActionCaseBuilder().setStripVlanAction(stripActionBuilder.build()).build());
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
        push.setEthernetType(new Integer(0x8847));
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
        pbb.setEthernetType(new Integer(0x88E7));
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

    private static InstructionsBuilder createAppyActionInstruction4() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        PushVlanActionBuilder vlan = new PushVlanActionBuilder();
        vlan.setEthernetType(new Integer(0x8100));
        ab.setAction(new PushVlanActionCaseBuilder().setPushVlanAction(vlan.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction5() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetDlDstActionBuilder setdl = new SetDlDstActionBuilder();
        setdl.setAddress(new MacAddress("00:05:b9:7c:81:5f"));
        ab.setAction(new SetDlDstActionCaseBuilder().setSetDlDstAction(setdl.build()).build());
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
        VlanId a = new VlanId(4000);
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

    private static InstructionsBuilder createAppyActionInstruction88() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetVlanPcpActionBuilder pcp = new SetVlanPcpActionBuilder();
        VlanPcp pcp1 = new VlanPcp((short) 9);
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

    private static InstructionsBuilder createAppyActionInstruction10() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        CopyTtlOutBuilder ttlout = new CopyTtlOutBuilder();
        ab.setAction(new CopyTtlOutCaseBuilder().setCopyTtlOut(ttlout.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction11() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        DecMplsTtlBuilder mpls = new DecMplsTtlBuilder();
        ab.setAction(new DecMplsTtlCaseBuilder().setDecMplsTtl(mpls.build()).build());
        actionList.add(ab.build());
        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setKey(new InstructionKey(0));
        ib.setOrder(0);

        // Put our Instruction in a list of Instruction
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction12() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        DecNwTtlBuilder nwttl = new DecNwTtlBuilder();
        ab.setAction(new DecNwTtlCaseBuilder().setDecNwTtl(nwttl.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction13() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        DropActionBuilder drop = new DropActionBuilder();
        ab.setAction(new DropActionCaseBuilder().setDropAction(drop.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction14() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        FloodActionBuilder fld = new FloodActionBuilder();
        ab.setAction(new FloodActionCaseBuilder().setFloodAction(fld.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction15() {

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

    private static InstructionsBuilder createAppyActionInstruction17() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        HwPathActionBuilder hwPathB = new HwPathActionBuilder();
        ab.setAction(new HwPathActionCaseBuilder().setHwPathAction(hwPathB.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction18() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        LoopbackActionBuilder loopbackActionBuilder = new LoopbackActionBuilder();
        ab.setAction(new LoopbackActionCaseBuilder().setLoopbackAction(loopbackActionBuilder.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction19() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        PopMplsActionBuilder popMplsActionBuilder = new PopMplsActionBuilder();
        popMplsActionBuilder.setEthernetType(0XB);
        ab.setAction(new PopMplsActionCaseBuilder().setPopMplsAction(popMplsActionBuilder.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction20() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        PopPbbActionBuilder popPbbActionBuilder = new PopPbbActionBuilder();
        ab.setAction(new PopPbbActionCaseBuilder().setPopPbbAction(popPbbActionBuilder.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction22() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetDlTypeActionBuilder setDlTypeActionBuilder = new SetDlTypeActionBuilder();
        setDlTypeActionBuilder.setDlType(new EtherType(8L));
        ab.setAction(new SetDlTypeActionCaseBuilder().setSetDlTypeAction(setDlTypeActionBuilder.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction23(NodeId nodeId) {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        setFieldBuilder.setInPort(new NodeConnectorId(nodeId + ":2"));
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

    private static InstructionsBuilder createAppyActionInstruction24() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetMplsTtlActionBuilder setMplsTtlActionBuilder = new SetMplsTtlActionBuilder();
        setMplsTtlActionBuilder.setMplsTtl((short) 0X1);
        ab.setAction(new SetMplsTtlActionCaseBuilder().setSetMplsTtlAction(setMplsTtlActionBuilder.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction25() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetNextHopActionBuilder setNextHopActionBuilder = new SetNextHopActionBuilder();
        Ipv4Builder ipnext = new Ipv4Builder();
        Ipv4Prefix prefix = new Ipv4Prefix("10.0.0.1/24");
        ipnext.setIpv4Address(prefix);
        setNextHopActionBuilder.setAddress(ipnext.build());
        ab.setAction(new SetNextHopActionCaseBuilder().setSetNextHopAction(setNextHopActionBuilder.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction29() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetNwTtlActionBuilder setNwTtlActionBuilder = new SetNwTtlActionBuilder();
        setNwTtlActionBuilder.setNwTtl((short) 1);
        ab.setAction(new SetNwTtlActionCaseBuilder().setSetNwTtlAction(setNwTtlActionBuilder.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction30() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetQueueActionBuilder setQueueActionBuilder = new SetQueueActionBuilder();
        setQueueActionBuilder.setQueueId(1L);
        ab.setAction(new SetQueueActionCaseBuilder().setSetQueueAction(setQueueActionBuilder.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction31() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetTpDstActionBuilder setTpDstActionBuilder = new SetTpDstActionBuilder();
        setTpDstActionBuilder.setPort(new PortNumber(109));

        ab.setAction(new SetTpDstActionCaseBuilder().setSetTpDstAction(setTpDstActionBuilder.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction32() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetTpSrcActionBuilder setTpSrcActionBuilder = new SetTpSrcActionBuilder();
        setTpSrcActionBuilder.setPort(new PortNumber(109));
        ab.setAction(new SetTpSrcActionCaseBuilder().setSetTpSrcAction(setTpSrcActionBuilder.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction33() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetVlanCfiActionBuilder setVlanCfiActionBuilder = new SetVlanCfiActionBuilder();
        setVlanCfiActionBuilder.setVlanCfi(new VlanCfi(2));
        ab.setAction(new SetVlanCfiActionCaseBuilder().setSetVlanCfiAction(setVlanCfiActionBuilder.build()).build());
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

    private static InstructionsBuilder createAppyActionInstruction35() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        ActionBuilder ab1 = new ActionBuilder();
        ActionBuilder ab2 = new ActionBuilder();

        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();
        SetFieldBuilder setFieldBuilder2 = new SetFieldBuilder();

        // Ethernet
        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
        ethSourceBuilder.setAddress(new MacAddress("00:00:00:00:00:01"));
        EthernetMatchBuilder ethernetMatch1 = new EthernetMatchBuilder();
        EthernetDestinationBuilder ethDestBuilder = new EthernetDestinationBuilder();
        ethDestBuilder.setAddress(new MacAddress("00:00:00:00:00:02"));
        EthernetMatchBuilder ethernetMatch2 = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x86ddL));

        ethernetMatch.setEthernetSource(ethSourceBuilder.build());
        ethernetMatch1.setEthernetDestination(ethDestBuilder.build());
        ethernetMatch2.setEthernetType(ethTypeBuilder.build());
        setFieldBuilder.setEthernetMatch(ethernetMatch.build());
        setFieldBuilder1.setEthernetMatch(ethernetMatch1.build());
        setFieldBuilder2.setEthernetMatch(ethernetMatch2.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.setKey(new ActionKey(1));
        actionList.add(ab1.build());

        ab2.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder2.build()).build());
        ab2.setKey(new ActionKey(2));
        actionList.add(ab2.build());

        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction36() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        ActionBuilder ab1 = new ActionBuilder();

        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();

        // Vlan
        VlanMatchBuilder vlanBuilder = new VlanMatchBuilder();
        VlanMatchBuilder vlanBuilder1 = new VlanMatchBuilder();
        VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
        VlanId vlanId = new VlanId(10);
        VlanPcp vpcp = new VlanPcp((short) 3);
        vlanBuilder.setVlanPcp(vpcp);
        vlanBuilder1.setVlanId(vlanIdBuilder.setVlanId(vlanId).setVlanIdPresent(true).build());
        setFieldBuilder.setVlanMatch(vlanBuilder.build());
        setFieldBuilder1.setVlanMatch(vlanBuilder1.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.setKey(new ActionKey(1));
        actionList.add(ab1.build());

        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction37() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        ActionBuilder ab1 = new ActionBuilder();
        ActionBuilder ab2 = new ActionBuilder();

        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();
        SetFieldBuilder setFieldBuilder2 = new SetFieldBuilder();
        // Ip
        IpMatchBuilder ipmatch = new IpMatchBuilder();
        IpMatchBuilder ipmatch1 = new IpMatchBuilder();
        IpMatchBuilder ipmatch2 = new IpMatchBuilder();
        Dscp dscp = new Dscp((short) 3);
        ipmatch.setIpDscp(dscp);
        ipmatch1.setIpEcn((short) 2);
        ipmatch2.setIpProtocol((short) 120);
        setFieldBuilder.setIpMatch(ipmatch.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        setFieldBuilder1.setIpMatch(ipmatch1.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.setKey(new ActionKey(1));
        actionList.add(ab1.build());

        setFieldBuilder2.setIpMatch(ipmatch2.build());
        ab2.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder2.build()).build());
        ab2.setKey(new ActionKey(2));
        actionList.add(ab2.build());

        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction38() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        ActionBuilder ab1 = new ActionBuilder();

        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();
        // IPv4
        Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        Ipv4MatchBuilder ipv4Match1 = new Ipv4MatchBuilder();
        Ipv4Prefix dstip = new Ipv4Prefix("200.71.9.5210");
        Ipv4Prefix srcip = new Ipv4Prefix("100.1.1.1");
        ipv4Match1.setIpv4Destination(dstip);
        ipv4Match.setIpv4Source(srcip);
        setFieldBuilder.setLayer3Match(ipv4Match.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        setFieldBuilder1.setLayer3Match(ipv4Match1.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.setKey(new ActionKey(1));
        actionList.add(ab1.build());

        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction39() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        ActionBuilder ab1 = new ActionBuilder();

        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();

        // Tcp
        PortNumber tcpsrcport = new PortNumber(1213);
        PortNumber tcpdstport = new PortNumber(646);
        TcpMatchBuilder tcpmatch = new TcpMatchBuilder();
        TcpMatchBuilder tcpmatch1 = new TcpMatchBuilder();
        tcpmatch.setTcpSourcePort(tcpsrcport);
        tcpmatch1.setTcpDestinationPort(tcpdstport);
        setFieldBuilder.setLayer4Match(tcpmatch.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        setFieldBuilder1.setLayer4Match(tcpmatch1.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.setKey(new ActionKey(1));
        actionList.add(ab.build());

        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction40() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        ActionBuilder ab1 = new ActionBuilder();

        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();

        // Udp
        PortNumber udpsrcport = new PortNumber(1325);
        PortNumber udpdstport = new PortNumber(42);
        UdpMatchBuilder udpmatch = new UdpMatchBuilder();
        UdpMatchBuilder udpmatch1 = new UdpMatchBuilder();
        udpmatch.setUdpDestinationPort(udpdstport);
        udpmatch1.setUdpSourcePort(udpsrcport);
        setFieldBuilder.setLayer4Match(udpmatch.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        setFieldBuilder1.setLayer4Match(udpmatch1.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.setKey(new ActionKey(1));
        actionList.add(ab1.build());

        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction41() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        ActionBuilder ab1 = new ActionBuilder();

        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();

        // Sctp
        SctpMatchBuilder sctpmatch = new SctpMatchBuilder();
        SctpMatchBuilder sctpmatch1 = new SctpMatchBuilder();
        PortNumber srcport = new PortNumber(1435);
        PortNumber dstport = new PortNumber(22);
        sctpmatch.setSctpSourcePort(srcport);
        sctpmatch1.setSctpDestinationPort(dstport);
        setFieldBuilder.setLayer4Match(sctpmatch.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        setFieldBuilder1.setLayer4Match(sctpmatch1.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.setKey(new ActionKey(1));
        actionList.add(ab1.build());

        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction42() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        ActionBuilder ab1 = new ActionBuilder();
        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();

        // Icmpv4
        Icmpv4MatchBuilder icmpv4match = new Icmpv4MatchBuilder();
        Icmpv4MatchBuilder icmpv4match1 = new Icmpv4MatchBuilder();
        icmpv4match.setIcmpv4Type((short) 8);
        icmpv4match1.setIcmpv4Code((short) 0);
        setFieldBuilder.setIcmpv4Match(icmpv4match.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        setFieldBuilder1.setIcmpv4Match(icmpv4match1.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.setKey(new ActionKey(1));
        actionList.add(ab1.build());

        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction43() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        ActionBuilder ab1 = new ActionBuilder();
        ActionBuilder ab2 = new ActionBuilder();
        ActionBuilder ab3 = new ActionBuilder();
        ActionBuilder ab4 = new ActionBuilder();

        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();
        SetFieldBuilder setFieldBuilder2 = new SetFieldBuilder();
        SetFieldBuilder setFieldBuilder3 = new SetFieldBuilder();
        SetFieldBuilder setFieldBuilder4 = new SetFieldBuilder();

        // setting the values of ARP
        MacAddress macdest = new MacAddress("ff:ff:ff:ff:ff:ff");
        MacAddress macsrc = new MacAddress("00:00:00:00:23:ae");
        Ipv4Prefix dstiparp = new Ipv4Prefix("200.71.9.52");
        Ipv4Prefix srciparp = new Ipv4Prefix("100.1.1.1");
        // create ARP match action
        ArpMatchBuilder arpmatch = new ArpMatchBuilder();
        ArpMatchBuilder arpmatch1 = new ArpMatchBuilder();
        ArpMatchBuilder arpmatch2 = new ArpMatchBuilder();
        ArpMatchBuilder arpmatch3 = new ArpMatchBuilder();
        ArpMatchBuilder arpmatch4 = new ArpMatchBuilder();
        ArpSourceHardwareAddressBuilder arpsrc = new ArpSourceHardwareAddressBuilder();
        arpsrc.setAddress(macsrc);
        ArpTargetHardwareAddressBuilder arpdst = new ArpTargetHardwareAddressBuilder();
        arpdst.setAddress(macdest);
        arpmatch.setArpOp(2);
        arpmatch1.setArpSourceHardwareAddress(arpsrc.build());
        arpmatch2.setArpTargetHardwareAddress(arpdst.build());
        arpmatch3.setArpSourceTransportAddress(srciparp);
        arpmatch4.setArpTargetTransportAddress(dstiparp);
        setFieldBuilder.setLayer3Match(arpmatch.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        setFieldBuilder1.setLayer3Match(arpmatch1.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.setKey(new ActionKey(1));
        actionList.add(ab1.build());

        setFieldBuilder2.setLayer3Match(arpmatch2.build());
        ab2.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder2.build()).build());
        ab2.setKey(new ActionKey(2));
        actionList.add(ab2.build());

        setFieldBuilder3.setLayer3Match(arpmatch3.build());
        ab3.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder3.build()).build());
        ab3.setKey(new ActionKey(3));
        actionList.add(ab3.build());

        setFieldBuilder4.setLayer3Match(arpmatch4.build());
        ab4.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder4.build()).build());
        ab4.setKey(new ActionKey(4));
        actionList.add(ab4.build());

        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction44() {

        List<Action> actionLists = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        ActionBuilder ab1 = new ActionBuilder();
        SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();
        /*
         * ActionBuilder ab2 = new ActionBuilder(); SetFieldBuilder
         * setFieldBuilder2 = new SetFieldBuilder(); ActionBuilder ab3 = new
         * ActionBuilder(); SetFieldBuilder setFieldBuilder3 = new
         * SetFieldBuilder(); ActionBuilder ab4 = new ActionBuilder();
         * SetFieldBuilder setFieldBuilder4 = new SetFieldBuilder();
         */
        ActionBuilder ab5 = new ActionBuilder();
        SetFieldBuilder setFieldBuilder5 = new SetFieldBuilder();
        ActionBuilder ab6 = new ActionBuilder();
        SetFieldBuilder setFieldBuilder6 = new SetFieldBuilder();

        // IPv6
        Ipv6MatchBuilder ipv6Builder = new Ipv6MatchBuilder();
        Ipv6MatchBuilder ipv6Builder1 = new Ipv6MatchBuilder();
        // Ipv6MatchBuilder ipv6Builder2 = new Ipv6MatchBuilder();
        // Ipv6MatchBuilder ipv6Builder3 = new Ipv6MatchBuilder();
        // Ipv6MatchBuilder ipv6Builder4 = new Ipv6MatchBuilder();
        Ipv6MatchBuilder ipv6Builder5 = new Ipv6MatchBuilder();
        Ipv6MatchBuilder ipv6Builder6 = new Ipv6MatchBuilder();

        Ipv6Prefix dstip6 = new Ipv6Prefix("2002::2");
        Ipv6Prefix srcip6 = new Ipv6Prefix("2001:0:0:0:0:0:0:1");
        // Ipv6Address ndtarget = new
        // Ipv6Address("2001:db8:0:1:fd97:f9f0:a810:782e");
        // MacAddress ndsll = new MacAddress("c2:00:54:f5:00:00");
        // MacAddress ndtll = new MacAddress("00:0c:29:0e:4c:67");
        Ipv6ExtHeaderBuilder nextheader = new Ipv6ExtHeaderBuilder();
        nextheader.setIpv6Exthdr(58);
        Ipv6LabelBuilder ipv6label = new Ipv6LabelBuilder();
        Ipv6FlowLabel label = new Ipv6FlowLabel(10028L);
        ipv6label.setIpv6Flabel(label);

        ipv6Builder.setIpv6Source(srcip6);
        ipv6Builder1.setIpv6Destination(dstip6);
        // ipv6Builder2.setIpv6NdTarget(ndtarget);
        // ipv6Builder3.setIpv6NdSll(ndsll);
        // ipv6Builder4.setIpv6NdTll(ndtll);
        ipv6Builder5.setIpv6ExtHeader(nextheader.build());
        ipv6Builder6.setIpv6Label(ipv6label.build());

        setFieldBuilder.setLayer3Match(ipv6Builder.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionLists.add(ab.build());

        setFieldBuilder1.setLayer3Match(ipv6Builder1.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.setKey(new ActionKey(1));
        actionLists.add(ab1.build());

        /*
         * setFieldBuilder2.setLayer3Match(ipv6Builder2.build());
         * ab2.setAction(new
         * SetFieldCaseBuilder().setSetField(setFieldBuilder2.build()).build());
         * ab2.setKey(new ActionKey(2)); actionLists.add(ab2.build());
         *
         * setFieldBuilder3.setLayer3Match(ipv6Builder3.build());
         * ab3.setAction(new
         * SetFieldCaseBuilder().setSetField(setFieldBuilder3.build()).build());
         * ab3.setKey(new ActionKey(3)); actionLists.add(ab3.build());
         *
         * setFieldBuilder4.setLayer3Match(ipv6Builder4.build());
         * ab4.setAction(new
         * SetFieldCaseBuilder().setSetField(setFieldBuilder4.build()).build());
         * ab4.setKey(new ActionKey(4)); actionLists.add(ab4.build());
         */
        setFieldBuilder5.setLayer3Match(ipv6Builder5.build());
        ab5.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder5.build()).build());
        ab5.setKey(new ActionKey(5));
        actionLists.add(ab5.build());

        setFieldBuilder6.setLayer3Match(ipv6Builder6.build());
        ab6.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder6.build()).build());
        ab6.setKey(new ActionKey(6));
        actionLists.add(ab6.build());

        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionLists);

        InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction45() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        ActionBuilder ab1 = new ActionBuilder();
        SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();

        // Icmpv6
        Icmpv6MatchBuilder icmpv6match = new Icmpv6MatchBuilder();
        Icmpv6MatchBuilder icmpv6match1 = new Icmpv6MatchBuilder();
        icmpv6match.setIcmpv6Type((short) 135);
        icmpv6match1.setIcmpv6Code((short) 0);
        setFieldBuilder.setIcmpv6Match(icmpv6match.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        setFieldBuilder1.setIcmpv6Match(icmpv6match1.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.setKey(new ActionKey(1));
        actionList.add(ab1.build());

        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction46() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        ActionBuilder ab1 = new ActionBuilder();
        SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();
        ActionBuilder ab2 = new ActionBuilder();
        SetFieldBuilder setFieldBuilder2 = new SetFieldBuilder();

        // MPLS
        ProtocolMatchFieldsBuilder protomatch = new ProtocolMatchFieldsBuilder();
        ProtocolMatchFieldsBuilder protomatch1 = new ProtocolMatchFieldsBuilder();
        ProtocolMatchFieldsBuilder protomatch2 = new ProtocolMatchFieldsBuilder();
        protomatch.setMplsLabel((long) 36008);
        protomatch1.setMplsTc((short) 4);
        protomatch2.setMplsBos((short) 1);
        setFieldBuilder.setProtocolMatchFields(protomatch.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        setFieldBuilder1.setProtocolMatchFields(protomatch1.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.setKey(new ActionKey(1));
        actionList.add(ab1.build());

        setFieldBuilder2.setProtocolMatchFields(protomatch2.build());
        ab2.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder2.build()).build());
        ab2.setKey(new ActionKey(2));
        actionList.add(ab2.build());

        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction47() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        // PBB
        ProtocolMatchFieldsBuilder protomatch = new ProtocolMatchFieldsBuilder();
        protomatch.setPbb(new PbbBuilder().setPbbIsid(4L).setPbbMask((new BigInteger(new byte[] { 0, 1, 0, 0 }).longValue())).build());
        setFieldBuilder.setProtocolMatchFields(protomatch.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        actionList.add(ab.build());

        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction48() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        // Tunnel
        TunnelBuilder tunnel = new TunnelBuilder();
        tunnel.setTunnelId(BigInteger.valueOf(10668));
        setFieldBuilder.setTunnel(tunnel.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        actionList.add(ab.build());

        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
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

    /**
     * @return
     */
    private static MatchBuilder createIPv4DstMatch() {
        MatchBuilder match = new MatchBuilder();
        Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        Ipv4Prefix prefix = new Ipv4Prefix("10.0.0.1/24");
        ipv4Match.setIpv4Destination(prefix);
        Ipv4Match i4m = ipv4Match.build();
        match.setLayer3Match(i4m);

        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createIPv4SrcMatch() {
        MatchBuilder match = new MatchBuilder();
        Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        Ipv4Prefix prefix = new Ipv4Prefix("10.20.30.40/24");
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

    /**
     * @return
     */
    private static MatchBuilder createICMPv6Match1() {

        MatchBuilder match = new MatchBuilder();
        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x86ddL));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol((short) 256);
        match.setIpMatch(ipmatch.build());

        Icmpv6MatchBuilder icmpv6match = new Icmpv6MatchBuilder(); // icmpv6
                                                                   // match
        icmpv6match.setIcmpv6Type((short) 135);
        icmpv6match.setIcmpv6Code((short) 1);
        match.setIcmpv6Match(icmpv6match.build());

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

        byte[] mask1 = new byte[] { (byte) -1, (byte) -1, 0, 0, 0, 0 };
        byte[] mask2 = new byte[] { (byte) -1, (byte) -1, (byte) -1, 0, 0, 0 };

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
        ethdest.setMask(new MacAddress("ff:ff:ff:00:00:00"));

        ethmatch.setEthernetDestination(ethdest.build());

        EthernetSourceBuilder ethsrc = new EthernetSourceBuilder();
        MacAddress macsrc = new MacAddress("00:00:00:00:23:ae");
        ethsrc.setAddress(macsrc);
        ethsrc.setMask(new MacAddress("ff:ff:00:00:00:00"));

        ethmatch.setEthernetSource(ethsrc.build());
        match.setEthernetMatch(ethmatch.build());
        return match;

    }

    /**
     * @return
     */

    private static MatchBuilder createVlanMatch() {
        MatchBuilder match = new MatchBuilder();
        VlanMatchBuilder vlanBuilder = new VlanMatchBuilder(); // vlan match
        VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
        VlanId vlanId = new VlanId(10);
        VlanPcp vpcp = new VlanPcp((short) 3);
        vlanBuilder.setVlanPcp(vpcp);
        vlanIdBuilder.setVlanId(vlanId);
        vlanIdBuilder.setVlanIdPresent(true);
        vlanBuilder.setVlanId(vlanIdBuilder.build());
        match.setVlanMatch(vlanBuilder.build());
        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createArpMatch() {
        MatchBuilder match = new MatchBuilder();

        EthernetMatchBuilder ethmatch = new EthernetMatchBuilder();
        MacAddress macdest = new MacAddress("ff:ff:ff:ff:ff:ff");
        MacAddress macsrc = new MacAddress("00:00:00:00:23:ae");

        byte[] mask = new byte[] { (byte) -1, (byte) -1, 0, 0, 0, 0 };
        byte[] mask2 = new byte[] { (byte) -1, (byte) -1, (byte) -1, 0, 0, 0 };

        EthernetTypeBuilder ethtype = new EthernetTypeBuilder();
        EtherType type = new EtherType(0x0806L);
        ethmatch.setEthernetType(ethtype.setType(type).build());

        Ipv4Prefix dstip = new Ipv4Prefix("200.71.9.52/10"); // ipv4 match
        Ipv4Prefix srcip = new Ipv4Prefix("100.1.1.1/8");

        ArpMatchBuilder arpmatch = new ArpMatchBuilder(); // arp match
        ArpSourceHardwareAddressBuilder arpsrc = new ArpSourceHardwareAddressBuilder();
        arpsrc.setAddress(macsrc);
        arpsrc.setMask(new MacAddress("ff:ff:ff:00:00:00"));
        ArpTargetHardwareAddressBuilder arpdst = new ArpTargetHardwareAddressBuilder();
        arpdst.setAddress(macdest);
        arpdst.setMask(new MacAddress("ff:ff:00:00:00:00"));
        arpmatch.setArpOp(2);
        arpmatch.setArpSourceHardwareAddress(arpsrc.build());
        arpmatch.setArpTargetHardwareAddress(arpdst.build());
        arpmatch.setArpSourceTransportAddress(srcip);
        arpmatch.setArpTargetTransportAddress(dstip);

        match.setEthernetMatch(ethmatch.build());
        match.setLayer3Match(arpmatch.build());

        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createIPMatch() {
        MatchBuilder match = new MatchBuilder();
        EthernetMatchBuilder ethmatch = new EthernetMatchBuilder();
        EthernetTypeBuilder ethtype = new EthernetTypeBuilder();
        EtherType type = new EtherType(0x0800L);
        ethmatch.setEthernetType(ethtype.setType(type).build());
        match.setEthernetMatch(ethmatch.build());

        IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol((short) 1);
        Dscp dscp = new Dscp((short) 3);
        ipmatch.setIpDscp(dscp);
        ipmatch.setIpEcn((short) 2);
        match.setIpMatch(ipmatch.build());
        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createL3IPv4Match() {
        MatchBuilder match = new MatchBuilder();

        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x0800L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        Ipv4Prefix dstip = new Ipv4Prefix("200.71.9.52/10"); // ipv4 match
        Ipv4Prefix srcip = new Ipv4Prefix("100.1.1.1/8");
        Ipv4MatchBuilder ipv4match = new Ipv4MatchBuilder();
        ipv4match.setIpv4Destination(dstip);
        ipv4match.setIpv4Source(srcip);
        match.setLayer3Match(ipv4match.build());

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
        ipv6label.setFlabelMask(new Ipv6FlowLabel(1L));

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

    private static MatchBuilder createICMPv4Match() {
        MatchBuilder match = new MatchBuilder();
        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x0800L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol((short) 1);
        match.setIpMatch(ipmatch.build());

        Icmpv4MatchBuilder icmpv4match = new Icmpv4MatchBuilder(); // icmpv4
                                                                   // match
        icmpv4match.setIcmpv4Type((short) 8);
        icmpv4match.setIcmpv4Code((short) 0);
        match.setIcmpv4Match(icmpv4match.build());
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
    private static MatchBuilder createToSMatch() {
        MatchBuilder match = new MatchBuilder();
        EthernetMatchBuilder ethmatch = new EthernetMatchBuilder();
        EthernetTypeBuilder ethtype = new EthernetTypeBuilder();
        EtherType type = new EtherType(0x0800L);
        ethmatch.setEthernetType(ethtype.setType(type).build());
        match.setEthernetMatch(ethmatch.build());

        IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol((short) 6);
        Dscp dscp = new Dscp((short) 8);
        ipmatch.setIpDscp(dscp);
        match.setIpMatch(ipmatch.build());
        return match;
    }

    /**
     * @return
     */

    private static MatchBuilder createL4TCPMatch() {
        MatchBuilder match = new MatchBuilder();

        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x0800L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol((short) 6);
        match.setIpMatch(ipmatch.build());

        PortNumber srcport = new PortNumber(1213);
        PortNumber dstport = new PortNumber(646);
        TcpMatchBuilder tcpmatch = new TcpMatchBuilder(); // tcp match
        tcpmatch.setTcpSourcePort(srcport);
        tcpmatch.setTcpDestinationPort(dstport);
        match.setLayer4Match(tcpmatch.build());

        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createL4UDPMatch() {
        MatchBuilder match = new MatchBuilder();

        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x0800L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol((short) 17);
        match.setIpMatch(ipmatch.build());

        PortNumber srcport = new PortNumber(1325);
        PortNumber dstport = new PortNumber(42);
        UdpMatchBuilder udpmatch = new UdpMatchBuilder(); // udp match
        udpmatch.setUdpDestinationPort(dstport);
        udpmatch.setUdpSourcePort(srcport);
        match.setLayer4Match(udpmatch.build());

        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createL4SCTPMatch() {
        MatchBuilder match = new MatchBuilder();

        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x0800L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol((short) 132);
        match.setIpMatch(ipmatch.build());

        SctpMatchBuilder sctpmatch = new SctpMatchBuilder();
        PortNumber srcport = new PortNumber(1435);
        PortNumber dstport = new PortNumber(22);
        sctpmatch.setSctpSourcePort(srcport);
        sctpmatch.setSctpDestinationPort(dstport);
        match.setLayer4Match(sctpmatch.build());

        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createMetadataMatch() {
        MatchBuilder match = new MatchBuilder();
        byte[] metamask = new byte[] { (byte) -1, (byte) -1, (byte) -1, 0, 0, 0, (byte) 1, (byte) 1 };
        MetadataBuilder metadata = new MetadataBuilder(); // metadata match
        metadata.setMetadata(BigInteger.valueOf(500L));
        metadata.setMetadataMask(new BigInteger(metamask));
        match.setMetadata(metadata.build());

        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createMplsMatch() {
        MatchBuilder match = new MatchBuilder();

        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x8847L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        ProtocolMatchFieldsBuilder protomatch = new ProtocolMatchFieldsBuilder(); // mpls
        // match
        protomatch.setMplsLabel((long) 36008);
        protomatch.setMplsTc((short) 4);
        protomatch.setMplsBos((short) 1);
        match.setProtocolMatchFields(protomatch.build());

        return match;

    }

    /**
     * @return
     */
    private static MatchBuilder createPbbMatch() {
        MatchBuilder match = new MatchBuilder();

        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x88E7L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        ProtocolMatchFieldsBuilder protomatch = new ProtocolMatchFieldsBuilder(); // mpls
        // match
        protomatch.setPbb(new PbbBuilder().setPbbIsid(4L).setPbbMask(new BigInteger(new byte[] { 0, 1, 0, 0 }).longValue()).build());
        match.setProtocolMatchFields(protomatch.build());

        return match;

    }

    /**
     * @return
     */
    private static MatchBuilder createTunnelIDMatch() {
        MatchBuilder match = new MatchBuilder();
        TunnelBuilder tunnel = new TunnelBuilder(); // tunnel id match
        tunnel.setTunnelId(BigInteger.valueOf(10668));
        byte[] mask = new byte[] { (byte) -1, (byte) -1, (byte) -1, 0, 0, 0, (byte) 1, (byte) 1 };
        tunnel.setTunnelMask(new BigInteger(mask));
        match.setTunnel(tunnel.build());

        return match;
    }

    public void _removeMDFlow(CommandInterpreter ci) {
        DataModification<InstanceIdentifier<?>, DataObject> modification = dataBrokerService.beginTransaction();
        NodeBuilder tn = createTestNode(ci.nextArgument());
        String flowtype = ci.nextArgument();
        FlowBuilder tf;
        if (flowtype.equals("fTM")) {
            tf = createtablemiss(tn, flowtype, ci.nextArgument());
        } else {
            tf = createTestFlow(tn, flowtype, ci.nextArgument());
        }
        InstanceIdentifier<Flow> path1 = InstanceIdentifier.builder(Nodes.class).child(Node.class, tn.getKey())
                .augmentation(FlowCapableNode.class).child(Table.class, new TableKey(tf.getTableId()))
                .child(Flow.class, tf.getKey()).build();
        modification.removeConfigurationData(path1);
        Future<RpcResult<TransactionStatus>> commitFuture = modification.commit();
        try {
            RpcResult<TransactionStatus> result = commitFuture.get();
            TransactionStatus status = result.getResult();
            ci.println("Status of Flow Data Loaded Transaction: " + status);

        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        } catch (ExecutionException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * @param ci arguments: switchId flowType tableNum
     * 
     * <pre>
     * e.g.: addMDFlow openflow:1 f1 42
     * </pre>
     */
    public void _addMDFlow(CommandInterpreter ci) {
        NodeBuilder tn = createTestNode(ci.nextArgument());
        String flowtype = ci.nextArgument();
        FlowBuilder tf;
        if (flowtype.equals("fTM")) {
            tf = createtablemiss(tn, flowtype, ci.nextArgument());
        } else {
            tf = createTestFlow(tn, flowtype, ci.nextArgument());
        }
        writeFlow(ci, tf, tn);
    }

    private void writeFlow(CommandInterpreter ci, FlowBuilder flow, NodeBuilder nodeBuilder) {
        DataModification<InstanceIdentifier<?>, DataObject> modification = dataBrokerService.beginTransaction();
        InstanceIdentifier<Flow> path1 = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, nodeBuilder.getKey()).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flow.getTableId())).child(Flow.class, flow.getKey()).build();
        modification.putConfigurationData(nodeBuilderToInstanceId(nodeBuilder), nodeBuilder.build());
        modification.putConfigurationData(path1, flow.build());
        Future<RpcResult<TransactionStatus>> commitFuture = modification.commit();
        try {
            RpcResult<TransactionStatus> result = commitFuture.get();
            TransactionStatus status = result.getResult();
            ci.println("Status of Flow Data Loaded Transaction: " + status);

        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        } catch (ExecutionException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void _modifyMDFlow(CommandInterpreter ci) {
        NodeBuilder tn = createTestNode(ci.nextArgument());
        FlowBuilder tf = createTestFlow(tn, ci.nextArgument(), ci.nextArgument());
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
