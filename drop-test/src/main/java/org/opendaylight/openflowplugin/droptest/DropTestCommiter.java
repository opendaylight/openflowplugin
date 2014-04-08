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
import org.opendaylight.controller.sal.binding.api.data.DataModificationTransaction;
import org.opendaylight.controller.sal.binding.api.data.DataProviderService;
import org.opendaylight.openflowplugin.droptest.DropTestProvider;
import org.opendaylight.openflowplugin.droptest.DropTestUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
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
public class DropTestCommiter implements PacketProcessingListener {
  private final static Logger LOG = new Function0<Logger>() {
    public Logger apply() {
      Logger _logger = LoggerFactory.getLogger(DropTestProvider.class);
      return _logger;
    }
  }.apply();
  
  private final DropTestProvider _manager;
  
  public DropTestProvider getManager() {
    return this._manager;
  }
  
  public DropTestCommiter(final DropTestProvider manager) {
    this._manager = manager;
  }
  
  public void onPacketReceived(final PacketReceived notification) {
    String _plus = ("onPacketReceived - Entering - " + notification);
    DropTestCommiter.LOG.debug(_plus);
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
    DropTestCommiter.LOG.debug("onPacketReceived - received Packet on Node {} and NodeConnector {} payload {}", _id, _id_1, _encodeHexString);
    final byte[] srcMac = Arrays.copyOfRange(rawPacket, 6, 12);
    NodeId _id_2 = nodeKey.getId();
    NodeConnectorId _id_3 = ncKey.getId();
    String _encodeHexString_1 = Hex.encodeHexString(srcMac);
    DropTestCommiter.LOG.debug("onPacketReceived - received Packet on Node {} and NodeConnector {} srcMac {}", _id_2, _id_3, _encodeHexString_1);
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
    ab.setOrder(Integer.valueOf(0));
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
    ib.setOrder(Integer.valueOf(0));
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
    FlowBuilder _flowBuilder = new FlowBuilder();
    final FlowBuilder fb = _flowBuilder;
    Match _build_7 = match.build();
    fb.setMatch(_build_7);
    Instructions _build_8 = isb.build();
    fb.setInstructions(_build_8);
    int _hashCode = fb.hashCode();
    String _string = Long.toString(_hashCode);
    FlowId _flowId = new FlowId(_string);
    fb.setId(_flowId);
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
    InstanceIdentifierBuilder<Nodes> _builder = InstanceIdentifier.<Nodes>builder(Nodes.class);
    InstanceIdentifierBuilder<Node> _child = _builder.<Node, NodeKey>child(Node.class, nodeKey);
    InstanceIdentifierBuilder<FlowCapableNode> _augmentation = _child.<FlowCapableNode>augmentation(FlowCapableNode.class);
    Short _short = new Short(((short) 0));
    TableKey _tableKey = new TableKey(_short);
    InstanceIdentifierBuilder<Table> _child_1 = _augmentation.<Table, TableKey>child(Table.class, _tableKey);
    FlowId _id_4 = fb.getId();
    FlowKey _flowKey = new FlowKey(_id_4);
    InstanceIdentifierBuilder<Flow> _child_2 = _child_1.<Flow, FlowKey>child(Flow.class, _flowKey);
    final InstanceIdentifier<Flow> flowInstanceId = _child_2.build();
    final Flow flow = fb.build();
    DataProviderService _dataService = this._manager.getDataService();
    final DataModificationTransaction transaction = _dataService.beginTransaction();
    String _plus_1 = ("onPacketReceived - About to write flow - " + flow);
    DropTestCommiter.LOG.debug(_plus_1);
    transaction.putConfigurationData(flowInstanceId, flow);
    transaction.commit();
    DropTestCommiter.LOG.debug("onPacketReceived - About to write flow commited");
  }
}
