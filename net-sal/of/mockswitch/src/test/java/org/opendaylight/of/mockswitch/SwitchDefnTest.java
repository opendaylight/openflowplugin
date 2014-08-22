/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.junit.Test;
import org.opendaylight.of.lib.dt.DataPathId;

import java.util.Iterator;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.Capability.*;
import static org.opendaylight.of.lib.msg.PortConfig.*;
import static org.opendaylight.of.lib.msg.PortFeature.*;
import static org.opendaylight.of.lib.msg.SupportedAction.*;
import static org.opendaylight.of.mockswitch.SwitchDefn.CustomProp;
import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.net.IpAddress.LOOPBACK_IPv4;

/**
 * Unit tests for SwitchDefn.
 *
 * @author Simon Hunt
 */
public class SwitchDefnTest extends SwTest {

    private static final String SIMPLE_10 = "org/opendaylight/of/mockswitch/simple10sw4port.def";
    private static final String SIMPLE_13 = "org/opendaylight/of/mockswitch/simple13sw32port.def";

    private static final DataPathId EXP_DPID_10 =
            DataPathId.valueOf("42/0016b9:006502");
    private static final DataPathId EXP_DPID_13 =
            DataPathId.valueOf("42/0016b9:068000");

    @Test
    public void simple13sw32port() {
        print(EOL + "simple13sw32port()");
        SwitchDefn defn = new SwitchDefn(SIMPLE_13);
        print(defn.toDebugString());

        // DPID
        assertEquals(AM_NEQ, EXP_DPID_13, defn.getDpid());

        // HELLO
        CfgHello hello = defn.getCfgHello();
        assertEquals(AM_NEQ, CfgHello.Behavior.EAGER, hello.getBehavior());
        verifyFlags(hello.getVersions(), V_1_0, V_1_2, V_1_3);
        assertFalse(AM_HUH, hello.isLegacy());

        // BASE
        CfgBase base = defn.getCfgBase();
        assertEquals(AM_NEQ, LOOPBACK_IPv4, base.getControllerAddress());
        assertEquals(AM_NEQ, 6633, base.getOpenflowPort());
        assertEquals(AM_NEQ, 256, base.getBufferCount());
        assertEquals(AM_NEQ, 12, base.getTableCount());
        verifyFlags(base.getCapabilities(),
                FLOW_STATS, TABLE_STATS, PORT_STATS, IP_REASM, QUEUE_STATS);

        // FEATURES
        CfgFeat feat = defn.getCfgFeat();
        assertNull(AM_HUH, feat.getSuppActs());

        assertEquals(AM_NEQ, 32, feat.getPortCount());
        assertEquals(AM_NEQ, "00:16:b9:0d:01",
                feat.getPortMac().toString().toLowerCase());
        verifyFlags(feat.getPortConfig());
        verifyFlags(feat.getPortFeatures(), RATE_1GB_FD, FIBER);

        verifyFlags(feat.getPortConfig(0), NO_FWD);
        verifyFlags(feat.getPortConfig(1), NO_FWD, PORT_DOWN);
        verifyFlags(feat.getPortConfig(2));
        verifyFlags(feat.getPortConfig(3));
        // assume rest are ok
        verifyFlags(feat.getPortConfig(31), NO_PACKET_IN);

        verifyFlags(feat.getPortFeatures(0), RATE_1GB_FD, FIBER);
        verifyFlags(feat.getPortFeatures(1), RATE_1GB_FD, FIBER);
        verifyFlags(feat.getPortFeatures(2), RATE_1GB_FD, FIBER);
        // assume rest are okay
        verifyFlags(feat.getPortFeatures(31), RATE_1GB_FD, FIBER);

        // CUSTOM PROPERTIES
        assertEquals(AM_UXS, 0, defn.getCustomProps().size());

    }

    @Test
    public void simple10sw4port() {
        print(EOL + "simple10sw4port()");
        SwitchDefn defn = new SwitchDefn(SIMPLE_10);
        print(defn.toDebugString());

        // DPID
        assertEquals(AM_NEQ, EXP_DPID_10, defn.getDpid());

        // HELLO
        CfgHello hello = defn.getCfgHello();
        assertEquals(AM_NEQ, CfgHello.Behavior.LAZY, hello.getBehavior());
        verifyFlags(hello.getVersions(), V_1_0);
        assertTrue(AM_HUH, hello.isLegacy());

        // BASE
        CfgBase base = defn.getCfgBase();
        assertEquals(AM_NEQ, LOOPBACK_IPv4, base.getControllerAddress());
        assertEquals(AM_NEQ, 6633, base.getOpenflowPort());
        assertEquals(AM_NEQ, 256, base.getBufferCount());
        assertEquals(AM_NEQ, 12, base.getTableCount());
        verifyFlags(base.getCapabilities(),
                FLOW_STATS, TABLE_STATS, PORT_STATS, IP_REASM, QUEUE_STATS);

        // FEATURES
        CfgFeat feat = defn.getCfgFeat();
        verifyFlags(feat.getSuppActs(), OUTPUT, SET_VLAN_VID, STRIP_VLAN,
                SET_DL_SRC, SET_DL_DST, SET_NW_SRC, SET_NW_DST);

        assertEquals(AM_NEQ, 4, feat.getPortCount());
        assertEquals(AM_NEQ, "00:16:b9:0a:01",
                feat.getPortMac().toString().toLowerCase());
        verifyFlags(feat.getPortConfig(), NO_RECV);
        verifyFlags(feat.getPortFeatures(), RATE_100MB_FD, COPPER);

        verifyFlags(feat.getPortConfig(0), NO_RECV);
        verifyFlags(feat.getPortConfig(1), NO_RECV, PORT_DOWN);
        verifyFlags(feat.getPortConfig(2), NO_RECV);
        verifyFlags(feat.getPortConfig(3), NO_RECV);

        verifyFlags(feat.getPortFeatures(0), RATE_100MB_FD, COPPER);
        verifyFlags(feat.getPortFeatures(1), RATE_100MB_FD, COPPER, AUTONEG);
        verifyFlags(feat.getPortFeatures(2), RATE_100MB_FD, COPPER);
        verifyFlags(feat.getPortFeatures(3), RATE_100MB_FD, COPPER);
        try {
            feat.getPortConfig(4);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print("EX> {}", e);
        }

        // CUSTOM PROPERTIES
        print("now the custom properties...");
        Iterator<CustomProp> pIter = defn.getCustomProps().iterator();
        verifyProp(pIter.next(), "foo", "value for FOO");
        verifyProp(pIter.next(), "bar", "value for BAR");
        verifyProp(pIter.next(), "bazAndSoOn", "more values...");
    }

    private void verifyProp(CustomProp prop, String expName, String expValue) {
        print(prop);
        assertEquals(AM_NEQ, expName, prop.name());
        assertEquals(AM_NEQ, expValue, prop.value());
    }

}
