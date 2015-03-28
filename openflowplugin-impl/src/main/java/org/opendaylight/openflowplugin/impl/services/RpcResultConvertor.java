/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 26.3.2015.
 */
public class RpcResultConvertor<T extends DataObject> {

    private final RequestContext<T> requestContext;
    private final DeviceContext deviceContext;

    public RpcResultConvertor(final RequestContext<T> requestContext, final DeviceContext deviceContext) {
        this.requestContext = requestContext;
        this.deviceContext = deviceContext;
    }

    public <F> void processResultFromOfJava(final Future<RpcResult<F>> futureResultFromOfLib) {
        try {
            final RpcResult<F> rpcResult = futureResultFromOfLib.get(requestContext.getWaitTimeout(), TimeUnit.MILLISECONDS);
            if (!rpcResult.isSuccessful()) {
                requestContext.getFuture().set(
                        RpcResultBuilder.<T> failed().withRpcErrors(rpcResult.getErrors()).build());
                RequestContextUtil.closeRequstContext(requestContext);
            } else {
                deviceContext.hookRequestCtx(requestContext.getXid(), requestContext);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            requestContext.getFuture().set(
                    RpcResultBuilder
                            .<T> failed()
                            .withError(RpcError.ErrorType.APPLICATION, "",
                                    "Flow modification on device wasn't successfull.").build());
            RequestContextUtil.closeRequstContext(requestContext);
        } catch (final Exception e) {
            requestContext.getFuture().set(
                    RpcResultBuilder.<T> failed()
                            .withError(RpcError.ErrorType.APPLICATION, "", "Flow translation to OF JAVA failed.")
                            .build());
            RequestContextUtil.closeRequstContext(requestContext);
        }
    }
}
