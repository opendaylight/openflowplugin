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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowIdGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class FlowUtilTest {

    public static final Pattern INDEX_PATTERN = Pattern.compile("^#UF\\$TABLE\\*1-([0-9]+)$");
    public static final NodeId DUMMY_NODE_ID = new NodeId("dummyNodeId");
    public static final FlowId DUMMY_FLOW_ID = new FlowId("dummyFlowId");
    public static final FlowId DUMMY_FLOW_ID_2 = new FlowId("dummyFlowId_2");
    public static final Short DUMMY_TABLE_ID = 1;

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
    public void testBuildFlowPath() throws Exception {
        final InstanceIdentifier<Node> nodePath = InstanceIdentifier
                .create(Nodes.class)
                .child(Node.class, new NodeKey(DUMMY_NODE_ID));

        final FlowRef flowRef = FlowUtil.buildFlowPath(nodePath, DUMMY_TABLE_ID, DUMMY_FLOW_ID);
        final InstanceIdentifier<?> flowRefValue = flowRef.getValue();
        Assert.assertEquals(DUMMY_NODE_ID, flowRefValue.firstKeyOf(Node.class).getId());
        Assert.assertEquals(DUMMY_TABLE_ID, flowRefValue.firstKeyOf(Table.class).getId());
        Assert.assertEquals(DUMMY_FLOW_ID, flowRefValue.firstKeyOf(Flow.class).getId());
    }

    @Test
    public void testCreateFunction() throws Exception {
        final Function<List<RpcResult<String>>, ArrayList<BatchFlowsOutput>> function =
                FlowUtil.createCumulativeFunction(Lists.newArrayList(createBatchFlowIdGrouping(DUMMY_FLOW_ID),
                        createBatchFlowIdGrouping(DUMMY_FLOW_ID_2)));

        final ArrayList<BatchFlowsOutput> output = function.apply(Lists.newArrayList(
                RpcResultBuilder.success("a").build(),
                RpcResultBuilder.<String>failed().build()));

        Assert.assertEquals(2, output.size());
        Assert.assertEquals(DUMMY_FLOW_ID, output.get(0).getFlowId());
        Assert.assertEquals(DUMMY_FLOW_ID_2, output.get(1).getFlowId());
    }

    protected BatchFlowIdGrouping createBatchFlowIdGrouping(final FlowId flowId) {
        final BatchFlowIdGrouping mock = Mockito.mock(BatchFlowIdGrouping.class);
        Mockito.when(mock.getFlowId()).thenReturn(flowId);
        return mock;
    }
}
