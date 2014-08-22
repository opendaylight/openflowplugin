/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.IncompleteMessageException;
import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.err.*;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.instr.ActionFactory;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.mp.*;
import org.opendaylight.of.lib.mp.MBodyMeterStats.MeterBandStats;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.TimeUtils;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.VlanId;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import static org.opendaylight.of.controller.DatatypeUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.of.lib.instr.ActionFactory.createActionSetField;
import static org.opendaylight.of.lib.instr.ActionType.DEC_NW_TTL;
import static org.opendaylight.of.lib.instr.ActionType.OUTPUT;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.MatchFactory.createMatch;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.of.lib.mp.MpBodyFactory.createReplyBodyElement;
import static org.opendaylight.of.lib.mp.MultipartType.*;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.of.lib.msg.MeterBandType.DROP;
import static org.opendaylight.of.lib.msg.MeterBandType.DSCP_REMARK;
import static org.opendaylight.of.lib.msg.MeterFlag.*;

/**
 * A mock switch that is tailored for flow related interactions.
 *
 * @author Radhika Hegde
 * @author Simon Hunt
 */
// TODO: this should be split into FlowSwitch, GroupSwitch, MeterSwitch
// Refactoring to have a base superclass with common constants, etc.
public class FlowSwitch extends BasicSwitch {

    private static final String E_EXP = "Expect<";
    private static final String E_EXP2 = ">: ";
    private static final String E_UNEX_MSG = "Unexpected msg from controller: ";
    private static final String E_PROG_ERR = "Fix programming error!";
    private static final String E_WRONG_FLOW = "Flow is incomplete!";

    private static final long BASE_PACKETS = 1000;
    private static final long DELTA_PACKETS = 20;
    private static final long BASE_BYTES = 3300;
    private static final long DELTA_BYTES = 70;

    private static final MacAddress SRC_MAC = mac("112233:445566");
    private static final long DUR_S = 145;
    private static final long DUR_NS = 20000;
    private static final int PRI = 4;
    private static final int IDLE_TO = 60;
    private static final int HARD_TO = 60;
    private static final long COOKIE = 0x1234;
    private static final VlanId VLAN_ID = VlanId.valueOf(100);
    private static final IpAddress DST_IP = ip("1.1.1.1");

    private static final Match MATCH_10 = (Match) createMatch(V_1_0)
            .addField(createBasicField(V_1_0, ETH_SRC, SRC_MAC)).toImmutable();
    private static final Match MATCH_13 = (Match) createMatch(V_1_3)
            .addField(createBasicField(V_1_3, ETH_SRC, SRC_MAC)).toImmutable();

    private static final GroupType GRP_TYPE = GroupType.ALL;
    private static final int GRP_B_WT = 1;
    private static final GroupId GRP_W_GID = gid(20);
    private static final BigPortNumber GRP_W_PORT = BigPortNumber.valueOf(99);
    private static final BigPortNumber OUT_PORT = BigPortNumber.valueOf(100);

    private static final long GRP_RF_COUNT = 8;
    private static final long GRP_PKTS = 1011;
    private static final long GRP_BYTES = 9999;
    private static final long GRP_DUR = 25;
    private static final long GRP_DUR_N = 430;
    private static final long BKT_PKTS = 500;
    private static final long BKT_BYTES = 1500;

    private static final MeterFlag[] M_FLAGS = {KBPS, BURST, STATS};
    private static final Set<MeterFlag> M_FLAGS_SET =
            new TreeSet<>(Arrays.asList(M_FLAGS));
    private static final int BURST_SIZE = 1000;
    private static final int RATE_BAND1 = 1500;
    private static final int RATE_BAND2 = 100;
    private static final int PRECEDENCE = 1;
    // Constants for Meter Statistics 1.3.
    private static final long PKTBAND_CNT = 10;
    private static final long BYTEBAND_CNT = 10;
    private static final long PACKETIN_COUNT = 100;
    private static final long FLOW_COUNT = 1000;
    private static final long BYTEIN_COUNT = 10000;

    // Constants for MBodyExperimenter.
    private static final short CMD = 0;
    private static final short PAD = 0;
    private static final int ID = 0;
    private static final int SUB_TYPE = 0;
    private static final int KBPS_FLAG = 0x1;
    private static final int SET_DROP_FLAG = 0x4;
    private static final int CAPABILITY_FLAG = KBPS_FLAG | SET_DROP_FLAG;
    private static final int DROP_RATE = 1500;
    private static final int MARK_RATE = 0;
    private static final int MINIMUM_LENGTH = 28;
    private static final int M_EXP_LEN = 4;
    private static final byte[] M_EXP_BODY = {0x00, 0x00, 0x00, 0x00};
    private static final String E_EXP_INC =
            "Incorrect EXPERIMENTER from controller";


    // Designates the expected response from the switch.
    private Expect expect;

    /**
     * Constructs a mock flow-related interactions switch with the given
     * datapath ID, using the specified definition file for configuration.
     * The expected response from the switch defaults to
     * {@link Expect#MP_REPLY_SINGLE MP_REPLY_SINGLE}, though this can be
     * changed by invoking {@link #expected(Expect)}.
     *
     * @param dpid the datapath ID
     * @param defPath the switch definition file
     * @throws IOException if there was an issue reading switch configuration
     */
    public FlowSwitch(DataPathId dpid, String defPath) throws IOException {
        super(dpid, defPath);
        expect = Expect.MP_REPLY_SINGLE;
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

    @Override
    protected void send(OpenflowMessage msg) {
        log.info("{} Switch send: {}", TimeUtils.getInstance().hhmmssnnn(), msg);
        super.send(msg);
    }
    
    @Override
    protected void msgRx(OpenflowMessage msg) {
        log.info("{} Switch msgRx: {}", TimeUtils.getInstance().hhmmssnnn(), msg);
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
            case GROUP_MOD:
                if(!handleGroupMod((OfmGroupMod) msg))
                    unhandled(msg);
                break;
            case METER_MOD:
                if(!handleMeterMod((OfmMeterMod) msg))
                    unhandled(msg);
                break;
            case BARRIER_REQUEST:
                if(!handleBarrierRequest((OfmBarrierRequest) msg))
                    unhandled(msg);
                break;
            default:
                unhandled(msg);
                break;
        }
    }

 // ============================================================

    private boolean handleFlowMod(OfmFlowMod msg)
                                     throws IncompleteMessageException {
        switch (expect) {
            case NO_BARRIER_BUT_ERROR:
                send(createOfError(msg, ErrorType.FLOW_MOD_FAILED,
                        ECodeFlowModFailed.BAD_COMMAND));
                return true;
            case BARRIER_SUCCESS:
                msg.validate();
                // do nothing -- switch does not reply for successful FlowMod
                return true;
        }
        return false;
    }

    private boolean handleGroupMod(OfmGroupMod msg) {
        switch (expect) {
            case NO_BARRIER_BUT_ERROR:
                send(createOfError(msg, ErrorType.GROUP_MOD_FAILED,
                                   ECodeGroupModFailed.BAD_BUCKET));
                return true;
            case BARRIER_SUCCESS:
                // do nothing -- switch does not reply for successful GroupMod
                return true;
        }
        return false;
    }


    private boolean handleMeterMod(OfmMeterMod msg) {
        switch (expect) {
            case NO_BARRIER_BUT_ERROR:
                send(createOfError(msg, ErrorType.METER_MOD_FAILED,
                                   ECodeMeterModFailed.INVALID_METER));
                return true;
            case BARRIER_SUCCESS:
                // do nothing -- switch does not reply for successful MeterMod
                return true;
        }
        return false;
    }

    private boolean handleBarrierRequest(OfmBarrierRequest msg) {
        send(createOfBarrierRep(msg));
        return true;
    }

    // ==========================================================

    private boolean handleMpRequest(OfmMultipartRequest request) {
        switch (request.getMultipartType()) {
            case FLOW:
                return handleMpFlow(request);
            case GROUP:
                return handleMpGroupStats(request);
            case GROUP_DESC:
                return handleMpGroupDesc(request);
            case GROUP_FEATURES:
                return handleMpGroupFeatures(request);
            case METER:
                return handleMpMeterStats(request);
            case METER_CONFIG:
                return handleMpMeterConfig(request);
            case METER_FEATURES:
                return handleMpMeterFeatures(request);
            case EXPERIMENTER:
                return handleMpExperimenter(request);
            case TABLE_FEATURES:
                return true; // Ignore pipeline manager request
        }
        // message was not handled
        return false;
    }

    // ===========================================================

    private boolean handleMpFlow(OfmMultipartRequest req) {
        MBodyFlowStatsRequest fsr = (MBodyFlowStatsRequest) req.getBody();
        // we'll use table ID and out port to semi-parameterize the response
        switch (expect) {
            case MP_REPLY_SINGLE:
                sendFlowStatsSingle(req, fsr.getTableId(), fsr.getOutPort());
                return true;

            case MP_REPLY_MULTIPLE:
                sendFlowStatsMultiple(req, fsr.getTableId(), fsr.getOutPort());
                return true;

            case MP_REPLY_SINGLE_LIST:
                sendFlowStatsSingleList(req, fsr.getTableId(), fsr.getOutPort());
                return true;

            case MP_REPLY_MULTIPLE_LIST:
                sendFlowStatsMultiList(req, fsr.getTableId(), fsr.getOutPort());
                return true;

        }
        return false;
    }

    private void sendFlowStatsSingleList(OfmMultipartRequest req,
                                         TableId tableId,
                                         BigPortNumber outPort) {
        try {
            send(createMpFlowReply(req, tableId, outPort, 3, 0, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }
    }

    private void sendFlowStatsSingle(OfmMultipartRequest req, TableId tableId,
                                     BigPortNumber outPort) {
        try {
            send(createMpFlowReply(req, tableId, outPort, 1, 0, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }
    }

    private void sendFlowStatsMultiple(OfmMultipartRequest req, TableId tableId,
                                       BigPortNumber outPort) {
        try {
            send(createMpFlowReply(req, tableId, outPort, 1, 0, true));
            send(createMpFlowReply(req, tableId, outPort, 1, 1, true));
            send(createMpFlowReply(req, tableId, outPort, 1, 2, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }
    }

    private void sendFlowStatsMultiList(OfmMultipartRequest req, TableId tableId,
                                        BigPortNumber outPort) {
        try {
            send(createMpFlowReply(req, tableId, outPort, 2, 0, true));
            send(createMpFlowReply(req, tableId, outPort, 2, 1, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }
    }

    // ================================================================

    private boolean handleMpGroupDesc(OfmMultipartRequest req) {
       switch (expect) {
            case MP_REPLY_SINGLE:
                sendGroupDescSingle(req);
                return true;
            case MP_REPLY_MULTIPLE:
                sendGroupDescMultiple(req);
                return true;
            case MP_REPLY_SINGLE_LIST:
                sendGroupDescSingleList(req);
                return true;
            case MP_REPLY_MULTIPLE_LIST:
                sendGroupDescMultiList(req);
                return true;
        }
        return false;
    }

    private void sendGroupDescMultiple(OfmMultipartRequest req) {
        try {
            send(createMpGroupDescReply(req, 1, 1, true));
            send(createMpGroupDescReply(req, 2, 1, true));
            send(createMpGroupDescReply(req, 3, 1, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }

    }

    private void sendGroupDescSingleList(OfmMultipartRequest req) {
        try {
            send(createMpGroupDescReply(req, 1, 5, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }
    }

    private void sendGroupDescSingle(OfmMultipartRequest req) {
        try {
            send(createMpGroupDescReply(req, 1, 1, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }

    }

    private void sendGroupDescMultiList(OfmMultipartRequest req) {
        try {
            send(createMpGroupDescReply(req, 1, 2, true));
            send(createMpGroupDescReply(req, 3, 2, true));
            send(createMpGroupDescReply(req, 5, 1, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }

    }

    //================================================================
    private boolean handleMpGroupStats(OfmMultipartRequest req) {
        switch (expect) {
        case MP_REPLY_SINGLE:
            sendGroupStatsSingle(req);
            return true;
        case MP_REPLY_MULTIPLE:
            sendGroupStatsMultiple(req);
            return true;
        case MP_REPLY_SINGLE_LIST:
            sendGroupStatsSingleList(req);
            return true;
        case MP_REPLY_MULTIPLE_LIST:
            sendGroupStatsMultiList(req);
            return true;
    }
    return false;
    }

    private void sendGroupStatsMultiple(OfmMultipartRequest req) {
        try {
            send(createMpGroupStatsReply(req, 1, 1, true));
            send(createMpGroupStatsReply(req, 2, 1, true));
            send(createMpGroupStatsReply(req, 3, 1, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }

    }

    private void sendGroupStatsSingleList(OfmMultipartRequest req) {
        try {
            send(createMpGroupStatsReply(req, 1, 5, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }
    }

    private void sendGroupStatsSingle(OfmMultipartRequest req) {
        try {
            send(createMpGroupStatsReply(req, 1, 1, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }

    }

    private void sendGroupStatsMultiList(OfmMultipartRequest req) {
        try {
            send(createMpGroupStatsReply(req, 1, 2, true));
            send(createMpGroupStatsReply(req, 3, 2, true));
            send(createMpGroupStatsReply(req, 5, 1, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }

    }

    //========================================================================

    private boolean handleMpGroupFeatures(OfmMultipartRequest req) {
        try {
            send(createMpGroupFeaturesReply(req));
        } catch (Exception e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }
        return true;
    }

    private OpenflowMessage createMpGroupFeaturesReply(OfmMultipartRequest req) {
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
                MessageFactory.create(req, MULTIPART_REPLY, GROUP_FEATURES);
        MBodyMutableGroupFeatures gf =
                (MBodyMutableGroupFeatures) reply.getBody();

        gf.groupTypes(GF_TYPES)
                .capabilities(GF_CAPS)
                .maxGroupsForType(GroupType.ALL, GT_ALL_MAX)
                .maxGroupsForType(GroupType.SELECT, GT_SELECT_MAX)
                .maxGroupsForType(GroupType.INDIRECT, GT_INDIRECT_MAX)
                .maxGroupsForType(GroupType.FF, GT_FF_MAX)
                .actionsForType(GroupType.ALL, GT_ALL_ACTIONS)
                .actionsForType(GroupType.SELECT, GT_SELECT_ACTIONS)
                .actionsForType(GroupType.INDIRECT, GT_INDIRECT_ACTIONS)
                .actionsForType(GroupType.FF, GT_FF_ACTIONS);

        return reply.toImmutable();
    }

    private static final Set<GroupType> GF_TYPES = EnumSet.of(
            GroupType.ALL,
            GroupType.SELECT,
            GroupType.INDIRECT,
            GroupType.FF
    );
    private static final Set<GroupCapability> GF_CAPS = EnumSet.of(
            GroupCapability.SELECT_LIVENESS,
            GroupCapability.SELECT_WEIGHT
    );
    private static final long GT_ALL_MAX = 3;
    private static final long GT_SELECT_MAX = 6;
    private static final long GT_INDIRECT_MAX = 8;
    private static final long GT_FF_MAX = 2;

    private static final Set<ActionType> GT_ALL_ACTIONS = EnumSet.of(
            ActionType.COPY_TTL_IN,
            ActionType.COPY_TTL_OUT
    );
    private static final Set<ActionType> GT_SELECT_ACTIONS = EnumSet.of(
            ActionType.PUSH_VLAN,
            ActionType.POP_VLAN
    );
    private static final Set<ActionType> GT_INDIRECT_ACTIONS = EnumSet.of(
            ActionType.SET_MPLS_TTL,
            ActionType.SET_FIELD,
            ActionType.SET_NW_TTL
    );
    private static final Set<ActionType> GT_FF_ACTIONS =
            EnumSet.of(ActionType.DEC_NW_TTL);

    //========================================================================

    private boolean handleMpMeterConfig(OfmMultipartRequest req) {
        switch (expect) {
             case MP_REPLY_SINGLE:
                 sendMeterConfigSingle(req);
                 return true;
             case MP_REPLY_MULTIPLE:
                 sendMeterConfigMultiple(req);
                 return true;
             case MP_REPLY_SINGLE_LIST:
                 sendMeterConfigSingleList(req);
                 return true;
             case MP_REPLY_MULTIPLE_LIST:
                 sendMeterConfigMultiList(req);
                 return true;
         }
         return false;
     }

    private void sendMeterConfigMultiList(OfmMultipartRequest req) {
        try {
            send(createMpMeterConfigReply(req, 1, 2, true));
            send(createMpMeterConfigReply(req, 3, 1, true));
            send(createMpMeterConfigReply(req, 4, 1, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }

    }

    private void sendMeterConfigSingleList(OfmMultipartRequest req) {
        try {
            send(createMpMeterConfigReply(req, 1, 3, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }
    }

    private void sendMeterConfigMultiple(OfmMultipartRequest req) {
        try {
            send(createMpMeterConfigReply(req, 1, 1, true));
            send(createMpMeterConfigReply(req, 2, 1, true));
            send(createMpMeterConfigReply(req, 3, 1, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }
    }

    private void sendMeterConfigSingle(OfmMultipartRequest req) {
        try {
            send(createMpMeterConfigReply(req, 1, 1, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }
    }

    //========================================================================

    private boolean handleMpMeterFeatures(OfmMultipartRequest req) {
        try {
            send(createMpMeterFeaturesReply(req));
        } catch (Exception e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }
        return true;
    }

    private OpenflowMessage createMpMeterFeaturesReply(OfmMultipartRequest req) {
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
                MessageFactory.create(req, MULTIPART_REPLY, METER_FEATURES);
        MBodyMutableMeterFeatures mf =
                (MBodyMutableMeterFeatures) reply.getBody();
        mf.maxMeters(MF_MAX_METERS)
                .bandTypes(MF_BAND_TYPES)
                .capabilities(MF_CAPABILITIES)
                .maxBands(MF_MAX_BANDS)
                .maxColor(MF_MAX_COLOR);

        return reply.toImmutable();
    }

    private static final long MF_MAX_METERS = 36;
    private static final Set<MeterBandType> MF_BAND_TYPES =
            EnumSet.of(DSCP_REMARK, DROP);
    private static final Set<MeterFlag> MF_CAPABILITIES =
            EnumSet.of(BURST, KBPS);
    private static final int MF_MAX_BANDS = 4;
    private static final int MF_MAX_COLOR = 15;

    // =============================================================

    private boolean handleMpMeterStats(OfmMultipartRequest req) {
        switch (expect) {
             case MP_REPLY_SINGLE:
                 sendMeterStatsSingle(req);
                 return true;
             case MP_REPLY_MULTIPLE:
                 sendMeterStatsMultiple(req);
                 return true;
             case MP_REPLY_SINGLE_LIST:
                 sendMeterStatsSingleList(req);
                 return true;
             case MP_REPLY_MULTIPLE_LIST:
                 sendMeterStatsMultiList(req);
                 return true;
         }
         return false;
     }

    private void sendMeterStatsMultiList(OfmMultipartRequest req) {
        try {
            send(createMpMeterStatsReply(req, 1, 2, true));
            send(createMpMeterStatsReply(req, 3, 1, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }

    }

    private void sendMeterStatsSingleList(OfmMultipartRequest req) {
        try {
            send(createMpMeterStatsReply(req, 1, 3, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }
    }

    private void sendMeterStatsMultiple(OfmMultipartRequest req) {
        try {
            send(createMpMeterStatsReply(req, 1, 1, true));
            send(createMpMeterStatsReply(req, 2, 1, true));
            send(createMpMeterStatsReply(req, 3, 1, true));
            send(createMpMeterStatsReply(req, 4, 1, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }
    }

    private void sendMeterStatsSingle(OfmMultipartRequest req) {
        try {
            send(createMpMeterStatsReply(req, 1, 1, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }
    }

    // ===============================================================

    private boolean handleMpExperimenter(OfmMultipartRequest req) {
        switch (expect) {
             case MP_REPLY_SINGLE:
                 sendExperimenterSingle(req);
                 return true;
             case MP_REPLY_MULTIPLE:
                 sendExperimenterMultiple(req);
                 return true;
             case MP_REPLY_SINGLE_LIST:
                 break; // not applicable
             case MP_REPLY_MULTIPLE_LIST:
                 break; // not applicable
         }
         return false;
     }

    private void sendExperimenterMultiple(OfmMultipartRequest req) {
        try {
            send(createMpExperimenterReply(req, true));
            send(createMpExperimenterReply(req, false));
        } catch (IncompleteStructureException e) {
            // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }
    }

    private void sendExperimenterSingle(OfmMultipartRequest req) {
        try {
            send(createMpExperimenterReply(req, false));
        } catch (IncompleteStructureException e) {
         // this is a mock-switch-side programming error
            throw new RuntimeException(E_PROG_ERR, e);
        }
    }

    // ==============================================================

    /** Creates an MP-Reply/EXPERIMENTER message.
    *
    * @param req the request (needed for xid)
    * @param more whether to set the MORE flag or no
    * @return an immutable MP-Reply/EXPERIMENTER message
    * @throws IncompleteStructureException if we made a mistake
    */
    private OpenflowMessage createMpExperimenterReply(OfmMultipartRequest req,
                                                      boolean more)
                       throws IncompleteStructureException {
        if (req.getVersion() == V_1_0)
            validateExperimenter10(req);
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
        MessageFactory.create(req, MULTIPART_REPLY, MultipartType.EXPERIMENTER);
        MBodyMutableExperimenter exper =
                        (MBodyMutableExperimenter) rep.getBody();

        ByteBuffer bb = ByteBuffer.allocate(MINIMUM_LENGTH);
        bb.putInt(SUB_TYPE);
        bb.putInt(ID);
        bb.putInt(CAPABILITY_FLAG);
        bb.putInt(DROP_RATE);
        bb.putInt(MARK_RATE);
        bb.putInt(BURST_SIZE);
        bb.putShort(CMD);
        bb.putShort(PAD);
        exper.expId(ExperimenterId.HP);
        exper.data(bb.array());
        if (more)
            rep.setMoreFlag();
        return rep.toImmutable();
    }


    private void validateExperimenter10(OfmMultipartRequest req)
                             throws IncompleteStructureException {
        MBodyExperimenter body =  (MBodyExperimenter) req.getBody();
        if (body.getExpId() == ExperimenterId.HP &&
                body.getData().length == M_EXP_LEN &&
                        Arrays.equals(body.getData(), M_EXP_BODY))
                return;
        throw new IncompleteStructureException(E_EXP_INC);
    }

    /** Creates an MP-Reply/METER message.
    *
    * @param req the request (needed for xid)
    * @param startMeterId the meter id to return response deterministically
    * @param nElem number of elements to generate in the array
    * @param more whether to set the MORE flag or no
    * @return an immutable MP-Reply/METER message
    * @throws IncompleteStructureException if we made a mistake
    */
    private OpenflowMessage createMpMeterStatsReply(OfmMultipartRequest req,
                                                   int startMeterId,
                                                   int nElem,
                                                   boolean more)
                        throws IncompleteStructureException {
        // reply with the same xid as the request
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
          MessageFactory.create(req, MULTIPART_REPLY, METER);
        MBodyMeterStats.MutableArray array =
                (MBodyMeterStats.MutableArray) reply.getBody();
        ProtocolVersion pv = req.getVersion();

        // ==== create the meter stats objects ====
        for (int i=0; i<nElem; i++) {
            MBodyMutableMeterStats mstats = (MBodyMutableMeterStats)
                    MpBodyFactory.createReplyBodyElement(pv,
                                                         MultipartType.METER);
            MeterBandStats bandStats = new MeterBandStats
                    (PKTBAND_CNT, BYTEBAND_CNT);
            mstats.meterId(mid(startMeterId + i)).flowCount(FLOW_COUNT)
            .byteInCount(BYTEIN_COUNT)
            .duration(DUR_S, DUR_NS)
            .packetInCount(PACKETIN_COUNT * (i+1))
            .addMeterBandStat(bandStats);
            array.addMeterStats((MBodyMeterStats) mstats.toImmutable());
        }
        if (more)
            reply.setMoreFlag();
         return reply.toImmutable();
    }


    /** Creates an MP-Reply/METER_CONFIG message.
    *
    * @param req the request (needed for xid)
    * @param startMeterId the meter id to return response deterministically
    * @param nElem number of elements to generate in the array
    * @param more whether to set the MORE flag or no
    * @return an immutable MP-Reply/METER_CONFIG message
    * @throws IncompleteStructureException if we made a mistake
    */
    private OpenflowMessage createMpMeterConfigReply(OfmMultipartRequest req,
                                                   int startMeterId,
                                                   int nElem,
                                                   boolean more)
                        throws IncompleteStructureException {
        // reply with the same xid as the request
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
          MessageFactory.create(req, MULTIPART_REPLY, METER_CONFIG);
        MBodyMeterConfig.MutableArray array =
                (MBodyMeterConfig.MutableArray) reply.getBody();
        ProtocolVersion pv = req.getVersion();

        // ==== create the meter config objects ====
        for (int i=0; i<nElem; i++) {
            MBodyMutableMeterConfig mc = (MBodyMutableMeterConfig)
                MpBodyFactory.createReplyBodyElement(pv, METER_CONFIG);

            mc.meterId(mid(startMeterId + i)).meterFlags(M_FLAGS_SET)
            .addBand(MeterBandFactory.createBand(pv, DROP,
                                     RATE_BAND1, BURST_SIZE))
            .addBand(MeterBandFactory.createBand(pv, DSCP_REMARK, RATE_BAND2,
                                    BURST_SIZE, PRECEDENCE));
            array.addMeterConfigs((MBodyMeterConfig)mc.toImmutable());
        }
        if (more)
            reply.setMoreFlag();
         return reply.toImmutable();
    }

    /** Creates an MP-Reply/GROUP message.
    *
    * @param req the request (needed for xid)
    * @param startGroupId the group id to return response deterministically
    * @param nElem number of elements to generate in the array
    * @param more whether to set the MORE flag or no
    * @return an immutable MP-Reply/GROUP message
    * @throws IncompleteStructureException if we made a mistake
    */

    private OpenflowMessage createMpGroupStatsReply(OfmMultipartRequest req,
                                                   int startGroupId,
                                                   int nElem,
                                                   boolean more)
                        throws IncompleteStructureException {
        // reply with the same xid as the request
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
          MessageFactory.create(req, MULTIPART_REPLY, GROUP);
        MBodyGroupStats.MutableArray array =
                (MBodyGroupStats.MutableArray) reply.getBody();
        ProtocolVersion pv = req.getVersion();

        // ==== create the group stats objects ====
        for (int i=0; i<nElem; i++) {
            MBodyMutableGroupStats mgs = (MBodyMutableGroupStats) MpBodyFactory
                    .createReplyBodyElement(pv, GROUP);
            mgs.groupId(gid(startGroupId + i))
                    .refCount(GRP_RF_COUNT * (startGroupId + i))
                    .packetCount(GRP_PKTS * (startGroupId + i))
                    .byteCount(GRP_BYTES * (startGroupId + i))
                    .duration(GRP_DUR, GRP_DUR_N);
            for (int j=0; j<i+1; j++) {
                mgs.addBucketStats(BKT_PKTS * (j + 1), BKT_BYTES * (j + 1));
            }
            array.addGroupStats((MBodyGroupStats) mgs.toImmutable());
        }
        if (more)
            reply.setMoreFlag();
         return reply.toImmutable();
    }

    /** Creates an MP-Reply/GROUP_DESC message.
    *
    * @param req the request (needed for xid)
    * @param startGroupId the group id to return response deterministically
    * @param nElem number of elements to generate in the array
    * @param more whether to set the MORE flag or no
    * @return an immutable MP-Reply/GROUP_DESC message
    * @throws IncompleteStructureException if we made a mistake
    */

    private OpenflowMessage createMpGroupDescReply(OfmMultipartRequest req,
                                                   int startGroupId,
                                                   int nElem,
                                                   boolean more)
                        throws IncompleteStructureException {
        // reply with the same xid as the request
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
          MessageFactory.create(req, MULTIPART_REPLY, GROUP_DESC);
        MBodyGroupDescStats.MutableArray array =
                (MBodyGroupDescStats.MutableArray) reply.getBody();
        ProtocolVersion pv = req.getVersion();

        // ==== create the group desc stats objects ====
        for (int i=0; i<nElem; i++) {
            MBodyMutableGroupDescStats mgs = (MBodyMutableGroupDescStats)
                MpBodyFactory.createReplyBodyElement(pv, GROUP_DESC);

            mgs.groupId(gid(startGroupId + i)).groupType(GRP_TYPE);

            // create bucket list
            List<Bucket> bkts = new ArrayList<>();
            // add the list of buckets
            for (int j=0; j<i+1; j++) {
                 MutableBucket bucket = BucketFactory.createMutableBucket(pv);
                 bucket.weight(GRP_B_WT).watchGroup(GRP_W_GID).watchPort(GRP_W_PORT)
                 .addAction(ActionFactory.createAction(pv, OUTPUT, OUT_PORT))
                 .addAction(ActionFactory.createAction(pv, DEC_NW_TTL))
                 .addAction(ActionFactory.createActionSetField(pv, IPV4_DST,
                                                            DST_IP));
                 bkts.add(bucket);
            }
            mgs.buckets(bkts);
            array.addGroupDesc((MBodyGroupDescStats) mgs.toImmutable());
        }
        if (more)
            reply.setMoreFlag();
         return reply.toImmutable();
    }

    /** Creates an MP-Reply/FLOW message.
     *
     * @param req the request (needed for xid)
     * @param tableId requested table id
     * @param outPort requested output port
     * @param nElem number of elements to generate in the array
     * @param delta delta for deterministic data
     * @param more whether to set the MORE flag or no
     * @return an immutable MP-Reply/FLOW message
     * @throws IncompleteStructureException if we made a mistake
     */
    private OpenflowMessage createMpFlowReply(OfmMultipartRequest req,
                                              TableId tableId,
                                              BigPortNumber outPort, int nElem,
                                              int delta, boolean more)
            throws IncompleteStructureException {
        // reply with the same xid as the request
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
                MessageFactory.create(req, MULTIPART_REPLY, FLOW);
        MBodyFlowStats.MutableArray array =
                (MBodyFlowStats.MutableArray) reply.getBody();

        ProtocolVersion pv = req.getVersion();
        int table = 0;
        int port = 1;
        long packets = BASE_PACKETS + DELTA_PACKETS * delta;
        long bytes = BASE_BYTES + DELTA_BYTES * delta;
        MBodyMutableFlowStats mfs;

        for (int i=0; i<nElem; i++) {
            mfs = (MBodyMutableFlowStats) createReplyBodyElement(pv, FLOW);
            TableId t = tableId.equals(TableId.ALL) ? tid(table++) : tableId;
            BigPortNumber bpn = outPort.equals(Port.ANY) ? bpn(port++) : outPort;
            Match m = pv == V_1_0 ? MATCH_10 : MATCH_13;

            mfs.tableId(t).match(m)
                    .duration(DUR_S, DUR_NS).priority(PRI)
                    .idleTimeout(IDLE_TO).hardTimeout(HARD_TO).cookie(COOKIE)
                    .packetCount(packets++).byteCount(bytes++)
                    .actions(createActionList(pv, bpn));
            array.addFlowStats((MBodyFlowStats) mfs.toImmutable());
        }
        if (more)
            reply.setMoreFlag();

        return reply.toImmutable();
    }

    private List<Action> createActionList(ProtocolVersion pv, BigPortNumber bpn) {
        List<Action> actList = new ArrayList<>();
        actList.add(createActionSetField(pv, VLAN_VID, VLAN_ID));
        actList.add(createActionSetField(pv, IPV4_DST, DST_IP));
        actList.add(createAction(pv, ActionType.OUTPUT, bpn));
        return actList;
    }


    private OpenflowMessage createOfBarrierRep(OfmBarrierRequest msg) {
        return MessageFactory.create(msg, BARRIER_REPLY).toImmutable();
    }

    private OpenflowMessage createOfError(OpenflowMessage msg, ErrorType eType,
                                          ErrorCode eCode) {
        OfmMutableError error = (OfmMutableError)
                MessageFactory.create(msg, ERROR, eType);
        error.errorCode(eCode);
        return error.toImmutable();
    }

    //========================================================================

    // One of the switch responses.
    static enum Expect {
        /**
         * A single FLOW stats in a single message.
         */
        MP_REPLY_SINGLE,

        /**
         * A single FLOW stats in multiple messages (using the MORE flag).
         */
        MP_REPLY_MULTIPLE,

        /**
         * An array of FLOW stats in a single message.
         */
        MP_REPLY_SINGLE_LIST,

        /**
         * Arrays of FLOW stats in multiple messages (using the MORE flag).
         */
        MP_REPLY_MULTIPLE_LIST,

        /**
         * Flow push failure.
         */
        NO_BARRIER_BUT_ERROR,

        /**
         * Flow push success.
         */
        BARRIER_SUCCESS,
        ;
    }
}
