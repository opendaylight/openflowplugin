/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services;

import org.opendaylight.openflowplugin.impl.callback.BaseCallback;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 26.3.2015.
 */
public class OFJResult2RequestCtxFuture<T> {

    private static final Logger LOG = LoggerFactory.getLogger(OFJResult2RequestCtxFuture.class);
    private final RequestContext<T> requestContext;
    private final DeviceContext deviceContext;

    public OFJResult2RequestCtxFuture(final RequestContext<T> requestContext, final DeviceContext deviceContext) {
        this.requestContext = requestContext;
        this.deviceContext = deviceContext;
    }

    public <F> void processResultFromOfJava(final ListenableFuture<RpcResult<F>> futureResultFromOfLib) {
        Futures.addCallback(futureResultFromOfLib, new BaseCallback<>(deviceContext, requestContext, futureResultFromOfLib));
                deviceContext.unhookRequestCtx(requestContext.getXid());
    }
}