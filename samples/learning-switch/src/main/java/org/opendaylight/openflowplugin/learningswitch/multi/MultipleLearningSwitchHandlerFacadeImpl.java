/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.learningswitch.multi;

import org.opendaylight.openflowplugin.learningswitch.DataChangeListenerRegistrationPublisher;
import org.opendaylight.openflowplugin.learningswitch.OFDataStoreAccessor;
import org.opendaylight.openflowplugin.learningswitch.SimpleLearningSwitchHandler;
import org.opendaylight.openflowplugin.learningswitch.SimpleLearningSwitchHandlerImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class MultipleLearningSwitchHandlerFacadeImpl implements SimpleLearningSwitchHandler {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(MultipleLearningSwitchHandlerFacadeImpl.class);
    
    private OFDataStoreAccessor dataStoreAccessor;
    private PacketProcessingService packetProcessingService;
    

    private InstanceIdentifier<Node> nodePath;

    private PacketInDispatcherImpl packetInDispatcher;

    @Override
    public synchronized void onSwitchAppeared(InstanceIdentifier<Table> appearedTablePath) {
        LOG.debug("expected table acquired, learning ..");
       
        nodePath = appearedTablePath.firstIdentifierOf(Node.class);
        
        if (!packetInDispatcher.getHandlerMapping().containsKey(nodePath)) {
            // delegate this node (owning appearedTable) to SimpleLearningSwitchHandler  
            SimpleLearningSwitchHandlerImpl simpleLearningSwitch = new SimpleLearningSwitchHandlerImpl();
            simpleLearningSwitch.setDataStoreAccessor(dataStoreAccessor);
            simpleLearningSwitch.setPacketProcessingService(packetProcessingService);
            simpleLearningSwitch.onSwitchAppeared(appearedTablePath);
            packetInDispatcher.getHandlerMapping().put(nodePath, simpleLearningSwitch);
        }
    }

    @Override
    public void setRegistrationPublisher(
            DataChangeListenerRegistrationPublisher registrationPublisher) {
        //NOOP
    }
    
    @Override
    public void setDataStoreAccessor(OFDataStoreAccessor dataStoreAccessor) {
        this.dataStoreAccessor = dataStoreAccessor;
    }
    
    @Override
    public void setPacketProcessingService(
            PacketProcessingService packetProcessingService) {
        this.packetProcessingService = packetProcessingService;
    }

    /**
     * @param packetInDispatcher
     */
    public void setPacketInDispatcher(PacketInDispatcherImpl packetInDispatcher) {
        this.packetInDispatcher = packetInDispatcher;
    }
    
}
