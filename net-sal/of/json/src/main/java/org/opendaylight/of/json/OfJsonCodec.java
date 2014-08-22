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
import org.opendaylight.of.lib.Structure;
import org.opendaylight.util.json.AbstractJsonCodec;
import org.opendaylight.util.json.JsonCodec;
import org.opendaylight.util.json.JsonCodecException;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The base JSON codec for all Openflow-related stuff, particularly all
 * {@link Structure} classes. All JSON representations of the
 * {@link Structure} will have an injected top-level "version" node that
 * correspond to the version from {@link Structure#getVersion()}.
 * <p>
 * Accordingly, the {@link JsonCodec#decode(ObjectNode)} method implemented by
 * sub-classes of this class <b>must</b> ensure that the ObjectNode parameter
 * indeed contains a "version" node so that it can re-hydrate the
 * {@link Structure} POJO successfully.
 * 
 * @author Liem Nguyen
 * @param <T> type of POJO for which a JSON codec is provided
 */
public abstract class OfJsonCodec<T extends Structure> extends
        AbstractJsonCodec<T> {

    public static final String VERSION = "version";

    protected OfJsonCodec(String root, String roots) {
        super(root, roots);
    }

    // Inject in OF version at the node level
    @Override
    public T decode(String json) {
        ObjectNode top = (ObjectNode) read(json);
        ObjectNode node = (ObjectNode) top.get(root);
        if (node == null)
            throw new JsonCodecException("Invalid JSON format: " + json);
        node.put(VERSION, versionText(top));
        return decode(node);
    }

    // Inject in OF version at the node level
    @Override
    public List<T> decodeList(String json) {
        ObjectNode top = (ObjectNode) read(json);
        ArrayNode nodes = (ArrayNode) top.get(roots);
        if (nodes == null)
            throw new JsonCodecException("Invalid JSON format: " + json);

        List<T> pojos = new LinkedList<T>();
        Iterator<JsonNode> it = nodes.iterator();
        while (it.hasNext()) {
            ObjectNode node = (ObjectNode) it.next();
            node.put(VERSION, versionText(top));
            pojos.add(decode(node));
        }
        return pojos;
    }

    // Inject in the openflow version number at the root-level
    @Override
    public String encode(T pojo, boolean prettyPrint) {
        ObjectNode top = objectNode();
        top.put(VERSION, pojo.getVersion().toDisplayString());
        top.put(root, encode(pojo));
        return write(top, prettyPrint);
    }

    // Inject in the openflow version number at the root-level
    @Override
    public String encodeList(Collection<T> pojos, boolean prettyPrint) {
        ObjectNode top = objectNode();
        if (pojos != null && !pojos.isEmpty())
            top.put(VERSION, pojos.iterator().next().getVersion()
                .toDisplayString());
        top.put(roots, encodeList(pojos));
        return write(top, prettyPrint);
    }
    
    // Retrieve the text string of the version node
    private String versionText(ObjectNode node) {
        JsonNode verNode = node.get(VERSION); 
        if (verNode == null)
            throw new JsonCodecException("Missing \"" + VERSION + "\" tag");
        return verNode.textValue();
    }

    /**
     * Retrieve the protocol version from a given json node that contains the 
     * {@link #VERSION} tag.
     * 
     * @param node JSON node that contains the VERSION tag
     * @return protocol version
     */
    protected ProtocolVersion version(ObjectNode node) {
        return ProtocolVersion.fromString(versionText(node));
    }
}
