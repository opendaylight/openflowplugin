/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptest;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.opendaylight.openflowplugin.droptest.DropTestProvider;
import org.opendaylight.openflowplugin.droptest.DropTestRpcProvider;
import org.opendaylight.openflowplugin.droptest.DropTestUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("all")
public class DropTestRpcSender implements PacketProcessingListener {
    private final static Logger LOG = new Function0<Logger>() {
        public Logger apply() {
            Logger _logger = LoggerFactory.getLogger(DropTestProvider.class);
            return _logger;
        }
    }.apply();

    private final DropTestRpcProvider _manager;

    public DropTestRpcProvider getManager() {
        return this._manager;
    }

    private final SalFlowService _flowService;

    public SalFlowService getFlowService() {
        return this._flowService;
    }

    public DropTestRpcSender(final DropTestRpcProvider manager, final SalFlowService flowService) {
        this._manager = manager;
        this._flowService = flowService;
    }

    public void onPacketReceived(final PacketReceived notification) {
        String _plus = ("onPacketReceived - Entering - " + notification);
        DropTestRpcSender.LOG.debug(_plus);

        final NodeConnectorRef ncr = notification.getIngress();
        InstanceIdentifier<? extends Object> _value = ncr.getValue();
        final InstanceIdentifier<NodeConnector> ncri = ((InstanceIdentifier<NodeConnector>) _value);
        final NodeConnectorKey ncKey = InstanceIdentifier.<NodeConnector, NodeConnectorKey>keyOf(ncri);
        final InstanceIdentifier<Node> nodeInstanceId = ncri.<Node>firstIdentifierOf(Node.class);
        final NodeKey nodeKey = InstanceIdentifier.<Node, NodeKey>keyOf(nodeInstanceId);
        final byte[] rawPacket = notification.getPayload();
        NodeId _id = nodeKey.getId();
        NodeConnectorId _id_1 = ncKey.getId();
        String _encodeHexString = Hex.encodeHexString(rawPacket);
        DropTestRpcSender.LOG.debug("onPacketReceived - received Packet on Node {} and NodeConnector {} payload {}", _id, _id_1, _encodeHexString);
        final byte[] srcMac = Arrays.copyOfRange(rawPacket, 6, 12);
        NodeId _id_2 = nodeKey.getId();
        NodeConnectorId _id_3 = ncKey.getId();
        String _encodeHexString_1 = Hex.encodeHexString(srcMac);
        DropTestRpcSender.LOG.debug("onPacketReceived - received Packet on Node {} and NodeConnector {} srcMac {}", _id_2, _id_3, _encodeHexString_1);

        MatchBuilder _matchBuilder = new MatchBuilder();
        final MatchBuilder match = _matchBuilder;

        EthernetMatchBuilder _ethernetMatchBuilder = new EthernetMatchBuilder();
        final EthernetMatchBuilder ethernetMatch = _ethernetMatchBuilder;

        EthernetSourceBuilder _ethernetSourceBuilder = new EthernetSourceBuilder();
        final EthernetSourceBuilder ethSourceBuilder = _ethernetSourceBuilder;

        String _macToString = DropTestUtils.macToString(srcMac);
        MacAddress _macAddress = new MacAddress(_macToString);
        ethSourceBuilder.setAddress(_macAddress);
        EthernetSource _build = ethSourceBuilder.build();
        ethernetMatch.setEthernetSource(_build);
        EthernetMatch _build_1 = ethernetMatch.build();
        match.setEthernetMatch(_build_1);

        DropActionBuilder _dropActionBuilder = new DropActionBuilder();
        final DropActionBuilder dab = _dropActionBuilder;
        final DropAction dropAction = dab.build();
        ActionBuilder _actionBuilder = new ActionBuilder();
        final ActionBuilder ab = _actionBuilder;
        DropActionCaseBuilder _dropActionCaseBuilder = new DropActionCaseBuilder();
        DropActionCaseBuilder _setDropAction = _dropActionCaseBuilder.setDropAction(dropAction);
        DropActionCase _build_2 = _setDropAction.build();
        ab.setAction(_build_2);
        ArrayList<Action> _arrayList = new ArrayList<Action>();
        final ArrayList<Action> actionList = _arrayList;
        Action _build_3 = ab.build();
        actionList.add(_build_3);
        ApplyActionsBuilder _applyActionsBuilder = new ApplyActionsBuilder();
        final ApplyActionsBuilder aab = _applyActionsBuilder;
        aab.setAction(actionList);
        InstructionBuilder _instructionBuilder = new InstructionBuilder();
        final InstructionBuilder ib = _instructionBuilder;
        ApplyActionsCaseBuilder _applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
        ApplyActions _build_4 = aab.build();
        ApplyActionsCaseBuilder _setApplyActions = _applyActionsCaseBuilder.setApplyActions(_build_4);
        ApplyActionsCase _build_5 = _setApplyActions.build();
        ib.setInstruction(_build_5);
        InstructionsBuilder _instructionsBuilder = new InstructionsBuilder();
        final InstructionsBuilder isb = _instructionsBuilder;
        ArrayList<Instruction> _arrayList_1 = new ArrayList<Instruction>();
        final ArrayList<Instruction> instructions = _arrayList_1;
        Instruction _build_6 = ib.build();
        instructions.add(_build_6);
        isb.setInstruction(instructions);
        AddFlowInputBuilder _addFlowInputBuilder = new AddFlowInputBuilder();
        final AddFlowInputBuilder fb = _addFlowInputBuilder;
        Match _build_7 = match.build();
        fb.setMatch(_build_7);
        Instructions _build_8 = isb.build();
        fb.setInstructions(_build_8);
        InstanceIdentifierBuilder<Nodes> _builder = InstanceIdentifier.<Nodes>builder(Nodes.class);
        InstanceIdentifierBuilder<Node> _child = _builder.<Node, NodeKey>child(Node.class, nodeKey);
        final InstanceIdentifier<Node> flowInstanceId = _child.toInstance();
        NodeRef _nodeRef = new NodeRef(flowInstanceId);
        fb.setNode(_nodeRef);
        fb.setPriority(Integer.valueOf(4));
        fb.setBufferId(Long.valueOf(0L));
        BigInteger _bigInteger = new BigInteger("10", 10);
        final BigInteger value = _bigInteger;
        fb.setCookie(value);
        fb.setCookieMask(value);
        fb.setTableId(Short.valueOf(((short) 0)));
        fb.setHardTimeout(Integer.valueOf(300));
        fb.setIdleTimeout(Integer.valueOf(240));
        FlowModFlags _flowModFlags = new FlowModFlags(Boolean.valueOf(false), Boolean.valueOf(false), Boolean.valueOf(false), Boolean.valueOf(false), Boolean.valueOf(false));
        fb.setFlags(_flowModFlags);
        SalFlowService _flowService = this.getFlowService();
        AddFlowInput _build_9 = fb.build();
        _flowService.addFlow(_build_9);
    }
}
