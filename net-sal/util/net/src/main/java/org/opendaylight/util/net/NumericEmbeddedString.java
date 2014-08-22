/*
 * (c) Copyright 2009-2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.opendaylight.util.StringPool;
import org.opendaylight.util.cache.CacheableDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Forms the basis for datatypes that represent strings with (potentially)
 * embedded numeric values. This is to support a more natural ordering of
 * elements, based on perceived numeric value. For example:
 * <ul>
 * <li> aaa1xxx1 </li>
 * <li> aaa1xxx2 </li>
 * <li> aaa1xxx4 </li>
 * <li> aaa1xxx16 </li>
 * <li> aaa1xxx32 </li>
 * <li> aaa2xxx5 </li>
 * <li> aaa11xxx7 </li>
 * <li> aaa12xxx7 </li>
 * <li> aaa12xxx42 </li>
 * <li> aaa55xxx7 </li>
 * </ul>
 * <p>
 * This class provides a package-private inner class {@link Couplet} which
 * represents pieces of the string broken up into (non-numeric, numeric) pairs.
 *
 * @author Simon Hunt
 */
abstract class NumericEmbeddedString extends CacheableDataType {

    private static final long serialVersionUID = 4249516395755037680L;

    /* Implementation Note:
     *  This super class contains no instance 'state', and therefore does
     *  not need to declare that it implements Serializable.
     */

    // RE that matches zero or more non-digits followed by zero or more digits
    private static final String RE_COUPLET_STR = "(\\D*)(\\d*)";
    private static final Pattern RE_COUPLET = Pattern.compile(RE_COUPLET_STR);

    /** Constant designating no numeric suffix */
    private static final int NO_SUFFIX = -1;

    /** Pool our common strings */
    private static final StringPool SP = new StringPool();

    private static final Map<String, Couplet[]> cachedArrays =
            new HashMap<String, Couplet[]>();


    /** Parses a string into (non-numeric,numeric) couplets
     *
     * @param piece the string to parse
     * @return an array of couplets, one for each
     *          [non-numeric prefix, numeric suffix] pair
     */
    Couplet[] createCoupletArray(String piece) {
        Couplet[] array;
        synchronized (cachedArrays) {
            array = cachedArrays.get(piece);
            if (array == null) {
                List<Couplet> tokens = new ArrayList<Couplet>();
                Matcher m = RE_COUPLET.matcher(piece);

                while((m.find())) {
                    String prefix = m.group(1);
                    String number = m.group(2);
                    int nVal = number.length() > 0 ?
                            Integer.parseInt(number) : NO_SUFFIX;
                    // note, because of repeated greedy matching, the last
                    // match will always be ("")("")
                    if (prefix.length() > 0 || nVal != NO_SUFFIX) {
                        tokens.add(getCouplet(m.group(0), prefix, nVal));
                    }
                }
                array = tokens.toArray(new Couplet[tokens.size()]);
                cachedArrays.put(piece, array);
            }
        } // sync
        return array;
    }

    //=== INNER CLASS: Couplet ================================================

    /** Embodies a substring composed of a non-numeric prefix and a
     * positive integer suffix.
     * <p>
     * The Comparable interface considers the prefixes first, and only if
     * they are equal will it compare the numeric suffixes.
     */
    // package private
    static class Couplet implements Comparable<Couplet> {
        private final String prefix;
        private final int suffix;

        private Couplet(String prefix, int suffix) {
            this.prefix = SP.get(prefix);
            this.suffix = suffix;
        }

        @Override
        public String toString() {
            return "(" + prefix + "," + suffix + ")";
        }

        @Override
        public int compareTo(Couplet o) {
            // compare string prefixes first, and only if they are equal
            // use the numeric suffix as a tie-breaker
            int result = prefix.compareToIgnoreCase(o.prefix);
            return result != 0 ? result : suffix - o.suffix;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Couplet couplet = (Couplet) o;

            if (suffix != couplet.suffix) return false;
            return !(prefix != null ? !prefix.equals(couplet.prefix)
                                    : couplet.prefix != null);
        }

        @Override
        public int hashCode() {
            int result;
            result = (prefix != null ? prefix.hashCode() : 0);
            result = 31 * result + suffix;
            return result;
        }
    }


    /** Cache our couplets, for efficiency. */
    private static final Map<String, Couplet> cachedCouplets =
            new HashMap<String, Couplet>();

    /** Return cached instances of couplets where possible.
     *
     * @param key the cache map key
     * @param prefix the couplet prefix
     * @param suffix the couplet suffix
     * @return a couplet instance
     */
    private static Couplet getCouplet(String key, String prefix, int suffix) {
        Couplet c = cachedCouplets.get(key);
        if (c == null) {
            c = new Couplet(prefix, suffix);
            cachedCouplets.put(key, c);
        }
        return c;
    }

}
