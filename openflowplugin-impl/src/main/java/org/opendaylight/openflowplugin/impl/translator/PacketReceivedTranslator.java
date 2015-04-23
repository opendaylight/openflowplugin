/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * <p/>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.translator;

import java.math.BigInteger;
import java.util.List;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.AugmentTuple;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.openflow.md.core.extension.MatchExtensionHelper;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorImpl;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.openflowplugin.openflow.md.util.PacketInUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceivedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.MatchBuilder;

/**
 * Created by tkubas on 4/1/15.
 */
public class PacketReceivedTranslator implements MessageTranslator<PacketInMessage, PacketReceived> {
    @Override
    public PacketReceived translate(PacketInMessage input, DeviceContext deviceContext, Object connectionDistinguisher) {

        PacketReceivedBuilder packetReceivedBuilder = new PacketReceivedBuilder();
        BigInteger datapathId = deviceContext.getPrimaryConnectionContext().getFeatures().getDatapathId();

        // extract the port number
        Long port = null;
        if (input.getVersion() == OFConstants.OFP_VERSION_1_0 && input.getInPort() != null) {
            port = input.getInPort().longValue();
        } else if (input.getVersion() == OFConstants.OFP_VERSION_1_3) {
            if (input.getMatch() != null && input.getMatch().getMatchEntry() != null) {
                port = getPortNumberFromMatch(input.getMatch().getMatchEntry());
            }
        }

        //TODO connection cookie from connection distinguisher
//        packetReceivedBuilder.setConnectionCookie(new ConnectionCookie(input.getCookie().longValue()));
        packetReceivedBuilder.setPayload(input.getData());
        // get the Cookie if it exists
        if (input.getCookie() != null) {
            packetReceivedBuilder.setFlowCookie(new FlowCookie(input.getCookie()));
        }
        if (port != null) {
            packetReceivedBuilder.setIngress(InventoryDataServiceUtil.nodeConnectorRefFromDatapathIdPortno(datapathId, port, OpenflowVersion.get(input.getVersion())));
        }
        packetReceivedBuilder.setPacketInReason(PacketInUtil.getMdSalPacketInReason(input.getReason()));

        if (input.getTableId() != null) {
            packetReceivedBuilder.setTableId(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId(input.getTableId().getValue().shortValue()));
        }

        if (input.getMatch() != null) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.Match packetInMatch = getPacketInMatch(input, datapathId);
            packetReceivedBuilder.setMatch(packetInMatch);
        }

        return packetReceivedBuilder.build();
    }

    private org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.Match getPacketInMatch(PacketInMessage input, BigInteger datapathId) {
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

    private Long getPortNumberFromMatch(List<MatchEntry> entries) {
        Long port = null;
        for (MatchEntry entry : entries) {
            if (InPortCase.class.equals(entry.getMatchEntryValue().getImplementedInterface())) {
                InPortCase inPortCase = ((InPortCase) entry.getMatchEntryValue());
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.in.port._case.InPort inPort = inPortCase.getInPort();
                if (inPort != null) {
                    port = inPort.getPortNumber().getValue();
                    break;
                }
            }
        }
        return port;
    }
}
