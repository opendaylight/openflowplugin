/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.VersionNotSupportedException;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.mp.MBodyGroupFeatures;
import org.opendaylight.of.lib.mp.MBodyMutableGroupFeatures;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.mp.MultipartType.GROUP_FEATURES;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REQUEST;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for the OfmMultipartRequest and OfmMultipartReply messages of
 * type MultipartType.GROUP_FEATURES and related components.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public class OfmMultipartGroupFeaturesTest extends OfmMultipartTest  {

    // Test files...
    private static final String TF_REQ_GF_13 = "v13/mpRequestGroupFeatures";
    private static final String TF_REQ_GF_12 = "v12/statsRequestGroupFeatures";

    private static final String TF_REP_GF_13 = "v13/mpReplyGroupFeatures";
    private static final String TF_REP_GF_12 = "v12/statsReplyGroupFeatures";

    // ====== Expected values
    private static final Set<GroupType> EXP_GRP_TYPES =
            new TreeSet<GroupType>(Arrays.asList(GroupType.ALL,
                    GroupType.SELECT, GroupType.INDIRECT, GroupType.FF));

    private static final Set<GroupCapability> EXP_GRP_CAPS =
       new TreeSet<GroupCapability>(Arrays.asList(GroupCapability.SELECT_WEIGHT,
              GroupCapability.CHAINING));

    private static final long EXP_MAX_GRP_ALL = 1l;
    private static final long EXP_MAX_GRP_SELECT = 2l;
    private static final long EXP_MAX_GRP_INDIRECT = 3l;
    private static final long EXP_MAX_GRP_FF = 4l;

    private static final Set<ActionType> EXP_ACT_GRP_ALL =
            new TreeSet<ActionType>(Arrays.asList(ActionType.OUTPUT,
                    ActionType.SET_MPLS_TTL, ActionType.DEC_MPLS_TTL,
                    ActionType.GROUP));

    private static final Set<ActionType> EXP_ACT_GRP_SEL =
            new TreeSet<ActionType>(Arrays.asList(ActionType.OUTPUT,
                    ActionType.PUSH_VLAN, ActionType.POP_VLAN,
                    ActionType.SET_QUEUE, ActionType.SET_FIELD));

    private static final Set<ActionType> EXP_ACT_GRP_INDIRECT =
            new TreeSet<ActionType>(Arrays.asList(ActionType.COPY_TTL_IN,
                    ActionType.GROUP, ActionType.DEC_NW_TTL,
                    ActionType.PUSH_PBB, ActionType.POP_PBB));

    private static final Set<ActionType> EXP_ACT_GRP_FF;
    static {
        EXP_ACT_GRP_FF =
                new TreeSet<ActionType>(Arrays.asList(ActionType.values()));
        EXP_ACT_GRP_FF.remove(ActionType.EXPERIMENTER);
    }

    // ========================================================= PARSING ====

    @Test
    public void mpRequestGroupFeatures13() {
        print(EOL + "mpRequestGroupFeatures13()");
        OfmMultipartRequest msg =
                (OfmMultipartRequest) verifyMsgHeader(TF_REQ_GF_13,
                        V_1_3, MULTIPART_REQUEST, 16);
        verifyMpHeader(msg, GROUP_FEATURES);
    }

    @Test
    public void statsRequestGroupFeatures12() {
        print(EOL + "statsRequestGroupFeatures12()");
        verifyNotSupported(TF_REQ_GF_12);
//        OfmMultipartRequest msg =
//                (OfmMultipartRequest) verifyMsgHeader(TF_REQ_GF_12,
//                        V_1_2, MULTIPART_REQUEST, 16);
//        verifyMpHeader(msg, GROUP_FEATURES);
    }

    @Test
    public void mpReplyGroupFeatures13() {
        print(EOL + "mpReplyGroupFeatures13()");
        OfmMultipartReply rep =
                (OfmMultipartReply) verifyMsgHeader(TF_REP_GF_13, V_1_3,
                        MULTIPART_REPLY, 56);

        MBodyGroupFeatures body =
                (MBodyGroupFeatures) verifyMpHeader(rep, GROUP_FEATURES);
        assertEquals(AM_NEQ, EXP_GRP_TYPES, body.getTypes());
        assertEquals(AM_NEQ, EXP_GRP_CAPS, body.getCapabilities());
        assertEquals(AM_NEQ, EXP_MAX_GRP_ALL,
                body.getMaxGroupsForType(GroupType.ALL));
        assertEquals(AM_NEQ, EXP_MAX_GRP_SELECT,
                body.getMaxGroupsForType(GroupType.SELECT));
        assertEquals(AM_NEQ, EXP_MAX_GRP_INDIRECT,
                body.getMaxGroupsForType(GroupType.INDIRECT));
        assertEquals(AM_NEQ, EXP_MAX_GRP_FF,
                body.getMaxGroupsForType(GroupType.FF));

        assertEquals(AM_NEQ, EXP_ACT_GRP_ALL,
                body.getActionsForType(GroupType.ALL));
        assertEquals(AM_NEQ, EXP_ACT_GRP_SEL,
                body.getActionsForType(GroupType.SELECT));
        assertEquals(AM_NEQ, EXP_ACT_GRP_INDIRECT,
                body.getActionsForType(GroupType.INDIRECT));
        assertEquals(AM_NEQ, EXP_ACT_GRP_FF,
                body.getActionsForType(GroupType.FF));
    }

    @Test
    public void mpReplyGroupFeatures12() {
        print(EOL + "mpReplyGroupFeatures12()");
        verifyNotSupported(TF_REP_GF_12);
    }

    // NOTE: Multipart GROUP_FEATURES not defined in 1.0, 1,1

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeMpRequestGroupFeatures13() {
        print(EOL + "encodeMpRequestGroupFeatures13()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST, GROUP_FEATURES);
        req.clearXid();
        // now encode and verify
        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_GF_13);
    }

    @Test
    public void encodeMpReplyGroupFeatures13() {
        print(EOL + "encodeMpReplyGroupFeatures13()");
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY, GROUP_FEATURES);
        rep.clearXid();

        MBodyMutableGroupFeatures gf =
                (MBodyMutableGroupFeatures) rep.getBody();
        gf.groupTypes(EXP_GRP_TYPES).capabilities(EXP_GRP_CAPS);

        gf.maxGroupsForType(GroupType.ALL, EXP_MAX_GRP_ALL)
                .maxGroupsForType(GroupType.SELECT, EXP_MAX_GRP_SELECT)
                .maxGroupsForType(GroupType.INDIRECT, EXP_MAX_GRP_INDIRECT)
                .maxGroupsForType(GroupType.FF, EXP_MAX_GRP_FF);
        gf.actionsForType(GroupType.ALL, EXP_ACT_GRP_ALL)
                .actionsForType(GroupType.SELECT, EXP_ACT_GRP_SEL)
                .actionsForType(GroupType.INDIRECT, EXP_ACT_GRP_INDIRECT)
                .actionsForType(GroupType.FF, EXP_ACT_GRP_FF);

        // now encode and verify
        encodeAndVerifyMessage(rep.toImmutable(), TF_REP_GF_13);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeStatsRequestGroupFeatures12() {
        MessageFactory.create(V_1_2, MULTIPART_REQUEST, GROUP_FEATURES);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeStatsReplyGroupFeatures12() {
        MessageFactory.create(V_1_2, MULTIPART_REPLY, GROUP_FEATURES);
    }

    // NOTE: Multipart Group Features not defined in 1.0, 1,1

    @Test(expected = VersionNotSupportedException.class)
    public void encodeMpRequestGroupFeatures11() {
        MessageFactory.create(V_1_1, MULTIPART_REQUEST, GROUP_FEATURES);
    }

    @Test(expected = VersionMismatchException.class)
    public void encodeMpRequestGroupFeatures10() {
        MessageFactory.create(V_1_0, MULTIPART_REQUEST, GROUP_FEATURES);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeMpReplyGroupFeatures11() {
        MessageFactory.create(V_1_1, MULTIPART_REPLY, GROUP_FEATURES);
    }

    @Test(expected = VersionMismatchException.class)
    public void encodeMpReplyGroupFeatures10() {
        MessageFactory.create(V_1_0, MULTIPART_REPLY, GROUP_FEATURES);
    }

    // ==================================================== CHECK BOUNDS ====

    private static final long U32_PLUS_ONE = 0xffffffffL + 1;

    @Test(expected = IllegalArgumentException.class)
    public void maxGroupsTypeTooSmall() {
        MBodyMutableGroupFeatures gf = new MBodyMutableGroupFeatures(V_1_3);
        gf.maxGroupsForType(GroupType.ALL, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void maxGroupsTypeTooBig() {
        MBodyMutableGroupFeatures gf = new MBodyMutableGroupFeatures(V_1_3);
        gf.maxGroupsForType(GroupType.SELECT, U32_PLUS_ONE);
    }

}
