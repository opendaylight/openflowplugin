/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;

import com.google.common.base.Function;
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
 *
 */
public class ServiceCallProcessingUtil {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ServiceCallProcessingUtil.class);

    static <T extends DataObject, F>  Future<RpcResult<T>> handleServiceCall(final RpcContext rpcContext,
final BigInteger connectionID, final DeviceContext deviceContext, final Function<BigInteger,Future<RpcResult<F>>> function) {
        LOG.debug("Calling the FlowMod RPC method on MessageDispatchService");
        // use primary connection

        final RequestContext<T> requestContext = rpcContext.createRequestContext();
        final SettableFuture<RpcResult<T>> result = rpcContext.storeOrFail(requestContext);

        if (!result.isDone()) {
            final Future<RpcResult<F>> resultFromOFLib = function.apply(connectionID);

            final RpcResultConvertor<T> rpcResultConvertor = new RpcResultConvertor<>(requestContext, deviceContext);
            rpcResultConvertor.processResultFromOfJava(resultFromOFLib);

        } else {
            RequestContextUtil.closeRequstContext(requestContext);
        }
        return result;
    }
}
