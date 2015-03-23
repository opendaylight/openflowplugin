/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.rpc;

import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * This context is registered with MD-SAL as a routed RPC provider for the inventory node backed by this switch and
 * tracks the state of any user requests and how they map onto protocol requests. It uses
 * {@link org.opendaylight.openflowplugin.api.openflow.device.RequestContext} to perform requests.
 * <p/>
 * Created by Martin Bobak <mbobak@cisco.com> on 25.2.2015.
 */
public interface RpcContext extends AutoCloseable {

    <S extends RpcService> void registerRpcServiceImplementation(Class<S> serviceClass, S serviceInstance);

    void setDeviceContext(DeviceContext deviceContext);

    /**
     * Method adds request to request queue which has limited quota. After number of requests exceeds quota limit
     * {@link org.opendaylight.openflowplugin.api.openflow.device.exception.RequestQuotaExceededException} is thrown.
     * 
     * @param result
     *            resulting future
     */
    void addNewRequest(Future<RpcResult<? extends DataObject>> result);

    /**
     * Method for setting request quota value. When the Request Context quota is exceeded, incoming RPCs fail
     * immediately, with a well-defined error.
     * 
     * @param maxRequestsPerDevice
     */
    void setRequestContextQuota(int maxRequestsPerDevice);

    boolean isRequestContextCapacityEmpty();

}
