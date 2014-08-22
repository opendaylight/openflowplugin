/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides useful utilities operating on or with enumerations.
 * 
 * @author Frank Wood
 * @author Simon Hunt
 */
public class EnumUtils {

    /**
     * This interface must be implemented by enumerations to support
     * the creation of a code lookup map.
     */
    public interface Coded {
        /** Returns the integer code for this enumeration constant.
         *
         * @return the integer code
         */
        public int getCode();
    }

    // No Instantiation Allowed
    private EnumUtils() { }

    /**
     * Returns the enumeration constant whose {@link Enum#toString()
     * toString()} matches the {@code toString()} of the specified object.
     * 
     * @param <E> enumeration type
     * @param enumClass enumeration class
     * @param obj object
     * @return enumeration constant for the object or null if not found
     */
    public static <E extends Enum<E>> E getEnum(Class<E> enumClass,
                                                Object obj) {
        if (null != obj) {
            for (E e : enumClass.getEnumConstants()) {
                if (e.toString().equals(obj.toString()))
                    return e;
            }
        }
        return null;
    }

    /**
     * Returns the enumeration constant whose {@link Enum#name() name()} matches
     * the {@code toString()} of the specified object.
     *
     * @param <E> enumeration type
     * @param enumClass enumeration class
     * @param obj object
     * @return enumeration constant for the object or null if not found
     */
    public static <E extends Enum<E>> E getEnumByName(Class<E> enumClass,
                                                      Object obj) {
        if (null != obj) {
            for (E e : enumClass.getEnumConstants()) {
                if (e.name().equals(obj.toString()))
                    return e;
            }
        }
        return null;
    }

    /**
     * Returns a new map providing code to enumeration lookup.
     * 
     * @param <E> enumeration type
     * @param enumClass enumeration class
     * @return mapping of enumeration code to enumeration.
     */
    public static <E extends Enum<E> & Coded> Map<Integer, E>
            createLookup(Class<E> enumClass) {
        
        Map<Integer, E> m = new HashMap<Integer, E>();
        for (E e : enumClass.getEnumConstants())
            m.put(e.getCode(), e);
        return m;
    }
    
    /**
     * Returns an enumeration constant based on the code.
     * 
     * @param <E> enumeration type
     * @param enumClass enumeration class
     * @param code target code
     * @param defEnum enumeration constant to return if code not found
     * @return enumeration constant associated with the target code
     */
    public static <E extends Enum<E> & Coded> E getEnum(Class<E> enumClass,
                                                        int code,
                                                        E defEnum) {
        for (E e : enumClass.getEnumConstants()) {
            if (code == e.getCode())
                return e;
        }
        return defEnum;
    }

}
