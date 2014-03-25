/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptest

import java.util.ArrayList
import java.util.Arrays
import org.apache.commons.codec.binary.Hex
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropActionBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress
import org.slf4j.LoggerFactory
import java.math.BigInteger
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags

class DropTestCommiter implements PacketProcessingListener {
    static val LOG = LoggerFactory.getLogger(DropTestProvider);
    @Property
    val DropTestProvider manager;

    new(DropTestProvider manager) {
        _manager = manager;
    }
    
    override onPacketReceived(PacketReceived notification) {
        LOG.debug("onPacketReceived - Entering - " + notification);
        val ncr = notification.ingress // Get the Ingress nodeConnectorRef
        val ncri = (ncr.value as InstanceIdentifier<NodeConnector>); // Get the instance identifier for the nodeConnectorRef
        val ncKey = InstanceIdentifier.keyOf(ncri);
        val nodeInstanceId = ncri.firstIdentifierOf(Node); // Get the instanceID for the Node in the tree above us
        val nodeKey = InstanceIdentifier.keyOf(nodeInstanceId);
        val rawPacket = notification.payload;
        LOG.debug("onPacketReceived - received Packet on Node {} and NodeConnector {} payload {}",nodeKey.id,ncKey.id,Hex.encodeHexString(rawPacket));
        val srcMac = Arrays.copyOfRange(rawPacket,6,12);
        LOG.debug("onPacketReceived - received Packet on Node {} and NodeConnector {} srcMac {}",nodeKey.id,ncKey.id,Hex.encodeHexString(srcMac));
        
        val match = new MatchBuilder();
        val ethernetMatch = new EthernetMatchBuilder();
        val ethSourceBuilder = new EthernetSourceBuilder();
        //TODO: use HEX, use binary form
        //Hex.decodeHex("000000000001".toCharArray());
        ethSourceBuilder.setAddress(new MacAddress(DropTestUtils.macToString(srcMac)));
        ethernetMatch.setEthernetSource(ethSourceBuilder.build());
        match.setEthernetMatch(ethernetMatch.build());
        val dab = new DropActionBuilder();
        val dropAction = dab.build();
        val ab = new ActionBuilder();
        ab.setOrder(0);
        ab.setAction(new DropActionCaseBuilder().setDropAction(dropAction).build());
        
        // Add our drop action to a list
        val actionList = new ArrayList<Action>();
        actionList.add(ab.build());
        
        // Create an Apply Action
        val aab = new ApplyActionsBuilder();
        aab.setAction(actionList);
        
        // Wrap our Apply Action in an Instruction
        val ib = new InstructionBuilder();
        ib.setOrder(0);
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        
        // Put our Instruction in a list of Instructions
        val isb = new InstructionsBuilder();
        val instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        
        // Finally build our flow
        val fb = new FlowBuilder();
        fb.setMatch(match.build());
        fb.setInstructions(isb.build());
        fb.setId(new FlowId(Long.toString(fb.hashCode)));
        fb.setPriority(4);
        fb.setBufferId(0L);
        val value = new BigInteger("10", 10);
        fb.setCookie(value);
        fb.setCookieMask(value);
        fb.setTableId(0 as short);
        fb.setHardTimeout(300);
        fb.setIdleTimeout(240);
        fb.setFlags(new FlowModFlags(false, false, false, false, false));
        
        // Construct the flow instance id
        val flowInstanceId = InstanceIdentifier.builder(Nodes) // File under nodes
            .child(Node,nodeKey) // A particular node indentified by nodeKey
            .augmentation(FlowCapableNode) // That is flow capable, only FlowCapableNodes have tables
            .child(Table,new TableKey(new Short((0 as short)))) // In the table identified by TableKey
            .child(Flow,new FlowKey(fb.id)) // A flow identified by flowKey
            .build();
        val flow = fb.build()
        val transaction = _manager.dataService.beginTransaction
        LOG.debug("onPacketReceived - About to write flow - " + flow);
        transaction.putConfigurationData(flowInstanceId,flow);
        transaction.commit;
        LOG.debug("onPacketReceived - About to write flow commited");
    }
    
}
