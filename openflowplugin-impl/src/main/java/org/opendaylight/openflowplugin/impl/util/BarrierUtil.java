/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import com.google.common.base.Function;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * provides barrier message chaining and factory methods
 */
public final class BarrierUtil {


    private BarrierUtil() {
        throw new IllegalStateException("This class should not be instantiated.");
    }


    /**
     * chain a barrier message - regardless of previous result and use given {@link Function} to combine
     * original result and barrier result
     *
     * @param <T>                type of input future
     * @param input              future to chain barrier to
     * @param nodeRef            target device
     * @param transactionService barrier service
     * @param compositeTransform
     * @return future holding both results (input and of the barrier)
     */
    public static <T> ListenableFuture<RpcResult<T>> chainBarrier(
            final ListenableFuture<RpcResult<T>> input, final NodeRef nodeRef,
            final FlowCapableTransactionService transactionService,
            final Function<Pair<RpcResult<T>, RpcResult<Void>>, RpcResult<T>> compositeTransform) {
        final MutablePair<RpcResult<T>, RpcResult<Void>> resultPair = new MutablePair<>();

        // store input result and append barrier
        final ListenableFuture<RpcResult<Void>> barrierResult = Futures.transform(input,
                new AsyncFunction<RpcResult<T>, RpcResult<Void>>() {
                    @Override
                    public ListenableFuture<RpcResult<Void>> apply(@Nullable final RpcResult<T> interInput) throws Exception {
                        resultPair.setLeft(interInput);
                        final SendBarrierInput barrierInput = createSendBarrierInput(nodeRef);
                        return JdkFutureAdapters.listenInPoolThread(transactionService.sendBarrier(barrierInput));
                    }
                });
        // store barrier result and return initiated pair
        final ListenableFuture<Pair<RpcResult<T>, RpcResult<Void>>> compositeResult = Futures.transform(
                barrierResult, new Function<RpcResult<Void>, Pair<RpcResult<T>, RpcResult<Void>>>() {
                    @Nullable
                    @Override
                    public Pair<RpcResult<T>, RpcResult<Void>> apply(@Nullable final RpcResult<Void> input) {
                        resultPair.setRight(input);
                        return resultPair;
                    }
                });
        // append assembling transform to barrier result
        return Futures.transform(compositeResult, compositeTransform);
    }

    /**
     * @param nodeRef rpc routing context
     * @return input for {@link FlowCapableTransactionService#sendBarrier(SendBarrierInput)}
     */
    public static SendBarrierInput createSendBarrierInput(final NodeRef nodeRef) {
        return new SendBarrierInputBuilder()
                .setNode(nodeRef)
                .build();
    }
}
