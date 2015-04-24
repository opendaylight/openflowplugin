/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.callback;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.yangtools.yang.common.RpcResult;

public abstract class SuccessCallback<I, O> extends BaseCallback<I, O> {

    public SuccessCallback(DeviceContext deviceContext, RequestContext<O> requestContext,
            ListenableFuture<RpcResult<I>> futureResultFromOfLib) {
        super(deviceContext, requestContext, futureResultFromOfLib);
    }

    protected void processSuccess(final RpcResult<I> rpcResult) {
        requestContext.getFuture().set(transform(rpcResult));
    }


    abstract public RpcResult<O> transform(RpcResult<I> rpcResult);
}
