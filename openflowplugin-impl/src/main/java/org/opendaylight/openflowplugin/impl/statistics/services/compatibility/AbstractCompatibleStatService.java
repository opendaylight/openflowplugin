/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.compatibility;

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
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.api.openflow.statistics.compatibility.BackwardCompatibleAtomicService;
import org.opendaylight.openflowplugin.impl.services.AbstractMultipartService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * pulled up common functionality of notification emitting stats services (backward compatibility relic)
 */
public abstract class AbstractCompatibleStatService<I extends DataContainer, O, N extends Notification> extends AbstractMultipartService<I, MultipartReply> implements BackwardCompatibleAtomicService<I, O> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCompatibleStatService.class);

    private final AtomicLong compatibilityXidSeed;
    private final OpenflowVersion ofVersion;

    public AbstractCompatibleStatService(RequestContextStack requestContextStack, DeviceContext deviceContext, AtomicLong compatibilityXidSeed) {
        super(requestContextStack, deviceContext);
        this.compatibilityXidSeed = compatibilityXidSeed;
        ofVersion = OpenflowVersion.get(getVersion());
    }

    public OpenflowVersion getOfVersion() {
        return ofVersion;
    }

    @Override
    public ListenableFuture<RpcResult<O>> handleAndNotify(final I input, final NotificationPublishService notificationPublishService) {
        // prepare emulated xid
        final long emulatedXid = compatibilityXidSeed.incrementAndGet();
        final TransactionId emulatedTxId = new TransactionId(BigInteger.valueOf(emulatedXid));

        // do real processing
        final ListenableFuture<RpcResult<List<MultipartReply>>> rpcResultListenableFuture = handleServiceCall(input);

        // hook notification publishing
        Futures.addCallback(rpcResultListenableFuture, new FutureCallback<RpcResult<List<MultipartReply>>>() {
            @Override
            public void onSuccess(@Nullable RpcResult<List<MultipartReply>> result) {
                if (result != null && result.isSuccessful()) {
                    // transform rpc result (raw multipart) to notification
                    final N flowNotification = transformToNotification(result.getResult(), emulatedTxId);
                    notificationPublishService.offerNotification(flowNotification);
                } else {
                    LOG.debug("compatibility callback failed - NOT emitting notification: {}", input.getClass().getSimpleName());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.debug("compatibility callback crashed - NOT emitting notification: {}", input.getClass().getSimpleName(), t);
            }
        });

        return RpcResultBuilder.<O>success(buildTxCapableResult(emulatedTxId)).buildFuture();
    }

    public abstract O buildTxCapableResult(TransactionId emulatedTxId);

    public abstract N transformToNotification(List<MultipartReply> result, TransactionId emulatedTxId);
}
