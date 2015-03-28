/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceContextReadyHandler;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * This interface is responsible for instantiating DeviceContext and
 * registering transaction chain for each DeviceContext. Each device
 * has its own device context managed by this manager.
 * <p>
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 25.2.2015.
 */
public interface DeviceManager extends DeviceConnectedHandler {


    /**
     * Method allows to send message that will be using RequestContext
     * wrapped by this context.
     *
     * @param dataObject
     */
    void sendMessage(DataObject dataObject, RequestContext requestContext);

    /**
     * Method allows to send rpc request that will be using RequestContext
     * wrapped by this context.
     *
     * @param dataObject
     */
    Xid sendRequest(DataObject dataObject, RequestContext requestContext);

    /**
     * Method registers handler responsible for handling operations related to connected device after
     * request context is created.
     *
     * @param deviceContextReadyHandler
     */
    public void addRequestContextReadyHandler(DeviceContextReadyHandler deviceContextReadyHandler);

}
