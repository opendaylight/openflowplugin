/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import org.junit.Test;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.mp.MBodyFlowStats;
import org.opendaylight.of.lib.mp.MBodyMutableFlowStats;
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.net.MacAddress;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.FlowUtils.flowKey;
import static org.opendaylight.of.lib.FlowUtils.sameFlow;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.MatchFactory.createMatch;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.of.lib.mp.MpBodyFactory.createReplyBodyElement;
import static org.opendaylight.util.junit.TestTools.*;


/**
 * Unit tests for FlowUtils.
 *
 * @author Simon Hunt
 */
public class FlowUtilsTest extends OfmMultipartTest {

    private static final TableId TABLE_1 = tid(1);
    private static final TableId TABLE_2 = tid(2);
    private static final int PRI_20 = 20;
    private static final int PRI_30 = 30;
    private static final BigPortNumber PORT_7 = bpn(7);

    private static final Match MATCH_13_ALL = (Match) createMatch(V_1_3)
            .toImmutable();
    private static final Match MATCH_10_ALL = (Match) createMatch(V_1_0)
            .toImmutable();

    private static final Match MATCH_13_PORT = (Match) createMatch(V_1_3)
            .addField(createBasicField(V_1_3, IN_PORT, PORT_7)).toImmutable();
    private static final Match MATCH_10_PORT = (Match) createMatch(V_1_0)
            .addField(createBasicField(V_1_0, IN_PORT, PORT_7)).toImmutable();

    private static final MacAddress MAC_SRC = mac("111111:222222");
    private static final MacAddress MAC_DST = mac("333333:444444");

    // fabricate a flow stats element
    private static MBodyFlowStats flowStats(TableId tid, int pri, Match match) {
        ProtocolVersion pv = match.getVersion();
        MBodyMutableFlowStats mfs = (MBodyMutableFlowStats)
                createReplyBodyElement(pv, MultipartType.FLOW);
        mfs.tableId(tid).priority(pri).match(match);
        return (MBodyFlowStats) mfs.toImmutable();
    }

    private static final MBodyFlowStats[] FLOWS_13 = {
            flowStats(TABLE_1, PRI_20, MATCH_13_ALL),
            flowStats(TABLE_1, PRI_20, MATCH_13_PORT),
            flowStats(TABLE_1, PRI_30, MATCH_13_ALL),
            flowStats(TABLE_1, PRI_30, MATCH_13_PORT),
            flowStats(TABLE_2, PRI_20, MATCH_13_ALL),
            flowStats(TABLE_2, PRI_20, MATCH_13_PORT),
            flowStats(TABLE_2, PRI_30, MATCH_13_ALL),
            flowStats(TABLE_2, PRI_30, MATCH_13_PORT),
    };

    private static final MBodyFlowStats[] FLOWS_10 = {
            flowStats(TABLE_1, PRI_20, MATCH_10_ALL),
            flowStats(TABLE_1, PRI_20, MATCH_10_PORT),
            flowStats(TABLE_1, PRI_30, MATCH_10_ALL),
            flowStats(TABLE_1, PRI_30, MATCH_10_PORT),
    };

    private static final MBodyFlowStats FLOW_13_A =
            flowStats(TABLE_1, PRI_20, MATCH_13_ALL);
    private static final MBodyFlowStats FLOW_13_B =
            flowStats(TABLE_2, PRI_30, MATCH_13_PORT);

    private static final MBodyFlowStats FLOW_10_A =
            flowStats(TABLE_1, PRI_20, MATCH_10_ALL);
    private static final MBodyFlowStats FLOW_10_B =
            flowStats(TABLE_2, PRI_20, MATCH_10_ALL);

    @Test
    public void basic() {
        print(EOL + "basic()");
        MBodyFlowStats a = flowStats(TABLE_1, PRI_20, MATCH_13_ALL);
        MBodyFlowStats b = flowStats(TABLE_1, PRI_20, MATCH_13_ALL);
        assertNotSame(AM_HUH, a, b);
        print(a.toDebugString());
        assertTrue(AM_HUH, sameFlow(a, b));

        // same except for table id - should NOT be considered equal...
        b = flowStats(TABLE_2, PRI_20, MATCH_13_ALL);
        print(b.toDebugString());
        assertFalse(AM_HUH, sameFlow(a, b));
    }


    // === SAME FLOW tests

    private void verifyCombos(MBodyFlowStats... flows) {
        final int len = flows.length;
        for (int i=0; i<len; i++)
            for (int j=0; j<len; j++)
                assertEquals(AM_HUH, i==j, sameFlow(flows[i], flows[j]));
    }

    @Test
    public void allCombos13() {
        print(EOL + "allCombos13()");
        verifyCombos(FLOWS_13);
    }

    @Test
    public void allCombos10() {
        print(EOL + "allCombos10()");
        verifyCombos(FLOWS_10);
    }

    @Test(expected = NullPointerException.class)
    public void nullFirstFlow() {
        sameFlow(null, FLOW_13_B);
    }

    @Test(expected = NullPointerException.class)
    public void nullSecondFlow() {
        sameFlow(FLOW_13_A, null);
    }

    @Test
    public void mismatchFlows() {
        assertFalse(AM_HUH, sameFlow(FLOW_10_A, FLOW_13_A));
    }

    @Test
    public void sameFlowTableIdIgnoredFor10() {
        print(EOL + "sameFlowTableIdIgnoredFor10()");
        assertFalse(AM_HUH, FLOW_10_A.getTableId().equals(FLOW_10_B.getTableId()));
        assertTrue(AM_HUH, sameFlow(FLOW_10_A, FLOW_10_B));
    }

    // === FLOW KEY tests

    private void verifyKeys(MBodyFlowStats... flows) {
        final int len = flows.length;
        for (int i=0; i<len; i++) {
            print("flow: {} => key: {}", flows[i], flowKey(flows[i]));
            for (int j=0; j<len; j++)
                assertEquals(AM_HUH, i==j, flowKey(flows[i])==flowKey(flows[j]));
        }
    }

    @Test
    public void allKeys13() {
        print(EOL + "allKeys13()");
        verifyKeys(FLOWS_13);
    }

    @Test
    public void allKeys10() {
        print(EOL + "allKeys10()");
        verifyKeys(FLOWS_10);
    }

    @Test(expected = NullPointerException.class)
    public void nullFlowKeysStats() {
        flowKey((MBodyFlowStats) null);
    }

    @Test(expected = NullPointerException.class)
    public void nullFlowKeysMod() {
        flowKey((OfmFlowMod) null);
    }

    @Test
    public void flowKeyTableIdIgnoredFor10() {
        print(EOL + "flowKeyTableIdIgnoredFor10()");
        assertFalse(AM_HUH, FLOW_10_A.getTableId().equals(FLOW_10_B.getTableId()));
        assertEquals(AM_NEQ, flowKey(FLOW_10_A), flowKey(FLOW_10_B));
    }

    @Test
    public void flowModMBodyStatsKeys() {
        print(EOL + "flowModMBodyStatsKeys()");
        OfmMutableFlowMod fm = (OfmMutableFlowMod)
                MessageFactory.create(V_1_3, MessageType.FLOW_MOD);
        fm.tableId(TABLE_1).priority(PRI_20).match(MATCH_13_ALL);
        OfmFlowMod f = (OfmFlowMod) fm.toImmutable();
        MBodyFlowStats fs = flowStats(TABLE_1, PRI_20, MATCH_13_ALL);
        assertEquals(AM_NEQ, flowKey(fm), flowKey(f));
        assertEquals(AM_NEQ, flowKey(fm), flowKey(fs));
    }


    //===
    /*
     * The order of match fields may vary (where there are no pre-requisite
     * dependencies), so we must be lenient in that regard...
     */

    private static final Match M_A = (Match) createMatch(V_1_3)
            .addField(createBasicField(V_1_3, ETH_TYPE, EthernetType.IPv4))
            .addField(createBasicField(V_1_3, ETH_SRC, MAC_SRC))
            .addField(createBasicField(V_1_3, ETH_DST, MAC_DST))
            .toImmutable();

    private static final Match M_B = (Match) createMatch(V_1_3)
            .addField(createBasicField(V_1_3, ETH_TYPE, EthernetType.IPv4))
            .addField(createBasicField(V_1_3, ETH_DST, MAC_DST))
            .addField(createBasicField(V_1_3, ETH_SRC, MAC_SRC))
            .toImmutable();


    @Test
    public void orderIndependentMatch() {
        print(EOL + "orderIndependentMatch()");
        print(M_A.toDebugString());
        print(M_B.toDebugString());
        // Match fields are now auto-sorted by the library, regardless of
        // the order of addition to the mutable match...
        assertTrue(AM_HUH, M_A.getMatchFields().equals(M_B.getMatchFields()));
        assertTrue("Match.equals() failed", M_A.equals(M_B));
    }

    @Test
    public void orderIndependentMatchWithinFlow() {
        print(EOL + "orderIndependentMatchWithinFlow()");
        MBodyFlowStats fsA = flowStats(TABLE_1, PRI_20, M_A);
        MBodyFlowStats fsB = flowStats(TABLE_1, PRI_20, M_B);
        assertTrue("sameFlow() failed", sameFlow(fsA, fsB));
    }

}
