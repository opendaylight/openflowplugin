/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Provides barrier message chaining and factory methods.
 */
public final class BarrierUtil {
    private BarrierUtil() {
        // Hidden on purpose
    }

    /**
     * Chain a barrier message - regardless of previous result and use given {@link Function} to combine
     * original result and barrier result.
     *
     * @param <T>                type of input future
     * @param input              future to chain barrier to
     * @param nodeRef            target device
     * @param sendBarrier        barrier service
     * @param compositeTransform composite transform
     * @return future holding both results (input and of the barrier)
     */
    public static <T> ListenableFuture<RpcResult<T>> chainBarrier(final ListenableFuture<RpcResult<T>> input,
            final NodeRef nodeRef, final SendBarrier sendBarrier,
            final Function<Pair<RpcResult<T>, RpcResult<SendBarrierOutput>>, RpcResult<T>> compositeTransform) {
        final var resultPair = new MutablePair<RpcResult<T>, RpcResult<SendBarrierOutput>>();

        // store input result and append barrier
        final var barrierResult = Futures.transformAsync(input, interInput -> {
            resultPair.setLeft(interInput);
            return sendBarrier.invoke(createSendBarrierInput(nodeRef));
        }, MoreExecutors.directExecutor());
        // store barrier result and return initiated pair
        final var compositeResult = Futures.transform(barrierResult, input1 -> {
            resultPair.setRight(input1);
            return resultPair;
        }, MoreExecutors.directExecutor());
        // append assembling transform to barrier result
        return Futures.transform(compositeResult, compositeTransform, MoreExecutors.directExecutor());
    }

    /**
     * Creates barrier input.
     *
     * @param nodeRef rpc routing context
     * @return input for {@link SendBarrier#invoke(SendBarrierInput)}
     */
    @VisibleForTesting
    static SendBarrierInput createSendBarrierInput(final NodeRef nodeRef) {
        return new SendBarrierInputBuilder().setNode(nodeRef).build();
    }
}
