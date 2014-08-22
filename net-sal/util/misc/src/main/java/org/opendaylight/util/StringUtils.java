/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides useful utilities revolving around strings.
 *
 * @author Simon Hunt
 * @author Steve Britt
 * @author Thomas Vachuska
 */
public final class StringUtils {

    /** The platform dependent line separator (new line character). */
    public static final String EOL = System.getProperty("line.separator");

    /** The tab character. */
    public static final String TAB = "\t";

    /** The empty string. */
    public static final String EMPTY = "";

    /** The underscore character. */
    public static final String UNDERSCORE = "_";

    /** The zero ("0") character as a string. */
    public static final String ZERO = "0";

    /** The comma (",") character as a string. */
    public static final String COMMA = ",";

    /** String representing UTF-8 encoding (e.g. String.getBytes(UTF8)) */
    public static final String UTF8 = "UTF-8";


    /** Format token used for parameter replacement. */
    private static final String FORMAT_TOKEN = "{}";

    /** String representation of null. */
    private static final String NULL_REP = "{null}";

    private static final char SPACE = ' ';

    /** regular expression for identifiers. */
    private static final Pattern ID_REGEXP =
            Pattern.compile("[A-Z][A-Z0-9_\\.]*", Pattern.CASE_INSENSITIVE);

    private static final String E_INVALID_ID =
            "identifier must begin with a letter and may be followed by " +
                    "zero or more letters, numbers, underscores, and/or dots";

    private static final String E_NULL_PARAM = "parameter cannot be null";

    private static final String E_NOT_UPPER_FORMAT =
            "string must consist of only uppercase letters, digits, and/or" +
                    "underscores, beginning with a letter or underscore";

    private static final String E_NOT_CAMEL_CASE =
            "string must consist of only letters and digits, starting " +
                    "with a letter";

    // matches string form of "0" to "255" (and nothing else)
    private static final String RE_0_TO_255 = "([01]?\\d\\d?|2[0-4]\\d|25[0-5])";

    // start with a letter, followed by zero or more letters or digits
    private static final String RE_ALPHA_NUMERIC = "[A-Za-z][A-Za-z0-9]*";

    // start with a letter or underscore, followed by zero or more
    //   letters, digits or underscores
    private static final String RE_ALPHA_NUMERIC_UNDERSCORES =
            "[A-Za-z_][A-Za-z0-9_]*";

    // start with uppercase letter or underscore, followed by zero or more
    //   uppercase letters, digits or underscores
    private static final String RE_UPPER_ALPHA_NUMERIC_UNDERSCORES =
            "[A-Z_][A-Z0-9_]*";

    // matches one or more lowercase letters
    private static final String RE_LOWER_ALPHA = "[a-z]+";

    // required for proper use of toUpperCase/toLowerCase
    private static final Locale LOCALE = Locale.getDefault();

    // no instantiation
    private StringUtils() { }


    /** This predicate returns true if the specified parameter is null, or
     * consists only of whitespace.
     *
     * @param value the string value to test
     * @return true if the value is null, or empty
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }

    /**
     * Safely invokes {@code toString} on the specified entity and returns the
     * result, unless the specified object is null, in which case it will
     * return null.
     *
     * @param o object on which to invoke toString
     * @return result of {@code o.toString()} invocation or null if the object
     *         is null
     */
    public static String safeToString(Object o) {
        return o == null ? null : o.toString();
    }

    /** This predicate returns true if the specified string is alpha numeric;
     * that is to say, if it begins with a letter and consists only of letters
     * and numbers. Underscores are allowed if the second parameter is set true.
     *
     * @param value the string value to test
     * @param allowUnderscores if true, underscores are allowed
     * @return true if the value is alphanumeric
     * @throws NullPointerException if value is null
     */
    public static boolean isAlphaNumeric(String value, boolean allowUnderscores) {
        return value.matches(allowUnderscores ? RE_ALPHA_NUMERIC_UNDERSCORES
                                              : RE_ALPHA_NUMERIC);
    }

    /** This predicate returns true if the specified string is alpha numeric;
     * that is to say, if it begins with a letter and consists only of letters,
     * numbers and underscores.
     *
     * @param value the string value to test
     * @return true if the value is alphanumeric
     * @throws NullPointerException if value is null
     */
    public static boolean isAlphaNumeric(String value) {
        return isAlphaNumeric(value, true);
    }

    /** Returns the given value wrapped in double-quote characters (") if the
     * value is not null, or the string {@code "null"} if it is null.
     *
     * @param value the value
     * @return the string quoted (if not null), or "null"
     */
    public static String quoted(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }

    /** A convenience overloaded method for {@link #toCamelCase(String)}
     * which takes enumeration constants as the parameter.
     * @param e the enum constant
     * @return the name of the enum constant, camel-cased
     */
    public static String toCamelCase(Enum<?> e) {
        return toCamelCase(e.name());
    }

    /**
     * This method expects an input string consisting of uppercase letters,
     * digits, and underscores (the standard format for constant names), and
     * converts it to "camelCase".
     * <p>
     * For example, {@code "LORD_OF_THE_RINGS"} becomes
     * {@code "lordOfTheRings"}.
     *
     * @param s the uppercase words delimited by underscores input string
     * @return the input string converted to camelCase
     * @throws NullPointerException if the parameter is null
     * @throws IllegalArgumentException if s has unexpected (or no) characters
     *         in it
     */
    public static String toCamelCase(String s) {
        return toCamelCase(EMPTY, s);
    }

    /**
     * A convenience overloaded method for
     * {@link #toCamelCase(String, String)}, which uses {@code e.name()}.
     *
     * @param prefix optional prefix
     * @param e the enum constant
     * @return the name of the enum constant, camel-cased
     */
    public static String toCamelCase(String prefix, Enum<?> e) {
        return toCamelCase(prefix, e.name());
    }

    /**
     * This method expects a string consisting of uppercase letters, digits,
     * and underscores (the standard format for constant names), and converts
     * it to "camelCase". If a prefix is specified, i.e. is not null or empty
     * string, it will be prepended as-is.
     * <p>
     * For example:
     * <pre>
     * "LORD_OF_THE_RINGS" --> "lordOfTheRings"
     * "LORD_OF_THE_RINGS" and prefix "prequelTo" --> "prequelToLordOfTheRings"
     * </pre>
     *
     * @param prefix optional prefix
     * @param s the uppercase words delimited by underscores input string
     * @return the input string converted to camelCase
     * @throws NullPointerException if the parameter is null
     * @throws IllegalArgumentException if s has unexpected (or no) characters
     *         in it
     */
    public static String toCamelCase(String prefix, String s) {
        if (s == null)
            throw new NullPointerException(E_NULL_PARAM);
        if (!s.matches(RE_UPPER_ALPHA_NUMERIC_UNDERSCORES))
            throw new IllegalArgumentException(E_NOT_UPPER_FORMAT);

        String p = prefix != null ? prefix : EMPTY;
        StringBuilder sb = new StringBuilder(p);
        String[] words = s.split("_");
        for (String w: words) {
            if (w.length()>0) {
                String lc = w.toLowerCase(Locale.getDefault());
                if (sb.length() > 0) {
                    sb.append(w.charAt(0)).append(lc.substring(1));
                } else {
                    sb.append(lc);
                }
            }
        }
        return sb.toString();
    }

    // matches zero or more lowercase letters (group 1), followed by
    //  exactly one uppercase letter or digit (group 2)
    private static final Pattern P_LOWER_UPPER =
            Pattern.compile("([a-z]*)([A-Z0-9])");

    // matches zero or more lowercase letters (group 1) at the end of the string
    private static final Pattern P_LOWER_SUFFIX = Pattern.compile("([a-z]*)$");

    /** This method expects an input string consisting of letters and digits
     * in "camelCase" (the standard format for variable names), and converts
     * it to uppercase letters with underscore delimiters between the words.
     * <p>
     * For example, {@code "lordOfTheRings"} becomes
     * {@code "LORD_OF_THE_RINGS"}.
     *
     * @param cc the camelCase input string
     * @return the input string converted to uppercase words delimited
     *          by underscores
     * @throws NullPointerException if the parameter is null
     * @throws IllegalArgumentException if s has unexpected (or no) characters
     *          in it
     */
    public static String fromCamelCase(String cc) {
        if (cc == null)
            throw new NullPointerException(E_NULL_PARAM);
        if (!cc.matches(RE_ALPHA_NUMERIC))
            throw new IllegalArgumentException(E_NOT_CAMEL_CASE);

        String result;
        if (cc.matches(RE_LOWER_ALPHA)) {
            // if string is just lowercase letters, simply convert to uppercase
            result = cc.toUpperCase(LOCALE);
        } else {
            StringBuilder sb = new StringBuilder();
            // matcher to match successive groups of "[xxxx]X"
            //  (optional lowercase letters; one uppercase letter or digit)
            Matcher m = P_LOWER_UPPER.matcher(cc);
            if (m != null) {
                while(m.find()) {
                    String lower = m.group(1);
                    String upper = m.group(2);
                    sb.append(lower.toUpperCase(LOCALE))
                      .append(UNDERSCORE).append(upper);
                }
            }
            // don't forget trailing lowercase letters
            m = P_LOWER_SUFFIX.matcher(cc);
            if (m != null) {
                m.find();
                sb.append(m.group(1).toUpperCase(LOCALE));
            }
            result = sb.toString();
        }
        return result;
    }

    /** This predicate returns true if the specified string, when converted
     * to an int, lies in the range 0 - 255. Leading zeroes are allowed.
     *
     * @param value the string value to test
     * @return true if the value is "0" - "255"
     * @throws NullPointerException if value is null
     */
    public static boolean isIntegerZeroTo255(String value) {
        return value.matches(RE_0_TO_255);
    }

    /** This predicate returns true if the supplied string matches the regular
     * expression defined for general identifiers. That is to say, the string
     * starts with letter, and is followed by zero or more letters, numbers,
     * underscores, and/or dots.
     *
     * @param id the identifier
     * @return true if it is well-formed
     */
    public static boolean isStandardIdentifier(String id) {
        return ID_REGEXP.matcher(id).matches();
    }

    /** This method checks the supplied string to see that it matches the
     * regular expression defined for general identifiers. That is to say,
     * the string starts with letter, and is followed by zero or more letters,
     * numbers, underscores, and/or dots. If it matches, the method silently
     * returns. If it does not match, an exception is thrown.
     *
     * @param id the identifier
     * @throws IllegalArgumentException if the string does not match the
     *          pattern for standard identifiers.
     */
    public static void validateStandardIdentifier(String id) {
        if (!ID_REGEXP.matcher(id).matches())
            throw new IllegalArgumentException(E_INVALID_ID);
    }

    /**
     * Determine if two Strings, considering nulls to be identical, are equal.
     *
     * @param one the first String to compare
     * @param two the second String to compare
     * @return true if the Strings are identical, including if both are null,
     *         and false otherwise
     */
    public static boolean equals(String one, String two) {
        return (((one != null) && (two != null) && one.equals(two)) ||
                ((one == null) && (two == null)));
    }

    /**
     * Returns either the correctly parsed hex value (as an int) or the
     * supplied fallback value.
     *
     * @param hex String to parse as a hex value
     * @param fallback The int to return if the supplied hex string is invalid
     * @return the hex value
     */
    public static int parseHex(String hex, int fallback) {
        int result;
        try {
            result = Integer.parseInt(hex, 16);
        } catch (NumberFormatException nfe) {
            result = fallback;
        }
        return result;
    }

    /**
     * Returns either the correctly parsed int value or the supplied
     * fallback value.
     *
     * @param value String to parse as an int
     * @param fallback The int to return if the supplied string is invalid
     * @return the int value
     */
    public static int parseInt(String value, int fallback) {
        int result;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            result = fallback;
        }
        return result;
    }


    /**
     * Returns a zero-filled string.  For example:
     * <pre>
     * StringUtils.zeroFill(45, 6);
     * </pre>
     * returns the string {@code "000045"}.
     *
     * @param value the value
     * @param fieldWidth the field width
     * @return the zero filled string
     */
    public static String zeroFill(int value, int fieldWidth) {
        StringBuilder buf = new StringBuilder();
        buf.append(value);
        while (buf.length() < fieldWidth) {
            buf.insert(0, ZERO);
        }
        return buf.toString();
    }

    /**
     * A simple string formatter that replaces each occurrence of
     * <code>{}</code> in the format string with the string representation
     * of each of the subsequent, optional arguments.
     * For example:
     * <pre>
     *     StringUtils.format("{} => {}", "Foo", 123);
     *     // returns "Foo => 123"
     * </pre>
     *
     * @param fmt the format string
     * @param o arguments to be inserted into the output string
     * @return a formatted string
     */
    public static String format(String fmt, Object... o) {
        if (fmt == null)
            throw new NullPointerException("null format string");
        if (o.length == 0)
            return fmt;

        // Format the message using the format string as the seed.
        // Stop either when the list of objects is exhausted or when
        // there are no other place-holder tokens.
        final int ftlen = FORMAT_TOKEN.length();
        int i = 0;
        int p = -1;
        String rep;
        StringBuilder sb = new StringBuilder(fmt);
        while (i < o.length && (p = sb.indexOf(FORMAT_TOKEN, p + 1)) >= 0) {
            rep = o[i] == null ? NULL_REP : o[i].toString();
            sb.replace(p, p + ftlen, rep);
            i++;
        }
        return sb.toString();
    }

    /**
     * Formats the supplied strings so that they are arranged in neat columns.
     *
     * @param strings the array of strings to format
     * @param maxWidth the maximum number of characters any single line can be
     * @param colSpacing the number of spaces between columns
     * @param indent number of spaces to indent the whole thing by
     * @return a string (with embedded newlines) that is the columnized result
     */
    public static String columnize(String[] strings, int maxWidth,
                                   int colSpacing, int indent) {
        // a little sanity checking..
        if (strings == null || strings.length == 0)
            return EMPTY;

        final String indenter = pad(EMPTY, indent);
        final String spacer = pad(EMPTY, colSpacing);

        // find the longest string..
        int longest = 0;
        for (String s : strings)
            longest = (s.length() > longest ? s.length() : longest);
        // calc the number of columns..
        int ncol = 1;
        int width = indent + longest;
        while (width < maxWidth) {
            width += colSpacing + longest;
            if (width <= maxWidth)
                ncol++;
        }
        int nrow = (strings.length + ncol - 1) / ncol;

        StringBuilder result = new StringBuilder();
        for (int r=0; r<nrow; r++) {
            int d = 0;
            while (r + d < strings.length) {
                result.append(d == 0 ? indenter : spacer);
                result.append(pad(strings[r + d], longest));
                d += nrow;
            }
            result.append(EOL);
        }
        return result.toString();
    }

    /**
     * Formats the supplied strings so that they are arranged in
     * neat columns, but with no indent.
     *
     * @param strings the array of strings to format
     * @param maxWidth the maximum number of characters any single line can be
     * @param colSpacing the number of spaces between columns
     * @return the columnized strings
     */
    public static String columnize(String[] strings, int maxWidth,
                                   int colSpacing) {
        return columnize(strings, maxWidth, colSpacing, 0);
    }

    /**
     * Formats the supplied strings so that they are arranged in
     * neat columns; space between columns is 2; no indent.
     *
     * @param strings the array of strings to format
     * @param maxWidth the maximum number of characters any single line can be
     * @return the columnized strings
     */
    public static String columnize(String[] strings, int maxWidth) {
        return columnize(strings, maxWidth, 2, 0);
    }

    /**
     * Formats the supplied strings so that they are arranged in
     * neat columns; maximum line width is 78; space between columns is 2;
     * no indent.
     *
     * @param strings the array of strings to format
     * @return the columnized strings
     */
    public static String columnize(String[] strings) {
        return columnize(strings, 78, 2, 0);
    }


    /** Enumeration for alignment. */
    public static enum Align { LEFT, CENTER, RIGHT }

    /**
     * Pads a field to the specified field width. If what is longer than
     * width, it is truncated, unless the noTruncate flag is set true.
     *
     * @param what the string to be padded
     * @param width the width of the padded (truncated) field
     * @param with the character to pad with
     * @param align the alignment
     * @param noTruncate do not truncate the string if it is longer than width
     * @return the padded string
     */
    public static String pad(String what, int width, char with, Align align,
                             boolean noTruncate) {
        if (what.length() >= width)
            return noTruncate ? what : what.substring(0,width);

        StringBuilder sb = new StringBuilder(what);
        int insertPos = (align == Align.RIGHT) ? 0 : sb.length();
        while (sb.length() < width) {
            sb.insert(insertPos, with);
            if (align == Align.CENTER) {
                // flip insert pos to other side
                insertPos = (insertPos==0 ? sb.length() : 0);
            }
        }
        return sb.toString();
    }

    /**
     * Pads a field to the specified field width. If what is longer than width,
     * it is truncated.
     *
     * @param what the string to be padded
     * @param width the width of the padded (truncated) field
     * @param with the character to pad with
     * @param align the alignment
     * @return the padded string
     */
    public static String pad(String what, int width, char with, Align align) {
        return pad(what, width, with, align, false);
    }


    /**
     * Pads a field to the specified field width. If what is longer than width,
     * it is truncated. The text is LEFT justified.
     *
     * @param what the string to be padded
     * @param width the width of the padded (truncated) field
     * @param with the character to pad with
     * @return the padded string
     */
    public static String pad(String what, int width, char with) {
        return pad(what, width, with, Align.LEFT, false);
    }

    /**
     * Pads a field to the specified field width. If what is longer than width,
     * it is truncated. Space is used as the padding character.
     *
     * @param what the string to be padded
     * @param width the width of the padded (truncated) field
     * @param align one of LEFT, CENTER or RIGHT
     * @return the padded string
     */
    public static String pad(String what, int width, Align align) {
        return pad(what, width, SPACE, align, false);
    }

    /**
     * Pads a field to the specified field width. If what is longer than width,
     * it is truncated. The text is LEFT justified. Space is used as the
     * padding character.
     *
     * @param what the string to be padded
     * @param width the width of the padded (truncated) field
     * @return the padded string
     */
    public static String pad(String what, int width) {
        return pad(what, width, SPACE, Align.LEFT, false);
    }

    /** Returns a string of spaces of the specified length.
     *
     * @param length the length
     * @return a string of spaces
     */
    public static String spaces(int length) {
        return pad("", length);
    }

    /**
     * Returns a comma-separated string of the items in the collection. The
     * individual tokens within the string are produced using
     * {@code Object#toString()} on each item in the collection.
     *
     * @param items collection of items
     * @return comma-separated string of items in the collection; empty string
     *         if the collection is empty; null if the collection is null
     */
    public static String commaSeparated(Collection<?> items) {
        return join(items, COMMA);
    }


    /**
     * Convert the given string to null, if it is already null or is empty.
     *
     * @param s string to be verified
     * @return null if the string is null or empty; otherwise the unmodified
     *         string
     */
    public static String emptyIsNull(String s) {
        return s == null || s.length() == 0 ? null : s;
    }

    /**
     * Convert the given string to null, if it is already null or is empty if
     * trimmed.
     *
     * @param s string to be verified
     * @return null if the string is null or empty; otherwise the unmodified
     *         string
     */
    public static String trimmedEmptyIsNull(String s) {
        return s == null || s.trim().length() == 0 ? null : s;
    }

    /**
     * Convert the given string to an empty string, if it is null.
     *
     * @param s string to be verified
     * @return empty string if the string is null; otherwise the unmodified
     *         string
     */
    public static String nullIsEmpty(String s) {
        return s == null ? EMPTY : s;
    }

    /**
     * Removes any punctuation in the passed in string.
     *
     * @param value the original string value that may contain punctuation
     * @return true new string with the punctuation removed
     * @throws NullPointerException if value is null
     */
    public static String trimPunct(String value) {
        return value.replaceAll("\\p{Punct}+", "");
    }

    /**
     * Removes any punctuation and whitespace in the passed in string.
     *
     * @param value the original string value that may contain punctuation
     * @param replace string to be replaced instead of punctuation and
     *                white space
     * @return true new string with the punctuation removed
     * @throws NullPointerException if value is null
     */
    public static String trimPunctAndSpaceWith(String value, String replace) {
        return value.replaceAll("\\W", replace);
    }


    /** Default delimiter for the {@link #join} methods. */
    public static final String DEFAULT_JOIN_DELIMITER = ", ";

    /**
     * Joins a collection of objects' toString() values into a single string,
     * using the default delimiter of ", " (comma-space).
     *
     * @param values the values
     * @return the delimiter-separated string
     */
    public static String join(Collection<?> values) {
        if (values == null)
            return null;
        return join(values.toArray(new Object[values.size()]));
    }

    /**
     * Joins an array of objects' toString() values into a single string,
     * using the default delimiter of ", " (comma-space).
     *
     * @param values the values
     * @return the delimiter-separated string
     */
    public static String join(Object[] values) {
        return join(values, DEFAULT_JOIN_DELIMITER);
    }

    /**
     * Joins a collection of objects' toString() values into a single string,
     * using the given delimiter. If the collection is null, null is returned.
     *
     * @param values the values
     * @param delim the delimiter
     * @return the delimiter-separated string
     */
    public static String join(Collection<?> values, String delim) {
        if (values==null)
            return null;
        return join(values.toArray(new Object[values.size()]), delim);
    }

    /**
     * Joins an array of objects' toString() values into a single string,
     * using the given delimiter. If the array is null, null is returned.
     *
     * @param values the values
     * @param delim the delimiter
     * @return the delimiter-separated string
     */
    public static String join(Object[] values, String delim) {
        if (values==null)
            return null;

        StringBuilder sb = new StringBuilder();
        for (Object v: values) {
            if (sb.length()>0)
                sb.append(delim);
            sb.append(v);
        }
        return sb.toString();
    }

    /** Concatenates an array of objects using a StringBuilder and its
     * {@code append()} method. This convenience method reduces something like:
     * <pre>
     * String s = new StringBuilder().append(e1).append(e2).append(e3).toString();
     * </pre>
     * to
     * <pre>
     * String s = StringUtils.concat(e1, e2, e3);
     * </pre>
     *
     * @param values the values to concatenate
     * @return the concatentated string
     */
    public static String concat(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (Object o: values)
            sb.append(o);
        return sb.toString();
    }


    /**
     * Normalizes any compound line breaks (\r\n or \n\r) to only \n.
     *
     * @param string string to be normalized
     * @return normalized string
     */
    public static String normalizeEOL(String string) {
        return string.replaceAll("(\n\r|\r\n|\r)", "\n");
    }

    private static final Pattern RE_MULTLINE =
            Pattern.compile("^(.*)$", Pattern.MULTILINE);

    /**
     * Removes all lines that begin with the '#' character.
     *
     * @param s the multiline input
     * @return multiline output with "comment" lines removed
     */
    public static String stripCommentLines(String s) {
        StringBuilder sb = new StringBuilder();
        Matcher m = RE_MULTLINE.matcher(s);
        if (m != null) {
            while(m.find()) {
                String line = m.group(1);
                if (!line.startsWith("#")) {
                    sb.append(line).append(EOL);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Read in the contents of a text file from the given path. If the
     * resource cannot be found, null is returned.
     *
     * @param path path of resource containing the data
     * @param classLoader class-loader to be used for locating resource
     * @return the contents of the file as a string
     * @throws IOException if there is a problem reading the file.
     */
    public static String getFileContents(String path, ClassLoader classLoader)
            throws IOException {
        InputStream is = classLoader.getResourceAsStream(path);
        if (is == null)
            return null;

        String result = null;
        try {
            // use Apache Commons IOUtils to read byte array from input stream
            byte[] bytes = IOUtils.toByteArray(is);
            result = new String(bytes, UTF8).replaceAll("\0", "");
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // What to do, what to do?
            }
        }
        return result;
    }

}
