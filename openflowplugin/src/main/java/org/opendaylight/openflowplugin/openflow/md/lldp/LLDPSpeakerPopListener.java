package org.opendaylight.openflowplugin.openflow.md.lldp;

import org.opendaylight.openflowplugin.openflow.md.queue.PopListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LLDPSpeakerPopListener<NodeConnectorUpdated> implements PopListener<NodeConnectorUpdated> {


    @Override
    public void onPop(NodeConnectorUpdated connector) {
       FlowCapableNodeConnectorUpdated flowConnector = connector.<FlowCapableNodeConnectorUpdated>getAugmentation(FlowCapableNodeConnectorUpdated.class);
       if(flowConnector != null) {
	    InstanceIdentifier<NodeConnector> nodeConnectorInstanceId = (InstanceIdentifier<NodeConnector>) connector.getNodeConnectorRef().getValue();       	
	    NodeConnectorBuilder ncb = new NodeConnectorBuilder(connector);
	    FlowCapableNodeConnectorBuilder fcncb = new FlowCapableNodeConnectorBuilder(flowConnector);
	    ncb.addAugmentation(FlowCapableNodeConnector.class, fcncb.build());
	    PortState portState = flowConnector.getState();
	    PortConfig portConfig = flowConnector.getConfiguration();
	    if((portState ==null || !portState.equals(PortState.LinkDown)) && (portConfig == null || !portConfig.isPORTDOWN())) {
	         LLDPSpeaker.getInstance().addNodeConnector(nodeConnectorInstanceId,ncb.build());
	    } else {
	         LLDPSpeaker.getInstance().removeNodeConnector(nodeConnectorInstanceId,ncb.build());
	    }
        }
    } 
}


