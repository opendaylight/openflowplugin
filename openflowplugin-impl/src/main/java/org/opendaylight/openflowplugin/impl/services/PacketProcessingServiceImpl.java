/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import java.util.concurrent.Future;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * @author joe
 * 
 */
// TODO: implement this
public class PacketProcessingServiceImpl extends CommonService implements PacketProcessingService {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService#transmitPacket
     * (org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput)
     */
    @Override
    public Future<RpcResult<Void>> transmitPacket(final TransmitPacketInput input) {
        // TODO Auto-generated method stub
        return null;
    }
}
