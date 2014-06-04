/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptest;

import java.math.BigInteger;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DropTestRpcSender extends AbstractDropTest {
    private final static Logger LOG = LoggerFactory.getLogger(DropTestProvider.class);

    private final SalFlowService flowService;

    public DropTestRpcSender(final SalFlowService flowService) {
        this.flowService = flowService;
    }

    @Override
    protected void processPacket(final NodeKey node, final Match match, final Instructions instructions) {

        // Finally build our flow
        final AddFlowInputBuilder fb = new AddFlowInputBuilder();
        fb.setMatch(match);
        fb.setInstructions(instructions);
        //fb.setId(new FlowId(Long.toString(fb.hashCode)));

        // Construct the flow instance id
        final InstanceIdentifier<Node> flowInstanceId = InstanceIdentifier
                .builder(Nodes.class) // File under nodes
                .child(Node.class, node).toInstance(); // A particular node identified by nodeKey
        fb.setNode(new NodeRef(flowInstanceId));

        fb.setPriority(4);
        fb.setBufferId(0L);
        final BigInteger value = BigInteger.valueOf(10);
        fb.setCookie(new FlowCookie(value));
        fb.setCookieMask(new FlowCookie(value));
        fb.setTableId((short) 0);
        fb.setHardTimeout(300);
        fb.setIdleTimeout(240);
        fb.setFlags(new FlowModFlags(false, false, false, false, false));

        // Add flow
        flowService.addFlow(fb.build());
    }
}
