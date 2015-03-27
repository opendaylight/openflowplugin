/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services;

import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Martin Bobak <mbobak@cisco.com> on 26.3.2015.
 */
public class RpcResultConvertor<T extends DataObject> {

    private final RequestContext requestContext;

    public RpcResultConvertor(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    public void processResultFromOfJava(final Future<RpcResult<Void>> futureResultFromOfLib,
                                        final long waitTime) {
        try {
            final RpcResult<Void> rpcResult = futureResultFromOfLib.get(waitTime, TimeUnit.MILLISECONDS);
            if (!rpcResult.isSuccessful()) {
                requestContext.getFuture().set(RpcResultBuilder.<T>failed().withRpcErrors(rpcResult.getErrors()).build());
                requestContext.close();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            requestContext.getFuture().set(RpcResultBuilder
                    .<T>failed()
                    .withError(RpcError.ErrorType.APPLICATION, "",
                            "Flow modification on device wasn't successfull.").build());
            requestContext.close();
        } catch (final Exception e) {
            requestContext.getFuture().set(RpcResultBuilder.<T>failed()
                    .withError(RpcError.ErrorType.APPLICATION, "", "Flow translation to OF JAVA failed.").build());
            requestContext.close();
        }
    }

}
