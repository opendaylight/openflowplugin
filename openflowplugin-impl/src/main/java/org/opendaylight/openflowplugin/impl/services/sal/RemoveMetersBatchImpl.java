/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.stream.Collectors;
import org.opendaylight.openflowplugin.impl.util.BarrierUtil;
import org.opendaylight.openflowplugin.impl.util.MeterUtil;
import org.opendaylight.openflowplugin.impl.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchOutput;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RemoveMetersBatchImpl  implements RemoveMetersBatch {
    private static final Logger LOG = LoggerFactory.getLogger(RemoveMetersBatchImpl.class);

    private final RemoveMeter removeMeter;
    private final SendBarrier sendBarrier;

    public RemoveMetersBatchImpl(final RemoveMeter removeMeter, final SendBarrier sendBarrier) {
        this.removeMeter = requireNonNull(removeMeter);
        this.sendBarrier = requireNonNull(sendBarrier);
    }

    @Override
    public ListenableFuture<RpcResult<RemoveMetersBatchOutput>> invoke(final RemoveMetersBatchInput input) {
        final var meters = input.nonnullBatchRemoveMeters().values();
        if (LOG.isTraceEnabled()) {
            LOG.trace("Removing meters @ {} : {}", PathUtil.extractNodeId(input.getNode()), meters.size());
        }

        final var resultsLot = meters.stream()
            .map(meter -> removeMeter.invoke(new RemoveMeterInputBuilder(meter)
                .setMeterRef(createMeterRef(input.getNode(), meter))
                .setNode(input.getNode())
                .build()))
            .collect(Collectors.toList());

        final var commonResult = Futures.transform(Futures.allAsList(resultsLot),
            MeterUtil.createCumulativeFunction(meters), MoreExecutors.directExecutor());

        final var removeMetersBulkFuture = Futures.transform(commonResult, MeterUtil.METER_REMOVE_TRANSFORM,
            MoreExecutors.directExecutor());

        return input.getBarrierAfter()
            ? BarrierUtil.chainBarrier(removeMetersBulkFuture, input.getNode(), sendBarrier,
                MeterUtil.METER_REMOVE_COMPOSING_TRANSFORM)
            : removeMetersBulkFuture;
    }

    private static MeterRef createMeterRef(final NodeRef nodeRef, final Meter batchMeter) {
        return MeterUtil.buildMeterPath((DataObjectIdentifier<Node>) nodeRef.getValue(), batchMeter.getMeterId());
    }
}
