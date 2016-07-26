/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceLifecycleSupervisor;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.translator.TranslatorLibrarian;

/**
 * This interface is responsible for instantiating DeviceContext and
 * registering transaction chain for each DeviceContext. Each device
 * has its own device context managed by this manager.
 */
public interface DeviceManager extends DeviceConnectedHandler, DeviceDisconnectedHandler, DeviceLifecycleSupervisor,
        DeviceInitializationPhaseHandler, DeviceTerminationPhaseHandler, TranslatorLibrarian, AutoCloseable, OFPManager {


    /**
     * invoked after all services injected
     */
    void initialize();

    void setIsNotificationFlowRemovedOff(boolean value);

    boolean getIsNotificationFlowRemovedOff();

}

