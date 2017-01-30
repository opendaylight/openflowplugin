/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.sal;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.impl.util.BarrierUtil;
import org.opendaylight.openflowplugin.impl.util.MeterUtil;
import org.opendaylight.openflowplugin.impl.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.OriginalMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.UpdatedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.SalMetersBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.add.meters.batch.input.BatchAddMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.output.list.grouping.BatchFailedMetersOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.remove.meters.batch.input.BatchRemoveMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.update.meters.batch.input.BatchUpdateMeters;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link SalMetersBatchService} - delegates work to {@link SalMeterService}.
 */
public class SalMetersBatchServiceImpl implements SalMetersBatchService {

    private static final Logger LOG = LoggerFactory.getLogger(SalMetersBatchServiceImpl.class);

    private final SalMeterService salMeterService;
    private final FlowCapableTransactionService transactionService;

    public SalMetersBatchServiceImpl(final SalMeterService salMeterService, final FlowCapableTransactionService transactionService) {
        this.salMeterService = Preconditions.checkNotNull(salMeterService);
        this.transactionService = Preconditions.checkNotNull(transactionService);
    }

    @Override
    public Future<RpcResult<UpdateMetersBatchOutput>> updateMetersBatch(final UpdateMetersBatchInput input) {
        final List<BatchUpdateMeters> batchUpdateMeters = input.getBatchUpdateMeters();
        LOG.trace("Updating meters @ {} : {}", PathUtil.extractNodeId(input.getNode()), batchUpdateMeters.size());

        final ArrayList<ListenableFuture<RpcResult<UpdateMeterOutput>>> resultsLot = new ArrayList<>();
        for (BatchUpdateMeters batchMeter : batchUpdateMeters) {
            final UpdateMeterInput updateMeterInput = new UpdateMeterInputBuilder(input)
                    .setOriginalMeter(new OriginalMeterBuilder(batchMeter.getOriginalBatchedMeter()).build())
                    .setUpdatedMeter(new UpdatedMeterBuilder(batchMeter.getUpdatedBatchedMeter()).build())
                    .setMeterRef(createMeterRef(input.getNode(), batchMeter))
                    .setNode(input.getNode())
                    .build();
            resultsLot.add(JdkFutureAdapters.listenInPoolThread(salMeterService.updateMeter(updateMeterInput)));
        }

        final Iterable<Meter> meters = Iterables.transform(batchUpdateMeters, new Function<BatchUpdateMeters, Meter>() {
                    @Nullable
                    @Override
                    public Meter apply(@Nullable final BatchUpdateMeters input) {
                        return input.getUpdatedBatchedMeter();
                    }
                }
        );

        final ListenableFuture<RpcResult<List<BatchFailedMetersOutput>>> commonResult =
                Futures.transform(Futures.allAsList(resultsLot), MeterUtil.<UpdateMeterOutput>createCumulativeFunction(
                        meters, batchUpdateMeters.size()));

        ListenableFuture<RpcResult<UpdateMetersBatchOutput>> updateMetersBulkFuture =
                Futures.transform(commonResult, MeterUtil.METER_UPDATE_TRANSFORM);

        if (input.isBarrierAfter()) {
            updateMetersBulkFuture = BarrierUtil.chainBarrier(updateMetersBulkFuture, input.getNode(),
                    transactionService, MeterUtil.METER_UPDATE_COMPOSING_TRANSFORM);
        }

        return updateMetersBulkFuture;
    }

    @Override
    public Future<RpcResult<AddMetersBatchOutput>> addMetersBatch(final AddMetersBatchInput input) {
        LOG.trace("Adding meters @ {} : {}", PathUtil.extractNodeId(input.getNode()), input.getBatchAddMeters().size());
        final ArrayList<ListenableFuture<RpcResult<AddMeterOutput>>> resultsLot = new ArrayList<>();
        for (BatchAddMeters addMeter : input.getBatchAddMeters()) {
            final AddMeterInput addMeterInput = new AddMeterInputBuilder(addMeter)
                    .setMeterRef(createMeterRef(input.getNode(), addMeter))
                    .setNode(input.getNode())
                    .build();
            resultsLot.add(JdkFutureAdapters.listenInPoolThread(salMeterService.addMeter(addMeterInput)));
        }

        final ListenableFuture<RpcResult<List<BatchFailedMetersOutput>>> commonResult =
                Futures.transform(Futures.allAsList(resultsLot),
                        MeterUtil.<AddMeterOutput>createCumulativeFunction(input.getBatchAddMeters()));

        ListenableFuture<RpcResult<AddMetersBatchOutput>> addMetersBulkFuture =
                Futures.transform(commonResult, MeterUtil.METER_ADD_TRANSFORM);

        if (input.isBarrierAfter()) {
            addMetersBulkFuture = BarrierUtil.chainBarrier(addMetersBulkFuture, input.getNode(),
                    transactionService, MeterUtil.METER_ADD_COMPOSING_TRANSFORM);
        }

        return addMetersBulkFuture;
    }

    @Override
    public Future<RpcResult<RemoveMetersBatchOutput>> removeMetersBatch(final RemoveMetersBatchInput input) {
        LOG.trace("Removing meters @ {} : {}", PathUtil.extractNodeId(input.getNode()), input.getBatchRemoveMeters().size());
        final ArrayList<ListenableFuture<RpcResult<RemoveMeterOutput>>> resultsLot = new ArrayList<>();
        for (BatchRemoveMeters addMeter : input.getBatchRemoveMeters()) {
            final RemoveMeterInput removeMeterInput = new RemoveMeterInputBuilder(addMeter)
                    .setMeterRef(createMeterRef(input.getNode(), addMeter))
                    .setNode(input.getNode())
                    .build();
            resultsLot.add(JdkFutureAdapters.listenInPoolThread(salMeterService.removeMeter(removeMeterInput)));
        }

        final ListenableFuture<RpcResult<List<BatchFailedMetersOutput>>> commonResult =
                Futures.transform(Futures.allAsList(resultsLot),
                        MeterUtil.<RemoveMeterOutput>createCumulativeFunction(input.getBatchRemoveMeters()));

        ListenableFuture<RpcResult<RemoveMetersBatchOutput>> removeMetersBulkFuture =
                Futures.transform(commonResult, MeterUtil.METER_REMOVE_TRANSFORM);

        if (input.isBarrierAfter()) {
            removeMetersBulkFuture = BarrierUtil.chainBarrier(removeMetersBulkFuture, input.getNode(),
                    transactionService, MeterUtil.METER_REMOVE_COMPOSING_TRANSFORM);
        }

        return removeMetersBulkFuture;
    }

    private static MeterRef createMeterRef(final NodeRef nodeRef, final Meter batchMeter) {
        return MeterUtil.buildMeterPath((InstanceIdentifier<Node>) nodeRef.getValue(), batchMeter.getMeterId());
    }

    private static MeterRef createMeterRef(final NodeRef nodeRef, final BatchUpdateMeters batchMeter) {
        return MeterUtil.buildMeterPath((InstanceIdentifier<Node>) nodeRef.getValue(),
                batchMeter.getUpdatedBatchedMeter().getMeterId());
    }
}
