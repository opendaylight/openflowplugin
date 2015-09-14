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
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceContextClosedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializator;
import org.opendaylight.openflowplugin.api.openflow.translator.TranslatorLibrarian;

/**
 * This interface is responsible for instantiating DeviceContext and
 * registering transaction chain for each DeviceContext. Each device
 * has its own device context managed by this manager.
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 25.2.2015.
 */
public interface DeviceManager extends DeviceConnectedHandler,
        TranslatorLibrarian,
        DeviceInitializator,
        DeviceInitializationPhaseHandler, DeviceContextClosedHandler,
        AutoCloseable {

    /**
     * Sets notification receiving service
     *
     * @param notificationService
     */
    void setNotificationService(NotificationService notificationService);

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
}

