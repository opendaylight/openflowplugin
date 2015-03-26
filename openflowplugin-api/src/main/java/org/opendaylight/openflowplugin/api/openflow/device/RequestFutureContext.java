/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Created by Martin Bobak <mbobak@cisco.com> on 25.3.2015.
 */
public interface RequestFutureContext<T extends DataObject> {

    /**
     * Method returns future to be used for handling device requests.
     *
     * @param <T>
     * @return
     */
    <T> SettableFuture<RpcResult<T>> getFuture();

}
