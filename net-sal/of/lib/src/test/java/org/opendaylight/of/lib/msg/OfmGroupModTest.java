/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.of.lib.VersionNotSupportedException;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_2;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.of.lib.instr.ActionFactory.createActionSetField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.ETH_DST;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.IPV4_DST;
import static org.opendaylight.of.lib.msg.BucketFactory.createMutableBucket;
import static org.opendaylight.of.lib.msg.MessageType.GROUP_MOD;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test for the OfmGroupMod message.
 *
 * @author Simon Hunt
 */
public class OfmGroupModTest extends OfmTest {

    // Test files...
    private static final String TF_GM_13 = "v13/groupMod";
    private static final String TF_GM_12 = "v12/groupMod";
    private static final String TF_GM_11 = "v11/groupMod";


    private static final int EXP_GROUP_MOD_MSG_LEN = 88;

    private static final GroupModCommand EXP_CMD = GroupModCommand.MODIFY;
    private static final GroupType EXP_TYPE = GroupType.FF;
    private static final GroupId EXP_ID = gid(2012);

    private static final int EXP_NUM_BUCKETS = 2;

    private static final BigPortNumber EXP_W_PORT = bpn(99);
    private static final GroupId EXP_W_GROUP = gid(2013);

    private static final int EXP_B1_LEN = 24;
    private static final int EXP_B1_WEIGHT = 8;
    private static final int EXP_B1_NUM_ACTIONS = 1;

    private static final int EXP_B2_LEN = 48;
    private static final int EXP_B2_WEIGHT = 1;
    private static final int EXP_B2_NUM_ACTIONS = 2;

    private static final MacAddress MAC = mac("00001e:453411");
    private static final IpAddress IP = ip("15.254.17.1");


    // ========================================================= PARSING ====

    private void verifyBucketHeader(Bucket bkt, int expLen, int expWgt,
                                    BigPortNumber expPort, GroupId expGroup,
                                    int expNumAct) {
        assertEquals(AM_NEQ, expLen, bkt.length);   // no getter
        assertEquals(AM_NEQ, expWgt, bkt.getWeight());
        assertEquals(AM_NEQ, expPort, bkt.getWatchPort());
        assertEquals(AM_NEQ, expGroup, bkt.getWatchGroup());
        assertEquals(AM_UXS, expNumAct, bkt.getActions().size());
    }

    @Test
    public void groupMod13() {
        print(EOL + "groupMod13()");
        OfmGroupMod msg = (OfmGroupMod) verifyMsgHeader(TF_GM_13, V_1_3,
                GROUP_MOD, EXP_GROUP_MOD_MSG_LEN);

        GroupModCommand gmc = msg.getCommand();
        GroupType gtype = msg.getGroupType();
        GroupId groupId = msg.getGroupId();
        List<Bucket> buckets = msg.getBuckets();

        assertEquals(AM_NEQ, EXP_CMD, gmc);
        assertEquals(AM_NEQ, EXP_TYPE, gtype);
        assertEquals(AM_NEQ, EXP_ID, groupId);
        assertEquals(AM_UXS, EXP_NUM_BUCKETS, buckets.size());

        Iterator<Bucket> bIter = buckets.iterator();

        // Bucket 0
        Bucket bkt = bIter.next();
        verifyBucketHeader(bkt, EXP_B1_LEN, EXP_B1_WEIGHT,
                EXP_W_PORT, EXP_W_GROUP, EXP_B1_NUM_ACTIONS);
        Iterator<Action> aIter = bkt.getActions().iterator();
        verifyAction(aIter.next(), ActionType.DEC_NW_TTL);
        assertFalse(AM_HUH, aIter.hasNext());

        // Bucket 1
        bkt = bIter.next();
        verifyBucketHeader(bkt, EXP_B2_LEN, EXP_B2_WEIGHT,
                EXP_W_PORT, EXP_W_GROUP, EXP_B2_NUM_ACTIONS);
        aIter = bkt.getActions().iterator();
        verifyActionSetField(aIter.next(), ETH_DST, MAC);
        verifyActionSetField(aIter.next(), IPV4_DST, IP);
        assertFalse(AM_HUH, aIter.hasNext());
    }




    @Test
    public void groupMod12() {
        print(EOL + "groupMod12()");
        verifyNotSupported(TF_GM_12);

//        OfmGroupMod msg = (OfmGroupMod) verifyMsgHeader(TF_GM_12, V_1_2,
//                GROUP_MOD, EXP_GROUP_MOD_MSG_LEN);
//
//        GroupModCommand gmc = msg.getCommand();
//        GroupType gtype = msg.getGroupType();
//        GroupId groupId = msg.getGroupId();
//        List<Bucket> buckets = msg.getBuckets();
//
//        assertEquals(AM_NEQ, EXP_CMD, gmc);
//        assertEquals(AM_NEQ, EXP_TYPE, gtype);
//        assertEquals(AM_NEQ, EXP_ID, groupId);
//        assertEquals(AM_UXS, EXP_NUM_BUCKETS, buckets.size());
//
//        Iterator<Bucket> bIter = buckets.iterator();
//
//        // Bucket 0
//        Bucket bkt = bIter.next();
//        verifyBucketHeader(bkt, EXP_B1_LEN, EXP_B1_WEIGHT,
//                EXP_W_PORT, EXP_W_GROUP, EXP_B1_NUM_ACTIONS);
//        Iterator<Action> aIter = bkt.getActions().iterator();
//        verifyAction(aIter.next(), ActionType.DEC_NW_TTL);
//        assertFalse(AM_HUH, aIter.hasNext());
//
//        // Bucket 1
//        bkt = bIter.next();
//        verifyBucketHeader(bkt, EXP_B2_LEN, EXP_B2_WEIGHT,
//                EXP_W_PORT, EXP_W_GROUP, EXP_B2_NUM_ACTIONS);
//        aIter = bkt.getActions().iterator();
//        verifyActionSetField(aIter.next(), ETH_DST, MAC);
//        verifyActionSetField(aIter.next(), IPV4_DST, IP);
//        assertFalse(AM_HUH, aIter.hasNext());
    }

    @Ignore
    public void groupMod11() {
        print(EOL + "groupMod11()");
        verifyNotSupported(TF_GM_11);

//        OfmGroupMod msg = (OfmGroupMod) verifyMsgHeader(TF_GM_11, V_1_1,
//                GROUP_MOD, EXP_GROUP_MOD_MSG_LEN);
//
//        GroupModCommand gmc = msg.getCommand();
//        GroupType gtype = msg.getGroupType();
//        GroupId groupId = msg.getGroupId();
//        List<Bucket> buckets = msg.getBuckets();
//
//        assertEquals(AM_NEQ, EXP_CMD, gmc);
//        assertEquals(AM_NEQ, EXP_TYPE, gtype);
//        assertEquals(AM_NEQ, EXP_ID, groupId);
//        assertEquals(AM_UXS, EXP_NUM_BUCKETS, buckets.size());
//
//        Iterator<Bucket> bIter = buckets.iterator();
//
//        // Bucket 0
//        Bucket bkt = bIter.next();
//        verifyBucketHeader(bkt, EXP_B1_LEN, EXP_B1_WEIGHT,
//                EXP_W_PORT, EXP_W_GROUP, EXP_B1_NUM_ACTIONS);
//        Iterator<Action> aIter = bkt.getActions().iterator();
//        verifyAction(aIter.next(), ActionType.DEC_NW_TTL);
//        assertFalse(AM_HUH, aIter.hasNext());
//
//        // Bucket 1
//        bkt = bIter.next();
//        verifyBucketHeader(bkt, EXP_B2_LEN, EXP_B2_WEIGHT,
//                EXP_W_PORT, EXP_W_GROUP, EXP_B2_NUM_ACTIONS);
//        aIter = bkt.getActions().iterator();
//        verifyActionSetField(aIter.next(), ETH_DST, MAC);
//        verifyActionSetField(aIter.next(), IPV4_DST, IP);
//        assertFalse(AM_HUH, aIter.hasNext());
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeGroupMod13() {
        print(EOL + "encodeGroupMod13()");
        OfmMutableGroupMod mod = (OfmMutableGroupMod)
                MessageFactory.create(V_1_3, GROUP_MOD);
        mod.clearXid();
        verifyMutableHeader(mod, V_1_3, GROUP_MOD, 0);

        mod.command(EXP_CMD).groupType(EXP_TYPE).groupId(EXP_ID);

        // add the first bucket
        MutableBucket bkt = createMutableBucket(V_1_3);
        bkt.weight(EXP_B1_WEIGHT).watchPort(EXP_W_PORT).watchGroup(EXP_W_GROUP)
                .addAction(createAction(V_1_3, ActionType.DEC_NW_TTL));
        mod.addBucket((Bucket) bkt.toImmutable());

        // add the second bucket
        bkt = createMutableBucket(V_1_3);
        bkt.weight(EXP_B2_WEIGHT).watchPort(EXP_W_PORT).watchGroup(EXP_W_GROUP)
                .addAction(createActionSetField(V_1_3, ETH_DST, MAC))
                .addAction(createActionSetField(V_1_3, IPV4_DST, IP));
        mod.addBucket((Bucket) bkt.toImmutable());

        // now encode and verify
        encodeAndVerifyMessage(mod.toImmutable(), TF_GM_13);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeGroupMod12() {
        MessageFactory.create(V_1_2, GROUP_MOD);
    }

    @Test
    public void createWithCommand() {
        print(EOL + "createWithCommand()");
        OfmMutableGroupMod m = (OfmMutableGroupMod)
                MessageFactory.create(V_1_3, GROUP_MOD, GroupModCommand.ADD);
        m.clearXid();
        verifyMutableHeader(m, V_1_3, GROUP_MOD, 0);
        assertEquals(AM_NEQ, GroupModCommand.ADD, m.getCommand());
    }

}
