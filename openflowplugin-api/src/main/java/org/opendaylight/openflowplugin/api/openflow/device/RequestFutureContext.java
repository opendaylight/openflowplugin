/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 25.3.2015.
 * @param <T>
 */
public interface RequestFutureContext<T> {
    /**
     * Method returns future to be used for handling device requests.
     *
     * @return
     */
    ListenableFuture<RpcResult<T>> getFuture();

    void setResult(RpcResult<T> result);
}
