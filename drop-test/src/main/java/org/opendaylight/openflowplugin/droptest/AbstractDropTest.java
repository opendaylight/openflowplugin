/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractDropTest implements PacketProcessingListener {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDropTest.class);

    private static final AtomicIntegerFieldUpdater<AbstractDropTest> SENT_UPDATER = AtomicIntegerFieldUpdater.newUpdater(AbstractDropTest.class, "_sent");
    private volatile int _sent;

    private static final AtomicIntegerFieldUpdater<AbstractDropTest> RCVD_UPDATER = AtomicIntegerFieldUpdater.newUpdater(AbstractDropTest.class, "_rcvd");
    private volatile int _rcvd;

    private static final AtomicIntegerFieldUpdater<AbstractDropTest> EXCS_UPDATER = AtomicIntegerFieldUpdater.newUpdater(AbstractDropTest.class, "_excs");
    private volatile int _excs;

    public final DropTestStats getStats() {
        return new DropTestStats(this._sent, this._rcvd, this._excs);
    }

    public final void clearStats(){
        this._sent = 0;
        this._rcvd = 0;
        this._excs = 0;
    }

    @Override
    public final void onPacketReceived(final PacketReceived notification) {
        LOG.debug("onPacketReceived - Entering - {}", notification);

        RCVD_UPDATER.incrementAndGet(this);

        try {
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
            ab.setOrder(0);
            ab.setAction(new DropActionCaseBuilder().setDropAction(dropAction).build());

            // Add our drop action to a list
            final List<Action> actionList = Collections.singletonList(ab.build());

            // Create an Apply Action
            final ApplyActionsBuilder aab = new ApplyActionsBuilder();
            aab.setAction(actionList);

            // Wrap our Apply Action in an Instruction
            final InstructionBuilder ib = new InstructionBuilder();
            ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build()).setOrder(0);

            // Put our Instruction in a list of Instructions
            final InstructionsBuilder isb = new InstructionsBuilder();
            final List<Instruction> instructions = Collections.singletonList(ib.build());
            isb.setInstruction(instructions);

            // Get the Ingress nodeConnectorRef
            final NodeConnectorRef ncr = notification.getIngress();

            // Get the instance identifier for the nodeConnectorRef
            final InstanceIdentifier<?> ncri = ncr.getValue();

            // Get the instanceID for the Node in the tree above us
            final NodeKey node = ncri.firstKeyOf(Node.class, NodeKey.class);

            processPacket(node, match.build(), isb.build());
            SENT_UPDATER.incrementAndGet(this);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            LOG.error("Failed to process packet", e);
            EXCS_UPDATER.incrementAndGet(this);
        }

        LOG.debug("onPacketReceived - Leaving", notification);
    }

    protected abstract void processPacket(NodeKey node, Match match, Instructions instructions);
}
