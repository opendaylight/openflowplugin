/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.supplier.of.link.impl;

import org.opendaylight.of.controller.InitialFlowContributor;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.instr.*;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.net.EthernetType;

import java.util.ArrayList;
import java.util.List;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.of.lib.instr.InstructionFactory.createMutableInstruction;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.MatchFactory.createMatch;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.ETH_TYPE;
import static org.opendaylight.of.lib.msg.MessageFactory.create;

/**
 * Encapsulates the OpenFlow-related details of stealing BDDP packets to the
 * controller.
 *
 * @author Shaun Wackerly
 */
public class BddpPacketStealer implements InitialFlowContributor {

    // Our own recognizable cookie for stealing HSDP packets.
    private static final long COOKIE = 0x00000000faded000L;

    private static final int BDDP_STEAL_PRIORITY = 60000;


    private final Action action10;
    private final Instruction instruct13;
    private final Match match10;
    private final Match match13;

    /**
     * Constructs a BDDP packet stealer that will steal BDDP packets to the
     * controller.
     */
    public BddpPacketStealer() {

        // Initialize cached values per OF version that will never change
        ProtocolVersion pv = V_1_0;
        match10 = (Match) createMatch(pv)
                .addField(createBasicField(pv, ETH_TYPE, EthernetType.BDDP))
                .toImmutable(); 
        action10 = createAction(pv,
                ActionType.OUTPUT, Port.CONTROLLER,
                ActOutput.CONTROLLER_NO_BUFFER);

        pv = V_1_3;
        match13 = (Match) createMatch(pv)
                .addField(createBasicField(pv, ETH_TYPE, EthernetType.BDDP))
                .toImmutable();
        Action action13 = createAction(pv,
                ActionType.OUTPUT, Port.CONTROLLER,
                ActOutput.CONTROLLER_NO_BUFFER);
        instruct13 = (Instruction)
                createMutableInstruction(pv, InstructionType.APPLY_ACTIONS)
                .addAction(action13).toImmutable();
    }

    @Override
    public List<OfmFlowMod> provideInitialFlows(DataPathInfo info,
                                                boolean isHybrid) {
        List<OfmFlowMod> result = new ArrayList<>(1);
        if (isHybrid) 
            result.add(buildFlowMod(info));
        return result;
    }

    /**
     * Build a flow modification message that will match all BDDP packets
     * and redirect (steal) them to the controller.
     * 
     * @param dpi datapath info
     * @return the flow mod
     * @throws IllegalArgumentException if protocol version not supported
     */
    private OfmFlowMod buildFlowMod(DataPathInfo dpi) {
        ProtocolVersion pv = dpi.negotiated();

        // Create a new flow add message
        OfmMutableFlowMod flow = (OfmMutableFlowMod)
                create(pv, MessageType.FLOW_MOD, FlowModCommand.ADD);

        flow.bufferId(BufferId.NO_BUFFER)
                .priority(BDDP_STEAL_PRIORITY)
                .cookie(COOKIE);

        // Choose cached values based upon protocol version
        if (pv == V_1_0)
            flow = flow.match(match10).addAction(action10);
        else if (pv == V_1_3)
            flow = flow.match(match13).addInstruction(instruct13);

        // No flow.idleTimeout specified. We want this flow to be permanent.
        return (OfmFlowMod) flow.toImmutable();
    }

}
