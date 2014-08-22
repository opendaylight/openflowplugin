/*
 * (c) Copyright 2009-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.opendaylight.util.StringPool;
import org.opendaylight.util.cache.WeakValueCache;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static org.opendaylight.util.ResourceUtils.getBundledResource;

/**
 * Represents a DNS name.
 * <p>
 * All constructors for this class are private. Creating instances
 * of {@code DnsName} is done via the static {@link #valueOf} methods on
 * the class. Note that a special value representing "Unresolvable" can be
 * obtained via the {@link #UNRESOLVABLE} public field.
 * <p>
 * Instances of this class are immutable, making them inherently threadsafe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that
 * instances play nicely with the Collection classes.
 * <p>
 * Implements the {@link Comparable} interface to ensure that a sorted list
 * of DnsNames is presented in an intuitive order. In particular, case is
 * ignored, and numeric suffixes are taken into account. Also note that
 * "Unresolvable" instances are sorted to the end of the list.
 *
 * For example:
 * <ul>
 * <li> {@code switch1.cup.hp.com} </li>
 * <li> {@code switch1.rose.hp.com} </li>
 * <li> {@code switch3.cup.hp.com} </li>
 * <li> {@code switch3.rose.hp.com} </li>
 * <li> {@code switch4.rose.hp.com} </li>
 * <li> {@code switch13.rose.hp.com} </li>
 * <li> {@code switch14.rose.hp.com} </li>
 * <li> {@code switch22.rose.hp.com} </li>
 * <li> {@code switch23.rose.hp.com} </li>
 * <li> {@code zodiac.rose.hp.com} </li>
 * <li> {@code (Unresolvable DNS Name)} </li>
 * <li> {@code (Unresolvable DNS Name)} </li>
 * </ul>
 *
 * @author Simon Hunt
 */
public final class DnsName extends NumericEmbeddedString
                           implements Comparable<DnsName> {

    private static final long serialVersionUID = 4945600293707243316L;


    private static final String E_NULL = "Argument cannot be null";
    private static final String E_BLANK = "Argument cannot be blank";

    private static final ResourceBundle RES =
            getBundledResource(DnsName.class, "dnsName");

    private static final String UNRES_STR = RES.getString("unresolvable");
    private static final String UNHOST_STR = RES.getString("unknownHost");
    private static final String UNDOMAIN_STR = RES.getString("unknownDomain");

    private static final StringPool SP = new StringPool();

    // RegExp that matches '.'
    private static final Pattern RE_DOT = Pattern.compile("\\.");

    /** 
     * Fully qualified domain name, or null if this is the unresolvable
     * instance.
     * @serial fully qualified name
     */
    private final String str;

    // the host name portion
    private transient String host;

    // the domain name portion
    private transient String domain;

    // the dot-delimited substrings broken out into couplets
    private transient Couplet[][] couplets;


    /** Constructs an instance for "Unresolvable DNS Name". */
    private DnsName () {
        str = null; // this is the ONLY way str can be null
        host = SP.get(UNHOST_STR);
        domain = SP.get(UNDOMAIN_STR);
        couplets = null;
    }

    /** 
     * Constructs a DNS name from the specified string.
     *
     * @param str the string representation
     * @throws NullPointerException if the string is null
     * @throws IllegalArgumentException if the string is blank
     */
    private DnsName (String str) {
        if (str == null)
            throw new NullPointerException(E_NULL);

        String s = str.trim();
        if (s.length() == 0)
            throw new IllegalArgumentException(E_BLANK);

        this.str = s;
        deriveValues();

        // post conditions: with no splits, we'll still have one element
        assert couplets.length > 0;
    }

    // Populates our derived values
    private void deriveValues() {
        // first, split the string using dot as the delimiter
        final String[] pieces = RE_DOT.split(str, -1);

        // store off the hostname and the domain
        host = SP.get(pieces[0]);
        domain = pieces.length > 1 ? SP.get(str.substring(host.length()+1)) : "";

        // then create a couplet array for each piece
        couplets = new Couplet[pieces.length][];
        for (int i=0; i<couplets.length; i++) 
            couplets[i] = createCoupletArray(pieces[i]);
    }

    //== Implementation note:
    //      Default deserialization will set the serialized field (str) to
    //      whatever was in the stream, and will set the transient fields to
    //      defaults (null). This is good enough for our purposes, because we
    //      will simply use valueOf() to (create and?) return the cached
    //      instance. If str is null, it is the unresolvable instance.

    private Object readResolve() throws ObjectStreamException {
        // when this is called, str has been populated.
        // return the appropriate cached instance instead.
        Object o;
        try {
            o = str==null ? UNRESOLVABLE : valueOf(str);
        } catch (NullPointerException e) {
            throw new InvalidObjectException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new InvalidObjectException(e.getMessage());
        }
        return o;
    }

    /** 
     * Returns the host name; that is, everything up to (but not including)
     * the first dot.
     *
     * @return the host name portion
     */
    public String getHostName() {
        return host;
    }

    /** 
     * Returns the domain name; that is, everything after the first dot.
     *
     * @return the domain name portion
     */
    public String getDomainName() {
        return domain;
    }

    @Override
    public String toString() {
        return str == null ? UNRES_STR : str;
    }

    /** 
     * Returns true if this instance is the "unresolvable" instance.
     *
     * @return true if this is the "unresolvable" instance
     */
    public boolean isUnresolvable() {
        return this == UNRESOLVABLE;
    }

    /** 
     * Overridden to (a) ignore case, and (b) take embedded numbers into
     * account. Note that {@link #UNRESOLVABLE} instances will sort to the end.
     *
     * @param other the other instance we are comparing to
     * @return a number less than, equal to, or greater than zero as this
     * instance is earlier than, the same as, or later than the other in
     * natural sort order
     */
    @Override
    public int compareTo(DnsName other) {
        // Special handling of the "unresolvable" instance
        if (this == UNRESOLVABLE) {
            return other == UNRESOLVABLE ? 0 : 1;
        } else if (other == UNRESOLVABLE) {
            // this is NOT unresolvable .. put it before
            return -1;
        }

        /* TODO: consider changing this to sort "from the right"
         * that is, rather than sorting like this:
         *   switch1.cup.hp.com
         *   switch1.rose.hp.com
         *   switch3.cup.hp.com
         *   switch3.rose.hp.com
         *   switch4.rose.hp.com
         *
         *   isn't a more natural order this: ?
         *   switch1.cup.hp.com
         *   switch3.cup.hp.com
         *   switch1.rose.hp.com
         *   switch3.rose.hp.com
         *   switch4.rose.hp.com
         */

        int result = 0;
        boolean done = false;
        int si = 0; // substring index
        while (result == 0 && !done) {
            if (si < couplets.length && si < other.couplets.length) {
                // === INNER COMPARE ===
                int innerResult = 0;
                boolean innerDone = false;
                int ci = 0; // couplet index
                while (innerResult == 0 && !innerDone) {
                    if (ci < couplets[si].length &&
                            ci < other.couplets[si].length) {
                        innerResult =
                            couplets[si][ci].compareTo(other.couplets[si][ci]);
                        ci++; // tee up the next pair of couplets
                    } else {
                        // ran off the end of an array
                        innerResult = couplets[si].length -
                                      other.couplets[si].length;
                        innerDone = true;
                    }
                }
                result = innerResult;
                si++; // tee up the next pair of arrays
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

        return (this == UNRESOLVABLE) ? (o == UNRESOLVABLE)
                                      : str.equalsIgnoreCase(((DnsName)o).str);
    }

    @Override
    public int hashCode() {
        return str==null ? 0 : str.hashCode();
    }


    // TODO: add DnsName.NONE
    
    /** Our instance that represents an unresolvable DNS name. */
    public static final DnsName UNRESOLVABLE = new DnsName();

    /** Our self-trimming cache. */
    private static final WeakValueCache<String, DnsName> cachedDnsNames =
            new WeakValueCache<String, DnsName>(getRefQ());


    /** 
     * Returns an object that represents the value of the DNS name identified
     * by the specified string. Note that the string is trimmed of
     * whitespace first.
     * <p>
     * If the parameter is an empty string ({@code ""}) then the instance
     * representing "unresolvable DNS name" is returned instead.
     *
     * @param s the string representation of the DNS name
     * @return an object representing the specified DNS name
     * @throws NullPointerException if the string is null
     */
    public static DnsName valueOf(String s) {
        if (s != null && s.trim().isEmpty()) return UNRESOLVABLE;

        synchronized (cachedDnsNames) {
            DnsName dns = cachedDnsNames.get(s);
            if (dns == null) {
                dns = new DnsName(s);
                cachedDnsNames.put(s, dns);
            }
            return dns;
        }
    }

    /**
     * Convenience method that simply delegates to {@link #valueOf(String)}.
     * Note that code can be written more concisely by using a static import 
     * of this method; for example, the following two statements are
     * equivalent:
     * <pre>
     * DnsName a = DnsName.valueOf("foo.hp.com");
     * DnsName a = dns("foo.hp.com");
     * </pre>
     *
     * @param s the string representation of the DNS name
     * @return an object representing the specified DNS name
     * @throws NullPointerException if the string is null
     */
    public static DnsName dns(String s) {
        return valueOf(s);
    }
}
