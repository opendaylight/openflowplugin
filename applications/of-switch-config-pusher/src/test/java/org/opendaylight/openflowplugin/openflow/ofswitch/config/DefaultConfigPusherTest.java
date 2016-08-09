/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.ofswitch.config;

import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.NodeConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigInput;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link DefaultConfigPusher}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultConfigPusherTest {
    private DefaultConfigPusher defaultConfigPusher;
    private final static InstanceIdentifier<Node> nodeIID = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId("testnode:1")));
    @Mock
    private NodeConfigService nodeConfigService;
    @Mock
    private DataTreeModification<FlowCapableNode> dataTreeModification;

    @Before
    public void setUp() throws Exception {
        defaultConfigPusher = new DefaultConfigPusher(nodeConfigService, Mockito.mock(DataBroker.class));
        final DataTreeIdentifier<FlowCapableNode> identifier = new DataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, nodeIID);
        Mockito.when(dataTreeModification.getRootPath()).thenReturn(identifier);
    }

    @Test
    public void testOnDataChanged() throws Exception {
        AddFlowInputBuilder addFlowInputBuilder = new AddFlowInputBuilder()
                .setFlowName("flow:1");
        Map<InstanceIdentifier<?>,DataObject> created = new HashMap<>();
        created.put(nodeIID, addFlowInputBuilder.build());

        defaultConfigPusher.onDataTreeChanged(Collections.singleton(dataTreeModification));
        verify(nodeConfigService).setConfig(Matchers.<SetConfigInput>any());
    }

}