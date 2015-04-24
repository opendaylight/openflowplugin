/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.callback;


import org.opendaylight.openflowplugin.impl.services.RequestContextUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import com.google.common.util.concurrent.FutureCallback;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * @author joe
 *
 */
public class BaseCallback<I, O> implements FutureCallback<RpcResult<I>> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseCallback.class);

    protected DeviceContext deviceContext;
    protected RequestContext<O> requestContext;
    private ListenableFuture<RpcResult<I>> futureResultFromOfLib;

    public BaseCallback(final DeviceContext deviceContext, final RequestContext<O> requestContext, final ListenableFuture<RpcResult<I>> futureResultFromOfLib) {
        this.deviceContext = deviceContext;
        this.requestContext = requestContext;
        this.futureResultFromOfLib = futureResultFromOfLib;
    }

    @Override
    public void onSuccess(final RpcResult<I> fRpcResult) {
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
                    RpcResultBuilder.<O>failed().withRpcErrors(fRpcResult.getErrors()).build());
            RequestContextUtil.closeRequstContext(requestContext);
        } else {
            // else: message was successfully sent - waiting for callback on requestContext.future to get invoked
            // or can be implemented specific processing via processSuccess() method
            processSuccess(fRpcResult);
        }
    }


    @Override
    public void onFailure(final Throwable throwable) {
        deviceContext.unhookRequestCtx(requestContext.getXid());

        if (futureResultFromOfLib.isCancelled()) {
            deviceContext.getMessageSpy().spyMessage(requestContext.getClass(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS);

            LOG.trace("Asymmetric message - no response from OF Java expected for XID {}. Closing as successful.", requestContext.getXid().getValue());
            requestContext.getFuture().set(RpcResultBuilder.<O>success().build());
        } else {
            deviceContext.getMessageSpy().spyMessage(requestContext.getClass(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_FAILURE);
            LOG.trace("Exception occured while processing OF Java response for XID {}.", requestContext.getXid().getValue(), throwable);
            requestContext.getFuture().set(
                    RpcResultBuilder.<O>failed()
                            .withError(RpcError.ErrorType.APPLICATION, "OF JAVA operation failed.", throwable)
                            .build());
        }

        RequestContextUtil.closeRequstContext(requestContext);
    }

    protected void processSuccess(final RpcResult<I> fRpcResult) {
        //should be override in child class where is awaited processing of
        //successfull future (e. g. transformation)
    }

}
