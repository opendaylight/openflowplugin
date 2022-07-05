/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.InvalidTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.NoMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.SendToController;

/**
 * Created by Martin Bobak mbobak@cisco.com on 7/2/14.
 */
public class PacketInUtilTest {
    /**
     * Test method for PacketInUtil#getMdSalPacketInReason(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow
     * .common.types.rev130731.PacketInReason).
     */
    @Test
    public void testGetMdSalPacketInReason() {
        assertEquals(SendToController.VALUE, PacketInUtil.getMdSalPacketInReason(PacketInReason.OFPRACTION));
        assertEquals(InvalidTtl.VALUE, PacketInUtil.getMdSalPacketInReason(PacketInReason.OFPRINVALIDTTL));
        assertEquals(NoMatch.VALUE, PacketInUtil.getMdSalPacketInReason(PacketInReason.OFPRNOMATCH));
    }
}
