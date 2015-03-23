/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import java.util.Stack;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;

public class RpcManagerImpl implements RpcManager {

    /**
     * Number of existing RPC contexts
     */
    private int counter;

    // TODO: add field deviceContext

    /**
     * Collection of all rpc contexts which are available in rpc manager
     */
    private final Stack<RpcContext> rpcContexts = new Stack<>();

    public RpcManagerImpl() {
    }

    @Override
    public void deviceConnected(final RequestContext requestContext) {
        final RpcContext rpcContext = new RpcContextImpl();
        // TODO create new RpcContext
        // TODO increase number of context
    }

}
