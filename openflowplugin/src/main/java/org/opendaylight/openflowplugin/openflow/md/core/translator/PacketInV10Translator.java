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
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.openflowplugin.openflow.md.util.PacketInUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceivedBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * translates packetIn from OF-API model to MD-SAL model, supports OF-1.0
 */
public class PacketInV10Translator implements IMDMessageTranslator<OfHeader, List<DataObject>> {

    private static final Logger LOG = LoggerFactory
            .getLogger(PacketInV10Translator.class);
    @Override
    public List<DataObject> translate(final SwitchConnectionDistinguisher cookie,
            final SessionContext sc, final OfHeader msg) {

        List<DataObject> salPacketIn = Collections.emptyList();

        if (sc != null && msg instanceof PacketInMessage) {
            PacketInMessage message = (PacketInMessage)msg;
            LOG.trace("PacketIn[v{}]: InPort: {}",
                    msg.getVersion(), message.getInPort());

            // create a packet received event builder
            PacketReceivedBuilder pktInBuilder = new PacketReceivedBuilder();
            pktInBuilder.setPayload(message.getData());

            // get the DPID
            GetFeaturesOutput features = sc.getFeatures();
            // Make sure we actually have features, some naughty switches start sending packetIn before they send us the FeatureReply
            if ( features == null) {
                LOG.warn("Received packet_in, but there is no device datapathId received yet");
            } else {
                BigInteger dpid = features.getDatapathId();

                // extract the port number
                Long port = null;
                if (message.getInPort() != null) {
                    port = message.getInPort().longValue();
                }

                if (port == null) {
                    // no incoming port, so drop the event
                    LOG.warn("Received packet_in, but couldn't find an input port");
                } else {
                    LOG.trace("Received packet_in from {} on port {}", dpid, port);
                    pktInBuilder.setPacketInReason(PacketInUtil.getMdSalPacketInReason(message.getReason()));
                    pktInBuilder.setIngress(InventoryDataServiceUtil.nodeConnectorRefFromDatapathIdPortno(dpid, port,
                            OpenflowVersion.get(sc.getPrimaryConductor().getVersion())));
                    PacketReceived pktInEvent = pktInBuilder.build();
                    salPacketIn = Collections.<DataObject>singletonList(pktInEvent);
                }
            }
        }
        return salPacketIn;
    }
}
