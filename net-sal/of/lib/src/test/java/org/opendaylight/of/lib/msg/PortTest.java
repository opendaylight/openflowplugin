/*
 * (c) Copyright 2013-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.AbstractTest;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.PortNumber;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.Port.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for Port.
 *
 * @author Simon Hunt
 */
public class PortTest extends AbstractTest {

    private void verifyLookup(PortNumber pn, BigPortNumber bpn) {
        BigPortNumber u32 = Port.equivalentSpecial(pn);
        PortNumber u16 = Port.equivalentSpecial(bpn);
        print("  {} => {} => {}", Port.portNumberToString(pn),
                Port.portNumberToString(u32, V_1_3),
                Port.portNumberToString(u16));
        assertEquals(AM_NEQ, bpn, u32);
        assertEquals(AM_NEQ, pn, u16);
    }

    @Test
    public void specialLookups() {
        print(EOL + "specialLookups()");
        verifyLookup(MAX_V10, MAX);
        verifyLookup(IN_PORT_V10, IN_PORT);
        verifyLookup(TABLE_V10, TABLE);
        verifyLookup(NORMAL_V10, NORMAL);
        verifyLookup(FLOOD_V10, FLOOD);
        verifyLookup(ALL_V10, ALL);
        verifyLookup(CONTROLLER_V10, CONTROLLER);
        verifyLookup(LOCAL_V10, LOCAL);
        verifyLookup(NONE_V10, ANY);
    }


    private void verifyStandard(ProtocolVersion pv, long pn, boolean expResult) {
        BigPortNumber bpn = BigPortNumber.valueOf(pn);
        boolean result = Port.isStandardPort(bpn, pv);
        print("{} => {}", Port.portNumberToString(bpn, pv), result);
        assertEquals(AM_NEQ, expResult, result);
    }

    @Test
    public void standardPorts10() {
        print(EOL + "standardPorts10()");
        verifyStandard(V_1_0, 0, false);
        verifyStandard(V_1_0, 1, true);
        verifyStandard(V_1_0, 2, true);
        verifyStandard(V_1_0, 0xfeff, true);
        verifyStandard(V_1_0, 0xff00, true);
        verifyStandard(V_1_0, 0xff01, false);
        verifyStandard(V_1_0, 0xfffe, false);
        verifyStandard(V_1_0, 0xffff, false);
    }

    @Test
    public void standardPorts13() {
        print(EOL + "standardPorts13()");
        verifyStandard(V_1_3, 0, false);
        verifyStandard(V_1_3, 1, true);
        verifyStandard(V_1_3, 2, true);
        verifyStandard(V_1_3, 0xfeff, true);
        verifyStandard(V_1_3, 0xff00, true);
        verifyStandard(V_1_3, 0xff01, true);
        verifyStandard(V_1_3, 0xfffe, true);
        verifyStandard(V_1_3, 0xffff, true);
        verifyStandard(V_1_3, 0xfffffeffL, true);
        verifyStandard(V_1_3, 0xffffff00L, true);
        verifyStandard(V_1_3, 0xffffff01L, false);
        verifyStandard(V_1_3, 0xfffffffeL, false);
        verifyStandard(V_1_3, 0xffffffffL, false);
    }

    @Test
    public void portAnyAndNone() {
        print(EOL + "portAnyAndNone()");
        BigPortNumber bpn = Port.ANY;

        print(Port.portNumberToString(bpn, V_1_3));
        assertEquals(AM_NEQ, "0xffffffff(ANY)",
                Port.portNumberToString(bpn, V_1_3));

        print(Port.portNumberToString(bpn, V_1_0));
        assertEquals(AM_NEQ, "0xffff(NONE)",
                Port.portNumberToString(bpn, V_1_0));
    }

    private void verifyLogicalName(ProtocolVersion pv, BigPortNumber bpn,
                                   String exp) {
        String s = Port.logicalName(bpn, pv);
        print("{} {} => '{}'", pv, bpn, s);
        assertEquals(AM_NEQ, exp, s);
    }

    @Test
    public void portLogicalName() {
        print(EOL + "portLogicalName()");
        verifyLogicalName(V_1_0, Port.MAX, "MAX");
        verifyLogicalName(V_1_0, Port.IN_PORT, "IN_PORT");
        verifyLogicalName(V_1_0, Port.TABLE, "TABLE");
        verifyLogicalName(V_1_0, Port.NORMAL, "NORMAL");
        verifyLogicalName(V_1_0, Port.FLOOD, "FLOOD");
        verifyLogicalName(V_1_0, Port.ALL, "ALL");
        verifyLogicalName(V_1_0, Port.CONTROLLER, "CONTROLLER");
        verifyLogicalName(V_1_0, Port.LOCAL, "LOCAL");
        verifyLogicalName(V_1_0, Port.NONE, "NONE");

        verifyLogicalName(V_1_0, null, null);
        verifyLogicalName(V_1_0, bpn(0), null);
        verifyLogicalName(V_1_0, bpn(1), null);
        verifyLogicalName(V_1_0, bpn(2), null);
        verifyLogicalName(V_1_0, bpn(0xfffffff0L), null);

        print("--");
        verifyLogicalName(V_1_3, Port.MAX, "MAX");
        verifyLogicalName(V_1_3, Port.IN_PORT, "IN_PORT");
        verifyLogicalName(V_1_3, Port.TABLE, "TABLE");
        verifyLogicalName(V_1_3, Port.NORMAL, "NORMAL");
        verifyLogicalName(V_1_3, Port.FLOOD, "FLOOD");
        verifyLogicalName(V_1_3, Port.ALL, "ALL");
        verifyLogicalName(V_1_3, Port.CONTROLLER, "CONTROLLER");
        verifyLogicalName(V_1_3, Port.LOCAL, "LOCAL");
        verifyLogicalName(V_1_3, Port.ANY, "ANY");

        verifyLogicalName(V_1_3, null, null);
        verifyLogicalName(V_1_3, bpn(0), null);
        verifyLogicalName(V_1_3, bpn(1), null);
        verifyLogicalName(V_1_3, bpn(2), null);
        verifyLogicalName(V_1_3, bpn(0xfffffff0L), null);
        print("--");

        verifyLogicalName(V_1_3, Port.FLOOD, "FLOOD");
        verifyLogicalName(V_1_3, bpn(0xfffffffbL), "FLOOD");

        verifyLogicalName(V_1_0, Port.FLOOD, "FLOOD");
        verifyLogicalName(V_1_0, bpn(0xfffffffbL), "FLOOD");
        verifyLogicalName(V_1_0, bpn(0xfffbL), null);

        verifyLogicalName(V_1_3, Port.ANY, "ANY");
        verifyLogicalName(V_1_3, Port.NONE, "ANY");
        verifyLogicalName(V_1_3, bpn(0xffffffffL), "ANY");

        verifyLogicalName(V_1_0, Port.ANY, "NONE");
        verifyLogicalName(V_1_0, Port.NONE, "NONE");
        verifyLogicalName(V_1_0, bpn(0xffffffffL), "NONE");
        verifyLogicalName(V_1_0, bpn(0xffffL), null);
    }
    
    private void verifyBigPortNumber(String port, BigPortNumber exp) {
        BigPortNumber act = Port.getBigPortNumber(port);
        assertEquals(AM_NEQ, exp, act);
    }
    
    @Test
    public void getBigPortNumber() {
        print(EOL + "getBigPortNumber()");
        verifyBigPortNumber("max", Port.MAX);
        verifyBigPortNumber("in_port", Port.IN_PORT);
        verifyBigPortNumber("table", Port.TABLE);
        verifyBigPortNumber("normal", Port.NORMAL);
        verifyBigPortNumber("flood", Port.FLOOD);
        verifyBigPortNumber("all", Port.ALL);
        verifyBigPortNumber("controller", Port.CONTROLLER);
        verifyBigPortNumber("local", Port.LOCAL);
        verifyBigPortNumber("none", Port.NONE);
        verifyBigPortNumber("any", Port.ANY);
        verifyBigPortNumber("23", BigPortNumber.valueOf(23));
        verifyBigPortNumber("0xbeef", BigPortNumber.valueOf("0xbeef"));
        verifyBigPortNumber("", null);
        verifyBigPortNumber(null, null);
    }
    
    private void verifyPortNumber(String port, PortNumber exp) {
        PortNumber act = Port.getPortNumber(port);
        assertEquals(AM_NEQ, exp, act);
    }
    
    @Test
    public void getPortNumber() {
        print(EOL + "getPortNumber()");
        verifyPortNumber("max", Port.MAX_V10);
        verifyPortNumber("in_port", Port.IN_PORT_V10);
        verifyPortNumber("table", Port.TABLE_V10);
        verifyPortNumber("normal", Port.NORMAL_V10);
        verifyPortNumber("flood", Port.FLOOD_V10);
        verifyPortNumber("all", Port.ALL_V10);
        verifyPortNumber("controller", Port.CONTROLLER_V10);
        verifyPortNumber("local", Port.LOCAL_V10);
        verifyPortNumber("none", Port.NONE_V10);
        verifyPortNumber("23", PortNumber.valueOf(23));
        verifyPortNumber("0xbeef", PortNumber.valueOf("0xbeef"));
        verifyPortNumber("", null);
        verifyPortNumber(null, null);
    }

    private void verifyLogicalNumber(ProtocolVersion pv, BigPortNumber bpn,
                                     String exp) {
        String result = Port.getLogicalNumber(bpn, pv);
        print("{}: {} => '{}'", pv, bpn, result);
        assertEquals(AM_NEQ, exp, result);
    }

    @Test
    public void getLogicalNumber13() {
        print(EOL + "getLogicalNumber13()");
        verifyLogicalNumber(V_1_3, Port.MAX, "MAX");
        verifyLogicalNumber(V_1_3, Port.IN_PORT, "IN_PORT");
        verifyLogicalNumber(V_1_3, Port.TABLE, "TABLE");
        verifyLogicalNumber(V_1_3, Port.NORMAL, "NORMAL");
        verifyLogicalNumber(V_1_3, Port.FLOOD, "FLOOD");
        verifyLogicalNumber(V_1_3, Port.ALL, "ALL");
        verifyLogicalNumber(V_1_3, Port.CONTROLLER, "CONTROLLER");
        verifyLogicalNumber(V_1_3, Port.LOCAL, "LOCAL");
        verifyLogicalNumber(V_1_3, Port.ANY, "ANY");
        verifyLogicalNumber(V_1_3, bpn(7), "7");
        verifyLogicalNumber(V_1_3, bpn(1), "1");
        verifyLogicalNumber(V_1_3, bpn(23), "23");
        verifyLogicalNumber(V_1_3, bpn(0x17), "23");
    }

    @Test
    public void getLogicalNumber10() {
        print(EOL + "getLogicalNumber10()");
        verifyLogicalNumber(V_1_0, Port.MAX, "MAX");
        verifyLogicalNumber(V_1_0, Port.IN_PORT, "IN_PORT");
        verifyLogicalNumber(V_1_0, Port.TABLE, "TABLE");
        verifyLogicalNumber(V_1_0, Port.NORMAL, "NORMAL");
        verifyLogicalNumber(V_1_0, Port.FLOOD, "FLOOD");
        verifyLogicalNumber(V_1_0, Port.ALL, "ALL");
        verifyLogicalNumber(V_1_0, Port.CONTROLLER, "CONTROLLER");
        verifyLogicalNumber(V_1_0, Port.LOCAL, "LOCAL");
        verifyLogicalNumber(V_1_0, Port.NONE, "NONE");
        verifyLogicalNumber(V_1_0, bpn(7), "7");
        verifyLogicalNumber(V_1_0, bpn(1), "1");
        verifyLogicalNumber(V_1_0, bpn(23), "23");
        verifyLogicalNumber(V_1_0, bpn(0x17), "23");
    }

}
