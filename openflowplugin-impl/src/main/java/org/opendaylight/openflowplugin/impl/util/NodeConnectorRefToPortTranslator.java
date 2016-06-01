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
import java.math.BigInteger;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.in.port._case.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketIn;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * Created by Tomas Slusny on 23.3.2016.
 */
public class NodeConnectorRefToPortTranslator {
    /**
     * Converts {@link PacketIn} to {@link NodeConnectorRef}
     * @param packetIn Packet input
     * @param dataPathId Data path id
     * @return packet input converted to node connector reference
     */
    @Nullable
    public static NodeConnectorRef toNodeConnectorRef(@Nonnull PacketIn packetIn, BigInteger dataPathId) {
        Preconditions.checkNotNull(packetIn);

        NodeConnectorRef ref = null;
        Long port = getPortNoFromPacketIn(packetIn);

        if (port != null) {
            OpenflowVersion version = OpenflowVersion.get(packetIn.getVersion());

            ref = InventoryDataServiceUtil.nodeConnectorRefFromDatapathIdPortno(dataPathId, port, version);
        }

        return ref;
    }

    /**
     * Gets port number from {@link NodeConnectorRef}.
     * @param nodeConnectorRef Node connector reference
     * @param version Openflow version
     * @return port number
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static Long fromNodeConnectorRef(@Nonnull NodeConnectorRef nodeConnectorRef, short version) {
        Preconditions.checkNotNull(nodeConnectorRef);

        Long port = null;

        if (nodeConnectorRef.getValue() instanceof KeyedInstanceIdentifier) {
            KeyedInstanceIdentifier<NodeConnector, NodeConnectorKey> identifier =
                    (KeyedInstanceIdentifier<NodeConnector, NodeConnectorKey>) nodeConnectorRef.getValue();

            OpenflowVersion ofVersion = OpenflowVersion.get(version);
            String nodeConnectorId = identifier.getKey().getId().getValue();

            port = InventoryDataServiceUtil.portNumberfromNodeConnectorId(ofVersion, nodeConnectorId);
        }

        return port;
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
