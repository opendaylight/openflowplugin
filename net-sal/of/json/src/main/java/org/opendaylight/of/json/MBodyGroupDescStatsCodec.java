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
import org.opendaylight.of.lib.mp.MBodyGroupDescStats;
import org.opendaylight.of.lib.mp.MBodyMutableGroupDescStats;
import org.opendaylight.of.lib.msg.Bucket;
import org.opendaylight.of.lib.msg.BucketFactory;
import org.opendaylight.of.lib.msg.GroupType;
import org.opendaylight.of.lib.msg.MutableBucket;

import java.util.ArrayList;
import java.util.List;

import static org.opendaylight.of.json.CodecUtils.*;
import static org.opendaylight.util.JSONUtils.fromKey;
import static org.opendaylight.util.JSONUtils.toKey;

/**
 * A JSON codec capable of encoding and decoding {@link MBodyGroupDescStats} 
 * objects.
 *
 * @author Prashant Nayak
 * @author Simon Hunt
 *
 */
public class MBodyGroupDescStatsCodec extends OfJsonCodec<MBodyGroupDescStats> {

    // unit tests access
    static final String ROOT = "group";
    static final String ROOTS = "groups";
    
    private static final String GROUP_ID = "id";
    private static final String GROUP_TYPE = "type";
    private static final String BUCKETS = "buckets";
    private static final String WEIGHT = "weight";
    private static final String WATCH_GROUP = "watch_group";
    private static final String WATCH_PORT = "watch_port";
    private static final String ACTIONS = "actions";
    
    private volatile ActionCodec codec = null;

    protected MBodyGroupDescStatsCodec() {
        super(ROOT, ROOTS);
    }

    @Override
    public ObjectNode encode(MBodyGroupDescStats gds) {
        ObjectNode gNode = mapper.createObjectNode();

        gNode.put(GROUP_ID, gds.getGroupId().toLong());
        gNode.put(GROUP_TYPE, toKey(gds.getType()));
        gNode.put(BUCKETS, encodeBuckets(gds));

        return  gNode;
    }

    private ArrayNode encodeBuckets(MBodyGroupDescStats gds) {
        List<Bucket> buckets = gds.getBuckets();
        ArrayNode bArray = mapper.createArrayNode();
        if (buckets != null) {
            for (Bucket bkt: buckets){
                ObjectNode bNode = mapper.createObjectNode();
                bNode.put(WEIGHT, bkt.getWeight());
                bNode.put(WATCH_GROUP, encodeGroupId(bkt.getWatchGroup()));
                encodeBigPort(bNode, WATCH_PORT, bkt.getWatchPort(),
                        bkt.getVersion());
                bNode.put(ACTIONS, encodeActions(bkt));
                bArray.add(bNode);
            }
        }
        return bArray;
    }

    private ArrayNode encodeActions(Bucket bucket) {
        return getActionCodecInstance().encodeList(bucket.getActions());
    }

    @Override
    public MBodyGroupDescStats decode(ObjectNode gNode) {
        ProtocolVersion pv  = decodeProtocolVersion(gNode.get(VERSION));
        
        MBodyMutableGroupDescStats gds = new MBodyMutableGroupDescStats(pv);
        
        gds.groupId(GroupId.valueOf(gNode.get(GROUP_ID).longValue()));
        gds.groupType(fromKey(GroupType.class, gNode.get(GROUP_TYPE).asText()));
        List<Bucket> buckets = decodeBuckets(pv, gNode.get(BUCKETS));
        gds.buckets(buckets);
       
        return (MBodyGroupDescStats) gds.toImmutable();
    }
    
    private List<Bucket> decodeBuckets(ProtocolVersion pv, JsonNode node) {
        List<Bucket> buckets = new ArrayList<>();
        
        for (JsonNode bktNode: node) {
            MutableBucket bkt = BucketFactory.createMutableBucket(pv);
            ObjectNode bNode = (ObjectNode) bktNode;
            bkt.weight(bNode.get(WEIGHT).asInt());
            bkt.watchGroup(decodeGroupId(bNode.get(WATCH_GROUP)));
            bkt.watchPort(decodeBigPort(bNode.get(WATCH_PORT)));
            
            List<Action> actions = decodeActions(pv, bNode.get(ACTIONS));
            for(Action action: actions)
               bkt.addAction(action);
            buckets.add((Bucket) bkt.toImmutable());
        }
        return buckets;       
    }
    
    private List<Action> decodeActions(ProtocolVersion pv, JsonNode node) {
        List<Action> actions = new ArrayList<>();
        
        for (JsonNode actNode: node) {
            ObjectNode action = (ObjectNode) actNode;
            action.put(VERSION, encodeProtocolVersion(pv));
            actions.add(getActionCodecInstance().decode(action));
        }
        return actions; 
    }
    
    private ActionCodec getActionCodecInstance() {
        if (codec == null)
            codec = (ActionCodec) OfJsonFactory.instance().codec(Action.class);
        return codec;
    }
}
