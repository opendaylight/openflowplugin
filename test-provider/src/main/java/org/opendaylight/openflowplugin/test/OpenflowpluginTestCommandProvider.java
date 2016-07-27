/*
 * Copyright (c) 2013, 2015 Ericsson, Inc. and others.  All rights reserved.
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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.NodeErrorListener;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginTestCommandProvider implements CommandProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginTestCommandProvider.class);

    private DataBroker dataBroker;
    private final BundleContext ctx;
    private FlowBuilder testFlow;
    private static final String ORIGINAL_FLOW_NAME = "Foo";
    private static final String UPDATED_FLOW_NAME = "Bar";
    private static final String IPV4_PREFIX = "10.0.0.1/24";
    private static final String DEST_MAC_ADDRESS = "ff:ff:ff:ff:ff:ff";
    private static final String SRC_MAC_ADDRESS = "00:00:00:00:23:ae";
    private final SalFlowListener flowEventListener = new FlowEventListenerLoggingImpl();
    private final NodeErrorListener nodeErrorListener = new NodeErrorListenerLoggingImpl();
    private static NotificationService notificationService;

    public OpenflowpluginTestCommandProvider(final BundleContext ctx) {
        this.ctx = ctx;
    }

    public void onSessionInitiated(final ProviderContext session) {
        notificationService = session.getSALService(NotificationService.class);
        // For switch events
        notificationService.registerNotificationListener(flowEventListener);
        notificationService.registerNotificationListener(nodeErrorListener);
        dataBroker = session.getSALService(DataBroker.class);
        ctx.registerService(CommandProvider.class.getName(), this, null);
        createTestFlow(createTestNode(null), null, null);
    }

    private NodeBuilder createTestNode(final String nodeId) {
        String localNodeId;

        if (nodeId == null) {
            localNodeId = OpenflowpluginTestActivator.NODE_ID;
        } else {
            localNodeId = nodeId;
        }

        final NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(localNodeId));
        builder.setKey(new NodeKey(builder.getId()));
        return builder;
    }

    private InstanceIdentifier<Node> nodeBuilderToInstanceId(final NodeBuilder node) {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, node.getKey());
    }

    private FlowBuilder createTestFlow(final NodeBuilder nodeBuilder, final String flowTypeArg, final String tableId) {
        final long TEST_ID = 123;

        final FlowBuilder flow = new FlowBuilder();
        long id = TEST_ID;

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

        final FlowKey key = new FlowKey(new FlowId(Long.toString(id)));
        if (null == flow.isBarrier()) {
            flow.setBarrier(Boolean.FALSE);
        }
        final BigInteger value = BigInteger.valueOf(10);
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

        flow.setKey(key);
        flow.setFlowName(ORIGINAL_FLOW_NAME + "X" + flowType);
        testFlow = flow;
        return flow;
    }


    private FlowBuilder createTestFlowPerfTest(final String flowTypeArg, final String tableId, final int id) {
        final FlowBuilder flow = new FlowBuilder();
        String flowType = flowTypeArg;
        int flowId = id;

        if (flowType == null) {
            flowType = "f1";
        }

        flow.setPriority(flowId);

        switch (flowType) {
            case "f1":
                flowId += 1;
                flow.setMatch(createMatch1().build());
                flow.setInstructions(createDecNwTtlInstructions().build());
                break;
            default:
                LOG.warn("flow type not understood: {}", flowType);
        }

        final FlowKey key = new FlowKey(new FlowId(Long.toString(flowId)));
        if (null == flow.isBarrier()) {
            flow.setBarrier(Boolean.FALSE);
        }
        final BigInteger value = BigInteger.valueOf(10);
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

        flow.setKey(key);
        flow.setFlowName(ORIGINAL_FLOW_NAME + "X" + flowType);
        testFlow = flow;
        return flow;
    }

    private FlowBuilder createtablemiss() {
        final FlowBuilder flow = new FlowBuilder();
        final long id = 456;
        final MatchBuilder matchBuilder = new MatchBuilder();
        flow.setMatch(matchBuilder.build());
        flow.setInstructions(createSentToControllerInstructions().build());
        flow.setPriority(0);
        flow.setTableId((short) 0);
        final FlowKey key = new FlowKey(new FlowId(Long.toString(id)));
        flow.setKey(key);
        testFlow = flow;
        return flow;
    }

    private short getTableId(final String tableId) {
        final short TABLE_ID = 2;
        short table = TABLE_ID;
        try {
            table = Short.parseShort(tableId);
        } catch (final Exception ex) {
            LOG.info("Parsing String tableId {} failed. Continuing with default tableId {}.",
                    tableId, table);
        }
        return table;
    }

    /**
     * @return
     */
    private static InstructionsBuilder createDecNwTtlInstructions() {
        final DecNwTtlBuilder ta = new DecNwTtlBuilder();
        final DecNwTtl decNwTtl = ta.build();
        final ActionBuilder ab = new ActionBuilder();
        ab.setAction(new DecNwTtlCaseBuilder().setDecNwTtl(decNwTtl).build());
        ab.setKey(new ActionKey(0));
        // Add our drop action to a list
        final List<Action> actionList = new ArrayList<Action>();
        actionList.add(ab.build());

        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setKey(new InstructionKey(0));
        ib.setOrder(0);

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        ib.setKey(new InstructionKey(0));
        isb.setInstruction(instructions);
        return isb;
    }

    /**
     * @return
     */
    private static InstructionsBuilder createMeterInstructions() {

        final MeterBuilder aab = new MeterBuilder();
        aab.setMeterId(new MeterId(1L));

        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new MeterCaseBuilder().setMeter(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createMetadataInstructions() {

        final WriteMetadataBuilder aab = new WriteMetadataBuilder();
        aab.setMetadata(BigInteger.valueOf(10));
        aab.setMetadataMask(BigInteger.valueOf(10));

        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new WriteMetadataCaseBuilder().setWriteMetadata(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createGotoTableInstructions() {

        final GoToTableBuilder aab = new GoToTableBuilder();
        aab.setTableId((short) 5);

        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new GoToTableCaseBuilder().setGoToTable(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createDropInstructions() {
        final DropActionBuilder dab = new DropActionBuilder();
        final DropAction dropAction = dab.build();
        final ActionBuilder ab = new ActionBuilder();
        ab.setAction(new DropActionCaseBuilder().setDropAction(dropAction).build());
        ab.setKey(new ActionKey(0));
        // Add our drop action to a list
        final List<Action> actionList = new ArrayList<Action>();
        actionList.add(ab.build());
        ab.setKey(new ActionKey(0));
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();
        final ControllerActionBuilder controller = new ControllerActionBuilder();
        controller.setMaxLength(5);
        ab.setAction(new ControllerActionCaseBuilder().setControllerAction(controller.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction1() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(56);
        final Uri value = new Uri("PCEP");
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createOutputInstructions() {

        // test case for Output Port works if the particular port exists
        // this particular test-case is for Port : 1
        // tested as (addMDFlow openflow:<dpid> f82)
        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();
        final OutputActionBuilder output = new OutputActionBuilder();

        final Uri value = new Uri("1");
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createSentToControllerInstructions() {
        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(Integer.valueOf(0xffff));
        final Uri value = new Uri(OutputPortValues.CONTROLLER.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createOutputInstructions(final String outputType, final int outputValue) {
        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(outputValue);
        final Uri value = new Uri(outputType);
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createStripVlanInstructions() {
        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final StripVlanActionBuilder stripActionBuilder = new StripVlanActionBuilder();
        ab.setAction(new StripVlanActionCaseBuilder().setStripVlanAction(stripActionBuilder.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction2() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final PushMplsActionBuilder push = new PushMplsActionBuilder();
        push.setEthernetType(Integer.valueOf(0x8847));
        ab.setAction(new PushMplsActionCaseBuilder().setPushMplsAction(push.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction3() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final PushPbbActionBuilder pbb = new PushPbbActionBuilder();
        pbb.setEthernetType(Integer.valueOf(0x88E7));
        ab.setAction(new PushPbbActionCaseBuilder().setPushPbbAction(pbb.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction4() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final PushVlanActionBuilder vlan = new PushVlanActionBuilder();
        vlan.setEthernetType(Integer.valueOf(0x8100));
        ab.setAction(new PushVlanActionCaseBuilder().setPushVlanAction(vlan.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction5() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final SetDlDstActionBuilder setdl = new SetDlDstActionBuilder();
        setdl.setAddress(new MacAddress("00:05:b9:7c:81:5f"));
        ab.setAction(new SetDlDstActionCaseBuilder().setSetDlDstAction(setdl.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction6() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final SetDlSrcActionBuilder src = new SetDlSrcActionBuilder();
        src.setAddress(new MacAddress("00:05:b9:7c:81:5f"));
        ab.setAction(new SetDlSrcActionCaseBuilder().setSetDlSrcAction(src.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction7() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final SetVlanIdActionBuilder vl = new SetVlanIdActionBuilder();
        final VlanId a = new VlanId(4000);
        vl.setVlanId(a);
        ab.setAction(new SetVlanIdActionCaseBuilder().setSetVlanIdAction(vl.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction8() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final SetVlanPcpActionBuilder pcp = new SetVlanPcpActionBuilder();
        final VlanPcp pcp1 = new VlanPcp((short) 2);
        pcp.setVlanPcp(pcp1);
        ab.setAction(new SetVlanPcpActionCaseBuilder().setSetVlanPcpAction(pcp.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction88() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final SetVlanPcpActionBuilder pcp = new SetVlanPcpActionBuilder();
        // the code point is a 3-bit(0-7) field representing the frame priority level
        final VlanPcp pcp1 = new VlanPcp((short) 4);
        pcp.setVlanPcp(pcp1);
        ab.setAction(new SetVlanPcpActionCaseBuilder().setSetVlanPcpAction(pcp.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction9() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final CopyTtlInBuilder ttlin = new CopyTtlInBuilder();
        ab.setAction(new CopyTtlInCaseBuilder().setCopyTtlIn(ttlin.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction10() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final CopyTtlOutBuilder ttlout = new CopyTtlOutBuilder();
        ab.setAction(new CopyTtlOutCaseBuilder().setCopyTtlOut(ttlout.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction11() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final DecMplsTtlBuilder mpls = new DecMplsTtlBuilder();
        ab.setAction(new DecMplsTtlCaseBuilder().setDecMplsTtl(mpls.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setKey(new InstructionKey(0));
        ib.setOrder(0);

        // Put our Instruction in a list of Instruction
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction12() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final DecNwTtlBuilder nwttl = new DecNwTtlBuilder();
        ab.setAction(new DecNwTtlCaseBuilder().setDecNwTtl(nwttl.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction13() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final DropActionBuilder drop = new DropActionBuilder();
        ab.setAction(new DropActionCaseBuilder().setDropAction(drop.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction14() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final FloodActionBuilder fld = new FloodActionBuilder();
        ab.setAction(new FloodActionCaseBuilder().setFloodAction(fld.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction15() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final FloodAllActionBuilder fldall = new FloodAllActionBuilder();
        ab.setAction(new FloodAllActionCaseBuilder().setFloodAllAction(fldall.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction16() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final GroupActionBuilder groupActionB = new GroupActionBuilder();
        groupActionB.setGroupId(1L);
        groupActionB.setGroup("0");
        ab.setAction(new GroupActionCaseBuilder().setGroupAction(groupActionB.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction17() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final HwPathActionBuilder hwPathB = new HwPathActionBuilder();
        ab.setAction(new HwPathActionCaseBuilder().setHwPathAction(hwPathB.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction18() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final LoopbackActionBuilder loopbackActionBuilder = new LoopbackActionBuilder();
        ab.setAction(new LoopbackActionCaseBuilder().setLoopbackAction(loopbackActionBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction19() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final PopMplsActionBuilder popMplsActionBuilder = new PopMplsActionBuilder();
        popMplsActionBuilder.setEthernetType(0XB);
        ab.setAction(new PopMplsActionCaseBuilder().setPopMplsAction(popMplsActionBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction20() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final PopPbbActionBuilder popPbbActionBuilder = new PopPbbActionBuilder();
        ab.setAction(new PopPbbActionCaseBuilder().setPopPbbAction(popPbbActionBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction21() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final PopVlanActionBuilder popVlanActionBuilder = new PopVlanActionBuilder();
        ab.setAction(new PopVlanActionCaseBuilder().setPopVlanAction(popVlanActionBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction22() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final SetDlTypeActionBuilder setDlTypeActionBuilder = new SetDlTypeActionBuilder();
        setDlTypeActionBuilder.setDlType(new EtherType(8L));
        ab.setAction(new SetDlTypeActionCaseBuilder().setSetDlTypeAction(setDlTypeActionBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction23(final NodeId nodeId) {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        setFieldBuilder.setInPort(new NodeConnectorId(nodeId + ":2"));
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction24() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final SetMplsTtlActionBuilder setMplsTtlActionBuilder = new SetMplsTtlActionBuilder();
        setMplsTtlActionBuilder.setMplsTtl((short) 0X1);
        ab.setAction(new SetMplsTtlActionCaseBuilder().setSetMplsTtlAction(setMplsTtlActionBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction25() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final SetNextHopActionBuilder setNextHopActionBuilder = new SetNextHopActionBuilder();
        final Ipv4Builder ipnext = new Ipv4Builder();
        final Ipv4Prefix prefix = new Ipv4Prefix(IPV4_PREFIX);
        ipnext.setIpv4Address(prefix);
        setNextHopActionBuilder.setAddress(ipnext.build());
        ab.setAction(new SetNextHopActionCaseBuilder().setSetNextHopAction(setNextHopActionBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction26() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final SetNwDstActionBuilder setNwDstActionBuilder = new SetNwDstActionBuilder();
        final Ipv4Builder ipdst = new Ipv4Builder();
        final Ipv4Prefix prefixdst = new Ipv4Prefix("10.0.0.21/24");
        ipdst.setIpv4Address(prefixdst);
        setNwDstActionBuilder.setAddress(ipdst.build());
        ab.setAction(new SetNwDstActionCaseBuilder().setSetNwDstAction(setNwDstActionBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction27() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final SetNwSrcActionBuilder setNwsrcActionBuilder = new SetNwSrcActionBuilder();
        final Ipv4Builder ipsrc = new Ipv4Builder();
        final Ipv4Prefix prefixsrc = new Ipv4Prefix("10.0.23.21/24");
        ipsrc.setIpv4Address(prefixsrc);
        setNwsrcActionBuilder.setAddress(ipsrc.build());
        ab.setAction(new SetNwSrcActionCaseBuilder().setSetNwSrcAction(setNwsrcActionBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction28() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final SetNwTosActionBuilder setNwTosActionBuilder = new SetNwTosActionBuilder();
        setNwTosActionBuilder.setTos(8);
        ab.setAction(new SetNwTosActionCaseBuilder().setSetNwTosAction(setNwTosActionBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction29() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final SetNwTtlActionBuilder setNwTtlActionBuilder = new SetNwTtlActionBuilder();
        setNwTtlActionBuilder.setNwTtl((short) 1);
        ab.setAction(new SetNwTtlActionCaseBuilder().setSetNwTtlAction(setNwTtlActionBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction30() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final SetQueueActionBuilder setQueueActionBuilder = new SetQueueActionBuilder();
        setQueueActionBuilder.setQueueId(1L);
        ab.setAction(new SetQueueActionCaseBuilder().setSetQueueAction(setQueueActionBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction31() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final SetTpDstActionBuilder setTpDstActionBuilder = new SetTpDstActionBuilder();
        setTpDstActionBuilder.setPort(new PortNumber(109));

        ab.setAction(new SetTpDstActionCaseBuilder().setSetTpDstAction(setTpDstActionBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction32() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final SetTpSrcActionBuilder setTpSrcActionBuilder = new SetTpSrcActionBuilder();
        setTpSrcActionBuilder.setPort(new PortNumber(109));
        ab.setAction(new SetTpSrcActionCaseBuilder().setSetTpSrcAction(setTpSrcActionBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction33() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final SetVlanCfiActionBuilder setVlanCfiActionBuilder = new SetVlanCfiActionBuilder();
        setVlanCfiActionBuilder.setVlanCfi(new VlanCfi(2));
        ab.setAction(new SetVlanCfiActionCaseBuilder().setSetVlanCfiAction(setVlanCfiActionBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction34() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();

        final SwPathActionBuilder swPathAction = new SwPathActionBuilder();
        ab.setAction(new SwPathActionCaseBuilder().setSwPathAction(swPathAction.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create an Apply Action
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction35() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();
        final ActionBuilder ab1 = new ActionBuilder();
        final ActionBuilder ab2 = new ActionBuilder();

        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        final SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();
        final SetFieldBuilder setFieldBuilder2 = new SetFieldBuilder();

        // Ethernet
        final EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        final EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
        ethSourceBuilder.setAddress(new MacAddress("00:00:00:00:00:01"));
        final EthernetMatchBuilder ethernetMatch1 = new EthernetMatchBuilder();
        final EthernetDestinationBuilder ethDestBuilder = new EthernetDestinationBuilder();
        ethDestBuilder.setAddress(new MacAddress("00:00:00:00:00:02"));
        final EthernetMatchBuilder ethernetMatch2 = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
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

        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        final InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction36() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();
        final ActionBuilder ab1 = new ActionBuilder();

        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        final SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();

        // Vlan
        final VlanMatchBuilder vlanBuilder = new VlanMatchBuilder();
        final VlanMatchBuilder vlanBuilder1 = new VlanMatchBuilder();
        final VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
        final VlanId vlanId = new VlanId(10);
        final VlanPcp vpcp = new VlanPcp((short) 3);
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

        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        final InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction37() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();
        final ActionBuilder ab1 = new ActionBuilder();
        final ActionBuilder ab2 = new ActionBuilder();

        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        final SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();
        final SetFieldBuilder setFieldBuilder2 = new SetFieldBuilder();
        // Ip
        final IpMatchBuilder ipmatch = new IpMatchBuilder();
        final IpMatchBuilder ipmatch1 = new IpMatchBuilder();
        final IpMatchBuilder ipmatch2 = new IpMatchBuilder();
        final Dscp dscp = new Dscp((short) 3);
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

        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        final InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction38() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();
        final ActionBuilder ab1 = new ActionBuilder();

        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        final SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();
        // IPv4
        final Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        final Ipv4MatchBuilder ipv4Match1 = new Ipv4MatchBuilder();
        final Ipv4Prefix dstip = new Ipv4Prefix("200.71.9.5210");
        final Ipv4Prefix srcip = new Ipv4Prefix("100.1.1.1");
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

        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        final InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction39() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();
        final ActionBuilder ab1 = new ActionBuilder();

        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        final SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();

        // Tcp
        final PortNumber tcpsrcport = new PortNumber(1213);
        final PortNumber tcpdstport = new PortNumber(646);
        final TcpMatchBuilder tcpmatch = new TcpMatchBuilder();
        final TcpMatchBuilder tcpmatch1 = new TcpMatchBuilder();
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

        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        final InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction40() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();
        final ActionBuilder ab1 = new ActionBuilder();

        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        final SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();

        // Udp
        final PortNumber udpsrcport = new PortNumber(1325);
        final PortNumber udpdstport = new PortNumber(42);
        final UdpMatchBuilder udpmatch = new UdpMatchBuilder();
        final UdpMatchBuilder udpmatch1 = new UdpMatchBuilder();
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

        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        final InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction41() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();
        final ActionBuilder ab1 = new ActionBuilder();

        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        final SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();

        // Sctp
        final SctpMatchBuilder sctpmatch = new SctpMatchBuilder();
        final SctpMatchBuilder sctpmatch1 = new SctpMatchBuilder();
        final PortNumber srcport = new PortNumber(1435);
        final PortNumber dstport = new PortNumber(22);
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

        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        final InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction42() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();
        final ActionBuilder ab1 = new ActionBuilder();
        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        final SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();

        // Icmpv4
        final Icmpv4MatchBuilder icmpv4match = new Icmpv4MatchBuilder();
        final Icmpv4MatchBuilder icmpv4match1 = new Icmpv4MatchBuilder();
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

        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        final InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction43() {

        final List<Action> actionList = new ArrayList<Action>();
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

        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        final InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction44() {

        final List<Action> actionLists = new ArrayList<Action>();
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
        nextheader.setIpv6Exthdr(58);
        final Ipv6LabelBuilder ipv6label = new Ipv6LabelBuilder();
        final Ipv6FlowLabel label = new Ipv6FlowLabel(10028L);
        ipv6label.setIpv6Flabel(label);

        ipv6Builder.setIpv6Source(srcip6);
        ipv6Builder1.setIpv6Destination(dstip6);
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

        setFieldBuilder5.setLayer3Match(ipv6Builder5.build());
        ab5.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder5.build()).build());
        ab5.setKey(new ActionKey(5));
        actionLists.add(ab5.build());

        setFieldBuilder6.setLayer3Match(ipv6Builder6.build());
        ab6.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder6.build()).build());
        ab6.setKey(new ActionKey(6));
        actionLists.add(ab6.build());

        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionLists);

        final InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction45() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();
        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        final ActionBuilder ab1 = new ActionBuilder();
        final SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();

        // Icmpv6
        final Icmpv6MatchBuilder icmpv6match = new Icmpv6MatchBuilder();
        final Icmpv6MatchBuilder icmpv6match1 = new Icmpv6MatchBuilder();
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

        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        final InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction46() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();
        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        final ActionBuilder ab1 = new ActionBuilder();
        final SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();
        final ActionBuilder ab2 = new ActionBuilder();
        final SetFieldBuilder setFieldBuilder2 = new SetFieldBuilder();

        // MPLS
        final ProtocolMatchFieldsBuilder protomatch = new ProtocolMatchFieldsBuilder();
        final ProtocolMatchFieldsBuilder protomatch1 = new ProtocolMatchFieldsBuilder();
        final ProtocolMatchFieldsBuilder protomatch2 = new ProtocolMatchFieldsBuilder();
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

        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        final InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction47() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();
        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        // PBB
        final ProtocolMatchFieldsBuilder protomatch = new ProtocolMatchFieldsBuilder();
        protomatch.setPbb(new PbbBuilder().setPbbIsid(4L).setPbbMask((new BigInteger(new byte[]{0, 1, 0, 0}).longValue())).build());
        setFieldBuilder.setProtocolMatchFields(protomatch.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        actionList.add(ab.build());

        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        final InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createAppyActionInstruction48() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();
        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        // Tunnel
        final TunnelBuilder tunnel = new TunnelBuilder();
        tunnel.setTunnelId(BigInteger.valueOf(10668));
        setFieldBuilder.setTunnel(tunnel.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        final InstructionBuilder ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createTunnelIpv4DstInstructions() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();
        // Build the tunnel endpoint destination IPv4 address
        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        final Ipv4Prefix dstIp = new Ipv4Prefix("172.16.100.100");
        // Add the mew IPv4 object as the tunnel destination
        final TunnelIpv4MatchBuilder tunnelIpv4DstMatchBuilder = new TunnelIpv4MatchBuilder();
        tunnelIpv4DstMatchBuilder.setTunnelIpv4Destination(dstIp);
        setFieldBuilder.setLayer3Match(tunnelIpv4DstMatchBuilder.build());
        // Add the IPv4 tunnel dst to the set_field value
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Resulting action is a per/flow src TEP (set_field:172.16.100.100->tun_dst)
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);
        // Add the action to the ordered list of Instructions
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        // Add the Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static InstructionsBuilder createTunnelIpv4SrcInstructions() {

        final List<Action> actionList = new ArrayList<Action>();
        final ActionBuilder ab = new ActionBuilder();
        // Build the tunnel endpoint source IPv4 address
        final SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        final Ipv4Prefix dstIp = new Ipv4Prefix("172.16.100.200");
        // Add the new IPv4 object as the tunnel destination
        final TunnelIpv4MatchBuilder tunnelIpv4MatchBuilder = new TunnelIpv4MatchBuilder();
        tunnelIpv4MatchBuilder.setTunnelIpv4Source(dstIp);
        setFieldBuilder.setLayer3Match(tunnelIpv4MatchBuilder.build());
        // Add the IPv4 tunnel src to the set_field value
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Resulting action is a per/flow src TEP (set_field:172.16.100.100->tun_src)
        final ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);
        // Add the action to the ordered list of Instructions
        final InstructionBuilder ib = new InstructionBuilder();
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        // Put our Instruction in a list of Instructions
        final InstructionsBuilder isb = new InstructionsBuilder();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

    private static MatchBuilder createLLDPMatch() {
        final MatchBuilder match = new MatchBuilder();
        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x88ccL));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());
        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createMatch1() {
        final MatchBuilder match = new MatchBuilder();
        final Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        final Ipv4Prefix prefix = new Ipv4Prefix(IPV4_PREFIX);;
        ipv4Match.setIpv4Destination(prefix);
        final Ipv4Match i4m = ipv4Match.build();
        match.setLayer3Match(i4m);

        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x0800L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());
        return match;
    }


    /**
     * @return
     */
    private static MatchBuilder createMatch2() {
        final MatchBuilder match = new MatchBuilder();
        final Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        final Ipv4Prefix prefix = new Ipv4Prefix("10.0.0.1");
        ipv4Match.setIpv4Source(prefix);
        final Ipv4Match i4m = ipv4Match.build();
        match.setLayer3Match(i4m);

        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x0800L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());
        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createMatch3() {
        final MatchBuilder match = new MatchBuilder();
        final EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        final EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
        ethSourceBuilder.setAddress(new MacAddress("00:00:00:00:00:01"));
        ethernetMatch.setEthernetSource(ethSourceBuilder.build());
        match.setEthernetMatch(ethernetMatch.build());

        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createICMPv6Match1() {

        final MatchBuilder match = new MatchBuilder();
        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x86ddL));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        // ipv4 version
        final IpMatchBuilder ipmatch = new IpMatchBuilder();
        ipmatch.setIpProtocol((short) 256);
        match.setIpMatch(ipmatch.build());

        // icmpv6
        final Icmpv6MatchBuilder icmpv6match = new Icmpv6MatchBuilder();

        // match
        icmpv6match.setIcmpv6Type((short) 135);
        icmpv6match.setIcmpv6Code((short) 1);
        match.setIcmpv6Match(icmpv6match.build());

        return match;
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
        ethTypeBuilder.setType(new EtherType(0xfffeL));
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
        final EtherType type = new EtherType(0x0800L);
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

    /**
     * @return
     */

    private static MatchBuilder createVlanMatch() {
        final MatchBuilder match = new MatchBuilder();
        // vlan match
        final VlanMatchBuilder vlanBuilder = new VlanMatchBuilder();
        final VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
        final VlanId vlanId = new VlanId(10);
        final VlanPcp vpcp = new VlanPcp((short) 3);
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
        final MatchBuilder match = new MatchBuilder();

        final EthernetMatchBuilder ethmatch = new EthernetMatchBuilder();
        final MacAddress macdest = new MacAddress(DEST_MAC_ADDRESS);
        final MacAddress macsrc = new MacAddress(SRC_MAC_ADDRESS);

        final EthernetTypeBuilder ethtype = new EthernetTypeBuilder();
        final EtherType type = new EtherType(0x0806L);
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
    private static MatchBuilder createL3IPv4Match() {
        final MatchBuilder match = new MatchBuilder();

        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x0800L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        final Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        // ipv4 match
        final Ipv4Prefix dstip = new Ipv4Prefix("200.71.9.52/10");
        final Ipv4Prefix srcip = new Ipv4Prefix("100.1.1.1/8");
        final Ipv4MatchBuilder ipv4match = new Ipv4MatchBuilder();
        ipv4match.setIpv4Destination(dstip);
        ipv4match.setIpv4Source(srcip);
        match.setLayer3Match(ipv4match.build());

        return match;

    }

    /**
     * @return
     */
    private static MatchBuilder createL3IPv6Match() {
        final MatchBuilder match = new MatchBuilder();

        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x86ddL));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        final Ipv6Prefix dstip6 = new Ipv6Prefix("2002::2/64");
        final Ipv6Prefix srcip6 = new Ipv6Prefix("2001:0:0:0:0:0:0:1/56");
        final Ipv6Address ndtarget = new Ipv6Address("2001:db8:0:1:fd97:f9f0:a810:782e");
        final MacAddress ndsll = new MacAddress("c2:00:54:f5:00:00");
        final MacAddress ndtll = new MacAddress("00:0c:29:0e:4c:67");
        final Ipv6ExtHeaderBuilder nextheader = new Ipv6ExtHeaderBuilder();
        nextheader.setIpv6Exthdr(58);
        final Ipv6LabelBuilder ipv6label = new Ipv6LabelBuilder();
        final Ipv6FlowLabel label = new Ipv6FlowLabel(10028L);
        ipv6label.setIpv6Flabel(label);
        ipv6label.setFlabelMask(new Ipv6FlowLabel(1L));

        final Icmpv6MatchBuilder icmpv6match = new Icmpv6MatchBuilder(); // icmpv6
        // match
        icmpv6match.setIcmpv6Type((short) 135);
        icmpv6match.setIcmpv6Code((short) 0);
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

    /**
     * @return
     */

    private static MatchBuilder createICMPv4Match() {
        final MatchBuilder match = new MatchBuilder();
        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x0800L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        final IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol((short) 1);
        match.setIpMatch(ipmatch.build());

        final Icmpv4MatchBuilder icmpv4match = new Icmpv4MatchBuilder(); // icmpv4
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

        final MatchBuilder match = new MatchBuilder();
        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x86ddL));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        final IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol((short) 58);
        match.setIpMatch(ipmatch.build());

        final Icmpv6MatchBuilder icmpv6match = new Icmpv6MatchBuilder(); // icmpv6
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
        final MatchBuilder match = new MatchBuilder();
        final EthernetMatchBuilder ethmatch = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethtype = new EthernetTypeBuilder();
        final EtherType type = new EtherType(0x0800L);
        ethmatch.setEthernetType(ethtype.setType(type).build());
        match.setEthernetMatch(ethmatch.build());

        final IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol((short) 6);
        final Dscp dscp = new Dscp((short) 8);
        ipmatch.setIpDscp(dscp);
        match.setIpMatch(ipmatch.build());
        return match;
    }

    /**
     * @return
     */

    private static MatchBuilder createL4TCPMatch() {
        final MatchBuilder match = new MatchBuilder();

        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x0800L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        final IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol((short) 6);
        match.setIpMatch(ipmatch.build());

        final PortNumber srcport = new PortNumber(1213);
        final PortNumber dstport = new PortNumber(646);
        final TcpMatchBuilder tcpmatch = new TcpMatchBuilder(); // tcp match
        tcpmatch.setTcpSourcePort(srcport);
        tcpmatch.setTcpDestinationPort(dstport);
        match.setLayer4Match(tcpmatch.build());

        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createL4UDPMatch() {
        final MatchBuilder match = new MatchBuilder();

        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x0800L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        final IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol((short) 17);
        match.setIpMatch(ipmatch.build());

        final PortNumber srcport = new PortNumber(1325);
        final PortNumber dstport = new PortNumber(42);
        final UdpMatchBuilder udpmatch = new UdpMatchBuilder(); // udp match
        udpmatch.setUdpDestinationPort(dstport);
        udpmatch.setUdpSourcePort(srcport);
        match.setLayer4Match(udpmatch.build());

        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createL4SCTPMatch() {
        final MatchBuilder match = new MatchBuilder();

        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x0800L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        final IpMatchBuilder ipmatch = new IpMatchBuilder(); // ipv4 version
        ipmatch.setIpProtocol((short) 132);
        match.setIpMatch(ipmatch.build());

        final SctpMatchBuilder sctpmatch = new SctpMatchBuilder();
        final PortNumber srcport = new PortNumber(1435);
        final PortNumber dstport = new PortNumber(22);
        sctpmatch.setSctpSourcePort(srcport);
        sctpmatch.setSctpDestinationPort(dstport);
        match.setLayer4Match(sctpmatch.build());

        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createMetadataMatch() {
        final MatchBuilder match = new MatchBuilder();
        final byte[] metamask = new byte[]{(byte) -1, (byte) -1, (byte) -1, 0, 0, 0, (byte) 1, (byte) 1};
        final MetadataBuilder metadata = new MetadataBuilder(); // metadata match
        metadata.setMetadata(BigInteger.valueOf(500L));
        metadata.setMetadataMask(new BigInteger(1, metamask));
        match.setMetadata(metadata.build());

        return match;
    }

    /**
     * @return
     */
    private static MatchBuilder createMplsMatch() {
        final MatchBuilder match = new MatchBuilder();

        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x8847L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        final ProtocolMatchFieldsBuilder protomatch = new ProtocolMatchFieldsBuilder(); // mpls
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
        final MatchBuilder match = new MatchBuilder();

        final EthernetMatchBuilder eth = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x88E7L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());

        final ProtocolMatchFieldsBuilder protomatch = new ProtocolMatchFieldsBuilder(); // mpls
        // match
        protomatch.setPbb(new PbbBuilder().setPbbIsid(4L).setPbbMask(new BigInteger(new byte[]{0, 1, 0, 0}).longValue()).build());
        match.setProtocolMatchFields(protomatch.build());

        return match;

    }

    /**
     * @return
     */
    private static MatchBuilder createTunnelIDMatch() {
        final MatchBuilder match = new MatchBuilder();
        final TunnelBuilder tunnel = new TunnelBuilder(); // tunnel id match
        tunnel.setTunnelId(BigInteger.valueOf(10668));
        final byte[] mask = new byte[]{(byte) -1, (byte) -1, (byte) -1, 0, 0, 0, (byte) 1, (byte) 1};
        tunnel.setTunnelMask(new BigInteger(1, mask));
        match.setTunnel(tunnel.build());

        return match;
    }

    /**
     * Test match for TCP_Flags
     *
     * @return match containing Ethertype (0x0800), IP Protocol (TCP), TCP_Flag (SYN)
     */
    //FIXME: move to extensible support
    private static MatchBuilder createTcpFlagMatch() {
        final MatchBuilder match = new MatchBuilder();

        // Ethertype match
        final EthernetMatchBuilder ethernetType = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x0800L));
        ethernetType.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(ethernetType.build());

        // TCP Protocol Match
        final IpMatchBuilder ipMatch = new IpMatchBuilder(); // ipv4 version
        ipMatch.setIpProtocol((short) 6);
        match.setIpMatch(ipMatch.build());

        // TCP Port Match
        final PortNumber dstPort = new PortNumber(80);
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
        tcpFlagsMatch.setTcpFlags(0x002);
        match.setTcpFlagsMatch(tcpFlagsMatch.build());

        return match;
    }

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
        final InstanceIdentifier<Flow> path1 = InstanceIdentifier.create(Nodes.class).child(Node.class, tn.getKey())
                .augmentation(FlowCapableNode.class).child(Table.class, new TableKey(tf.getTableId()))
                .child(Flow.class, tf.getKey());
        modification.delete(LogicalDatastoreType.CONFIGURATION, path1);
        final CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        Futures.addCallback(commitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void aVoid) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error(throwable.getMessage(), throwable);
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        });
    }

    /**
     * @param ci arguments: switchId flowType tableNum
     *           <p>
     *           <pre>
     *                     e.g.: addMDFlow openflow:1 f1 42
     *                     </pre>
     */
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
                .child(Node.class, nodeBuilder.getKey()).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flow.getTableId())).child(Flow.class, flow.getKey());
        modification.merge(LogicalDatastoreType.CONFIGURATION, nodeBuilderToInstanceId(nodeBuilder), nodeBuilder.build(), true);
        modification.merge(LogicalDatastoreType.CONFIGURATION, path1, flow.build(), true);
        final CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        Futures.addCallback(commitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void aVoid) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error(throwable.getMessage(), throwable);
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        });
    }

    public void _modifyMDFlow(final CommandInterpreter ci) {
        final NodeBuilder tn = createTestNode(ci.nextArgument());
        final FlowBuilder tf = createTestFlow(tn, ci.nextArgument(), ci.nextArgument());
        tf.setFlowName(UPDATED_FLOW_NAME);
        writeFlow(ci, tf, tn);
        tf.setFlowName(ORIGINAL_FLOW_NAME);
        writeFlow(ci, tf, tn);
    }

    private static NodeRef createNodeRef(final String string) {
        final NodeKey key = new NodeKey(new NodeId(string));
        final InstanceIdentifier<Node> path = InstanceIdentifier.create(Nodes.class).child(Node.class, key);

        return new NodeRef(path);
    }

    @Override
    public String getHelp() {
        return "No help";
    }

    /*
     * usage testSwitchFlows <numberOfSwitches> <numberOfFlows> <warmup iterations> <Number Of Threads>
     * ex: _perfFlowTest 10 5 1 2
     */
    public void _perfFlowTest(final CommandInterpreter ci) {

        final String numberOfSwtichesStr = ci.nextArgument();
        final String numberOfFlowsStr = ci.nextArgument();
        final String warmupIterationsStr = ci.nextArgument();
        final String threadCountStr = ci.nextArgument();
        final String warmUpStr = ci.nextArgument();

        Collection<String> testResults = null;
        if (testResults == null) {
            testResults = new ArrayList<String>();
        }

        int numberOfSwtiches = 0;
        int numberOfFlows = 0;
        int warmupIterations = 0;
        boolean warmUpIterations = false;

        int threadCount = 0;
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

        if (threadCountStr != null && !threadCountStr.trim().equals("")) {
            threadCount = Integer.parseInt(threadCountStr);
        } else {
            threadCount = 2;
        }
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
        } catch (final Exception e) {
            ci.println("Exception:" + e.getMessage());
        }
    }

    public class TestFlowThread implements Runnable {

        int numberOfSwitches;
        int numberOfFlows;
        int testTime;
        CommandInterpreter ci;
        int testFlowsAdded;
        int theadNumber;
        Collection<String> testResults = null;
        int tableID = 0;

        TestFlowThread(final int numberOfSwtiches, final int numberOfFlows, final CommandInterpreter ci, final int t, final int tableID) {
            this.numberOfSwitches = numberOfSwtiches;
            this.numberOfFlows = numberOfFlows;
            this.ci = ci;
            this.theadNumber = t;
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

            ci.println("New Thread started with id:  ID_"
                    + this.theadNumber);
            int totalNumberOfFlows = 0;
            final long startTime = System.currentTimeMillis();

            for (int i = 1; i <= this.numberOfSwitches; i++) {
                dataPath = "openflow:" + i;
                tn = createTestNode(dataPath);
                for (int flow2 = 1; flow2 <= this.numberOfFlows; flow2++) {
                    tf = createTestFlowPerfTest("f1", "" + this.tableID, flow2);
                    writeFlow(this.ci, tf, tn);
                    totalNumberOfFlows++;
                }
            }
            final long endTime = System.currentTimeMillis();
            final long timeInSeconds = Math.round((endTime - startTime) / 1000);
            if (timeInSeconds > 0) {
                ci.println("Total flows added in Thread:" + this.theadNumber + ": Flows/Sec::" + Math.round(totalNumberOfFlows / timeInSeconds));
            } else {
                ci.println("Total flows added in Thread:" + this.theadNumber + ": Flows/Sec::" + totalNumberOfFlows);
            }
        }

    }

    /*
     * usage testAllFlows <dp>
     * ex: _perfFlowTest 1
     */
    public void _testAllFlows(final CommandInterpreter ci) {
        String dataPathID = ci.nextArgument();
        final int numberOfFlows = 82;
        if (dataPathID == null || dataPathID.trim().equals("")) {
            dataPathID = "1";
        }
        ci.println("*     Test All Flows	*");
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
            } catch (final Exception e) {
                ci.println("--Test Failed--Issue found while adding flow" + flow);
                break;
            }
        }
    }
}
