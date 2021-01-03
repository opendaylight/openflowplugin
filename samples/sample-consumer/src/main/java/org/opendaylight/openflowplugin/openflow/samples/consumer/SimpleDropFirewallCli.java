/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.samples.consumer;

import java.util.List;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;

public class SimpleDropFirewallCli {

    /**
     * Form of input is: node name node-connector number source ip-address destinatinon ip-address.
     *
     * @param cliInput Parsed input from CLI
     */
    public AddFlowInput createTcpFlow(final List<String> cliInput) {
        return new AddFlowInputBuilder()
            .setNode(nodeFromString(cliInput.get(0)))
            // We construct a match
            .setMatch(new MatchBuilder()
                .setLayer3Match(new Ipv4MatchBuilder()
                    .setIpv4Source(new Ipv4Prefix(cliInput.get(3)))
                    .setIpv4Destination(new Ipv4Prefix(cliInput.get(4)))
                    .build())
                .setLayer4Match(new TcpMatchBuilder().build())
                .build())
            .setInstructions(new InstructionsBuilder()
                .setInstruction(BindingMap.of(new InstructionBuilder()
                    .setInstruction(new ApplyActionsCaseBuilder()
                        .setApplyActions(new ApplyActionsBuilder()
                            .setAction(BindingMap.of(new ActionBuilder()
                                .setOrder(0)
                                .setAction(new DropActionCaseBuilder().build())
                                .build()))
                            .build())
                        .build())
                    .build()))
                .build())
            .build();
    }

    private static NodeRef nodeFromString(final String string) {
        // TODO Auto-generated method stub
        return null;
    }
}
