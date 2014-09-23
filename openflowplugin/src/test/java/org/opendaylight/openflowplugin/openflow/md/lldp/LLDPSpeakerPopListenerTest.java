/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.lldp;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.ModelDrivenSwitch;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by Martin Bobak mbobak@cisco.com on 9/23/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class LLDPSpeakerPopListenerTest {

    @MockitoAnnotations.Mock
    private ModelDrivenSwitch modelDrivenSwitch;
    @MockitoAnnotations.Mock
    private SessionContext context;
    @MockitoAnnotations.Mock
    private ConnectionConductor connectionConductor;


    @Before
    public void setup() {
        when(modelDrivenSwitch.getSessionContext()).thenReturn(context);
        when(context.getPrimaryConductor()).thenReturn(connectionConductor);
        when(connectionConductor.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        OpenflowPortsUtil.init();
    }

    @Test
    /**
     * Test method which verifies registration of nodeConnector in LLDPSpeaker. 
     */
    public void TestOnPop() {
        LLDPSpeakerPopListener lldpSpeakerPopListener = new LLDPSpeakerPopListener();
        NodeConnectorUpdatedBuilder nodeConnectorUpdatedBuilder = new NodeConnectorUpdatedBuilder();
        FlowCapableNodeConnectorUpdatedBuilder flowCapableNodeConnectorUpdatedBuilder = new FlowCapableNodeConnectorUpdatedBuilder();

        StateBuilder stateBuilder = new StateBuilder();
        stateBuilder.setBlocked(false);
        stateBuilder.setLinkDown(false);
        stateBuilder.setLive(true);
        flowCapableNodeConnectorUpdatedBuilder.setState(stateBuilder.build());

        flowCapableNodeConnectorUpdatedBuilder.setConfiguration(new PortConfig(true, true, true, false));
        flowCapableNodeConnectorUpdatedBuilder.setHardwareAddress(new MacAddress("00:00:00:00:00:00"));
        nodeConnectorUpdatedBuilder.addAugmentation(FlowCapableNodeConnectorUpdated.class, flowCapableNodeConnectorUpdatedBuilder.build());
        NodeConnectorId nodeConnectorId = new NodeConnectorId("1");
        nodeConnectorUpdatedBuilder.setId(nodeConnectorId);
        InstanceIdentifier instanceIdentifier = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId("1"))).child(NodeConnector.class, new NodeConnectorKey(nodeConnectorId));
        NodeConnectorRef nodeConnectorRef = new NodeConnectorRef(instanceIdentifier);

        LLDPSpeaker.getInstance().addModelDrivenSwitch(instanceIdentifier.firstIdentifierOf(Node.class), modelDrivenSwitch);

        nodeConnectorUpdatedBuilder.setNodeConnectorRef(nodeConnectorRef);
        lldpSpeakerPopListener.onPop(nodeConnectorUpdatedBuilder.build());

    }

}
