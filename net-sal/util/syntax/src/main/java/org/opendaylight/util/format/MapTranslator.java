/*
 * (c) Copyright 2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.format;

import java.util.*;

/**
 * Formats message strings by substituting results of a map lookup for the
 * specified key string.&nbsp; It can be used in several different ways.
 * 
 * @author Thomas Vachuska
 */
public class MapTranslator extends InvalidTokenTranslator {

    /** Map used to perform token translation. */
    protected Map<String, Object> map = null;

    /**
     * Constructs a mapped translator that will use the specified map to
     * translate the tokens into their values.
     * 
     * @param map map of token-to-value bindings
     */
    public MapTranslator(Map<String, Object> map) {
        this.map = map;
    }

    /**
     * Constructs a mapped translator that will use the specified map to
     * translate the tokens into their values and will treat illegal tokens
     * according to the given behaviour.
     * 
     * @param map map of token-to-value bindings
     * @param behaviour behaviour when dealing with invalid tokens
     */
    public MapTranslator(Map<String, Object> map, int behaviour) {
        super(behaviour);
        this.map = map;
    }

    /**
     * Constructs a mapped translator that will translate the specified two
     * column array as a map, which it will use to translate the tokens into
     * their values.
     * 
     * @param bindings array of token/value bindings
     */
    public MapTranslator(Object[][] bindings) {
        this.map = new HashMap<String, Object>(bindings.length);
        for (int i = 0; i < bindings.length; i++)
            map.put(bindings[i][0].toString(), bindings[i][1]);
    }

    /**
     * Constructs a mapped translator that will translate the specified array
     * into a map, where the value is the object and the key is the string
     * image of the index at which the object was located in the array. This
     * map will then be used to translate the tokens into their values.
     * 
     * @param objects numbered array of values
     */
    public MapTranslator(Object[] objects) {
        this.map = new HashMap<String, Object>(objects.length);
        for (int i = 0; i < objects.length; i++)
            map.put(i + "", objects[i]);
    }

    /**
     * Returns the map whose methods the translator invokes.
     * 
     * @return current map of token/value bindings
     */
    public Map<String, Object> getMap() {
        return map;
    }

    /**
     * Sets the map whose methods the translator will invoke.
     * 
     * @param map new map of token/value bindings
     */
    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Translates the given token with the value obtained map.get call, using
     * the token as the key.
     */
    @Override
    public String translate(String token) {
        try {
            return map.get(token).toString();
        } catch (Exception e) {
            return translateInvalidToken(token);
        }
    }

}
