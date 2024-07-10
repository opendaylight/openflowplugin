/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.OriginalMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.UpdatedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.BatchMeterInputUpdateGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.update.meters.batch.input.BatchUpdateMeters;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UpdateMetersBatchImpl implements UpdateMetersBatch {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateMetersBatchImpl.class);

    private final UpdateMeter updateMeter;
    private final SendBarrier sendBarrier;

    public UpdateMetersBatchImpl(final UpdateMeter updateMeter, final SendBarrier sendBarrier) {
        this.updateMeter = requireNonNull(updateMeter);
        this.sendBarrier = requireNonNull(sendBarrier);
    }

    @Override
    public ListenableFuture<RpcResult<UpdateMetersBatchOutput>> invoke(final UpdateMetersBatchInput input) {
        final var batchUpdateMeters = input.nonnullBatchUpdateMeters();
        if (LOG.isTraceEnabled()) {
            LOG.trace("Updating meters @ {} : {}", PathUtil.extractNodeId(input.getNode()), batchUpdateMeters.size());
        }

        final var resultsLot = batchUpdateMeters.stream()
            .map(batchMeter -> updateMeter.invoke(new UpdateMeterInputBuilder(input)
                .setOriginalMeter(new OriginalMeterBuilder(batchMeter.getOriginalBatchedMeter()).build())
                .setUpdatedMeter(new UpdatedMeterBuilder(batchMeter.getUpdatedBatchedMeter()).build())
                .setMeterRef(createMeterRef(input.getNode(), batchMeter))
                .setNode(input.getNode())
                .build()))
            .collect(Collectors.toList());

        final var meters = batchUpdateMeters.stream()
                .map(BatchMeterInputUpdateGrouping::getUpdatedBatchedMeter)
                .collect(Collectors.toList());

        final var commonResult = Futures.transform(Futures.allAsList(resultsLot),
            MeterUtil.createCumulativeFunction(meters, batchUpdateMeters.size()), MoreExecutors.directExecutor());

        final var updateMetersBulkFuture = Futures.transform(commonResult, MeterUtil.METER_UPDATE_TRANSFORM,
            MoreExecutors.directExecutor());

        return input.getBarrierAfter()
            ? BarrierUtil.chainBarrier(updateMetersBulkFuture, input.getNode(), sendBarrier,
                MeterUtil.METER_UPDATE_COMPOSING_TRANSFORM)
            : updateMetersBulkFuture;
    }

    private static MeterRef createMeterRef(final NodeRef nodeRef, final BatchUpdateMeters batchMeter) {
        return MeterUtil.buildMeterPath(((DataObjectIdentifier<Node>) nodeRef.getValue()).toLegacy(),
                batchMeter.getUpdatedBatchedMeter().getMeterId());
    }
}
