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
import org.opendaylight.openflowplugin.api.openflow.device.exception.DeviceDataException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.*;

/**
 *
 * @author tkubas
 *
 */
public interface DeviceReplyProcessor {

    /**
     * Method that set future to context in Map
     * @param ofHeader
     */
    public void processReply(OfHeader ofHeader);

    /**
     * Method that set future to context in Map
     * @param xid,
     * @param ofHeaderList
     */
    public void processReply(Xid xid, List<MultipartReply> ofHeaderList);

    /**
     * Method that set exception to the future
     * @param xid,
     * @param deviceDataException
     */
    public void processException(Xid xid, DeviceDataException deviceDataException);

    /**
     * Method process async flow removed from device
     * @param flowRemoved
     */
    public void processFlowRemovedMessage(FlowRemoved flowRemoved);

    /**
     * Method process async port status from device
     * @param portStatus
     */
    public void processPortStatusMessage(PortStatusMessage portStatus);

    /**
     * Method process async packet in from device
     * @param packetInMessage
     */
    public void processPacketInMessage(PacketInMessage packetInMessage);

}
