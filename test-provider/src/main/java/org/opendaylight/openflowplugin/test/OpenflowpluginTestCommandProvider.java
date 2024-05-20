/*
 * Copyright (c) 2013, 2015 Ericsson, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtlBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.go.to.table._case.GoToTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.meter._case.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.write.metadata._case.WriteMetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TcpFlagsMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TunnelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.TunnelIpv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.protocol.match.fields.PbbBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginTestCommandProvider implements CommandProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginTestCommandProvider.class);

    private final DataBroker dataBroker;
    private final BundleContext ctx;
    private static final String ORIGINAL_FLOW_NAME = "Foo";
    private static final String UPDATED_FLOW_NAME = "Bar";
    private static final String IPV4_PREFIX = "10.0.0.1/24";
    private static final String DEST_MAC_ADDRESS = "ff:ff:ff:ff:ff:ff";
    private static final String SRC_MAC_ADDRESS = "00:00:00:00:23:ae";

    private final NotificationService notificationService;

    public OpenflowpluginTestCommandProvider(final DataBroker dataBroker, final NotificationService notificationService,
            final BundleContext ctx) {
        this.dataBroker = dataBroker;
        this.notificationService = notificationService;
        this.ctx = ctx;
    }

    public void init() {
        // For switch events
        notificationService.registerCompositeListener(FlowEventListenerLoggingImpl.newListener());

        ctx.registerService(CommandProvider.class.getName(), this, null);
        createTestFlow(createTestNode(null), null, null);
    }

    private static NodeBuilder createTestNode(final String nodeId) {
        return new NodeBuilder().setId(new NodeId(nodeId != null ? nodeId : OpenflowpluginTestActivator.NODE_ID));
    }

    private static InstanceIdentifier<Node> nodeBuilderToInstanceId(final NodeBuilder node) {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, node.key());
    }

    private static FlowBuilder createTestFlow(final NodeBuilder nodeBuilder, final String flowTypeArg,
            final String tableId) {
        final long TEST_ID = 123;

        long id = TEST_ID;

        String flowType = flowTypeArg;
        if (flowType == null) {
            flowType = "f1";
        }

        final FlowBuilder flow = new FlowBuilder()
            .setPriority(Uint16.TWO);

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
                flow.setPriority(Uint16.ZERO);
                break;
            case "f55":
                id += 55;
                flow.setMatch(createToSMatch().build());
                flow.setInstructions(createDropInstructions().build());
                break;
            case "f56":
                id += 56;
                flow.setMatch(createToSMatch().build());
                flow.setInstructions(createOutputInstructions("INPORT", Uint16.TEN).build());
                break;
            case "f57":
                id += 57;
                flow.setMatch(createToSMatch().build());
                flow.setInstructions(createOutputInstructions("FLOOD", Uint16.valueOf(20)).build());
                break;
            case "f58":
                id += 58;
                flow.setMatch(createToSMatch().build());
                flow.setInstructions(createOutputInstructions("ALL", Uint16.valueOf(30)).build());
                break;
            case "f59":
                id += 59;
                flow.setMatch(createToSMatch().build());
                flow.setInstructions(createOutputInstructions("NORMAL", Uint16.valueOf(40)).build());
                break;
            case "f60":
                id += 60;
                flow.setMatch(createToSMatch().build());
                flow.setInstructions(createOutputInstructions("LOCAL", Uint16.valueOf(50)).build());
                break;
            case "f61":
                id += 61;
                flow.setMatch(createToSMatch().build());
                flow.setInstructions(createOutputInstructions("TABLE", Uint16.valueOf(60)).build());
                break;
            case "f62":
                id += 62;
                flow.setMatch(createToSMatch().build());
                flow.setInstructions(createOutputInstructions("NONE", Uint16.valueOf(70)).build());
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
                flow.setMatch(createVlanMatch().build());
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
            case "f83":
                // Test TCP_Flag Match
                id += 83;
                flow.setMatch(createTcpFlagMatch().build());
                flow.setInstructions(createDropInstructions().build());
                break;
            case "f84":
                id += 84;
                // match vlan=10,dl_vlan_pcp=3
                flow.setMatch(createVlanMatch().build());
                // vlan_pcp=4
                flow.setInstructions(createAppyActionInstruction88().build());
                break;
            case "f85":
                // Test Tunnel IPv4 Src (e.g. set_field:172.16.100.200->tun_src)
                id += 85;
                flow.setMatch(createMatch3().build());
                flow.setInstructions(createTunnelIpv4SrcInstructions().build());
                break;
            case "f86":
                // Test Tunnel IPv4 Dst (e.g. set_field:172.16.100.100->tun_dst)
                id += 86;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createTunnelIpv4DstInstructions().build());
                break;
            default:
                LOG.warn("flow type not understood: {}", flowType);
        }

        if (flow.getBarrier() == null) {
            flow.setBarrier(Boolean.FALSE);
        }

        return flow
            .setCookie(new FlowCookie(Uint64.TEN))
            .setCookieMask(new FlowCookie(Uint64.TEN))
            .setHardTimeout(Uint16.ZERO)
            .setIdleTimeout(Uint16.ZERO)
            .setInstallHw(false)
            .setStrict(false)
            .setContainerName(null)
            .setFlags(new FlowModFlags(false, false, false, false, true))
            .setId(new FlowId("12"))
            .setTableId(getTableId(tableId))
            .withKey(new FlowKey(new FlowId(Long.toString(id))))
            .setFlowName(ORIGINAL_FLOW_NAME + "X" + flowType);
    }

    private static FlowBuilder createTestFlowPerfTest(final String flowTypeArg, final String tableId, final int id) {
        String flowType = flowTypeArg;
        int flowId = id;

        if (flowType == null) {
            flowType = "f1";
        }

        final FlowBuilder flow = new FlowBuilder()
            .setPriority(Uint16.valueOf(flowId));

        switch (flowType) {
            case "f1":
                flowId += 1;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createDecNwTtlInstructions().build());
                break;
            default:
                LOG.warn("flow type not understood: {}", flowType);
        }

        if (flow.getBarrier() == null) {
            flow.setBarrier(Boolean.FALSE);
        }

        return flow
            .setCookie(new FlowCookie(Uint64.TEN))
            .setCookieMask(new FlowCookie(Uint64.TEN))
            .setHardTimeout(Uint16.ZERO)
            .setIdleTimeout(Uint16.ZERO)
            .setInstallHw(false)
            .setStrict(false)
            .setContainerName(null)
            .setFlags(new FlowModFlags(false, false, false, false, true))
            .setId(new FlowId("12"))
            .setTableId(getTableId(tableId))
            .withKey(new FlowKey(new FlowId(Long.toString(flowId))))
            .setFlowName(ORIGINAL_FLOW_NAME + "X" + flowType);
    }

    private static FlowBuilder createtablemiss() {
        return new FlowBuilder()
            .setMatch(new MatchBuilder().build())
            .setInstructions(createSentToControllerInstructions().build())
            .setPriority(Uint16.ZERO)
            .setTableId(Uint8.ZERO)
            .withKey(new FlowKey(new FlowId("456")));
    }

    private static Uint8 getTableId(final String tableId) {
        Uint8 table = Uint8.TWO;

        if (tableId == null) {
            return table;
        }

        try {
            table = Uint8.valueOf(tableId);
        } catch (NumberFormatException ex) {
            LOG.info("Parsing String tableId {} failed. Continuing with default tableId {}.",
                    tableId, table);
        }
        return table;
    }

    private static InstructionsBuilder createDecNwTtlInstructions() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
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

    private static InstructionsBuilder createMetadataInstructions() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .withKey(new InstructionKey(0))
                .setInstruction(new WriteMetadataCaseBuilder()
                    .setWriteMetadata(new WriteMetadataBuilder()
                        .setMetadata(Uint64.TEN).setMetadataMask(Uint64.TEN)
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createGotoTableInstructions() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new GoToTableCaseBuilder()
                    .setGoToTable(new GoToTableBuilder().setTableId(Uint8.valueOf(5)).build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createDropInstructions() {
        // Wrap our Apply Action in an Instruction
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .withKey(new ActionKey(0))
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
                            .setOrder(0)
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

    private static InstructionsBuilder createAppyActionInstruction1() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new OutputActionCaseBuilder()
                                .setOutputAction(new OutputActionBuilder()
                                    .setMaxLength(Uint16.valueOf(56))
                                    .setOutputNodeConnector(new Uri("PCEP"))
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createOutputInstructions() {
        // test case for Output Port works if the particular port exists
        // this particular test-case is for Port : 1
        // tested as (addMDFlow openflow:<dpid> f82)
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new OutputActionCaseBuilder()
                                .setOutputAction(new OutputActionBuilder().setOutputNodeConnector(new Uri("1")).build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createOutputInstructions(final String outputType, final Uint16 outputValue) {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new OutputActionCaseBuilder()
                                .setOutputAction(new OutputActionBuilder()
                                    .setMaxLength(outputValue)
                                    .setOutputNodeConnector(new Uri(outputType))
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

    private static InstructionsBuilder createStripVlanInstructions() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new StripVlanActionCaseBuilder()
                                .setStripVlanAction(new StripVlanActionBuilder().build())
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
                            .setOrder(0)
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
                            .setOrder(0)
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

    private static InstructionsBuilder createAppyActionInstruction4() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new PushVlanActionCaseBuilder()
                                .setPushVlanAction(new PushVlanActionBuilder()
                                    .setEthernetType(Uint16.valueOf(0x8100))
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction5() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetDlDstActionCaseBuilder()
                                .setSetDlDstAction(new SetDlDstActionBuilder()
                                    .setAddress(new MacAddress("00:05:b9:7c:81:5f"))
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
                    .setApplyActions(new ApplyActionsBuilder().setAction(BindingMap.of(new ActionBuilder()
                        .setOrder(0)
                        .setAction(new SetDlSrcActionCaseBuilder()
                            .setSetDlSrcAction(new SetDlSrcActionBuilder()
                                .setAddress(new MacAddress("00:05:b9:7c:81:5f"))
                                .build())
                            .build()).build()))
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
                            .setOrder(0)
                            .setAction(new SetVlanIdActionCaseBuilder()
                                .setSetVlanIdAction(new SetVlanIdActionBuilder()
                                    .setVlanId(new VlanId(Uint16.valueOf(4000))).build())
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
                            .setOrder(0)
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

    private static InstructionsBuilder createAppyActionInstruction88() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetVlanPcpActionCaseBuilder()
                                .setSetVlanPcpAction(new SetVlanPcpActionBuilder()
                                    // the code point is a 3-bit(0-7) field representing the frame priority level
                                    .setVlanPcp(new VlanPcp(Uint8.valueOf(4)))
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
                            .setOrder(0)
                            .setAction(new CopyTtlInCaseBuilder().setCopyTtlIn(new CopyTtlInBuilder().build()).build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction10() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new CopyTtlOutCaseBuilder()
                                .setCopyTtlOut(new CopyTtlOutBuilder().build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction11() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new DecMplsTtlCaseBuilder()
                                .setDecMplsTtl(new DecMplsTtlBuilder().build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction12() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new DecNwTtlCaseBuilder().setDecNwTtl(new DecNwTtlBuilder().build()).build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction13() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new DropActionCaseBuilder()
                                .setDropAction(new DropActionBuilder().build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction14() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new FloodActionCaseBuilder()
                                .setFloodAction(new FloodActionBuilder().build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction15() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new FloodAllActionCaseBuilder()
                                .setFloodAllAction(new FloodAllActionBuilder().build())
                                .build())
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
                            .setOrder(0)
                            .setAction(new GroupActionCaseBuilder()
                                .setGroupAction(new GroupActionBuilder().setGroupId(Uint32.ONE).setGroup("0").build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction17() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new HwPathActionCaseBuilder()
                                .setHwPathAction(new HwPathActionBuilder().build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction18() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new LoopbackActionCaseBuilder()
                                .setLoopbackAction(new LoopbackActionBuilder().build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction19() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new PopMplsActionCaseBuilder()
                                .setPopMplsAction(new PopMplsActionBuilder()
                                    .setEthernetType(Uint16.valueOf(0xB))
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction20() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new PopPbbActionCaseBuilder()
                                .setPopPbbAction(new PopPbbActionBuilder().build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction21() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new PopVlanActionCaseBuilder()
                                .setPopVlanAction(new PopVlanActionBuilder().build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction22() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetDlTypeActionCaseBuilder()
                                .setSetDlTypeAction(new SetDlTypeActionBuilder()
                                    .setDlType(new EtherType(Uint32.valueOf(8)))
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction23(final NodeId nodeId) {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder().setOrder(0).build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction24() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetMplsTtlActionCaseBuilder()
                                .setSetMplsTtlAction(new SetMplsTtlActionBuilder().setMplsTtl(Uint8.ONE).build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction25() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetNextHopActionCaseBuilder()
                                .setSetNextHopAction(new SetNextHopActionBuilder()
                                    .setAddress(new Ipv4Builder().setIpv4Address(new Ipv4Prefix(IPV4_PREFIX)).build())
                                    .build())
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
                            .setOrder(0)
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
                            .setOrder(0)
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
                            .setOrder(0)
                            .setAction(new SetNwTosActionCaseBuilder()
                                .setSetNwTosAction(new SetNwTosActionBuilder().setTos(8).build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction29() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetNwTtlActionCaseBuilder()
                                .setSetNwTtlAction(new SetNwTtlActionBuilder().setNwTtl(Uint8.ONE).build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction30() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetQueueActionCaseBuilder()
                                .setSetQueueAction(new SetQueueActionBuilder().setQueueId(Uint32.ONE).build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction31() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetTpDstActionCaseBuilder()
                                .setSetTpDstAction(new SetTpDstActionBuilder()
                                    .setPort(new PortNumber(Uint16.valueOf(109)))
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction32() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetTpSrcActionCaseBuilder()
                                .setSetTpSrcAction(new SetTpSrcActionBuilder()
                                    .setPort(new PortNumber(Uint16.valueOf(109)))
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction33() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetVlanCfiActionCaseBuilder()
                                .setSetVlanCfiAction(new SetVlanCfiActionBuilder().setVlanCfi(new VlanCfi(2)).build())
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
                            .setOrder(0)
                            .setAction(new SwPathActionCaseBuilder()
                                .setSwPathAction(new SwPathActionBuilder().build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction35() {
        // Ethernet
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetFieldCaseBuilder()
                                .setSetField(new SetFieldBuilder()
                                    .setEthernetMatch(new EthernetMatchBuilder()
                                        .setEthernetSource(new EthernetSourceBuilder()
                                            .setAddress(new MacAddress("00:00:00:00:00:01"))
                                            .build())
                                        .build())
                                    .build())
                                .build())
                            .build(), new ActionBuilder()
                            .setOrder(1)
                            .setAction(new SetFieldCaseBuilder()
                                .setSetField(new SetFieldBuilder()
                                    .setEthernetMatch(new EthernetMatchBuilder()
                                        .setEthernetDestination(new EthernetDestinationBuilder()
                                            .setAddress(new MacAddress("00:00:00:00:00:02"))
                                            .build())
                                        .build())
                                    .build())
                                .build())
                            .build(), new ActionBuilder()
                            .setOrder(2)
                            .setAction(new SetFieldCaseBuilder()
                                .setSetField(new SetFieldBuilder()
                                    .setEthernetMatch(new EthernetMatchBuilder()
                                        .setEthernetType(new EthernetTypeBuilder()
                                            .setType(new EtherType(Uint32.valueOf(0x86dd))).build())
                                        .build())
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction36() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetFieldCaseBuilder().setSetField(new SetFieldBuilder()
                                .setVlanMatch(new VlanMatchBuilder()
                                    .setVlanPcp(new VlanPcp(Uint8.valueOf(3)))
                                    .build())
                                .build()).build()).build(), new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetFieldCaseBuilder().setSetField(new SetFieldBuilder()
                                .setVlanMatch(new VlanMatchBuilder()
                                    .setVlanId(new VlanIdBuilder()
                                        .setVlanId(new VlanId(Uint16.TEN))
                                        .setVlanIdPresent(true)
                                        .build())
                                    .build())
                                .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction37() {
        // Ip
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetFieldCaseBuilder()
                                .setSetField(new SetFieldBuilder()
                                    .setIpMatch(new IpMatchBuilder().setIpDscp(new Dscp(Uint8.valueOf(3))).build())
                                    .build())
                                .build())
                            .build(), new ActionBuilder()
                            .setOrder(1)
                            .setAction(new SetFieldCaseBuilder()
                                .setSetField(new SetFieldBuilder()
                                    .setIpMatch(new IpMatchBuilder().setIpEcn(Uint8.TWO).build())
                                    .build())
                                .build())
                            .build(), new ActionBuilder()
                            .setOrder(2)
                            .setAction(new SetFieldCaseBuilder()
                                .setSetField(new SetFieldBuilder()
                                    .setIpMatch(new IpMatchBuilder().setIpProtocol(Uint8.valueOf(120)).build())
                                    .build())
                                .build()).build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction38() {
        // IPv4
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetFieldCaseBuilder()
                                .setSetField(new SetFieldBuilder()
                                    .setLayer3Match(new Ipv4MatchBuilder()
                                        .setIpv4Source(new Ipv4Prefix("100.1.1.1"))
                                        .build())
                                    .build())
                                .build())
                            .build(), new ActionBuilder()
                            .setOrder(1)
                            .setAction(new SetFieldCaseBuilder()
                                .setSetField(new SetFieldBuilder()
                                    .setLayer3Match(new Ipv4MatchBuilder()
                                        .setIpv4Destination(new Ipv4Prefix("200.71.9.5210"))
                                        .build())
                                .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    // FIXME: refactor these for brevity

    private static InstructionsBuilder createAppyActionInstruction39() {
        final ActionBuilder ab = new ActionBuilder();
        final ActionBuilder ab1 = new ActionBuilder();

        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        final SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();

        // Tcp
        final PortNumber tcpsrcport = new PortNumber(Uint16.valueOf(1213));
        final PortNumber tcpdstport = new PortNumber(Uint16.valueOf(646));
        final TcpMatchBuilder tcpmatch = new TcpMatchBuilder();
        final TcpMatchBuilder tcpmatch1 = new TcpMatchBuilder();
        tcpmatch.setTcpSourcePort(tcpsrcport);
        tcpmatch1.setTcpDestinationPort(tcpdstport);
        setFieldBuilder.setLayer4Match(tcpmatch.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setOrder(0);

        setFieldBuilder1.setLayer4Match(tcpmatch1.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.withKey(new ActionKey(1));

        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(ab.build(), ab1.build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction40() {
        final ActionBuilder ab = new ActionBuilder();
        final ActionBuilder ab1 = new ActionBuilder();

        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        final SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();

        // Udp
        final PortNumber udpsrcport = new PortNumber(Uint16.valueOf(1325));
        final PortNumber udpdstport = new PortNumber(Uint16.valueOf(42));
        final UdpMatchBuilder udpmatch = new UdpMatchBuilder();
        final UdpMatchBuilder udpmatch1 = new UdpMatchBuilder();
        udpmatch.setUdpDestinationPort(udpdstport);
        udpmatch1.setUdpSourcePort(udpsrcport);
        setFieldBuilder.setLayer4Match(udpmatch.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setOrder(0);

        setFieldBuilder1.setLayer4Match(udpmatch1.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.withKey(new ActionKey(1));

        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(ab.build(), ab1.build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction41() {
        final ActionBuilder ab = new ActionBuilder();
        final ActionBuilder ab1 = new ActionBuilder();

        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        final SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();

        // Sctp
        final SctpMatchBuilder sctpmatch = new SctpMatchBuilder();
        final SctpMatchBuilder sctpmatch1 = new SctpMatchBuilder();
        final PortNumber srcport = new PortNumber(Uint16.valueOf(1435));
        final PortNumber dstport = new PortNumber(Uint16.valueOf(22));
        sctpmatch.setSctpSourcePort(srcport);
        sctpmatch1.setSctpDestinationPort(dstport);
        setFieldBuilder.setLayer4Match(sctpmatch.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setOrder(0);

        setFieldBuilder1.setLayer4Match(sctpmatch1.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.withKey(new ActionKey(1));

        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(ab.build(), ab1.build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction42() {
        final ActionBuilder ab = new ActionBuilder();
        final ActionBuilder ab1 = new ActionBuilder();
        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        final SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();

        // Icmpv4
        final Icmpv4MatchBuilder icmpv4match = new Icmpv4MatchBuilder();
        final Icmpv4MatchBuilder icmpv4match1 = new Icmpv4MatchBuilder();
        icmpv4match.setIcmpv4Type(Uint8.valueOf(8));
        icmpv4match1.setIcmpv4Code(Uint8.ZERO);
        setFieldBuilder.setIcmpv4Match(icmpv4match.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setOrder(0);

        setFieldBuilder1.setIcmpv4Match(icmpv4match1.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.withKey(new ActionKey(1));

        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(ab.build(), ab1.build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction43() {
        final ActionBuilder ab = new ActionBuilder();
        final ActionBuilder ab1 = new ActionBuilder();
        final ActionBuilder ab2 = new ActionBuilder();
        final ActionBuilder ab3 = new ActionBuilder();
        final ActionBuilder ab4 = new ActionBuilder();

        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        final SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();
        final SetFieldBuilder setFieldBuilder2 = new SetFieldBuilder();
        final SetFieldBuilder setFieldBuilder3 = new SetFieldBuilder();
        final SetFieldBuilder setFieldBuilder4 = new SetFieldBuilder();

        // setting the values of ARP
        final MacAddress macdest = new MacAddress(DEST_MAC_ADDRESS);
        final MacAddress macsrc = new MacAddress(SRC_MAC_ADDRESS);
        final Ipv4Prefix dstiparp = new Ipv4Prefix("200.71.9.52");
        final Ipv4Prefix srciparp = new Ipv4Prefix("100.1.1.1");
        // create ARP match action
        final ArpMatchBuilder arpmatch = new ArpMatchBuilder();
        final ArpMatchBuilder arpmatch1 = new ArpMatchBuilder();
        final ArpMatchBuilder arpmatch2 = new ArpMatchBuilder();
        final ArpMatchBuilder arpmatch3 = new ArpMatchBuilder();
        final ArpMatchBuilder arpmatch4 = new ArpMatchBuilder();
        final ArpSourceHardwareAddressBuilder arpsrc = new ArpSourceHardwareAddressBuilder();
        arpsrc.setAddress(macsrc);
        final ArpTargetHardwareAddressBuilder arpdst = new ArpTargetHardwareAddressBuilder();
        arpdst.setAddress(macdest);
        arpmatch.setArpOp(Uint16.TWO);
        arpmatch1.setArpSourceHardwareAddress(arpsrc.build());
        arpmatch2.setArpTargetHardwareAddress(arpdst.build());
        arpmatch3.setArpSourceTransportAddress(srciparp);
        arpmatch4.setArpTargetTransportAddress(dstiparp);
        setFieldBuilder.setLayer3Match(arpmatch.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setOrder(0);

        setFieldBuilder1.setLayer3Match(arpmatch1.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.withKey(new ActionKey(1));

        setFieldBuilder2.setLayer3Match(arpmatch2.build());
        ab2.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder2.build()).build());
        ab2.withKey(new ActionKey(2));

        setFieldBuilder3.setLayer3Match(arpmatch3.build());
        ab3.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder3.build()).build());
        ab3.withKey(new ActionKey(3));

        setFieldBuilder4.setLayer3Match(arpmatch4.build());
        ab4.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder4.build()).build());
        ab4.withKey(new ActionKey(4));

        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(ab.build(), ab1.build(), ab2.build(), ab3.build(), ab4.build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction44() {
        final ActionBuilder ab = new ActionBuilder();
        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        final ActionBuilder ab1 = new ActionBuilder();
        final SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();
        final ActionBuilder ab5 = new ActionBuilder();
        final SetFieldBuilder setFieldBuilder5 = new SetFieldBuilder();
        final ActionBuilder ab6 = new ActionBuilder();
        final SetFieldBuilder setFieldBuilder6 = new SetFieldBuilder();

        // IPv6
        final Ipv6MatchBuilder ipv6Builder = new Ipv6MatchBuilder();
        final Ipv6MatchBuilder ipv6Builder1 = new Ipv6MatchBuilder();
        final Ipv6MatchBuilder ipv6Builder5 = new Ipv6MatchBuilder();
        final Ipv6MatchBuilder ipv6Builder6 = new Ipv6MatchBuilder();

        final Ipv6Prefix dstip6 = new Ipv6Prefix("2002::2/128");
        final Ipv6Prefix srcip6 = new Ipv6Prefix("2001:0:0:0:0:0:0:1/128");
        final Ipv6ExtHeaderBuilder nextheader = new Ipv6ExtHeaderBuilder();
        nextheader.setIpv6Exthdr(Uint16.valueOf(58));
        final Ipv6LabelBuilder ipv6label = new Ipv6LabelBuilder();
        final Ipv6FlowLabel label = new Ipv6FlowLabel(Uint32.valueOf(10028));
        ipv6label.setIpv6Flabel(label);

        ipv6Builder.setIpv6Source(srcip6);
        ipv6Builder1.setIpv6Destination(dstip6);
        ipv6Builder5.setIpv6ExtHeader(nextheader.build());
        ipv6Builder6.setIpv6Label(ipv6label.build());

        setFieldBuilder.setLayer3Match(ipv6Builder.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setOrder(0);

        setFieldBuilder1.setLayer3Match(ipv6Builder1.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.withKey(new ActionKey(1));

        setFieldBuilder5.setLayer3Match(ipv6Builder5.build());
        ab5.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder5.build()).build());
        ab5.withKey(new ActionKey(5));

        setFieldBuilder6.setLayer3Match(ipv6Builder6.build());
        ab6.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder6.build()).build());
        ab6.withKey(new ActionKey(6));

        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(ab.build(), ab1.build(), ab5.build(), ab6.build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction45() {
        // Icmpv6
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetFieldCaseBuilder()
                                .setSetField(new SetFieldBuilder()
                                    .setIcmpv6Match(new Icmpv6MatchBuilder().setIcmpv6Type(Uint8.valueOf(135)).build())
                                    .build())
                                .build())
                            .build(), new ActionBuilder()
                            .setOrder(1)
                            .setAction(new SetFieldCaseBuilder()
                                .setSetField(new SetFieldBuilder()
                                    .setIcmpv6Match(new Icmpv6MatchBuilder().setIcmpv6Code(Uint8.ZERO).build())
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction46() {
        // MPLS
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetFieldCaseBuilder()
                                .setSetField(new SetFieldBuilder()
                                    .setProtocolMatchFields(new ProtocolMatchFieldsBuilder()
                                        .setMplsLabel(Uint32.valueOf(36008))
                                        .build())
                                    .build())
                                .build())
                            .build(), new ActionBuilder()
                            .setOrder(1)
                            .setAction(new SetFieldCaseBuilder()
                                .setSetField(new SetFieldBuilder()
                                    .setProtocolMatchFields(new ProtocolMatchFieldsBuilder()
                                        .setMplsTc(Uint8.valueOf(4))
                                        .build())
                                    .build())
                                .build())
                            .build(), new ActionBuilder()
                            .setOrder(2)
                            .setAction(new SetFieldCaseBuilder()
                                .setSetField(new SetFieldBuilder()
                                    .setProtocolMatchFields(new ProtocolMatchFieldsBuilder()
                                        .setMplsBos(Uint8.ONE)
                                        .build())
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction47() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetFieldCaseBuilder()
                                .setSetField(new SetFieldBuilder()
                                    .setProtocolMatchFields(new ProtocolMatchFieldsBuilder()
                                        .setPbb(new PbbBuilder()
                                            .setPbbIsid(Uint32.valueOf(4))
                                            .setPbbMask(Uint32.valueOf(0x10000))
                                            .build())
                                        .build())
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createAppyActionInstruction48() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetFieldCaseBuilder()
                                .setSetField(new SetFieldBuilder()
                                    // Tunnel
                                    .setTunnel(new TunnelBuilder().setTunnelId(Uint64.valueOf(10668)).build()).build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createTunnelIpv4DstInstructions() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetFieldCaseBuilder()
                                // Add the IPv4 tunnel dst to the set_field value
                                .setSetField(new SetFieldBuilder()
                                    .setLayer3Match(new TunnelIpv4MatchBuilder()
                                        // Add the mew IPv4 object as the tunnel destination
                                        .setTunnelIpv4Destination(new Ipv4Prefix("172.16.100.100"))
                                        .build())
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static InstructionsBuilder createTunnelIpv4SrcInstructions() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setOrder(0)
                            .setAction(new SetFieldCaseBuilder()
                                // Add the IPv4 tunnel src to the set_field value
                                .setSetField(new SetFieldBuilder()
                                    .setLayer3Match(new TunnelIpv4MatchBuilder()
                                        // Add the new IPv4 object as the tunnel destination
                                        .setTunnelIpv4Source(new Ipv4Prefix("172.16.100.200"))
                                        .build())
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()));
    }

    private static MatchBuilder createLLDPMatch() {
        final MatchBuilder match = new MatchBuilder();
        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(Uint32.valueOf(0x88cc)));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());
        return match;
    }

    private static MatchBuilder createMatch1() {
        final MatchBuilder match = new MatchBuilder();
        final Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        final Ipv4Prefix prefix = new Ipv4Prefix(IPV4_PREFIX);
        ipv4Match.setIpv4Destination(prefix);
        final Ipv4Match i4m = ipv4Match.build();
        match.setLayer3Match(i4m);

        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(Uint32.valueOf(0x0800)));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());
        return match;
    }

    private static MatchBuilder createMatch2() {
        final MatchBuilder match = new MatchBuilder();
        final Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        final Ipv4Prefix prefix = new Ipv4Prefix("10.0.0.1");
        ipv4Match.setIpv4Source(prefix);
        final Ipv4Match i4m = ipv4Match.build();
        match.setLayer3Match(i4m);

        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(Uint32.valueOf(0x0800)));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());
        return match;
    }

    private static MatchBuilder createMatch3() {
        final MatchBuilder match = new MatchBuilder();
        final EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        final EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
        ethSourceBuilder.setAddress(new MacAddress("00:00:00:00:00:01"));
        ethernetMatch.setEthernetSource(ethSourceBuilder.build());
        match.setEthernetMatch(ethernetMatch.build());

        return match;
    }

    private static MatchBuilder createICMPv6Match1() {
        return new MatchBuilder()
            .setEthernetMatch(new EthernetMatchBuilder()
                .setEthernetType(new EthernetTypeBuilder()
                    .setType(new EtherType(Uint32.valueOf(0x86dd)))
                    .build())
                .build())
            // ipv4 version
            .setIpMatch(new IpMatchBuilder().setIpProtocol(Uint8.MAX_VALUE).build())
            // icmpv6
            .setIcmpv6Match(new Icmpv6MatchBuilder()
                .setIcmpv6Type(Uint8.valueOf(135))
                .setIcmpv6Code(Uint8.ONE)
                .build());
    }

    private static MatchBuilder createMatch33() {

        final MatchBuilder match = new MatchBuilder();
        final Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        final Ipv4Prefix prefix = new Ipv4Prefix("10.0.0.10");
        ipv4Match.setIpv4Source(prefix);
        final Ipv4Match i4m = ipv4Match.build();
        match.setLayer3Match(i4m);

        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(Uint32.valueOf(0xfffe)));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());
        return match;
    }

    private static MatchBuilder createInphyportMatch(final NodeId nodeId) {
        final MatchBuilder match = new MatchBuilder();
        match.setInPort(new NodeConnectorId(nodeId + ":202"));
        match.setInPhyPort(new NodeConnectorId(nodeId + ":10122"));
        return match;
    }

    private static MatchBuilder createEthernetMatch() {
        final MatchBuilder match = new MatchBuilder();
        final EthernetMatchBuilder ethmatch = new EthernetMatchBuilder(); // ethernettype
        // match
        final EthernetTypeBuilder ethtype = new EthernetTypeBuilder();
        final EtherType type = new EtherType(Uint32.valueOf(0x0800));
        ethmatch.setEthernetType(ethtype.setType(type).build());

        final EthernetDestinationBuilder ethdest = new EthernetDestinationBuilder(); // ethernet
        // macaddress
        // match
        final MacAddress macdest = new MacAddress(DEST_MAC_ADDRESS);
        ethdest.setAddress(macdest);
        ethdest.setMask(new MacAddress("ff:ff:ff:00:00:00"));

        ethmatch.setEthernetDestination(ethdest.build());

        final EthernetSourceBuilder ethsrc = new EthernetSourceBuilder();
        final MacAddress macsrc = new MacAddress(SRC_MAC_ADDRESS);
        ethsrc.setAddress(macsrc);
        ethsrc.setMask(new MacAddress("ff:ff:00:00:00:00"));

        ethmatch.setEthernetSource(ethsrc.build());
        match.setEthernetMatch(ethmatch.build());
        return match;

    }

    private static MatchBuilder createVlanMatch() {
        final MatchBuilder match = new MatchBuilder();
        // vlan match
        final VlanMatchBuilder vlanBuilder = new VlanMatchBuilder();
        final VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
        final VlanId vlanId = new VlanId(Uint16.TEN);
        final VlanPcp vpcp = new VlanPcp(Uint8.valueOf(3));
        vlanBuilder.setVlanPcp(vpcp);
        vlanIdBuilder.setVlanId(vlanId);
        vlanIdBuilder.setVlanIdPresent(true);
        vlanBuilder.setVlanId(vlanIdBuilder.build());
        match.setVlanMatch(vlanBuilder.build());
        return match;
    }

    private static MatchBuilder createArpMatch() {
        final MatchBuilder match = new MatchBuilder();

        final EthernetMatchBuilder ethmatch = new EthernetMatchBuilder();
        final MacAddress macdest = new MacAddress(DEST_MAC_ADDRESS);
        final MacAddress macsrc = new MacAddress(SRC_MAC_ADDRESS);

        final EthernetTypeBuilder ethtype = new EthernetTypeBuilder();
        final EtherType type = new EtherType(Uint32.valueOf(0x0806));
        ethmatch.setEthernetType(ethtype.setType(type).build());

        // ipv4 match
        final Ipv4Prefix dstip = new Ipv4Prefix("200.71.9.52/10");
        final Ipv4Prefix srcip = new Ipv4Prefix("100.1.1.1/8");

        // arp match
        final ArpMatchBuilder arpmatch = new ArpMatchBuilder();
        final ArpSourceHardwareAddressBuilder arpsrc = new ArpSourceHardwareAddressBuilder();
        arpsrc.setAddress(macsrc);
        arpsrc.setMask(new MacAddress("ff:ff:ff:00:00:00"));
        final ArpTargetHardwareAddressBuilder arpdst = new ArpTargetHardwareAddressBuilder();
        arpdst.setAddress(macdest);
        arpdst.setMask(new MacAddress("ff:ff:00:00:00:00"));
        arpmatch.setArpOp(Uint16.TWO);
        arpmatch.setArpSourceHardwareAddress(arpsrc.build());
        arpmatch.setArpTargetHardwareAddress(arpdst.build());
        arpmatch.setArpSourceTransportAddress(srcip);
        arpmatch.setArpTargetTransportAddress(dstip);

        match.setEthernetMatch(ethmatch.build());
        match.setLayer3Match(arpmatch.build());

        return match;
    }

    private static MatchBuilder createL3IPv6Match() {
        final MatchBuilder match = new MatchBuilder();

        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(Uint32.valueOf(0x86dd)));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        final MacAddress ndsll = new MacAddress("c2:00:54:f5:00:00");
        final MacAddress ndtll = new MacAddress("00:0c:29:0e:4c:67");
        final Ipv6LabelBuilder ipv6label = new Ipv6LabelBuilder();
        final Ipv6FlowLabel label = new Ipv6FlowLabel(Uint32.valueOf(10028));
        ipv6label.setIpv6Flabel(label);
        ipv6label.setFlabelMask(new Ipv6FlowLabel(Uint32.ONE));

        final Icmpv6MatchBuilder icmpv6match = new Icmpv6MatchBuilder(); // icmpv6
        // match
        icmpv6match.setIcmpv6Type(Uint8.valueOf(135));
        icmpv6match.setIcmpv6Code(Uint8.ZERO);
        match.setIcmpv6Match(icmpv6match.build());

        final Ipv6MatchBuilder ipv6match = new Ipv6MatchBuilder();
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

    private static MatchBuilder createICMPv4Match() {
        final MatchBuilder match = new MatchBuilder();
        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(Uint32.valueOf(0x0800)));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        final IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol(Uint8.ONE);
        match.setIpMatch(ipmatch.build());

        final Icmpv4MatchBuilder icmpv4match = new Icmpv4MatchBuilder(); // icmpv4
        // match
        icmpv4match.setIcmpv4Type(Uint8.valueOf(8));
        icmpv4match.setIcmpv4Code(Uint8.ZERO);
        match.setIcmpv4Match(icmpv4match.build());
        return match;
    }

    private static MatchBuilder createICMPv6Match() {

        final MatchBuilder match = new MatchBuilder();
        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(Uint32.valueOf(0x86dd)));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        final IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol(Uint8.valueOf(58));
        match.setIpMatch(ipmatch.build());

        final Icmpv6MatchBuilder icmpv6match = new Icmpv6MatchBuilder(); // icmpv6
        // match
        icmpv6match.setIcmpv6Type(Uint8.valueOf(135));
        icmpv6match.setIcmpv6Code(Uint8.ONE);
        match.setIcmpv6Match(icmpv6match.build());

        return match;
    }

    private static MatchBuilder createToSMatch() {
        final MatchBuilder match = new MatchBuilder();
        final EthernetMatchBuilder ethmatch = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethtype = new EthernetTypeBuilder();
        final EtherType type = new EtherType(Uint32.valueOf(0x0800));
        ethmatch.setEthernetType(ethtype.setType(type).build());
        match.setEthernetMatch(ethmatch.build());

        final IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol(Uint8.valueOf(6));
        final Dscp dscp = new Dscp(Uint8.valueOf(8));
        ipmatch.setIpDscp(dscp);
        match.setIpMatch(ipmatch.build());
        return match;
    }

    private static MatchBuilder createL4TCPMatch() {
        final MatchBuilder match = new MatchBuilder();

        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(Uint32.valueOf(0x0800)));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        final IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol(Uint8.valueOf(6));
        match.setIpMatch(ipmatch.build());

        final PortNumber srcport = new PortNumber(Uint16.valueOf(1213));
        final PortNumber dstport = new PortNumber(Uint16.valueOf(646));
        final TcpMatchBuilder tcpmatch = new TcpMatchBuilder(); // tcp match
        tcpmatch.setTcpSourcePort(srcport);
        tcpmatch.setTcpDestinationPort(dstport);
        match.setLayer4Match(tcpmatch.build());

        return match;
    }

    private static MatchBuilder createL4UDPMatch() {
        final MatchBuilder match = new MatchBuilder();

        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(Uint32.valueOf(0x0800)));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        final IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol(Uint8.valueOf(17));
        match.setIpMatch(ipmatch.build());

        final PortNumber srcport = new PortNumber(Uint16.valueOf(1325));
        final PortNumber dstport = new PortNumber(Uint16.valueOf(42));
        final UdpMatchBuilder udpmatch = new UdpMatchBuilder(); // udp match
        udpmatch.setUdpDestinationPort(dstport);
        udpmatch.setUdpSourcePort(srcport);
        match.setLayer4Match(udpmatch.build());

        return match;
    }

    private static MatchBuilder createL4SCTPMatch() {
        final MatchBuilder match = new MatchBuilder();

        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(Uint32.valueOf(0x0800)));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        final IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol(Uint8.valueOf(132));
        match.setIpMatch(ipmatch.build());

        final SctpMatchBuilder sctpmatch = new SctpMatchBuilder();
        final PortNumber srcport = new PortNumber(Uint16.valueOf(1435));
        final PortNumber dstport = new PortNumber(Uint16.valueOf(22));
        sctpmatch.setSctpSourcePort(srcport);
        sctpmatch.setSctpDestinationPort(dstport);
        match.setLayer4Match(sctpmatch.build());

        return match;
    }

    private static MatchBuilder createMetadataMatch() {
        final MatchBuilder match = new MatchBuilder();
        final MetadataBuilder metadata = new MetadataBuilder(); // metadata match
        metadata.setMetadata(Uint64.valueOf(500));
        metadata.setMetadataMask(Uint64.valueOf(0xFFFFFF00_00000101L));
        match.setMetadata(metadata.build());

        return match;
    }

    private static MatchBuilder createMplsMatch() {
        final MatchBuilder match = new MatchBuilder();

        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(Uint32.valueOf(0x8847)));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        final ProtocolMatchFieldsBuilder protomatch = new ProtocolMatchFieldsBuilder(); // mpls
        // match
        protomatch.setMplsLabel(Uint32.valueOf(36008));
        protomatch.setMplsTc(Uint8.valueOf(4));
        protomatch.setMplsBos(Uint8.ONE);
        match.setProtocolMatchFields(protomatch.build());

        return match;

    }

    private static MatchBuilder createPbbMatch() {
        final MatchBuilder match = new MatchBuilder();

        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(Uint32.valueOf(0x88E7)));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        final ProtocolMatchFieldsBuilder protomatch = new ProtocolMatchFieldsBuilder(); // mpls
        // match
        protomatch.setPbb(new PbbBuilder().setPbbIsid(Uint32.valueOf(4)).setPbbMask(Uint32.valueOf(0x10000)).build());
        match.setProtocolMatchFields(protomatch.build());

        return match;

    }

    private static MatchBuilder createTunnelIDMatch() {
        final MatchBuilder match = new MatchBuilder();
        final TunnelBuilder tunnel = new TunnelBuilder(); // tunnel id match
        tunnel.setTunnelId(Uint64.valueOf(10668));
        tunnel.setTunnelMask(Uint64.valueOf(0xFFFFFF00_00000101L));
        match.setTunnel(tunnel.build());

        return match;
    }

    /**
     * Test match for TCP_Flags.
     *
     * @return match containing Ethertype (0x0800), IP Protocol (TCP), TCP_Flag (SYN)
     */
    //FIXME: move to extensible support
    private static MatchBuilder createTcpFlagMatch() {
        final MatchBuilder match = new MatchBuilder();

        // Ethertype match
        final EthernetMatchBuilder ethernetType = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(Uint32.valueOf(0x0800)));
        ethernetType.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(ethernetType.build());

        // TCP Protocol Match
        final IpMatchBuilder ipMatch = new IpMatchBuilder(); // ipv4 version
        ipMatch.setIpProtocol(Uint8.valueOf(6));
        match.setIpMatch(ipMatch.build());

        // TCP Port Match
        final PortNumber dstPort = new PortNumber(Uint16.valueOf(80));
        final TcpMatchBuilder tcpMatch = new TcpMatchBuilder();
        tcpMatch.setTcpDestinationPort(dstPort);
        match.setLayer4Match(tcpMatch.build());
        /**
         * Defined TCP Flag values in OVS v2.1+
         * TCP_FIN 0x001 / TCP_SYN 0x002 / TCP_RST 0x004
         * TCP_PSH 0x008 / TCP_ACK 0x010 / TCP_URG 0x020
         * TCP_ECE 0x040 / TCP_CWR 0x080 / TCP_NS  0x100
         */
        final TcpFlagsMatchBuilder tcpFlagsMatch = new TcpFlagsMatchBuilder();
        tcpFlagsMatch.setTcpFlags(Uint16.valueOf(0x002));
        match.setTcpFlagsMatch(tcpFlagsMatch.build());

        return match;
    }

    @SuppressWarnings("checkstyle:MethodName")
    public void _removeMDFlow(final CommandInterpreter ci) {
        final ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        final NodeBuilder tn = createTestNode(ci.nextArgument());
        final String flowtype = ci.nextArgument();
        FlowBuilder tf;
        if (flowtype.equals("fTM")) {
            tf = createtablemiss();
        } else {
            tf = createTestFlow(tn, flowtype, ci.nextArgument());
        }
        final InstanceIdentifier<Flow> path1 = InstanceIdentifier.create(Nodes.class).child(Node.class, tn.key())
                .augmentation(FlowCapableNode.class).child(Table.class, new TableKey(tf.getTableId()))
                .child(Flow.class, tf.key());
        modification.delete(LogicalDatastoreType.CONFIGURATION, path1);
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

    /**
     * Adds a flow.
     *
     * @param ci arguments: switchId flowType tableNum, e.g.: addMDFlow openflow:1 f1 42
     */
    @SuppressWarnings("checkstyle:MethodName")
    public void _addMDFlow(final CommandInterpreter ci) {
        final NodeBuilder tn = createTestNode(ci.nextArgument());
        final String flowtype = ci.nextArgument();
        FlowBuilder tf;
        if (flowtype.equals("fTM")) {
            tf = createtablemiss();
        } else {
            tf = createTestFlow(tn, flowtype, ci.nextArgument());
        }
        writeFlow(ci, tf, tn);
    }

    private void writeFlow(final CommandInterpreter ci, final FlowBuilder flow, final NodeBuilder nodeBuilder) {
        final ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        final InstanceIdentifier<Flow> path1 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, nodeBuilder.key()).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flow.getTableId())).child(Flow.class, flow.key());
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION,
                nodeBuilderToInstanceId(nodeBuilder), nodeBuilder.build());
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, path1, flow.build());
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

    @SuppressWarnings("checkstyle:MethodName")
    public void _modifyMDFlow(final CommandInterpreter ci) {
        final NodeBuilder tn = createTestNode(ci.nextArgument());
        final FlowBuilder tf = createTestFlow(tn, ci.nextArgument(), ci.nextArgument());
        tf.setFlowName(UPDATED_FLOW_NAME);
        writeFlow(ci, tf, tn);
        tf.setFlowName(ORIGINAL_FLOW_NAME);
        writeFlow(ci, tf, tn);
    }

    @Override
    public String getHelp() {
        return "No help";
    }

    /*
     * usage testSwitchFlows <numberOfSwitches> <numberOfFlows> <warmup iterations> <Number Of Threads>
     * ex: _perfFlowTest 10 5 1 2
     */
    @SuppressWarnings("checkstyle:MethodName")
    public void _perfFlowTest(final CommandInterpreter ci) {

        final String numberOfSwtichesStr = ci.nextArgument();
        final String numberOfFlowsStr = ci.nextArgument();
        final String warmupIterationsStr = ci.nextArgument();
        final String threadCountStr = ci.nextArgument();
        final String warmUpStr = ci.nextArgument();

        int numberOfSwtiches = 0;
        int numberOfFlows = 0;
        int warmupIterations = 0;

        if (numberOfSwtichesStr != null && !numberOfSwtichesStr.trim().equals("")) {
            numberOfSwtiches = Integer.parseInt(numberOfSwtichesStr);
        } else {
            numberOfSwtiches = 2;
        }

        if (numberOfFlowsStr != null && !numberOfFlowsStr.trim().equals("")) {
            numberOfFlows = Integer.parseInt(numberOfFlowsStr);
        } else {
            numberOfFlows = 2;
        }

        if (warmupIterationsStr != null && !warmupIterationsStr.trim().equals("")) {
            warmupIterations = Integer.parseInt(warmupIterationsStr);
        } else {
            warmupIterations = 2;
        }

        final int threadCount;
        if (threadCountStr != null && !threadCountStr.trim().equals("")) {
            threadCount = Integer.parseInt(threadCountStr);
        } else {
            threadCount = 2;
        }

        final boolean warmUpIterations;
        if (warmUpStr != null && !warmUpStr.trim().equals("") && warmUpStr.trim().equals("true")) {
            warmUpIterations = true;
        } else {
            warmUpIterations = false;
        }

        ci.println("*     Test Configurations*");
        ci.println("*     numberOfSwtiches:::" + numberOfSwtiches + "");
        ci.println("*     numberOfFlows:::" + numberOfFlows + "");
        ci.println("*     warmupIterations:::" + warmupIterations + "");
        ci.println("*     Number of Threads :::" + threadCount + "");
        ci.println("*     Warmup Required? :::" + warmUpIterations + "");

        String dataPath = "openflow:1";
        NodeBuilder tn;
        FlowBuilder tf;
        final String tableId = "0";
        if (warmUpIterations) {
            ci.println("----Warmup Started-----");
            for (int j = 1; j <= warmupIterations; j++) {
                for (int i = 1; i <= numberOfSwtiches; i++) {
                    dataPath = "openflow:" + i;
                    tn = createTestNode(dataPath);
                    for (int flow = 1; flow < numberOfFlows; flow++) {
                        tf = createTestFlowPerfTest("f1", tableId, flow);
                        writeFlow(ci, tf, tn);
                    }
                }
            }

            ci.println("----Warmup Done-----");
        }
        try {
            final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            int tableID = 0;
            for (int t = 0; t < threadCount; t++) {
                tableID = t + 1;
                final Runnable tRunnable = new TestFlowThread(numberOfSwtiches, numberOfFlows, ci, t, tableID);
                executor.execute(tRunnable);
            }
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            ci.println("Exception:" + e.getMessage());
        }
    }

    public class TestFlowThread implements Runnable {

        int numberOfSwitches;
        int numberOfFlows;
        CommandInterpreter ci;
        int theadNumber;
        int tableID = 0;

        TestFlowThread(final int numberOfSwtiches, final int numberOfFlows, final CommandInterpreter ci,
                final int threadNumber, final int tableID) {
            numberOfSwitches = numberOfSwtiches;
            this.numberOfFlows = numberOfFlows;
            this.ci = ci;
            theadNumber = threadNumber;
            this.tableID = tableID;
        }

        @Override
        public void run() {
            executeFlow();
        }

        public void executeFlow() {

            String dataPath = "openflow:1";
            NodeBuilder tn;
            FlowBuilder tf;
            //String tableId = "0";

            ci.println("New Thread started with id:  ID_" + theadNumber);
            int totalNumberOfFlows = 0;
            final long startTime = System.currentTimeMillis();

            for (int i = 1; i <= numberOfSwitches; i++) {
                dataPath = "openflow:" + i;
                tn = createTestNode(dataPath);
                for (int flow2 = 1; flow2 <= numberOfFlows; flow2++) {
                    tf = createTestFlowPerfTest("f1", "" + tableID, flow2);
                    writeFlow(ci, tf, tn);
                    totalNumberOfFlows++;
                }
            }
            final long endTime = System.currentTimeMillis();
            final long timeInSeconds = Math.round((endTime - startTime) / 1000.0F);
            if (timeInSeconds > 0) {
                ci.println("Total flows added in Thread:" + theadNumber + ": Flows/Sec::"
                    + Math.round((float)totalNumberOfFlows / timeInSeconds));
            } else {
                ci.println("Total flows added in Thread:" + theadNumber + ": Flows/Sec::" + totalNumberOfFlows);
            }
        }

    }

    /*
     * usage testAllFlows <dp>
     * ex: _perfFlowTest 1
     */
    @SuppressWarnings({ "checkstyle:MethodName", "checkstyle:IllegalCatch" })
    public void _testAllFlows(final CommandInterpreter ci) {
        String dataPathID = ci.nextArgument();
        final int numberOfFlows = 82;
        if (dataPathID == null || dataPathID.trim().equals("")) {
            dataPathID = "1";
        }
        ci.println("*     Test All Flows    *");
        ci.println("*     dataPathID:::" + dataPathID + "");
        final String dataPath = "openflow:" + dataPathID;
        final String tableId = "0";
        final NodeBuilder tn = createTestNode(dataPath);
        FlowBuilder tf;
        for (int flow = 1; flow < numberOfFlows; flow++) {
            final String flowID = "f" + flow;
            try {
                tf = createTestFlow(tn, flowID, tableId);
                writeFlow(ci, tf, tn);
            } catch (RuntimeException e) {
                ci.println("--Test Failed--Issue found while adding flow" + flow);
                break;
            }
        }
    }
}
