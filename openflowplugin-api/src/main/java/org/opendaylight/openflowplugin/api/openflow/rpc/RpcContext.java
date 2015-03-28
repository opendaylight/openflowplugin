/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.rpc;

import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * This context is registered with MD-SAL as a routed RPC provider for the inventory node backed by this switch and
 * tracks the state of any user requests and how they map onto protocol requests. It uses
 * {@link org.opendaylight.openflowplugin.api.openflow.device.RequestContext} to perform requests.
 * <p>
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 25.2.2015.
 */
public interface RpcContext extends AutoCloseable {

    <S extends RpcService> void registerRpcServiceImplementation(Class<S> serviceClass, S serviceInstance);

    /**
     * Method adds request to request queue which has limited quota. After number of requests exceeds quota limit future
     * will be done immediately and will contain information about exceeded request quota.
     * 
     * @param data
     */
    <T extends DataObject> SettableFuture<RpcResult<T>> storeOrFail(RequestContext<T> data);

    /**
     * Method for setting request quota value. When the Request Context quota is exceeded, incoming RPCs fail
     * immediately, with a well-defined error.
     * 
     * @param maxRequestsPerDevice
     */
    void setRequestContextQuota(int maxRequestsPerDevice);

    <T extends DataObject> void forgetRequestContext(RequestContext<T> requestContext);

    /**
     * Method provides device context.
     * 
     * @return
     */
    DeviceContext getDeviceContext();

    /**
     * Method returns new request context for current request.
     * 
     * @return
     */
    <T extends DataObject> RequestContext<T> createRequestContext();

}
