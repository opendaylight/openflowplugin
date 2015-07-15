/*
 *
 *  * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 *  * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 *
 */

package org.opendaylight.openflowplugin.impl.statistics;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;


@RunWith(MockitoJUnitRunner.class)
public class StatisticsGatheringUtilsTest {

    @Test
    public void writeFlowStatisticsTest() {
        DeviceContext mockedDeviceContext = mock(DeviceContext.class);
        DeviceState mockedDeviceState = mock(DeviceState.class);

        KeyedInstanceIdentifier<Node,NodeKey> dummyFlowCapableNode = InstanceIdentifier.create(Nodes.class).child(Node
                .class, new
                NodeKey(new NodeId("dummyNodeId")));
        when(mockedDeviceState.getNodeInstanceIdentifier()).thenReturn(dummyFlowCapableNode);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        StatisticsGatheringUtils.writeFlowStatistics(prepareFlowStatisticsData(), mockedDeviceContext);
    }



    private Iterable<FlowsStatisticsUpdate> prepareFlowStatisticsData() {
        return Collections.emptyList();
    }
}
