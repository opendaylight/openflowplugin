/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.lib.*;
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
import org.opendaylight.util.ByteUtils;

import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static org.junit.Assert.fail;
import static org.opendaylight.of.lib.dt.TableId.tid;
import static org.opendaylight.of.lib.instr.ActionType.*;
import static org.opendaylight.of.lib.instr.InstructionType.*;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.of.lib.mp.MultipartType.TABLE_FEATURES;
import static org.opendaylight.of.lib.msg.MessageType.BARRIER_REPLY;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;
import static org.opendaylight.of.lib.msg.TableFeatureFactory.*;
import static org.opendaylight.of.lib.msg.TableFeaturePropType.*;

/**
 * A mock switch that is tailored for pipeline related interactions.
 *
 * @author Radhika Hegde
 * @author Pramod Shanbhag
 * @author Simon Hunt
 */
public class PipelineSwitch extends BasicSwitch {

    private static final String E_EXP = "Expect<";
    private static final String E_EXP2 = ">: ";
    private static final String E_UNEX_MSG = "Unexpected msg from controller: ";
    private static final String E_PROG_ERR = "Fix programming error!";
    private static final String E_WRONG_FLOW = "Flow is incorrect!";
    private static final String E_FILE_ERR = "Couldn't read test data from: ";
    
    private static final String PATH = "org/opendaylight/of/controller/pipeline/";

    // Designates the expected type of well known pipeline.
    private Expect expect;

    /**
     * Constructs a mock table pipeline related interactions switch with the 
     * given datapath ID, using the specified definition file for configuration.
     *
     * @param dpid the datapath ID
     * @param defPath the switch definition file
     * @throws IOException if there was an issue reading switch configuration
     */
    public PipelineSwitch(DataPathId dpid, String defPath, Expect exp) 
            throws IOException {
        super(dpid, defPath);
        this.expect = exp;
    }

    /**
     * Sets the expected response from the switch.
     *
     * @param e the flavor of response
     */
    public void expected(Expect e) {
        this.expect = e;
    }

    private void unhandled(OpenflowMessage msg) {
        throw new RuntimeException(E_EXP + expect + E_EXP2 + E_UNEX_MSG + msg);
    }

    // NOTE: This override routes MP-Request/TABLE_FEATURES via msgRx
    @Override
    protected boolean sendMpTableFeaturesReply(OfmMultipartRequest request) {
        return false;
    }

    @Override
    protected void msgRx(OpenflowMessage msg) {
        switch (msg.getType()) {
        case MULTIPART_REQUEST:
            if (!handleMpRequest((OfmMultipartRequest) msg))
                unhandled(msg);
            break;
        case FLOW_MOD:
            try {
                if(!handleFlowMod((OfmFlowMod) msg))
                    unhandled(msg);
            } catch (IncompleteMessageException e) {
                throw new RuntimeException(E_WRONG_FLOW);
            }
            break;
        case BARRIER_REQUEST:
            if(!handleBarrierRequest((OfmBarrierRequest) msg))
                unhandled(msg);
            break;
        default:
            break;
        }
    }

    private boolean handleBarrierRequest(OfmBarrierRequest msg) {
        send(createOfBarrierRep(msg));
        return true;
    }

    private OpenflowMessage createOfBarrierRep(OfmBarrierRequest msg) {
        return MessageFactory.create(msg, BARRIER_REPLY).toImmutable();
    }

    private boolean handleFlowMod(OfmFlowMod msg)
            throws IncompleteMessageException {

        msg.validate();
        // do nothing -- switch does not reply for successful FlowMod
        return true;

    }

    private boolean handleMpRequest(OfmMultipartRequest request) {
        switch (request.getMultipartType()) {
            case TABLE_FEATURES:
                return handleMpTables(request);
            default:
                break;
        }
        return false;
    }

    private boolean handleMpTables(OfmMultipartRequest request) {
       switch (expect) {
           case CUSTOM:
           case CUSTOM_1:
               sendTableFeaturesCustom(request);
               return true;
           case THAMES_PE_SW:
               String file = "tableFeatureReplyThames.hex";
               sendTableFeaturesFromHexDump(file, request);
               return true;
           case THAMES_PE_SW_DELAYED:
               file = "tableFeatureReplyThames.hex";
               // induced 1 second delay
               // TODO: understand why we are doing this delay?
               try {
                   Thread.sleep(1000);
               } catch (InterruptedException e){
                   throw new RuntimeException(E_PROG_ERR, e);
               }
               sendTableFeaturesFromHexDump(file, request);
               return true;
           case NO_DEFINITION:
              sendNoDefinition();
              return true;
           case THAMES_IP_PE_SW:
           case COMWARE_EXT:
           case COMWARE_IPMAC_EXT:
            // Simulate two replies
               file = "tableFeatureReplyComware_sw1_t1.hex";
               sendTableFeaturesFromHexDump(file, request);
               file = "tableFeatureReplyComware_sw1_t2.hex";
               sendTableFeaturesFromHexDump(file, request);
               return true;
       }
       return false;
    }

    private void sendNoDefinition() {        
       // don't send any definition; just sleep
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(E_PROG_ERR, e);
        }        
    }

    private void sendTableFeaturesCustom(OfmMultipartRequest req) {
        try {
            switch (expect) {
                case CUSTOM :
                    send(createTableFeaturesCustom(req));
                    break;

                case CUSTOM_1:
                    send(createTableFeaturesCustom1(req));
                    break;
            }

        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }

    }

    private void sendTableFeaturesFromHexDump(String file, OfmMultipartRequest req) {
        try {
            send(createMPTFReplyFromHex(file, req));
        } catch (Exception e) {
            throw new RuntimeException(E_PROG_ERR, e);
        }
    }

    //----------------------- Table features for custom------------------------
    private OpenflowMessage createTableFeaturesCustom(OfmMultipartRequest request)
            throws IncompleteStructureException {
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(request, MULTIPART_REPLY, TABLE_FEATURES);
        ProtocolVersion pv = request.getVersion();
        MBodyTableFeatures.MutableArray array =
                (MBodyTableFeatures.MutableArray)
                MpBodyFactory.createReplyBody(pv, TABLE_FEATURES);
        //add table feature for Policy Engine.
        array.addTableFeatures(createFirstTableFeatures(pv));
        //add table feature for Software Table.
        array.addTableFeatures(createSecondTableFeatures(pv));
        rep.body((MultipartBody) array.toImmutable());

        return rep.toImmutable();
    }


    private  MBodyTableFeatures createFirstTableFeatures(ProtocolVersion pv) {

        MBodyMutableTableFeatures tf = (MBodyMutableTableFeatures)
                MpBodyFactory.createReplyBodyElement(pv, TABLE_FEATURES);
        tf.tableId(TableId.valueOf(0)).name("Table0").metadataMatch(0)
                .metadataWrite(7).maxEntries(1);
        //add INSTRUCTIONS property
        Set<InstructionType> suppInstr = EnumSet.of(GOTO_TABLE);
        Set<TableId> tableIds = new HashSet<>(asList(tid("1")));
        Set<ActionType> acts = new HashSet<>();
        
        tf.addProp(createInstrProp(pv, INSTRUCTIONS, suppInstr))
          .addProp(createInstrProp(pv, INSTRUCTIONS_MISS, suppInstr))
          .addProp(createNextTablesProp(pv, NEXT_TABLES, tableIds))
          .addProp(createNextTablesProp(pv, NEXT_TABLES_MISS, tableIds))
          .addProp(createActionProp(pv, TableFeaturePropType.WRITE_ACTIONS, acts))
          .addProp(createActionProp(pv, WRITE_ACTIONS_MISS, acts))
          .addProp(createActionProp(pv, TableFeaturePropType.APPLY_ACTIONS, acts))
          .addProp(createActionProp(pv, APPLY_ACTIONS_MISS, acts));
        
        //add MATCH property
        Map<OxmBasicFieldType, Boolean> map = new HashMap<>();
        tf.addProp(createOxmProp(pv, MATCH, map))
          .addProp(createOxmProp(pv, WILDCARDS, map))
          .addProp(createOxmProp(pv, WRITE_SETFIELD, map))
          .addProp(createOxmProp(pv, WRITE_SETFIELD_MISS, map))
          .addProp(createOxmProp(pv, APPLY_SETFIELD, map))
          .addProp(createOxmProp(pv, APPLY_SETFIELD_MISS, map));
        
        return (MBodyTableFeatures) tf.toImmutable();
    }

    private MBodyTableFeatures createSecondTableFeatures(ProtocolVersion pv) {
        MBodyMutableTableFeatures tf = (MBodyMutableTableFeatures)
                MpBodyFactory.createReplyBodyElement(pv, TABLE_FEATURES);
        tf.tableId(TableId.valueOf(1)).name("Table1").metadataMatch(0)
                .metadataWrite(7).maxEntries(64000);
        //add INSTRUCTIONS property
        Set<InstructionType> suppInstr = new HashSet<>(
                asList(InstructionType.APPLY_ACTIONS));
        Set<TableId> tableIds = new HashSet<>();
        Set<ActionType> acts = new HashSet<>();
        
        tf.addProp(createInstrProp(pv, INSTRUCTIONS, suppInstr))
          .addProp(createInstrProp(pv, INSTRUCTIONS_MISS, suppInstr))
          .addProp(createNextTablesProp(pv, NEXT_TABLES, tableIds))
          .addProp(createNextTablesProp(pv, NEXT_TABLES_MISS, tableIds))
          .addProp(createActionProp(pv, TableFeaturePropType.WRITE_ACTIONS, acts))
          .addProp(createActionProp(pv, WRITE_ACTIONS_MISS, acts));
        
        //add APPLY_ACTION property
        acts.add(ActionType.OUTPUT);
        tf.addProp(createActionProp(pv, TableFeaturePropType.APPLY_ACTIONS, acts))
          .addProp(createActionProp(pv, APPLY_ACTIONS_MISS, acts));
        
        //add MATCH property
        Map<OxmBasicFieldType, Boolean> map = new HashMap<>();
        tf.addProp(createOxmProp(pv, MATCH, map))
           .addProp(createOxmProp(pv, WILDCARDS, map))
           .addProp(createOxmProp(pv, WRITE_SETFIELD, map))
           .addProp(createOxmProp(pv, WRITE_SETFIELD_MISS, map))
           .addProp(createOxmProp(pv, APPLY_SETFIELD, map))
           .addProp(createOxmProp(pv, APPLY_SETFIELD_MISS, map));
        
        return (MBodyTableFeatures) tf.toImmutable();
    }

  //----------------------- Table features for custom_1------------------------
    private OpenflowMessage createTableFeaturesCustom1(OfmMultipartRequest req)
            throws IncompleteStructureException {
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(req, MULTIPART_REPLY, TABLE_FEATURES);
        ProtocolVersion pv = req.getVersion();
        MBodyTableFeatures.MutableArray array =
                (MBodyTableFeatures.MutableArray)
                MpBodyFactory.createReplyBody(pv, TABLE_FEATURES);
        //add table feature for Policy Engine.
        array.addTableFeatures(createFirstTableFeatures1(pv))
             .addTableFeatures(createSecondTableFeatures1(pv));
        rep.body((MultipartBody) array.toImmutable());
        return rep.toImmutable();
    }


    private  MBodyTableFeatures createFirstTableFeatures1(ProtocolVersion pv) {

        MBodyMutableTableFeatures tf = (MBodyMutableTableFeatures)
                MpBodyFactory.createReplyBodyElement(pv, TABLE_FEATURES);
        tf.tableId(TableId.valueOf(0)).name("Table0").metadataMatch(0)
                .metadataWrite(7).maxEntries(1);
        //add INSTRUCTIONS property
        Set<InstructionType> suppInstr = 
                EnumSet.of(GOTO_TABLE, WRITE_METADATA, 
                        InstructionType.WRITE_ACTIONS,
                        InstructionType.APPLY_ACTIONS, CLEAR_ACTIONS, METER,
                        InstructionType.EXPERIMENTER);
        
        Set<TableId> tableIds = new HashSet<>(asList(tid("1")));
        Set<ActionType> acts = EnumSet.of(OUTPUT, COPY_TTL_OUT, COPY_TTL_IN, 
                SET_MPLS_TTL, DEC_MPLS_TTL, PUSH_VLAN, POP_VLAN, PUSH_MPLS, 
                POP_MPLS, SET_QUEUE, GROUP, SET_NW_TTL, DEC_NW_TTL, 
                SET_FIELD, PUSH_PBB, POP_PBB);
        Set<ActionType> empty = new HashSet<>();
        
        tf.addProp(createInstrProp(pv, INSTRUCTIONS, suppInstr))
          .addProp(createInstrProp(pv, INSTRUCTIONS_MISS, suppInstr))
          .addProp(createNextTablesProp(pv, NEXT_TABLES, tableIds))
          .addProp(createNextTablesProp(pv, NEXT_TABLES_MISS, tableIds))
          .addProp(createActionProp(pv, TableFeaturePropType.WRITE_ACTIONS, acts))
          .addProp(createActionProp(pv, WRITE_ACTIONS_MISS, empty))
          .addProp(createActionProp(pv, TableFeaturePropType.APPLY_ACTIONS, acts))
          .addProp(createActionProp(pv, APPLY_ACTIONS_MISS, empty));
        
        //add MATCH property
        Map<OxmBasicFieldType, Boolean> map = new HashMap<>();
        map.put(IN_PORT, false);
        map.put(IN_PHY_PORT, false);
        map.put(METADATA, true);
        map.put(ETH_DST, true);
        map.put(ETH_SRC, true);
        map.put(ETH_TYPE, false);
        map.put(VLAN_VID, false);
        map.put(VLAN_PCP, false);
        map.put(IP_DSCP, false);
        map.put(IP_ECN, false);
        map.put(IP_PROTO, false);
        map.put(IPV4_SRC, true);
        map.put(IPV4_DST, true);
        map.put(TCP_SRC, false);
        map.put(TCP_DST, false);
        map.put(UDP_SRC, false);
        map.put(UDP_DST, false);
        map.put(SCTP_SRC, false);
        map.put(SCTP_DST, false);
        map.put(ICMPV4_TYPE, false);
        map.put(ICMPV4_CODE, false);
        map.put(ARP_OP, false);
        map.put(ARP_SPA, false);
        map.put(ARP_TPA, false);
        map.put(ARP_SHA, false);
        map.put(ARP_THA, false);
        map.put(IPV6_SRC, false);
        map.put(IPV6_DST, false);
        map.put(IPV6_FLABEL, false);
        map.put(ICMPV6_TYPE, false);
        map.put(ICMPV6_CODE, false);
        map.put(IPV6_ND_TARGET, false);
        map.put(IPV6_ND_SLL, false);
        map.put(IPV6_ND_TLL, false);
        map.put(MPLS_LABEL, false);
        map.put(MPLS_TC, false);
        map.put(MPLS_BOS, false);
        map.put(PBB_ISID, false);
        map.put(TUNNEL_ID, false);
        map.put(IPV6_EXTHDR, false);
        tf.addProp(createOxmProp(pv, MATCH, map))
          .addProp(createOxmProp(pv, WILDCARDS, map))
          .addProp(createOxmProp(pv, WRITE_SETFIELD, map))
          .addProp(createOxmProp(pv, WRITE_SETFIELD_MISS, map))
          .addProp(createOxmProp(pv, APPLY_SETFIELD, map))
          .addProp(createOxmProp(pv, APPLY_SETFIELD_MISS, map));
        
        return (MBodyTableFeatures) tf.toImmutable();
    }

    private MBodyTableFeatures createSecondTableFeatures1(ProtocolVersion pv) {
        MBodyMutableTableFeatures tf = (MBodyMutableTableFeatures)
                MpBodyFactory.createReplyBodyElement(pv, TABLE_FEATURES);
        tf.tableId(TableId.valueOf(1)).name("Table1").metadataMatch(0)
                .metadataWrite(7).maxEntries(64000);
        //add INSTRUCTIONS property
        Set<InstructionType> suppInstr = 
                EnumSet.of(InstructionType.APPLY_ACTIONS);
        Set<TableId> tableIds = new HashSet<>();
        Set<ActionType> acts = new HashSet<>();
        
        tf.addProp(createInstrProp(pv, INSTRUCTIONS, suppInstr))
          .addProp(createInstrProp(pv, INSTRUCTIONS_MISS, suppInstr))
          .addProp(createNextTablesProp(pv, NEXT_TABLES, tableIds))
          .addProp(createNextTablesProp(pv, NEXT_TABLES_MISS, tableIds))
          .addProp(createActionProp(pv, TableFeaturePropType.WRITE_ACTIONS, acts))
          .addProp(createActionProp(pv, WRITE_ACTIONS_MISS, acts));
        
        //add APPLY_ACTION property
        acts.add(ActionType.OUTPUT);
        tf.addProp(createActionProp(pv, TableFeaturePropType.APPLY_ACTIONS, acts))
        .addProp(createActionProp(pv, APPLY_ACTIONS_MISS, acts));
        //add MATCH property
        Map<OxmBasicFieldType, Boolean> map = new HashMap<>();
        tf.addProp(createOxmProp(pv, MATCH, map))
          .addProp(createOxmProp(pv, WILDCARDS, map))
          .addProp(createOxmProp(pv, WRITE_SETFIELD, map))
          .addProp(createOxmProp(pv, WRITE_SETFIELD_MISS, map))
          .addProp(createOxmProp(pv, APPLY_SETFIELD, map))
          .addProp(createOxmProp(pv, APPLY_SETFIELD_MISS, map));

        // add EXPERIMENTER property
        tf.addProp(createExperProp(pv, TableFeaturePropType.EXPERIMENTER,
                ExperimenterId.HP, 10, null));
        return (MBodyTableFeatures) tf.toImmutable();
    }


    //------------- table features reply from hexfile --------------------------
    private OfmMultipartReply createMPTFReplyFromHex(String file, 
                                                     OfmMultipartRequest req)
            throws MessageParseException {
        String filename = PATH + file;
        byte[] packet = null;
        try {
            packet = ByteUtils.slurpBytesFromHexFile(filename,
                                                  getClass().getClassLoader());
            if (packet == null)
                fail(E_FILE_ERR + filename);
 
        } catch (IOException e) {
            fail(E_FILE_ERR + filename);
        }
        OfPacketReader pkt = new OfPacketReader(packet);
        OpenflowMessage msg = MessageFactory.parseMessage(pkt, req);
        return (OfmMultipartReply)msg;
    }
    
    // One of the pipelines known.
    static enum Expect {
        /**
         * Provision Pipeline with one TCAM and one Software Table.
         */
        THAMES_PE_SW,

        /**
         * Provision Pipeline with IP Table (3 TCAMS), PE Table (1 TCAM) and
         * a Software Table.
         */
        THAMES_IP_PE_SW,

        /**
         * Comware Pipeline with Extensibility Table only.
         */
        COMWARE_EXT,

        /**
         * Comware Pipeline with both IP/MAC and Extensibility Tables.
         */
        COMWARE_IPMAC_EXT,

        /**
         * Custom Pipeline definitions. Several of them. 
         */
        CUSTOM, // random one to test align from within FlowTrk2.sendFlowMod().
        CUSTOM_1, // more meaningful table def to test APIs of PipelineManager.
        /**
         * Table_features is sent after 4 seconds, default time_out in PipelineManager is 3 seconds
         */
        THAMES_PE_SW_DELAYED, 
        NO_DEFINITION, // no definition is sent
    }
}
