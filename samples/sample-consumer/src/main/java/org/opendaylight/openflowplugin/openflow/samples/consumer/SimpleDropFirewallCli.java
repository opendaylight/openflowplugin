/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.samples.consumer;

import java.util.Collections;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;

public class SimpleDropFirewallCli {

    private SimpleDropFirewall service;

    public void setService(final SimpleDropFirewall service) {
        this.service = service;
    }

    /**
     * Form of input is:
     *
     * node name node-connector number source ip-address destinatinon ip-address
     *
     * @param cliInput
     *            Parsed input from CLI
     * @return
     */
    public AddFlowInput createTcpFlow(final List<String> cliInput) {
        AddFlowInputBuilder ret = new AddFlowInputBuilder();
        ret.setNode(nodeFromString(cliInput.get(0)));

        // We construct a match
        MatchBuilder match = new MatchBuilder();

        Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();

        ipv4Match.setIpv4Source(new Ipv4Prefix(cliInput.get(3)));
        ipv4Match.setIpv4Destination(new Ipv4Prefix(cliInput.get(4)));

        match.setLayer3Match(ipv4Match.build());

        match.setLayer4Match(new TcpMatchBuilder().build());

        ret.setMatch(match.build());

        DropActionCase dropAction = new DropActionCaseBuilder().build();

        ActionBuilder action = new ActionBuilder();
        action.setAction(dropAction);

        List<Action> actions = Collections.singletonList(action.build());
        //
        ApplyActionsCaseBuilder aaBldr = new ApplyActionsCaseBuilder();
        aaBldr.setApplyActions(new ApplyActionsBuilder().setAction(actions).build());

        InstructionBuilder instructionBldr = new InstructionBuilder();
        instructionBldr.setInstruction(aaBldr.build());

        List<Instruction> isntructions = Collections.singletonList(instructionBldr.build());
        InstructionsBuilder instructionsBldr = new InstructionsBuilder();
		instructionsBldr.setInstruction(isntructions);

        ret.setInstructions(instructionsBldr.build());

        return ret.build();
    }

    private NodeRef nodeFromString(final String string) {
        // TODO Auto-generated method stub
        return null;
    }

}
