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
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * @author joe
 */
public class RequestContextImpl<T> implements RequestContext<T> {

    private final RequestContextStack requestContextStack;
    private SettableFuture<RpcResult<T>> rpcResultFuture;
    private long waitTimeout;
    private Xid xid;

    public RequestContextImpl(RequestContextStack requestContextStack) {
        this.requestContextStack = requestContextStack;
    }

    @Override
    public void close() {
        requestContextStack.forgetRequestContext(this);
    }

    @Override
    public SettableFuture<RpcResult<T>> getFuture() {
        if (null == rpcResultFuture) {
            rpcResultFuture = SettableFuture.create();
        }
        return rpcResultFuture;
    }

    @Override
    public Xid getXid() {
        return xid;
    }

    @Override
    public void setXid(Xid xid) {
        this.xid = xid;
    }

    @Override
    public long getWaitTimeout() {
        return waitTimeout;
    }

    @Override
    public void setWaitTimeout(long waitTimeout) {
        this.waitTimeout = waitTimeout;
    }
}
