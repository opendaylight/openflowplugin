/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.ofswitch.config;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.applications.deviceownershipservice.DeviceOwnershipService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.SwitchConfigFlag;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link DefaultConfigPusher}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultConfigPusherTest {
    private DefaultConfigPusher defaultConfigPusher;
    private static final DataObjectIdentifier<Node> NODE_IID = DataObjectIdentifier.builder(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId("testnode:1")))
            .build();
    @Mock
    private DataBroker dataBroker;
    @Mock
    private RpcService rpcService;
    @Mock
    private SetConfig setConfig;
    @Mock
    private ListenableFuture<?> setConfigResult;
    @Mock
    private DataTreeModification<FlowCapableNode> dataTreeModification;
    @Mock
    private DataObjectModification<FlowCapableNode> dataObjectModification;
    @Mock
    private DeviceOwnershipService deviceOwnershipService;
    @Mock
    private Registration reg;
    @Captor
    private ArgumentCaptor<SetConfigInput> setConfigInputCaptor;

    @Before
    public void setUp() {
        doReturn(RpcResultBuilder.success().buildFuture()).when(setConfig).invoke(any());
        doReturn(reg).when(dataBroker).registerTreeChangeListener(any(), any(), any());
        doReturn(setConfig).when(rpcService).getRpc(any());
        defaultConfigPusher = new DefaultConfigPusher(dataBroker, rpcService, deviceOwnershipService);
        when(dataTreeModification.path()).thenReturn(NODE_IID.toBuilder().augmentation(FlowCapableNode.class).build());
        when(dataTreeModification.getRootNode()).thenReturn(dataObjectModification);
        when(dataTreeModification.getRootNode().modificationType()).thenReturn(ModificationType.WRITE);
        when(deviceOwnershipService.isEntityOwned(any())).thenReturn(true);
    }

    @Test
    public void testOnDataTreeChanged() {
        defaultConfigPusher.onDataTreeChanged(List.of(dataTreeModification));
        verify(setConfig).invoke(setConfigInputCaptor.capture());
        final var captured = setConfigInputCaptor.getValue();
        assertEquals(SwitchConfigFlag.FRAGNORMAL.toString(), captured.getFlag());
        assertEquals(OFConstants.OFPCML_NO_BUFFER, captured.getMissSearchLength());
        assertEquals(NODE_IID, captured.getNode().getValue());
    }

    @After
    public void tearDown() {
        defaultConfigPusher.close();
    }
}
