/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.impl;


import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.controller.InitialFlowContributor;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.dt.DataPathInfoAdapter;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.msg.FlowModCommand;
import org.opendaylight.of.lib.msg.OfmFlowMod;
import org.opendaylight.of.lib.msg.OfmMutableFlowMod;
import org.opendaylight.util.junit.TestLogger;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.MatchFactory.createMatch;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageType.FLOW_MOD;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link InitialFlowManager}.
 *
 * @author Simon Hunt
 */
public class InitialFlowManagerTest {
    // mock 1.3 device info
    private static class MyInfo extends DataPathInfoAdapter {
        @Override
        public ProtocolVersion negotiated() {
            return V_1_3;
        }
    }

    private static enum Bad {
        NOT_BAD, PV_MISMATCH, MUTABLE_FM,
        NULL_MATCH, NULL_CMD, NO_TABLE_ID
    }

    private static final TableId TABLE_ID = TableId.valueOf(3);

    private static class MyContrib implements InitialFlowContributor {
        private final int id;
        private final int numFlows;

        private Bad badness = Bad.NOT_BAD;

        public MyContrib(int id, int numFlows) {
            this.id = id;
            this.numFlows = numFlows;
        }

        public InitialFlowContributor bad(Bad badness) {
            this.badness = badness;
            return this;
        }

        @Override
        public String toString() {
            return "MyContrib{" +
                    "id=" + id +
                    ", numFlows=" + numFlows +
                    ", badness=" + badness +
                    '}';
        }

        @Override
        public List<OfmFlowMod> provideInitialFlows(DataPathInfo info,
                                                    boolean isHybrid) {
            List<OfmFlowMod> flows = new ArrayList<>();
            for (int i = 0; i < numFlows; i++)
                flows.add(createFlow(i));
            return flows;
        }

        private OfmFlowMod createFlow(int index) {
            OfmMutableFlowMod fm = null;

            switch (badness) {
                case PV_MISMATCH:
                    fm = createFlow(V_1_0, id, index);
                    break;

                case MUTABLE_FM:
                    return createFlow(V_1_3, id, index);

                case NULL_CMD:
                case NULL_MATCH:
                    return incompleteFlow13(id, index, badness);

                case NOT_BAD:
                    // no evil machinations to perform...
                    fm = createFlow(V_1_3, id, index);
                    break;

                case NO_TABLE_ID:
                    // not really bad, since tableId can be null
                    fm = createFlow(V_1_3, id, index, false);
                    break;

                default:
                    fail("Missing badness switch case!! " + badness);
                    break;
            }
            return (OfmFlowMod) fm.toImmutable();
        }

        private OfmFlowMod incompleteFlow13(int id, int index, Bad badness) {
            OfmMutableFlowMod fm = (OfmMutableFlowMod) create(V_1_3, FLOW_MOD);
            // we'll set the table id, although not doing so should be valid
            fm.tableId(TABLE_ID);
            fm.hardTimeout(id).idleTimeout(index); // just markers
            switch (badness) {
                case NULL_MATCH:
                    fm.command(FlowModCommand.ADD);
                    // DON'T set the match
                    break;
                case NULL_CMD:
                    // DON'T set the command
                    fm.match(MATCH_13);
                    break;
            }
            return (OfmFlowMod) fm.toImmutable();
        }

        private OfmMutableFlowMod createFlow(ProtocolVersion pv,
                                             int id, int index) {
            // include a table-id
            return createFlow(pv, id, index, true);
        }

        private OfmMutableFlowMod createFlow(ProtocolVersion pv,
                                             int id, int index,
                                             boolean tableIdIncluded) {
            OfmMutableFlowMod fm = (OfmMutableFlowMod)
                    create(pv, FLOW_MOD, FlowModCommand.ADD);
            fm.hardTimeout(id).idleTimeout(index); // just markers

            if (pv == V_1_3 && tableIdIncluded)
                fm.tableId(TABLE_ID);
            fm.match(pv.ge(V_1_3) ? MATCH_13 : MATCH_10);
            return fm;
        }
    }

    private static final Match MATCH_10 =
            (Match) createMatch(V_1_0).toImmutable();
    private static final Match MATCH_13 =
            (Match) createMatch(V_1_3).toImmutable();

    private static final DataPathInfo INFO = new MyInfo();

    private static final MyContrib BASE_CONTRIB = new MyContrib(0, 0);

    private static final TestLogger tlog = new TestLogger();

    private InitialFlowManager ifm;
    private List<OfmFlowMod> fms;

    @BeforeClass
    public static void classSetUp() {
        InitialFlowManager.setLogger(tlog);
    }

    @AfterClass
    public static void classTearDown() {
        InitialFlowManager.restoreLogger();
    }

    @Before
    public void setUp() {
        ifm = new InitialFlowManager();
    }

    @Test(expected = NullPointerException.class)
    public void nullRegister() {
        ifm.register(null);
    }

    @Test(expected = NullPointerException.class)
    public void nullUnregister() {
        ifm.unregister(null);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        print(ifm);
        assertEquals(AM_UXS, 0, ifm.size());
        fms = ifm.collateFlowMods(INFO, false);
        print(fms);
        assertEquals(AM_UXS, 0, fms.size());
    }

    @Test
    public void addAndRemoveContrib() {
        print(EOL + "addAndRemoveContrib()");
        print(ifm);
        assertEquals(AM_UXS, 0, ifm.size());

        ifm.register(BASE_CONTRIB);
        print(ifm);
        assertEquals(AM_UXS, 1, ifm.size());

        // re-registering is idempotent
        ifm.register(BASE_CONTRIB);
        print(ifm);
        assertEquals(AM_UXS, 1, ifm.size());

        ifm.unregister(BASE_CONTRIB);
        print(ifm);
        assertEquals(AM_UXS, 0, ifm.size());
    }

    private void showFlows(List<OfmFlowMod> fms) {
        for (OfmFlowMod m: fms) {
            print(m);
            print("---> {} / {}", m.getHardTimeout(), m.getIdleTimeout());
        }
    }

    private void addTwoGoodContribs() {
        ifm.register(new MyContrib(111, 1));
        ifm.register(new MyContrib(222, 2));
    }

    private void addBadContrib(Bad badness) {
        ifm.register(new MyContrib(333, 3).bad(badness));
    }

    @Test
    public void contributions() {
        print(EOL + "contributions()");
        addTwoGoodContribs();
        print(ifm);
        assertEquals(AM_UXS, 2, ifm.size());

        fms = ifm.collateFlowMods(INFO, false);
        showFlows(fms);
        assertEquals(AM_UXS, 3, fms.size());
    }

    @Test
    public void brokenContributorMutable() {
        print(EOL + "brokenContributorMutable()");
        addTwoGoodContribs();
        addBadContrib(Bad.MUTABLE_FM);
        print(ifm);
        assertEquals(AM_UXS, 3, ifm.size());

        fms = ifm.collateFlowMods(INFO, false);
        tlog.assertWarningContains("Error: Mutable FlowMod:");
        showFlows(fms);
        assertEquals(AM_UXS, 3, fms.size());
    }

    @Test
    public void brokenContributorMismatch() {
        print(EOL + "brokenContributorMismatch()");
        addTwoGoodContribs();
        addBadContrib(Bad.PV_MISMATCH);
        print(ifm);
        assertEquals(AM_UXS, 3, ifm.size());

        fms = ifm.collateFlowMods(INFO, false);
        tlog.assertWarningContains("Error: Version mismatch.");
        showFlows(fms);
        assertEquals(AM_UXS, 3, fms.size());
    }

    @Test
    public void fmWithNoCommand() {
        print(EOL + "fmWithNoCommand()");
        addBadContrib(Bad.NULL_CMD);
        print(ifm);
        assertEquals(AM_UXS, 1, ifm.size());

        fms = ifm.collateFlowMods(INFO, false);
        tlog.assertWarningContains("Error: FlowMod Incomplete:");
        showFlows(fms);
        assertEquals(AM_UXS, 0, fms.size());
    }

    @Test
    public void fmWithNoMatch() {
        print(EOL + "fmWithNoMatch()");
        addBadContrib(Bad.NULL_MATCH);
        print(ifm);
        assertEquals(AM_UXS, 1, ifm.size());

        fms = ifm.collateFlowMods(INFO, false);
        tlog.assertWarningContains("Error: FlowMod Incomplete:");
        showFlows(fms);
        assertEquals(AM_UXS, 0, fms.size());
    }

    @Test
    public void fmWithNoTableId() {
        print(EOL + "fmWithNoTableId()");
        addBadContrib(Bad.NO_TABLE_ID);
        print(ifm);
        assertEquals(AM_UXS, 1, ifm.size());

        fms = ifm.collateFlowMods(INFO, false);
        tlog.assertWarning(false);
        showFlows(fms);
        assertEquals(AM_UXS, 3, fms.size());
    }

}
