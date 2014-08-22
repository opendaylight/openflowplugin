/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.driver.base;

import org.opendaylight.util.driver.DefaultDeviceInfo;
import org.opendaylight.of.controller.pipeline.PipelineDefinition;
import org.opendaylight.of.controller.pipeline.TableContext;
import org.opendaylight.of.controller.pipeline.impl.DefaultTableContext;
import org.opendaylight.of.lib.OfpCodeBasedEnum;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.*;
import org.opendaylight.of.lib.match.*;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.net.facet.FlowUnsupportedException;
import org.opendaylight.util.junit.TestTools;
import org.opendaylight.util.net.*;
import org.easymock.EasyMock;
import org.junit.Test;

import java.util.*;

import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.junit.Assert.*;

/**
 * Set of unit tests for the {@link DefaultFlowMod} facet class.
 *
 * @author Julie Britt
 */
public class DefaultFlowModTest {

    private static final TableId HW_TABLE = TableId.valueOf("100");

    private static final TableId SW_TABLE = TableId.valueOf("200");
    private static final TableId BASE_TABLE = TableId.valueOf("0");

    /* Collections of various objects for verification */
    List<OxmFieldType> fieldList = new ArrayList<>();

    List<BigPortNumber> portList = new ArrayList<>();

    private PipelineDefinition pd;

    private DefaultDeviceInfo devInfoMock = EasyMock
            .createMock(DefaultDeviceInfo.class);

    private FlowCreator flowCreator;

    private DefaultFlowMod facet;

    private ProtocolVersion pv;

    private boolean hybridMode;

    private Map<TableId, DefaultTableContext> tableContexts = new HashMap<>();

    /*
     * Configurable setUp. Test specifies which OF version to use and which
     * module configuration.
     */
    public void setUp(ProtocolVersion version, boolean mode, boolean hasTables) {

        pv = version;
        hybridMode = mode;

        EasyMock.reset(devInfoMock);
        EasyMock.expect(devInfoMock.getBoolean(EasyMock.isA(String.class)))
                .andReturn(true).atLeastOnce();
        EasyMock.expect(devInfoMock.getTypeName()).andReturn("TestType");
        EasyMock.replay(devInfoMock);

        if (!hasTables) {
            tableContexts.clear();
        } else if (tableContexts.isEmpty()) {
            TableId tblId = HW_TABLE;
            DefaultTableContext tc = createTableContext(tblId);
            tableContexts.put(tblId, tc);
            tblId = SW_TABLE;
            tc = createTableContext(tblId);
            tableContexts.put(tblId, tc);
        }

        flowCreator = new FlowCreator();
        facet = new DefaultFlowMod(devInfoMock);
        pd = makePipelineDefinition();
        facet.setTableProperties(pd, pv, mode);
    }

    public void setUp(ProtocolVersion version, boolean mode) {
        setUp(version, mode, true);
    }

    private PipelineDefinition makePipelineDefinition() {

        return new PipelineDefinition() {

            @Override
            public TableContext getTableContext(TableId tableId) {
                return getFromTableContext(tableId);
            }

            @Override
            public Set<TableId> getTableIds() {
                return new TreeSet<>(Arrays.asList(HW_TABLE, SW_TABLE));
            }

            @Override
            public boolean hasTables() {
                if (tableContexts.isEmpty()) {
                    return false;
                }
                return true;
            }
        };
    }

    private MBodyDesc makeMBodyDesc() {
        return new MBodyDesc(pv) {

            @Override
            public String getMfrDesc() {
                return "HP";
            }

            @Override
            public String getSwDesc() {
                return "HP";
            }
        };
    }

    private DefaultTableContext getFromTableContext(TableId id) {
        return tableContexts.get(id);
    }

    private DefaultTableContext createTableContext(final TableId id) {
        DefaultTableContext ctx = new DefaultTableContext() {

            @Override
            public MBodyDesc dpDesc() {
                return makeMBodyDesc();
            }

            @Override
            public TableId tableId() {
                return id;
            }

            @Override
            public boolean supportsMatchField(MatchField x) {
                if (id.equals(HW_TABLE)) {
                    List<OxmBasicFieldType> types = Arrays
                            .asList(OxmBasicFieldType.ETH_TYPE,
                                    OxmBasicFieldType.IP_PROTO,
                                    OxmBasicFieldType.UDP_SRC,
                                    OxmBasicFieldType.IPV4_SRC,
                                    OxmBasicFieldType.UDP_DST,
                                    OxmBasicFieldType.VLAN_VID);
                    if (types.contains(x.getFieldType())) {
                        return true;
                    }
                } else {
                    return true;
                }

                return false;
            }

            @Override
            public TableId getNextTableMiss() {
                if (id.equals(HW_TABLE)) {
                    return SW_TABLE;
                }

                return null;
            }

            @Override
            public boolean containsNextTableMiss(TableId toId) {
                return (id.equals(HW_TABLE) && (toId.equals(SW_TABLE)));
            }

            @Override
            public boolean hasNextTablesMiss() {
                return (id.equals(HW_TABLE));
            }

            @Override
            public boolean supportsCapability(TableFeaturePropType prop,
                                              OfpCodeBasedEnum code) {
                if (prop.equals(TableFeaturePropType.APPLY_ACTIONS)) {
                    if ((code == ActionType.OUTPUT) || (code == ActionType.SET_FIELD)) {
                        return true;
                    }
                }
                if (prop.equals(TableFeaturePropType.INSTRUCTIONS_MISS)) {
                    if (code == InstructionType.GOTO_TABLE) {
                        return true;
                    }
                }
                if (prop.equals(TableFeaturePropType.APPLY_ACTIONS_MISS)) {
                    if (id.equals(SW_TABLE)) {
                        return true;
                    }
                }
                if (prop.equals(TableFeaturePropType.WRITE_ACTIONS_MISS)) {
                    if (id.equals(SW_TABLE) && (code == ActionType.SET_NW_TTL)) {
                        return true;
                    }
                    if (id.equals(SW_TABLE) && (code == ActionType.SET_FIELD)) {
                        return true;
                    }
                }

                return false;
            }

            @Override
            public boolean supportsMatchFieldCapability(TableFeaturePropType prop,
                                                        OxmFieldType code) {
                return (prop.equals(TableFeaturePropType.APPLY_SETFIELD));
            }
        };

        return ctx;
    }

    @Test
    public void testReturn() throws Exception {
        setUp(ProtocolVersion.V_1_3, true);
        OfmFlowMod ofm = flowCreator.buildArpFlowMod();
        OfmMutableFlowMod mutFlow = (OfmMutableFlowMod) MessageFactory.exactMutableCopy(ofm);
        mutFlow.tableId(HW_TABLE);
        ofm = (OfmFlowMod) mutFlow.toImmutable();
        Set<OfmFlowMod> adjustedFlows = facet.adjustFlowMod(ofm);

        assertEquals("Failed ARP test", 1, adjustedFlows.size());
        for (OfmFlowMod adjustedFlow : adjustedFlows) {
            if (!ofm.equals(adjustedFlow)) {
                fail("Arp Flow test failed " + adjustedFlow.toDebugString());
            }
        }
    }

    @Test
    public void testNoTable() throws Exception {
        setUp(ProtocolVersion.V_1_3, true, false);
        OfmFlowMod ofm = flowCreator.buildArpFlowMod();
        Set<OfmFlowMod> adjustedFlows = facet.adjustFlowMod(ofm);

        assertEquals("Failed ARP test", 1, adjustedFlows.size());
        for (OfmFlowMod adjustedFlow : adjustedFlows) {
            assertEquals("Arp flow with no tables should be sent to base table",
                    BASE_TABLE, adjustedFlow.getTableId());
            if ((ofm.getMatch().equals(adjustedFlow.getMatch()))
                    && (adjustedFlow.getInstructions().size() == 1)
                    && (adjustedFlow.getInstructions().get(0).getInstructionType()
                    == InstructionType.APPLY_ACTIONS)
                    && (((InstrApplyActions)adjustedFlow.getInstructions()
                    .get(0)).getActionList().size() == 2)
                    && (((InstrApplyActions)adjustedFlow.getInstructions()
                    .get(0)).getActionList().get(0).getActionType()
                    == ActionType.OUTPUT)) {
                // it matches
            } else {
                fail("Arp Flow test failed " + adjustedFlow.toDebugString());
            }
        }
    }

    @Test
    public void testArpOF13() throws Exception {

        setUp(ProtocolVersion.V_1_3, true);
        OfmFlowMod ofm = flowCreator.buildArpFlowMod();
        Set<OfmFlowMod> adjustedFlows = facet.adjustFlowMod(ofm);

        assertEquals("Failed ARP test", 1, adjustedFlows.size());
        for (OfmFlowMod adjustedFlow : adjustedFlows) {
            if (!verifyArpFlow(adjustedFlow)) {
                fail("Arp Flow test failed " + adjustedFlow.toDebugString());
            }
        }
    }

    @Test
    public void testDhcpOF13() throws Exception {

        setUp(ProtocolVersion.V_1_3, true);
        OfmFlowMod ofm = flowCreator.buildDhcpFlowMod();
        Set<OfmFlowMod> adjustedFlows = facet.adjustFlowMod(ofm);

        assertEquals("Failed DHCP test", 1, adjustedFlows.size());
        for (OfmFlowMod adjustedFlow : adjustedFlows) {
            if (!verifyDhcpFlow(adjustedFlow)) {
                fail("Dhcp Flow test failed " + adjustedFlow.toDebugString());
            }
        }
    }

    @Test
    public void testHsdpOF13() throws Exception {

        setUp(ProtocolVersion.V_1_3, true);
        OfmFlowMod ofm = flowCreator.buildHsdpFlowMod();
        Set<OfmFlowMod> adjustedFlows = facet.adjustFlowMod(ofm);

        assertEquals("Failed HSDP test", 1, adjustedFlows.size());

        for (OfmFlowMod adjustedFlow : adjustedFlows) {
            if (!verifyHsdpFlow(adjustedFlow)) {
                fail("Hdsp Flow test failed " + adjustedFlow.toDebugString());
            }
        }
    }

    @Test
    public void testMatches() throws Exception {
        setUp(ProtocolVersion.V_1_3, true);
        OfmFlowMod ofm = flowCreator.buildInvalidMatch1();
        Set<OfmFlowMod> adjustedFlows = facet.adjustFlowMod(ofm);

        assertEquals("Failed match 1", 2, adjustedFlows.size());

        for (OfmFlowMod adjustedFlow : adjustedFlows) {
            if (!verifyInvalid1Flow(adjustedFlow)) {
                fail("First Invalid Flow test failed "
                        + adjustedFlow.toDebugString());
            }
        }

        setUp(ProtocolVersion.V_1_3, true);
        ofm = flowCreator.buildInvalidInstruction1();
        adjustedFlows = facet.adjustFlowMod(ofm);

        assertEquals("Failed match 1", 1, adjustedFlows.size());

        for (OfmFlowMod adjustedFlow : adjustedFlows) {
            if (!verifyInvalid2Flow(adjustedFlow)) {
                fail("First Invalid Flow test failed "
                        + adjustedFlow.toDebugString());
            }
        }

        boolean didFail = false;
        setUp(ProtocolVersion.V_1_3, true);
        ofm = flowCreator.buildInvalidInstruction2();
        try {
            adjustedFlows = facet.adjustFlowMod(ofm);
        } catch (FlowUnsupportedException e) {
            // This is good
            didFail = true;
        }

        if (!didFail) {
            fail("Invalid instruction 2 didn't fail!");
        }

        didFail = false;
        setUp(ProtocolVersion.V_1_3, true);
        ofm = flowCreator.buildInvalidInstruction4();
        try {
            adjustedFlows = facet.adjustFlowMod(ofm);
        } catch (FlowUnsupportedException e) {
            // This is good
            didFail = true;
        }

        if (!didFail) {
            fail("Invalid instruction 4 didn't fail!");
        }

        setUp(ProtocolVersion.V_1_3, true);
        ofm = flowCreator.buildInvalidInstruction3();

        adjustedFlows = facet.adjustFlowMod(ofm);

        assertEquals("Failed match 3", 0, adjustedFlows.size());

        setUp(ProtocolVersion.V_1_3, true);
        ofm = flowCreator.buildInvalidInstruction5();

        try {
            adjustedFlows = facet.adjustFlowMod(ofm);
        } catch (FlowUnsupportedException e) {
            // This is good
            didFail = true;
        }

        if (!didFail) {
            fail("Invalid instruction 5 didn't fail!");
        }

        setUp(ProtocolVersion.V_1_3, true);
        ofm = flowCreator.buildInvalidInstruction6();

        try {
            adjustedFlows = facet.adjustFlowMod(ofm);
        } catch (FlowUnsupportedException e) {
            // This is good
            didFail = true;
        }

        if (!didFail) {
            fail("Invalid instruction 6 didn't fail!");
        }
    }

    @Test
    public void testStrings() throws Exception {
        setUp(ProtocolVersion.V_1_3, true);
        String s = facet.toDebugString();
        assertTrue("Debug string didn't return anything", !s.isEmpty());
        s = facet.toString();
        assertTrue("toString didn't return anything", !s.isEmpty());
    }

    @Test
    public void testDefaultFlowsOF13() throws Exception {

        setUp(ProtocolVersion.V_1_3, true);
        Set<OfmFlowMod> defFlows = facet.generateDefaultFlows();
        int numSpecializedFlows = 0;
        int numDefaultFlows = 0;

        // V1 modules will have two extra specialized flows
        assertEquals("Failed defFlows", 2, defFlows.size());

        TestTools.print("");
        TestTools.print("");
        TestTools.print("OF 1.3 Flows");
        for (OfmFlowMod flow : defFlows) {
            TestTools.print(flow.toDebugString());
            assertEquals("Failed version check", pv, flow.getVersion());

            if (!verifyFlows(flow, flowCreator.buildDefaultRules())) {
                fail("Unrecognized default flow for " + flow.toDebugString());
            }
        }
    }

    @Test
    public void testArpOF10() throws Exception {

        setUp(ProtocolVersion.V_1_0, true);
        OfmFlowMod ofm = flowCreator.buildArpFlowMod();
        Set<OfmFlowMod> adjustedFlows = facet.adjustFlowMod(ofm);

        assertEquals("Failed arp 1.0 count", 1, adjustedFlows.size());
        assertEquals("Failed arp 1.0 test", ofm, adjustedFlows.iterator().next());
    }

    @Test
    public void testDhcpOF10() throws Exception {

        setUp(ProtocolVersion.V_1_0, true);
        OfmFlowMod ofm = flowCreator.buildDhcpFlowMod();
        Set<OfmFlowMod> adjustedFlows = facet.adjustFlowMod(ofm);

        assertEquals("Failed DHCP 1.0 count", 1, adjustedFlows.size());
        assertEquals("Failed DHCP 1.0 test", ofm, adjustedFlows.iterator().next());
    }

    @Test
    public void testHsdpOF10() throws Exception {

        setUp(ProtocolVersion.V_1_0, true);
        OfmFlowMod ofm = flowCreator.buildHsdpFlowMod();
        Set<OfmFlowMod> adjustedFlows = facet.adjustFlowMod(ofm);

        assertEquals("Failed HSDP 1.0 count", 1, adjustedFlows.size());
        assertEquals("Failed HSDP 1.0 test", ofm, adjustedFlows.iterator().next());
    }

    /**
     * Makes sure the match field has the info that's expected.
     *
     * @param match the object being checked
     * @param matchSize the number of fields that should be matched
     * @param fieldTypes the types of fields that should be matched
     * @return true if match is what's expected; false otherwise
     * @throws Exception if problems
     */
    private boolean checkMatches(Match match, int matchSize,
                                 List<OxmFieldType> fieldTypes)
            throws Exception {
        if (match.getMatchFields().size() != matchSize) {
            return false;
        }

        OxmFieldType oldField = null;
        for (MatchField field : match.getMatchFields()) {
            if (!fieldTypes.contains(field.getFieldType())) {
                return false;
            }
            if (oldField == field.getFieldType()) {
                return false;
            }
            oldField = field.getFieldType();
        }

        return true;
    }

    /**
     * Makes sure the actions are what's expected.
     *
     * @param actions that are being checked
     * @param actionSize of action list supposedly
     * @param actionType the type of actions expected
     * @param portTypes the types of actions that should be found
     * @return true if actions are what's expected; false otherwise
     * @throws Exception if problems
     */
    private boolean checkActions(List<Action> actions, int actionSize,
                                 ActionType actionType,
                                 List<BigPortNumber> portTypes)
            throws Exception {

        if (actions.size() != actionSize) {
            return false;
        }
        BigPortNumber oldPort = null;
        for (Action action : actions) {
            if (action.getActionType() != actionType) {
                return false;
            }

            // Action types of output are all we work on right now
            if (ActOutput.class.isInstance(action)) {
                ActOutput out = ((ActOutput) action);
                if (!portTypes.contains(out.getPort())) {
                    return false;
                }
                if (out.getPort().equals(oldPort)) {
                    return false;
                }
                oldPort = out.getPort();
            }
        }

        return true;
    }

    @Test
    public void testDefaultFlowsOF10() throws Exception {

        setUp(ProtocolVersion.V_1_0, true);
        Set<OfmFlowMod> defFlows = facet.generateDefaultFlows();

        assertEquals("Failed defFlows 1.0 count", 1, defFlows.size());

        TestTools.print("");
        TestTools.print("");
        TestTools.print("OF 1.0 Flows");
        for (OfmFlowMod flow : defFlows) {
            TestTools.print(flow.toDebugString());
            assertEquals("Failed defFlow 1.0 version", pv, flow.getVersion());

            if (verifyHsdpFlow10(flow) || verifyArpFlow10(flow)
                    || verifyDhcpFlow10(flow)
                    || verifyFlows10(flow, flowCreator.buildDefaultRules())) {
                continue;
            }

            fail("Unknown flow:  " + flow.toDebugString());
        }
    }

    @Test
    public void testDefaultFlowsNonHybrid() throws Exception {

        setUp(ProtocolVersion.V_1_3, false);
        Set<OfmFlowMod> defFlows = facet.generateDefaultFlows();

        assertEquals("Failed nonhybrid count", 2, defFlows.size());

        TestTools.print("");
        TestTools.print("");
        TestTools.print("Nonhybrid Flows");
        for (OfmFlowMod flow : defFlows) {
            TestTools.print(flow.toDebugString());
            assertEquals("Failed nonhybrid version", pv, flow.getVersion());

            if (verifyFlows(flow, flowCreator.buildDefaultRules())) {
                continue;
            }

            fail("Unknown flow:  " + flow.toDebugString());
        }
    }

    @Test
    public void testDefaultFlowsNonHybrid10() throws Exception {

        setUp(ProtocolVersion.V_1_0, false);
        Set<OfmFlowMod> defFlows = facet.generateDefaultFlows();

        assertEquals("Failed nonhybrid 1.0 count", 1, defFlows.size());

        TestTools.print("");
        TestTools.print("");
        TestTools.print("Nonhybrid 1.0 Flows");
        for (OfmFlowMod flow : defFlows) {
            TestTools.print(flow.toDebugString());
            if (verifyFlows10(flow, flowCreator.buildDefaultRules())) {
                continue;
            }

            fail("Unknown flow:  " + flow.toDebugString());
        }
    }

    @Test
    public void testSentinel() throws Exception {
        setUp(ProtocolVersion.V_1_0, true);
        OfmFlowMod ofm = flowCreator.buildSentinelFlow();
        Set<OfmFlowMod> adjustedFlows = facet.adjustFlowMod(ofm);

        assertEquals("Failed Sentinel 1.0 count", 1, adjustedFlows.size());
        assertEquals("Failed Sentinel 1.0", ofm, adjustedFlows.iterator().next());

        setUp(ProtocolVersion.V_1_3, true);
        ofm = flowCreator.buildSentinelFlow();
        adjustedFlows = facet.adjustFlowMod(ofm);

        assertEquals("Failed Sentinel 1.3 count", 1, adjustedFlows.size());
        assertEquals("Failed Sentinel 1.3 matches", ofm.getMatch(), adjustedFlows.iterator().next().getMatch());
        assertEquals("Failed Sentinel 1.3 instructions", ofm.getInstructions(), adjustedFlows.iterator().next().getInstructions());
    }

    @Test
    public void testVega() throws Exception {
        setUp(ProtocolVersion.V_1_0, true);
        OfmFlowMod ofm = flowCreator.buildVegaFlow();
        Set<OfmFlowMod> adjustedFlows = facet.adjustFlowMod(ofm);

        assertEquals("Failed Vega 1.0 count", 1, adjustedFlows.size());
        assertEquals("Failed Vega 1.0", ofm, adjustedFlows.iterator().next());

        setUp(ProtocolVersion.V_1_3, true);
        ofm = flowCreator.buildVegaFlow();
        adjustedFlows = facet.adjustFlowMod(ofm);

        assertEquals("Failed Vega 1.3 count", 1, adjustedFlows.size());
        assertEquals("Failed Vega 1.3 match", ofm.getMatch(), adjustedFlows.iterator().next().getMatch());
        assertEquals("Failed Vega 1.3 instructions", ofm.getInstructions(), adjustedFlows.iterator().next().getInstructions());
    }

    /**
     * Compares the arp flow to the expected one.
     *
     * @param flow to be analyzed
     * @return boolean indicating success
     * @throws Exception if problems
     */
    private boolean verifyArpFlow(OfmFlowMod flow) throws Exception {

        fieldList.clear();
        portList.clear();
        populateArpMatchList();
        OfmFlowMod ofm = flowCreator.buildArpFlowMod();

        if (ofm.getType() != flow.getType() || (flow.getActions() != null)
                || (flow.getTableId() == null)) {
            return false;
        }

        Match match = flow.getMatch();
        if (match.getVersion() != flow.getMatch().getVersion()) {
            return false;
        }

        if (flow.getTableId() == HW_TABLE) {

            // Verify the match rules are valid
            // No ARP match rules are valid for v1
            if (!checkMatches(match, fieldList.size(), fieldList)) {
                return false;
            }

            // Verify the instructions and actions are valid
            if (flow.getInstructions().size() != 1) {
                return false;
            }

            portList.add(Port.CONTROLLER);
            portList.add(Port.NORMAL);
            if (!checkInstructionAndActions(flow.getInstructions().get(0),
                    ofm.getInstructions().get(0).getInstructionType(),
                    portList.size(), ActionType.OUTPUT, portList)) {
                return false;
            }

        } else {
            return false;
        }

        return true;
    }

    /**
     * Compares the dhcp flow to the expected one.
     *
     * @param flow to be analyzed
     * @return boolean indicating success
     * @throws Exception if problems
     */
    private boolean verifyDhcpFlow(OfmFlowMod flow) throws Exception {

        fieldList.clear();
        portList.clear();
        OfmFlowMod ofm = flowCreator.buildDhcpFlowMod();
        if ((ofm.getType() != flow.getType()) || (flow.getActions() != null)
                || (flow.getTableId() == null)) {
            return false;
        }

        Match match = flow.getMatch();
        if (match.getVersion() != ofm.getMatch().getVersion()) {
            return false;
        }

        if (flow.getTableId().equals(HW_TABLE)) {

            // Verify the match rules are valid
            // All of the Dhcp ones are allowable in the hw table
            populateDhcpMatchList();
            if (!checkMatches(match, fieldList.size(), fieldList)) {
                return false;
            }

            // Verify the instructions and actions are valid
            if (flow.getInstructions().size() != 1) {
                return false;
            }

            portList.add(Port.CONTROLLER);
            portList.add(Port.NORMAL);
            if (!checkInstructionAndActions(flow.getInstructions().get(0),
                    ofm.getInstructions().get(0).getInstructionType(),
                    portList.size(), ActionType.OUTPUT, portList)) {
                return false;
            }

        } else {
            fail("Table id needs to either be 100");
        }

        return true;
    }

    /**
     * Compares the hsdp flow to the expected one.
     *
     * @param flow to be analyzed
     * @return boolean indicating success
     * @throws Exception if problems
     */
    private boolean verifyHsdpFlow(OfmFlowMod flow) throws Exception {

        fieldList.clear();
        portList.clear();
        OfmFlowMod ofm = flowCreator.buildHsdpFlowMod();

        if ((ofm.getType() != flow.getType()) || (flow.getActions() != null)) {
            return false;
        }

        Match match = flow.getMatch();

        if (flow.getTableId().equals(HW_TABLE)) {

            // Verify the match rules are valid
            // Only matching on ETH_TYPE
            fieldList.add(OxmBasicFieldType.ETH_TYPE);
            if (!checkMatches(match, fieldList.size(), fieldList)) {
                return false;
            }

            // Verify the instructions and actions are valid
            if (flow.getInstructions().size() != 1) {
                return false;
            }

            portList.add(Port.CONTROLLER);
            if (!checkInstructionAndActions(flow.getInstructions().get(0),
                    InstructionType.APPLY_ACTIONS, 1,
                    ActionType.OUTPUT, portList)) {
                return false;
            }
        } else {
            fail("Table id needs to either be 100");
        }

        return true;
    }

    /**
     * Compares the flow against a list passed in.
     *
     * @param flow to be analyzed
     * @param flows to compare against
     * @return boolean true if flows are right
     * @throws Exception if problems
     */
    private boolean verifyFlows(OfmFlowMod flow, List<OfmFlowMod> flows)
            throws Exception {

        fieldList.clear();
        portList.clear();
        for (OfmFlowMod defFlow : flows) {

            // Check for equality based on the fields we care about
            if (flow.getMatch().equals(defFlow.getMatch())
                    && flow.getCommand().equals(defFlow.getCommand())
                    && flow.getOutPort().equals(defFlow.getOutPort())
                    && flow.getTableId().equals(defFlow.getTableId())
                    && flow.getType().equals(defFlow.getType())
                    && (flow.getInstructions().size() == defFlow
                    .getInstructions().size())) {
                for (Instruction instr : flow.getInstructions()) {
                    boolean instrFound = false;
                    for (Instruction defInstr : defFlow.getInstructions()) {
                        if (instr.getInstructionType()
                                .equals(defInstr.getInstructionType())
                                && instr.getVersion().equals(defInstr
                                .getVersion())
                                && (instr.getTotalLength() == defInstr
                                .getTotalLength())) {
                            instrFound = true;
                        }
                    }

                    if (instrFound) {
                        return true;
                    }
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Compares the arp flow to the expected one. For OF 1.0.
     *
     * @param flow to be analyzed
     * @return boolean indicating success
     * @throws Exception if problems
     */
    private boolean verifyArpFlow10(OfmFlowMod flow) throws Exception {

        fieldList.clear();
        portList.clear();
        OfmFlowMod ofm = flowCreator.buildArpFlowMod();

        if ((ofm.getType() != flow.getType()) || (flow.getActions() == null)
                || (flow.getTableId() != null)) {
            return false;
        }

        Match match = flow.getMatch();
        if (match.getVersion() != flow.getMatch().getVersion()) {
            return false;
        }

        // Verify the match rules are valid
        populateArpMatchList();
        if (!checkMatches(match, fieldList.size(), fieldList)) {
            return false;
        }

        // Verify the actions are valid
        portList.add(Port.CONTROLLER);
        portList.add(Port.NORMAL);
        if (!checkActions(flow.getActions(), 2, ActionType.OUTPUT, portList)) {
            return false;
        }

        return true;
    }

    /**
     * Compares the dhcp flow to the expected one. For OF 1.0.
     *
     * @param flow to be analyzed
     * @return boolean indicating success
     * @throws Exception if problems
     */
    private boolean verifyDhcpFlow10(OfmFlowMod flow) throws Exception {

        fieldList.clear();
        portList.clear();
        OfmFlowMod ofm = flowCreator.buildDhcpFlowMod();
        if ((ofm.getType() != flow.getType()) || (flow.getActions() == null)
                || (flow.getTableId() != null)) {
            return false;
        }

        Match match = flow.getMatch();
        if (match.getVersion() != ofm.getMatch().getVersion()) {
            return false;
        }

        // Verify the match rules are valid
        populateDhcpMatchList();
        if (!checkMatches(match, fieldList.size(), fieldList)) {
            return false;
        }

        // Verify the actions are valid
        portList.add(Port.CONTROLLER);
        portList.add(Port.NORMAL);
        if (!checkActions(flow.getActions(), 2, ActionType.OUTPUT, portList)) {
            return false;
        }

        return true;
    }

    /**
     * Compares the hsdp flow to the expected one. For OF 1.0.
     *
     * @param flow to be analyzed
     * @return boolean indicating success
     * @throws Exception if problems
     */
    private boolean verifyHsdpFlow10(OfmFlowMod flow) throws Exception {

        fieldList.clear();
        portList.clear();
        OfmFlowMod ofm = flowCreator.buildHsdpFlowMod();

        if ((ofm.getType() != flow.getType()) || (flow.getActions() == null)
                || (flow.getTableId() != null)) {
            return false;
        }

        Match match = flow.getMatch();

        // Verify the match rules are valid
        // Only matching on ETH_TYPE
        fieldList.add(OxmBasicFieldType.ETH_TYPE);
        if (!checkMatches(match, fieldList.size(), fieldList)) {
            return false;
        }

        // Verify the actions are valid
        portList.add(Port.CONTROLLER);
        if (!checkActions(flow.getActions(), 1, ActionType.OUTPUT, portList)) {
            return false;
        }

        return true;
    }

    /**
     * Compares a flow to a list of expected ones. For OF 1.0.
     *
     * @param flow to be analyzed
     * @param flows to be compared against
     * @return boolean true if flows are right
     * @throws Exception if problems
     */
    private boolean verifyFlows10(OfmFlowMod flow, List<OfmFlowMod> flows)
            throws Exception {

        fieldList.clear();
        portList.clear();
        for (OfmFlowMod defFlow : flows) {

            // Compare based on fields we care about
            if (flow.getMatch().equals(defFlow.getMatch())
                    && flow.getCommand().equals(defFlow.getCommand())
                    && flow.getOutPort().equals(defFlow.getOutPort())
                    && flow.getType().equals(defFlow.getType())
                    && (flow.getActions().size() == defFlow.getActions().size())) {
                for (Action action : flow.getActions()) {
                    boolean actionFound = false;
                    for (Action defAction : defFlow.getActions()) {
                        if (action.getActionType().equals(defAction
                                .getActionType())
                                && action.getVersion()
                                .equals(defAction.getVersion())
                                && (action.getTotalLength() == defAction
                                .getTotalLength())) {
                            actionFound = true;
                        }
                    }

                    if (actionFound) {
                        return true;
                    }
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Compares the invalid flow to the expected one.
     *
     * @param flow to be analyzed
     * @return boolean indicating success
     * @throws Exception if problems
     */
    private boolean verifyInvalid1Flow(OfmFlowMod flow) throws Exception {

        fieldList.clear();
        portList.clear();
        OfmFlowMod ofm = flowCreator.buildInvalidMatch1();
        if ((ofm.getType() != flow.getType()) || (flow.getActions() != null)
                || (flow.getTableId() == null)) {
            return false;
        }

        Match match = flow.getMatch();
        if (match.getVersion() != ofm.getMatch().getVersion()) {
            return false;
        }

        if (flow.getTableId().equals(HW_TABLE)) {

            // Verify the match rules are valid
            // Two of the three should be allowable in the hw table
            fieldList.add(OxmBasicFieldType.ETH_TYPE);
            fieldList.add(OxmBasicFieldType.VLAN_VID);
            if (!checkMatches(match, fieldList.size(), fieldList)) {
                return false;
            }

            // Verify the instructions and actions are valid
            if (flow.getInstructions().size() != 1) {
                return false;
            }
            if (!checkInstructionAndActions(flow.getInstructions().get(0),
                    InstructionType.GOTO_TABLE, 0, null, null)) {
                return false;
            }

        } else if (flow.getTableId().equals(SW_TABLE)) {

            // Verify the match rules are valid
            fieldList.add(OxmBasicFieldType.ETH_TYPE);
            fieldList.add(OxmBasicFieldType.VLAN_VID);
            fieldList.add(OxmBasicFieldType.VLAN_PCP);
            fieldList.add(OxmBasicFieldType.IPV4_DST);
            if (!checkMatches(match, fieldList.size(), fieldList)) {
                return false;
            }

            // Verify the instructions and actions are valid
            if (flow.getInstructions().size() != 1) {
                return false;
            }
            portList.add(Port.CONTROLLER);
            if (!checkInstructionAndActions(flow.getInstructions().get(0),
                    InstructionType.APPLY_ACTIONS, 1, ActionType.OUTPUT,
                    portList)) {
                return false;
            }
        } else {
            fail("Table id needs to either be 100 or 200");
        }

        return true;
    }


    /**
     * Compares the invalid flow to the expected one.
     *
     * @param flow to be analyzed
     * @return boolean indicating success
     * @throws Exception if problems
     */
    private boolean verifyInvalid2Flow(OfmFlowMod flow) throws Exception {

        fieldList.clear();
        portList.clear();
        OfmFlowMod ofm = flowCreator.buildInvalidInstruction1();
        if ((ofm.getType() != flow.getType()) || (flow.getActions() != null)
                || (flow.getTableId() == null)) {
            return false;
        }

        Match match = flow.getMatch();
        if (match.getVersion() != ofm.getMatch().getVersion()) {
            return false;
        }

        if (flow.getTableId().equals(SW_TABLE)) {

            // Verify the match rules are valid
            if (!checkMatches(match, fieldList.size(), fieldList)) {
                return false;
            }

            // Verify the instructions and actions are valid
            if (flow.getInstructions().size() != 1) {
                return false;
            }

            if (!checkInstructionAndActions(flow.getInstructions().get(0),
                    InstructionType.APPLY_ACTIONS, 1, ActionType.COPY_TTL_IN,
                    null)) {
                return false;
            }
        } else {
            fail("Table id needs to either be 100 or 200");
        }

        return true;
    }

    /**
     * Compares the invalid flow to the expected one.
     *
     * @param flow to be analyzed
     * @return boolean indicating success
     * @throws Exception if problems
     */
    private boolean verifyInvalid3Flow(OfmFlowMod flow) throws Exception {

        fieldList.clear();
        portList.clear();
        OfmFlowMod ofm = flowCreator.buildInvalidInstruction3();
        if ((ofm.getType() != flow.getType()) || (flow.getActions() != null)
                || (flow.getTableId() == null)) {
            return false;
        }

        Match match = flow.getMatch();
        if (match.getVersion() != ofm.getMatch().getVersion()) {
            return false;
        }

        if (flow.getTableId().equals(HW_TABLE)) {

            // Verify the match rules are valid
            if (!checkMatches(match, fieldList.size(), fieldList)) {
                return false;
            }

            // Verify the instructions and actions are valid
            if (flow.getInstructions().size() != 1) {
                return false;
            }

            if (!checkInstructionAndActions(flow.getInstructions().get(0),
                    InstructionType.GOTO_TABLE, 0, null,
                    null)) {
                return false;
            }

        } else {
            fail("Table id needs to be 100");
        }

        return true;
    }

    /**
     * Makes sure the instructions have the info that's expected.
     *
     * @param instr the object being checked
     * @param it the type of the instruction expected
     * @param actionListSize the number of actions expected
     * @param actionType the type of actions expected
     * @param validPortList the ports on which the actions should operate
     * @return true if instruction is expected; false otherwise
     * @throws Exception if problems
     */
    private boolean checkInstructionAndActions(Instruction instr,
                                               InstructionType it,
                                               int actionListSize,
                                               ActionType actionType,
                                               List<BigPortNumber> validPortList)
            throws Exception {

        if (instr.getInstructionType() != it) {
            return false;
        }
        if (InstrApplyActions.class.isInstance(instr)) {
            InstrApplyActions actions = (InstrApplyActions) instr;
            if (actions.getActionList().size() != actionListSize) {
                return false;
            }
            BigPortNumber oldPort = null;
            for (Action action : actions.getActionList()) {
                if (action.getActionType() != actionType) {
                    return false;
                }

                // We will only be creating output flows
                if (ActOutput.class.isInstance(action)) {
                    ActOutput out = ((ActOutput) action);
                    if (!validPortList.contains(out.getPort())) {
                        return false;
                    }
                    if (out.getPort().equals(oldPort)) {
                        return false;
                    }
                    oldPort = out.getPort();
                }
            }
        } else if (instr.getInstructionType() == InstructionType.WRITE_ACTIONS) {

            InstrWriteActions actions = (InstrWriteActions) instr;
            if (actions.getActionSet().size() != 1) {
                return false;
            }

            for (Action action : actions.getActionSet()) {
                if (action.getActionType() != ActionType.OUTPUT) {
                    return false;
                }

                // We will only be creating output flows
                if (ActOutput.class.isInstance(action)) {
                    ActOutput out = ((ActOutput) action);
                    if (!(out.getPort().equals(Port.NORMAL))) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Populate the Match fields for DHCP flows.
     */
    public void populateDhcpMatchList() {
        fieldList.add(OxmBasicFieldType.ETH_TYPE);
        fieldList.add(OxmBasicFieldType.IP_PROTO);
        fieldList.add(OxmBasicFieldType.UDP_SRC);
        fieldList.add(OxmBasicFieldType.UDP_DST);
    }

    /**
     * Populate the Match fields for ARP flows.
     */
    public void populateArpMatchList() {
        fieldList.add(OxmBasicFieldType.ETH_TYPE);
    }

    /*-------------Create flows to be tested-------------
     * This test covers an ARP flow, a DHCP flow, an HSDP flow and various
     * forward normal and steal flows.  It is creating the flows for both
     * OF version 1.3 and 1.0 as an expected result of the testing class.
     */
    private class FlowCreator {

        private final Action cntrlaction10;

        private final Action normalaction10;

        private final Action cntrlaction13;
        private final Action invalidcntrlaction13;
        private final Action normalaction13;

        private final Instruction copyinstruct13;
        private final Instruction invalidcopyinstruct13;
        private final Instruction cntrlinstruct13;

        private final Instruction normalinstruct13;

        private final Match arpmatch10;

        private final Match arpmatch13;

        private final Match dhcpmatch10;

        private final Match dhcpmatch13;

        private final Match match10;

        private final Match match13;

        private final Action action10;

        private final Instruction instruct13;

        private final Match matchAll10;

        private final Match matchAll13;

        private final Match matchInvalid1;

        private final Instruction instrInvalid2;
        private final Instruction instrInvalid3;
        private final Instruction instrInvalid4;
        private final Instruction instrInvalid6;
        private final Action write2action13;
        private final Action write1action13;

        public FlowCreator() {

            // --- Create unchangeable reference flows for OF V1.0 ---
            ProtocolVersion pv = ProtocolVersion.V_1_0;

            // Create match condition for ARP Reply packet in OF 1.0
            arpmatch10 = (Match) MatchFactory
                    .createMatch(pv)
                    .addField(FieldFactory
                            .createBasicField(pv, OxmBasicFieldType.ETH_TYPE,
                                    EthernetType.ARP))
                    .toImmutable();

            // Create match condition for DHCP Reply packets(OFFER/ACK) in OF
            // 1.0
            dhcpmatch10 = (Match) MatchFactory
                    .createMatch(pv)
                    .addField(FieldFactory
                            .createBasicField(pv, OxmBasicFieldType.ETH_TYPE,
                                    EthernetType.IPv4))
                    .addField(FieldFactory
                            .createBasicField(pv, OxmBasicFieldType.IP_PROTO,
                                    IpProtocol.UDP))
                    .addField(FieldFactory
                            .createBasicField(pv, OxmBasicFieldType.UDP_SRC,
                                    PortNumber.valueOf(67)))
                    .addField(FieldFactory
                            .createBasicField(pv, OxmBasicFieldType.UDP_DST,
                                    PortNumber.valueOf(68)))
                    .toImmutable();

            // Create OF 1.0 action to send to controller
            cntrlaction10 = ActionFactory
                    .createAction(pv, ActionType.OUTPUT, Port.CONTROLLER,
                            ActOutput.CONTROLLER_NO_BUFFER);

            // Create OF 1.0 action to forward normal
            normalaction10 = ActionFactory.createAction(pv, ActionType.OUTPUT,
                    Port.NORMAL);

            match10 = (Match) MatchFactory
                    .createMatch(pv)
                    .addField(FieldFactory
                            .createBasicField(pv, OxmBasicFieldType.ETH_TYPE,
                                    EthernetType.BDDP))
                    .toImmutable();
            action10 = ActionFactory
                    .createAction(pv, ActionType.OUTPUT, Port.CONTROLLER,
                            ActOutput.CONTROLLER_NO_BUFFER);

            matchAll10 = (Match) MatchFactory.createMatch(pv).toImmutable();

            // --- Create unchangeable reference flows for OF V1.3 ---
            pv = ProtocolVersion.V_1_3;
            // Create match condition for ARP Reply packet in OF 1.3
            arpmatch13 = (Match) MatchFactory
                    .createMatch(pv)
                    .addField(FieldFactory
                            .createBasicField(pv, OxmBasicFieldType.ETH_TYPE,
                                    EthernetType.ARP))
                    .toImmutable();

            // Create match condition for DHCP Reply packets(OFFER/ACK) in OF
            // 1.3
            dhcpmatch13 = (Match) MatchFactory
                    .createMatch(pv)
                    .addField(FieldFactory
                            .createBasicField(pv, OxmBasicFieldType.ETH_TYPE,
                                    EthernetType.IPv4))
                    .addField(FieldFactory
                            .createBasicField(pv, OxmBasicFieldType.IP_PROTO,
                                    IpProtocol.UDP))
                    .addField(FieldFactory
                            .createBasicField(pv, OxmBasicFieldType.UDP_SRC,
                                    PortNumber.valueOf(67)))
                    .addField(FieldFactory
                            .createBasicField(pv, OxmBasicFieldType.UDP_DST,
                                    PortNumber.valueOf(68)))
                    .toImmutable();

            // Create OF 1.3 action to send to controller
            cntrlaction13 = ActionFactory
                    .createAction(pv, ActionType.OUTPUT, Port.CONTROLLER,
                            ActOutput.CONTROLLER_NO_BUFFER);
            invalidcntrlaction13 = ActionFactory
                    .createAction(pv,ActionType.COPY_TTL_IN);
            // Create OF 1.3 action to forward normal
            normalaction13 = ActionFactory.createAction(pv, ActionType.OUTPUT,
                    Port.NORMAL);
            write2action13 = ActionFactory.createAction(pv, ActionType.SET_MPLS_TTL, 2);

            // Create 1.3 instruction by combining both the actions.
            copyinstruct13 = (Instruction) InstructionFactory
                    .createMutableInstruction(ProtocolVersion.V_1_3,
                            InstructionType.APPLY_ACTIONS)
                    .addAction(cntrlaction13).addAction(normalaction13)
                    .toImmutable();
            invalidcopyinstruct13 = (Instruction) InstructionFactory
                    .createMutableInstruction(ProtocolVersion.V_1_3,
                            InstructionType.APPLY_ACTIONS)
                    .addAction(invalidcntrlaction13).toImmutable();

            // Create 1.3 instruction to steal all.
            cntrlinstruct13 = (Instruction) InstructionFactory
                    .createMutableInstruction(ProtocolVersion.V_1_3,
                            InstructionType.APPLY_ACTIONS)
                    .addAction(cntrlaction13).toImmutable();

            match13 = (Match) MatchFactory
                    .createMatch(pv)
                    .addField(FieldFactory
                            .createBasicField(pv, OxmBasicFieldType.ETH_TYPE,
                                    EthernetType.BDDP))
                    .toImmutable();
            Action action13 = ActionFactory
                    .createAction(pv, ActionType.OUTPUT, Port.CONTROLLER,
                            ActOutput.CONTROLLER_NO_BUFFER);
            write1action13 = ActionFactory.createAction(pv, ActionType.SET_FIELD,
                    FieldFactory.createBasicField(pv,
                            OxmBasicFieldType.ETH_DST, MacAddress.valueOf("11:22:33:44:55:66")));
            instruct13 = (Instruction) InstructionFactory
                    .createMutableInstruction(pv, InstructionType.APPLY_ACTIONS)
                    .addAction(action13).toImmutable();
            normalinstruct13 = (Instruction) InstructionFactory
                    .createMutableInstruction(pv, InstructionType.APPLY_ACTIONS)
                    .addAction(normalaction13).toImmutable();

            matchAll13 = (Match) MatchFactory.createMatch(pv).toImmutable();

            matchInvalid1 = (Match) MatchFactory
                    .createMatch(pv)
                    .addField(FieldFactory
                            .createBasicField(pv, OxmBasicFieldType.ETH_TYPE,
                                    EthernetType.IPv4))
                    .addField(FieldFactory
                            .createBasicField(pv, OxmBasicFieldType.VLAN_PCP,
                                    3))
                    .addField(FieldFactory
                            .createBasicField(pv, OxmBasicFieldType.VLAN_VID,
                                    VlanId.valueOf(3)))
                    .addField(FieldFactory
                            .createBasicField(pv, OxmBasicFieldType.IPV4_DST,
                                    IpAddress.BROADCAST_IPv4))
                    .toImmutable();

            instrInvalid2 = (Instruction) InstructionFactory
                    .createMutableInstruction(pv, InstructionType.WRITE_ACTIONS)
                    .addAction(write1action13).toImmutable();
            instrInvalid6 = (Instruction) InstructionFactory
                    .createMutableInstruction(pv, InstructionType.WRITE_ACTIONS)
                    .addAction(write2action13).toImmutable();
            instrInvalid4 = InstructionFactory
                    .createInstruction(pv, InstructionType.CLEAR_ACTIONS);
            instrInvalid3 = InstructionFactory
                    .createInstruction(pv, InstructionType.GOTO_TABLE, SW_TABLE);

        }

        /**
         * Build a flow modification message that will match all HSDP packets
         * and redirect (steal) them to the controller.
         *
         * @return the flow mod
         * @throws IllegalArgumentException if protocol version not supported
         */
        public OfmFlowMod buildHsdpFlowMod() {
            // Create a new flow add message
            OfmMutableFlowMod flow = (OfmMutableFlowMod) create(pv,
                    MessageType.FLOW_MOD,
                    FlowModCommand.ADD);
            flow.bufferId(BufferId.NO_BUFFER);

            // Choose cached values based upon protocol version
            if (pv == ProtocolVersion.V_1_0) {
                flow.match(match10).addAction(action10);
            } else if (pv == ProtocolVersion.V_1_3) {
                flow.match(match13).addInstruction(instruct13);
            } else {
                throw new IllegalArgumentException("Illegal protocol version "
                        + pv);
            }

            // No flow.idleTimeout specified. We want this flow to be
            // permanent.
            return (OfmFlowMod) flow.toImmutable();
        }

        /**
         * Build a flow modification message that will forward all packets
         * normally.
         *
         * @return the flow mod
         * @throws IllegalArgumentException if protocol version not supported
         */
        public OfmFlowMod buildFwdFlowMod() {
            // Create a new flow add message
            OfmMutableFlowMod flow = (OfmMutableFlowMod) create(pv,
                    MessageType.FLOW_MOD,
                    FlowModCommand.ADD);
            flow.bufferId(BufferId.NO_BUFFER);
            if (pv == ProtocolVersion.V_1_0) {
                flow.match(matchAll10).addAction(normalaction10);
            } else {
                flow.match(matchAll13).addInstruction(normalinstruct13);
            }

            // No flow.idleTimeout specified. We want this flow to be
            // permanent.
            return (OfmFlowMod) flow.toImmutable();
        }

        /**
         * Build a flow modification message that will steal all packets.
         *
         * @return the flow mod
         * @throws IllegalArgumentException if protocol version not supported
         */
        public OfmFlowMod buildStealFlowMod() {
            // Create a new flow add message
            OfmMutableFlowMod flow = (OfmMutableFlowMod) create(pv,
                    MessageType.FLOW_MOD,
                    FlowModCommand.ADD);
            flow.bufferId(BufferId.NO_BUFFER);
            if (pv == ProtocolVersion.V_1_0) {
                flow.match(matchAll10).addAction(cntrlaction10);
            } else {
                flow.match(matchAll13).addInstruction(cntrlinstruct13);
            }

            // No flow.idleTimeout specified. We want this flow to be
            // permanent.
            return (OfmFlowMod) flow.toImmutable();
        }

        /**
         * Build a flow modification message that will match all ARP reply
         * packets and send a copy of them to the controller in addition to
         * forwarding them normally.
         *
         * @return the flow mod
         * @throws IllegalArgumentException if protocol version not supported
         */
        public OfmFlowMod buildArpFlowMod() {
            // Create a new flow add message
            OfmMutableFlowMod flow = (OfmMutableFlowMod) MessageFactory
                    .create(pv, MessageType.FLOW_MOD, FlowModCommand.ADD);
            flow.bufferId(BufferId.NO_BUFFER);

            // Choose cached values based upon protocol version
            if (pv == ProtocolVersion.V_1_0) {
                flow.match(arpmatch10).addAction(cntrlaction10)
                        .addAction(normalaction10);
            } else if (pv == ProtocolVersion.V_1_3) {
                flow.match(arpmatch13).addInstruction(copyinstruct13);
            } else {
                throw new IllegalArgumentException("Illegal protocol version "
                        + pv);
            }

            // No flow.idleTimeout specified. We want this flow to be
            // permanent.
            return (OfmFlowMod) flow.toImmutable();
        }

        /**
         * Build a flow modification message that will match all DHCP reply
         * (i.e. DHCPOFFER and DHCPACK) packets and send a copy of them to the
         * controller in addition to forwarding them normally.
         *
         * @return the flow mod
         * @throws IllegalArgumentException if protocol version not supported
         */
        public OfmFlowMod buildDhcpFlowMod() {
            // Create a new flow add message
            OfmMutableFlowMod flow = (OfmMutableFlowMod) MessageFactory
                    .create(pv, MessageType.FLOW_MOD, FlowModCommand.ADD);
            flow.bufferId(BufferId.NO_BUFFER);

            // Choose cached values based upon protocol version
            if (pv.lt(ProtocolVersion.V_1_3)) {
                flow.match(dhcpmatch10).addAction(cntrlaction10)
                        .addAction(normalaction10);
            } else if (pv == ProtocolVersion.V_1_3) {
                flow.match(dhcpmatch13).addInstruction(copyinstruct13);
            } else {
                throw new IllegalArgumentException("Illegal protocol version "
                        + pv);
            }

            // No flow.idleTimeout specified. We want this flow to be
            // permanent.
            return (OfmFlowMod) flow.toImmutable();
        }

        /**
         * Build the default goto rules.
         *
         * @return List of default rules
         */
        public List<OfmFlowMod> buildDefaultRules() {
            OfmMutableFlowMod flow;
            List<OfmFlowMod> defaultRules = new ArrayList<>();

            if (pv == ProtocolVersion.V_1_3) {
                // hw table
                flow = (OfmMutableFlowMod) MessageFactory
                        .create(pv, MessageType.FLOW_MOD, FlowModCommand.ADD);

                flow.match(matchAll13).tableId(HW_TABLE);

                if (!hybridMode) {
                    flow.addInstruction(InstructionFactory
                            .createInstruction(pv, InstructionType.GOTO_TABLE,
                                    SW_TABLE));
                } else {
                    flow.addInstruction((Instruction) InstructionFactory
                            .createMutableInstruction(pv,
                                    InstructionType.APPLY_ACTIONS)
                            .addAction(normalaction13).toImmutable());
                }

                defaultRules.add((OfmFlowMod) flow.toImmutable());

                // sw table
                flow = (OfmMutableFlowMod) MessageFactory
                        .create(pv, MessageType.FLOW_MOD, FlowModCommand.ADD);

                flow.match(matchAll13).tableId(SW_TABLE);

                if (hybridMode) {
                    flow.addInstruction((Instruction) InstructionFactory
                            .createMutableInstruction(pv,
                                    InstructionType.APPLY_ACTIONS)
                            .addAction(normalaction13).toImmutable());
                } else {
                    flow.addInstruction((Instruction) InstructionFactory
                            .createMutableInstruction(pv,
                                    InstructionType.APPLY_ACTIONS)
                            .addAction(cntrlaction13).toImmutable());
                }

                defaultRules.add((OfmFlowMod) flow.toImmutable());
            } else {
                if (hybridMode) {
                    defaultRules.add(flowCreator.buildFwdFlowMod());
                } else {
                    defaultRules.add(flowCreator.buildStealFlowMod());
                }
            }

            return defaultRules;
        }

        /**
         * Build a flow modification message that will match ethtype 800 and
         * ip src, causing a split for v1.
         *
         * @return the flow mod
         * @throws IllegalArgumentException if protocol version not supported
         */
        public OfmFlowMod buildInvalidMatch1() {
            // Create a new flow add message
            OfmMutableFlowMod flow = (OfmMutableFlowMod) create(pv,
                    MessageType.FLOW_MOD,
                    FlowModCommand.ADD);
            flow.bufferId(BufferId.NO_BUFFER);

            // Choose cached values based upon protocol version
            if (pv == ProtocolVersion.V_1_0) {
                flow.match(match10).addAction(action10);
            } else if (pv == ProtocolVersion.V_1_3) {
                flow.match(matchInvalid1).addInstruction(instruct13);
            } else {
                throw new IllegalArgumentException("Illegal protocol version "
                        + pv);
            }

            // No flow.idleTimeout specified. We want this flow to be
            // permanent.
            return (OfmFlowMod) flow.toImmutable();
        }

        /**
         * Build a flow modification message that with an unknown instruction.
         *
         * @return the flow mod
         * @throws IllegalArgumentException if protocol version not supported
         */
        public OfmFlowMod buildInvalidInstruction1() {
            // Create a new flow add message
            OfmMutableFlowMod flow = (OfmMutableFlowMod) create(pv,
                    MessageType.FLOW_MOD,
                    FlowModCommand.ADD);
            flow.bufferId(BufferId.NO_BUFFER);

            // Choose cached values based upon protocol version
            if (pv == ProtocolVersion.V_1_0) {
                flow.match(match10).addAction(action10);
            } else if (pv == ProtocolVersion.V_1_3) {
                flow.match(matchAll13).addInstruction(invalidcopyinstruct13);
            } else {
                throw new IllegalArgumentException("Illegal protocol version "
                        + pv);
            }

            // No flow.idleTimeout specified. We want this flow to be
            // permanent.
            return (OfmFlowMod) flow.toImmutable();
        }

        /**
         * Build a flow modification message that will match anything but
         * with an invalid instruction.
         *
         * @return the flow mod
         * @throws IllegalArgumentException if protocol version not supported
         */
        public OfmFlowMod buildInvalidInstruction2() {
            // Create a new flow add message
            OfmMutableFlowMod flow = (OfmMutableFlowMod) create(pv,
                    MessageType.FLOW_MOD,
                    FlowModCommand.ADD);
            flow.bufferId(BufferId.NO_BUFFER);
            // Choose cached values based upon protocol version

            if (pv == ProtocolVersion.V_1_0) {
                flow.match(match10).addAction(action10);
            } else if (pv == ProtocolVersion.V_1_3) {
                flow.match(matchAll13).addInstruction(instrInvalid2);
            } else {
                throw new IllegalArgumentException("Illegal protocol version "
                        + pv);
            }
            // No flow.idleTimeout specified. We want this flow to be
            // permanent.
            return (OfmFlowMod) flow.toImmutable();
        }

        /**
         * Build a flow modification message that will match anything but
         * with an invalid instruction.
         *
         * @return the flow mod
         * @throws IllegalArgumentException if protocol version not supported
         */
        public OfmFlowMod buildInvalidInstruction6() {
            // Create a new flow add message
            OfmMutableFlowMod flow = (OfmMutableFlowMod) create(pv,
                    MessageType.FLOW_MOD,
                    FlowModCommand.ADD);
            flow.bufferId(BufferId.NO_BUFFER);
            // Choose cached values based upon protocol version

            if (pv == ProtocolVersion.V_1_0) {
                flow.match(match10).addAction(action10);
            } else if (pv == ProtocolVersion.V_1_3) {
                flow.match(match13).addInstruction(instrInvalid6);
            } else {
                throw new IllegalArgumentException("Illegal protocol version "
                        + pv);
            }
            // No flow.idleTimeout specified. We want this flow to be
            // permanent.
            return (OfmFlowMod) flow.toImmutable();
        }

        /**
         * Build a flow modification message that will match anything but
         * with an invalid instruction.
         *
         * @return the flow mod
         * @throws IllegalArgumentException if protocol version not supported
         */
        public OfmFlowMod buildInvalidInstruction4() {
            // Create a new flow add message
            OfmMutableFlowMod flow = (OfmMutableFlowMod) create(pv,
                    MessageType.FLOW_MOD,
                    FlowModCommand.ADD);
            flow.bufferId(BufferId.NO_BUFFER);
            // Choose cached values based upon protocol version

            if (pv == ProtocolVersion.V_1_0) {
                flow.match(match10).addAction(action10);
            } else if (pv == ProtocolVersion.V_1_3) {
                flow.match(matchAll13).addInstruction(instrInvalid4);
            } else {
                throw new IllegalArgumentException("Illegal protocol version "
                        + pv);
            }
            // No flow.idleTimeout specified. We want this flow to be
            // permanent.
            return (OfmFlowMod) flow.toImmutable();
        }

        /**
         * Build a flow modification message that will match anything but
         * with an invalid instruction.
         *
         * @return the flow mod
         * @throws IllegalArgumentException if protocol version not supported
         */
        public OfmFlowMod buildInvalidInstruction5() {
            // Create a new flow add message
            OfmMutableFlowMod flow = (OfmMutableFlowMod) create(pv,
                    MessageType.FLOW_MOD,
                    FlowModCommand.ADD);
            flow.bufferId(BufferId.NO_BUFFER);
            // Choose cached values based upon protocol version

            if (pv == ProtocolVersion.V_1_0) {
                flow.match(match10).addAction(action10);
            } else if (pv == ProtocolVersion.V_1_3) {
                flow.match(match13).addInstruction(instrInvalid4);
            } else {
                throw new IllegalArgumentException("Illegal protocol version "
                        + pv);
            }
            // No flow.idleTimeout specified. We want this flow to be
            // permanent.
            return (OfmFlowMod) flow.toImmutable();
        }

        /**
         * Build a flow modification message that will match anything but with
         * an invalid instruction.
         *
         * @return the flow mod
         * @throws IllegalArgumentException if protocol version not supported
         */
        public OfmFlowMod buildInvalidInstruction3() {
            // Create a new flow add message
            OfmMutableFlowMod flow = (OfmMutableFlowMod) create(pv,
                    MessageType.FLOW_MOD,
                    FlowModCommand.ADD);
            flow.bufferId(BufferId.NO_BUFFER);

            // Choose cached values based upon protocol version
            if (pv == ProtocolVersion.V_1_0) {
                flow.match(match10).addAction(action10);
            } else if (pv == ProtocolVersion.V_1_3) {
                flow.match(matchAll13).addInstruction(instrInvalid3);
            } else {
                throw new IllegalArgumentException("Illegal protocol version "
                        + pv);
            }

            // No flow.idleTimeout specified. We want this flow to be
            // permanent.
            return (OfmFlowMod) flow.toImmutable();
        }


        /**
         * Build a flow mod based on Sentinel flows.
         *
         * @return the flow mod
         */
        public OfmFlowMod buildSentinelFlow() {
            MutableMatch mm = MatchFactory.createMatch(pv);
            if (pv.equals(ProtocolVersion.V_1_3)) {
                OfmMutableFlowMod fMod = (OfmMutableFlowMod) MessageFactory
                        .create(pv, MessageType.FLOW_MOD, FlowModCommand.ADD);
                mm.addField(FieldFactory.createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
                mm.addField(FieldFactory.createBasicField(pv, IP_PROTO, IpProtocol.UDP));
                mm.addField(FieldFactory.createBasicField(pv, VLAN_VID, VlanId.valueOf(12)));
                mm.addField(FieldFactory.createBasicField(pv, UDP_DST, PortNumber.valueOf(53)));
                Match m = (Match) mm.toImmutable();
                InstrMutableAction mutIns = InstructionFactory
                        .createMutableInstruction(pv, InstructionType.APPLY_ACTIONS);
                mutIns.addAction(ActionFactory
                        .createAction(pv, ActionType.OUTPUT, Port.NORMAL,
                                ActOutput.CONTROLLER_NO_BUFFER));
                fMod.match(m).bufferId(BufferId.NO_BUFFER).addInstruction((Instruction)mutIns.toImmutable())
                        .outPort(Port.ANY);
                return fMod;
            } else if (pv.equals(ProtocolVersion.V_1_0)) {
                OfmMutableFlowMod fMod = (OfmMutableFlowMod) MessageFactory.
                        create(pv, MessageType.FLOW_MOD);
                mm.addField(FieldFactory.createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
                mm.addField(FieldFactory.createBasicField(pv, IP_PROTO, IpProtocol.UDP));
                mm.addField(FieldFactory.createBasicField(pv, UDP_DST, PortNumber.valueOf(53)));
                Match m = (Match) mm.toImmutable();
                fMod.match(m).bufferId(BufferId.NO_BUFFER).outPort(Port.ANY)
                        .command(FlowModCommand.DELETE_STRICT);
                return fMod;
            }

            throw new IllegalArgumentException("Illegal protocol version "
                    + pv);
        }

        /**
         * Build a flow mod based on Vega flows.
         *
         * @return the flow mod
         */
        public OfmFlowMod buildVegaFlow() {
            MutableMatch mm = MatchFactory.createMatch(pv);
            if (pv.equals(ProtocolVersion.V_1_3)) {
                OfmMutableFlowMod fMod = (OfmMutableFlowMod) MessageFactory
                        .create(pv, MessageType.FLOW_MOD, FlowModCommand.MODIFY);
                mm.addField(FieldFactory.createBasicField(pv, IPV4_SRC, IpAddress.valueOf("1.1.1.1")));
                mm.addField(FieldFactory.createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
                Match m = (Match) mm.toImmutable();
                InstrMutableAction mutIns = InstructionFactory
                        .createMutableInstruction(pv, InstructionType.APPLY_ACTIONS);
                mutIns.addAction(ActionFactory
                        .createActionSetField(pv, OxmBasicFieldType.IP_DSCP,25))
                        .addAction(ActionFactory
                                .createActionSetField(pv, OxmBasicFieldType.VLAN_PCP, 6));
                mutIns.addAction(ActionFactory.createAction(pv, ActionType.OUTPUT, Port.NORMAL));
                //fMod.match(m).addInstruction((Instruction)mutIns.toImmutable());
                fMod.match(m).bufferId(BufferId.NO_BUFFER).addInstruction((Instruction)mutIns.toImmutable());
                return fMod;
            } else if (pv.equals(ProtocolVersion.V_1_0)) {
                OfmMutableFlowMod fMod = (OfmMutableFlowMod) MessageFactory.
                        create(pv, MessageType.FLOW_MOD, FlowModCommand.MODIFY);
                mm.addField(FieldFactory.createBasicField(pv, IPV4_SRC, IpAddress.valueOf("1.1.1.1")));
                mm.addField(FieldFactory.createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
                Match m = (Match) mm.toImmutable();
                fMod.match(m).bufferId(BufferId.NO_BUFFER).addAction(ActionFactory
                        .createActionSetField(pv, OxmBasicFieldType.IP_DSCP,25))
                        .addAction(ActionFactory
                                .createActionSetField(pv, OxmBasicFieldType.VLAN_PCP, 6));
                fMod.addAction(ActionFactory.createAction(pv, ActionType.OUTPUT, Port.NORMAL));

                return fMod;
            }

            throw new IllegalArgumentException("Illegal protocol version "
                    + pv);
        }
    }
}
