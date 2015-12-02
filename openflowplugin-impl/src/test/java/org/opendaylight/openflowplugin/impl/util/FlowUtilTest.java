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

package org.opendaylight.openflowplugin.impl.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.device.DeviceStateImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;

@RunWith(MockitoJUnitRunner.class)
public class FlowUtilTest {

    private static final short DUMMY_TABLE_ID = 1;
    public static final Pattern INDEX_PATTERN = Pattern.compile("^#UF\\$TABLE\\*1-([0-9]+)$");

    @Mock
    private DataBroker dataBroker;
    @Mock
    private DeviceFlowRegistry flowRegistry;
    @Mock
    private ReadOnlyTransaction rTx;
    @Captor
    private ArgumentCaptor<FlowRegistryKey> flowRegistryKeyCaptor;
    @Captor
    private ArgumentCaptor<FlowDescriptor> flowDescriptorCaptor;


    @Test
    public void createAlienFlowIdTest() {
        final String alienFlowId1 = FlowUtil.createAlienFlowId(DUMMY_TABLE_ID).getValue();
        final Integer index1 = parseIndex(alienFlowId1);
        final String alienFlowId2 = FlowUtil.createAlienFlowId(DUMMY_TABLE_ID).getValue();
        final Integer index2 = parseIndex(alienFlowId2);

        assertNotNull("index1 parsing failed: " + alienFlowId1, index1);
        assertNotNull("index2 parsing failed: " + alienFlowId2, index2);
        assertTrue(index1 < index2);
    }

    private static Integer parseIndex(String alienFlowIdValue) {
        final Matcher mach = INDEX_PATTERN.matcher(alienFlowIdValue);
        if (mach.find()) {
            return Integer.valueOf(mach.group(1));
        }
        return null;
    }

    @Test
    public void testPreloadConfiguredFlows() throws Exception {
        final NodeId nodeId = new NodeId("ut-dummy-node-id");
        final String FLOW_ID = "ut-dummy-flow-id";

        final FeaturesReply featuresReply = new GetFeaturesOutputBuilder()
                .setVersion(OFConstants.OFP_VERSION_1_3)
                .build();
        final DeviceState deviceState = new DeviceStateImpl(featuresReply, nodeId);
        final Flow flow = new FlowBuilder()
                .setId(new FlowId(FLOW_ID))
                .setTableId((short) 0)
                .setMatch(new MatchBuilder().build())
                .setPriority(1)
                .build();
        final Table table = new TableBuilder()
                .setFlow(Collections.singletonList(flow))
                .setId((short) 0)
                .build();
        final FlowCapableNode flowCapableNode = new FlowCapableNodeBuilder()
                .setTable(Collections.singletonList(table))
                .build();

        Mockito.when(dataBroker.newReadOnlyTransaction()).thenReturn(rTx);
        Mockito.when(rTx.read(
                Matchers.same(LogicalDatastoreType.CONFIGURATION),
                Matchers.eq(deviceState.getNodeInstanceIdentifier().augmentation(FlowCapableNode.class))))
                .thenReturn(Futures.<Optional<FlowCapableNode>, ReadFailedException>immediateCheckedFuture(Optional.of(flowCapableNode)));

        FlowUtil.preloadConfiguredFlows(flowRegistry, dataBroker, deviceState, 1);

        Mockito.verify(flowRegistry).store(flowRegistryKeyCaptor.capture(), flowDescriptorCaptor.capture());
        final FlowRegistryKey flowRegistryKey = flowRegistryKeyCaptor.getValue();
        Assert.assertEquals(0, flowRegistryKey.getTableId());
        Assert.assertEquals("0", flowRegistryKey.getCookie().toString());
        Assert.assertEquals(1, flowRegistryKey.getPriority());
        final FlowDescriptor flowDescriptor = flowDescriptorCaptor.getValue();
        Assert.assertEquals(FLOW_ID, flowDescriptor.getFlowId().getValue());
        Assert.assertEquals(0L, flowDescriptor.getTableKey().getId().longValue());

        Mockito.verify(rTx).read(
                Matchers.same(LogicalDatastoreType.CONFIGURATION),
                Matchers.eq(deviceState.getNodeInstanceIdentifier().augmentation(FlowCapableNode.class)));
        Mockito.verify(rTx).close();
    }
}
