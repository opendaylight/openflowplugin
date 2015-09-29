/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowplugin.api.ConnectionException;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 *
 */
public final class RpcResultUtil {

    private RpcResultUtil() {
        throw new UnsupportedOperationException("RpcResultUtil is not expected to be instantiated.");
    }

    /**
     * @param e connection exception
     * @param <T> rpc result return type
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

}
