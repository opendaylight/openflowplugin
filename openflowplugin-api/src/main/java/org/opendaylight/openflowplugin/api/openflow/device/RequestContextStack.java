/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 1.4.2015.
 */
public interface RequestContextStack {

    <T extends DataObject> void forgetRequestContext(RequestContext<T> requestContext);

    /**
     * Method adds request to request queue which has limited quota. After number of requests exceeds quota limit future
     * will be done immediately and will contain information about exceeded request quota.
     *
     * @param data
     */
    <T extends DataObject> SettableFuture<RpcResult<T>> storeOrFail(RequestContext<T> data);

    /**
     * Method returns new request context for current request.
     *
     * @return
     */
    <T extends DataObject> RequestContext<T> createRequestContext();

}
