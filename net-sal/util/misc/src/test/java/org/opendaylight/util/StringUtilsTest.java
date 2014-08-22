/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.opendaylight.util.junit.TestTools;
import org.opendaylight.util.StringUtils.Align;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.StringUtils.EOL;
import static org.opendaylight.util.StringUtils.pad;
import static org.junit.Assert.*;

/**
 * Test suite of utilities revolving around strings.
 *
 * @author Simon Hunt
 * @author Steve Britt
 */
public class StringUtilsTest {
    private final ClassLoader classLoader = getClass().getClassLoader();

    private static final String[] BAD_BYTES = {
            "",
            "ABC",
            "-5",
            "-1",
            "256",
            "2000",
    };

    private static final String[] GOOD_BYTES = {
            "0",
            "1",
            "50",
            "127",
            "129",
            "254",
            "255",
            "000",
            "012",
    };

    private static final String HELLO = "Hello\nWorld\n";
    private static final String HELLO2 = "Hello\n\nWorld\n";

    @Test
    public void normalizeString() {
        // Effective no-ops...
        assertEquals(AM_HUH, "Hello", StringUtils.normalizeEOL("Hello"));
        assertEquals(AM_HUH, HELLO, StringUtils.normalizeEOL("Hello\nWorld\n"));

        // Carriage return replacements...
        assertEquals(AM_HUH, HELLO, StringUtils.normalizeEOL("Hello\rWorld\r"));
        assertEquals(AM_HUH, HELLO, StringUtils.normalizeEOL("Hello\n\rWorld\n\r"));
        assertEquals(AM_HUH, HELLO, StringUtils.normalizeEOL("Hello\r\nWorld\r\n"));

        // Blank lines and carriage return replacements...
        assertEquals(AM_HUH, HELLO2, StringUtils.normalizeEOL("Hello\r\rWorld\r"));
        assertEquals(AM_HUH, HELLO2, StringUtils.normalizeEOL("Hello\n\r\n\rWorld\n\r"));
        assertEquals(AM_HUH, HELLO2, StringUtils.normalizeEOL("Hello\r\n\r\nWorld\r\n"));

        // Mix-mode
        assertEquals(AM_HUH, HELLO2, StringUtils.normalizeEOL("Hello\r\n\n\rWorld\r\n"));
    }

    @Test(expected = NumberFormatException.class)
    public void parseDouble() {
        Double.parseDouble("1,23456789");
    }

    @Test(expected = NullPointerException.class)
    public void nullStringBytes() {
        print(EOL + "nullStringBytes()");
        StringUtils.isIntegerZeroTo255(null);
    }

    @Test
    public void badStringBytes() {
        print(EOL + "badStringBytes()");
        for (String s: BAD_BYTES) {
            print("  bad byte [" + s + "]");
            assertFalse(AM_HUH, StringUtils.isIntegerZeroTo255(s));
        }
    }

    @Test
    public void goodStringBytes() {
        print(EOL + "goodStringBytes()");
        for (String s: GOOD_BYTES) {
            print("  good byte [" + s + "]");
            assertTrue(AM_HUH, StringUtils.isIntegerZeroTo255(s));
        }
    }

    private static final String[] BAD_IDS = {
            "",
            "0", "_", ".", "9", "-",
            "0a", "_a", ".a",

            "a-b", "a!a", "a@a", "a#a", "a$a",
            "a%a", "a^a", "a&a", "a*a", "a(a",
            "a)a", "a:a", "a;a", "a~a",
    };

    private static final String[] GOOD_IDS = {
            "a", "A", "z", "Z", "a0", "Simon.Hunt", "Thomas_Vachuska",
            "Fred.123",
    };

    @Test
    public void checkBadIds() {
        print(EOL + "checkBadIds()");
        for (String s: BAD_IDS) {
            if (StringUtils.isStandardIdentifier(s)) {
                fail("Should not allow this one: '" + s + "'");
            }
        }
    }

    @Test
    public void checkGoodIds() {
        print(EOL + "checkGoodIds()");
        for (String s: GOOD_IDS) {
            if (!StringUtils.isStandardIdentifier(s)) {
                fail("Should have allowed this one: '" + s + "'");
            }
        }
    }

    @Test
    public void checkBadIdsException() {
        print(EOL + "checkBadIdsException()");
        for (String s: BAD_IDS) {
            try {
                StringUtils.validateStandardIdentifier(s);
                fail("should have thrown an exception");
            } catch (IllegalArgumentException iae) {
                // good
            }
        }
    }

    @Test
    public void checkGoodIdsException() {
        print(EOL + "checkGoodIdsException()");
        for (String s: GOOD_IDS) {
            try {
                StringUtils.validateStandardIdentifier(s);
            } catch (IllegalArgumentException iae) {
                fail("should NOT have thrown an exception");
            }
        }
    }


    private static final String HOMER_1 = "Homer";
    private static final String HOMER_2 = "Homer";
    private static final String MARGE = "Marge";

    @Test
    public void testEquals() {
        print(EOL + "testEquals()");
        assertTrue("Null strings should be equal", StringUtils.equals(null, null));
        assertTrue("Strings should be equal", StringUtils.equals(HOMER_1, HOMER_2));
        assertFalse("Strings should be unequal", StringUtils.equals(null, HOMER_1));
        assertFalse("Strings should be unequal", StringUtils.equals(HOMER_1, null));
        assertFalse("Strings should be unequal", StringUtils.equals(HOMER_1, MARGE));
    }


    private static final String STR_F = "f";
    private static final String STR_25 = "25";
    private static final String STR_BADBOY = "badb0y";
    private static final int FALLBACK = 42;

    @Test
    public void parseSomeHexes() {
        print(EOL + "parseSomeHexes()");
        assertEquals(AM_VMM, 15, StringUtils.parseHex(STR_F, FALLBACK));
        assertEquals(AM_VMM, 37, StringUtils.parseHex(STR_25, FALLBACK));
        assertEquals(AM_VMM, 42, StringUtils.parseHex(STR_BADBOY, FALLBACK));
    }

    @Test
    public void parseSomeInts() {
        print(EOL + "parseSomeInts()");
        assertEquals(AM_VMM, 25, StringUtils.parseInt(STR_25, FALLBACK));
        assertEquals(AM_VMM, 42, StringUtils.parseInt(STR_F, FALLBACK));
        assertEquals(AM_VMM, 42, StringUtils.parseInt(STR_BADBOY, FALLBACK));
    }

    private static final String ZEROFILL = "0000042";
    private static final int ZF_VALUE = 42;
    private static final int ZF_WIDTH = 7;

    @Test
    public void fillWithZeros() {
        print(EOL + "fillWithZeros()");
        assertEquals(AM_HUH, ZEROFILL, StringUtils.zeroFill(ZF_VALUE, ZF_WIDTH));
    }

    private static final String[] NUMBERS = {
            "one", "two", "three", "four", "five",
            "six", "seven", "eight", "nine", "ten",
    };
    private static final String DEFAULT_COL_STR =
            "one    two    three  four   five   six    seven  eight  nine   ten  " + EOL;

    private static final String COL_STR_W_40 =
            "one    three  five   seven  nine " + EOL +
            "two    four   six    eight  ten  " + EOL;

    private static final String COL_STR_W_20 =
            "one    five   nine " + EOL +
            "two    six    ten  " + EOL +
            "three  seven" + EOL +
            "four   eight" + EOL;

    @Test
    public void columnsEmpty() {
        print(EOL + "columnsEmpty()");
        assertEquals(AM_HUH, "", StringUtils.columnize(null));
        assertEquals(AM_HUH, "", StringUtils.columnize(new String[0]));
    }

    @Test
    public void columnsDefault() {
        print(EOL + "columnsDefault()");
        String s = StringUtils.columnize(NUMBERS);
        print(s);
        assertEquals(AM_HUH, DEFAULT_COL_STR, s);
    }

    @Test
    public void columnsWidth40() {
        print(EOL + "columnsWidth40()");
        String s = StringUtils.columnize(NUMBERS, 40);
        print(s);
        assertEquals(AM_HUH, COL_STR_W_40, s);
    }

    @Test
    public void columnsWidth20() {
        print(EOL + "columnsWidth20()");
        String s = StringUtils.columnize(NUMBERS, 20);
        print(s);
        assertEquals(AM_HUH, COL_STR_W_20, s);
    }

    private static final String COL_STR_W_30_SPC_4 =
            "one      five     nine " + EOL +
            "two      six      ten  " + EOL +
            "three    seven" + EOL +
            "four     eight" + EOL;

    @Test
    public void columnsWidth30Spacer4() {
        print(EOL + "columnsWidth30Spacer4()");
        String s = StringUtils.columnize(NUMBERS, 30, 4);
        print(s);
        assertEquals(AM_HUH, COL_STR_W_30_SPC_4, s);
    }

    private static final String COL_STR_W_30_SPC_4_IND_3 =
            "   one      five     nine " + EOL +
            "   two      six      ten  " + EOL +
            "   three    seven" + EOL +
            "   four     eight" + EOL;

    @Test
    public void columnsWidth30Spacer4indent3() {
        print(EOL + "columnsWidth30Spacer4indent3()");
        String s = StringUtils.columnize(NUMBERS, 30, 4, 3);
        print(s);
        assertEquals(AM_HUH, COL_STR_W_30_SPC_4_IND_3, s);
    }
    //====

    private static final String SOMETHING = "xyzzy";

    private static final int SOME_WIDTH = 10;

    private static final String SOMETHING_PAD_LEFT = "xyzzy     ";
    private static final String SOMETHING_PAD_CENTER = "  xyzzy   ";
    private static final String SOMETHING_PAD_RIGHT = "     xyzzy";

    private static final String SOMETHING_PAD_LEFT_PLUS = "xyzzy+++++";
    private static final String SOMETHING_PAD_CENTER_PLUS = "++xyzzy+++";
    private static final String SOMETHING_PAD_RIGHT_PLUS = "+++++xyzzy";
    private static final char PLUS = '+';


    @Test
    public void paddingXyzzy() {
        print(EOL + "paddingXyzzy()");
        assertEquals(AM_HUH, SOMETHING_PAD_LEFT, pad(SOMETHING, SOME_WIDTH));
        assertEquals(AM_HUH, SOMETHING_PAD_LEFT,
                             pad(SOMETHING, SOME_WIDTH, Align.LEFT));
        assertEquals(AM_HUH, SOMETHING_PAD_CENTER,
                             pad(SOMETHING, SOME_WIDTH, Align.CENTER));
        assertEquals(AM_HUH, SOMETHING_PAD_RIGHT,
                             pad(SOMETHING, SOME_WIDTH, Align.RIGHT));
    }

    @Test
    public void paddingXyzzyPlus() {
        print(EOL + "paddingXyzzyPlus()");
        assertEquals(AM_HUH, SOMETHING_PAD_LEFT_PLUS, pad(SOMETHING, SOME_WIDTH, PLUS));
        assertEquals(AM_HUH, SOMETHING_PAD_LEFT_PLUS, pad(SOMETHING, SOME_WIDTH, PLUS, Align.LEFT));
        assertEquals(AM_HUH, SOMETHING_PAD_CENTER_PLUS, pad(SOMETHING, SOME_WIDTH, PLUS, Align.CENTER));
        assertEquals(AM_HUH, SOMETHING_PAD_RIGHT_PLUS, pad(SOMETHING, SOME_WIDTH, PLUS, Align.RIGHT));
    }

    @Test
    public void paddingTruncNoTrunc() {
        print(EOL + "paddingTruncNoTrunc()");
        char pad = 'o';
        assertEquals(AM_NEQ, "Bubb", pad("Bubbles", 4, pad));
        assertEquals(AM_NEQ, "Bubbles", pad("Bubbles", 7, pad));
        assertEquals(AM_NEQ, "Bubblesooo", pad("Bubbles", 10, pad));
        assertEquals(AM_NEQ, "oooBubbles", pad("Bubbles", 10, pad, Align.RIGHT));

        assertEquals(AM_NEQ, "oooX", pad("X", 4, pad, Align.RIGHT));
        assertEquals(AM_NEQ, "oooX", pad("X", 4, pad, Align.RIGHT, false));
        assertEquals(AM_NEQ, "oooX", pad("X", 4, pad, Align.RIGHT, true));

        assertEquals(AM_NEQ, "ABCD", pad("ABCDE", 4, pad, Align.RIGHT));
        assertEquals(AM_NEQ, "ABCD", pad("ABCDE", 4, pad, Align.RIGHT, false));
        assertEquals(AM_NEQ, "ABCDE", pad("ABCDE", 4, pad, Align.RIGHT, true));
    }

    private static final String FOO = "foo";
    private static final String SPACE = " ";
    private static final String EMPTY = "";
    private static final String DOT = ".";

    @Test
    public void nullIsEmptyAndViceVersa() {
        print(EOL + "nullIsEmptyAndViceVersa()");
        assertEquals(AM_NEQ, "", StringUtils.nullIsEmpty(null));

        assertEquals(AM_NEQ, FOO, StringUtils.nullIsEmpty(FOO));
        assertEquals(AM_NEQ, FOO, StringUtils.emptyIsNull(FOO));
        assertEquals(AM_NEQ, FOO, StringUtils.trimmedEmptyIsNull(FOO));

        assertEquals(AM_NEQ, SPACE, StringUtils.emptyIsNull(SPACE));
        assertNull(AM_HUH, StringUtils.trimmedEmptyIsNull(SPACE));
        assertNull(AM_HUH, StringUtils.emptyIsNull(EMPTY));
    }


    @Test
    public void spaces() {
        print(EOL + "spaces()");
        assertEquals(AM_NEQ, "", StringUtils.spaces(0));
        assertEquals(AM_NEQ, "   ", StringUtils.spaces(3));
        assertEquals(AM_NEQ, "       ", StringUtils.spaces(7));
    }

    @Test
    public void isEmpty() {
        print(EOL + "isEmpty()");
       assertTrue(AM_HUH, StringUtils.isEmpty(null));
       assertTrue(AM_HUH, StringUtils.isEmpty(""));
       assertTrue(AM_HUH, StringUtils.isEmpty(" "));
       assertTrue(AM_HUH, StringUtils.isEmpty("     "));
       assertTrue(AM_HUH, StringUtils.isEmpty("\t\t"));
       assertTrue(AM_HUH, StringUtils.isEmpty("\t \t"));
       assertFalse(AM_HUH, StringUtils.isEmpty("\t\t ABC  "));
       assertFalse(AM_HUH, StringUtils.isEmpty("ABC"));
    }

    @Test
    public void safeToString() {
        assertNull("null object should give null toString", StringUtils.safeToString(null));
        assertEquals("incorrect toString", "foo", StringUtils.safeToString("foo"));
    }

    @Test
    public void isAlphaNumeric() {
        print(EOL + "isAlphaNumeric()");
        try {
            StringUtils.isAlphaNumeric(null);
            fail(AM_NOEX);
        } catch (NullPointerException e) {
            print("Caught NPE");
        }

        assertTrue(AM_HUH, StringUtils.isAlphaNumeric("A"));
        assertTrue(AM_HUH, StringUtils.isAlphaNumeric("Abc"));
        assertTrue(AM_HUH, StringUtils.isAlphaNumeric("Abc0123"));
        assertTrue(AM_HUH, StringUtils.isAlphaNumeric("_foo"));
        assertTrue(AM_HUH, StringUtils.isAlphaNumeric("foo_"));
        assertTrue(AM_HUH, StringUtils.isAlphaNumeric("fo_o"));

        assertFalse(AM_HUH, StringUtils.isAlphaNumeric(""));
        assertFalse(AM_HUH, StringUtils.isAlphaNumeric("123"));
        assertFalse(AM_HUH, StringUtils.isAlphaNumeric("123ABC"));
        assertFalse(AM_HUH, StringUtils.isAlphaNumeric("foo.bar"));

        assertTrue(AM_HUH, StringUtils.isAlphaNumeric("A", false));
        assertTrue(AM_HUH, StringUtils.isAlphaNumeric("Abc", false));
        assertTrue(AM_HUH, StringUtils.isAlphaNumeric("Abc0123", false));

        assertFalse(AM_HUH, StringUtils.isAlphaNumeric("", false));
        assertFalse(AM_HUH, StringUtils.isAlphaNumeric("123", false));
        assertFalse(AM_HUH, StringUtils.isAlphaNumeric("123ABC", false));
        assertFalse(AM_HUH, StringUtils.isAlphaNumeric("foo.bar", false));
        assertFalse(AM_HUH, StringUtils.isAlphaNumeric("_foo", false));
        assertFalse(AM_HUH, StringUtils.isAlphaNumeric("foo_", false));
        assertFalse(AM_HUH, StringUtils.isAlphaNumeric("fo_o", false));


    }

    @Test
    public void trimPunct() {
        print(EOL + "trimPunct()");
        try {
            StringUtils.trimPunct(null);
            fail(AM_NOEX);
        } catch (NullPointerException e) {
            print("Caught NPE");
        }

        assertEquals(AM_HUH, "HelloMonkey",
                     StringUtils.trimPunct("Hello.Monkey"));
        assertEquals(AM_HUH, "want banana",
                     StringUtils.trimPunct(".want ban-ana.&*#@()*$<>[]{}!"));
    }

    @Test
    public void trimPunctAndSpaceWith() {
        print(EOL + "trimPunctAndSpaceWith()");
        try {
            StringUtils.trimPunctAndSpaceWith(null, "");
            fail(AM_NOEX);
        } catch (NullPointerException e) {
            print("Caught NPE");
        }

        assertEquals(AM_HUH, "Hello_Monkey",
                     StringUtils.trimPunctAndSpaceWith("Hello.Monkey", "_"));
        assertEquals(AM_HUH, "-want-ban-ana----------------",
                     StringUtils.trimPunctAndSpaceWith(".want ban-ana.&*#@()*$<>[]{}!", "-"));
    }

    private static final String[] STUFF_ARRAY = { "Foo", "Bar", "Baz" };
    private static final List<String> STUFF_LIST = Arrays.asList(STUFF_ARRAY);
    private static final Set<String> STUFF_SET = new TreeSet<String>(STUFF_LIST);

    private static final String STUFF_JOIN_DEFAULT = "Foo, Bar, Baz";
    private static final String STUFF_JOIN_EMPTY = "FooBarBaz";
    private static final String STUFF_JOIN_DOT = "Foo.Bar.Baz";

    private static final String SET_JOIN_DEFAULT = "Bar, Baz, Foo";
    private static final String SET_JOIN_EMPTY = "BarBazFoo";
    private static final String SET_JOIN_DOT = "Bar.Baz.Foo";

    @Test
    public void joinVariants() {
        print(EOL + "joinVariants()");
        assertEquals(AM_NEQ, STUFF_JOIN_DEFAULT, StringUtils.join(STUFF_ARRAY));
        assertEquals(AM_NEQ, STUFF_JOIN_DEFAULT, StringUtils.join(STUFF_LIST));
        assertEquals(AM_NEQ, SET_JOIN_DEFAULT, StringUtils.join(STUFF_SET));

        assertEquals(AM_NEQ, STUFF_JOIN_EMPTY, StringUtils.join(STUFF_ARRAY, EMPTY));
        assertEquals(AM_NEQ, STUFF_JOIN_EMPTY, StringUtils.join(STUFF_LIST, EMPTY));
        assertEquals(AM_NEQ, SET_JOIN_EMPTY, StringUtils.join(STUFF_SET, EMPTY));

        assertEquals(AM_NEQ, STUFF_JOIN_DOT, StringUtils.join(STUFF_ARRAY, DOT));
        assertEquals(AM_NEQ, STUFF_JOIN_DOT, StringUtils.join(STUFF_LIST, DOT));
        assertEquals(AM_NEQ, SET_JOIN_DOT, StringUtils.join(STUFF_SET, DOT));

        assertEquals(AM_NEQ, EMPTY, StringUtils.join(new ArrayList<String>()));
        assertEquals(AM_NEQ, EMPTY, StringUtils.join(new ArrayList<String>(), EMPTY));
        assertEquals(AM_NEQ, EMPTY, StringUtils.join(new ArrayList<String>(), DOT));

        assertNull(AM_HUH, StringUtils.join((Object[]) null));
        assertNull(AM_HUH, StringUtils.join((Collection<?>) null));
        assertNull(AM_HUH, StringUtils.join((Object[]) null, EMPTY));
        assertNull(AM_HUH, StringUtils.join((Collection<?>) null, EMPTY));
        assertNull(AM_HUH, StringUtils.join((Object[]) null, DOT));
        assertNull(AM_HUH, StringUtils.join((Collection<?>) null, DOT));
    }

    @Test
    public void commaSeparated() {
        print(EOL + "commaSeparated()");
        List<String> items = Arrays.asList("xyzzy", "Zork", "Frobozz");
        assertEquals(AM_NEQ, "xyzzy,Zork,Frobozz", StringUtils.commaSeparated(items));
        assertEquals(AM_NEQ, "", StringUtils.commaSeparated(Collections.emptyList()));
    }

    @Test
    public void align() {
        print(EOL + "align()");
        for (Align a: Align.values()) {
            print(a);
            String s = a.toString();
            Align a2 = Align.valueOf(s);
            assertSame(AM_NSR, a, a2);
        }
    }

    @Test(expected = NullPointerException.class)
    public void toCamelCaseNull() {
        StringUtils.toCamelCase((String)null);
    }

    @Test(expected = NullPointerException.class)
    public void toCamelCaseEnumNull() {
        StringUtils.toCamelCase((Enum<?>)null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toCamelCaseIllegalChars() {
        StringUtils.toCamelCase("FOO_BAR!");
    }

    // good camel case examples
    private static final String[] GOOD_CC = {
            "FRODO",                "frodo",
            "THE_HOBBIT",           "theHobbit",
            "LORD_OF_THE_RINGS",    "lordOfTheRings",
            "CHAPTER_1",            "chapter1",
            "THE_3ELVEN_RINGS",     "the3elvenRings",
    };

    @Test
    public void toCamelCase() {
        print(EOL + "toCamelCase()");
        for (int i=0,n=GOOD_CC.length; i<n; i+=2) {
            String from = GOOD_CC[i];
            String expected = GOOD_CC[i+1];
            String actual = StringUtils.toCamelCase(from);
            print("  " + from + "  --> " + actual);
            assertEquals(M_BC, expected, actual);
        }
    }

    @Test(expected = NullPointerException.class)
    public void fromCamelCaseNull() {
        StringUtils.fromCamelCase(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromCamelCaseBad() {
        StringUtils.fromCamelCase("fooBar!");
    }

    private static final String M_BC = "bad conversion";

    @Test
    public void fromCamelCase() {
        print(EOL + "fromCamelCase()");
        for (int i=0,n=GOOD_CC.length; i<n; i+=2) {
            String from = GOOD_CC[i+1];
            String expected = GOOD_CC[i];
            String actual = StringUtils.fromCamelCase(from);
            print("  " + from + "  --> " + actual);
            assertEquals(M_BC, expected, actual);
        }
    }

    private static enum Colors {
        RED, GREENY_BLUE, SEA_BLUE, A_BRIGHTER_SHADE_OF_PINK
    }

    @Test
    public void toCamelCaseFromEnums() {
        print(EOL + "toCamelCaseFromEnums()");
        assertEquals(M_BC, "red", StringUtils.toCamelCase(Colors.RED));
        assertEquals(M_BC, "greenyBlue", StringUtils.toCamelCase(Colors.GREENY_BLUE));
        assertEquals(M_BC, "seaBlue", StringUtils.toCamelCase(Colors.SEA_BLUE));
        assertEquals(M_BC, "aBrighterShadeOfPink", StringUtils.toCamelCase(Colors.A_BRIGHTER_SHADE_OF_PINK));
    }

    private static final String PREFIX_1 = "foo";
    private static final String PREFIX_2 = "BAR";

    @Test
    public void prefixedCamelCaseFromEnums() {
        print(EOL + "prefixedCamelCaseFromEnums()");
        assertEquals(M_BC, PREFIX_1 + "Red", StringUtils.toCamelCase(PREFIX_1, Colors.RED));
        assertEquals(M_BC, PREFIX_2 + "GreenyBlue", StringUtils.toCamelCase(PREFIX_2, Colors.GREENY_BLUE));
        assertEquals(M_BC, "red", StringUtils.toCamelCase("", Colors.RED));
        assertEquals(M_BC, "greenyBlue", StringUtils.toCamelCase(null, Colors.GREENY_BLUE));
    }

    // TODO: Add argument verification tests.


    private static final String FRODO = "Baggins";
    private static final String SAM = "Gamgee";
    private static final String MERRY = "Brandybuck";
    private static final String PIPPIN = "Took";

    @Test
    public void concatSamples() {
        print(EOL + "concatSamples()");
        assertEquals(AM_NEQ, "", StringUtils.concat());
        assertEquals(AM_NEQ, "Baggins", StringUtils.concat(FRODO));
        assertEquals(AM_NEQ, "BagginsGamgee", StringUtils.concat(FRODO, SAM));
        assertEquals(AM_NEQ, "BagginsTookGamgee", StringUtils.concat(FRODO, PIPPIN, SAM));
        assertEquals(AM_NEQ, "Brandybuck37", StringUtils.concat(MERRY, 37));
        assertEquals(AM_NEQ, "SEA_BLUEtrueBaggins", StringUtils.concat(Colors.SEA_BLUE, true, FRODO));
    }

    @Test
    public void stripCommentsOne() {
        String path = "org/opendaylight/util/stringutils/";
        ClassLoader cl = getClass().getClassLoader();

        print(EOL + "stripCommentsOne()");
        String input = TestTools.getFileContents(path + "stripInput.txt", cl);
        print(EOL + "===");
        print(input);
        String withoutComments = StringUtils.stripCommentLines(input);
        print(EOL + "===");
        print(withoutComments);
        assertData(path + "stripOutput.txt", withoutComments, cl);
    }

    @Test
    public void getNullFile() throws IOException {
        String result = StringUtils.getFileContents("nothing/here", classLoader);
        assertNull(AM_HUH, result);
    }

    @Test
    public void getFileContents() throws IOException {
        print(EOL + "getFileContents()");
        String path = "org/opendaylight/util/stringutils/contents.txt";
        String result = StringUtils.getFileContents(path, classLoader);
        print(result);
        String exp = "Foo\n" +
                "Bar\n" +
                "Baz\n" +
                "and all that jazz!\n";
        String normExp = StringUtils.normalizeEOL(exp);
        String normAct = StringUtils.normalizeEOL(result);
        assertEquals(AM_NEQ, normExp, normAct);
    }

    @Test(expected = NullPointerException.class)
    public void formatNullStr() {
        StringUtils.format(null);
    }

    private static final String FMT = "{} => {}";
    @Test
    public void formatNoObjects() {
        print(EOL + "formatNoObjects()");
        String result = StringUtils.format(FMT);
        print(result);
        assertEquals(AM_NEQ, FMT, result);
    }

    @Test
    public void formatTooFewObjects() {
        print(EOL + "formatTooFewObjects()");
        String result = StringUtils.format(FMT, 123);
        print(result);
        assertEquals(AM_NEQ, "123 => {}", result);
    }

    @Test
    public void formatTooManyObjects() {
        print(EOL + "formatTooManyObjects()");
        String result = StringUtils.format(FMT, 123, "FOO", "Bar");
        print(result);
        assertEquals(AM_NEQ, "123 => FOO", result);
    }

    @Test
    public void formatExactlyRightNumberOfObjects() {
        print(EOL + "formatExactlyRightNumberOfObjects()");
        String result = StringUtils.format(FMT, 123, "FOO");
        print(result);
        assertEquals(AM_NEQ, "123 => FOO", result);
    }

    @Test
    public void quotedString() {
        print(EOL + "quotedString()");
        String result = StringUtils.quoted("foo");
        print(result);
        assertEquals(AM_NEQ, "\"foo\"", result);
    }
    @Test
    public void quotedNull() {
        print(EOL + "quotedNull()");
        String result = StringUtils.quoted(null);
        print(result);
        assertEquals(AM_NEQ, "null", result);
    }
}
