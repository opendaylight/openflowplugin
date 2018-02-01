/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.learningswitch;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;

public interface LearningSwitchManager {

    /**
     * Stops this manager.
     */
    void stop();

    /**
     * Starts this manager.
     */
    void start();

    /**
     * Set's Data Broker dependency.
     *
     * <p>
     * Data Broker is used to access overall operational and configuration tree.
     *
     * <p>
     * In simple Learning Switch handler, data broker is used to listen
     * for changes in Openflow tables and to configure flows which will
     * be provisioned down to the Openflow switch.
     *
     * <p>
     * inject {@link DataBroker}
     */
    void setDataBroker(DataBroker data);

    /**
     * Set's Packet Processing dependency.
     *
     * <p>
     * Packet Processing service is used to send packet Out on Openflow switch.
     *
     * <p>
     * inject {@link PacketProcessingService}
     */
    void setPacketProcessingService(
            PacketProcessingService packetProcessingService);

    /**
     * Set's Notification service dependency.
     *
     * <p>
     * Notification service is used to register for listening packet-in notifications.
     *
     * <p>
     * inject {@link NotificationService}
     */
    void setNotificationService(NotificationService notificationService);
}
