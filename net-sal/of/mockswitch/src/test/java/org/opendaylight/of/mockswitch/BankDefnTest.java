/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.junit.Test;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;

import java.util.Iterator;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.mockswitch.AbstractDefn.EOL;
import static org.opendaylight.of.mockswitch.BankDefn.SwitchInfo;
import static org.opendaylight.of.mockswitch.CmdPortStatus.State;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test for BankDefn.
 *
 * @author Simon Hunt
 */
public class BankDefnTest extends SwTest {

    private static final String ROOT = "org/opendaylight/of/mockswitch/";
    private static final String BANK_ONE = ROOT + "switchBankOne.def";
    private static final String BANK_PSS = ROOT + "switchBankPortStateScenario.def";
    private static final String NO_CONTENT = ROOT + "switchBankNoContent.def";
    private static final String BAD_SW_REF = ROOT + "switchBankBadSwitchRef.def";

    private static final String SW10 = "SW10";
    private static final String SW13 = "SW13";

    private void verifySw(SwitchInfo si, String expVar, String expFname,
                          String expDpid, ProtocolVersion expPv, int expPorts) {
        assertEquals(AM_NEQ, expVar, si.getVar());
        assertEquals(AM_NEQ, expFname, si.getFname());
        MockOpenflowSwitch sw = si.getSwitch();
        assertEquals(AM_NEQ, DataPathId.valueOf(expDpid), sw.getDpid());
        assertEquals(AM_NEQ, expPv, sw.getDefn().getCfgHello().getMaxVersion());
        assertEquals(AM_NEQ, expPorts, sw.getDefn().getCfgFeat().getPortCount());
    }

    private void verifyCmd(ScenarioCommand cmd, CommandType expType, int expMs) {
        assertEquals(AM_NEQ, expType, cmd.getType());
        assertEquals(AM_NEQ, expMs, ((CmdDelay)cmd).getMsDelay());
    }

    private void verifyCmd(ScenarioCommand cmd, CommandType expType, String var) {
        assertEquals(AM_NEQ, expType, cmd.getType());
        assertEquals(AM_NEQ, var, ((CmdSwitchVerb)cmd).getVar());
    }

    private void verifyCmd(ScenarioCommand cmd, CommandType expType,
                           String expSw, int expPort,
                           State expState) {
        assertEquals(AM_NEQ, expType, cmd.getType());
        assertEquals(AM_NEQ, expSw, ((CmdPortStatus)cmd).getSwid());
        assertEquals(AM_NEQ, expPort, ((CmdPortStatus)cmd).getPortNum());
        assertEquals(AM_NEQ, expState, ((CmdPortStatus) cmd).getState());
    }


    @Test
    public void basic() {
        print(EOL + "basic()");
        BankDefn bank = new BankDefn(BANK_ONE, showOutput);
        print(bank.toDebugString());

        Iterator<SwitchInfo> swIter = bank.getSwitches().iterator();
        verifySw(swIter.next(), SW10, "simple10sw4port.def",
                "42/0016b9:006502", V_1_0, 4);
        verifySw(swIter.next(), SW13, "simple13sw32port.def",
                "42/0016b9:068000", V_1_3, 32);

        Iterator<ScenarioCommand> cmdIter = bank.getCommands().iterator();
        verifyCmd(cmdIter.next(), CommandType.DELAY, 1000);
        verifyCmd(cmdIter.next(), CommandType.ACTIVATE, SW10);
        verifyCmd(cmdIter.next(), CommandType.DELAY, 200);
        verifyCmd(cmdIter.next(), CommandType.ACTIVATE, SW13);
        verifyCmd(cmdIter.next(), CommandType.DELAY, 4000);
        verifyCmd(cmdIter.next(), CommandType.DEACTIVATE, SW13);
        verifyCmd(cmdIter.next(), CommandType.DELAY, 10);
        verifyCmd(cmdIter.next(), CommandType.DEACTIVATE, SW10);
        assertFalse(cmdIter.hasNext());
    }

    @Test
    public void noContent() {
        print(EOL + "noContent()");
        BankDefn bank = new BankDefn(NO_CONTENT, showOutput);
        print(bank.toDebugString());
        assertEquals(AM_UXS, 0, bank.getSwitches().size());
        assertEquals(AM_UXS, 0, bank.getCommands().size());
    }

    @Test
    public void badSwitchRef() {
        print(EOL + "badSwitchRef()");
        try {
            new BankDefn(BAD_SW_REF, showOutput);
            fail(AM_NOEX);
        } catch (RuntimeException e) {
            print("EX> {}", e);
            assertEquals(AM_NEQ,
                    "Unable to read file: \"org/opendaylight/of/mockswitch/noSuchFile.def\"",
                    e.getMessage());
        }
    }

    @Test
    public void portStateScenario() {
        print(EOL + "portStateScenario()");
        BankDefn bank = new BankDefn(BANK_PSS, showOutput);
        print(bank.toDebugString());

        Iterator<ScenarioCommand> cmdIter = bank.getCommands().iterator();
        verifyCmd(cmdIter.next(), CommandType.DELAY, 10);
        verifyCmd(cmdIter.next(), CommandType.ACTIVATE, SW10);

        verifyCmd(cmdIter.next(), CommandType.DELAY, 20);
        verifyCmd(cmdIter.next(), CommandType.PORT_STATUS, SW10, 3, State.DOWN);

        verifyCmd(cmdIter.next(), CommandType.DELAY, 20);
        verifyCmd(cmdIter.next(), CommandType.PORT_STATUS, SW10, 1, State.UP);

        verifyCmd(cmdIter.next(), CommandType.DELAY, 20);
        verifyCmd(cmdIter.next(), CommandType.PORT_STATUS, SW10, 3, State.UP);

        assertFalse(cmdIter.hasNext());
    }

}
