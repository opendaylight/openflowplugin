/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.rpc;

import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.yangtools.yang.binding.RpcService;

/**
 * This context is registered with MD-SAL as a routed RPC provider for the inventory node backed by this switch and
 * tracks the state of any user requests and how they map onto protocol requests. It uses
 * {@link org.opendaylight.openflowplugin.api.openflow.device.RequestContext} to perform requests.
 * <p>
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 25.2.2015.
 */
public interface RpcContext extends RequestContextStack, AutoCloseable {
    <S extends RpcService> void registerRpcServiceImplementation(Class<S> serviceClass, S serviceInstance);

    <S extends RpcService> S lookupRpcService(Class<S> serviceClass);
    <S extends RpcService> void unregisterRpcServiceImplementation(Class<S> serviceClass);
}
