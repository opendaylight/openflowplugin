/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.learningswitch;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * 
 */
public interface SimpleLearningSwitchHandler extends PacketProcessingListener {

    /**
     * @param tablePath 
     */
    void onSwitchAppeared(InstanceIdentifier<Table> tablePath);

    /**
     * @param packetProcessingService the packetProcessingService to set
     */
   void setPacketProcessingService(PacketProcessingService packetProcessingService);

   /**
    * @param dataStoreAccessor the dataStoreAccessor to set
    */
   void setDataStoreAccessor(OFDataStoreAccessor dataStoreAccessor);

   /**
    * @param registrationPublisher the registrationPublisher to set
    */
   void setRegistrationPublisher(DataChangeListenerRegistrationPublisher registrationPublisher);
}
