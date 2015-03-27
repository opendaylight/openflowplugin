/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import java.math.BigInteger;
import java.util.concurrent.Future;

/**
 * @author joe
 */
public class ServiceCallProcessingUtil {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ServiceCallProcessingUtil.class);

    static <T extends DataObject> Future<RpcResult<T>> handleServiceCall(final RpcContext rpcContext,
                                                                         final BigInteger connectionID, final long waitTime, final Function function) {
        LOG.debug("Calling the FlowMod RPC method on MessageDispatchService");
        // use primary connection

        final RequestContext requestContext = rpcContext.createRequestContext();
        final SettableFuture<RpcResult<T>> result = rpcContext.storeOrFail(requestContext);

        if (!result.isDone()) {
            final Future<RpcResult<Void>> resultFromOFLib = function.apply(connectionID);

            final RpcResultConvertor<T> rpcResultConvertor = new RpcResultConvertor<>(requestContext);
            rpcResultConvertor.processResultFromOfJava(resultFromOFLib, waitTime);

        } else {
            requestContext.close();
        }
        return result;
    }
}
