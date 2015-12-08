/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl.util;

import com.google.common.base.Function;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * utility methods for group reconcil task (future chaining, transforms)
 */
public class ReconcileUtil {

    /** general future transformer from list of addGroup results to single void result */
    public static final Function<List<RpcResult<AddGroupOutput>>, RpcResult<Void>> CONDENSE_GROUP_ADDS_TO_VOID =
            new Function<List<RpcResult<AddGroupOutput>>, RpcResult<Void>>() {
                @Nullable
                @Override
                public RpcResult<Void> apply(@Nullable final List<RpcResult<AddGroupOutput>> input) {
                    final RpcResultBuilder<Void> resultSink;
                    if (input != null) {
                        List<RpcError> errors = new ArrayList<>();
                        for (RpcResult<AddGroupOutput> addGroupOutputRpcResult : input) {
                            if (!addGroupOutputRpcResult.isSuccessful()) {
                                errors.addAll(addGroupOutputRpcResult.getErrors());
                            }
                        }
                        if (errors.isEmpty()) {
                            resultSink = RpcResultBuilder.success();
                        } else {
                            resultSink = RpcResultBuilder.<Void>failed().withRpcErrors(errors);
                        }
                    } else {
                        resultSink = RpcResultBuilder.<Void>failed()
                                .withError(RpcError.ErrorType.APPLICATION, "previous group pushing failed");

                    }

                    return resultSink.build();
                }
            };

    /** general future transformer from list of addMeter results to single void result */
    public static final Function<List<RpcResult<AddMeterOutput>>, RpcResult<Void>> CONDENSE_METER_ADDS_TO_VOID =
            new Function<List<RpcResult<AddMeterOutput>>, RpcResult<Void>>() {
                @Nullable
                @Override
                public RpcResult<Void> apply(@Nullable final List<RpcResult<AddMeterOutput>> input) {
                    final RpcResultBuilder<Void> resultSink;
                    if (input != null) {
                        List<RpcError> errors = new ArrayList<>();
                        for (RpcResult<AddMeterOutput> addGroupOutputRpcResult : input) {
                            if (!addGroupOutputRpcResult.isSuccessful()) {
                                errors.addAll(addGroupOutputRpcResult.getErrors());
                            }
                        }
                        if (errors.isEmpty()) {
                            resultSink = RpcResultBuilder.success();
                        } else {
                            resultSink = RpcResultBuilder.<Void>failed().withRpcErrors(errors);
                        }
                    } else {
                        resultSink = RpcResultBuilder.<Void>failed()
                                .withError(RpcError.ErrorType.APPLICATION, "previous meter pushing failed");

                    }

                    return resultSink.build();
                }
            };

    /** general future transformer from list of addFlow results to single void result */
    public static final Function<List<RpcResult<AddFlowOutput>>, RpcResult<Void>> CONDENSE_FLOW_ADDS_TO_VOID =
            new Function<List<RpcResult<AddFlowOutput>>, RpcResult<Void>>() {
                @Nullable
                @Override
                public RpcResult<Void> apply(@Nullable final List<RpcResult<AddFlowOutput>> input) {
                    final RpcResultBuilder<Void> resultSink;
                    if (input != null) {
                        List<RpcError> errors = new ArrayList<>();
                        for (RpcResult<AddFlowOutput> addGroupOutputRpcResult : input) {
                            if (!addGroupOutputRpcResult.isSuccessful()) {
                                errors.addAll(addGroupOutputRpcResult.getErrors());
                            }
                        }
                        if (errors.isEmpty()) {
                            resultSink = RpcResultBuilder.success();
                        } else {
                            resultSink = RpcResultBuilder.<Void>failed().withRpcErrors(errors);
                        }
                    } else {
                        resultSink = RpcResultBuilder.<Void>failed()
                                .withError(RpcError.ErrorType.APPLICATION, "previous flow pushing failed");

                    }

                    return resultSink.build();
                }
            };


    /**
     * @param nodeIdent flow capable node path - target device for routed rpc
     * @param flowCapableTransactionService barrier rpc service
     * @return async barrier result
     */
    public static AsyncFunction<RpcResult<Void>, RpcResult<Void>> chainBarrierFlush(
            final InstanceIdentifier<FlowCapableNode> nodeIdent,
            final FlowCapableTransactionService flowCapableTransactionService) {
        return new AsyncFunction<RpcResult<Void>, RpcResult<Void>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final RpcResult<Void> input) throws Exception {
                final SendBarrierInput barrierInput = new SendBarrierInputBuilder()
                        .setNode(new NodeRef(nodeIdent))
                        .build();
                return JdkFutureAdapters.listenInPoolThread(flowCapableTransactionService.sendBarrier(barrierInput));
            }
        };
    }
}
