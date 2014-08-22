/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.util.ByteUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Unit tests for PacketUtils.
 *
 * @author Simon Hunt
 */
public class PacketUtilsTest {

    private static final ClassLoader CL = PacketUtilsTest.class.getClassLoader();
    private static final String LLDP_PATH = "org/opendaylight/of/lib/msg/lldpPacket.hex";

    private static final String EXP_LLDP =
            "[[ETHERNET, LLDP], dst=01:80:c2:00:00:0e, src=08:2e:5f:69:c4:7b]";


    private byte[] lldpPacket() {
        try {
            return ByteUtils.slurpBytesFromHexFile(LLDP_PATH, CL);
        } catch (IOException e) {
            Assert.fail("Unable to load file: " + LLDP_PATH);
        }
        return null;
    }

    @Test
    public void lldp() {
        print("lldp()");
        byte[] data = lldpPacket();
        String result = PacketUtils.packetSummary(data, BufferId.NO_BUFFER);
        print(result);
        assertEquals(AM_NEQ, EXP_LLDP, result);
    }

    // TODO: write unit tests for unhappy paths
}
