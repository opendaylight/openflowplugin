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
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yangtools.yang.common.RpcResult;

public abstract class AbstractRequestContext<T> implements RequestContext<T> {
    private SettableFuture<RpcResult<T>> rpcResultFuture;
    private long waitTimeout;
    private final Xid xid;

    protected AbstractRequestContext(final Long xid) {
        this.xid = new Xid(xid);
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
    public long getWaitTimeout() {
        return waitTimeout;
    }

    @Override
    public void setWaitTimeout(final long waitTimeout) {
        this.waitTimeout = waitTimeout;
    }
}
