/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.output.list.grouping.BatchMetersOutput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link MeterUtil}.
 */
public class MeterUtilTest {

    public static final NodeId DUMMY_NODE_ID = new NodeId("dummyNodeId");
    private static final MeterId DUMMY_METER_ID = new MeterId(42L);
    private static final MeterId DUMMY_METER_ID_2 = new MeterId(43L);

    @Test
    public void testBuildGroupPath() throws Exception {
        final InstanceIdentifier<Node> nodePath = InstanceIdentifier
                .create(Nodes.class)
                .child(Node.class, new NodeKey(DUMMY_NODE_ID));

        final MeterRef meterRef = MeterUtil.buildMeterPath(nodePath, DUMMY_METER_ID);
        final InstanceIdentifier<?> meterRefValue = meterRef.getValue();
        Assert.assertEquals(DUMMY_NODE_ID, meterRefValue.firstKeyOf(Node.class).getId());
        Assert.assertEquals(DUMMY_METER_ID, meterRefValue.firstKeyOf(Meter.class).getMeterId());
    }

    @Test
    public void testCreateCumulativeFunction() throws Exception {
        final Function<List<RpcResult<String>>, ArrayList<BatchMetersOutput>> function =
                MeterUtil.createCumulativeFunction(Lists.newArrayList(
                        createBatchMeter(DUMMY_METER_ID),
                        createBatchMeter(DUMMY_METER_ID_2)));

        final ArrayList<BatchMetersOutput> output = function.apply(Lists.newArrayList(
                RpcResultBuilder.success("a").build(),
                RpcResultBuilder.<String>failed().build()));

        Assert.assertEquals(2, output.size());
        Assert.assertEquals(DUMMY_METER_ID, output.get(0).getMeterId());
        Assert.assertEquals(DUMMY_METER_ID_2, output.get(1).getMeterId());
    }

    private org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter createBatchMeter(final MeterId meterId) {
        return new MeterBuilder()
                .setMeterId(meterId)
                .build();
    }
}