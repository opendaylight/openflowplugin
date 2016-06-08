/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;

/**
 * This API is defined for listening when services (Statistics and RPCs) are fully stopped
 * or fully started. Role manager use it for unregister tx entity on shutdown when all is stopped.
 */
public interface ServiceChangeListener {

    /**
     * Notification when services (rpc, statistics) are started or stopped working
     * @param deviceInfo
     * @param success
     */
    void servicesChangeDone(DeviceInfo deviceInfo, boolean success);

}
