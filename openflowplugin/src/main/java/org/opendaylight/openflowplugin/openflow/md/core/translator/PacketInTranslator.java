package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.util.List;

import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortNumberMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.Cookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceivedBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketInTranslator implements IMDMessageTranslator<OfHeader, DataObject> {

    protected static final Logger LOG = LoggerFactory
            .getLogger(PacketInTranslator.class);
    @Override
    public PacketReceived translate(SwitchConnectionDistinguisher cookie,
            SessionContext sc, OfHeader msg) {
        if(msg instanceof PacketInMessage) {
            PacketInMessage message = (PacketInMessage)msg;
            LOG.info("PacketIn: InPort: {} Cookie: {} Match.type: {}",
                    message.getInPort(), message.getCookie(),
                    message.getMatch() != null ? message.getMatch().getType()
                                              : message.getMatch());

           // create a packet received event builder
           PacketReceivedBuilder pktInBuilder = new PacketReceivedBuilder();
           pktInBuilder.setPayload(message.getData());

           // get the DPID
           GetFeaturesOutput features = sc.getFeatures();
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

           //TODO: need to get the NodeConnectorRef, but NodeConnectors aren't there yet
           InstanceIdentifier<NodeConnector> nci = ncIndentifierFromDPIDandPort(dpid, port);
           NodeConnectorRef ncr = new NodeConnectorRef(nci);
           PacketReceived pktInEvent = pktInBuilder.build();
            return pktInEvent;
        } else {
            return null;
        }
    }

    public static InstanceIdentifier<NodeConnector> ncIndentifierFromDPIDandPort(BigInteger dpid, Long port) {
        InstanceIdentifierBuilder<?> builder = InstanceIdentifier.builder().node(Node.class);

        // TODO: this doesn't work yet, needs to actaully get the ref for the real NodeConnector
        //       but that doesn't exist yet
        NodeConnectorKey ncKey = ncKeyFromDPIDandPort(dpid, port);
        return builder.node(NodeConnector.class, ncKey).toInstance();
    }


    public static NodeConnectorKey ncKeyFromDPIDandPort(BigInteger dpid, Long port){
        return new NodeConnectorKey(ncIDfromDPIDandPort(dpid, port));
    }

    public static NodeConnectorId ncIDfromDPIDandPort(BigInteger dpid, Long port){
        return new NodeConnectorId("openflow:"+dpid.toString()+":"+port.toString());
    }
}
