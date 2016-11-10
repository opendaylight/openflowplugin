/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.translator.TranslatorLibrarian;

/**
 * This interface is responsible for instantiating DeviceContext and
 * registering transaction chain for each DeviceContext. Each device
 * has its own device context managed by this manager.
 */
public interface DeviceManager extends
        OFPManager,
        DeviceConnectedHandler,
        DeviceDisconnectedHandler,
        TranslatorLibrarian {

    /**
     * invoked after all services injected
     */
    void initialize();

    void setFlowRemovedNotificationOn(boolean value);

    boolean isFlowRemovedNotificationOn();

    void setSkipTableFeatures(boolean skipTableFeatures);

    void setBarrierCountLimit(int barrierCountLimit);

    void setBarrierInterval(long barrierTimeoutLimit);

    CheckedFuture<Void, TransactionCommitFailedException> removeDeviceFromOperationalDS(DeviceInfo deviceInfo);
}

