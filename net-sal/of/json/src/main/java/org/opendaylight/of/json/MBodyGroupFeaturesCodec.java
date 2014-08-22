/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
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
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.mp.MBodyGroupFeatures;
import org.opendaylight.of.lib.mp.MBodyMutableGroupFeatures;
import org.opendaylight.of.lib.msg.GroupCapability;
import org.opendaylight.of.lib.msg.GroupType;

import java.util.HashSet;
import java.util.Set;

import static org.opendaylight.of.json.CodecUtils.decodeProtocolVersion;
import static org.opendaylight.of.lib.mp.MpBodyFactory.createReplyBody;
import static org.opendaylight.of.lib.mp.MultipartType.GROUP_FEATURES;
import static org.opendaylight.util.JSONUtils.fromKey;
import static org.opendaylight.util.JSONUtils.toKey;

/**
 * A JSON codec capable of encoding and decoding {@link MBodyGroupFeatures}
 * objects.
 *
 * @author Shaila Shree
 */
public class MBodyGroupFeaturesCodec extends OfJsonCodec<MBodyGroupFeatures> {

    public static final String ROOT = "group_features";
    public static final String ROOTS = "group_features";

    private static final String CAPABILITIES = "capabilities";
    private static final String TYPES = "types";
    private static final String MAX_GROUPS = "max_groups";
    private static final String ACTION_TYPES = "actions";

    /**
     * Constructs a MBodyGroupFeaturesCodec.
     */
    protected MBodyGroupFeaturesCodec() {
        super(ROOT, ROOTS);
    }

    @Override
    public ObjectNode encode(MBodyGroupFeatures features) {
        ObjectNode node = objectNode();

        node.put(CAPABILITIES, encodeCap(features.getCapabilities()));
        node.put(TYPES, encodeTypes(features.getTypes()));
        node.put(MAX_GROUPS, encodeMaxGroups(features));
        node.put(ACTION_TYPES, encodeActionTypes(features));

        return node;
    }

    /**
     * Convert a set of {@link GroupCapability}s to a
     * JSON {@link com.fasterxml.jackson.databind.node.ArrayNode}.
     *
     * @param capabilities a set of GroupCapability
     * @return JSON representation of an array of GroupCapability
     */
    private ArrayNode encodeCap(Set<GroupCapability> capabilities) {
        ArrayNode cap = arrayNode();
        for (GroupCapability type : capabilities)
            cap.add(toKey(type));

        return cap;
    }

    /**
     * Convert a set of {@link GroupType}s to a
     * JSON {@link com.fasterxml.jackson.databind.node.ArrayNode}.
     *
     * @param groupTypes a set of GroupType
     * @return JSON representation of an array of GroupTypes
     */
    private ArrayNode encodeTypes(Set<GroupType> groupTypes) {
        ArrayNode gTypes = arrayNode();
        for (GroupType type : groupTypes)
            gTypes.add(toKey(type));

        return gTypes;
    }

    /**
     * Convert a maximum number of groups for each type to a
     * JSON {@link com.fasterxml.jackson.databind.node.ArrayNode}.
     *
     * @param mf an object of MBodyMutableGroupFeatures
     * @return JSON representation of an array of Group type
     * and max number of groups for a given type
     */
    private ArrayNode encodeMaxGroups(MBodyGroupFeatures mf) {
        ArrayNode gTypes = arrayNode();

        for (GroupType type : mf.getTypes()) {
            ObjectNode node = objectNode();
            node.put(toKey(type), mf.getMaxGroupsForType(type));
            gTypes.add(node);
        }

        return gTypes;
    }

    /**
     * Convert a set of {@link ActionType}s for each group type to a
     * JSON {@link com.fasterxml.jackson.databind.node.ArrayNode}.
     *
     * @param mf an object of MBodyMutableGroupFeatures
     * @return JSON representation of an array of Grouptype
     * and max number of groups for a given type
     */
    private ArrayNode encodeActionTypes(MBodyGroupFeatures mf) {
        ArrayNode gTypes = arrayNode();

        for (GroupType type : mf.getTypes()) {
            ObjectNode node = objectNode();

            ArrayNode aTypes = arrayNode();
            for(ActionType aType: mf.getActionsForType(type))
                aTypes.add(toKey(aType));

            node.put(toKey(type), aTypes);

            gTypes.add(node);
        }

        return gTypes;
    }

    @Override
    public MBodyGroupFeatures decode(ObjectNode node) {
        ProtocolVersion ver = decodeProtocolVersion(node.get(VERSION));
        MBodyMutableGroupFeatures features =
                (MBodyMutableGroupFeatures) createReplyBody(ver, GROUP_FEATURES);

        features.capabilities(decodeCap(node.get(CAPABILITIES)));
        decodeTypes(features, node.get(TYPES));
        decodeMaxGroupsForType(features, node.get(MAX_GROUPS));
        decodeActionsForType(features, node.get(ACTION_TYPES));

        return (MBodyGroupFeatures) features.toImmutable();
    }

    /**
     * Convert a JSON array of group capability to a set of
     * {@link GroupCapability}s.
     *
     * @param jsonNode a JSON array of group capability
     * @return a set of GroupCapability as decoded from JSON
     */
    private Set<GroupCapability> decodeCap(JsonNode jsonNode) {
        ArrayNode an = (ArrayNode) jsonNode;
        Set<GroupCapability> types = new HashSet<GroupCapability>();
        for (int i = 0; i < an.size(); i++)
            types.add(fromKey(GroupCapability.class, an.get(i).asText()));

        return types;
    }

    /**
     * Parse a JSON array of group types to a set of {@link GroupType}s
     * and populate MBodyMutableGroupFeatures.
     *
     * @param features MBodyMutableGroupFeatures to be populated
     * @param jsonNode a JSON array of group type vs max groups
     */
    private void decodeTypes(MBodyMutableGroupFeatures features,
                             JsonNode jsonNode) {
        ArrayNode gTypeNode = (ArrayNode) jsonNode;
        Set<GroupType> gTypes = new HashSet<GroupType>();
        for (int i = 0; i < gTypeNode.size(); i++)
            gTypes.add(fromKey(GroupType.class, gTypeNode.get(i).asText()));

        features.groupTypes(gTypes);
    }

    /**
     * Parse the JSON array of group types vs max number of groups
     * and populate MBodyMutableGroupFeatures.
     *
     * @param features MBodyMutableGroupFeatures to be populated
     * @param jsonNode a JSON array of group type vs max groups
     */
    private void decodeMaxGroupsForType(MBodyMutableGroupFeatures features,
                                        JsonNode jsonNode) {
        ArrayNode gTypeNode = (ArrayNode) jsonNode;
        for (int i = 0; i < gTypeNode.size(); i++) {
            JsonNode node = gTypeNode.get(i);

            String typeName = node.fieldNames().next();
            GroupType type = fromKey(GroupType.class, typeName);
            long maxGroups = node.get(typeName).asLong();

            features.maxGroupsForType(type, maxGroups);
        }
    }

    /**
     * Parse a JSON array of group types vs action types supported for each group
     * and populate MBodyMutableGroupFeatures.
     *
     * @param features MBodyMutableGroupFeatures to be populated
     * @param jsonNode a JSON array of group types
     */
    private void decodeActionsForType(MBodyMutableGroupFeatures features,
                                      JsonNode jsonNode) {
        ArrayNode gTypeNode = (ArrayNode) jsonNode;
        for (int i = 0; i < gTypeNode.size(); i++) {
            JsonNode node = gTypeNode.get(i);

            String typeName = node.fieldNames().next();
            GroupType type = fromKey(GroupType.class, typeName);

            ArrayNode aTypeNode = (ArrayNode) node.get(typeName);
            Set<ActionType> aTypes = new HashSet<ActionType>();
            for (int j = 0; j < aTypeNode.size(); j++)
                aTypes.add(fromKey(ActionType.class, aTypeNode.get(j).asText()));

            features.actionsForType(type, aTypes);
        }
    }
}
