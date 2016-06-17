/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.rpc;

import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceLifecycleSupervisor;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;

/**
 * The RPC Manager will maintain an RPC Context for each online switch. RPC context for device is created when
 * {@link org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler#onDeviceContextLevelUp(org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo)}
 * is called.
 * <p>
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 25.2.2015.
 */
public interface RpcManager extends DeviceLifecycleSupervisor, DeviceInitializationPhaseHandler, AutoCloseable, DeviceTerminationPhaseHandler, OFPManager {

    void setStatisticsRpcEnabled(boolean statisticsRpcEnabled);
}
