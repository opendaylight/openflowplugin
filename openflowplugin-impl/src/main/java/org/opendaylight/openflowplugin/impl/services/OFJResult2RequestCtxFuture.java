/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
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
        Futures.addCallback(futureResultFromOfLib, new FutureCallback<RpcResult<F>>() {
            @Override
            public void onSuccess(final RpcResult<F> fRpcResult) {
                if (!fRpcResult.isSuccessful()) {
                    deviceContext.getMessageSpy().spyMessage(requestContext.getClass(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_FAILURE);

                    // remove current request from request cache in deviceContext
                    deviceContext.unhookRequestCtx(requestContext.getXid());
                    // handle requestContext failure
                    StringBuilder rpcErrors = new StringBuilder();
                    if (null != fRpcResult.getErrors() && fRpcResult.getErrors().size() > 0) {
                        for (RpcError error : fRpcResult.getErrors()) {
                            rpcErrors.append(error.getMessage());
                        }
                    }
                    LOG.trace("OF Java result for XID {} was not successful. Errors : {}", requestContext.getXid().getValue(), rpcErrors.toString());
                    requestContext.getFuture().set(
                            RpcResultBuilder.<T>failed().withRpcErrors(fRpcResult.getErrors()).build());
                    RequestContextUtil.closeRequstContext(requestContext);
                }
                // else: message was successfully sent - waiting for callback on requestContext.future to get invoked
            }

            @Override
            public void onFailure(final Throwable throwable) {
                if (futureResultFromOfLib.isCancelled()) {
                    deviceContext.getMessageSpy().spyMessage(requestContext.getClass(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS);

                    LOG.trace("Asymmetric message - no response from OF Java expected for XID {}. Closing as successful.", requestContext.getXid().getValue());
                    requestContext.getFuture().set(RpcResultBuilder.<T>success().build());
                } else {
                    deviceContext.getMessageSpy().spyMessage(requestContext.getClass(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_FAILURE);
                    deviceContext.unhookRequestCtx(requestContext.getXid());
                    LOG.trace("Exception occured while processing OF Java response for XID {}.", requestContext.getXid().getValue(), throwable);
                    requestContext.getFuture().set(
                            RpcResultBuilder.<T>failed()
                                    .withError(RpcError.ErrorType.APPLICATION, "", "Flow translation to OF JAVA failed.")
                                    .build());
                }

                RequestContextUtil.closeRequstContext(requestContext);
            }
        });
    }
}