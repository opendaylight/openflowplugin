/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.ofswitch.config;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.NodeConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigInput;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link DefaultConfigPusher}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultConfigPusherTest {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultConfigPusherTest.class);

    @Mock
    private NodeConfigService mockNodeConfigService;
    @Mock
    private AsyncDataChangeEvent mockAsyncDataChangeEvent;
    @Mock
    private DataBroker mockDataBroker;

    private DefaultConfigPusher defaultConfigPusher;

    @Before
    public void setUp() throws Exception {
        defaultConfigPusher = new DefaultConfigPusher(mockNodeConfigService, mockDataBroker);
    }

    @Test
    public void testOnDataChanged() throws Exception {
        final InstanceIdentifier<Node> nodeIID = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId("1")))
                .build();

        AddFlowInputBuilder addFlowInputBuilder = new AddFlowInputBuilder()
                .setFlowName("flow:1");

        Map<InstanceIdentifier<?>,DataObject> created = new HashMap<>();
        created.put(nodeIID, addFlowInputBuilder.build());

        when(mockAsyncDataChangeEvent.getCreatedData()).thenReturn(created);
        defaultConfigPusher.onDataChanged(mockAsyncDataChangeEvent);
        verify(mockNodeConfigService).setConfig(Matchers.<SetConfigInput>any());
    }

}