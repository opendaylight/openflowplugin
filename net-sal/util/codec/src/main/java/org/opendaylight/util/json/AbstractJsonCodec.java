/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.json;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendaylight.util.JSONUtils;
import org.opendaylight.util.Log;

/**
 * An abstract JsonCodec. Sub-classes of this class should implement a
 * strongly-typed {@link JsonCodec} interface.
 *
 * @author Liem Nguyen
 * @param <T> typed class of the POJO for which this JsonCodec serves
 */
public abstract class AbstractJsonCodec<T> implements JsonCodec<T> {
    private static final String INVALID_JSON = "Invalid JSON format. ";

    protected final Logger log = Log.RS.getLogger();

    /**
     * A JSON Mapper used for JSON encoding/decoding.
     */
    public static final ObjectMapper mapper;
    static {
        mapper = new ObjectMapper();
        // No nulls in JSON serialization
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
    protected static final String NO_DECODING_SUP = "No JSON decoding supported for ";

    protected final String root;
    protected final String roots;

    // FIXME: missing Javadocs on protected constructor
    protected AbstractJsonCodec(String root, String roots) {
        this.root = root;
        this.roots = roots;
    }

    @Override
    public String root() {
        return root;
    }

    @Override
    public String roots() {
        return roots;
    }

    @Override
    public String encode(T pojo, boolean prettyPrint) {
        ObjectNode top = objectNode();
        top.put(root, encode(pojo));
        return write(top, prettyPrint);
    }

    @Override
    public String encodeList(Collection<T> pojos, boolean prettyPrint) {
        ObjectNode top = objectNode();
        top.put(roots, encodeList(pojos));
        return write(top, prettyPrint);
    }

    @Override
    public ArrayNode encodeList(Collection<T> pojos) {
        ArrayNode node = arrayNode();
        if (pojos != null)
            for (T pojo : pojos) {
                node.add(encode(pojo));
            }
        return node;
    }

    @Override
    public T decode(String json) {
        try {
            ObjectNode top = (ObjectNode) read(json);
            ObjectNode node = (ObjectNode) top.get(root);
            return decode(node);
        } catch (Throwable t) {
            log.error(INVALID_JSON + json, t);
            throw new JsonCodecException(t.getMessage());
        }
    }

    @Override
    public List<T> decodeList(String json) {
        try {
            ObjectNode top = (ObjectNode) read(json);
            ArrayNode nodes = (ArrayNode) top.get(roots);
            List<T> pojos = new LinkedList<T>();
            for (JsonNode node : nodes)
                pojos.add(decode((ObjectNode) node));
            return pojos;
        } catch (Throwable t) {
            log.error(INVALID_JSON + json, t);            
            throw new JsonCodecException(t.getMessage());
        }
    }

    // FIXME : missing javadocs on protected methods.

    // Return an array node from a set of enums
    protected JsonNode fromEnums(Set<? extends Enum<?>> enums) {
        ArrayNode an = arrayNode();
        if (enums != null)
            for (Enum<?> e : enums)
                an.add(JSONUtils.toKey(e));
        return an;
    }

    // Return a set of enums from an array node
    protected <E extends Enum<E>> Set<E> toEnums(ArrayNode ar,
                                                 Class<E> enumClazz) {
        Set<E> enums = new HashSet<E>();
        if (ar != null)
            for (JsonNode node : ar)
                enums.add(JSONUtils.fromKey(enumClazz, node.textValue()));
        return enums;
    }

    // Convert given json node to string
    protected String write(JsonNode node, boolean prettyPrint) {
        try {
            if (prettyPrint)
                return mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(node);
            return mapper.writeValueAsString(node);
        } catch (Throwable t) {
            throw new JsonCodecException(
                    "Failed to serialize JSON for " + node, t);
        }
    }

    // Convert given json string to json node
    protected JsonNode read(String json) {
        try {
            return mapper.readTree(json);
        } catch (Throwable t) {
            throw new JsonCodecException("Failed to deserialize " + json, t);
        }
    }

    /**
     * Checks for missing keys in a node.
     *
     * @param node the JsonNode to be checked
     * @param keys key names
     * @return true if one or more keys are missing, false otherwise
     */
    protected boolean isMissingContent(JsonNode node, String... keys) {
        for (String k : keys)
            if (node.path(k).isMissingNode())
                return true;
        return false;
    }

    // creates an ObjectNode
    protected ObjectNode objectNode() {
        return mapper.createObjectNode();
    }

    // creates a ArrayNode
    protected ArrayNode arrayNode() {
        return mapper.createArrayNode();
    }
    
    // get long value from a JsonNode
    protected static long longVal(JsonNode node, String name) {
        JsonNode jn = node.get(name);
        return (jn != null) ? jn.asLong(0L) : 0L;
    }
}
