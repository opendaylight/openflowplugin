/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import org.opendaylight.of.lib.mp.MultipartBody;
import org.opendaylight.of.lib.msg.MutableMessage;
import org.opendaylight.of.lib.msg.OpenflowMessage;
import org.opendaylight.util.StringUtils;

import java.math.BigInteger;
import java.util.Collection;

/**
 * Provides common constants and utility methods.
 *
 * @author Simon Hunt
 */
public class CommonUtils {

    /** String representation of the {@code null} value. */
    public static final String NULL_REP = "{null}";

    /** String representing "(none)". */
    public static final String NONE = "(none)";

    /** "(no body)" text for toDebugString() implementations. */
    public static final String NO_BODY = "(no body)";

    /** Number of spaces to indent for toDebugString() implementations. */
    public static final int INDENT_SIZE = 2;

    /** Default indentation for toDebugString() implementations. */
    public static final String INDENT = "  ";

    /** Combined EOL and spaced indent for toDebugString() implementations. */
    public static final String EOLI = StringUtils.EOL + INDENT;

    /** Null parameter(s) exception message. */
    public static final String E_NULL_PARAMS = "Null parameter(s)";

    /** "Deprecated since" exception message. */
    public static final String E_DEPRECATED = " This method deprecated since: ";

    /** "Reserved value" exception message. */
    public static final String E_RESERVED = "Cannot use reserved value: ";

    /** "Mutable" exception message. */
    public static final String E_MUTABLE = "Cannot be mutable: ";

    /** "Already contains" exception message. */
    public static final String E_CONTAINS = "Already contains: ";

    // version mismatch exception messages...
    public static final String E_NOT_SUP_BEFORE = "Not supported before version ";

    /** "Not supported before 1.1" exception message. */
    public static final String E_NOT_SUP_BEFORE_11 = "Not supported before version 1.1";

    /** "Not supported before 1.2" exception message. */
    public static final String E_NOT_SUP_BEFORE_12 = "Not supported before version 1.2";

    /** "Not supported before 1.3" exception message. */
    public static final String E_NOT_SUP_BEFORE_13 = "Not supported before version 1.3";

    // no instantiation
    private CommonUtils() { }

    /**
     * Returns the given long as a string in hex form.  For example:
     * <pre>
     * String s = hex(48);   // "0x30"
     * </pre>
     *
     * @param value the value
     * @return the value in hex form
     */
    public static String hex(long value) {
        return "0x" + Long.toHexString(value);
    }

    /**
     * Returns the given int as a string in hex form.  For example:
     * <pre>
     * String s = hex(64);    // "0x40"
     * </pre>
     *
     * @param value the value
     * @return the value in hex form
     */
    public static String hex(int value) {
        return "0x" + Integer.toHexString(value);
    }

    /**
     * Converts a hex string with an optional "0x" prefix into an int. The hex
     * string is a string representation of an unsigned integer in base 16.
     * For example:
     * <pre>
     * int m = parseHexInt(&quot;0xff&quot;); // 255
     * int n = parseHexInt(&quot;ff&quot;);   // 255
     * </pre>
     *
     * @param hex a string representing a hexadecimal number with an optional
     *        "0x" prefix
     * @return the value as an int
     * @throws NumberFormatException if the hex string is not formatted
     *         correctly
     */
    public static int parseHexInt(String hex) {
        return Integer.parseInt(hex.toLowerCase().replaceFirst("0x", ""), 16);
    }

    /**
     * Converts a hex string with an optional "0x" prefix into a long. The hex
     * string is a string representation of an unsigned long in base 16.
     * For example:
     * <pre>
     * long m = parseHexInt(&quot;0xff&quot;); // 255L
     * long n = parseHexInt(&quot;ff&quot;);   // 255L
     * </pre>
     *
     * @param hex a string representing a hexadecimal number with an optional
     *        "0x" prefix
     * @return the value as a long
     * @throws NumberFormatException if the hex string is not formatted
     *         correctly
     */
    public static long parseHexLong(String hex) {
        return new BigInteger(hex.toLowerCase().replaceFirst("0x", ""), 16).longValue();
    }

    /**
     * Returns the size of the specified collection.
     * If null is specified, zero is returned.
     *
     * @param c the collection
     * @return the collection's size
     */
    public static int cSize(Collection<?> c) {
        return c == null ? 0 : c.size();
    }

    /**
     * Returns the size of the specified array.
     * If null is specified, zero is returned.
     *
     * @param array the array
     * @return the array's size
     */
    public static <T> int aSize(T[] array) {
        return array == null ? 0 : array.length;
    }

    /**
     * Returns the size of the specified array.
     * If null is specified, zero is returned.
     *
     * @param array the array
     * @return the array's size
     */
    public static int aSize(byte[] array) {
        return array == null ? 0 : array.length;
    }

    // =====================================================================
    // These methods verify something and silently return if all is well.
    //  If something is amiss, an exception is thrown.

    /**
     * Verifies that arguments are not null.
     * Throws a NullPointerException if one (or more) is.
     *
     * @param objects the objects to test
     * @throws NullPointerException if any argument is null
     */
    public static void notNull(Object... objects) {
        for (Object o: objects)
            if (o == null)
                throw new NullPointerException(E_NULL_PARAMS);
    }

    /**
     * Verifies that arguments are not null. Throws an
     * IncompleteMessageException if one (or more) is.
     *
     * @param objects the objects to test
     * @throws IncompleteMessageException if any argument is null
     */
    public static void notNullIncompleteMsg(Object... objects)
            throws IncompleteMessageException {
        for (Object o: objects)
            if (o == null)
                throw new IncompleteMessageException();
    }

    /**
     * Verifies that arguments are not null. Throws an
     * IncompleteStructureException if one (or more) is.
     *
     * @param objects the objects to test
     * @throws IncompleteStructureException if any argument is null
     */
    public static void notNullIncompleteStruct(Object... objects)
            throws IncompleteStructureException {
        for (Object o: objects)
            if (o == null)
                throw new IncompleteStructureException();
    }

    /**
     * Verifies that the argument is not mutable.
     *
     * @param struct the structure to test
     * @throws IllegalArgumentException if the argument is mutable
     */
    public static void notMutable(OpenflowStructure struct) {
        if (MutableStructure.class.isInstance(struct))
            throw new IllegalArgumentException(E_MUTABLE + struct);
    }

    /**
     * Verifies that the argument is not mutable.
     *
     * @param body the multipart-body to test
     * @throws IllegalArgumentException if the argument is mutable
     */
    public static void notMutable(MultipartBody body) {
        if (MutableStructure.class.isInstance(body))
            throw new IllegalArgumentException(E_MUTABLE + body);
    }

    /**
     * Verifies that the argument is not mutable.
     *
     * @param msg the msg to test
     * @throws IllegalArgumentException if the argument is mutable
     */
    public static void notMutable(OpenflowMessage msg) {
        if (MutableMessage.class.isInstance(msg))
            throw new IllegalArgumentException(E_MUTABLE + msg);
    }

    /**
     * Verifies that the given collection does not already contain
     * the specified object.
     *
     * @param coll the collection
     * @param o the object
     * @throws IllegalArgumentException if the collection contains the object
     */
    public static void notContains(Collection<?> coll, Object o) {
        if (coll != null && coll.contains(o))
            throw new IllegalArgumentException(E_CONTAINS + o);
    }


    /**
     * Verifies that the given string will fit in the given field size.
     * The string length must be {@code fieldSize - 1} or less (we need
     * a byte for the null terminator).
     *
     * @param str the string to test (may be null)
     * @param fieldSize the size of field to fit the string into
     * @throws IllegalArgumentException if the string will not fit the field
     */
    public static void stringField(String str, int fieldSize) {
        if (str != null && str.length() >= fieldSize)
            throw new IllegalArgumentException("String must be " +
                    (fieldSize-1) + " characters or fewer");
    }

    /**
     * Ensures that the specified protocol version is
     * {@link ProtocolVersion#V_1_1 1.1} at a minimum. If it is, this
     * method silently returns. If it is not,
     * a {@link VersionMismatchException} is thrown.
     * The supplied text is included in the exception message.
     *
     * @param pv the protocol version to test
     * @param text additional detail to add to the exception message
     * @throws VersionMismatchException if protocol version is earlier than 1.1
     */
    public static void verMin11(ProtocolVersion pv, String text) {
        if (pv.lt(ProtocolVersion.V_1_1)) {
            String msg = text == null ? E_NOT_SUP_BEFORE_11
                    : E_NOT_SUP_BEFORE_11 + " (" + text + ")";
            throw new VersionMismatchException(msg);
        }
    }


    /**
     * Ensures that the specified protocol version is
     * {@link ProtocolVersion#V_1_1 1.1} at a minimum. If it is, this
     * method silently returns. If it is not,
     * a {@link VersionMismatchException} is thrown.
     *
     * @param pv the protocol version to test
     * @throws VersionMismatchException if protocol version is earlier than 1.1
     */
    public static void verMin11(ProtocolVersion pv) {
        verMin11(pv, null);
    }

    /**
     * Ensures that the specified protocol version is
     * {@link ProtocolVersion#V_1_2 1.2} at a minimum. If it is, this
     * method silently returns. If it is not,
     * a {@link VersionMismatchException} is thrown.
     * The supplied text is included in the exception message.
     *
     * @param pv the protocol version to test
     * @param text additional detail to add to the exception message
     * @throws VersionMismatchException if protocol version is earlier than 1.2
     */
    public static void verMin12(ProtocolVersion pv, String text) {
        if (pv.lt(ProtocolVersion.V_1_2)) {
            String msg = text == null ? E_NOT_SUP_BEFORE_12
                    : E_NOT_SUP_BEFORE_12 + " (" + text + ")";
            throw new VersionMismatchException(msg);
        }
    }


    /**
     * Ensures that the specified protocol version is
     * {@link ProtocolVersion#V_1_2 1.2} at a minimum. If it is, this
     * method silently returns. If it is not,
     * a {@link VersionMismatchException} is thrown.
     *
     * @param pv the protocol version to test
     * @throws VersionMismatchException if protocol version is earlier than 1.2
     */
    public static void verMin12(ProtocolVersion pv) {
        verMin12(pv, null);
    }

    /**
     * Ensures that the specified protocol version is
     * {@link ProtocolVersion#V_1_3 1.3} at a minimum. If it is, this
     * method silently returns. If it is not,
     * a {@link VersionMismatchException} is thrown.
     * The supplied text is included in the exception message.
     *
     * @param pv the protocol version to test
     * @param text additional detail to add to the exception message
     * @throws VersionMismatchException if protocol version is earlier than 1.3
     */
    public static void verMin13(ProtocolVersion pv, String text) {
        if (pv.lt(ProtocolVersion.V_1_3)) {
            String msg = text == null ? E_NOT_SUP_BEFORE_13
                    : E_NOT_SUP_BEFORE_13 + " (" + text + ")";
            throw new VersionMismatchException(msg);
        }
    }

    /**
     * Ensures that the specified protocol version is
     * {@link ProtocolVersion#V_1_3 1.3} at a minimum. If it is, this
     * method silently returns. If it is not,
     * a {@link VersionMismatchException} is thrown.
     *
     * @param pv the protocol version to test
     * @throws VersionMismatchException if protocol version is earlier than 1.3
     */
    public static void verMin13(ProtocolVersion pv) {
        verMin13(pv, null);
    }


    /**
     * Ensures that the specified protocol version is, at a minimum the
     * specified since version. If it is, this method silently returns.
     * If it is not, a {@link VersionMismatchException} is thrown.
     * The supplied text is included in the exception message.
     *
     * @param pv the protocol version to test
     * @param since the protocol version to test against
     * @param text additional detail to add to the exception message
     * @throws VersionMismatchException if {@code pv} is earlier than
     *          the version specified as {@code since}
     */
    public static void verMinSince(ProtocolVersion pv,
                                    ProtocolVersion since,
                                    String text) {
        if (pv.lt(since)) {
            String msg = text == null
                    ? CommonUtils.E_NOT_SUP_BEFORE + since
                    : CommonUtils.E_NOT_SUP_BEFORE + since + " (" + text + ")";
            throw new VersionMismatchException(msg);
        }
    }

    /**
     * Ensures that the specified versions are all the same. If they are,
     * this method silently returns. If not, a {@link VersionMismatchException}
     * is thrown, with the specified message text.
     *
     * @param text text of the exception message
     * @param vers the versions to test
     */
    public static void sameVersion(String text, ProtocolVersion... vers) {
        if (vers != null && vers.length > 1) {
            ProtocolVersion first = vers[0];
            for (int i=1; i<vers.length; i++)
                if (vers[i] != first)
                    throw new VersionMismatchException(text);
        }
    }

    /**
     * Ensures that the specified protocol version is prior to the specified
     * deprecated version (the version at which the feature is no longer valid).
     * If it is, this method silently returns. If not,
     * a {@link VersionMismatchException} is thrown, with the specified message
     * text.
     *
     * @param pv the protocol version to test
     * @param deprecatedAt the version at which the feature was deprecated
     * @param text additional detail to add to the exception message
     * @throws VersionMismatchException if {@code pv} is the same as or
     *          later than the version specified as {@code deprecatedAt}
     */
    public static void notDeprecated(ProtocolVersion pv,
                                     ProtocolVersion deprecatedAt,
                                     String text) {
        if (pv.ge(deprecatedAt)) {
            String msg = text == null
                    ? CommonUtils.E_DEPRECATED + deprecatedAt
                    : CommonUtils.E_DEPRECATED + deprecatedAt + " (" + text + ")";
            throw new VersionMismatchException(msg);
        }
    }
}
