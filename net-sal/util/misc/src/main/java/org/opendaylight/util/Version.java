/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;



/**
 * Version class is used to abstract a version string that can be compared.
 * 
 * @author Frank Wood
 */
public class Version implements Comparable<Version> {

    private static final String DEFAULT_SPLIT_DELIM = "\\.";
    private static final String ZERO = "";
    
    private String[] splitValue;
    private String value;

    /**
     * Constructor.
     */
    public Version() {
        value = ZERO;
        splitValue = new String[0];
    }
    
    /**
     * Constructor.
     * @param value version string
     */
    public Version(String value) {
        this(value, DEFAULT_SPLIT_DELIM);
    }
    
    /**
     * Constructor.
     * @param value version string
     * @param splitDelim regular expression delimiter; see {@link String#split(String)}
     */
    public Version(String value, String splitDelim) {
        this.value = value;
        String[] sa = value.split(splitDelim);
        splitValue = new String[sa.length];
        for (int i=0; i<sa.length; i++) {
            splitValue[i] = sa[i].trim().replaceAll("^0+(?!$)", "");
        }
    }

    @Override
    public String toString() {return value;}
    
    /**
     * Returns true if this version is greater than the passed in version.
     * @param targetVersion version in object form
     * @return true if the version of this instance is greater then the passed in version
     */
    public boolean isGreater(Version targetVersion) {
        return 0 < compareTo(targetVersion);
    }

    /**
     * Returns true if this version is greater than or equal the passed in version.
     * @param targetVersion version in object form
     * @return true if the version of this instance is greater then or equal the passed in version
     */
    public boolean isGreaterOrEqual(Version targetVersion) {
        return 0 <= compareTo(targetVersion);
    }

    /**
     * Returns true if this version is less than the passed in version.
     * @param targetVersion version in object form
     * @return true if the version of this instance is less then the passed in version
     */
    public boolean isLess(Version targetVersion) {
        return !isGreaterOrEqual(targetVersion);
    }

    /**
     * Returns true if this version is less than or equal the passed in version.
     * @param targetVersion version in object form
     * @return true if the version of this instance is less then or equal the passed in version
     */
    public boolean isLessOrEqual(Version targetVersion) {
        return !isGreater(targetVersion);
    }
    
    /**
     * Implement the comparable interface so the the extended device classes will be sorted by version (low to high).
     * @param o the object to be compared.
     * @return < 0 if this is less then o; 0 if this is equal to o; > 0 if this is greater than o
     */
    @Override
    public int compareTo(Version o) {
        if (null == o)
            return 1;
        int min = Math.min(splitValue.length, o.splitValue.length);
        for (int i=0; i<min; i++) {
            int cmp = splitValue[i].compareTo(o.splitValue[i]);
            if (0 != cmp)
                return cmp;
        }
        return splitValue.length - o.splitValue.length;
    }
    
    @Override
    public boolean equals(Object o) {
        return o instanceof Version && 0 == compareTo((Version) o);
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
