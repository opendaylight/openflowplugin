/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.util;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;

public final class RequestContextUtil {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RequestContextUtil.class);

    private RequestContextUtil() {
        throw new UnsupportedOperationException();
    }


    public static <T> ListenableFuture<RpcResult<T>> closeRequestContextWithRpcError(final RequestContext<T> requestContext, final String errorMessage) {
        RpcResultBuilder<T> rpcResultBuilder = RpcResultBuilder.<T>failed().withRpcError(RpcResultBuilder.newError(RpcError.ErrorType.APPLICATION, "", errorMessage));
        requestContext.setResult(rpcResultBuilder.build());
        closeRequestContext(requestContext);
        return requestContext.getFuture();
    }

    public static void closeRequestContext(final RequestContext<?> requestContext) {
        try {
            requestContext.close();
        } catch (Exception e) {
            LOG.error("Request context failed to close", e);
        }
    }
}
