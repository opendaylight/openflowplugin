/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opendaylight.controller.sal.common.util.RpcErrors;
import org.opendaylight.controller.sal.common.util.Rpcs;
import org.opendaylight.openflowplugin.ConnectionException;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import com.google.common.util.concurrent.SettableFuture;

/**
 * 
 */
public final class RpcResultUtil {

    private RpcResultUtil(){
        throw new AssertionError("RpcResultUtil is not expected to be instantiated.");
    }

    /**
     * @param e
     * @return error wrapped inside {@link RpcResult} which is wrapped inside future
     */
    public static <T> SettableFuture<RpcResult<T>> getRpcErrorFuture(ConnectionException e) {
        List<RpcError> rpcErrorList = wrapConnectionErrorIntoRpcErrors(e);
        SettableFuture<RpcResult<T>> futureWithError = SettableFuture.create();
        futureWithError.set(RpcResultBuilder.<T>failed().withRpcErrors(rpcErrorList).build());
        return futureWithError;
    }

    private static List<RpcError> wrapConnectionErrorIntoRpcErrors(ConnectionException e) {
        List<RpcError> rpcErrorList = new ArrayList<>();
        rpcErrorList.add(RpcResultBuilder.newError(
                RpcError.ErrorType.TRANSPORT, 
                OFConstants.ERROR_TAG_TIMEOUT, 
                e.getMessage(),
                OFConstants.APPLICATION_TAG,
                IMessageDispatchService.CONNECTION_ERROR_MESSAGE, 
                e.getCause()));
        return rpcErrorList;
    }

//    /**
//     * @param success
//     * @param result
//     * @param errorsArg
//     * @return assembled {@link RpcResult}
//     */
//    public static <T> RpcResult<T> createRpcResult(boolean success, T result, Collection<RpcError> errorsArg) {
//        RpcResultBuilder<T> rpcResult = RpcResultBuilder.<T>status(success).withResult(result).withRpcErrors(errorsArg);
//        return rpcResult.build();
//    }
}
