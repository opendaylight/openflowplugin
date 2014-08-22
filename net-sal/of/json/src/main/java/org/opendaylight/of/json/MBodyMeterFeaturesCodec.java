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
import org.opendaylight.of.lib.mp.MBodyMeterFeatures;
import org.opendaylight.of.lib.mp.MBodyMutableMeterFeatures;
import org.opendaylight.of.lib.mp.MpBodyFactory;
import org.opendaylight.of.lib.msg.MeterBandType;
import org.opendaylight.of.lib.msg.MeterFlag;
import org.opendaylight.util.JSONUtils;

import java.util.HashSet;
import java.util.Set;

import static org.opendaylight.of.json.CodecUtils.decodeProtocolVersion;
import static org.opendaylight.of.lib.mp.MultipartType.METER_FEATURES;

/**
 * A JSON codec capable of encoding and decoding {@link MBodyMeterFeatures}
 * objects.
 *
 * @author Jesse Hummer
 */
public class MBodyMeterFeaturesCodec extends OfJsonCodec<MBodyMeterFeatures> {

    public static final String ROOT = "meter_features";
    public static final String ROOTS = "meters_features";

    private static final String MAX_METERS = "max_meters";
    private static final String TYPES = "types";
    // FIXME: rename the following three constants
    //  "capabilities", "max_bands", "max_color"
    private static final String FLAGS = "flags";
    private static final String MAX_BANDS_PER_METER = "max_bands_per_meter";
    private static final String MAX_COLOR_VALUE = "max_color_value";

    /**
     * Constructs a MBodyMeterFeaturesCodec.
     */
    protected MBodyMeterFeaturesCodec() {
        super(ROOT, ROOTS);
    }

    @Override
    public ObjectNode encode(MBodyMeterFeatures features) {
        ObjectNode node = objectNode();

        node.put(MAX_METERS, features.getMaxMeters());
        node.put(TYPES, encodeTypes(features.getBandTypes()));
        node.put(FLAGS, encodeFlags(features.getCapabilities()));
        node.put(MAX_BANDS_PER_METER, features.getMaxBands());
        node.put(MAX_COLOR_VALUE, features.getMaxColor());

        return node;
    }

    /**
     * Convert a set of {@link MeterBandType}s to a JSON {@link ArrayNode}.
     *
     * @param bandTypes a set of MeterBandTypes
     * @return JSON representation of an array of MeterBandTypes
     */
    private ArrayNode encodeTypes(Set<MeterBandType> bandTypes) {
        ArrayNode an = arrayNode();
        for (MeterBandType type : bandTypes) {
            an.add(JSONUtils.toKey(type));
        }
        return an;
    }

    /**
     * Convert a set of {@link MeterFlag}s to a JSON {@link ArrayNode}.
     *
     * @param flags a set of MeterFlags
     * @return JSON representation of an array of MeterFlags
     */
    private ArrayNode encodeFlags(Set<MeterFlag> flags) {
        ArrayNode an = arrayNode();
        for (MeterFlag flag : flags) {
            an.add(JSONUtils.toKey(flag));
        }
        return an;
    }

    @Override
    public MBodyMeterFeatures decode(ObjectNode node) {
        ProtocolVersion ver = decodeProtocolVersion(node.get(VERSION));
        MBodyMutableMeterFeatures features =
                (MBodyMutableMeterFeatures) MpBodyFactory.createReplyBody(ver,
                        METER_FEATURES);
        features.maxMeters(node.get(MAX_METERS).asInt());
        features.bandTypes(decodeTypes(node.get(TYPES)));
        features.capabilities(decodeFlags(node.get(FLAGS)));
        features.maxBands(node.get(MAX_BANDS_PER_METER).asInt());
        features.maxColor(node.get(MAX_COLOR_VALUE).asInt());
        return (MBodyMeterFeatures) features.toImmutable();
    }

    /**
     * Convert a JSON array of meter band types to a set of
     * {@link MeterBandType}s.
     *
     * @param jsonNode a JSON array of meter band types
     * @return a set of MeterBandTypes as decoded from JSON
     */
    private Set<MeterBandType> decodeTypes(JsonNode jsonNode) {
        ArrayNode an = (ArrayNode) jsonNode;
        Set<MeterBandType> types = new HashSet<MeterBandType>();
        for (int i = 0; i < an.size(); i++) {
            types.add(JSONUtils
                .fromKey(MeterBandType.class, an.get(i).asText()));
        }
        return types;
    }

    /**
     * Convert a JSON array of meter flags to a set of {@link MeterFlag}s.
     *
     * @param jsonNode a JSON array of meter flags
     * @return a set of MeterFlags as decoded from JSON
     */
    private Set<MeterFlag> decodeFlags(JsonNode jsonNode) {
        ArrayNode an = (ArrayNode) jsonNode;
        Set<MeterFlag> flags = new HashSet<MeterFlag>();
        for (int i = 0; i < an.size(); i++) {
            flags.add(JSONUtils.fromKey(MeterFlag.class, an.get(i).asText()));
        }
        return flags;
    }
}
