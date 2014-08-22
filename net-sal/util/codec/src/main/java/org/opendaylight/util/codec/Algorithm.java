/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.codec;

/** This enumeration declares the algorithms available for creating mappings
 * in the codecs.
 */
public enum Algorithm {
    /** This algorithm uses the first few characters of the original string.
     * Duplicates are disambiguated with an integer suffix.
     */
    PREFIX     ("p", 2),

    /** This algorithm reverses the order of the segments in a
     * dot-delimited string, then uses the prefix of the result.
     * Duplicates are disambiguated with an integer suffix.
     */
    CLASS_NAMES("n", 2),

//        CAMEL_HUMPS("c", 0),
//        UNDERSCORES("u", 0),
    ;

    private final String code;
    private final int defaultPreserve;

    Algorithm(String code, int defaultPreserve) {
        this.code = code;
        this.defaultPreserve = defaultPreserve;
    }

    /** Return the internal code for this algorithm constant.
     *
     * @return the internal code
     */
    @Override
    public String toString() { return code; }

    /** Return the default preserve value for this algorithm.
     *
     * @return the default preserve value
     */
    public int getDefaultPreserve() { return defaultPreserve; }
}
