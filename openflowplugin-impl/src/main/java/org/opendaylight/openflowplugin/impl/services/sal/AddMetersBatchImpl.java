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
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchOutput;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AddMetersBatchImpl implements AddMetersBatch {
    private static final Logger LOG = LoggerFactory.getLogger(AddMetersBatchImpl.class);

    private final AddMeter addMeter;
    private final SendBarrier sendBarrier;

    public AddMetersBatchImpl(final AddMeter addMeter, final SendBarrier sendBarrier) {
        this.addMeter = requireNonNull(addMeter);
        this.sendBarrier = requireNonNull(sendBarrier);
    }

    @Override
    public ListenableFuture<RpcResult<AddMetersBatchOutput>> invoke(final AddMetersBatchInput input) {
        final var meters = input.nonnullBatchAddMeters().values();
        if (LOG.isTraceEnabled()) {
            LOG.trace("Adding meters @ {} : {}", PathUtil.extractNodeId(input.getNode()), meters.size());
        }

        final var resultsLot = meters.stream()
            .map(meter -> addMeter.invoke(new AddMeterInputBuilder(meter)
                .setMeterRef(createMeterRef(input.getNode(), meter))
                .setNode(input.getNode())
                .build()))
            .collect(Collectors.toList());

        final var commonResult = Futures.transform(Futures.allAsList(resultsLot),
            MeterUtil.createCumulativeFunction(meters), MoreExecutors.directExecutor());

        final var addMetersBulkFuture = Futures.transform(commonResult, MeterUtil.METER_ADD_TRANSFORM,
            MoreExecutors.directExecutor());

        return input.getBarrierAfter()
            ? BarrierUtil.chainBarrier(addMetersBulkFuture, input.getNode(), sendBarrier,
                MeterUtil.METER_ADD_COMPOSING_TRANSFORM)
            : addMetersBulkFuture;
    }

    private static MeterRef createMeterRef(final NodeRef nodeRef, final Meter batchMeter) {
        return MeterUtil.buildMeterPath(((DataObjectIdentifier<Node>) nodeRef.getValue()).toLegacy(),
            batchMeter.getMeterId());
    }
}
