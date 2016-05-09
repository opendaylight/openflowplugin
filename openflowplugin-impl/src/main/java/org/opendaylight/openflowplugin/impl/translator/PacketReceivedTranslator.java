/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.translator;

import com.google.common.annotations.VisibleForTesting;
import java.math.BigInteger;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.AugmentTuple;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.impl.util.NodeConnectorRefToPortTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.extension.MatchExtensionHelper;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorImpl;
import org.opendaylight.openflowplugin.openflow.md.util.PacketInUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceivedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;

/**
 * Created by tkubas on 4/1/15.
 */
public class PacketReceivedTranslator implements MessageTranslator<PacketInMessage, PacketReceived> {
    @Override
    public PacketReceived translate(final PacketInMessage input, final DeviceState deviceState, final Object connectionDistinguisher) {

        PacketReceivedBuilder packetReceivedBuilder = new PacketReceivedBuilder();
        BigInteger datapathId = deviceState.getFeatures().getDatapathId();

        // TODO: connection cookie from connection distinguisher
        // packetReceivedBuilder.setConnectionCookie(new ConnectionCookie(input.getCookie().longValue()));

        packetReceivedBuilder.setPayload(input.getData());

        // get the Cookie if it exists
        if (input.getCookie() != null) {
            packetReceivedBuilder.setFlowCookie(new FlowCookie(input.getCookie()));
        }

        // Try to create the NodeConnectorRef
        BigInteger dataPathId = deviceState.getFeatures().getDatapathId();
        NodeConnectorRef nodeConnectorRef = NodeConnectorRefToPortTranslator.toNodeConnectorRef(input, dataPathId);

        // If we was able to create NodeConnectorRef, use it
        if (nodeConnectorRef != null) {
            packetReceivedBuilder.setIngress(nodeConnectorRef);
        }

        packetReceivedBuilder.setPacketInReason(PacketInUtil.getMdSalPacketInReason(input.getReason()));

        if (input.getTableId() != null) {
            packetReceivedBuilder.setTableId(new TableId(input.getTableId().getValue().shortValue()));
        }

        if (input.getMatch() != null) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.Match packetInMatch = getPacketInMatch(input, datapathId);
            packetReceivedBuilder.setMatch(packetInMatch);
        }

        return packetReceivedBuilder.build();
    }

    @VisibleForTesting
    static org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.Match getPacketInMatch(final PacketInMessage input, final BigInteger datapathId) {
        Match match = MatchConvertorImpl.fromOFMatchToSALMatch(input.getMatch(),
                datapathId,
                OpenflowVersion.get(input.getVersion().shortValue())).build();
        MatchBuilder matchBuilder = new MatchBuilder(match);

        AugmentTuple<org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.Match> matchExtensionWrap =
                MatchExtensionHelper.processAllExtensions(
                        input.getMatch().getMatchEntry(), OpenflowVersion.get(input.getVersion().shortValue()), MatchPath.PACKETRECEIVED_MATCH);
        if (matchExtensionWrap != null) {
            matchBuilder.addAugmentation(matchExtensionWrap.getAugmentationClass(), matchExtensionWrap.getAugmentationObject());
        }
        return matchBuilder.build();
    }
}
