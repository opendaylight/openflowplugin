/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.rpc;

import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceContextClosedHandler;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializator;

/**
 * The RPC Manager will maintain an RPC Context for each online switch. RPC context for device is created when
 * {@link org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler#onDeviceContextLevelUp(org.opendaylight.openflowplugin.api.openflow.device.DeviceContext)}
 * is called.
 * <p>
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 25.2.2015.
 */
public interface RpcManager extends DeviceInitializator, DeviceInitializationPhaseHandler, AutoCloseable, DeviceContextClosedHandler {

    void setStatisticsRpcEnabled(boolean isStatisticsRpcEnabled);

    void setNotificationPublishService(NotificationPublishService notificationPublishService);
}
