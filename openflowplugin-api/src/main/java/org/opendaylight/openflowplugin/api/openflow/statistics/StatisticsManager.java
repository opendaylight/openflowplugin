/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.statistics;

import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceLifecycleSupervisor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 26.2.2015.
 */
public interface StatisticsManager extends DeviceLifecycleSupervisor, DeviceInitializationPhaseHandler,
        DeviceTerminationPhaseHandler, AutoCloseable {

    void startScheduling(NodeId nodeId);
    void stopScheduling(NodeId nodeId);

    @Override
    void close();
}
