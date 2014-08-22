/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.json;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A generic Json codec interface for a given POJO class.
 * 
 * @author Liem Nguyen
 * @param <T> Type of a particular object to be encoded/decoded
 */
public interface JsonCodec<T> {
    
    /**
     * Returns the root element's singular name.
     * 
     * @return the root element's singular name
     */
    String root();
    
    /**
     * Returns the root element's plural name.
     * 
     * @return the root element's plural name
     */
    String roots();

    /**
     * Encodes the given POJO to its JSON string representation.
     * 
     * @param pojo POJO to encode
     * @param prettyPrint Human-readable formatted JSON if true, single-line
     *        JSON otherwise
     * @return JSON string representation of POJO
     */
    String encode(T pojo, boolean prettyPrint);

    /**
     * Encodes the collection of POJOs to their JSON string representation.
     * 
     * @param pojos list of POJO's to encode
     * @param prettyPrint human-readable formatted JSON if true, single-line
     *        JSON otherwise
     * @return JSON string representation of the list of POJO's
     */
    String encodeList(Collection<T> pojos, boolean prettyPrint);

    /**
     * Decodes the JSON string into a typed POJO.
     * 
     * @param json JSON string representation of the typed POJO
     * @return typed POJO
     */
    T decode(String json);

    /**
     * Decodes the JSON string into a typed list of POJO.
     * 
     * @param json JSON string representation of the typed list of POJOs
     * @return typed list of POJOs
     */
    List<T> decodeList(String json);

    /**
     * Encodes the given POJO to its JSON representation.
     * 
     * @param pojo POJO to encode
     * @return ObjectNode representation of POJO. This does not have a "top"
     *         root
     */
    ObjectNode encode(T pojo);

    /**
     * Decodes the JSON into a typed POJO.
     * 
     * @param node JSON representation of the typed POJO. This does not have a
     *        "top" root
     * @return typed POJO
     */
    T decode(ObjectNode node);

    /**
     * Encodes the collection of POJOs to their JSON representation.
     * 
     * @param pojos list of POJO's to encode
     * @return ArrayNode representation of the list of POJO's. This does not
     *         have the "top" root
     */
    ArrayNode encodeList(Collection<T> pojos);
    
}
