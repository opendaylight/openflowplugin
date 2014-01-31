/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.learningswitch;

import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;

/**
 * 
 */
public interface LearningSwitchManager {

    /**
     * stop manager
     */
    void stop();

    /**
     * start manager
     */
    void start();

    /**
     * inject {@link DataBrokerService}
     * @param data
     */
    void setData(DataBrokerService data);

    /**
     * inject {@link PacketProcessingService}
     * @param packetProcessingService
     */
    void setPacketProcessingService(
            PacketProcessingService packetProcessingService);

    /**
     * inject {@link NotificationService}
     * @param notificationService
     */
    void setNotificationService(NotificationService notificationService);
}
