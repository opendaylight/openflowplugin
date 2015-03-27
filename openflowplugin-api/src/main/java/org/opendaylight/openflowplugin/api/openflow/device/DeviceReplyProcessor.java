/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import java.util.List;

import org.opendaylight.openflowplugin.api.openflow.device.exception.DeviceDataException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

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
    public void processReply(Xid xid, List<OfHeader> ofHeaderList);

    /**
     * Method that set exception to the future
     * @param xid,
     * @param deviceDataException
     */
    public void processException(Xid xid, DeviceDataException deviceDataException);

}
