package org.opendaylight.openflowplugin.openflow.md.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.common.port.Configuration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.common.port.ConfigurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PortTranslatorUtil {
    public static  org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures translatePortFeatures(PortFeatures apf) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures napf = null;
        if(apf != null){
                napf = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures(
                        apf.is_100gbFd(),apf.is_100mbFd(),apf.is_100mbHd(),apf.is_10gbFd(),apf.is_10mbFd(),apf.is_10mbHd(),
                        apf.is_1gbFd(),apf.is_1gbHd(),apf.is_1tbFd(),apf.is_40gbFd(),apf.isAutoneg(),apf.isCopper(),apf.isFiber(),apf.isOther(),
                        apf.isPause(), apf.isPauseAsym());
        }
        return napf;
    }

    public static  org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortState translatePortState(PortState state) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortState nstate = null;
        if(state != null) {
            if(state.isBlocked()) {
                nstate = org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortState.Blocked;
            } else if (state.isLinkDown()) {
                nstate = org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortState.LinkDown;
            } else if (state.isLive()) {
                nstate = org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortState.Live;
            }
        }
        return nstate;
    }

    public static Configuration translatePortConfig(PortConfig pc) {
        Configuration con = null;
        if(pc != null) {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setPortDown(pc.isPortDown());
            cb.setPortNoFwd(pc.isNoFwd());
            cb.setPortNoPacketIn(pc.isNoPacketIn());
            cb.setPortNoRecv(pc.isNoRecv());
            con = cb.build();
        }
        
        return con;
    }
    
    public static org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber nodeConnectorRefToPortNumber(NodeConnectorRef ncref) {
        InstanceIdentifier<Node> nInstanceId = ncref.getValue().firstIdentifierOf(Node.class);
        NodeId nId = InstanceIdentifier.keyOf(nInstanceId).getId();
        InstanceIdentifier<NodeConnector> ncInstanceId = ncref.getValue().firstIdentifierOf(NodeConnector.class);
        NodeConnectorId ncId = InstanceIdentifier.keyOf(ncInstanceId).getId();
        return nodeConnectorIdToPortNumber(nId,ncId);
        
    }

    private static PortNumber nodeConnectorIdToPortNumber(NodeId nId, NodeConnectorId ncId) {
        String portNumberString = ncId.getValue().replace(nId.getValue(), "");
        Long portNumberLong = Long.parseLong(portNumberString);
        PortNumber pn = new PortNumber(portNumberLong);
        return pn;
    }
}
