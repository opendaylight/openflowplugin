/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import com.google.common.annotations.VisibleForTesting;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.List;

/**
 * Created by Tomas Slusny on 23.3.2016.
 */
public class NodeConnectorRefToPortTranslator {
    /**
     * Converts {@link DeviceState} to {@link NodeConnectorRef}
     * @param deviceState Device state to be converted
     * @return Device state converted to node connector reference
     */
    @Nullable
    public static NodeConnectorRef toNodeConnectorRef(@Nonnull DeviceState deviceState) {
        Long port = getPortNoFromDeviceState(deviceState);

        if (port == null) {
            return null;
        }

        OpenflowVersion version = OpenflowVersion.get(deviceState.getVersion());
        BigInteger dataPathId = deviceState.getFeatures().getDatapathId();

        return InventoryDataServiceUtil.nodeConnectorRefFromDatapathIdPortno(dataPathId, port, version);
    }

    /**
     * Gets port number from {@link NodeConnectorRef}. If it is null, it will try to get the port from
     * {@link DeviceState}
     * @param deviceState Device state fallback if there is any problem with node connector reference
     * @param nodeConnectorRef Node connector reference
     * @return port number
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static Long fromNodeConnectorRef(@Nonnull DeviceState deviceState, NodeConnectorRef nodeConnectorRef) {
        Long port = getPortNoFromDeviceState(deviceState);

        if (nodeConnectorRef != null && nodeConnectorRef.getValue() instanceof KeyedInstanceIdentifier) {
            KeyedInstanceIdentifier<NodeConnector, NodeConnectorKey> identifier =
                    (KeyedInstanceIdentifier<NodeConnector, NodeConnectorKey>) nodeConnectorRef.getValue();

            OpenflowVersion version = OpenflowVersion.get(deviceState.getVersion());
            String nodeConnectorId = identifier.getKey().getId().getValue();

            port = InventoryDataServiceUtil.portNumberfromNodeConnectorId(version, nodeConnectorId);
        }

        return port;
    }

    @VisibleForTesting
    static Long getPortNoFromDeviceState(@Nonnull DeviceState deviceState) {
        List<PhyPort> ports = deviceState.getFeatures().getPhyPort();

        if (ports == null || ports.isEmpty()) {
            return null;
        }

        PhyPort port = ports.get(0);

        if (port == null) {
            return null;
        }

        return port.getPortNo();
    }
}
