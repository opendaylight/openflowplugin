/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * @author joe
 */
public class RequestContextImpl<T extends DataObject> implements RequestContext {

    private final RpcContext rpcContext;
    private SettableFuture<RpcResult<T>> rpcResultFuture;

    public RequestContextImpl(final RpcContext rpcContext) {
        this.rpcContext = rpcContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.openflowplugin.api.openflow.device.RequestContext#createRequestFuture(org.opendaylight.yangtools
     * .yang.binding.DataObject)
     */
    @Override
    public Future<RpcResult<T>> createRequestFuture(final DataObject dataObject) {
        rpcResultFuture = SettableFuture.create();
        return rpcResultFuture;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opendaylight.openflowplugin.api.openflow.device.RequestContext#requestSucceeded()
     */
    @Override
    public void requestSucceeded() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opendaylight.openflowplugin.api.openflow.device.RequestContext#requestFailed(java.lang.String)
     */
    @Override
    public void requestFailed(final String exception) {
        // TODO Auto-generated method stub

    }
}
