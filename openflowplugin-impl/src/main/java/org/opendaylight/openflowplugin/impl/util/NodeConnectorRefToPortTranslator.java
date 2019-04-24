/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

/**
 * Created by Tomas Slusny on 23.3.2016.
 */
public final class NodeConnectorRefToPortTranslator {

    private NodeConnectorRefToPortTranslator() {
    }

    /**
     * Converts {@link PacketIn} to {@link NodeConnectorRef}.
     * @param packetIn Packet input
     * @param dataPathId Data path id
     * @return packet input converted to node connector reference
     */
    @Nullable
    public static NodeConnectorRef toNodeConnectorRef(@NonNull final PacketIn packetIn, final Uint64 dataPathId) {
        Preconditions.checkNotNull(packetIn);

        NodeConnectorRef ref = null;
        Uint32 port = getPortNoFromPacketIn(packetIn);

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
    public static Uint32 fromNodeConnectorRef(@NonNull final NodeConnectorRef nodeConnectorRef, final short version) {
        Preconditions.checkNotNull(nodeConnectorRef);

        Uint32 port = null;

        final InstanceIdentifier<?> value = nodeConnectorRef.getValue();
        if (value instanceof KeyedInstanceIdentifier) {
            KeyedInstanceIdentifier<NodeConnector, NodeConnectorKey> identifier =
                    (KeyedInstanceIdentifier<NodeConnector, NodeConnectorKey>) value;

            OpenflowVersion ofVersion = OpenflowVersion.get(version);
            String nodeConnectorId = identifier.getKey().getId().getValue();

            port = InventoryDataServiceUtil.portNumberfromNodeConnectorId(ofVersion, nodeConnectorId);
        }

        return port;
    }

    @VisibleForTesting
    @Nullable
    static Uint32 getPortNoFromPacketIn(@NonNull final PacketIn packetIn) {
        Preconditions.checkNotNull(packetIn);

        Uint32 port = null;

        if (packetIn.getVersion().toJava() == OFConstants.OFP_VERSION_1_0 && packetIn.getInPort() != null) {
            port = Uint32.valueOf(packetIn.getInPort());
        } else if (packetIn.getVersion().toJava() == OFConstants.OFP_VERSION_1_3) {
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
