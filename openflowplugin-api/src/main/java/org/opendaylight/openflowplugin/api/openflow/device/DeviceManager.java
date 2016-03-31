/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceLifecycleSupervisor;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.translator.TranslatorLibrarian;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * This interface is responsible for instantiating DeviceContext and
 * registering transaction chain for each DeviceContext. Each device
 * has its own device context managed by this manager.
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 25.2.2015.
 */
public interface DeviceManager extends DeviceConnectedHandler, DeviceDisconnectedHandler, DeviceLifecycleSupervisor,
        DeviceInitializationPhaseHandler, DeviceTerminationPhaseHandler, TranslatorLibrarian, AutoCloseable {

    /**
     * Sets notification publish service
     *
     * @param notificationPublishService
     */
    void setNotificationPublishService(NotificationPublishService notificationPublishService);

    /**
     * invoked after all services injected
     */
    void initialize();

    /**
     * Returning device context from map maintained in device manager
     * This prevent to send whole device context to another context
     * If device context not exists for nodeId it will return null
     * @param nodeId
     * @return device context or null
     */
    DeviceContext getDeviceContextFromNodeId(NodeId nodeId);

    void setStatisticsRpcEnabled(boolean isStatisticsRpcEnabled);
}

