/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.flow.impl;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.*;
import org.opendaylight.of.controller.ControllerService;
import org.opendaylight.of.controller.DataPathEvent;
import org.opendaylight.of.controller.OpenflowEventType;
import org.opendaylight.of.controller.RoleAdvisor;
import org.opendaylight.of.controller.impl.ControllerConfig;
import org.opendaylight.of.controller.impl.ListenerService;
import org.opendaylight.of.controller.pipeline.MutableTableContext;
import org.opendaylight.of.controller.pipeline.PipelineDefinition;
import org.opendaylight.of.controller.pipeline.PipelineReader;
import org.opendaylight.of.controller.pipeline.TableContext;
import org.opendaylight.of.controller.pipeline.impl.DefaultMutableTableContext;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.*;
import org.opendaylight.of.lib.instr.*;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.match.MatchFactory;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.junit.EasyMockArgumentMatcher;
import org.opendaylight.util.junit.TestLogger;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;

import java.util.*;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.easymock.EasyMock.*;
import static org.opendaylight.of.controller.OpenflowEventType.DATAPATH_CONNECTED;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.dt.DataPathId.dpid;
import static org.opendaylight.of.lib.msg.MessageType.FLOW_MOD;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Set of unit tests for the {@link FlowTrk}.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public class FlowTrkTest {
    private static final TestLogger tlog = new TestLogger();

    private static final String FMT_EX = "EX> {}";

    // TODO: use Match.matchAll(pv) when available
    private static final Match EMPTY_MATCH =
            (Match) MatchFactory.createMatch(V_1_3).toImmutable();

    private static final DataPathId DPID = dpid("0x7b/000553:afaac0");
    private static final TableId TABLE_0 = TableId.valueOf(0);
    private static final TableId TABLE_1 = TableId.valueOf(1);

    private static final ControllerConfig DEF_CFG =
            new ControllerConfig.Builder().build();

    private ListenerService lsMock;
    private PipelineReader prMock;
    private RoleAdvisor raMock;
    private ControllerService csMock;
    private FlowTrk flowTrk;

    private final DataPathInfo dpMock = new DataPathInfoAdapter() {
        @Override public ProtocolVersion negotiated() { return PV; }
        @Override public DataPathId dpid() { return DPID; }
        @Override public String manufacturerDescription() { return ""; }
    };

    @BeforeClass
    public static void classSetUp() {
        FlowTrk.setLogger(tlog);
    }

    @AfterClass
    public static void classTearDown() {
        FlowTrk.restoreLogger();
    }

    @Before
    public void setUp() {
        lsMock = createMock(ListenerService.class);
        prMock = createMock(PipelineReader.class);
        raMock = createMock(RoleAdvisor.class);
        csMock = createMock(ControllerService.class);
        flowTrk = new FlowTrk();

//        expect(csMock.getDataPathInfo(DPID)).andReturn(dpMock).anyTimes();
    }
    
    private void setLsMockExpectations() {
        lsMock.addDataPathListener(flowTrk.getMyDataPathListener());
        lsMock.addMessageListener(flowTrk.getMyMessageListener(),
                flowTrk.getMessageTypes());
    }


    private static final String EXP_FAILED_LOG =
        "Failed to add default flow-rule to datapath 00:7b:00:05:53:af:aa:c0:";
    
    @Test @Ignore("Does this test make sense now?")
    public void openFlowErrorOnDefaultFlow() throws OpenflowException {
        print(EOL + "openFlowErrorOnDefaultFlow");
        setLsMockExpectations();
        expect(lsMock.getDataPathInfo(EasyMock.eq(DPID))).andReturn(dpMock);

        // handleDpConnectedExpectations
        expect(prMock.getDefinition(EasyMock.eq(DPID)))
                .andReturn(makePipelineDefinition());
        // aligning tables expectations
        expect(prMock.align(anyObject(OfmFlowMod.class),
                EasyMock.eq(DPID))).andThrow(new OpenflowException("oops"));
        setCsMockExpectations();

        EasyMock.replay(lsMock, prMock, csMock);
        flowTrk.init(lsMock, null, null, null, prMock, DEF_CFG, csMock);
        flowTrk.getMyDataPathListener().event(dpConnectedEvent());

        tlog.assertErrorContains(EXP_FAILED_LOG);
        EasyMock.verify(lsMock, prMock, csMock);
    }

    private void setCsMockExpectations() {
        // none for now
    }

    private static final String EXP_FAILED_EXCEP_LOG =
            "Exception sending flow mod [] on dpid 00:7b:00:05:53:af:aa:c0:";

    @Test @Ignore("Need to fix mocks")
    public void exceptionOnDefaultFlow() throws OpenflowException {
        print(EOL + "exceptionOnDefaultFlow");
        setLsMockExpectations();

        // handleDpConnectedExpectations
        expect(prMock.getDefinition(EasyMock.eq(DPID)))
                .andReturn(makePipelineDefinition());
        // aligning tables expectations
        expect(prMock.align(anyObject(OfmFlowMod.class),
                EasyMock.eq(DPID))).andThrow(new RuntimeException("oops"));

        setCsMockExpectations();
        setLsMockExpectations();
        expect(lsMock.getDataPathInfo(EasyMock.eq(DPID))).andReturn(dpMock);

        EasyMock.replay(lsMock, prMock, csMock);
        flowTrk.init(lsMock, null, null, null, prMock, DEF_CFG, csMock);
        flowTrk.getMyDataPathListener().event(dpConnectedEvent());

        tlog.assertErrorContains(EXP_FAILED_EXCEP_LOG);
        EasyMock.verify(lsMock, prMock, csMock);
    }


    private static final String EXP_NO_DEFAULT_FLOW_LOG = EXP_FAILED_LOG +
            " empty default flow list";

    @Test @Ignore("Need to update now that we have device drivers")
    public void noTableForDefaultFlow() throws OpenflowException {
        print(EOL + "noTableForDefaultFlow");
        setLsMockExpectations();
        expect(lsMock.getDataPathInfo(EasyMock.eq(DPID))).andReturn(dpMock);

        // handleDpConnectedExpectations
        expect(prMock.getDefinition(EasyMock.eq(DPID)))
                .andReturn(makePipelineDefinition());

        // aligning tables expectations
        expect(prMock.align(anyObject(OfmFlowMod.class),
                EasyMock.eq(DPID))).andReturn(Collections.<TableId>emptySet())
                .times(2);
        setCsMockExpectations();

        EasyMock.replay(lsMock, prMock, csMock);

        flowTrk.init(lsMock, null, null, null, prMock, DEF_CFG, csMock);
        flowTrk.getMyDataPathListener().event(dpConnectedEvent());

        tlog.assertWarning(EXP_NO_DEFAULT_FLOW_LOG);
        EasyMock.verify(lsMock, prMock, csMock);
    }

    @Test @Ignore("Need to fix mocks")
    @SuppressWarnings("unchecked")
    public void sendDefaultFlow() throws OpenflowException {
        print(EOL + "sendDefaultFlow()");
        setLsMockExpectations();
        setLsMockExpectations();
        expect(lsMock.getDataPathInfo(EasyMock.eq(DPID))).andReturn(dpMock);

        // handleDpConnectedExpectations
        expect(prMock.getDefinition(EasyMock.eq(DPID)))
                .andReturn(makePipelineDefinition()).anyTimes();

        IArgumentMatcher table0 = new IArgumentMatcher() {
            @Override
            public boolean matches(Object argument) {
                if (argument == null)
                    return false;
                if (!(argument instanceof OfmFlowMod))
                    return false;
                OfmFlowMod arg = (OfmFlowMod)argument;
                return arg.getTableId() == TABLE_0;
            }

            @Override
            public void appendTo(StringBuffer buffer) {
                buffer.append("<table0Matcher>");
            }
        };

        IArgumentMatcher table1 = new IArgumentMatcher() {
            @Override
            public boolean matches(Object argument) {
                if (argument == null)
                    return false;
                if (!(argument instanceof OfmFlowMod))
                    return false;
                OfmFlowMod arg = (OfmFlowMod)argument;
                return arg.getTableId() == TABLE_1;
            }

            @Override
            public void appendTo(StringBuffer buffer) {
                buffer.append("<table1Matcher>");
            }
        };
        
        class DefaultFlowMatcher implements IArgumentMatcher {
            private final BigPortNumber matchPort;
            DefaultFlowMatcher(BigPortNumber port) {
                matchPort = port;
            }
            
            @Override
            public boolean matches(Object argument) {
                if (argument == null)
                    return false;
                if (!(argument instanceof List))
                    return false;
                List<OpenflowMessage> arg = (List<OpenflowMessage>) argument;
                if (arg.size() != 2)
                    return false;

                for (OpenflowMessage msg : arg) {
                    if (msg.getVersion().lt(V_1_3))
                        return false;
                    if (!(msg instanceof OfmFlowMod))
                        return false;
                    OfmFlowMod mod = (OfmFlowMod) msg;
                    if (mod.getCommand() != FlowModCommand.ADD)
                        return false;

                    if (!(mod.getMatch().equals(EMPTY_MATCH)))
                        return false;
                    if (mod.getInstructions().size() != 1)
                        return false;

                    Instruction inst = mod.getInstructions().get(0);

                    InstructionType type = mod.getInstructions().get(0).getInstructionType();
                    if (mod.getTableId() == TABLE_0) {
                        if (type != InstructionType.GOTO_TABLE)
                            return false;
                        if (!(inst instanceof InstrGotoTable))
                            return false;
                        InstrGotoTable gotoTable = (InstrGotoTable) inst;
                        if (gotoTable.getTableId() != TABLE_1)
                            return false;
                    } else if (mod.getTableId() == TABLE_1) {
                        if (type != InstructionType.APPLY_ACTIONS)
                            return false;
                        if (! (inst instanceof InstrApplyActions))
                            return false;
                        InstrApplyActions appActs = (InstrApplyActions) inst;
                        if (appActs.getActionList().size() != 1)
                            return false;
                        Action act = appActs.getActionList().get(0);
                        if (act.getActionType() != ActionType.OUTPUT)
                            return false;
                        if (! (act instanceof ActOutput))
                            return false;
                        ActOutput actOut = (ActOutput) act;
                        if (actOut.getPort() != matchPort)
                            return false;
                    } else {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void appendTo(StringBuffer buffer) {
                buffer.append("<defFlowModMatcher>");
            }
        }

        IArgumentMatcher defaultPureOFMatcher = new DefaultFlowMatcher(Port.CONTROLLER);
        IArgumentMatcher defaultHybridOFMatcher = new DefaultFlowMatcher(Port.NORMAL);

        // aligning tables expectations
        expect(prMock.align(EasyMockArgumentMatcher.<OfmFlowMod>match(table0),
                EasyMock.eq(DPID))).andReturn(new TreeSet<>(Arrays.asList(TABLE_0))).anyTimes();

        expect(prMock.align(EasyMockArgumentMatcher.<OfmFlowMod>match(table1),
                EasyMock.eq(DPID))).andReturn(new TreeSet<>(Arrays.asList(TABLE_1))).anyTimes();

        // sending expectations
        lsMock.send(EasyMockArgumentMatcher.<List<OpenflowMessage>>match(defaultPureOFMatcher), EasyMock.eq(DPID));
        lsMock.send(EasyMockArgumentMatcher.<List<OpenflowMessage>>match(defaultHybridOFMatcher), EasyMock.eq(DPID));

        setCsMockExpectations();
        setCsMockExpectations();

        EasyMock.replay(lsMock, prMock, csMock);

        // initialize with pure OF
        ControllerConfig cfg =
                new ControllerConfig.Builder().hybridMode(false).build();
        flowTrk.init(lsMock, null, null, null, prMock, cfg, csMock);
        flowTrk.getMyDataPathListener().event(dpConnectedEvent());
        
        // initialize with hybrid OF
        cfg = new ControllerConfig.Builder().hybridMode(false).build();
        flowTrk.init(lsMock, null, null, null, prMock, cfg, csMock);
        flowTrk.getMyDataPathListener().event(dpConnectedEvent());

        EasyMock.verify(lsMock, prMock, csMock);
    }

    private static final ProtocolVersion PV = V_1_3;

    private DataPathEvent dpConnectedEvent() {
        return new DataPathEvent() {
            @Override public DataPathId dpid() { return DPID; }
            @Override public ProtocolVersion negotiated() { return PV; }
            @Override public IpAddress ip() { return IpAddress.ip("1.2.3.4"); }
            @Override public long ts() { return 0; }
            @Override public OpenflowEventType type() { return DATAPATH_CONNECTED; }
        };
    }

    private PipelineDefinition makePipelineDefinition() {
        return new PipelineDefinition() {
            @Override
            public TableContext getTableContext(TableId tableId) {
                return createTableContext(tableId);
            }

            @Override
            public Set<TableId> getTableIds() {
                return new TreeSet<>(Arrays.asList(TABLE_0, TABLE_1));
            }

            @Override
            public boolean hasTables() {
                return true;
            }
        };
    }

    private TableContext createTableContext(TableId id) {
        MutableTableContext ctx = new DefaultMutableTableContext().tableId(id);
        if (id.compareTo(TABLE_0) == 0) {
            ctx.addNextTable(TABLE_1);
            ctx.addNextTableMiss(TABLE_1);
        }
        return ctx.toImmutable();
    }

    // =======================================================================
    // Testing sendFlowMod() and sendConfirmedFlowMod()    
    // Note: we don't really care about pipeline definition for the following
    // tests, so will use the 'suppress' config. 
    
    private static OfmFlowMod createTestFlowMod() {
        return (OfmFlowMod) createTestMutableFlowMod().toImmutable();
    }
    
    private static OfmMutableFlowMod createTestMutableFlowMod() {
        return ((OfmMutableFlowMod) MessageFactory.create(V_1_3, FLOW_MOD))
                .bufferId(BufferId.NO_BUFFER)
                .match(EMPTY_MATCH)
                .tableId(TABLE_0);
    }
    
    @Test(expected = NullPointerException.class)
    public void sendFlowModNullFM() throws OpenflowException {
        print(EOL + "sendFlowModNullFM()");
        flowTrk.sendFlowMod(null, DPID);
    }
    
    @Test(expected = NullPointerException.class)
    public void sendFlowModNullDpid() throws OpenflowException {
        print(EOL + "sendFlowModNullDpid()");
        flowTrk.sendFlowMod(createTestFlowMod(), null);
    }
    
    @Test
    public void sendFlowModMutable() throws OpenflowException {
        print(EOL + "sendFlowModMutable()");
        try {
            flowTrk.sendFlowMod(createTestMutableFlowMod(), DPID);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(FMT_EX, e);
            assertTrue(AM_HUH, e.getMessage().startsWith("Cannot be mutable:"));
        }
    }
    
    @Test @Ignore("implementation changed- this needs review")
    public void testSendFlowMod() throws OpenflowException {
        print(EOL + "testSendFlowMod()");
        setLsMockExpectations();
        setCsMockExpectations();
        expect(lsMock.versionOf(DPID)).andReturn(V_1_3);
        expect(raMock.isMasterFor(DPID)).andReturn(true).atLeastOnce();
        // note: have to specify OfmFlowMod class (and not FMOD) since 
        //  equals/hashcode is not implemented for ofm messages
        lsMock.send(anyObject(OfmFlowMod.class), EasyMock.eq(DPID));
        EasyMock.expectLastCall();
        EasyMock.replay(lsMock, prMock, raMock, csMock);        
        
        flowTrk.init(lsMock, null, raMock, null, prMock, DEF_CFG, csMock);
        flowTrk.sendFlowMod(createTestFlowMod(), DPID);

        EasyMock.verify(lsMock, prMock, raMock);
    }

    @Test(expected = NullPointerException.class)
    public void sendConfirmedFlowModNullFM() throws OpenflowException {
        print(EOL + "sendConfirmedFlowModNullFM()");
        flowTrk.sendConfirmedFlowMod(null, DPID);
    }

    @Test(expected = NullPointerException.class)
    public void sendConfirmedFlowModNullDpid() throws OpenflowException {
        print(EOL + "sendConfirmedFlowModNullDpid()");
        flowTrk.sendConfirmedFlowMod(createTestFlowMod(), null);
    }

    @Test
    public void sendConfirmedFlowModMutable() throws OpenflowException {
        print(EOL + "sendConfirmedFlowModMutable()");
        try {
            flowTrk.sendConfirmedFlowMod(createTestMutableFlowMod(), DPID);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(FMT_EX, e);
            assertTrue(AM_HUH, e.getMessage().startsWith("Cannot be mutable:"));
        }
    }

    @Test @Ignore("Implementation changed - this needs review")
    public void testSendConfirmedFlowMod() throws OpenflowException {
        print(EOL + "testSendConfirmedFlowMod()"); 
        setLsMockExpectations();
        setCsMockExpectations();
        expect(lsMock.versionOf(DPID)).andReturn(V_1_3);
        expect(raMock.isMasterFor(DPID)).andReturn(true).atLeastOnce();
        lsMock.sendFuture(anyObject(DataPathMessageFuture.class),
                anyObject(OfmFlowMod.class),
                anyObject(OfmBarrierRequest.class));
        EasyMock.expectLastCall();
        EasyMock.replay(lsMock, prMock, raMock, csMock);
        
        flowTrk.init(lsMock, null, raMock, null, prMock, DEF_CFG, csMock);
        MessageFuture future = 
                flowTrk.sendConfirmedFlowMod(createTestFlowMod(), DPID);
        print(future);

        EasyMock.verify(lsMock, prMock, raMock);
    }
}
