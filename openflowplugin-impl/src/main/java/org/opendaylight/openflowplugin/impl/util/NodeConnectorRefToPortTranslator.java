/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.in.port._case.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Created by Tomas Slusny on 23.3.2016.
 */
public class NodeConnectorRefToPortTranslator {
    /**
     * Converts {@link DeviceState} to {@link NodeConnectorRef}
     * @param deviceState Device state fallback if there is any problem with packet input
     * @param packetIn Packet input
     * @return Device state converted to node connector reference
     */
    @Nullable
    public static NodeConnectorRef toNodeConnectorRef(@Nonnull DeviceState deviceState, PacketIn packetIn) {
        Preconditions.checkNotNull(deviceState);

        BigInteger dataPathId = deviceState.getFeatures().getDatapathId();
        Long port = null;
        OpenflowVersion version;

        if (packetIn != null) {
            port = getPortNoFromPacketIn(packetIn);
            version = OpenflowVersion.get(packetIn.getVersion());
        } else {
            version = OpenflowVersion.get(deviceState.getVersion());
        }

        if (port == null) {
            port = getPortNoFromDeviceState(deviceState);
        }

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
        Preconditions.checkNotNull(deviceState);

        if (nodeConnectorRef != null && nodeConnectorRef.getValue() instanceof KeyedInstanceIdentifier) {
            KeyedInstanceIdentifier<NodeConnector, NodeConnectorKey> identifier =
                    (KeyedInstanceIdentifier<NodeConnector, NodeConnectorKey>) nodeConnectorRef.getValue();

            OpenflowVersion version = OpenflowVersion.get(deviceState.getVersion());
            String nodeConnectorId = identifier.getKey().getId().getValue();

            return InventoryDataServiceUtil.portNumberfromNodeConnectorId(version, nodeConnectorId);
        } else {
            return getPortNoFromDeviceState(deviceState);
        }
    }

    @VisibleForTesting
    @Nullable
    static Long getPortNoFromDeviceState(@Nonnull DeviceState deviceState) {
        Preconditions.checkNotNull(deviceState);

        List<PhyPort> ports = deviceState.getFeatures().getPhyPort();

        return ports != null ?
                ports.stream().filter(Objects::nonNull).map(PhyPort::getPortNo).filter(Objects::nonNull).findFirst().orElse(null) :
                null;
    }

    @VisibleForTesting
    @Nullable
    static Long getPortNoFromPacketIn(@Nonnull PacketIn packetIn) {
        Preconditions.checkNotNull(packetIn);

        Long port = null;

        if (packetIn.getVersion() == OFConstants.OFP_VERSION_1_0 && packetIn.getInPort() != null) {
            port = packetIn.getInPort().longValue();
        } else if (packetIn.getVersion() == OFConstants.OFP_VERSION_1_3) {
            if (packetIn.getMatch() != null && packetIn.getMatch().getMatchEntry() != null) {
                List<MatchEntry> entries = packetIn.getMatch().getMatchEntry();

                for (MatchEntry entry : entries) {
                    if (entry.getMatchEntryValue() instanceof InPortCase) {
                        InPortCase inPortCase = (InPortCase) entry.getMatchEntryValue();

                        InPort inPort = inPortCase.getInPort();

                        if (inPort != null) {
                            port = inPort.getPortNumber().getValue();
                            break;
                        }
                    }
                }
            }
        }

        return port;
    }
}
