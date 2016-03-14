/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowIdGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFlowsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.4.2015.
 */
public final class FlowUtil {

    private static final String ALIEN_SYSTEM_FLOW_ID = "#UF$TABLE*";
    private static final AtomicInteger unaccountedFlowsCounter = new AtomicInteger(0);
    private static final Logger LOG = LoggerFactory.getLogger(FlowUtil.class);


    private FlowUtil() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    public static FlowId createAlienFlowId(final short tableId) {
        final StringBuilder sBuilder = new StringBuilder(ALIEN_SYSTEM_FLOW_ID)
                .append(tableId).append('-').append(unaccountedFlowsCounter.incrementAndGet());
        String alienId = sBuilder.toString();
        return new FlowId(alienId);

    }

    /**
     * @param nodePath
     * @param tableId
     * @param flowId
     * @return instance identifier assembled for given node, table and flow
     */
    public static FlowRef buildFlowPath(final InstanceIdentifier<Node> nodePath,
            final short tableId, final FlowId flowId) {
        final KeyedInstanceIdentifier<Flow, FlowKey> flowPath = nodePath
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(tableId))
                .child(Flow.class, new FlowKey(new FlowId(flowId)));

        return new FlowRef(flowPath);
    }

    public static <O> Function<List<RpcResult<O>>, ArrayList<BatchFlowsOutput>> createCumulativeFunction(
            final List<? extends BatchFlowIdGrouping> inputBatchFlows) {
        return new Function<List<RpcResult<O>>, ArrayList<BatchFlowsOutput>>() {
            @Nullable
            @Override
            public ArrayList<BatchFlowsOutput> apply(@Nullable final List<RpcResult<O>> innerInput) {
                final int sizeOfFutures = innerInput.size();
                final int sizeOfInputBatch = inputBatchFlows.size();
                Preconditions.checkArgument(sizeOfFutures == sizeOfInputBatch,
                        "wrong amount of returned futures: {} <> {}", sizeOfFutures, sizeOfInputBatch);

                final ArrayList<BatchFlowsOutput> batchFlows = new ArrayList<>();
                final Iterator<? extends BatchFlowIdGrouping> batchFlowIterator = inputBatchFlows.iterator();

                for (RpcResult<O> flowModOutput : innerInput) {
                    final FlowId flowId = batchFlowIterator.next().getFlowId();

                    //TODO: preserve/propagate errors
                    batchFlows.add(
                            new BatchFlowsOutputBuilder()
                                    .setFlowId(flowId)
                                    .setSuccess(flowModOutput.isSuccessful())
                                    .build());
                }

                return batchFlows;
            }
        };
    }
}
