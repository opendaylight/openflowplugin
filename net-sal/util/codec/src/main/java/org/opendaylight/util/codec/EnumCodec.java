/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.codec;

import org.opendaylight.util.EnumUtils;
import org.opendaylight.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * This class provides the ability to encode and decode enumeration
 * constants to a short string form. It uses a {@link StringSetCodec}
 * behind the scenes to do the mapping, but provides methods in terms
 * of the enumeration constants.
 *
 * @see StringSetCodec
 * @see CodecFactory
 *
 * @author Simon Hunt
 * @param <E> enumeration class
 */
public class EnumCodec<E extends Enum<E>> {

    // IMPLEMENTATION NOTE: rather than extend StringSetCodec, we
    //  compose one privately, and delegate, because it takes the
    //  set of strings up front. We can't do that in the first line
    //  of our constructor, since we are only given the enum class.

    private final StringSetCodec codec;
    private final Class<E> enumClass;

    /** Constructs an enumeration codec implementation using the names
     * of the constants of the given enumeration class as the
     * original strings.
     *
     * @param eClass the class of enumeration to encode
     */
    EnumCodec(Class<E> eClass) {
        if (eClass==null)
            throw new NullPointerException("Must specify the enum class");

        Set<String> names = new HashSet<String>();
        for (E e: eClass.getEnumConstants())
            names.add(e.name());
        this.codec = new StringSetCodec(names, Algorithm.PREFIX);
        this.enumClass = eClass;
    }


    /** Returns the encoding for the given constant.
     *
     * @param constant the constant
     * @param <T> enumeration class
     * @return the encoding
     */
    public <T extends Enum<T>> String encode(T constant) {
        return codec.encode(constant.name());
    }

    /** Returns the enum constant mapped to the given encoding.
     *
     * @param encoding the encoding
     * @return the corresponding enum constant
     */
    public E decode(String encoding) {
        String name = codec.decode(encoding);
        return EnumUtils.getEnumByName(enumClass, name);
    }

    /** Returns the number of mappings in this codec, that is, the
     * number of constants in the enumeration class.
     *
     * @return the number of mappings
     */
    public int size() {
        return codec.size();
    }

    /** Returns a string representation of the codec, useful for
     * debugging.
     *
     * @return the debug string
     */
    public String toDebugString() {
        return "Class: " + enumClass + StringUtils.EOL + codec.toDebugString();
    }
}
