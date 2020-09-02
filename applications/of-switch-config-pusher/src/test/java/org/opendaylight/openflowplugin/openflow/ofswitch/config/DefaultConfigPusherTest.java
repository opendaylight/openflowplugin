/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.ofswitch.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.applications.deviceownershipservice.DeviceOwnershipService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.NodeConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.SwitchConfigFlag;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link DefaultConfigPusher}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultConfigPusherTest {
    private DefaultConfigPusher defaultConfigPusher;
    private static final InstanceIdentifier<Node> NODE_IID = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId("testnode:1")));
    @Mock
    private NodeConfigService nodeConfigService;
    @Mock
    private DataTreeModification<FlowCapableNode> dataTreeModification;
    @Mock
    private DeviceOwnershipService deviceOwnershipService;
    @Captor
    private ArgumentCaptor<SetConfigInput> setConfigInputCaptor;

    @Before
    public void setUp() {
        doReturn(RpcResultBuilder.success().buildFuture()).when(nodeConfigService).setConfig(any());
        defaultConfigPusher = new DefaultConfigPusher(nodeConfigService, Mockito.mock(DataBroker.class),
                deviceOwnershipService);
        final DataTreeIdentifier<FlowCapableNode> identifier = DataTreeIdentifier.create(
                LogicalDatastoreType.OPERATIONAL, NODE_IID.augmentation(FlowCapableNode.class));
        Mockito.when(dataTreeModification.getRootPath()).thenReturn(identifier);
        Mockito.when(dataTreeModification.getRootNode()).thenReturn(Mockito.mock(DataObjectModification.class));
        Mockito.when(dataTreeModification.getRootNode().getModificationType()).thenReturn(ModificationType.WRITE);
        when(deviceOwnershipService.isEntityOwned(any())).thenReturn(true);
    }

    @Test
    public void testOnDataTreeChanged() {
        defaultConfigPusher.onDataTreeChanged(Collections.singleton(dataTreeModification));
        Mockito.verify(nodeConfigService).setConfig(setConfigInputCaptor.capture());
        final SetConfigInput captured = setConfigInputCaptor.getValue();
        Assert.assertEquals(SwitchConfigFlag.FRAGNORMAL.toString(), captured.getFlag());
        Assert.assertEquals(OFConstants.OFPCML_NO_BUFFER, captured.getMissSearchLength());
        Assert.assertEquals(NODE_IID, captured.getNode().getValue());
    }

    @After
    public void tearDown() {
        defaultConfigPusher.close();
    }

}
