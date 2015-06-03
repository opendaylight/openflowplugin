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

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 20.4.2015.
 */
public final class DeviceStateUtil {

    private DeviceStateUtil() {
        throw new IllegalStateException("This class should not be instantiated");
    }

    public static void setDeviceStateBasedOnV13Capabilities(final DeviceState deviceState, final Capabilities capabilities) {
        deviceState.setFlowStatisticsAvailable(capabilities.isOFPCFLOWSTATS());
        deviceState.setTableStatisticsAvailable(capabilities.isOFPCTABLESTATS());
        deviceState.setPortStatisticsAvailable(capabilities.isOFPCPORTSTATS());
        deviceState.setQueueStatisticsAvailable(capabilities.isOFPCQUEUESTATS());
        deviceState.setGroupAvailable(capabilities.isOFPCGROUPSTATS());
    }

    public static void setDeviceStateBasedOnV10Capabilities(final DeviceState deviceState, final CapabilitiesV10 capabilitiesV10) {
        deviceState.setFlowStatisticsAvailable(capabilitiesV10.isOFPCFLOWSTATS());
        deviceState.setTableStatisticsAvailable(capabilitiesV10.isOFPCTABLESTATS());
        deviceState.setPortStatisticsAvailable(capabilitiesV10.isOFPCPORTSTATS());
        deviceState.setQueueStatisticsAvailable(capabilitiesV10.isOFPCQUEUESTATS());
    }

    public static KeyedInstanceIdentifier<Node, NodeKey> createNodeInstanceIdentifier(NodeId nodeId){
        return InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId));
    }
}
