/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.codec;

import org.opendaylight.util.EnumUtils;
import org.opendaylight.util.StringUtils;

import java.util.*;

import static org.opendaylight.util.StringUtils.EOL;

/**
 * This class takes a set of strings and provides an implementation of
 * {@link StringCodec} that encodes each given string to a shorter, but unique
 * alternate. Note that once the codec is instantiated it is immutable.
 * For a codec that allows you to add mappings after construction, use 
 * {@link EntropicStringSetCodec} instead.
 *
 * @see CodecFactory
 * @author Simon Hunt
 */
public class StringSetCodec implements StringCodec, Encodable {

    /* Implementation Note:
     *  At some future point, we may implement other algorithms that are
     *  tuned for specific string patterns, such as...
     *    CAMEL_HUMPS : pick the upper case letters from the string
     *                      (simple Class names)
     *    UNDERSCORES : pick the first letter after an underscore
     *                      (static constant names)
     *
     *  Please leave the commented out code checked in under
     *  source control for now.
     */

    private final Algorithm algorithm;
    private final int preserve;
    private final Map<String,String> toEnc;
    private final Map<String,String> fromEnc;
    private final Map<String, Integer> suffixes;

    /** Constructs an implementation using the given set of strings, and employing the
     * specified algorithm to generate the shorter, encoded values.
     *
     * @param originals the set of strings to create a mapping for
     * @param algorithm the algorithm to use
     */
    // package-private
    StringSetCodec(Set<String> originals, Algorithm algorithm) {
        this.algorithm = algorithm;
        this.preserve = algorithm.getDefaultPreserve();

        toEnc = new HashMap<String, String>();
        fromEnc = new HashMap<String, String>();
        suffixes = new HashMap<String, Integer>();

        switch (algorithm) {
//            case CAMEL_HUMPS:
//                throw new UnsupportedOperationException("Not Yet Implemented");
//
            case CLASS_NAMES:
                if (originals != null && originals.size() > 0)
                    generateClassNameMapping(originals);
                break;

            case PREFIX:
            default:
                if (originals != null && originals.size() > 0)
                    generatePrefixMapping(originals);
                break;

//            case UNDERSCORES:
//                throw new UnsupportedOperationException("Not Yet Implemented");
        }
    }

    /** private constructor for recreating an instance from an encoded string. Called from
     * {@link #valueOf}.
     *
     * @param algorithm the algorithm
     * @param preserve the preserve value
     * @param baseEnc base encodings
     * @param baseOrig base original strings
     * @param baseSfx base suffix values
     * @param resEnc residual encodings
     * @param resOrig residual original strings
     */
    private StringSetCodec(Algorithm algorithm, int preserve,
                          String[] baseEnc, String[] baseOrig,
                          String[] baseSfx, String[] resEnc, String[] resOrig) {
        this.algorithm = algorithm;
        this.preserve = preserve;
        toEnc = new HashMap<String, String>();
        fromEnc = new HashMap<String, String>();
        suffixes = new HashMap<String, Integer>();
        for (int i=0; i<baseEnc.length; i++) {
            fromEnc.put(baseEnc[i], baseOrig[i]);
            toEnc.put(baseOrig[i], baseEnc[i]);
            int sfx = StringUtils.parseInt(baseSfx[i], -1);
            if (sfx == -1)
                throw new IllegalArgumentException("Bad Suffix");
            suffixes.put(baseEnc[i], sfx);
        }
        for (int i=0; i<resEnc.length; i++) {
            fromEnc.put(resEnc[i], resOrig[i]);
            toEnc.put(resOrig[i], resEnc[i]);
        }
    }

    /** Create the mapping using the PREFIX algorithm.
     * <p>
     * This algorithm iterates across the set of strings and preserves just
     * the first <em>N</em> characters of each string (default 2); or the
     * whole string if its length is less than <em>N</em>. If a duplicate
     * prefix is found, an integer suffix is appended to distinguish between
     * the matching prefixes.
     *
     * @param originals the set of strings to create a mapping for
     */
    private void generatePrefixMapping(Set<String> originals) {
        for (String orig: originals)
            addPrefixMapping(orig);
        assert toEnc.size() == originals.size();
        assert fromEnc.size() == originals.size();
    }

    /** Create the mapping using the CLASS_NAMES algorithm.
     * <p>
     * For each string in the set, this algorithm first transforms it by
     * reversing the segments, then preserves just the first <em>N</em>
     * characters of each resulting string (default 2); or the whole string
     * if its length is less than <em>N</em>. If a duplicate prefix is found,
     * an integer suffix is appended to distinguish between the matching
     * prefixes.
     *
     * @param originals the set of strings to create a mapping for
     */
    private void generateClassNameMapping(Set<String> originals) {
        for (String orig: originals)
            addClassNameMapping(orig);
        assert toEnc.size() == originals.size();
        assert fromEnc.size() == originals.size();
    }

    /** Adds a new mapping for the given string.
     *
     * @param original the original string
     */
    // package-private
    void addMapping(String original) {
        // silently ignore repeat mappings
        if (toEnc.get(original) != null) return; 

        switch (algorithm) {

//            case CAMEL_HUMPS:
//                throw new UnsupportedOperationException("Not Yet Implemented");
//
            case CLASS_NAMES:
                addClassNameMapping(original);
                break;

            case PREFIX:
            default:
                addPrefixMapping(original);
                break;

//            case UNDERSCORES:
//                throw new UnsupportedOperationException("Not Yet Implemented");
        }
    }

    /** Adds a mapping using the PREFIX algorithm.
     *
     * @param orig the original string to map
     */
    private void addPrefixMapping(String orig) {
        if (orig == null)
            throw new NullPointerException("cannot map null");

        String result = doPrefixMapAlgo(orig);
        toEnc.put(orig, result);
        fromEnc.put(result, orig);
    }

    /** Adds a mapping using the CLASS_NAME algorithm.
     *
     * @param orig the original string to map
     */
    private void addClassNameMapping(String orig) {
        if (orig == null)
            throw new NullPointerException("cannot map null");

        String result = doPrefixMapAlgo(reverse(orig));
        toEnc.put(orig, result);
        fromEnc.put(result, orig);
    }

    // helper method to create the code from the first few chars of the string
    private String doPrefixMapAlgo(String str) {
        StringBuilder coded = new StringBuilder();
        String pre = str.length() < preserve ? str : str.substring(0, preserve);
        coded.append(pre);
        Integer suffix = suffixes.get(pre);
        if (suffix == null) {
            // never seen this prefix before
            suffixes.put(pre, 0);
        } else {
            Integer newSuffix = suffix + 1;
            suffixes.put(pre, newSuffix);
            coded.append(newSuffix);
        }
        return coded.toString();
    }

    private static final String EMPTY_SEGS = "dot-delimited : empty segments";

    // helper method to reverse the segments in a dot-delimited string
    // package-private for unit test access
    static String reverse(String orig) {
        if (!orig.contains(DOT))
            return orig;

        String[] segments = orig.split("\\.");
        final int n = segments.length;
        if (n==0)
            throw new IllegalArgumentException(EMPTY_SEGS + ": ["+orig+"]");
        StringBuilder sb = new StringBuilder();
        sb.append(segments[n-1]);
        for (int i=n-2; i>=0; i--) {
            sb.append(DOT).append(segments[i]);
        }
        if (sb.length() != orig.length())
            throw new IllegalArgumentException(EMPTY_SEGS + "??: ["+orig+"]");
        return sb.toString();
    }

    private static final String DOT = ".";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringSetCodec other = (StringSetCodec) o;
        return preserve == other.preserve &&
               algorithm == other.algorithm &&
               toEnc.equals(other.toEnc) &&
               fromEnc.equals(other.fromEnc) &&
               suffixes.equals(other.suffixes);
    }

    @Override
    public int hashCode() {
        int result;
        result = algorithm.hashCode();
        result = 31 * result + preserve;
        result = 31 * result + toEnc.hashCode();
        result = 31 * result + fromEnc.hashCode();
        result = 31 * result + suffixes.hashCode();
        return result;
    }

//=== Public API ===

    /**
     * Encodes an original string.
     *
     * @param str the original string
     * @return the encoded value
     * @throws NullPointerException if the parameter is null
     * @throws IllegalArgumentException if the original string is not a
     *          member of the initializing set
     */
    @Override
    public String encode(String str) {
        if (str == null)
            throw new NullPointerException("string cannot be null");
        if (!toEnc.keySet().contains(str))
            throw new IllegalArgumentException("This implementation cannot encode '" + str + "'");
        return toEnc.get(str);
    }

    /**
     * Decodes an encoded value.
     *
     * @param enc the encoded value
     * @return the original string
     * @throws NullPointerException if the parameter is null
     * @throws IllegalArgumentException if the encoded value is not a member of
     *      the encodings for the initializing set
     */
    @Override
    public String decode(String enc) {
        if (enc == null)
            throw new NullPointerException("encoding cannot be null");
        if (!fromEnc.keySet().contains(enc))
            throw new IllegalArgumentException("This implementation cannot decode '" + enc + "'");
        return fromEnc.get(enc);
    }

    @Override
    public int size() {
        return toEnc.size();
    }

    /** Returns an encodement of this implementation. Essentially, this
     * is a <em>blueprint</em> that can be stored, and later used to
     * reconstruct this specific implementation of the codec.
     *
     * @return the blueprint of this codec instance
     */
    @Override
    public String toEncodedString() {
        List<String> pieces = new ArrayList<String>();
        addBlueprintHeader(pieces);
        addBlueprintBase(pieces);
        addBlueprintResidual(pieces);
        return CodecUtils.encodeStringList(pieces);
    }

    @Override
    public String toString() {
        return new StringBuilder("[").append(getClass().getSimpleName())
                .append(": ")
                .append(algorithm.name()).append("/").append(preserve)
                .append(", base=").append(suffixes.size())
                .append(", residual=").append(toEnc.size()-suffixes.size())
                .append("]").toString();
    }

    /** Provides a multi-line string representation of the codec useful for
     * debugging purposes. It shows all the mappings.
     *
     * @return a multi-line string representation
     */
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString()).append(EOL);
        for (Map.Entry<String, String> e: toEnc.entrySet()) {
            sb.append("  ").append(e.getKey()).append(" <=> ")
                    .append(e.getValue()).append(EOL);
        }
        sb.append("---");
        return sb.toString();
    }

    private String intAsStr(int i) {
        return Integer.valueOf(i).toString();
    }

    private void addBlueprintHeader(List<String> pieces) {
        pieces.add(MAGIC);
        pieces.add(algorithm.toString());
        pieces.add(intAsStr(preserve));
        pieces.add(intAsStr(suffixes.size()));
    }

    private void addBlueprintBase(List<String> pieces) {
        for (String s: suffixes.keySet()) {
            pieces.add(s);
            pieces.add(fromEnc.get(s));
            pieces.add(intAsStr(suffixes.get(s)));
        }
    }

    private void addBlueprintResidual(List<String> pieces) {
        Set<String> residuals = new HashSet<String>(fromEnc.keySet());
        residuals.removeAll(suffixes.keySet());
        for (String s: residuals) {
            pieces.add(s);
            pieces.add(fromEnc.get(s));
        }
    }

    /** Recreates an instance of StringSetCodec from the supplied blueprint.
     * Essentially, the blueprint is a <em>Memento</em> that was previously
     * generated via {@link #toEncodedString()}.
     *
     * @param blueprint the blueprint
     * @return the corresponding StringSetCodec implementation
     * @throws NullPointerException if blueprint is null
     * @throws IllegalArgumentException if blueprint if badly formed
     */
    public static StringSetCodec valueOf(String blueprint) {
        if (blueprint == null)
            throw new NullPointerException("blueprint cannot be null");

        String[] pieces = CodecUtils.decodeStringArray(blueprint);
        int ptr = 0;
        String magic = pieces[ptr++];

        if (!magic.equals(MAGIC))
            throw new IllegalArgumentException("Bad Magic");

        Algorithm alg = EnumUtils.getEnum(Algorithm.class, pieces[ptr++]);
        int preserve = StringUtils.parseInt(pieces[ptr++], -1);
        if (preserve == -1)
            throw new IllegalArgumentException("Bad Preserve");

        int baseSize = StringUtils.parseInt(pieces[ptr++], -1);
        if (baseSize < 1)
            throw new IllegalArgumentException("Bad Base Size");

        if ((pieces.length - ptr) < baseSize * 3)
            throw new IllegalArgumentException("Insufficient base data");

        String[] baseEnc = new String[baseSize];
        String[] baseOrig = new String[baseSize];
        String[] baseSfx = new String[baseSize];
        for (int i=0; i<baseSize; i++) {
            baseEnc[i] = pieces[ptr++];
            baseOrig[i] = pieces[ptr++];
            baseSfx[i] = pieces[ptr++];
        }

        int residualBlock = pieces.length - ptr;
        if (residualBlock % 2 != 0)
            throw new IllegalArgumentException("Bad residuals");
        int resSize = residualBlock / 2;
        String[] resEnc = new String[resSize];
        String[] resOrig = new String[resSize];
        int i = 0;
        while(ptr < pieces.length) {
            resEnc[i] = pieces[ptr++];
            resOrig[i] = pieces[ptr++];
            i++;
        }
        return new StringSetCodec(alg, preserve, baseEnc, baseOrig, baseSfx,
                                    resEnc, resOrig);
    }

    //=== Blueprint ====

    private static final String MAGIC = "ssc1";


    //=== Algorithm enumeration ====


    //=== JUNIT SUPPORT ===
    Map<String,String> getToEncMapRef() { return toEnc; }
    Map<String,String> getFromEncMapRef() { return fromEnc; }
    Algorithm getAlgorithm() { return algorithm; }

}
