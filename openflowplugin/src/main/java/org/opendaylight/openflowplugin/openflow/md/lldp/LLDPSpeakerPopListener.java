/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
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

public class LLDPSpeakerPopListener<T> implements PopListener<T> {


    @Override
    public void onPop(T processedMessage) {
        if(processedMessage instanceof NodeConnectorUpdated) {
        	NodeConnectorUpdated connector = (NodeConnectorUpdated) processedMessage;
        	FlowCapableNodeConnectorUpdated flowConnector = connector.<FlowCapableNodeConnectorUpdated>getAugmentation(FlowCapableNodeConnectorUpdated.class);
        	if(flowConnector != null) {
	        	InstanceIdentifier<NodeConnector> nodeConnectorInstanceId = (InstanceIdentifier<NodeConnector>) connector.getNodeConnectorRef().getValue();       	
	        	NodeConnectorBuilder ncb = new NodeConnectorBuilder(connector);
	        	FlowCapableNodeConnectorBuilder fcncb = new FlowCapableNodeConnectorBuilder(flowConnector);
	        	ncb.addAugmentation(FlowCapableNodeConnector.class, fcncb.build());
	        	PortState portState = flowConnector.getState();
	        	PortConfig portConfig = flowConnector.getConfiguration();
	        	if((portState ==null || !portState.isLinkDown()) && (portConfig == null || !portConfig.isPORTDOWN())) {
	        		LLDPSpeaker.getInstance().addNodeConnector(nodeConnectorInstanceId,ncb.build());
	        	} else {
	        		LLDPSpeaker.getInstance().removeNodeConnector(nodeConnectorInstanceId,ncb.build());
	        	}
        	}
        } 
    }

}
