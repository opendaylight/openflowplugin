/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.opendaylight.openflowplugin.impl.util.BarrierUtil;
import org.opendaylight.openflowplugin.impl.util.MeterUtil;
import org.opendaylight.openflowplugin.impl.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.OriginalMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.UpdatedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.BatchMeterInputUpdateGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.add.meters.batch.input.BatchAddMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.output.list.grouping.BatchFailedMetersOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.remove.meters.batch.input.BatchRemoveMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.update.meters.batch.input.BatchUpdateMeters;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation delegates work to {@link SalMeterRpcs}.
 */
public class SalMetersBatchRpcs {
    private static final Logger LOG = LoggerFactory.getLogger(SalMetersBatchRpcs.class);

    private final SalMeterRpcs salMeterRpcs;
    private final FlowCapableTransactionRpc transactionRpc;

    public SalMetersBatchRpcs(final SalMeterRpcs salMeterRpcs, final FlowCapableTransactionRpc transactionRpc) {
        this.salMeterRpcs = requireNonNull(salMeterRpcs);
        this.transactionRpc = requireNonNull(transactionRpc);
    }

    @VisibleForTesting
    ListenableFuture<RpcResult<UpdateMetersBatchOutput>> updateMetersBatch(final UpdateMetersBatchInput input) {
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
            resultsLot.add(salMeterRpcs.getRpcClassToInstanceMap().getInstance(UpdateMeter.class)
                .invoke(updateMeterInput));
        }

        final Iterable<Meter> meters = batchUpdateMeters.stream()
                .map(BatchMeterInputUpdateGrouping::getUpdatedBatchedMeter)
                .collect(Collectors.toList());

        final ListenableFuture<RpcResult<List<BatchFailedMetersOutput>>> commonResult =
                Futures.transform(Futures.allAsList(resultsLot),
                        MeterUtil.createCumulativeFunction(meters, batchUpdateMeters.size()),
                        MoreExecutors.directExecutor());

        ListenableFuture<RpcResult<UpdateMetersBatchOutput>> updateMetersBulkFuture =
                Futures.transform(commonResult, MeterUtil.METER_UPDATE_TRANSFORM, MoreExecutors.directExecutor());

        if (input.getBarrierAfter()) {
            updateMetersBulkFuture = BarrierUtil.chainBarrier(updateMetersBulkFuture, input.getNode(),
                transactionRpc, MeterUtil.METER_UPDATE_COMPOSING_TRANSFORM);
        }

        return updateMetersBulkFuture;
    }

    @VisibleForTesting
    ListenableFuture<RpcResult<AddMetersBatchOutput>> addMetersBatch(final AddMetersBatchInput input) {
        LOG.trace("Adding meters @ {} : {}", PathUtil.extractNodeId(input.getNode()), input.getBatchAddMeters().size());
        final ArrayList<ListenableFuture<RpcResult<AddMeterOutput>>> resultsLot = new ArrayList<>();
        for (BatchAddMeters addMeter : input.nonnullBatchAddMeters().values()) {
            final AddMeterInput addMeterInput = new AddMeterInputBuilder(addMeter)
                    .setMeterRef(createMeterRef(input.getNode(), addMeter))
                    .setNode(input.getNode())
                    .build();
            resultsLot.add(salMeterRpcs.getRpcClassToInstanceMap().getInstance(AddMeter.class)
                .invoke(addMeterInput));
        }

        final ListenableFuture<RpcResult<List<BatchFailedMetersOutput>>> commonResult =
                Futures.transform(Futures.allAsList(resultsLot),
                        MeterUtil.createCumulativeFunction(input.nonnullBatchAddMeters().values()),
                        MoreExecutors.directExecutor());

        ListenableFuture<RpcResult<AddMetersBatchOutput>> addMetersBulkFuture =
                Futures.transform(commonResult, MeterUtil.METER_ADD_TRANSFORM, MoreExecutors.directExecutor());

        if (input.getBarrierAfter()) {
            addMetersBulkFuture = BarrierUtil.chainBarrier(addMetersBulkFuture, input.getNode(),
                    transactionRpc, MeterUtil.METER_ADD_COMPOSING_TRANSFORM);
        }

        return addMetersBulkFuture;
    }

    @VisibleForTesting
    ListenableFuture<RpcResult<RemoveMetersBatchOutput>> removeMetersBatch(final RemoveMetersBatchInput input) {
        LOG.trace("Removing meters @ {} : {}",
                  PathUtil.extractNodeId(input.getNode()),
                  input.getBatchRemoveMeters().size());
        final ArrayList<ListenableFuture<RpcResult<RemoveMeterOutput>>> resultsLot = new ArrayList<>();
        for (BatchRemoveMeters addMeter : input.nonnullBatchRemoveMeters().values()) {
            final RemoveMeterInput removeMeterInput = new RemoveMeterInputBuilder(addMeter)
                    .setMeterRef(createMeterRef(input.getNode(), addMeter))
                    .setNode(input.getNode())
                    .build();
            resultsLot.add(salMeterRpcs.getRpcClassToInstanceMap().getInstance(RemoveMeter.class)
                .invoke(removeMeterInput));
        }

        final ListenableFuture<RpcResult<List<BatchFailedMetersOutput>>> commonResult =
                Futures.transform(Futures.allAsList(resultsLot),
                        MeterUtil.createCumulativeFunction(input.nonnullBatchRemoveMeters().values()),
                        MoreExecutors.directExecutor());

        ListenableFuture<RpcResult<RemoveMetersBatchOutput>> removeMetersBulkFuture =
                Futures.transform(commonResult, MeterUtil.METER_REMOVE_TRANSFORM, MoreExecutors.directExecutor());

        if (input.getBarrierAfter()) {
            removeMetersBulkFuture = BarrierUtil.chainBarrier(removeMetersBulkFuture, input.getNode(),
                transactionRpc, MeterUtil.METER_REMOVE_COMPOSING_TRANSFORM);
        }

        return removeMetersBulkFuture;
    }

    public ClassToInstanceMap<Rpc<?,?>> getRpcClassToInstanceMap() {
        return ImmutableClassToInstanceMap.<Rpc<?, ?>>builder()
            .put(RemoveMetersBatch.class, this::removeMetersBatch)
            .put(AddMetersBatch.class, this::addMetersBatch)
            .put(UpdateMetersBatch.class, this::updateMetersBatch)
            .build();
    }

    private static MeterRef createMeterRef(final NodeRef nodeRef, final Meter batchMeter) {
        return MeterUtil.buildMeterPath((InstanceIdentifier<Node>) nodeRef.getValue(), batchMeter.getMeterId());
    }

    private static MeterRef createMeterRef(final NodeRef nodeRef, final BatchUpdateMeters batchMeter) {
        return MeterUtil.buildMeterPath((InstanceIdentifier<Node>) nodeRef.getValue(),
                batchMeter.getUpdatedBatchedMeter().getMeterId());
    }
}