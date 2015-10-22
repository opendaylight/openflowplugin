/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device.handlers;

import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;

/**
 * @author tkubas
 */
public interface DeviceReplyProcessor {

    /**
     * Method that set future to context in Map
     *
     * @param ofHeader
     */
    void processReply(OfHeader ofHeader);

    /**
     * Method that set future to context in Map
     *
     * @param xid,
     * @param ofHeaderList
     */
    void processReply(Xid xid, List<MultipartReply> ofHeaderList);

    /**
     * Method process async flow removed from device
     *
     * @param flowRemoved
     */
    void processFlowRemovedMessage(FlowRemoved flowRemoved);

    /**
     * Method process async port status from device
     *
     * @param portStatus
     */
    void processPortStatusMessage(PortStatusMessage portStatus);

    /**
     * Method process async packet in from device
     *
     * @param packetInMessage
     */
    void processPacketInMessage(PacketInMessage packetInMessage);

    /**
     * Processing of experimenter symmetric message from device
     *
     * @param notification
     */
    void processExperimenterMessage(ExperimenterMessage notification);
}
