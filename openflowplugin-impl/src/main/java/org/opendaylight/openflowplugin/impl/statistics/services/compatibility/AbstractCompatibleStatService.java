/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.compatibility;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.statistics.compatibility.BackwardCompatibleAtomicService;
import org.opendaylight.openflowplugin.impl.services.AbstractMultipartService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Created by mirehak on 9/7/15.
 */
public abstract class AbstractCompatibleStatService<I, O, N extends Notification> extends AbstractMultipartService<I> implements BackwardCompatibleAtomicService<I, O> {
    private final AtomicLong compatibilityXidSeed;

    private Function<RpcResult<List<MultipartReply>>, N> notificationCallback = new Function<RpcResult<List<MultipartReply>>, N>() {
        @Nullable
        @Override
        public N apply(RpcResult<List<MultipartReply>> input) {
            if (input != null && input.isSuccessful()) {
                return transformToNotification(input.getResult());
            } else {
                return null;
            }
        }
    };

    public AbstractCompatibleStatService(RequestContextStack requestContextStack, DeviceContext deviceContext, AtomicLong compatibilityXidSeed) {
        super(requestContextStack, deviceContext);
        this.compatibilityXidSeed = compatibilityXidSeed;
    }

    @Override
    public ListenableFuture<RpcResult<O>> handleAndNotify(final I input, final NotificationPublishService notificationPublishService) {
        // prepare emulated xid
        final long emulatedXid = compatibilityXidSeed.incrementAndGet();
        final TransactionId emulatedTxId = new TransactionId(BigInteger.valueOf(emulatedXid));

        // do real processing
        final ListenableFuture<RpcResult<List<MultipartReply>>> rpcResultListenableFuture = handleServiceCall(input);

        // transform rpc result (raw multipart) to notification
        final ListenableFuture<N> preNotification = Futures.transform(rpcResultListenableFuture, notificationCallback);

        // hook notification publishing
        Futures.addCallback(preNotification, new FutureCallback<N>() {
            @Override
            public void onSuccess(@Nullable N result) {
                if (result != null) {
                    notificationPublishService.offerNotification(result);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // NOOP
            }
        });

        return RpcResultBuilder.<O>success(buildTxCapableResult(emulatedTxId)).buildFuture();
    }

    public abstract O buildTxCapableResult(TransactionId emulatedTxId);

    public abstract N transformToNotification(List<MultipartReply> result);
}
