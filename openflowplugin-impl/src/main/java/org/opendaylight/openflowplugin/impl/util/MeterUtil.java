/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.output.list.grouping.BatchMetersOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.output.list.grouping.BatchMetersOutputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * provides meter util methods
 */
public final class MeterUtil {

    private MeterUtil() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    /**
     * @param nodePath
     * @param meterId
     * @return instance identifier assembled for given node and meter
     */
    public static MeterRef buildMeterPath(final InstanceIdentifier<Node> nodePath, final MeterId meterId) {
        final KeyedInstanceIdentifier<Meter, MeterKey> meterPath = nodePath
                .augmentation(FlowCapableNode.class)
                .child(Meter.class, new MeterKey(meterId));

        return new MeterRef(meterPath);
    }

    public static <O> Function<List<RpcResult<O>>, ArrayList<BatchMetersOutput>> createCumulativeFunction(
            final Iterable<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter> inputBatchMeters) {
        return createCumulativeFunction(inputBatchMeters, Iterables.size(inputBatchMeters));
    }

    public static <O> Function<List<RpcResult<O>>, ArrayList<BatchMetersOutput>> createCumulativeFunction(
            final Iterable<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter> inputBatchMeters,
            final int sizeOfInputBatch) {
        return new Function<List<RpcResult<O>>, ArrayList<BatchMetersOutput>>() {
            @Nullable
            @Override
            public ArrayList<BatchMetersOutput> apply(@Nullable final List<RpcResult<O>> innerInput) {
                final int sizeOfFutures = innerInput.size();
                Preconditions.checkArgument(sizeOfFutures == sizeOfInputBatch,
                        "wrong amount of returned futures: {} <> {}", sizeOfFutures, sizeOfInputBatch);

                final ArrayList<BatchMetersOutput> batchFlows = new ArrayList<>();
                final Iterator<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter>
                        batchMeterIterator = inputBatchMeters.iterator();

                for (RpcResult<O> flowModOutput : innerInput) {
                    final MeterId meterId = batchMeterIterator.next().getMeterId();

                    //TODO: preserve/propagate errors
                    batchFlows.add(
                            new BatchMetersOutputBuilder()
                                    .setMeterId(meterId)
                                    .setSuccess(flowModOutput.isSuccessful())
                                    .build());
                }

                return batchFlows;
            }
        };
    }
}
