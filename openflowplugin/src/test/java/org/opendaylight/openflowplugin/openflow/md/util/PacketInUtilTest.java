/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.util;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.InvalidTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.NoMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.SendToController;

import static org.junit.Assert.assertTrue;

/**
 * Created by Martin Bobak mbobak@cisco.com on 7/2/14.
 */
public class PacketInUtilTest {


    /**
     * Test method for PacketInUtil#getMdSalPacketInReason(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason);
     */
    @Test
    public void testGetMdSalPacketInReason() {
        Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketInReason> resultReason;

        resultReason = PacketInUtil.getMdSalPacketInReason(PacketInReason.OFPRACTION);
        assertTrue(resultReason.getName().equals(SendToController.class.getName()));

        resultReason = PacketInUtil.getMdSalPacketInReason(PacketInReason.OFPRINVALIDTTL);
        assertTrue(resultReason.getName().equals(InvalidTtl.class.getName()));

        resultReason = PacketInUtil.getMdSalPacketInReason(PacketInReason.OFPRNOMATCH);
        assertTrue(resultReason.getName().equals(NoMatch.class.getName()));

    }
}
