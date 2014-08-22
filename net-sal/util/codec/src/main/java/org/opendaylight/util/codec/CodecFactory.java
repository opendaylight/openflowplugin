/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.codec;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This factory class produces different implementations of codecs.
 *
 * @author Simon Hunt
 */
public final class CodecFactory {

    // no instantiation
    private CodecFactory() { }


    /** Creates and returns a string codec instance.
     * If the algorithm specified is null, the {@link Algorithm#PREFIX}
     * algorithm will be employed.
     *
     * @param originals the original strings with which to initialize the codec
     * @param a the encoding algorithm to use
     * @return a string codec
     * @throws NullPointerException if originals is null
     * @throws IllegalArgumentException if originals is empty
     */
    public static StringSetCodec
                createStringSetCodec(Set<String> originals, Algorithm a) {
        if (originals==null)
            throw new NullPointerException("originals set cannot be null");
        if (originals.size() < 1)
            throw new IllegalArgumentException(
                    "originals set must contain elements");

        Algorithm alg = a==null ? Algorithm.PREFIX : a;
        return new StringSetCodec(originals, alg);
    }

    /** Creates and returns an entropic string codec instance.
     * If the originals parameter is null (or an empty set), the codec instance
     * returned will be devoid of any mappings to start.
     * If the algorithm specified is null, the {@link Algorithm#PREFIX}
     * algorithm will be employed.
     *
     * @param originals the original strings with which to initialize the codec
     * @param a the encoding algorithm to use
     * @return an entropic string codec
     */
    public static EntropicStringSetCodec
            createEntropicStringSetCodec(Set<String> originals, Algorithm a) {
        Set<String> orig = originals==null ? Collections.<String>emptySet()
                                           : originals;
        Algorithm alg = a==null ? Algorithm.PREFIX : a;
        return new EntropicStringSetCodec(orig, alg);
    }


    // === convenience methods

    /** A convenience method to create a string codec for a general
     * set of strings. The implementation uses the PREFIX encoding
     * method.
     *
     * @param originals the original string set
     * @return the codec
     */
    public static StringCodec createGeneralCodec(Set<String> originals) {
        return new StringSetCodec(originals, Algorithm.PREFIX);
    }

    /** A convenience method to create a string codec for
     * the constant names of the specified enumeration class.
     *
     * @param eClass the enumeration class
     * @param <E> constraint that the class must extend Enum
     * @return the codec
     */
    public static <E extends Enum<E>> StringCodec
                                    createCodecForEnumNames(Class<E> eClass) {
        Set<String> names = new HashSet<String>();
        for (E e: eClass.getEnumConstants())
            names.add(e.name());

        return new StringSetCodec(names, Algorithm.PREFIX);
    }

    /** A convenience method to create an entropic string codec
     * tailored for class names.
     *
     * @return the codec
     */
    public static ClassNameCodec createEntropicCodecForClassNames() {
        return new ClassNameCodec();
    }

    /** Creates and returns an EnumCodec for the specified enum class.
     *
     * @param eClass the enum class
     * @param <E> enum class constraint
     * @return the codec
     */
    public static <E extends Enum<E>> EnumCodec<E>
                                    createEnumCodec(Class<E> eClass) {
        return new EnumCodec<E>(eClass);
    }
}
