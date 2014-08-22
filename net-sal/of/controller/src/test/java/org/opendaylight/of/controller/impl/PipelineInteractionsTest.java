/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.opendaylight.of.controller.impl.PipelineSwitch.Expect;
import org.opendaylight.of.controller.pipeline.PipelineDefinition;
import org.opendaylight.of.controller.pipeline.TableContext;
import org.opendaylight.of.controller.pipeline.impl.PipelineManager;
import org.opendaylight.of.controller.pipeline.impl.PipelineTestUtils;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.*;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.match.MatchFactory;
import org.opendaylight.of.lib.match.MutableMatch;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.junit.SlowTests;
import org.opendaylight.util.junit.TestLogger;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.EthernetType;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.of.lib.instr.ActionFactory.createActionSetField;
import static org.opendaylight.of.lib.instr.InstructionFactory.createInstruction;
import static org.opendaylight.of.lib.instr.InstructionFactory.createMutableInstruction;
import static org.opendaylight.of.lib.instr.InstructionType.GOTO_TABLE;
import static org.opendaylight.of.lib.instr.InstructionType.METER;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.of.lib.msg.MessageType.FLOW_MOD;
import static org.opendaylight.of.lib.msg.TableFeaturePropType.INSTRUCTIONS;
import static org.opendaylight.of.lib.msg.TableFeaturePropType.INSTRUCTIONS_MISS;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test cases for switch interactions for table pipeline.
 *
 * @author Radhika Hegde
 * @author Pramod Shanbhag
 * @author Simon Hunt
 */
// FIXME: TOTAL REVIEW REQUIRED - since PipelineManager needs rework/nixing.
@Category(SlowTests.class)
public class PipelineInteractionsTest extends AbstractControllerTest {

    private static final DataPathId DPID_13 = SW13P32_DPID;
    // simple13sw32port.def
    private static final String DEF_13 = SW13P32;

    private static final ProtocolVersion pv = V_1_3;
    private static final Set<FlowModFlag> FM_FLAGS = 
            EnumSet.of(FlowModFlag.SEND_FLOW_REM);
    private static final BigPortNumber OUT_PORT = bpn(3);
    private static final OfmFlowMod CUSTOM_FLOW_1 = customFlow1();

    // default flow miss rule related constants
    private static final int EXP_IDLE = 0;
    private static final int EXP_HARD = 0;
    private static final int EXP_PRI = 0;
    private static final long EXP_COOKIE = 0;
    private static final BufferId EXP_BID = BufferId.NO_BUFFER;

    // logger
    private static final TestLogger tlog = new TestLogger();

    private PipelineManager pmgr;

    @After
    public void tearDown() {
        cmgr.shutdown();
    }

    @BeforeClass
    public static void classSetUp() {
        Assume.assumeTrue(!isUnderCoverage());
        PipelineTestUtils.setLogger(tlog);
    }

    @AfterClass
    public static void classTearDown() {
        PipelineTestUtils.restoreLogger();
    }

    private ControllerConfig defaultConfig() {
        return new ControllerConfig.Builder().build();
    }

    private void initController() {
        alertSink = new AlertLogger();
        roleAdvisor = new MockRoleAdvisor();
        eds = new MockEventDispatcher();

        cmgr = new ControllerManager(DEFAULT_CTRL_CFG, alertSink, PH_SINK,
                FM_ADV, roleAdvisor, eds);
        cs = cmgr;
        cmgr.startIOProcessing();
        lmgr = cmgr.getListenerManager();
        pmgr = (PipelineManager)cmgr.getPipelineMgr();

        cmgr.updateNonBounceConfig(defaultConfig());
        print("... controller activated ...");
    }

    private PipelineSwitch connectSwitch(DataPathId dpid, String def, Expect exp) {
        switchesReady = new CountDownLatch(1);
        lmgr.setDataPathAddedLatch(switchesReady);
        PipelineSwitch sw = null;
        try {
            sw = new PipelineSwitch(dpid, def, exp);
            sw.activate();
            print("... switch activated : {} ...", sw.getDpid());
            waitForHandshake();
        } catch (IOException e) {
            print(e);
            fail(AM_UNEX);
        }
        return sw;
    }

    private void disconnectSwitch(PipelineSwitch sw) {
        switchesGone = new CountDownLatch(1);
        lmgr.setDataPathRemovedLatch(switchesGone);
        sw.deactivate();
        waitForDisconnect();
    }


    private static OfmFlowMod customFlow1() {
        // not adding table ID purposely so that PipelineReader can patch.
        OfmMutableFlowMod m =
                (OfmMutableFlowMod) MessageFactory.create(V_1_3,
                                     MessageType.FLOW_MOD, FlowModCommand.ADD);
        m.bufferId(BufferId.NO_BUFFER).outPort(OUT_PORT).flowModFlags(FM_FLAGS);

        //Add match.
        MutableMatch mm = MatchFactory.createMatch(V_1_3)
            .addField(createBasicField(V_1_3, IN_PORT, bpn(2)))
            .addField(createBasicField(V_1_3,IN_PHY_PORT, bpn(3)))
            .addField(createBasicField(V_1_3, METADATA, 0xf1f2f3f4f5f6f7f8L,
                                       0xff00ff00ff00ff00L))
            .addField(createBasicField(V_1_3, ETH_DST, mac("112233:445567"),
                                       mac("ffffff:000000")))
            .addField(createBasicField(V_1_3, ETH_SRC, mac("112233:445566"),
                                       mac("ffffff:000000")))
            .addField(createBasicField(V_1_3, ETH_TYPE, EthernetType.IPv4));
        m.match((Match) mm.toImmutable());

        // Add Instructions and actions
        InstrMutableAction write =
                createMutableInstruction(V_1_3, InstructionType.WRITE_ACTIONS)
                 .addAction(createAction(V_1_3, ActionType.OUTPUT, bpn(2)))
                 .addAction(createAction(V_1_3, ActionType.COPY_TTL_OUT))
                 .addAction(createActionSetField(V_1_3, ETH_SRC,
                                                 mac("00001e:453411")))
                 .addAction(createActionSetField(V_1_3, ETH_DST,
                                                 mac("00001e:453422")));
        m.addInstruction((Instruction) write.toImmutable());
        return (OfmFlowMod) m.toImmutable();
    }

    private void verifyCustomDefinition(PipelineDefinition def) {
        Set<TableId> tids = def.getTableIds();
        assertNotNull(AM_HUH, tids);
        assertEquals(AM_VMM, 2, tids.size());

        Iterator<TableId> it = tids.iterator();
        verifyFirstTableProperties(def.getTableContext(it.next()));
        verifySecondTableProperties(def.getTableContext(it.next()));
    }

    private void verifyFirstTableProperties(TableContext tc) {
        assertNotNull(AM_HUH, tc);
        assertEquals(AM_NEQ, 0 ,tc.tableId().toInt());
        assertEquals(AM_NEQ, 1 ,tc.maxEntries());
        // next tables
        assertEquals(AM_NEQ, true, tc.containsNextTable(tid(1)));
        assertEquals(AM_NEQ, true, tc.containsNextTableMiss(tid(1)));

        // supported capability
        assertEquals(AM_NEQ, true,
                     tc.supportsCapability(INSTRUCTIONS, GOTO_TABLE));
        assertEquals(AM_NEQ, true,
                     tc.supportsCapability(INSTRUCTIONS_MISS, GOTO_TABLE));
        //  unsupported capability
        assertEquals(AM_NEQ, false,
                     tc.supportsCapability(INSTRUCTIONS, METER));
    }

    private void verifySecondTableProperties(TableContext tc) {
        assertNotNull(AM_HUH, tc);
        assertEquals(AM_NEQ, 1 ,tc.tableId().toInt());
        assertEquals(AM_NEQ, 64000 ,tc.maxEntries());
        // next tables
        assertEquals(AM_NEQ, false, tc.containsNextTable(tid(1)));
        assertEquals(AM_NEQ, false, tc.containsNextTableMiss(tid(1)));

        // supported capability
        assertEquals(AM_NEQ, true,
                     tc.supportsCapability(INSTRUCTIONS,
                                           InstructionType.APPLY_ACTIONS));
        assertEquals(AM_NEQ, true,
                     tc.supportsCapability(INSTRUCTIONS_MISS,
                                           InstructionType.APPLY_ACTIONS));
        assertEquals(AM_NEQ, true,
                     tc.supportsCapability(TableFeaturePropType.APPLY_ACTIONS,
                                           ActionType.OUTPUT));
        //  unsupported capability
        assertEquals(AM_NEQ, false,
                     tc.supportsCapability(INSTRUCTIONS, GOTO_TABLE));
    }

    private void verifyDefaultFlows() throws OpenflowException {
        verifyDefaultFlowForFirstTable();
        verifyDefaultFlowForSecondTable();
    }

    private void verifyDefaultFlowForFirstTable() throws OpenflowException {
        OfmFlowMod mod = createDefaultFlowForFirstTable();
        Set<TableId> tids = pmgr.align(mod, DPID_13);
        assertNotNull(AM_HUH, tids);
        assertEquals(AM_NEQ, 1, tids.size());
        Iterator<TableId> it = tids.iterator();
        assertEquals(AM_VMM, 0, it.next().toInt());
    }

    private OfmFlowMod createDefaultFlowForFirstTable() {
        OfmMutableFlowMod mutFlowMod = (OfmMutableFlowMod)
                MessageFactory.create(pv, FLOW_MOD);
        // all match fields wild carded
        // table id not set
        MutableMatch mutMatch = MatchFactory.createMatch(pv);
        mutFlowMod.match((Match) mutMatch.toImmutable());
        mutFlowMod.command(FlowModCommand.ADD).idleTimeout(EXP_IDLE)
            .hardTimeout(EXP_HARD).cookie(EXP_COOKIE).bufferId(EXP_BID)
            .priority(EXP_PRI).outPort(Port.ANY).outGroup(GroupId.ANY)
            .flowModFlags(FM_FLAGS);
        Instruction ins = createInstruction(pv, GOTO_TABLE, tid(1));
        mutFlowMod.addInstruction(ins);
        return (OfmFlowMod)mutFlowMod.toImmutable();
    }

    private void verifyDefaultFlowForSecondTable() throws OpenflowException {
        OfmFlowMod mod = createDefaultFlowForSecondTable();
        Set<TableId> tids = pmgr.align(mod, DPID_13);
        assertNotNull(AM_HUH, tids);
        assertEquals(AM_NEQ, 1, tids.size());
        Iterator<TableId> it = tids.iterator();
        assertEquals(AM_VMM, 1, it.next().toInt());
    }

    private OfmFlowMod createDefaultFlowForSecondTable() {
        OfmMutableFlowMod mutFlowMod = (OfmMutableFlowMod)
                MessageFactory.create(pv, FLOW_MOD);
        // all match fields wild carded
        // table id not set
        MutableMatch mutMatch = MatchFactory.createMatch(pv);
        mutFlowMod.match((Match) mutMatch.toImmutable());
        mutFlowMod.command(FlowModCommand.ADD).idleTimeout(EXP_IDLE)
            .hardTimeout(EXP_HARD).cookie(EXP_COOKIE).bufferId(EXP_BID)
            .priority(EXP_PRI).outPort(Port.ANY).outGroup(GroupId.ANY)
            .flowModFlags(FM_FLAGS);
        InstrMutableAction mutIns =
                createMutableInstruction(pv, InstructionType.APPLY_ACTIONS);
        mutIns.addAction(createAction(pv, ActionType.OUTPUT, Port.CONTROLLER,
                                      ActOutput.CONTROLLER_MAX));
        mutFlowMod.addInstruction((Instruction)
                                  mutIns.toImmutable());
        return (OfmFlowMod)mutFlowMod.toImmutable();
    }

    private static OfmFlowMod createPathDeamonARPFlow() {
        // not adding table ID purposely so that PipelineReader can patch.
        OfmMutableFlowMod m =
                (OfmMutableFlowMod) MessageFactory.create(V_1_3,
                                     MessageType.FLOW_MOD, FlowModCommand.ADD);
        m.bufferId(BufferId.NO_BUFFER).outPort(OUT_PORT).flowModFlags(FM_FLAGS);

        //Add match.
        MutableMatch mm = MatchFactory.createMatch(V_1_3)
            .addField(createBasicField(V_1_3, IN_PORT, bpn(11)))
            .addField(createBasicField(V_1_3, ETH_DST, mac("112233:445567")))
            .addField(createBasicField(V_1_3, ETH_SRC, mac("112233:445566")))
            .addField(createBasicField(V_1_3, ETH_TYPE, EthernetType.ARP))
            .addField(createBasicField(V_1_3, ARP_SPA, ip("10.250.100.23")))
            .addField(createBasicField(V_1_3, ARP_TPA, ip("10.250.100.24")));
        m.match((Match) mm.toImmutable());

        // Add Instructions and actions
        InstrMutableAction write =
                createMutableInstruction(V_1_3, InstructionType.APPLY_ACTIONS)
                 .addAction(createAction(V_1_3, ActionType.OUTPUT, bpn(13)));
        m.addInstruction((Instruction) write.toImmutable());
        return (OfmFlowMod) m.toImmutable();
    }

    private static OfmFlowMod createPathDeamonIPFlow() {
        // not adding table ID purposely so that PipelineReader can patch.
        OfmMutableFlowMod m =
                (OfmMutableFlowMod) MessageFactory.create(V_1_3,
                                     MessageType.FLOW_MOD, FlowModCommand.ADD);
        m.bufferId(BufferId.NO_BUFFER).outPort(OUT_PORT).flowModFlags(FM_FLAGS);

        //Add match.
        MutableMatch mm = MatchFactory.createMatch(V_1_3)
            .addField(createBasicField(V_1_3, ETH_DST, mac("112233:445567")))
            .addField(createBasicField(V_1_3, ETH_SRC, mac("112233:445566")))
            .addField(createBasicField(V_1_3, ETH_TYPE, EthernetType.IPv4))
            .addField(createBasicField(V_1_3, IP_ECN, 0, 0))
            .addField(createBasicField(V_1_3, IPV4_SRC, ip("10.250.200.130")))
            .addField(createBasicField(V_1_3, IPV4_DST, ip("10.250.200.107")))
            .addField(createBasicField(V_1_3, IP_DSCP, 0, 0))
            .addField(createBasicField(V_1_3, IN_PORT, bpn(11)));

        m.match((Match) mm.toImmutable());

        // Add Instructions and actions
        InstrMutableAction write =
                createMutableInstruction(V_1_3, InstructionType.APPLY_ACTIONS)
                 .addAction(createAction(V_1_3, ActionType.OUTPUT, bpn(15)));
        m.addInstruction((Instruction) write.toImmutable());
        return (OfmFlowMod) m.toImmutable();
    }

    private static OfmFlowMod createFlowSupportedByTwoTables() {
        // not adding table ID purposely so that PipelineReader can patch.
        OfmMutableFlowMod m =
                (OfmMutableFlowMod) MessageFactory.create(V_1_3,
                                     MessageType.FLOW_MOD, FlowModCommand.ADD);
        m.bufferId(BufferId.NO_BUFFER).outPort(OUT_PORT).flowModFlags(FM_FLAGS);

        //Add match.
        MutableMatch mm = MatchFactory.createMatch(V_1_3)
            .addField(createBasicField(V_1_3, ETH_DST, mac("112233:445567")))
            .addField(createBasicField(V_1_3, ETH_TYPE, EthernetType.IPv4));

        m.match((Match) mm.toImmutable());

        // Add Instructions and actions
        InstrMutableAction write =
                createMutableInstruction(V_1_3, InstructionType.APPLY_ACTIONS)
                 .addAction(createAction(V_1_3, ActionType.OUTPUT, bpn(15)));
        m.addInstruction((Instruction) write.toImmutable());
        return (OfmFlowMod) m.toImmutable();
    }

    //-------------------------------------------------------------------------
    // Test cases
    @Test
    public void testCustomPipelineDefinition() throws OpenflowException {
        beginTest("testCustomPipelineDefinition");
        initController();
        PipelineSwitch sw = connectSwitch(DPID_13, DEF_13, Expect.CUSTOM);

        PipelineDefinition def = pmgr.getDefinition(DPID_13);
        assertNotNull(AM_HUH, def);
        verifyCustomDefinition(def);
        verifyDefaultFlows();
        disconnectSwitch(sw);
        endTest();
    }
    
    @Test
    public void testDelayedPipelineDefinition() throws OpenflowException {
        beginTest("testCustomPipelineDefinition");
        initController();
        PipelineSwitch sw = 
                connectSwitch(DPID_13, DEF_13, Expect.THAMES_PE_SW_DELAYED);
        PipelineDefinition def = pmgr.getDefinition(DPID_13);
        assertNotNull(AM_HUH, def);
        disconnectSwitch(sw);
        endTest();
    }
       


    // test a custom flow for align()
    // FIXME: figure out why TF is broken, and fix it.
    @Test @Ignore("MPReply/TABLE_FEATURES from PipelineSwitch is broken :(")
    public void testAlignForFlow1() throws OpenflowException {
        beginTest("testAlignForFlow1");
        initController();
        PipelineSwitch sw = connectSwitch(DPID_13, DEF_13, Expect.CUSTOM_1);
        PipelineDefinition def = pmgr.getDefinition(DPID_13);

        //TODO:  We seem to process table feature prop type twice
        tlog.assertInfoContains("not added to table context");

        assertNotNull(AM_HUH, def);
        Set<TableId> tids = pmgr.align(CUSTOM_FLOW_1, DPID_13);
        assertNotNull(AM_HUH, tids);
        assertEquals(AM_UXS, 1, tids.size());
        Iterator<TableId> it = tids.iterator();
        assertEquals(AM_NEQ, 0, it.next().toInt());
        disconnectSwitch(sw);
        endTest();
    }

    // test flow align for Thames
    @Test
    public void testAlignForThames() throws OpenflowException {
        beginTest("testAlignForThames");
        initController();
        PipelineSwitch sw = connectSwitch(DPID_13, DEF_13, Expect.THAMES_PE_SW);
        sw.expected(Expect.THAMES_PE_SW);

        PipelineDefinition def = pmgr.getDefinition(DPID_13);
        assertNotNull(AM_HUH, def);

        // ARP flows
        Set<TableId> tids = pmgr.align(createPathDeamonARPFlow(), DPID_13);
        assertNotNull(AM_HUH, tids);
        assertEquals(AM_NEQ, 1, tids.size());
        Iterator<TableId> it = tids.iterator();
        assertEquals(AM_NEQ, 200, it.next().toInt());

        // IP flows
        tids = pmgr.align(createPathDeamonIPFlow(), DPID_13);
        assertNotNull(AM_HUH, tids);
        assertEquals(AM_NEQ, 1, tids.size());
        it = tids.iterator();
        assertEquals(AM_NEQ, 200, it.next().toInt());
        disconnectSwitch(sw);
        endTest();
    }

    // test flow align for Comware
    @Test
    public void testAlignForComware() throws OpenflowException {
        beginTest("testAlignForThames");
        initController();
        PipelineSwitch sw = connectSwitch(DPID_13, DEF_13,
                Expect.COMWARE_IPMAC_EXT);
        sw.expected(Expect.COMWARE_IPMAC_EXT);
        PipelineDefinition def = pmgr.getDefinition(DPID_13);
        assertNotNull(AM_HUH, def);

        // ARP flows : Fails as this is not supported on comware

        // IP flows
        Set<TableId> tids  = pmgr.align(createPathDeamonIPFlow(), DPID_13);
        assertNotNull(AM_HUH, tids);
        assertEquals(AM_NEQ, 1, tids.size());
        Iterator<TableId> it = tids.iterator();
        assertEquals(AM_NEQ, 1, it.next().toInt());

        // flow having match supported by both tables,
        // verify the order of tables.
        // It is expected that tables are ordered in increasing order of table Id.
        tids  = pmgr.align(createFlowSupportedByTwoTables(), DPID_13);
        assertNotNull(AM_HUH, tids);
        assertEquals(AM_NEQ, 2, tids.size());
        it = tids.iterator();
        assertEquals(AM_NEQ, 0, it.next().toInt());
        assertEquals(AM_NEQ, 1, it.next().toInt());

        disconnectSwitch(sw);
        endTest();
    }

}
