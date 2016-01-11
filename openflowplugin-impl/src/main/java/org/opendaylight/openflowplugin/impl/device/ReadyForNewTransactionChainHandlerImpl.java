/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 5.6.2015.
 */
public class ReadyForNewTransactionChainHandlerImpl implements ReadyForNewTransactionChainHandler {

    private final DeviceManager deviceManager;
    private final ConnectionContext connectionContext;

    public ReadyForNewTransactionChainHandlerImpl(final DeviceManager deviceManager, final ConnectionContext connectionContext) {
        this.deviceManager = deviceManager;
        this.connectionContext = connectionContext;
    }

    @Override
    public void onReadyForNewTransactionChain() {
        deviceManager.initializeDeviceContext(connectionContext);
    }
}
