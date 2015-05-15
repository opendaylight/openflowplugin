/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.callback;


import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.services.RequestContextUtil;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author joe
 */
public class BaseCallback<I, O> implements FutureCallback<RpcResult<I>> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseCallback.class);

    private final DeviceContext deviceContext;
    private final RequestContext<O> requestContext;
    private final ListenableFuture<RpcResult<I>> futureResultFromOfLib;

    public BaseCallback(final DeviceContext deviceContext, final RequestContext<O> requestContext, final ListenableFuture<RpcResult<I>> futureResultFromOfLib) {
        this.deviceContext = Preconditions.checkNotNull(deviceContext);
        this.requestContext = Preconditions.checkNotNull(requestContext);
        this.futureResultFromOfLib = Preconditions.checkNotNull(futureResultFromOfLib);
    }

    protected final RequestContext<O> getRequestContext() {
        return requestContext;
    }

    @Override
    public void onSuccess(final RpcResult<I> fRpcResult) {
        if (!fRpcResult.isSuccessful()) {
            deviceContext.getMessageSpy().spyMessage(getRequestContext().getClass(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_FAILURE);

            // remove current request from request cache in deviceContext
            deviceContext.unhookRequestCtx(getRequestContext().getXid());

            // handle requestContext failure
            if (LOG.isTraceEnabled()) {
                StringBuilder rpcErrors = new StringBuilder();
                if (null != fRpcResult.getErrors() && fRpcResult.getErrors().size() > 0) {
                    for (RpcError error : fRpcResult.getErrors()) {
                        rpcErrors.append(error.getMessage());
                            LOG.trace("Errors from rpc result.. ",error);
                    }
                }
                LOG.trace("OF Java result for XID {} was not successful. Errors : {}", getRequestContext().getXid().getValue(), rpcErrors.toString());
            
            }

            getRequestContext().getFuture().set(
                    RpcResultBuilder.<O>failed().withRpcErrors(fRpcResult.getErrors()).build());
            RequestContextUtil.closeRequstContext(getRequestContext());
        } else {
            // else: message was successfully sent - waiting for callback on requestContext.future to get invoked
            // or can be implemented specific processing via processSuccess() method
            deviceContext.getMessageSpy().spyMessage(getRequestContext().getClass(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_SUCCESS);
            processSuccess(fRpcResult);
        }
    }


    @Override
    public void onFailure(final Throwable throwable) {
        deviceContext.unhookRequestCtx(getRequestContext().getXid());

        if (futureResultFromOfLib.isCancelled()) {
            deviceContext.getMessageSpy().spyMessage(getRequestContext().getClass(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_SUCCESS_NO_RESPONSE);

            LOG.trace("Asymmetric message - no response from OF Java expected for XID {}. Closing as successful.", getRequestContext().getXid().getValue());
            getRequestContext().getFuture().set(RpcResultBuilder.<O>success().build());
        } else {
            deviceContext.getMessageSpy().spyMessage(getRequestContext().getClass(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_ERROR);
            LOG.trace("Exception occured while processing OF Java response for XID {}.", getRequestContext().getXid().getValue(), throwable);
            getRequestContext().getFuture().set(
                    RpcResultBuilder.<O>failed()
                            .withError(RpcError.ErrorType.APPLICATION, "OF JAVA operation failed.", throwable)
                            .build());
        }

        RequestContextUtil.closeRequstContext(getRequestContext());
    }

    protected void processSuccess(final RpcResult<I> fRpcResult) {
        //should be override in child class where is awaited processing of
        //successfull future (e. g. transformation)
    }
}
