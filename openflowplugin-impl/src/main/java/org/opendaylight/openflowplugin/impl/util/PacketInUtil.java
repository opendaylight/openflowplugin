/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.InvalidTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.NoMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.SendToController;

public final class PacketInUtil {
    private PacketInUtil() {
        // Hidden on purpose
    }

    /**
     * Get MDSAL packet-in reason.
     *
     * @param reason openflow java packet in reason
     * @return corresponding MD-SAL reason class for given OF-API reason
     */
    public static PacketInReason getMdSalPacketInReason(
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason reason) {
        // FIXME: use a switch expression
        PacketInReason resultReason = PacketInReason.VALUE;

        if (reason
                .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason
                                .OFPRNOMATCH)) {
            resultReason = NoMatch.VALUE;
        } else if (reason
                .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason
                                .OFPRINVALIDTTL)) {
            resultReason = InvalidTtl.VALUE;
        } else if (reason
                .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason
                                .OFPRACTION)) {
            resultReason = SendToController.VALUE;
        }
        return resultReason;
    }
}
