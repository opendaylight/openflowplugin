/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.opendaylight.openflowplugin.extension.api.AugmentTuple;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.extension.MatchExtensionHelper;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorImpl;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.PacketInUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortNumberMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifUpdateFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.BufferId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.ConnectionCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceivedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.MatchBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * translates packetIn from OF-API model to MD-SAL model, supports OF-1.3
 */
public class PacketInTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {

    protected static final Logger LOG = LoggerFactory
            .getLogger(PacketInTranslator.class);
    @Override
    public List<DataObject> translate(final SwitchConnectionDistinguisher cookie,
            final SessionContext sc, final OfHeader msg) {

        List<DataObject> salPacketIn = Collections.emptyList();

        if (sc != null && msg instanceof PacketInMessage) {
            PacketInMessage message = (PacketInMessage)msg;
            LOG.trace("PacketIn[v{}]: Cookie: {} Match.type: {}, BufferId: {}",
                    message.getVersion(), message.getCookie(),
                    message.getMatch() != null ? message.getMatch().getType() : message.getMatch(),
                    message.getBufferId() != null ? message.getBufferId().toString() : "No buffer Id");

           // create a packet received event builder
           PacketReceivedBuilder pktInBuilder = new PacketReceivedBuilder();
           pktInBuilder.setPayload(message.getData());
           if (cookie != null) {
               pktInBuilder.setConnectionCookie(new ConnectionCookie(cookie.getCookie()));
           }

           // get the DPID
           GetFeaturesOutput features = sc.getFeatures();
           // Make sure we actually have features, some naughty switches start sending packetIn before they send us the FeatureReply
           if ( features == null) {
               LOG.warn("Received packet_in, but there is no device datapathId received yet");
           } else {
               BigInteger dpid = features.getDatapathId();

               // get the Cookie if it exists
               if(message.getCookie() != null) {
                   pktInBuilder.setFlowCookie(new FlowCookie(message.getCookie()));
               }

               // extract the port number
               Long port = null;
               if (message.getMatch() != null && message.getMatch().getMatchEntries() != null) {
                   List<MatchEntries> entries = message.getMatch().getMatchEntries();
                   for (MatchEntries entry : entries) {
                       PortNumberMatchEntry tmp = entry.getAugmentation(PortNumberMatchEntry.class);
                       if (tmp != null) {
                           if (port == null) {
                               port = tmp.getPortNumber().getValue();
                           } else {
                               LOG.warn("Multiple input ports discovered when walking through match entries (at least {} and {})",
                                        port, tmp.getPortNumber().getValue());
                           }
                       }
                   }
               }

               if (port == null) {
                   // no incoming port, so drop the event
                   LOG.warn("Received packet_in, but couldn't find an input port");
               } else {
                   LOG.trace("Received packet_in from {} on port {}", dpid, port);

                   OpenflowVersion ofVersion = OpenflowVersion.get(sc.getPrimaryConductor().getVersion());
                   Match match = MatchConvertorImpl.fromOFMatchToSALMatch(message.getMatch(),dpid, ofVersion).build();
                   MatchBuilder matchBuilder = new MatchBuilder(match);

                   AugmentTuple<org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.Match> matchExtensionWrap = 
                           MatchExtensionHelper.processAllExtensions(
                                   message.getMatch().getMatchEntries(), ofVersion, MatchPath.PACKETRECEIVED_MATCH);
                   if (matchExtensionWrap != null) {
                       matchBuilder.addAugmentation(matchExtensionWrap.getAugmentationClass(), matchExtensionWrap.getAugmentationObject());
                   }
                   
                   org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.Match packetInMatch = matchBuilder.build();
                   pktInBuilder.setMatch(packetInMatch);
                   pktInBuilder.setPacketInReason(PacketInUtil.getMdSalPacketInReason(message.getReason()));
                   pktInBuilder.setTableId(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId(message.getTableId().getValue().shortValue()));
                   pktInBuilder.setIngress(InventoryDataServiceUtil.nodeConnectorRefFromDatapathIdPortno(dpid, port, ofVersion));
                   if (message.getBufferId() != null) {
                       pktInBuilder.setBufferId(new BufferId(message.getBufferId()));
                   }
                   PacketReceived pktInEvent = pktInBuilder.build();
                   salPacketIn = Collections.<DataObject>singletonList(pktInEvent);
               }
           }
        }
        return salPacketIn;
    }
}
