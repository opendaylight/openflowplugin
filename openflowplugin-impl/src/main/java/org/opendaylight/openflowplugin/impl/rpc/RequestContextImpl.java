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
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;
import java.util.concurrent.Future;

/**
 * @author joe
 */
public class RequestContextImpl implements RequestContext {

    private final Future<RpcResult<? extends DataObject>> result;
    private SettableFuture requestFuture;

    public RequestContextImpl(final Future<RpcResult<? extends DataObject>> result) {
        this.result = result;
    }

    @Override
    public <T extends DataObject> Future<RpcResult<T>> createRequestFuture(final DataObject dataObject) {
        requestFuture = SettableFuture.create();
        return requestFuture;
    }
}
