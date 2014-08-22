/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.codec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class defines methods to assist in the encodement and decodement of objects to
 * strings and back.
 * <p>
 * Although it would be preferable to hide the implementation details of how string arrays
 * (and lists) are encoded, we have determined cases where it is important for the consumer
 * to be able to make assumptions about the delimiters used. Therefore, a brief
 * description of the algorithm is given here:
 * <p>
 * The method {@link #encodeStringArray encodeStringArray} (and its counterpart,
 * {@link #encodeStringList encodeStringList} which simply delegates to this method),
 * examines the strings in the array and attempts to find a delimiter which does not
 * occur in the data. If one can be found, that one is used, avoiding the need to
 * escape occurrences of the delimiter in the data.
 * If <em>all</em> considered delimiters are found in the data, then a deeper examination
 * is made and the one that occurs the least number of times is used, and
 * escaped as appropriate.
 * <p>
 * If the caller knows that the string data does not contain the
 * {@link #getDefaultDelimiter default delimiter}, then the
 * assumption can be made that this will be the delimiter used. 
 */
public final class CodecUtils {

    static final String MAGIC = "cu2";     // codec utils version 2

    private static final String ESCAPE = "@";      // escape
    private static final String ESC_ESCAPE = "@@"; // escape escaped
    private static final Pattern RE_END_ESC = Pattern.compile("(@+)$");

    // no instantiation
    private CodecUtils() { }

    /** Enumeration of delimiters that we use */
    // package private for junit access
    static enum Delim {
        COMMA(","),
        TILDE("~"),
        SEMIC(";"),
        COLON(":"),
        EXCLA("!");

        private final String delim;
        private final String escDelim;
        Delim(String d) {
            this.delim = d;
            this.escDelim = ESCAPE + delim;
        }
        @Override public String toString() { return delim; }
        public String toEscString() { return escDelim; }

        public static Delim match(String ch) {
            for (Delim d: values()) {
                if (d.delim.equals(ch)) return d;
            }
            throw new IllegalArgumentException("unknown delimiter");
        }
    }

    /** Returns the default delimiter used when encoding string arrays and lists.
     *
     * @return the default delimiter
     */
    public static String getDefaultDelimiter() { return Delim.values()[0].toString(); }


    /**
     * Returns the best delimiter to use, based on no occurrence in the data,
     * or the smallest count of occurrences in the data.
     * 
     * @param strings the data
     * @return the delimiter to use
     */
    // package private - so we can unit test
    static Delim chooseDelim(String[] strings) {
        Delim toUse = null;
        Map<Delim, Integer> hits = null;

        for (Delim d: Delim.values()) {
            int matched = findMatch(d.toString(), strings);
            if (matched == -1) {
                toUse = d;
                break;
            }
            if (hits == null)
                hits = new HashMap<Delim, Integer>();
            hits.put(d, matched);
        }

        if (toUse == null && hits != null) {
            // find least count
            int smallest = Integer.MAX_VALUE;
            for (Delim d: Delim.values()) {
                int occurs = countOccurrences(d.toString(), strings, hits.get(d));
                if (occurs < smallest) {
                    smallest = occurs;
                    toUse = d;
                }
            }
        }

        return toUse;
    }

    /** Find a string in the data that contains the delimiter character.
     *
     * @param ch the delimiter character to match
     * @param data the data
     * @return the index in the array of the match, or -1 for no match
     */
    // package private - so we can unit test
    static int findMatch(String ch, String[] data) {
        int ptr = 0;
        while (ptr < data.length) {
            if (data[ptr] != null && data[ptr].contains(ch))
                break;
            ptr++;
        }
        return (ptr < data.length) ? ptr : -1;
    }

    /** Count the number of occurrences of the given character in the data.
     *
     * @param ch the character
     * @param data the data to scan
     * @param firstIdx the first index that the character was originally matched in
     * @return the number of times it occurs in the data
     */
    // package private - so we can unit test
    static int countOccurrences(String ch, String[] data, int firstIdx) {
        int count = 0;
        int ptr = firstIdx;
        while (ptr < data.length) {
            int i = data[ptr].indexOf(ch);
            while (i > -1) {
                count++;
                i = (i < data[ptr].length()-1) ? data[ptr].indexOf(ch, i+1) : -1;
            }
            ptr++;
        }
        return count;
    }

    //======================================


    /** This method takes a list of strings and returns a single string that encodes
     *  that list. This method converts the list to an array then delegates
     * the call to {@link #encodeStringArray encodeStringArray}.
     * <p>
     * Note that any {@code null} elements in the list will be silently converted to
     * empty strings.
     *
     * @param strings a list of strings
     * @return a single string representing the specified list of strings
     * @throws NullPointerException if the parameter is null
     */
    public static String encodeStringList(List<String> strings) {
        return encodeStringArray(strings.toArray(new String[strings.size()]));
    }

    /** This method takes an array of strings and returns a single string that encodes
     *  that string array. The original array can be regenerated by passing the
     *  encoded string to {@link #decodeStringArray decodeStringArray}.
     * <p>
     * Note that any {@code null} elements in the array will be silently converted to
     * empty strings.
     *
     * @param strings an array of strings
     * @return a single string encodement of the array of strings
     * @throws NullPointerException if the parameter is null
     */
    public static String encodeStringArray(String[] strings) {
        if (strings == null)
            throw new NullPointerException("string array cannot be null");

        // pick a delimiter
        Delim delim = chooseDelim(strings);

        // first, the header
        StringBuilder sb = new StringBuilder(MAGIC).append(delim);
        sb.append(strings.length).append(delim);

        // then, the payload
        for (String s: strings) {
            if (s!= null) {
                sb.append(s.replace(ESCAPE, ESC_ESCAPE)
                           .replace(delim.toString(), delim.toEscString()));
            }
            sb.append(delim);
        }
        // implementation note: we choose to leave a trailing delimiter.
        //                      when we decode, the extra element will be dropped.
        return sb.toString();
    }

    /** This method takes a string generated by {@link #encodeStringList encodeStringList}
     * and regenerates the contents of the original list. This method
     * calls {@link #decodeStringArray decodeStringArray} then converts that array
     * back to a list before returning it.
     *
     * @param encoded the encoded string
     * @return the reconstituted list of strings
     * @throws NullPointerException if the parameter is null
     * @throws IllegalArgumentException if the format of the string is not recognized
     */
    public static List<String> decodeStringList(String encoded) {
        return Arrays.asList(decodeStringArray(encoded));
    }

    /** This method takes a string generated by {@link #encodeStringArray encodeStringArray}
     * and regenerates the contents of the original array.
     *
     * @param encoded the encoded string
     * @return the reconstituted array of strings
     * @throws NullPointerException if the parameter is null
     * @throws IllegalArgumentException if the format of the string is not recognized
     */
    public static String[] decodeStringArray(String encoded) {
        if (encoded == null)
            throw new NullPointerException("encoded string cannot be null");

        Delim delim = Delim.match(encoded.substring(encoded.length()-1));

        String pieces[] = encoded.split(delim.toString(), -1);
        if (pieces.length < 2 || !MAGIC.equals(pieces[0])) {
            String s = encoded.length() < 30 ? encoded : encoded.substring(0,27)+"...";
            throw new IllegalArgumentException("bad encoded string format: " + s);
        }

        final int numItems = Integer.valueOf(pieces[1]); // number of array items (from header)

        // we have to find and un-escape the delimiters that were part of the data
        // re-assembling the strings that were split by accident
        int i = 2;
        List<String> data = new ArrayList<String>(pieces.length - 2);
        StringBuilder sb = new StringBuilder();
        boolean flushNeeded = false;
        while (i<pieces.length-1) {     // last element (empty string) should be dropped
            sb.append(pieces[i]);
            flushNeeded = true;
            Matcher m = RE_END_ESC.matcher(pieces[i]);
            if (!m.find()) {
                // we're done with this string
                data.add(sb.toString());
                sb.setLength(0);
                flushNeeded = false;
            } else {
                // possibly an escaped delimiter
                if (m.group(1).length() % 2 == 0) {
                    // an even number of escapes, therefore not an escaped delimiter
                    // we're done with this string
                    data.add(sb.toString());
                    sb.setLength(0);
                    flushNeeded = false;
                } else {
                    sb.append(delim); // replace the delimiter
                }
            }
            i++; // don't forget to increment
        }
        if (flushNeeded) data.add(sb.toString());  // final flush

        if (data.size() != numItems) {
            // SHOULD never happen
            throw new RuntimeException("problems re-assembling escaped strings");
        }

        String[] result = new String[numItems];
        i = 0;
        for (String s: data) {
            result[i++] = s.replace(delim.toEscString(), delim.toString())
                           .replace(ESC_ESCAPE, ESCAPE);
        }
        return result;
    }

    /** Returns the magic value used to tag encoded string arrays and lists. The following
     * assertion may be made:
     * <pre>
     * String[] myStrings = ...
     * String encArray = CodecUtils.encodeStringArray(myStrings);
     * assert encArray.startsWith(CodecUtils.getMagic());
     * </pre>
     *
     * @return the magic value
     */
    public static String getMagic() { return MAGIC; }

    //========================================================================================
    //=== Encoding Primitives
    //========================================================================================


    private static final String ID_BOOLEAN = "B";
    private static final String ID_CHAR    = "C";
    private static final String ID_BYTE    = "Y";
    private static final String ID_SHORT   = "S";
    private static final String ID_INT     = "I";
    private static final String ID_LONG    = "L";
    private static final String ID_FLOAT   = "F";
    private static final String ID_DOUBLE  = "D";
    private static final String ID_STRING  = "G";

    private static final Map<Class<?>, String> primIdMap = new HashMap<Class<?>, String>(9);
    private static final Map<String, Class<?>> primClsMap = new HashMap<String, Class<?>>(9);

    static {
        primIdMap.put(Boolean.class,   ID_BOOLEAN);
        primIdMap.put(Character.class, ID_CHAR);
        primIdMap.put(Byte.class,      ID_BYTE);
        primIdMap.put(Short.class,     ID_SHORT);
        primIdMap.put(Integer.class,   ID_INT);
        primIdMap.put(Long.class,      ID_LONG);
        primIdMap.put(Float.class,     ID_FLOAT);
        primIdMap.put(Double.class,    ID_DOUBLE);
        primIdMap.put(String.class,    ID_STRING);

        primClsMap.put(ID_BOOLEAN, Boolean.class);
        primClsMap.put(ID_CHAR,    Character.class);
        primClsMap.put(ID_BYTE,    Byte.class);
        primClsMap.put(ID_SHORT,   Short.class);
        primClsMap.put(ID_INT,     Integer.class);
        primClsMap.put(ID_LONG,    Long.class);
        primClsMap.put(ID_FLOAT,   Float.class);
        primClsMap.put(ID_DOUBLE,  Double.class);
        primClsMap.put(ID_STRING,  String.class);
    }

    private static final String BOOL_TRUE = "T";
    private static final String BOOL_FALSE = "F";
    private static final String STRING_PREFIX = "_";

    /** This method accepts a primitive and produces a string encoding of it. This
     * encoding can be parsed by {@link #decodePrimitive decodePrimitive} to restore
     * the original value (as the original type).
     * <p>
     * As a convenience, a {@code String} is also considered a "primitive" and
     * can be "encoded" by this method.
     *
     * @param p the primitive value
     * @return a string encoding of the value
     * @throws NullPointerException if the parameter is null
     * @throws IllegalArgumentException if the parameter is not a
     *   String, Boolean, Character, Byte, Short, Integer, Long, Float, or Double
     */
    public static String encodePrimitive(Object p) {
        if (p == null)
            throw new NullPointerException("parameter cannot be null");

        final Class<?> cls = p.getClass();
        final String clsId = primIdMap.get(cls);
        if (clsId == null)
                throw new IllegalArgumentException("Class not a primitive: " + cls);

        StringBuilder sb = new StringBuilder(clsId);

        if (cls == Boolean.class) {
            sb.append((Boolean) p ? BOOL_TRUE : BOOL_FALSE);
        } else if (cls == String.class) {
            sb.append(STRING_PREFIX).append(p);
        } else {
            sb.append(p);
        }
        return sb.toString();
    }

    /** This method accepts an encoded string (previously generated from
     * {@link #encodePrimitive encodePrimitive}) and returns the original
     * value (as the original type).
     *
     * @param encoded the encoded string
     * @return the (wrapped) primitive value that the encoded string represents
     * @throws NullPointerException if the parameter is null
     * @throws IllegalArgumentException if the encoding is not recognized
     */
    public static Object decodePrimitive(String encoded) {
        if (encoded == null)
            throw new NullPointerException("Parameter cannot be null");

        if (encoded.length() < 2)
            throw new IllegalArgumentException("Unrecognized encoding (too short): " + encoded);

        final String clsId = encoded.substring(0,1);
        final String valueStr = encoded.substring(1);
        final Class<?> cls = primClsMap.get(clsId);
        if (cls == null)
            throw new IllegalArgumentException("Unrecognized encoding (primitive ID): " + encoded);

        Object result = null;
        if (clsId.equals(ID_BOOLEAN)) {
            result = BOOL_TRUE.equals(valueStr) ? Boolean.TRUE : Boolean.FALSE;
        } else if (clsId.equals(ID_CHAR)) {
            result = valueStr.charAt(0);
        } else if (clsId.equals(ID_BYTE)) {
            result = Byte.valueOf(valueStr);
        } else if (clsId.equals(ID_SHORT)) {
            result = Short.valueOf(valueStr);
        } else if (clsId.equals(ID_INT)) {
            result = Integer.valueOf(valueStr);
        } else if (clsId.equals(ID_LONG)) {
            result = Long.valueOf(valueStr);
        } else if (clsId.equals(ID_FLOAT)) {
            result = Float.valueOf(valueStr);
        } else if (clsId.equals(ID_DOUBLE)) {
            result = Double.valueOf(valueStr);
        } else if (clsId.equals(ID_STRING)) {
            result = valueStr.substring(1); // drop the prefix placeholder
        }
        return result;
    }

}
