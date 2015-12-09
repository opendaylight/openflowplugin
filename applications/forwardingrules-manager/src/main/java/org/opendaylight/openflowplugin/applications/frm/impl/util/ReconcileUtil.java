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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * utility methods for group reconcil task (future chaining, transforms)
 */
public class ReconcileUtil {

    /**
     * @param previousItemAction description for case when the triggering future contains failure
     * @param <D>                type of rpc output (gathered in list)
     * @return single rpc result of type Void honoring all partial rpc results
     */
    public static <D extends DataObject> Function<List<RpcResult<D>>, RpcResult<Void>> createRpcResultCondenser(final String previousItemAction) {
        return new Function<List<RpcResult<D>>, RpcResult<Void>>() {
            @Nullable
            @Override
            public RpcResult<Void> apply(@Nullable final List<RpcResult<D>> input) {
                final RpcResultBuilder<Void> resultSink;
                if (input != null) {
                    List<RpcError> errors = new ArrayList<>();
                    for (RpcResult<D> rpcResult : input) {
                        if (!rpcResult.isSuccessful()) {
                            errors.addAll(rpcResult.getErrors());
                        }
                    }
                    if (errors.isEmpty()) {
                        resultSink = RpcResultBuilder.success();
                    } else {
                        resultSink = RpcResultBuilder.<Void>failed().withRpcErrors(errors);
                    }
                } else {
                    resultSink = RpcResultBuilder.<Void>failed()
                            .withError(RpcError.ErrorType.APPLICATION, "previous " + previousItemAction + " failed");

                }

                return resultSink.build();
            }
        };
    }

    /**
     * @param nodeIdent flow capable node path - target device for routed rpc
     * @param flowCapableTransactionService barrier rpc service
     * @return async barrier result
     */
    public static AsyncFunction<RpcResult<Void>, RpcResult<Void>> chainBarrierFlush(
            final InstanceIdentifier<Node> nodeIdent,
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
