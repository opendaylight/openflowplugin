/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.md.core.device;

import org.opendaylight.openflowplugin.api.openflow.md.core.device.exception.RequestQuotaExceededException;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Request context handles all requests on device. Number of requests is
 * limited by request quota. When this quota is exceeded all rpc's will
 * end up with exception.
 * <p/>
 * Created by Martin Bobak <mbobak@cisco.com> on 25.2.2015.
 */
public interface RequestContext {

    /**
     * Method to set device context which holds ConnectionContext and DeviceState.
     *
     * @param deviceContext
     */
    void setDeviceContext(DeviceContext deviceContext);

    /**
     * Method for setting request quota value.
     * When the Request Context quota is exceeded, incoming RPCs fail immediately, with a well-defined error.
     *
     * @param maxRequestsPerDevice
     */
    void setRequestContextQuota(int maxRequestsPerDevice);

    /**
     * Method adds request to request queue which has limited quota. After
     * number of requests exceeds quota limit
     * {@link org.opendaylight.openflowplugin.api.openflow.md.core.device.exception.RequestQuotaExceededException}
     * is thrown.
     *
     * @param data
     * @throws RequestQuotaExceededException
     */
    void addRequest(DataObject data) throws RequestQuotaExceededException;

}
