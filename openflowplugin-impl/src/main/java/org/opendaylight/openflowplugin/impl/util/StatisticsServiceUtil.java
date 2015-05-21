/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 18.5.2015.
 */
public class StatisticsServiceUtil {

    private StatisticsServiceUtil() {
        throw new IllegalStateException("This class should not be instantiated");
    }

    public static <T> ListenableFuture<RpcResult<T>> getRpcResultListenableFuture(final Xid xid,
                                                                                 final MultipartRequestInput multipartRequestInput,
                                                                                 final DeviceContext deviceContext) {
        final SettableFuture<RpcResult<T>> settableFuture = SettableFuture.create();
        final OutboundQueue outboundQueue = deviceContext.getPrimaryConnectionContext().getOutboundQueueProvider();
        outboundQueue.commitEntry(xid.getValue(), multipartRequestInput, new FutureCallback<OfHeader>() {
            @Override
            public void onSuccess(final OfHeader ofHeader) {
            }

            @Override
            public void onFailure(final Throwable throwable) {
            }
        });
        return settableFuture;
    }

}
