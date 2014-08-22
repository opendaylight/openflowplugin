/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.json;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A utility for encoding and decoding POJOs to and from their corresponding
 * JSON representations. This utility merely relies on the underlying
 * {@link JsonFactory}'s to provide the needed codecs.
 * 
 * @author Liem Nguyen
 */
public class JSON {

    private static Set<JsonFactory> factories;
    static {
        factories = new HashSet<JsonFactory>();
    }

    private JSON() {
    }

    /**
     * Register the given factory with this utility.
     * 
     * @param factory JsonFactory to register
     */
    public static void registerFactory(JsonFactory factory) {
        factories.add(factory);
    }

    /**
     * Unregister the given factory with this utility. It is the
     * responsibility of the factory to do any clean up necessary.
     * 
     * @param factory JsonFactory to unregister
     */
    public static void unregisterFactory(JsonFactory factory) {
        factories.remove(factory);
    }

    /**
     * Convert the given JSON string to the given typed POJO.
     * 
     * @param json JSON string representing an instance of the given POJO
     *        class
     * @param pojo POJO class of the given JSON string
     * @return POJO instance represented by the JSON string
     */
    public static <T> T fromJson(String json, Class<T> pojo) {
        JsonFactory factory = pojoFactory(pojo);
        @SuppressWarnings("unchecked")
        JsonCodec<T> codec = (JsonCodec<T>) factory.codec(pojo);
        return codec.decode(json);
    }

    /**
     * Convert the given JSON string to a typed list of POJO's.
     * 
     * @param json JSON string representing a typed list of POJO's
     * @param pojo POJO class of the given JSON string
     * @return typed list of POJO's represented by the JSON string
     */
    public static <T> List<T> fromJsonList(String json, Class<T> pojo) {
        JsonFactory factory = pojoFactory(pojo);
        @SuppressWarnings("unchecked")
        JsonCodec<T> codec = (JsonCodec<T>) factory.codec(pojo);
        return codec.decodeList(json);
    }

    /**
     * Convert a POJO to its JSON representation.
     * 
     * @param pojo POJO instance to convert
     * @param prettyPrint human-readable formatted JSON if true, single-line
     *        JSON otherwise
     * @return JSON string representation of POJO
     */
    public static <T> String toJson(T pojo, boolean prettyPrint) {
        JsonFactory factory = pojoFactory(pojo.getClass());
        @SuppressWarnings("unchecked")
        JsonCodec<T> codec = (JsonCodec<T>) factory.codec(pojo.getClass());
        return codec.encode(pojo, prettyPrint);
    }
    
    /**
     * Convert a POJO to its JSON representation with compact formatting.
     * 
     * @param pojo POJO instance to convert
     * 
     * @return JSON string representation of POJO
     */
    public static <T> String toJson(T pojo) {
        return toJson(pojo, false);
    }

    /**
     * Convert a collection of POJO's into its JSON representation.
     * 
     * @param pojos collection of POJO's
     * @param pojo class of POJO
     * @param prettyPrint human-readable formatted JSON if true, single-line
     *        JSON otherwise
     * @return JSON string representation of the list of POJO's
     */
    public static <T> String toJsonList(Collection<T> pojos, Class<T> pojo,
            boolean prettyPrint) {
        JsonFactory factory = pojoFactory(pojo);
        @SuppressWarnings("unchecked")
        JsonCodec<T> codec = (JsonCodec<T>) factory.codec(pojo);
        return codec.encodeList(pojos, prettyPrint);
    }
    
    /**
     * Convert a collection of POJO's into its JSON representation with compact
     * formatting.
     * 
     * @param pojos collection of POJO's
     * @param pojo class of POJO
     * 
     * @return JSON string representation of the list of POJO's
     */
    public static <T> String toJsonList(Collection<T> pojos, Class<T> pojo) {
        return toJsonList(pojos, pojo, false);
    }

    /**
     * List all registered factories (unmodifiable)
     * 
     * @return set of registered factories
     */
    public static Set<JsonFactory> factories() {
        return Collections.unmodifiableSet(factories);
    }

    /**
     * Retrieve the JSON factory instance by the given class.
     * 
     * @param factoryClazz class of json factory to retrieve
     * @return the JSON factory instance by the given class
     */
    public static <T> JsonFactory factory(Class<T> factoryClazz) {
        for (JsonFactory factory : factories)
            if (factory.getClass().getName().equals(factoryClazz.getName()))
                return factory;
        throw new JsonCodecException("No factory found for "
                + factoryClazz.getName());
    }

    /**
     * Retrieve a JSON factory for the given pojo class.
     * 
     * @param pojo pojo class
     * @return json factory for given pojo class
     * @throws JsonCodecException if there is no factory for pojo class
     */
    private static <T> JsonFactory pojoFactory(Class<T> pojo) {
        for (JsonFactory factory : factories)
            if (factory.hasCodec(pojo))
                return factory;
        throw new JsonCodecException("No codec found for " + pojo.getName());
    }
}
