/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yangtools.yang.common.RpcResult;

public abstract class AbstractRequestContext<T> implements RequestContext<T> {
    private final SettableFuture<RpcResult<T>> rpcResultFuture = SettableFuture.create();
    private final Xid xid;
    private long waitTimeout;

    protected AbstractRequestContext(final Long xid) {
        this.xid = xid == null ? null : new Xid(xid);
    }

    @Override
    public final ListenableFuture<RpcResult<T>> getFuture() {
        return rpcResultFuture;
    }

    @Override
    public final void setResult(final RpcResult<T> result) {
        rpcResultFuture.set(result);
    }

    @Override
    public final Xid getXid() {
        return xid;
    }

    @Override
    public final long getWaitTimeout() {
        return waitTimeout;
    }

    @Override
    public final void setWaitTimeout(final long waitTimeout) {
        this.waitTimeout = waitTimeout;
    }
}
