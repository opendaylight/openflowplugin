/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.VersionNotSupportedException;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.MacAddress;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.MessageType.PORT_MOD;
import static org.opendaylight.of.lib.msg.PortConfig.*;
import static org.opendaylight.of.lib.msg.PortFeature.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for the OfmPortMod message.
 *
 * @author Simon Hunt
 */
public class OfmPortModTest extends OfmTest {

    // Test files...
    private static final String TF_PM_13 = "v13/portMod";
    private static final String TF_PM_12 = "v12/portMod";
    private static final String TF_PM_11 = "v11/portMod";
    private static final String TF_PM_10 = "v10/portMod";

    private static final int EXP_v11v12v13_MSG_LEN = 40;
    private static final int EXP_v10_MSG_LEN = 32;

    private static final BigPortNumber EXP_PORT = bpn(0x79);
    private static final MacAddress EXP_HW = mac("3344aa:000012");

    private static final PortConfig[] EXP_CFG = {NO_RECV, NO_FWD};
    private static final PortConfig[] EXP_MASK = {NO_RECV, NO_FWD, NO_PACKET_IN};
    private static final PortFeature[] EXP_ADV = {RATE_1GB_FD, FIBER, AUTONEG};

    private static final Set<PortConfig> EXP_CFG_SET =
            new TreeSet<PortConfig>(Arrays.asList(EXP_CFG));
    private static final Set<PortConfig> EXP_MASK_SET =
            new TreeSet<PortConfig>(Arrays.asList(EXP_MASK));
    private static final Set<PortFeature> EXP_ADV_SET =
            new TreeSet<PortFeature>(Arrays.asList(EXP_ADV));

    // ========================================================= PARSING ====

    @Test
    public void portMod13() {
        print(EOL + "portMod13()");
        OfmPortMod msg = (OfmPortMod) verifyMsgHeader(TF_PM_13, V_1_3,
                PORT_MOD, EXP_v11v12v13_MSG_LEN);

        assertEquals(AM_NEQ, EXP_PORT, msg.getPort());
        assertEquals(AM_NEQ, EXP_HW, msg.getHwAddress());
        verifyFlags(msg.getConfig(), EXP_CFG);
        verifyFlags(msg.getConfigMask(), EXP_MASK);
        verifyFlags(msg.getAdvertise(), EXP_ADV);
    }

    @Test
    public void portMod12() {
        print(EOL + "portMod12()");
        verifyNotSupported(TF_PM_12);
//        OfmPortMod msg = (OfmPortMod) verifyMsgHeader(TF_PM_12, V_1_2,
//                PORT_MOD, EXP_v11v12v13_MSG_LEN);
//
//        assertEquals(AM_NEQ, EXP_PORT, msg.getPort());
//        assertEquals(AM_NEQ, EXP_HW, msg.getHwAddress());
//        verifyFlags(msg.getConfig(), EXP_CFG);
//        verifyFlags(msg.getConfigMask(), EXP_MASK);
//        verifyFlags(msg.getAdvertise(), EXP_ADV);
    }

    @Test
    public void portMod11() {
        print(EOL + "portMod11()");
        verifyNotSupported(TF_PM_11);
//        OfmPortMod msg = (OfmPortMod) verifyMsgHeader(TF_PM_11, V_1_1,
//                PORT_MOD, EXP_v11v12v13_MSG_LEN);
//
//        assertEquals(AM_NEQ, EXP_PORT, msg.getPort());
//        assertEquals(AM_NEQ, EXP_HW, msg.getHwAddress());
//        verifyFlags(msg.getConfig(), EXP_CFG);
//        verifyFlags(msg.getConfigMask(), EXP_MASK);
//        verifyFlags(msg.getAdvertise(), EXP_ADV);
    }

    @Test
    public void portMod10() {
        print(EOL + "portMod10()");
        OfmPortMod msg = (OfmPortMod) verifyMsgHeader(TF_PM_10, V_1_0,
                PORT_MOD, EXP_v10_MSG_LEN);

        assertEquals(AM_NEQ, EXP_PORT, msg.getPort());
        assertEquals(AM_NEQ, EXP_HW, msg.getHwAddress());
        verifyFlags(msg.getConfig(), EXP_CFG);
        verifyFlags(msg.getConfigMask(), EXP_MASK);
        verifyFlags(msg.getAdvertise(), EXP_ADV);
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodePortMod13() {
        print(EOL + "encodePortMod13()");
        OfmMutablePortMod mod =
                (OfmMutablePortMod) MessageFactory.create(V_1_3, PORT_MOD);
        mod.clearXid();
        verifyMutableHeader(mod, V_1_3, PORT_MOD, 0);
        mod.port(EXP_PORT).hwAddress(EXP_HW).config(EXP_CFG_SET)
                .configMask(EXP_MASK_SET).advertise(EXP_ADV_SET);
        // now encode and verify
        encodeAndVerifyMessage(mod.toImmutable(), TF_PM_13);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodePortMod12() {
        MessageFactory.create(V_1_2, PORT_MOD);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodePortMod11() {
        MessageFactory.create(V_1_1, PORT_MOD);
    }

    @Test
    public void encodePortMod10() {
        print(EOL + "encodePortMod10()");
        OfmMutablePortMod mod =
                (OfmMutablePortMod) MessageFactory.create(V_1_0, PORT_MOD);
        mod.clearXid();
        verifyMutableHeader(mod, V_1_0, PORT_MOD, 0);
        mod.port(EXP_PORT).hwAddress(EXP_HW).config(EXP_CFG_SET)
                .configMask(EXP_MASK_SET).advertise(EXP_ADV_SET);
        // now encode and verify
        encodeAndVerifyMessage(mod.toImmutable(), TF_PM_10);
    }
}
