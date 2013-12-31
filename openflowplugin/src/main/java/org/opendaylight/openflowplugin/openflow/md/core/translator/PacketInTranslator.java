package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortNumberMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.Cookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceivedBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketInTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {

    protected static final Logger LOG = LoggerFactory
            .getLogger(PacketInTranslator.class);
    @Override
    public List<DataObject> translate(SwitchConnectionDistinguisher cookie,
            SessionContext sc, OfHeader msg) {
        if(sc !=null && msg instanceof PacketInMessage) {
            PacketInMessage message = (PacketInMessage)msg;
            List<DataObject> list = new CopyOnWriteArrayList<DataObject>();
            LOG.info("PacketIn: InPort: {} Cookie: {} Match.type: {}",
                    message.getInPort(), message.getCookie(),
                    message.getMatch() != null ? message.getMatch().getType()
                                              : message.getMatch());

           // create a packet received event builder
           PacketReceivedBuilder pktInBuilder = new PacketReceivedBuilder();
           pktInBuilder.setPayload(message.getData());

           org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match match = 
                (org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match)
                MatchConvertorImpl.fromOFMatchToSALMatch(message.getMatch());
           pktInBuilder.setMatch((org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.Match)match);
           pktInBuilder.setBufferId(message.getBufferId());
           pktInBuilder.setPacketInReason((short)message.getReason().ordinal());
           pktInBuilder.setTableId(message.getTableId().getValue().shortValue());

           // get the DPID
           GetFeaturesOutput features = sc.getFeatures();
           // Make sure we actually have features, some naughty switches start sending packetIn before they send us the FeatureReply
           if ( features != null) {
               BigInteger dpid = features.getDatapathId();
    
               // get the Cookie if it exists
               if(message.getCookie() != null) {
                   pktInBuilder.setCookie(new Cookie(message.getCookie().longValue()));
               }
    
               // extract the port number
               Long port = null;
    
               if (message.getInPort() != null) {
                   // this doesn't work--at least for OF1.3
                   port = message.getInPort().longValue();
               }
    
               // this should work for OF1.3
               if (message.getMatch() != null && message.getMatch().getMatchEntries() != null) {
                   List<MatchEntries> entries = message.getMatch().getMatchEntries();
                   for (MatchEntries entry : entries) {
                       PortNumberMatchEntry tmp = entry.getAugmentation(PortNumberMatchEntry.class);
                       if (tmp != null) {
                           if (port == null) {
                               port = tmp.getPortNumber().getValue();
                           } else {
                               LOG.warn("Multiple input ports (at least {} and {})",
                                        port, tmp.getPortNumber().getValue());
                           }
                       }
                   }
               }
    
               if (port == null) {
                   // no incoming port, so drop the event
                   LOG.warn("Received packet_in, but couldn't find an input port");
                   return null;
               }else{
                   LOG.info("Receive packet_in from {} on port {}", dpid, port);
               }
               pktInBuilder.setIngress(InventoryDataServiceUtil.nodeConnectorRefFromDatapathIdPortno(dpid,port));
               PacketReceived pktInEvent = pktInBuilder.build();
               list.add(pktInEvent);
                return list;
           } 
        } 
        return Collections.emptyList();
    }
}
