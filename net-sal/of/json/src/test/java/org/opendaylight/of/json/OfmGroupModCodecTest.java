/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.instr.ActionFactory;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.json.JSON;
import org.opendaylight.util.json.JsonFactory;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.ActionType.DEC_NW_TTL;
import static org.opendaylight.of.lib.instr.ActionType.OUTPUT;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.IPV4_DST;
import static org.opendaylight.util.StringUtils.normalizeEOL;
import static org.opendaylight.util.json.JsonValidator.validate;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link OfmGroupModCodec}.
 *
 * @author Prashant Nayak
 */
public class OfmGroupModCodecTest extends AbstractCodecTest {

    private static final JsonFactory factory = OfJsonFactory.instance();
    private static final OfmGroupModCodec codec = (OfmGroupModCodec)
            factory.codec(OfmGroupMod.class);

    private static final String JSON_GROUP_MOD = "v13/ofmGroupMod";
    private static final String JSON_GROUP_MODS = "v13/ofmGroupMods";

    private static String groupJs = null;

    private static final GroupType EXP_GRP_TYPE = GroupType.ALL;
    private static final GroupId EXP_ID = GroupId.valueOf(1);
    private static final GroupModCommand EXP_COMMAND = GroupModCommand.ADD;
    private static final int EXP_NUM_BUCKET = 2;

    private static final int EXP_B1_WEIGHT = 1;
    private static final GroupId EXP_B1_W_GROUP = GroupId.valueOf(20);
    private static final BigPortNumber EXP_B1_W_PORT =
            BigPortNumber.valueOf(99);
    private static final int EXP_B1_NUM_ACTIONS = 3;

    private static final int EXP_B2_WEIGHT = 1;
    private static final GroupId EXP_B2_W_GROUP = GroupId.valueOf(19);
    private static final BigPortNumber EXP_B2_W_PORT =
            BigPortNumber.valueOf(25);
    private static final BigPortNumber EXP_PORT = BigPortNumber.valueOf(1987);
    private static final IpAddress EXP_IP_ADDRESS =
                                IpAddress.valueOf("15.255.124.141");

    private static final GroupType EXP_GRP_1_TYPE = GroupType.FF;
    private static final GroupId EXP_GRP_1_ID = GroupId.valueOf(2);
    private static final GroupModCommand EXP_GRP_1_COMMAND =
            GroupModCommand.MODIFY;

    private static final int EXP_GRP_1_B1_WEIGHT = 3;
    private static final GroupId EXP_GRP_1_B1_W_GROUP = GroupId.valueOf(10);
    private static final BigPortNumber EXP_GRP_1_B1_W_PORT =
            BigPortNumber.valueOf(90);

    @BeforeClass
    public static void beforeClass() {
        JsonFactory factory = new OfJsonFactory();
        JSON.registerFactory(factory);
        groupJs = getJsonContents(JSON_GROUP_MOD);
    }
    /**
     * An end-to-end test of groupCodec of a single group using JSON.fromJson
     * and JSON.toJson.
     */
    @Test
    public void testOfmGroupMod() {

        OfmGroupMod group = JSON.fromJson(groupJs, OfmGroupMod.class);
        print(JSON.toJson(group, true));
        String actual = JSON.toJson(group, true);
        assertEquals(normalizeEOL(groupJs), normalizeEOL(JSON.toJson(group,
                true)));
        validate(actual, OfmGroupModCodec.ROOT);
    }

    /**
     * An end-to-end test of Codec of multiple groups using JSON.fromJson
     * and JSON.toJson.
     */
    @Test
    public void testOfmGroupMods() {
        String groupsJs = getJsonContents(JSON_GROUP_MODS);

        List<OfmGroupMod> groups = JSON.fromJsonList(groupsJs,
                OfmGroupMod.class);
        String actual = JSON.toJsonList(groups,OfmGroupMod.class, true);
        assertEquals(normalizeEOL(groupsJs), normalizeEOL(JSON.toJsonList(
                groups, OfmGroupMod.class, true)));
        validate(actual, OfmGroupModCodec.ROOTS);
    }

    @Test
    public void encode() {
        OfmMutableGroupMod groupMod = (OfmMutableGroupMod)
                MessageFactory.create(V_1_3, MessageType.GROUP_MOD);
        List<Action> actions = ActionCodecTestUtils.createAllActions(V_1_3);

        groupMod.groupId(EXP_ID);
        groupMod.command(EXP_COMMAND);
        groupMod.groupType(EXP_GRP_TYPE);

        // add the first bucket
        MutableBucket bucket = BucketFactory.createMutableBucket(V_1_3);
        bucket.weight(EXP_B1_WEIGHT).watchGroup(EXP_B1_W_GROUP)
              .watchPort(EXP_B1_W_PORT)
              .addAction(ActionFactory.createAction(V_1_3, OUTPUT, EXP_PORT))
              .addAction(ActionFactory.createAction(V_1_3, DEC_NW_TTL))
              .addAction(ActionFactory.createActionSetField(V_1_3, IPV4_DST,
                                                             EXP_IP_ADDRESS));
        groupMod.addBucket((Bucket)bucket.toImmutable());

        //add the second bucket
        MutableBucket bucket1 = BucketFactory.createMutableBucket(V_1_3);
        bucket1.weight(EXP_B2_WEIGHT).watchGroup(EXP_B2_W_GROUP)
               .watchPort(EXP_B2_W_PORT)
               .addAction(ActionFactory.createAction(V_1_3, OUTPUT, EXP_PORT))
               .addAction(ActionFactory.createAction(V_1_3, DEC_NW_TTL))
               .addAction(ActionFactory.createActionSetField(V_1_3, IPV4_DST,
                                                       EXP_IP_ADDRESS));
        groupMod.addBucket((Bucket)bucket1.toImmutable());

        String exp = getJsonContents(JSON_GROUP_MOD);
        String actual = codec.encode((OfmGroupMod)
                                     groupMod.toImmutable(), true);
        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
        validate(actual, OfmGroupModCodec.ROOT);

    }

    @Test
    public void decodeList_v13() {
        String actual = getJsonContents(JSON_GROUP_MODS);

        List<OfmGroupMod> groupMods = codec.decodeList(actual);

        OfmGroupMod groupMod = groupMods.get(0);

        assertEquals(AM_NEQ, EXP_ID, groupMod.getGroupId());
        assertEquals(AM_NEQ, EXP_GRP_TYPE, groupMod.getGroupType());
        assertEquals(AM_NEQ, EXP_COMMAND, groupMod.getCommand());
        assertEquals(AM_NEQ, EXP_NUM_BUCKET, groupMod.getBuckets().size());

        List<Bucket> buckets = groupMod.getBuckets();
        Iterator<Bucket> bIter = buckets.iterator();

        // Bucket 0
        Bucket bkt = bIter.next();
        verifyBucketHeader(bkt, EXP_B1_WEIGHT,
                           EXP_B1_W_PORT, EXP_B1_W_GROUP, EXP_B1_NUM_ACTIONS);

        ActionCodecTestUtils.verifyGroupRandomActions(bkt.getActions());

     // Bucket 1
        bkt = bIter.next();
        verifyBucketHeader(bkt, EXP_B1_WEIGHT,
                           EXP_B2_W_PORT, EXP_B2_W_GROUP, EXP_B1_NUM_ACTIONS);

        ActionCodecTestUtils.verifyGroupRandomActions(bkt.getActions());

        groupMod = groupMods.get(1);

        assertEquals(AM_NEQ, EXP_GRP_1_ID, groupMod.getGroupId());
        assertEquals(AM_NEQ, EXP_GRP_1_TYPE, groupMod.getGroupType());
        assertEquals(AM_NEQ, EXP_GRP_1_COMMAND, groupMod.getCommand());
        assertEquals(AM_NEQ, EXP_NUM_BUCKET, groupMod.getBuckets().size());

        buckets = groupMod.getBuckets();
        bIter = buckets.iterator();

        // Bucket 0
        bkt = bIter.next();
        verifyBucketHeader(bkt, EXP_GRP_1_B1_WEIGHT,
                          EXP_GRP_1_B1_W_PORT, EXP_GRP_1_B1_W_GROUP,
                          EXP_B1_NUM_ACTIONS);

        ActionCodecTestUtils.verifyGroupRandomActions(bkt.getActions());

     // Bucket 1
        bkt = bIter.next();
        verifyBucketHeader(bkt, EXP_B1_WEIGHT,
                          EXP_B2_W_PORT, EXP_B2_W_GROUP, EXP_B1_NUM_ACTIONS);

        ActionCodecTestUtils.verifyGroupRandomActions(bkt.getActions());
    }

    @Test
    public void decode() {
        String actual = getJsonContents(JSON_GROUP_MOD);

        OfmGroupMod groupMod = codec.decode(actual);

        assertEquals(AM_NEQ, EXP_ID, groupMod.getGroupId());
        assertEquals(AM_NEQ, EXP_GRP_TYPE, groupMod.getGroupType());
        assertEquals(AM_NEQ, EXP_COMMAND, groupMod.getCommand());
        assertEquals(AM_NEQ, EXP_NUM_BUCKET, groupMod.getBuckets().size());

        List<Bucket> buckets = groupMod.getBuckets();
        Iterator<Bucket> bIter = buckets.iterator();

        // Bucket 0
        Bucket bkt = bIter.next();
        verifyBucketHeader(bkt, EXP_B1_WEIGHT,
                           EXP_B1_W_PORT, EXP_B1_W_GROUP, EXP_B1_NUM_ACTIONS);
        ActionCodecTestUtils.verifyGroupRandomActions(bkt.getActions());

     // Bucket 1
        bkt = bIter.next();
        verifyBucketHeader(bkt, EXP_B1_WEIGHT,
                           EXP_B2_W_PORT, EXP_B2_W_GROUP, EXP_B1_NUM_ACTIONS);

        ActionCodecTestUtils.verifyGroupRandomActions(bkt.getActions());
    }

    private void verifyBucketHeader(Bucket bkt, int expWgt,
                                    BigPortNumber expPort, GroupId expGroup,
                                    int expNumAct) {
        assertEquals(AM_NEQ, expWgt, bkt.getWeight());
        assertEquals(AM_NEQ, expPort, bkt.getWatchPort());
        assertEquals(AM_NEQ, expGroup, bkt.getWatchGroup());
        assertEquals(AM_UXS, expNumAct, bkt.getActions().size());
    }

    @Test
    public void encodeList_v13() {
        List<OfmGroupMod> groupMods = new ArrayList<OfmGroupMod>();

        // ==== create the first group mod object ====
        OfmMutableGroupMod groupMod = (OfmMutableGroupMod)
                MessageFactory.create(V_1_3, MessageType.GROUP_MOD);

        groupMod.groupId(EXP_ID);
        groupMod.command(EXP_COMMAND);
        groupMod.groupType(EXP_GRP_TYPE);

        // add the first bucket
        MutableBucket bucket = BucketFactory.createMutableBucket(V_1_3);
        bucket.weight(EXP_B1_WEIGHT).watchGroup(EXP_B1_W_GROUP)
              .watchPort(EXP_B1_W_PORT)
              .addAction(ActionFactory.createAction(V_1_3, OUTPUT, EXP_PORT))
              .addAction(ActionFactory.createAction(V_1_3, DEC_NW_TTL))
              .addAction(ActionFactory.createActionSetField(V_1_3, IPV4_DST,
                                                             EXP_IP_ADDRESS));
        groupMod.addBucket((Bucket)bucket.toImmutable());

        //add the second bucket
        MutableBucket bucket1 = BucketFactory.createMutableBucket(V_1_3);
        bucket1.weight(EXP_B2_WEIGHT).watchGroup(EXP_B2_W_GROUP)
               .watchPort(EXP_B2_W_PORT)
               .addAction(ActionFactory.createAction(V_1_3, OUTPUT, EXP_PORT))
               .addAction(ActionFactory.createAction(V_1_3, DEC_NW_TTL))
               .addAction(ActionFactory.createActionSetField(V_1_3, IPV4_DST,
                                                       EXP_IP_ADDRESS));
        groupMod.addBucket((Bucket)bucket1.toImmutable());

        groupMods.add((OfmGroupMod)groupMod.toImmutable());

     // ==== create the second group mod object ====

        groupMod = (OfmMutableGroupMod)
                MessageFactory.create(V_1_3, MessageType.GROUP_MOD);

        groupMod.groupId(EXP_GRP_1_ID);
        groupMod.command(EXP_GRP_1_COMMAND);
        groupMod.groupType(EXP_GRP_1_TYPE);

        // add the first bucket
        bucket = BucketFactory.createMutableBucket(V_1_3);
        bucket.weight(EXP_GRP_1_B1_WEIGHT).watchGroup(EXP_GRP_1_B1_W_GROUP)
                .watchPort(EXP_GRP_1_B1_W_PORT)
                .addAction(ActionFactory.createAction(V_1_3, OUTPUT, EXP_PORT))
                .addAction(ActionFactory.createAction(V_1_3, DEC_NW_TTL))
                .addAction(ActionFactory.createActionSetField(V_1_3, IPV4_DST,
                        EXP_IP_ADDRESS));
        groupMod.addBucket((Bucket)bucket.toImmutable());

        //add the second bucket
        bucket = BucketFactory.createMutableBucket(V_1_3);
        bucket.weight(EXP_B2_WEIGHT).watchGroup(EXP_B2_W_GROUP)
               .watchPort(EXP_B2_W_PORT)
               .addAction(ActionFactory.createAction(V_1_3, OUTPUT, EXP_PORT))
               .addAction(ActionFactory.createAction(V_1_3, DEC_NW_TTL))
               .addAction(ActionFactory.createActionSetField(V_1_3, IPV4_DST,
                                                       EXP_IP_ADDRESS));
        groupMod.addBucket((Bucket)bucket.toImmutable());

        groupMods.add((OfmGroupMod)groupMod.toImmutable());

        String exp = getJsonContents(JSON_GROUP_MODS);
        String actual = codec.encodeList(groupMods, true);
        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
        validate(actual, OfmGroupModCodec.ROOTS);

    }



}
