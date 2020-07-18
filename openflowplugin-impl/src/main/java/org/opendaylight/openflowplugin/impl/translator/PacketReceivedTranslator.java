/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.translator;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.AugmentTuple;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.impl.util.NodeConnectorRefToPortTranslator;
import org.opendaylight.openflowplugin.impl.util.PacketInUtil;
import org.opendaylight.openflowplugin.openflow.md.core.extension.MatchExtensionHelper;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceivedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yangtools.yang.common.Uint64;

public class PacketReceivedTranslator implements MessageTranslator<PacketInMessage, PacketReceived> {
    private final ConvertorExecutor convertorExecutor;

    public PacketReceivedTranslator(final ConvertorExecutor convertorExecutor) {
        this.convertorExecutor = convertorExecutor;
    }

    @Override
    public PacketReceived translate(final PacketInMessage input, final DeviceInfo deviceInfo,
                                    final Object connectionDistinguisher) {

        PacketReceivedBuilder packetReceivedBuilder = new PacketReceivedBuilder();
        Uint64 datapathId = deviceInfo.getDatapathId();

        // TODO: connection cookie from connection distinguisher
        packetReceivedBuilder.setPayload(input.getData());

        // get the Cookie if it exists
        if (input.getCookie() != null) {
            packetReceivedBuilder.setFlowCookie(new FlowCookie(input.getCookie()));
        }

        // Try to create the NodeConnectorRef
        Uint64 dataPathId = deviceInfo.getDatapathId();
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
            org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.Match packetInMatch
                    = getPacketInMatch(input, datapathId);
            packetReceivedBuilder.setMatch(packetInMatch);
        }

        return packetReceivedBuilder.build();
    }

    @VisibleForTesting
    org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.Match getPacketInMatch(
            final PacketInMessage input, final Uint64 datapathId) {

        final VersionDatapathIdConvertorData datapathIdConvertorData = new VersionDatapathIdConvertorData(
                input.getVersion().toJava());
        datapathIdConvertorData.setDatapathId(datapathId);

        final Optional<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder>
                matchOptional = convertorExecutor.convert(input.getMatch(), datapathIdConvertorData);
        final MatchBuilder matchBuilder = matchOptional.map(matchBuilder1 -> new MatchBuilder(matchBuilder1.build()))
                .orElseGet(MatchBuilder::new);

        final AugmentTuple<org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.Match>
                matchExtensionWrap = MatchExtensionHelper.processAllExtensions(input.getMatch().nonnullMatchEntry(),
                    OpenflowVersion.get(input.getVersion()), MatchPath.PACKET_RECEIVED_MATCH);

        if (matchExtensionWrap != null) {
            matchBuilder.addAugmentation(matchExtensionWrap.getAugmentationObject());
        }

        return matchBuilder.build();
    }
}
