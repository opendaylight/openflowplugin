/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.codec;

/**
 * This class is a specialization of {@link EntropicStringSetCodec} that
 * is tailored specifically for mapping (fully qualified) class names.
 *
 * @see CodecFactory
 * @author Simon Hunt
 */
public class ClassNameCodec extends EntropicStringSetCodec {
    /**
     * Constructs an implementation devoid of mappings, and using
     * the CLASS_NAMES encoding algorithm.
     */
    ClassNameCodec() {
        super(null, Algorithm.CLASS_NAMES);
    }

    /** Convenience method to add the class name of the given class to
     * the mappings. null is silently ignored.
     * @param cls the class
     */
    public void add(Class<?> cls) {
        if (cls != null)
            add(cls.getName());
    }

    /** Convenience method to add the class name of the class of the
     * given object to the mappings.
     * null is silently ignored.
     * @param o the object instance whose class name should be added
     */
    public void add(Object o) {
        if (o != null)
            add(o.getClass().getName());
    }

    /** Convenience method to return the encoded value for the given class.
     *
     * @param cls the class to encode
     * @return the encoded value
     */
    public String encode(Class<?> cls) {
        if (cls == null)
            throw new NullPointerException("null parameter");
        return encode(cls.getName());
    }

    /** Convenience method to return the encoded value of the class of the
     * given object.
     *
     * @param o the object whose class should be encoded
     * @return the encoded value
     */
    public String encode(Object o) {
        if (o == null)
            throw new NullPointerException("null parameter");
        return encode(o.getClass().getName());
    }
}
