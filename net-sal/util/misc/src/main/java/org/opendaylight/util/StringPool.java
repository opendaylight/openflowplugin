/*
 * (c) Copyright 2009,2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util;

import java.util.HashMap;
import java.util.Map;

/**
 * This class canonicalizes strings.
 * Using a string pool can be helpful in reducing the amount of memory used when there are
 * many references to the same few string values.
 * <p>
 * A string pool instance is initialized with
 * a "limit" parameter at construction.
 * For a string pool {@code strPool} with a limit of {@code N},
 * and two strings {@code S1} and {@code S2};
 * {@code strPool.get(S1)} and
 * {@code strPool.get(S2)} are guaranteed to return a reference to the same string instance
 * iff {@code S1.equals(S2) &amp;&amp; S1.length() &lt;= N}.
 * <p>
 * Using {@code StringPool} is preferrable to using {@link String#intern}, which stores strings in
 * its internal cache permanently. {@code StringPool} implements a mechanism to "age-out"
 * entries over time.
 * <p>
 * (NOTE: the age-out mechanism hasn't been implemented yet, but it will be soon!)
 *
 * @author Simon Hunt
 */
public final class StringPool {

    /** The default limit - package private for unit test access */
    static final int DEFAULT_LIMIT = 64;

    /** Our pool entries */
    private final Map<String, String> myPool = new HashMap<String, String>();

    /** Our limit parameter */
    private final int limit;

    // TODO : define and implement age-out policy and parameters etc.
    //  (possibly use WeakHashMap ??)


    /** Constructs a string pool with the default string length limit of 64. */
    public StringPool() {
        this(DEFAULT_LIMIT);
    }

    /** Constructs a string pool with the specified string length limit.
     * Only strings of length {@code limit} or less will be canonicalized.
     *
     * @param limit the string length limit
     */
    public StringPool(int limit) {
        this.limit = limit;
    }

    /** Returns the canonicalized string from the pool, as long as the length of the string is
     * less than the configured limit for this string pool.
     * The string is simply returned as is, if it is longer than the limit.
     *
     * @param s the string
     * @return the canonicalized string
     */
    public String get(String s) {
        if (s == null || s.length() > limit) return s;

        synchronized (myPool) {
            String canon = myPool.get(s);
            if (canon == null) {
                canon = s;
                myPool.put(canon, canon);
            }
            return canon;
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("[StringPool limit='").append(limit)
                .append("' entries=").append(myPool.size()).append("]").toString();
    }


    //=== JUnit Support ===

    // package-private -- unit tests can get the pool ref
    Map<String, String> getPoolRef() { return myPool; }

    // package-private -- unit tests can get the limit
    int getLimit() { return limit; }
}
