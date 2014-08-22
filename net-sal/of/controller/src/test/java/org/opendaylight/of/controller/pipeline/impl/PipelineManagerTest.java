/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pipeline.impl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.of.controller.MessageEvent;
import org.opendaylight.of.controller.OpenflowEventType;
import org.opendaylight.of.controller.impl.ListenerServiceAdapter;
import org.opendaylight.of.controller.pipeline.PipelineDefinition;
import org.opendaylight.of.controller.pipeline.TableContext;
import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.instr.InstructionType;
import org.opendaylight.of.lib.match.OxmBasicFieldType;
import org.opendaylight.of.lib.mp.MBodyMutableTableFeatures;
import org.opendaylight.of.lib.mp.MBodyTableFeatures;
import org.opendaylight.of.lib.mp.MpBodyFactory;
import org.opendaylight.of.lib.mp.MultipartBody;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.junit.TestLogger;

import java.util.*;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.dt.DataPathId.dpid;
import static org.opendaylight.of.lib.dt.TableId.tid;
import static org.opendaylight.of.lib.mp.MultipartType.TABLE_FEATURES;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;
import static org.opendaylight.of.lib.msg.TableFeatureFactory.*;
import static org.opendaylight.of.lib.msg.TableFeaturePropType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Set of test cases for the {@link PipelineManager}.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
// FIXME: Will need complete rework, in the wake of updating PipelineManager
public class PipelineManagerTest {
    private static final DataPathId DPID = dpid("0x7b/000553:afaac0");
    private static final TableId TID_0 = tid("0");
    private static final TableId TID_1 = tid("1");
    private static final TableId TID_2 = tid("2");

    private static final Set<TableId> TABLE_O_NEXT_IDS =
            new TreeSet<>(asList(TID_1, TID_2));
    private static final Set<TableId> TABLE_1_NEXT_IDS =
            new TreeSet<>(asList(TID_2));

    private static final Set<InstructionType> SUPP_INSTR = 
            EnumSet.of(InstructionType.APPLY_ACTIONS);
    private static final Set<InstructionType> SUPP_INSTR_MISS = 
            EnumSet.of(InstructionType.CLEAR_ACTIONS);
    private static final Set<InstructionType> EMPTY_INSTR = 
            Collections.emptySet();

    private static final Set<ActionType> SUPP_ACTION = EnumSet.of(ActionType.OUTPUT);
    private static final Set<ActionType> SUPP_ACTION_MISS = EnumSet.of(ActionType.GROUP);
    private static final Set<ActionType> EMPTY_ACTION = Collections.emptySet();

    private static final Map<OxmBasicFieldType, Boolean> SUPP_MATCH_CAPS =
            new HashMap<>();
    private static final Map<OxmBasicFieldType, Boolean> SUPP_MATCH_MISS_CAPS =
            new HashMap<>();
    static {
        SUPP_MATCH_CAPS.put(OxmBasicFieldType.ETH_DST, false);
        SUPP_MATCH_MISS_CAPS.put(OxmBasicFieldType.ETH_TYPE, true);
    }
    
    private static final Map<OxmBasicFieldType, Boolean> EMPTY_MATCH_CAPS =
            Collections.emptyMap();

    private static final TestLogger tlog = new TestLogger();

    @BeforeClass
    public static void classSetUp() {
        PipelineManager.setLogger(tlog);
    }

    @AfterClass
    public static void classTearDown() {
        PipelineManager.restoreLogger();
    }

    @Test @Ignore("Needs rework, now that core controller collects raw data")
    public void tableFeatureNoMiss() throws IncompleteStructureException {
        print(EOL + "tableFeatureNoMiss()");

        OpenflowMessage noMissIncluded = noMissReplyMsg();

        PipelineManager mgr = new PipelineManager();
        mgr.init(new MyListener(noMissIncluded), null);

//        MessageListener pipeListener = mgr.getMyMessageListener();
        mgr.getDefinition(DPID);
//        pipeListener.event(buildMessageEvent(noMissIncluded));

        PipelineDefinition def = mgr.getDefinition(DPID);
        assertEquals(AM_NEQ, 3, def.getTableIds().size());

        TableContext context = def.getTableContext(TID_0);
        checkTableContextNoMiss(context, TABLE_O_NEXT_IDS, TID_1);

        context = def.getTableContext(TID_1);
        checkTableContextNoMiss(context, TABLE_1_NEXT_IDS, TID_2);

        context = def.getTableContext(TID_2);
        checkTableContextNoMiss(context, new TreeSet<TableId>(), null);
    }

    private void checkTableContextNoMiss(TableContext context,
                                         Set<TableId> expNextTids,
                                         TableId expNext) {
        if (expNextTids.size() > 0) {
            assertTrue(AM_HUH, context.hasNextTablesMiss());
            for (TableId tid : expNextTids) {
                assertTrue(AM_HUH, context.containsNextTable(tid));
                assertTrue(AM_HUH, context.containsNextTableMiss(tid));
            }
            assertEquals(AM_NEQ, expNext, context.getNextTableMiss());
        } else {
            assertFalse(context.hasNextTablesMiss());
        }

        assertTrue(AM_HUH, context.supportsCapability(INSTRUCTIONS,
                InstructionType.GOTO_TABLE));
        assertTrue(AM_HUH, context.supportsCapability(INSTRUCTIONS_MISS,
                InstructionType.GOTO_TABLE));
        assertTrue(AM_HUH, context.supportsCapability(WRITE_ACTIONS,
                ActionType.OUTPUT));
        assertTrue(AM_HUH, context.supportsCapability(WRITE_ACTIONS_MISS,
                ActionType.OUTPUT));
        assertTrue(AM_HUH, context.supportsCapability(APPLY_ACTIONS,
                ActionType.COPY_TTL_IN));
        assertTrue(AM_HUH, context.supportsCapability(APPLY_ACTIONS_MISS,
                ActionType.COPY_TTL_IN));

        assertTrue(AM_HUH, context.supportsMatchFieldCapability(WRITE_SETFIELD,
                OxmBasicFieldType.ICMPV4_CODE));
        assertTrue(AM_HUH, context.supportsMatchFieldCapability(WRITE_SETFIELD_MISS,
                OxmBasicFieldType.ICMPV4_CODE));
        assertTrue(AM_HUH, context.supportsMatchFieldCapability(APPLY_SETFIELD,
                OxmBasicFieldType.ARP_SHA));
        assertTrue(AM_HUH, context.supportsMatchFieldCapability(APPLY_SETFIELD_MISS,
                OxmBasicFieldType.ARP_SHA));
    }

    @Test @Ignore("Needs rework, now that core controller collects raw data")
    public void tableFeatureWithMiss() throws IncompleteStructureException {
        print(EOL + "tableFeatureWithMiss()");

        OpenflowMessage withMiss = replyMsgWithMiss();

        PipelineManager mgr = new PipelineManager();
        mgr.init(new MyListener(withMiss), null);

//        MessageListener pipeListener = mgr.getMyMessageListener();
        mgr.getDefinition(DPID);
//        pipeListener.event(buildMessageEvent(withMiss));

        PipelineDefinition def = mgr.getDefinition(DPID);
        assertEquals(AM_NEQ, 3, def.getTableIds().size());

        TableContext context = def.getTableContext(TID_0);
        checkTableContextWithMiss(context, TABLE_O_NEXT_IDS);

        context = def.getTableContext(TID_1);
        checkTableContextWithMiss(context, TABLE_1_NEXT_IDS);

        context = def.getTableContext(TID_2);
        checkTableContextWithMiss(context, new TreeSet<TableId>());
    }

    private void checkTableContextWithMiss(TableContext context,
                                           Set<TableId> expNextTids) {
        assertFalse(AM_HUH, context.hasNextTablesMiss());
        if (expNextTids.size() > 0) {
            for (TableId tid : expNextTids) {
                assertTrue(AM_HUH, context.containsNextTable(tid));
                assertFalse(AM_HUH, context.containsNextTableMiss(tid));
            }
        }
        assertTrue(AM_HUH, context.supportsCapability(INSTRUCTIONS,
                InstructionType.APPLY_ACTIONS));
        assertTrue(AM_HUH, context.supportsCapability(INSTRUCTIONS_MISS,
                InstructionType.CLEAR_ACTIONS));
        assertTrue(AM_HUH, context.supportsCapability(WRITE_ACTIONS,
                ActionType.OUTPUT));
        assertTrue(AM_HUH, context.supportsCapability(WRITE_ACTIONS_MISS,
                ActionType.GROUP));
        assertTrue(AM_HUH, context.supportsCapability(APPLY_ACTIONS,
                ActionType.OUTPUT));
        assertTrue(AM_HUH, context.supportsCapability(APPLY_ACTIONS_MISS,
                ActionType.OUTPUT));

        assertTrue(AM_HUH, context.supportsMatchFieldCapability(WRITE_SETFIELD,
                OxmBasicFieldType.ETH_DST));
        assertTrue(AM_HUH, context.supportsMatchFieldCapability(WRITE_SETFIELD_MISS,
                OxmBasicFieldType.ETH_TYPE));
        assertTrue(AM_HUH, context.supportsMatchFieldCapability(APPLY_SETFIELD,
                OxmBasicFieldType.ETH_DST));
        assertTrue(AM_HUH, context.supportsMatchFieldCapability(APPLY_SETFIELD_MISS,
                OxmBasicFieldType.ETH_TYPE));
    }

    @Test @Ignore("Needs rework, now that core controller collects raw data")
    public void tableFeatureWithUnsupportedMiss() throws IncompleteStructureException {
        print(EOL + "tableFeatureWithUnsupportedMiss()");

        OpenflowMessage withUnsupportedMiss = replyMsgUnsupportedMiss();

        PipelineManager mgr = new PipelineManager();
        mgr.init(new MyListener(withUnsupportedMiss), null);

//        MessageListener pipeListener = mgr.getMyMessageListener();
        mgr.getDefinition(DPID);
//        pipeListener.event(buildMessageEvent(withUnsupportedMiss));

        PipelineDefinition def = mgr.getDefinition(DPID);
        assertEquals(AM_NEQ, 3, def.getTableIds().size());

        TableContext context = def.getTableContext(TID_0);
        checkTableContextWithUnsupportedMiss(context, TABLE_O_NEXT_IDS, TID_1);

        context = def.getTableContext(TID_1);
        checkTableContextWithUnsupportedMiss(context, TABLE_1_NEXT_IDS, TID_2);

        context = def.getTableContext(TID_2);
        checkTableContextWithUnsupportedMiss(context, new TreeSet<TableId>(), null);
    }

    private void checkTableContextWithUnsupportedMiss(TableContext context,
                                           Set<TableId> expNextTids,
                                           TableId expNext) {
        if (expNextTids.size() > 0) {
            assertTrue(AM_HUH, context.hasNextTablesMiss());
            for (TableId tid : expNextTids) {
                assertTrue(AM_HUH, context.containsNextTable(tid));
                assertTrue(AM_HUH, context.containsNextTableMiss(tid));
            }
            assertEquals(AM_NEQ, expNext, context.getNextTableMiss());
        } else {
            assertFalse(context.hasNextTablesMiss());
        }

        assertTrue(AM_HUH, context.supportsCapability(INSTRUCTIONS,
                InstructionType.APPLY_ACTIONS));

        for (InstructionType instrType : EnumSet.allOf(InstructionType.class)) {
            assertFalse(AM_HUH, context.supportsCapability(INSTRUCTIONS_MISS, instrType));
        }

        assertTrue(AM_HUH, context.supportsCapability(APPLY_ACTIONS,
                ActionType.OUTPUT));

        for (ActionType actType : EnumSet.allOf(ActionType.class)) {
            assertFalse(AM_HUH, context.supportsCapability(WRITE_ACTIONS, actType));
            assertFalse(AM_HUH, context.supportsCapability(WRITE_ACTIONS_MISS, actType));
            assertFalse(AM_HUH, context.supportsCapability(APPLY_ACTIONS_MISS, actType));
        }

        assertTrue(AM_HUH, context.supportsMatchFieldCapability(WRITE_SETFIELD,
                OxmBasicFieldType.ETH_DST));
        assertTrue(AM_HUH, context.supportsMatchFieldCapability(APPLY_SETFIELD,
                OxmBasicFieldType.ETH_DST));

        for (OxmBasicFieldType basicType : EnumSet.allOf(OxmBasicFieldType.class)) {
            assertFalse(AM_HUH, context.supportsMatchFieldCapability(WRITE_SETFIELD_MISS, basicType));
            assertFalse(AM_HUH, context.supportsMatchFieldCapability(APPLY_SETFIELD_MISS, basicType));
        }
    }

    private static final String EXP_NO_EXPER_LOG =
            "Ignoring TableFeaturePropType EXPERIMENTER - not added to table context";
    private static final String EXP_MISS_MATCH_LOG =
            "Missing table feature prop type MATCH in switch response";

    @Test @Ignore("Needs rework, now that core controller collects raw data")
    public void tableFeatureMissingMatch() throws IncompleteStructureException {
        print(EOL + "tableFeatureMissingMatch()");

        OpenflowMessage withNoMatch = replyNoMatch();

        PipelineManager mgr = new PipelineManager();
        mgr.init(new MyListener(withNoMatch), null);

//        MessageListener pipeListener = mgr.getMyMessageListener();
        mgr.getDefinition(DPID);
//        pipeListener.event(buildMessageEvent(withNoMatch));

        PipelineDefinition def = mgr.getDefinition(DPID);
        assertEquals(AM_NEQ, 1, def.getTableIds().size());

        TableContext context = def.getTableContext(TID_0);
        checkTableContextNoMatch(context);
    }

    private void checkTableContextNoMatch(TableContext context) {
        assertFalse(context.hasNextTablesMiss());

        assertTrue(AM_HUH, context.supportsCapability(INSTRUCTIONS,
                InstructionType.GOTO_TABLE));
        assertTrue(AM_HUH, context.supportsCapability(INSTRUCTIONS_MISS,
                InstructionType.GOTO_TABLE));
        assertTrue(AM_HUH, context.supportsCapability(WRITE_ACTIONS,
                ActionType.OUTPUT));
        assertTrue(AM_HUH, context.supportsCapability(WRITE_ACTIONS_MISS,
                ActionType.OUTPUT));
        assertTrue(AM_HUH, context.supportsCapability(APPLY_ACTIONS,
                ActionType.COPY_TTL_IN));
        assertTrue(AM_HUH, context.supportsCapability(APPLY_ACTIONS_MISS,
                ActionType.COPY_TTL_IN));

        assertTrue(AM_HUH, context.supportsMatchFieldCapability(WRITE_SETFIELD,
                OxmBasicFieldType.ICMPV4_CODE));
        assertTrue(AM_HUH, context.supportsMatchFieldCapability(WRITE_SETFIELD_MISS,
                OxmBasicFieldType.ICMPV4_CODE));
        assertTrue(AM_HUH, context.supportsMatchFieldCapability(APPLY_SETFIELD,
                OxmBasicFieldType.ARP_SHA));
        assertTrue(AM_HUH, context.supportsMatchFieldCapability(APPLY_SETFIELD_MISS,
                OxmBasicFieldType.ARP_SHA));

        tlog.assertInfo(EXP_NO_EXPER_LOG);
        tlog.assertWarning(EXP_MISS_MATCH_LOG);
    }

    private static final String EXP_BAD_TABLE_ID_LOG =
            "Invalid next table 2 encountered for table 2";

    @Test @Ignore("Needs rework, now that core controller collects raw data")
    public void nextTableIsItself() throws IncompleteStructureException {
        print(EOL + "nextTableIsItself()");

        OpenflowMessage nextTableIsMe = replyNextIsSelf();

        PipelineManager mgr = new PipelineManager();
        mgr.init(new MyListener(nextTableIsMe), null);

//        MessageListener pipeListener = mgr.getMyMessageListener();
        mgr.getDefinition(DPID);
//        pipeListener.event(buildMessageEvent(nextTableIsMe));

        PipelineDefinition def = mgr.getDefinition(DPID);
        assertEquals(AM_NEQ, 3, def.getTableIds().size());
        TableContext context = def.getTableContext(TID_0);
        checkTableNextIsSelf(context, TABLE_O_NEXT_IDS, TID_1);

        context = def.getTableContext(TID_1);
        checkTableNextIsSelf(context, TABLE_1_NEXT_IDS, TID_2);

        context = def.getTableContext(TID_2);
        checkTableNextIsSelf(context, new TreeSet<TableId>(), null);

        tlog.assertWarning(EXP_BAD_TABLE_ID_LOG);
    }

    private void checkTableNextIsSelf(TableContext context,
                                      Set<TableId> expNextTids,
                                      TableId expNext) {

        if (expNextTids.size() > 0) {
            assertTrue(AM_HUH, context.hasNextTablesMiss());
            for (TableId tid : expNextTids) {
                assertTrue(AM_HUH, context.containsNextTable(tid));
                assertTrue(AM_HUH, context.containsNextTableMiss(tid));
            }
            assertEquals(AM_NEQ, expNext, context.getNextTableMiss());
        } else {
            assertFalse(context.hasNextTablesMiss());
        }

        assertTrue(AM_HUH, context.supportsCapability(INSTRUCTIONS,
                InstructionType.GOTO_TABLE));
        assertTrue(AM_HUH, context.supportsCapability(INSTRUCTIONS_MISS,
                InstructionType.GOTO_TABLE));
        assertTrue(AM_HUH, context.supportsCapability(WRITE_ACTIONS,
                ActionType.OUTPUT));
        assertTrue(AM_HUH, context.supportsCapability(WRITE_ACTIONS_MISS,
                ActionType.OUTPUT));
        assertTrue(AM_HUH, context.supportsCapability(APPLY_ACTIONS,
                ActionType.COPY_TTL_IN));
        assertTrue(AM_HUH, context.supportsCapability(APPLY_ACTIONS_MISS,
                ActionType.COPY_TTL_IN));

        assertTrue(AM_HUH, context.supportsMatchFieldCapability(WRITE_SETFIELD,
                OxmBasicFieldType.ICMPV4_CODE));
        assertTrue(AM_HUH, context.supportsMatchFieldCapability(WRITE_SETFIELD_MISS,
                OxmBasicFieldType.ICMPV4_CODE));
        assertTrue(AM_HUH, context.supportsMatchFieldCapability(APPLY_SETFIELD,
                OxmBasicFieldType.ARP_SHA));
        assertTrue(AM_HUH, context.supportsMatchFieldCapability(APPLY_SETFIELD_MISS,
                OxmBasicFieldType.ARP_SHA));
    }

    private class MyListener extends ListenerServiceAdapter {
        private final OpenflowMessage reply;
        private MyListener(OpenflowMessage toSend) {
            reply = toSend;
        }
        DataPathMessageFuture request;

        @Override public ProtocolVersion versionOf(DataPathId dpid) { return ProtocolVersion.V_1_3; }

        @Override
        public void sendFuture(DataPathMessageFuture f, OpenflowMessage... msgs) throws OpenflowException {
            request = f;
            request.setSuccess(reply);
        }

        @Override
        public DataPathMessageFuture findFuture(OpenflowMessage msg, DataPathId dpid) {
            return request;
        }

    }

    private MessageEvent buildMessageEvent(final OpenflowMessage msg)
            throws IncompleteStructureException {
        return new MessageEvent() {
            OpenflowMessage ofm = msg;
            
            @Override public OpenflowMessage msg() { return ofm; }
            @Override public DataPathId dpid() { return DPID; }
            @Override public int auxId() { return 0; }
            @Override public ProtocolVersion negotiated() { return V_1_3; }
            @Override public String remoteId() { return DPID.toString(); }
            @Override public long ts() { return 0; }
            @Override public OpenflowEventType type() { return OpenflowEventType.MESSAGE_RX; }
        };
    }

    private OpenflowMessage noMissReplyMsg() throws IncompleteStructureException {
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY, TABLE_FEATURES);

        MBodyTableFeatures.MutableArray array =
                (MBodyTableFeatures.MutableArray)
                        MpBodyFactory.createReplyBody(V_1_3, TABLE_FEATURES);
        array.addTableFeatures(createNoMissTable(TID_0, TABLE_O_NEXT_IDS));
        array.addTableFeatures(createNoMissTable(TID_1, TABLE_1_NEXT_IDS));
        array.addTableFeatures(createNoMissTable(TID_2, new TreeSet<TableId>()));
        reply.body((MultipartBody)array.toImmutable());
        return reply.toImmutable();
    }

    private MBodyTableFeatures createNoMissTable(TableId id, Set<TableId> nextTableIds) {
        MBodyMutableTableFeatures tf = (MBodyMutableTableFeatures)
                MpBodyFactory.createReplyBodyElement(V_1_3, TABLE_FEATURES);
        tf.tableId(id).name("Flow Table: " + id.toString())
                .metadataMatch(MBodyTableFeatures.ALL_META_BITS)
                .metadataWrite(MBodyTableFeatures.ALL_META_BITS)
                .maxEntries(16777216);
        Set<InstructionType> suppInst =
                new HashSet<>(asList(InstructionType.GOTO_TABLE,
                        InstructionType.WRITE_METADATA, InstructionType.WRITE_ACTIONS,
                        InstructionType.APPLY_ACTIONS, InstructionType.CLEAR_ACTIONS,
                        InstructionType.METER));
        Set<ActionType> actionTypeSet = EnumSet.allOf(ActionType.class);
        tf.addProp(createInstrProp(V_1_3, INSTRUCTIONS, suppInst))
                .addProp(createNextTablesProp(V_1_3, TableFeaturePropType.NEXT_TABLES, nextTableIds))
                .addProp(createActionProp(V_1_3, TableFeaturePropType.WRITE_ACTIONS, actionTypeSet))
                .addProp(createActionProp(V_1_3, APPLY_ACTIONS, actionTypeSet));

        Map<OxmBasicFieldType, Boolean> map = new HashMap<>();
        for (OxmBasicFieldType t : EnumSet.allOf(OxmBasicFieldType.class)) {
            map.put(t, false);
        }

        tf.addProp(createOxmProp(V_1_3, TableFeaturePropType.MATCH, map))
                .addProp(createOxmProp(V_1_3, TableFeaturePropType.WILDCARDS, map))
                .addProp(createOxmProp(V_1_3, TableFeaturePropType.WRITE_SETFIELD, map))
                .addProp(createOxmProp(V_1_3, TableFeaturePropType.APPLY_SETFIELD, map));
        return (MBodyTableFeatures) tf.toImmutable();
    }

    private OpenflowMessage replyMsgWithMiss() throws IncompleteStructureException {
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY, TABLE_FEATURES);

        MBodyTableFeatures.MutableArray array =
                (MBodyTableFeatures.MutableArray)
                        MpBodyFactory.createReplyBody(V_1_3, TABLE_FEATURES);
        array.addTableFeatures(createTableWithMiss(TID_0, TABLE_O_NEXT_IDS));
        array.addTableFeatures(createTableWithMiss(TID_1, TABLE_1_NEXT_IDS));
        array.addTableFeatures(createTableWithMiss(TID_2, new TreeSet<TableId>()));
        reply.body((MultipartBody)array.toImmutable());
        return reply.toImmutable();
    }

    private MBodyTableFeatures createTableWithMiss(TableId id, Set<TableId> nextTableIds) {
        MBodyMutableTableFeatures tf = (MBodyMutableTableFeatures)
                MpBodyFactory.createReplyBodyElement(V_1_3, TABLE_FEATURES);
        tf.tableId(id).name("Flow Table: " + id.toString())
                .metadataMatch(MBodyTableFeatures.ALL_META_BITS)
                .metadataWrite(MBodyTableFeatures.ALL_META_BITS)
                .maxEntries(16777216);
        tf.addProp(createInstrProp(V_1_3, INSTRUCTIONS, SUPP_INSTR))
                .addProp(createInstrProp(V_1_3, INSTRUCTIONS_MISS, SUPP_INSTR_MISS))
                .addProp(createNextTablesProp(V_1_3, TableFeaturePropType.NEXT_TABLES, nextTableIds))
                .addProp(createNextTablesProp(V_1_3, TableFeaturePropType.NEXT_TABLES_MISS, Collections.< TableId >emptySet()))
                .addProp(createActionProp(V_1_3, TableFeaturePropType.WRITE_ACTIONS, SUPP_ACTION))
                .addProp(createActionProp(V_1_3, WRITE_ACTIONS_MISS, SUPP_ACTION_MISS))
                .addProp(createActionProp(V_1_3, APPLY_ACTIONS, SUPP_ACTION));

        Map<OxmBasicFieldType, Boolean> map = new HashMap<>();
        for (OxmBasicFieldType t : EnumSet.allOf(OxmBasicFieldType.class)) {
            map.put(t, false);
        }

        tf.addProp(createOxmProp(V_1_3, TableFeaturePropType.MATCH, map))
                .addProp(createOxmProp(V_1_3, TableFeaturePropType.WILDCARDS, map))
                .addProp(createOxmProp(V_1_3, TableFeaturePropType.WRITE_SETFIELD, SUPP_MATCH_CAPS))
                .addProp(createOxmProp(V_1_3, TableFeaturePropType.WRITE_SETFIELD_MISS, SUPP_MATCH_MISS_CAPS))
                .addProp(createOxmProp(V_1_3, TableFeaturePropType.APPLY_SETFIELD, SUPP_MATCH_CAPS))
                .addProp(createOxmProp(V_1_3, TableFeaturePropType.APPLY_SETFIELD_MISS, SUPP_MATCH_MISS_CAPS));
        return (MBodyTableFeatures) tf.toImmutable();
    }

    private OpenflowMessage replyMsgUnsupportedMiss() throws IncompleteStructureException {
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY, TABLE_FEATURES);

        MBodyTableFeatures.MutableArray array =
                (MBodyTableFeatures.MutableArray)
                        MpBodyFactory.createReplyBody(V_1_3, TABLE_FEATURES);
        array.addTableFeatures(createTableWithNoMissSupport(TID_0, TABLE_O_NEXT_IDS));
        array.addTableFeatures(createTableWithNoMissSupport(TID_1, TABLE_1_NEXT_IDS));
        array.addTableFeatures(createTableWithNoMissSupport(TID_2, new TreeSet<TableId>()));
        reply.body((MultipartBody)array.toImmutable());
        return reply.toImmutable();
    }

    private MBodyTableFeatures createTableWithNoMissSupport(TableId id, Set<TableId> nextTableIds) {
        MBodyMutableTableFeatures tf = (MBodyMutableTableFeatures)
                MpBodyFactory.createReplyBodyElement(V_1_3, TABLE_FEATURES);
        tf.tableId(id).name("Flow Table: " + id.toString())
                .metadataMatch(MBodyTableFeatures.ALL_META_BITS)
                .metadataWrite(MBodyTableFeatures.ALL_META_BITS)
                .maxEntries(16777216);
        tf.addProp(createInstrProp(V_1_3, INSTRUCTIONS, SUPP_INSTR))
                .addProp(createInstrProp(V_1_3, INSTRUCTIONS_MISS, EMPTY_INSTR))
                .addProp(createNextTablesProp(V_1_3, TableFeaturePropType.NEXT_TABLES, nextTableIds))
                .addProp(createNextTablesProp(V_1_3, TableFeaturePropType.NEXT_TABLES_MISS, nextTableIds))
                .addProp(createActionProp(V_1_3, TableFeaturePropType.WRITE_ACTIONS, EMPTY_ACTION))
                .addProp(createActionProp(V_1_3, WRITE_ACTIONS_MISS, EMPTY_ACTION))
                .addProp(createActionProp(V_1_3, APPLY_ACTIONS, SUPP_ACTION))
                .addProp(createActionProp(V_1_3, APPLY_ACTIONS_MISS, EMPTY_ACTION));

        Map<OxmBasicFieldType, Boolean> map = new HashMap<>();
        for (OxmBasicFieldType t : EnumSet.allOf(OxmBasicFieldType.class)) {
            map.put(t, false);
        }

        tf.addProp(createOxmProp(V_1_3, TableFeaturePropType.MATCH, map))
                .addProp(createOxmProp(V_1_3, TableFeaturePropType.WILDCARDS, map))
                .addProp(createOxmProp(V_1_3, TableFeaturePropType.WRITE_SETFIELD, SUPP_MATCH_CAPS))
                .addProp(createOxmProp(V_1_3, TableFeaturePropType.WRITE_SETFIELD_MISS, EMPTY_MATCH_CAPS))
                .addProp(createOxmProp(V_1_3, TableFeaturePropType.APPLY_SETFIELD, SUPP_MATCH_CAPS))
                .addProp(createOxmProp(V_1_3, TableFeaturePropType.APPLY_SETFIELD_MISS, EMPTY_MATCH_CAPS));
        return (MBodyTableFeatures) tf.toImmutable();
    }

    private OpenflowMessage replyNoMatch() throws IncompleteStructureException {
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY, TABLE_FEATURES);

        MBodyTableFeatures.MutableArray array =
                (MBodyTableFeatures.MutableArray)
                        MpBodyFactory.createReplyBody(V_1_3, TABLE_FEATURES);
        array.addTableFeatures(createNoMatchTable(TID_0, new TreeSet<TableId>()));
        reply.body((MultipartBody)array.toImmutable());
        return reply.toImmutable();
    }

    private MBodyTableFeatures createNoMatchTable(TableId id, Set<TableId> nextTableIds) {
        MBodyMutableTableFeatures tf = (MBodyMutableTableFeatures)
                MpBodyFactory.createReplyBodyElement(V_1_3, TABLE_FEATURES);
        tf.tableId(id).name("Flow Table: " + id.toString())
                .metadataMatch(MBodyTableFeatures.ALL_META_BITS)
                .metadataWrite(MBodyTableFeatures.ALL_META_BITS)
                .maxEntries(16777216);
        Set<InstructionType> suppInst =
                new HashSet<>(asList(InstructionType.GOTO_TABLE,
                        InstructionType.WRITE_METADATA, InstructionType.WRITE_ACTIONS,
                        InstructionType.APPLY_ACTIONS, InstructionType.CLEAR_ACTIONS,
                        InstructionType.METER));
        Set<ActionType> actionTypeSet = EnumSet.allOf(ActionType.class);
        tf.addProp(createInstrProp(V_1_3, INSTRUCTIONS, suppInst))
                .addProp(createNextTablesProp(V_1_3, TableFeaturePropType.NEXT_TABLES, nextTableIds))
                .addProp(createActionProp(V_1_3, TableFeaturePropType.WRITE_ACTIONS, actionTypeSet))
                .addProp(createActionProp(V_1_3, APPLY_ACTIONS, actionTypeSet));

        Map<OxmBasicFieldType, Boolean> map = new HashMap<>();
        for (OxmBasicFieldType t : EnumSet.allOf(OxmBasicFieldType.class)) {
            map.put(t, false);
        }

        tf.addProp(createOxmProp(V_1_3, TableFeaturePropType.WILDCARDS, map))
                .addProp(createOxmProp(V_1_3, TableFeaturePropType.WRITE_SETFIELD, map))
                .addProp(createOxmProp(V_1_3, TableFeaturePropType.APPLY_SETFIELD, map))
                .addProp(createExperProp(V_1_3, TableFeaturePropType.EXPERIMENTER,
                        ExperimenterId.HP, 10, "Experimenter".getBytes()));
        return (MBodyTableFeatures) tf.toImmutable();
    }

    private OpenflowMessage replyNextIsSelf() throws IncompleteStructureException {
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY, TABLE_FEATURES);

        MBodyTableFeatures.MutableArray array =
                (MBodyTableFeatures.MutableArray)
                        MpBodyFactory.createReplyBody(V_1_3, TABLE_FEATURES);

        array.addTableFeatures(creatNextIsSelfTable(TID_0, new TreeSet<>(asList(TID_0, TID_1, TID_2))));
        array.addTableFeatures(creatNextIsSelfTable(TID_1, new TreeSet<>(asList(TID_1, TID_2))));
        array.addTableFeatures(creatNextIsSelfTable(TID_2, new TreeSet<>(asList(TID_2))));
        reply.body((MultipartBody)array.toImmutable());
        return reply.toImmutable();
    }

    private MBodyTableFeatures creatNextIsSelfTable(TableId id, Set<TableId> nextTableIds) {
        MBodyMutableTableFeatures tf = (MBodyMutableTableFeatures)
                MpBodyFactory.createReplyBodyElement(V_1_3, TABLE_FEATURES);
        tf.tableId(id).name("Flow Table: " + id.toString())
                .metadataMatch(MBodyTableFeatures.ALL_META_BITS)
                .metadataWrite(MBodyTableFeatures.ALL_META_BITS)
                .maxEntries(16777216);
        Set<InstructionType> suppInst =
                new HashSet<>(asList(InstructionType.GOTO_TABLE,
                        InstructionType.WRITE_METADATA, InstructionType.WRITE_ACTIONS,
                        InstructionType.APPLY_ACTIONS, InstructionType.CLEAR_ACTIONS,
                        InstructionType.METER));
        Set<ActionType> actionTypeSet = EnumSet.allOf(ActionType.class);
        tf.addProp(createInstrProp(V_1_3, INSTRUCTIONS, suppInst))
                .addProp(createNextTablesProp(V_1_3, TableFeaturePropType.NEXT_TABLES, nextTableIds))
                .addProp(createActionProp(V_1_3, TableFeaturePropType.WRITE_ACTIONS, actionTypeSet))
                .addProp(createActionProp(V_1_3, APPLY_ACTIONS, actionTypeSet));

        Map<OxmBasicFieldType, Boolean> map = new HashMap<>();
        for (OxmBasicFieldType t : EnumSet.allOf(OxmBasicFieldType.class)) {
            map.put(t, false);
        }

        tf.addProp(createOxmProp(V_1_3, TableFeaturePropType.MATCH, map))
                .addProp(createOxmProp(V_1_3, TableFeaturePropType.WILDCARDS, map))
                .addProp(createOxmProp(V_1_3, TableFeaturePropType.WRITE_SETFIELD, map))
                .addProp(createOxmProp(V_1_3, TableFeaturePropType.APPLY_SETFIELD, map))
                .addProp(createExperProp(V_1_3, TableFeaturePropType.EXPERIMENTER,
                        ExperimenterId.HP, 10, "Experimenter".getBytes()));
        return (MBodyTableFeatures) tf.toImmutable();
    }
}
