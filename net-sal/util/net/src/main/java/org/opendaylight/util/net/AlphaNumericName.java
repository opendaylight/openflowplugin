/*
 * (c) Copyright 2009-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.opendaylight.util.cache.WeakValueCache;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;

/**
 * Represents an alpha-numeric name.
 * <p>
 * All constructors for this class are private. Creating instances of
 * {@code AlphaNumericName} is done via the static methods on the class.
 * <p>
 * Instances of this class are immutable, making them inherently threadsafe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that instances
 * play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that a
 * sorted list of names is presented in an intuitive order. In particular, case
 * is ignored, and numeric suffixes are taken into account. For example:
 * <ul>
 * <li> {@code A1} </li>
 * <li> {@code A2} </li>
 * <li> {@code A3} </li>
 * <li> {@code A11} </li>
 * <li> {@code A12} </li>
 * <li> {@code A20} </li>
 * <li> {@code B1} </li>
 * <li> {@code B5} </li>
 * <li> {@code B12} </li>
 * </ul>
 *
 * @author Simon Hunt
 */
public final class AlphaNumericName extends NumericEmbeddedString
                        implements Comparable<AlphaNumericName> {

    private static final long serialVersionUID = 3580350903927725565L;
    
    private static final String E_NULL = "Argument cannot be null";
    private static final String E_BLANK = "Argument cannot be blank";

    /**
     * The name.
     *
     * @serial name
     */
    private final String str;

    // our string broken out into couplets
    private transient Couplet[] couplets;


    private AlphaNumericName(String str) {
        if (str == null)
            throw new NullPointerException(E_NULL);

        String s = str.trim();

        if (s.length() == 0)
            throw new IllegalArgumentException(E_BLANK);

        this.str = s;
        couplets = createCoupletArray(s);
        assert couplets.length > 0;
    }

    // === PRIVATE serialization ==============================================

    //== Implementation note:
    //      Default deserialization will set the serialized field (str) to
    //      whatever was in the stream, and will set the transient fields to
    //      defaults (null). This is good enough for our purposes, because
    //      we will simply use valueOf() to (create and?) return the
    //      cached instance.


    private Object readResolve() throws ObjectStreamException {
        // when this is called, str has been populated.
        // return the appropriate cached instance instead.
        Object o;
        try {
            o = valueOf(str);
        } catch (NullPointerException e) {
            throw new InvalidObjectException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new InvalidObjectException(e.getMessage());
        }
        return o;
    }


    @Override
    public String toString() {
        return str;
    }


    /** 
     * Overridden to (a) ignore case, and (b) take embedded numbers into
     * account.
     *
     * @param other the other instance we are comparing to
     * @return a number less than, equal to, or greater than zero as this
     *      instance is earlier than, the same as, or later than the other in
     *      natural sort order
     */
    @Override
    public int compareTo(AlphaNumericName other) {
        int result = 0;
        boolean done = false;
        int ci = 0;  // couplet index
        while (result == 0 && !done) {
            if (ci < couplets.length && ci < other.couplets.length) {
                result = couplets[ci].compareTo(other.couplets[ci]);
                ci++; // tee up the next pair of couplets
            } else {
                // ran off the end of an array
                result = couplets.length - other.couplets.length;
                done = true;
            }
        }
        return result;
    }

    /** 
     * {@inheritDoc}
     * <p>
     * Note that this comparison ignores case.
     *
     * @param o the other instance to compare to
     * @return true if this instance is equivalent to the specified instance
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return str.equalsIgnoreCase(((AlphaNumericName) o).str);
    }

    @Override
    public int hashCode() {
        return str.hashCode();
    }

    /** Our self-trimming cache. */
    private static final WeakValueCache<String, AlphaNumericName> cachedNames =
                     new WeakValueCache<String, AlphaNumericName>(getRefQ());

    /** 
     * Returns an object representing the value of the specified alpha-numeric
     * name. Note that the parameter to this method is trimmed of whitespace.
     *
     * @param s the string representation of the alpha-numeric name
     * @return an AlphaNumericName instance
     * @throws NullPointerException if the string is null
     * @throws IllegalArgumentException if the string is blank
     */
    public static AlphaNumericName valueOf(String s) {
        synchronized (cachedNames) {
            AlphaNumericName pn = cachedNames.get(s);
            if (pn == null) {
                pn = new AlphaNumericName(s);
                cachedNames.put(s, pn);
            }
            return pn;
        }
    }

    /**
     * Convenience method that simply delegates to {@link #valueOf(String)}.
     * Note that code can be written more concisely by using a static import 
     * of this method; for example, the following two statements are
     * equivalent:
     * <pre>
     * AlphaNumericName a = AlphaNumericName.valueOf("foo25");
     * AlphaNumericName a = ann("foo25");
     * </pre>
     *
     * @param s the string representation of the alpha-numeric name
     * @return an AlphaNumericName instance
     * @throws NullPointerException if the string is null
     * @throws IllegalArgumentException if the string is blank
     */
    public static AlphaNumericName ann(String s) {
        return valueOf(s);
    }

    // TODO: public static final AlphaNumericName NONE ...
}
