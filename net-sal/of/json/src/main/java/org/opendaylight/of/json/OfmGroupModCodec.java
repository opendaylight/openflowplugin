/*
 * (c) Copyright 2013-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.msg.*;

import java.util.ArrayList;
import java.util.List;

import static org.opendaylight.of.json.CodecUtils.encodeBigPort;
import static org.opendaylight.of.json.CodecUtils.encodeGroupId;
import static org.opendaylight.util.JSONUtils.fromKey;
import static org.opendaylight.util.JSONUtils.toKey;

/**
 * A JSON codec capable of encoding and decoding {@link OfmGroupMod} objects.
 *
 * @author Prashant Nayak
 * @author Simon Hunt
 */
public class OfmGroupModCodec extends OfJsonCodec<OfmGroupMod> {

    // unit test access
    static final String ROOT = "group";
    static final String ROOTS = "groups";

    private static final String GROUP_TYPE = "type";
    private static final String BUCKETS = "buckets";
    private static final String WEIGHT = "weight";
    private static final String WATCH_GROUP = "watch_group";
    private static final String WATCH_PORT = "watch_port";
    private static final String ACTIONS = "actions";

    // the following are used externally for injecting values dynamically
    public static final String GROUP_ID = "id";
    public static final String COMMAND = "command";

    private volatile ActionCodec codec = null;

    protected OfmGroupModCodec() {
        super(ROOT, ROOTS);
    }

    @Override
    public ObjectNode encode(OfmGroupMod gm) {
        ObjectNode groupNode = mapper.createObjectNode();
        groupNode.put(GROUP_ID, gm.getGroupId().toLong());
        groupNode.put(GROUP_TYPE, toKey(gm.getGroupType()));
        groupNode.put(COMMAND, toKey(gm.getCommand()));
        groupNode.put(BUCKETS, encodeBuckets(gm));
        return  groupNode;
    }

    private ArrayNode encodeBuckets(OfmGroupMod gm) {
        List<Bucket> buckets = gm.getBuckets();
        ArrayNode bArray = arrayNode();
        if (buckets != null) {
            for (Bucket bucket : buckets) {
                ObjectNode bNode = mapper.createObjectNode();
                bNode.put(WEIGHT, bucket.getWeight());
                bNode.put(WATCH_GROUP, encodeGroupId(bucket.getWatchGroup()));
                encodeBigPort(bNode, WATCH_PORT, bucket.getWatchPort(),
                        bucket.getVersion());
                bNode.put(ACTIONS, encodeActions(bucket));
                bArray.add(bNode);
            }
        }
        return bArray;
    }

    private ArrayNode encodeActions(Bucket bucket) {
        return getActionCodecInstance().encodeList(bucket.getActions());
    }

    @Override
    public OfmGroupMod decode(ObjectNode groupNode) {
        ProtocolVersion pv = version(groupNode);

        OfmMutableGroupMod gm = (OfmMutableGroupMod)
                MessageFactory.create(pv, MessageType.GROUP_MOD);

        gm.groupId(GroupId.valueOf(groupNode.get(GROUP_ID).longValue()));
        gm.groupType(fromKey(GroupType.class, groupNode.get(GROUP_TYPE).asText()));
        gm.command(fromKey(GroupModCommand.class, groupNode.get(COMMAND).asText()));

        List<Bucket> buckets = decodeBuckets(pv, groupNode.get(BUCKETS));
        for (Bucket bucket: buckets)
            gm.addBucket(bucket);

        return (OfmGroupMod) gm.toImmutable();
    }

    private List<Bucket> decodeBuckets(ProtocolVersion pv, JsonNode node) {
        List<Bucket> buckets = new ArrayList<>();

        for (JsonNode bktNode: node) {
            MutableBucket bucket = BucketFactory.createMutableBucket(pv);
            ObjectNode bNode = (ObjectNode) bktNode;
            bucket.weight(bNode.get(WEIGHT).asInt());
            bucket.watchGroup(CodecUtils.decodeGroupId(bNode.get(WATCH_GROUP)));
            bucket.watchPort(CodecUtils.decodeBigPort(bNode.get(WATCH_PORT)));

            List<Action> actions = decodeActions(pv, bNode.get(ACTIONS));
            for (Action action: actions)
               bucket.addAction(action);
            buckets.add((Bucket) bucket.toImmutable());
        }

        return buckets;
    }

    private List<Action> decodeActions(ProtocolVersion pv, JsonNode node) {
        List<Action> actions = new ArrayList<>();

        for (JsonNode actNode: node) {
            ObjectNode action = (ObjectNode) actNode;
            action.put(VERSION, CodecUtils.encodeProtocolVersion(pv));
            actions.add(getActionCodecInstance().decode(action));
        }
        return actions;
    }

    private ActionCodec getActionCodecInstance(){
        if (codec == null) {
            codec = (ActionCodec) OfJsonFactory.instance().codec(Action.class);
        }
        return codec;
    }
}
