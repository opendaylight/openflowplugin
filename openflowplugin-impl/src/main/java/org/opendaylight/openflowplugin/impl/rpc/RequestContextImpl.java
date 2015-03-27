/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * @author joe
 */
public class RequestContextImpl<T extends DataObject> implements RequestContext<T> {

    private final RpcContext rpcContext;
    private SettableFuture<RpcResult<T>> rpcResultFuture;

    public RequestContextImpl(RpcContext rpcContext) {
        this.rpcContext = rpcContext;
    }

    @Override
    public void close() {
        rpcContext.forgetRequestContext(this);
    }

    @Override
    public SettableFuture<RpcResult<T>> getFuture() {
        if (null == rpcResultFuture) {
            rpcResultFuture = SettableFuture.create();
        }
        return rpcResultFuture;
    }
}
