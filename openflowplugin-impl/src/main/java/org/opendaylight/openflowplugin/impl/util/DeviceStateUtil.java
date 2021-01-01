/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.CapabilitiesV10;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public final class DeviceStateUtil {
    private DeviceStateUtil() {
        // Hidden on purpose
    }

    public static void setDeviceStateBasedOnV13Capabilities(final DeviceState deviceState,
                                                            final Capabilities capabilities) {
        deviceState.setFlowStatisticsAvailable(capabilities.getOFPCFLOWSTATS());
        deviceState.setTableStatisticsAvailable(capabilities.getOFPCTABLESTATS());
        deviceState.setPortStatisticsAvailable(capabilities.getOFPCPORTSTATS());
        deviceState.setQueueStatisticsAvailable(capabilities.getOFPCQUEUESTATS());
        deviceState.setGroupAvailable(capabilities.getOFPCGROUPSTATS());
    }

    public static void setDeviceStateBasedOnV10Capabilities(final DeviceState deviceState,
                                                            final CapabilitiesV10 capabilitiesV10) {
        deviceState.setFlowStatisticsAvailable(capabilitiesV10.getOFPCFLOWSTATS());
        deviceState.setTableStatisticsAvailable(capabilitiesV10.getOFPCTABLESTATS());
        deviceState.setPortStatisticsAvailable(capabilitiesV10.getOFPCPORTSTATS());
        deviceState.setQueueStatisticsAvailable(capabilitiesV10.getOFPCQUEUESTATS());
    }

    public static KeyedInstanceIdentifier<Node, NodeKey> createNodeInstanceIdentifier(final NodeId nodeId) {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId));
    }
}
