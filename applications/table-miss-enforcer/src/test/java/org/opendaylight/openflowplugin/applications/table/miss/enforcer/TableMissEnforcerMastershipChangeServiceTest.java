/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.table.miss.enforcer;

import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * Test for {@link org.opendaylight.openflowplugin.applications.table.miss.enforcer.TableMissEnforcerMastershipChangeService}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TableMissEnforcerMastershipChangeServiceTest {

    private TableMissEnforcerMastershipChangeService mastershipChangeService;
    @Mock
    private SalFlowService salFlowService;
    @Mock
    private DeviceInfo deviceInfo;

    private static final KeyedInstanceIdentifier<Node, NodeKey> NODE_INSTANCE_IDENTIFIER = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId("testnode:1")));

    @Before
    public void setUp() {
        mastershipChangeService = new TableMissEnforcerMastershipChangeService(salFlowService);
        when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(NODE_INSTANCE_IDENTIFIER);
        Mockito.when(salFlowService.addFlow(Mockito.any())).thenReturn(Futures.immediateFuture(null));
    }

    @Test
    public void onBecomeOwner() throws Exception {
        mastershipChangeService.onBecomeOwner(deviceInfo);
        mastershipChangeService.close();
        Mockito.verify(salFlowService).addFlow(Mockito.any());
    }
}