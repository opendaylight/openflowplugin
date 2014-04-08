/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.outputtest;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.lang.ArrayUtils;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.opendaylight.controller.sal.binding.api.data.DataModificationTransaction;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;

@SuppressWarnings("all")
public class OutputTestUtil {
  private OutputTestUtil() {
    UnsupportedOperationException _unsupportedOperationException = new UnsupportedOperationException("Utility class. Instantiation is not allowed.");
    throw _unsupportedOperationException;
  }
  
  public static TransmitPacketInput buildTransmitInputPacket(final String nodeId, final byte[] packValue, final String outPort, final String inPort) {
    ArrayList<Byte> _arrayList = new ArrayList<Byte>(40);
    ArrayList<Byte> list = _arrayList;
    String _string = new String("sendOutputMsg_TEST");
    byte[] msg = _string.getBytes();
    int index = 0;
    for (final byte b : msg) {
      {
        list.add(Byte.valueOf(b));
        boolean _lessThan = (index < 7);
        if (_lessThan) {
          int _plus = (index + 1);
          index = _plus;
        } else {
          index = 0;
        }
      }
    }
    boolean _lessThan = (index < 8);
    boolean _while = _lessThan;
    while (_while) {
      {
        Byte _byte = new Byte("0");
        list.add(_byte);
        int _plus = (index + 1);
        index = _plus;
      }
      boolean _lessThan_1 = (index < 8);
      _while = _lessThan_1;
    }
    NodeRef ref = OutputTestUtil.createNodeRef(nodeId);
    NodeConnectorRef _createNodeConnRef = OutputTestUtil.createNodeConnRef(nodeId, outPort);
    NodeConnectorRef _nodeConnectorRef = new NodeConnectorRef(_createNodeConnRef);
    NodeConnectorRef nEgressConfRef = _nodeConnectorRef;
    NodeConnectorRef _createNodeConnRef_1 = OutputTestUtil.createNodeConnRef(nodeId, inPort);
    NodeConnectorRef _nodeConnectorRef_1 = new NodeConnectorRef(_createNodeConnRef_1);
    NodeConnectorRef nIngressConRef = _nodeConnectorRef_1;
    TransmitPacketInputBuilder _transmitPacketInputBuilder = new TransmitPacketInputBuilder();
    TransmitPacketInputBuilder tPackBuilder = _transmitPacketInputBuilder;
    final ArrayList<Byte> _converted_list = (ArrayList<Byte>)list;
    byte[] _primitive = ArrayUtils.toPrimitive(((Byte[])Conversions.unwrapArray(_converted_list, Byte.class)));
    tPackBuilder.setPayload(_primitive);
    tPackBuilder.setNode(ref);
    tPackBuilder.setCookie(null);
    tPackBuilder.setEgress(nEgressConfRef);
    tPackBuilder.setIngress(nIngressConRef);
    return tPackBuilder.build();
  }
  
  public static String makePingFlowForNode(final String nodeId, final ProviderContext pc) {
    NodeBuilder nodeBuilder = OutputTestUtil.createNodeBuilder(nodeId);
    FlowBuilder flowBuilder = OutputTestUtil.createFlowBuilder(1235, null, "ping");
    DataBrokerService dataBrokerService = pc.<DataBrokerService>getSALService(DataBrokerService.class);
    DataModificationTransaction modif = dataBrokerService.beginTransaction();
    InstanceIdentifierBuilder<Nodes> _builder = InstanceIdentifier.<Nodes>builder(Nodes.class);
    NodeKey _key = nodeBuilder.getKey();
    InstanceIdentifierBuilder<Node> _child = _builder.<Node, NodeKey>child(Node.class, _key);
    InstanceIdentifierBuilder<FlowCapableNode> _augmentation = _child.<FlowCapableNode>augmentation(FlowCapableNode.class);
    Short _tableId = flowBuilder.getTableId();
    TableKey _tableKey = new TableKey(_tableId);
    InstanceIdentifierBuilder<Table> _child_1 = _augmentation.<Table, TableKey>child(Table.class, _tableKey);
    FlowKey _key_1 = flowBuilder.getKey();
    InstanceIdentifierBuilder<Flow> _child_2 = _child_1.<Flow, FlowKey>child(Flow.class, _key_1);
    InstanceIdentifier<Flow> path = _child_2.build();
    Flow _build = flowBuilder.build();
    modif.putConfigurationData(path, _build);
    Future<RpcResult<TransactionStatus>> commitFuture = modif.commit();
    try {
      RpcResult<TransactionStatus> resutl = commitFuture.get();
      TransactionStatus status = resutl.getResult();
      return ("Status of Flow Data Loaded Transaction: " + status);
    } catch (final Throwable _t) {
      if (_t instanceof InterruptedException) {
        final InterruptedException e = (InterruptedException)_t;
        e.printStackTrace();
        Class<? extends InterruptedException> _class = e.getClass();
        return _class.getName();
      } else if (_t instanceof ExecutionException) {
        final ExecutionException e_1 = (ExecutionException)_t;
        e_1.printStackTrace();
        Class<? extends ExecutionException> _class_1 = e_1.getClass();
        return _class_1.getName();
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
  
  public static NodeRef createNodeRef(final String nodeId) {
    NodeId _nodeId = new NodeId(nodeId);
    NodeKey _nodeKey = new NodeKey(_nodeId);
    NodeKey key = _nodeKey;
    InstanceIdentifierBuilder<Nodes> _builder = InstanceIdentifier.<Nodes>builder(Nodes.class);
    InstanceIdentifierBuilder<Node> _child = _builder.<Node, NodeKey>child(Node.class, key);
    InstanceIdentifier<Node> path = _child.toInstance();
    NodeRef _nodeRef = new NodeRef(path);
    return _nodeRef;
  }
  
  public static NodeConnectorRef createNodeConnRef(final String nodeId, final String port) {
    StringBuilder _stringBuilder = new StringBuilder(nodeId);
    StringBuilder _append = _stringBuilder.append(":");
    StringBuilder sBuild = _append.append(port);
    String _string = sBuild.toString();
    NodeConnectorId _nodeConnectorId = new NodeConnectorId(_string);
    NodeConnectorKey _nodeConnectorKey = new NodeConnectorKey(_nodeConnectorId);
    NodeConnectorKey nConKey = _nodeConnectorKey;
    InstanceIdentifierBuilder<Nodes> _builder = InstanceIdentifier.<Nodes>builder(Nodes.class);
    NodeId _nodeId = new NodeId(nodeId);
    NodeKey _nodeKey = new NodeKey(_nodeId);
    InstanceIdentifierBuilder<Node> _child = _builder.<Node, NodeKey>child(Node.class, _nodeKey);
    InstanceIdentifierBuilder<NodeConnector> _child_1 = _child.<NodeConnector, NodeConnectorKey>child(NodeConnector.class, nConKey);
    InstanceIdentifier<NodeConnector> path = _child_1.toInstance();
    NodeConnectorRef _nodeConnectorRef = new NodeConnectorRef(path);
    return _nodeConnectorRef;
  }
  
  private static NodeBuilder createNodeBuilder(final String nodeId) {
    NodeBuilder _nodeBuilder = new NodeBuilder();
    NodeBuilder builder = _nodeBuilder;
    NodeId _nodeId = new NodeId(nodeId);
    builder.setId(_nodeId);
    NodeId _id = builder.getId();
    NodeKey _nodeKey = new NodeKey(_id);
    builder.setKey(_nodeKey);
    return builder;
  }
  
  private static FlowBuilder createFlowBuilder(final long flowId, final String tableId, final String flowName) {
    FlowBuilder _flowBuilder = new FlowBuilder();
    FlowBuilder fBuild = _flowBuilder;
    MatchBuilder _matchBuilder = new MatchBuilder();
    Match _build = _matchBuilder.build();
    fBuild.setMatch(_build);
    InstructionsBuilder _createPingInstructionsBuilder = OutputTestUtil.createPingInstructionsBuilder();
    Instructions _build_1 = _createPingInstructionsBuilder.build();
    fBuild.setInstructions(_build_1);
    String _string = Long.toString(flowId);
    FlowId _flowId = new FlowId(_string);
    FlowKey _flowKey = new FlowKey(_flowId);
    FlowKey key = _flowKey;
    fBuild.setBarrier(Boolean.valueOf(false));
    BigInteger _bigInteger = new BigInteger("10", 10);
    BigInteger value = _bigInteger;
    fBuild.setCookie(value);
    fBuild.setCookieMask(value);
    fBuild.setHardTimeout(Integer.valueOf(0));
    fBuild.setIdleTimeout(Integer.valueOf(0));
    fBuild.setInstallHw(Boolean.valueOf(false));
    fBuild.setStrict(Boolean.valueOf(false));
    fBuild.setContainerName(null);
    FlowModFlags _flowModFlags = new FlowModFlags(Boolean.valueOf(false), Boolean.valueOf(false), Boolean.valueOf(false), Boolean.valueOf(false), Boolean.valueOf(false));
    fBuild.setFlags(_flowModFlags);
    FlowId _flowId_1 = new FlowId("12");
    fBuild.setId(_flowId_1);
    short _checkTableId = OutputTestUtil.checkTableId(tableId);
    fBuild.setTableId(Short.valueOf(_checkTableId));
    Long _long = new Long(2);
    fBuild.setOutGroup(_long);
    fBuild.setOutPort(value);
    fBuild.setKey(key);
    fBuild.setPriority(Integer.valueOf(2));
    fBuild.setFlowName(flowName);
    return fBuild;
  }
  
  private static InstructionsBuilder createPingInstructionsBuilder() {
    ArrayList<Action> _arrayList = new ArrayList<Action>();
    ArrayList<Action> aList = _arrayList;
    ActionBuilder _actionBuilder = new ActionBuilder();
    ActionBuilder aBuild = _actionBuilder;
    OutputActionBuilder _outputActionBuilder = new OutputActionBuilder();
    OutputActionBuilder output = _outputActionBuilder;
    output.setMaxLength(Integer.valueOf(56));
    Uri _uri = new Uri("CONTROLLER");
    output.setOutputNodeConnector(_uri);
    OutputActionCaseBuilder _outputActionCaseBuilder = new OutputActionCaseBuilder();
    OutputAction _build = output.build();
    OutputActionCaseBuilder _setOutputAction = _outputActionCaseBuilder.setOutputAction(_build);
    OutputActionCase _build_1 = _setOutputAction.build();
    aBuild.setAction(_build_1);
    aBuild.setOrder(Integer.valueOf(0));
    ActionKey _actionKey = new ActionKey(Integer.valueOf(0));
    aBuild.setKey(_actionKey);
    Action _build_2 = aBuild.build();
    aList.add(_build_2);
    ApplyActionsBuilder _applyActionsBuilder = new ApplyActionsBuilder();
    ApplyActionsBuilder asBuild = _applyActionsBuilder;
    asBuild.setAction(aList);
    InstructionBuilder _instructionBuilder = new InstructionBuilder();
    InstructionBuilder iBuild = _instructionBuilder;
    ApplyActionsCaseBuilder _applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
    ApplyActions _build_3 = asBuild.build();
    ApplyActionsCaseBuilder _setApplyActions = _applyActionsCaseBuilder.setApplyActions(_build_3);
    ApplyActionsCase _build_4 = _setApplyActions.build();
    iBuild.setInstruction(_build_4);
    iBuild.setOrder(Integer.valueOf(0));
    InstructionKey _instructionKey = new InstructionKey(Integer.valueOf(0));
    iBuild.setKey(_instructionKey);
    ArrayList<Instruction> _arrayList_1 = new ArrayList<Instruction>();
    ArrayList<Instruction> instr = _arrayList_1;
    Instruction _build_5 = iBuild.build();
    instr.add(_build_5);
    InstructionsBuilder _instructionsBuilder = new InstructionsBuilder();
    return _instructionsBuilder.setInstruction(instr);
  }
  
  private static short checkTableId(final String tableId) {
    try {
      return Short.parseShort(tableId);
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception ex = (Exception)_t;
        return Short.parseShort("2");
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
}
