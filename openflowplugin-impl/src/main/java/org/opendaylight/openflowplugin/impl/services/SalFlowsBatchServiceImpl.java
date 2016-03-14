/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev160314.AddFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev160314.AddFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev160314.RemoveFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev160314.RemoveFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev160314.RemoveFlowsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev160314.SalFlowsBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev160314.UpdateFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev160314.UpdateFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev160314.batch.flow.output.list.grouping.BatchFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev160314.batch.flow.output.list.grouping.BatchFlowsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev160314.remove.flows.batch.input.BatchFlows;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SalFlowsBatchServiceImpl implements SalFlowsBatchService {
    private static final Logger LOG = LoggerFactory.getLogger(SalFlowsBatchServiceImpl.class);

    private final DeviceContext deviceContext;
    private final SalFlowService salFlowService;

    public SalFlowsBatchServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
                                    final SalFlowService salFlowService) {
        this.deviceContext = deviceContext;
        this.salFlowService = Preconditions.checkNotNull(salFlowService);
    }

    @Override
    public Future<RpcResult<RemoveFlowsBatchOutput>> removeFlowsBatch(final RemoveFlowsBatchInput input) {
        final ArrayList<ListenableFuture<RpcResult<RemoveFlowOutput>>> resultsLot = new ArrayList<>();
        for (BatchFlows batchFlow : input.getBatchFlows()) {
            final RemoveFlowInput removeFlowInput = new RemoveFlowInputBuilder(batchFlow).build();
            resultsLot.add(JdkFutureAdapters.listenInPoolThread(salFlowService.removeFlow(removeFlowInput)));
        }

        Futures.transform(Futures.allAsList(resultsLot),
                this.<RemoveFlowsBatchOutput>createFunction(input.getBatchFlows()));
    }

    @Override
    public Future<RpcResult<AddFlowsBatchOutput>> addFlowsBatch(final AddFlowsBatchInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<UpdateFlowsBatchOutput>> updateFlowsBatch(final UpdateFlowsBatchInput input) {
        return null;
    }

    protected <O extends DataObject> Function<List<RpcResult<O>>, RpcResult<RemoveFlowsBatchOutput>> createFunction(
            final List<BatchFlows> inputBatchFlows) {
        return new Function<List<RpcResult<O>>, RpcResult<RemoveFlowsBatchOutput>>() {
            @Nullable
            @Override
            public RpcResult<RemoveFlowsBatchOutput> apply(@Nullable final List<RpcResult<O>> innerInput) {
                final int sizeOfFutures = innerInput.size();
                final int sizeOfInputBatch = inputBatchFlows.size();
                Preconditions.checkArgument(sizeOfFutures == sizeOfInputBatch,
                        "wrong amount of returned futures: {} <> {}", sizeOfFutures, sizeOfInputBatch);

                final ArrayList<BatchFlowsOutput> batchFlows = new ArrayList<>();
                final Iterator<BatchFlows> batchFlowIterator = inputBatchFlows.iterator();

                for (RpcResult<O> removeFlowOutput : innerInput) {
                    final FlowId flowId = batchFlowIterator.next().getFlowId();

                    //TODO: store errors
                    batchFlows.add(
                            new BatchFlowsOutputBuilder()
                                    .setFlowId(flowId)
                                    .setSuccess(removeFlowOutput.isSuccessful())
                                    .build());
                }

                final RemoveFlowsBatchOutput batchOutput = new RemoveFlowsBatchOutputBuilder()
                        .setBatchFlowsOutput(batchFlows).build();
                return RpcResultBuilder.success(batchOutput).build();
            }
        };
    }
}
