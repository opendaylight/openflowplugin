/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * Set of utilities for composing and parsing resource identifiers.
 *
 * @author Thomas Vachuska
 */
public class ResourceIdUtils {
    
    private ResourceIdUtils() {}

    /**
     * Substitutes the specified value into the supplied rid pattern.
     * 
     * @param value value to be substituted
     * @param token name of the segment
     * @param rid pattern identifying the resource id path structure
     * @return rid pattern with the given value incorporated into the named
     *         path segment
     */
    public static String replace(String value, String token, String rid) {
        return rid.replaceFirst("\\{" + token + "\\}", value);
    }

    /**
     * Extracts the specified segment from a resource identifier using the 
     * given pattern.
     * <p>
     * Some examples of usage are as follows:
     * <ul>
     * <li>extract("/book/I123818172", "isbn", /book/{author}/{isbn}") yields "I123818172"
     * <li>extract("/dev/group/switches/alpha", "gid", "/dev/group/{gid}/{uid}") yields "switches"
     * <li>...
     * </ul>
     * 
     * @param rid resource identifier to be parsed
     * @param token name of the token to be extracted
     * @param pattern pattern identifying segments of interest
     * @return segment of the resource id, identified by the token and the
     *         pattern; null if there is no matching token
     */
    public static String extract(String rid, String token, String pattern) {
        // Find the token in the pattern first.
        String tokenId = "{" + token + "}"; 
        int ti = pattern.indexOf(tokenId);
        
        // If we can't locate the segment token, bail out.
        if (ti < 0)
            return null;
        
        // Verify the fixed portions of the pattern against the rid
        if (!verify(rid, pattern))
            return null;
        
        // Otherwise, determine the number of the segment by counting the '/'
        // characters.
        int si = 0;
        for (int i = 0; i < ti; i++)
            if (pattern.charAt(i) == '/')
                si++;
        
        // Now split the rid into segments and return the appropriate one.
        String segments[] = rid.split("/");
        return si < segments.length ? segments[si] : null;
    }
    
    
    /**
     * Extracts the leaf portion of the specified resource id, i.e. the last
     * URL segment.
     * 
     * @param rid resource id
     * @return leaf segment of the resource id
     */
    public static String leaf(String rid) {
        return FilePathUtils.leafName(rid, '/');
    }

    /**
     * Extracts the parent portion of the specified resource id, i.e. the
     * resource id without the last URL segment.
     * 
     * @param rid resource id
     * @return parent resource id
     */
    public static String parent(String rid) {
        return FilePathUtils.parentPath(rid, '/');
    }

    /**
     * Verified the specified rid against the fixed portions of the pattern.
     * 
     * @param rid resource id
     * @param pattern resource id segment pattern
     * @return true if the rid matches the pattern
     */
    private static boolean verify(String rid, String pattern) {
        // Convert the '{...} tokens to ERE '[^/]+' tokens.
        String ere = pattern.replaceAll("\\{[^}]+\\}", "[^/]+");
        return rid.matches(ere);
    }

}
