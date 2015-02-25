/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.md.core.device.handlers;

import org.opendaylight.openflowplugin.api.openflow.md.core.device.RequestContext;

/**
 * Created by Martin Bobak <mbobak@cisco.com> on 27.2.2015.
 */
public interface DeviceContextReadyHandler {
    /**
     * Method is used to propagate information about established connection with device.
     * It propagates connected device's request context.
     */
    void deviceConnected(RequestContext requestContext);

}
