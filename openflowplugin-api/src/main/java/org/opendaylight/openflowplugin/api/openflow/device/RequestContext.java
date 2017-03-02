/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import javax.annotation.Nullable;

/**
 * Request context handles all requests on device. Number of requests is limited by request quota. When this quota is
 * exceeded all rpc's will end up with exception.
 */
public interface RequestContext<T> extends RequestFutureContext<T>, AutoCloseable {
    /**
     * Returns XID generated for this request.
     * @return Allocated XID, or null if the device has disconnected.
     */
    @Nullable Xid getXid();

    @Override
    void close();

    /**
     * Returns request timeout value.
     * @return timeout
     */
    long getWaitTimeout();

    /**
     * Sets request timeout value.
     */
    void setWaitTimeout(long waitTimeout);
}
