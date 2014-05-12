/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptest;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("all")
public class DropTestRpcSender implements PacketProcessingListener {
    private final static Logger LOG = LoggerFactory.getLogger(DropTestProvider.class);

    private final DropTestRpcProvider _manager;
    private final SalFlowService _flowService;
    
    private int _sent;
    private int _rcvd;
    private int _excs;
    
    public DropTestStats getStats(){
    	return new DropTestStats(this._sent, this._rcvd, this._excs);
    }
    
    public void clearStats(){
    	this._sent = 0;
    	this._rcvd = 0;
    	this._excs = 0;
   }

    public DropTestRpcProvider getManager() {
        return this._manager;
    }

    public SalFlowService getFlowService() {
        return this._flowService;
    }

    public DropTestRpcSender(final DropTestRpcProvider manager, final SalFlowService flowService) {
        this._manager = manager;
        this._flowService = flowService;
    }

    @Override
	public void onPacketReceived(final PacketReceived notification) {
        // LOG.debug("onPacketReceived - Entering - " + notification);

    	synchronized(this) {
    		this._rcvd++;
    	}
    	
    	try {
			// Get the Ingress nodeConnectorRef
			final NodeConnectorRef ncr = notification.getIngress();

			// Get the instance identifier for the nodeConnectorRef
			final InstanceIdentifier<NodeConnector> ncri = (InstanceIdentifier<NodeConnector>) ncr.getValue();
			final NodeConnectorKey ncKey = InstanceIdentifier.<NodeConnector, NodeConnectorKey>keyOf(ncri);

			// Get the instanceID for the Node in the tree above us
			final InstanceIdentifier<Node> nodeInstanceId = ncri.<Node>firstIdentifierOf(Node.class);
			final NodeKey nodeKey = InstanceIdentifier.<Node, NodeKey>keyOf(nodeInstanceId);
			final byte[] rawPacket = notification.getPayload();

			// LOG.debug("onPacketReceived - received Packet on Node {} and NodeConnector {} payload {}",
			//        nodeKey.getId(), ncKey.getId(), Hex.encodeHexString(rawPacket));

			final byte[] srcMac = Arrays.copyOfRange(rawPacket, 6, 12);

			//LOG.debug("onPacketReceived - received Packet on Node {} and NodeConnector {} srcMac {}",
			//        nodeKey.getId(), ncKey.getId(), Hex.encodeHexString(srcMac));


			final MatchBuilder match = new MatchBuilder();
			final EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
			final EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();

			//TODO: use HEX, use binary form
			//Hex.decodeHex("000000000001".toCharArray());

			ethSourceBuilder.setAddress(new MacAddress(DropTestUtils.macToString(srcMac)));
			ethernetMatch.setEthernetSource(ethSourceBuilder.build());
			match.setEthernetMatch(ethernetMatch.build());
			final DropActionBuilder dab = new DropActionBuilder();
			final DropAction dropAction = dab.build();
			final ActionBuilder ab = new ActionBuilder();
			ab.setAction(new DropActionCaseBuilder().setDropAction(dropAction).build());

			// Add our drop action to a list
			final ArrayList<Action> actionList = new ArrayList<Action>();
			actionList.add(ab.build());

			// Create an Apply Action
			final ApplyActionsBuilder aab = new ApplyActionsBuilder();
			aab.setAction(actionList);

			// Wrap our Apply Action in an Instruction
			final InstructionBuilder ib = new InstructionBuilder();
			ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

			// Put our Instruction in a list of Instructions
			final InstructionsBuilder isb = new InstructionsBuilder();;
			final ArrayList<Instruction> instructions = new ArrayList<Instruction>();
			instructions.add(ib.build());
			isb.setInstruction(instructions);

			// Finally build our flow
			final AddFlowInputBuilder fb = new AddFlowInputBuilder();
			fb.setMatch(match.build());
			fb.setInstructions(isb.build());
			//fb.setId(new FlowId(Long.toString(fb.hashCode)));

			// Construct the flow instance id
			final InstanceIdentifier<Node> flowInstanceId = InstanceIdentifier
					.builder(Nodes.class) // File under nodes
					.child(Node.class, nodeKey).toInstance(); // A particular node identified by nodeKey
			fb.setNode(new NodeRef(flowInstanceId));

			fb.setPriority(4);
			fb.setBufferId(0L);
			final BigInteger value = new BigInteger("10", 10);
			fb.setCookie(new FlowCookie(value));
			fb.setCookieMask(new FlowCookie(value));
			fb.setTableId(Short.valueOf(((short) 0)));
			fb.setHardTimeout(300);
			fb.setIdleTimeout(240);
			fb.setFlags(new FlowModFlags(false, false, false, false, false));

			// Add flow
			this.getFlowService().addFlow(fb.build());

			synchronized (this) {
				this._sent++;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("dropTestRpc exception: {}", e.toString());
			synchronized (this) {
				this._excs++;
			}
		}
    }
}
