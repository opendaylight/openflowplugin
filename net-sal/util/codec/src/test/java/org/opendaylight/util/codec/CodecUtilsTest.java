/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.codec;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

/**
 * This JUnit test class tests the {@code CodecUtils} class.
 *
 * @author Simon Hunt
 */
public class CodecUtilsTest {

    // these first tests confirm that the helper methods are working correctly...
    // Delimiters in order:   ,  ~  ;  :  !

    // Delim   First Occ.  Count
    //   ,       1           3
    //   ~       2           2
    //   ;       2           1
    //   :       1           4
    //   !       4           3

    private static final String[] TEST_DATA = {
            "xxx",          // 0
            "xxx:x,xx",     // 1
            "x;xxxx~xx~x",  // 2
            "x",            // 3
            "!!!",          // 4
            ":xx:xx:",      // 5
            "a,b,c",        // 6
    };

    @Test
    public void findMatch() {
        assertEquals(AM_HUH, 1, CodecUtils.findMatch(",", TEST_DATA));
        assertEquals(AM_HUH, 2, CodecUtils.findMatch("~", TEST_DATA));
        assertEquals(AM_HUH, 2, CodecUtils.findMatch(";", TEST_DATA));
        assertEquals(AM_HUH, 1, CodecUtils.findMatch(":", TEST_DATA));
        assertEquals(AM_HUH, 4, CodecUtils.findMatch("!", TEST_DATA));
        assertEquals(AM_HUH, -1, CodecUtils.findMatch("@", TEST_DATA));
    }

    @Test
    public void countOccurrences() {
        assertEquals(AM_HUH, 3, CodecUtils.countOccurrences(",", TEST_DATA, 1));
        assertEquals(AM_HUH, 3, CodecUtils.countOccurrences(",", TEST_DATA, 0));
        assertEquals(AM_HUH, 2, CodecUtils.countOccurrences("~", TEST_DATA, 2));
        assertEquals(AM_HUH, 2, CodecUtils.countOccurrences("~", TEST_DATA, 0));
        assertEquals(AM_HUH, 1, CodecUtils.countOccurrences(";", TEST_DATA, 2));
        assertEquals(AM_HUH, 1, CodecUtils.countOccurrences(";", TEST_DATA, 0));
        assertEquals(AM_HUH, 4, CodecUtils.countOccurrences(":", TEST_DATA, 1));
        assertEquals(AM_HUH, 4, CodecUtils.countOccurrences(":", TEST_DATA, 0));
        assertEquals(AM_HUH, 3, CodecUtils.countOccurrences("!", TEST_DATA, 4));
        assertEquals(AM_HUH, 3, CodecUtils.countOccurrences("!", TEST_DATA, 0));

        assertEquals(AM_HUH, 0, CodecUtils.countOccurrences("@", TEST_DATA, 0));
    }

    @Test
    public void whichDelimiter() {
        assertEquals(AM_HUH, ";", CodecUtils.chooseDelim(TEST_DATA).toString());
        // NOTE: other test cases below check more thoroughly the algorithm of
        //   picking the delimiter with least occurences in the data
    }

    //=======

    
    @Test (expected = NullPointerException.class)
    public void encodeStringArrayNull() {
        CodecUtils.encodeStringArray(null);
    }

    @Test (expected = NullPointerException.class)
    public void decodeStringArrayNull() {
        CodecUtils.decodeStringArray(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void decodeStringArrayBadFormat() {
        CodecUtils.decodeStringArray("xyz,2,xx");
    }

    @Test (expected = IllegalArgumentException.class)
    public void decodeStringArrayBadFormatLonger() {
        CodecUtils.decodeStringArray("abc,13,xx,xx,xx,xx,xx,xx,xx,xx,xx,xx,xx,xx,xx");
    }

    private static final String[] ARRAY = {
            "one",
            "two",
            "three",
            "something, with c,,o,,m,,m,,a,,s embedded",
            "",
            "DONE",
            "@",
            "@@",
            "@@@",
            "@,",
            "@@,",
            ",@",
            ",,@",
            "how about t~i~l~d~e~s then?",
    };

    @Test
    public void basicArrayEncoding() {
        String encArray = CodecUtils.encodeStringArray(ARRAY);
        print(encArray);
        assertTrue(AM_HUH, encArray.startsWith(CodecUtils.getMagic()));
        String[] copy = CodecUtils.decodeStringArray(encArray);
        assertArrayEquals(AM_NEQ, ARRAY, copy);
    }

    private static final String BAD_ENCODED_DATA_SIZE_MISMATCH =
            "cu1,13,one,two,three,something,,DONE,";

    @Test (expected = RuntimeException.class)
    public void badEncoding() {
        CodecUtils.decodeStringArray(BAD_ENCODED_DATA_SIZE_MISMATCH);
    }


    private static final String[] S1 = { };
    private static final String[] S2 = { "a" };
    private static final String[] S3 = { "a", "b" };
    private static final String[] S4 = { "a,b" };
    private static final String[] S5 = { "a,b," };
    private static final String[] S6 = { "," };
    private static final String[] S7 = { "", "", "" };
    private static final String[] S8 = { "@", "@,@" };
    private static final String[] S9 = { "@", "@,@", " " };
    private static final String[] S10 = { "@", "@,@,", ",,,,," };
    private static final String[] S11 = { "[1, 2, 3, 4, 5]", "abc,", "this, that, or the other" };
    private static final String[] S12 = { "5 coin(s) @ $1.00 each, unlimited edition" };
    private static final String[] S13 = { "one", null, "three" };

    private static final String[][] SARRAYS = {
            S1, S2, S3, S4, S5,
            S6, S7, S8, S9, S10,
            S11, S12,
    };

    @Test
    public void aBunchOfArrays() {
        for (String[] sarr: SARRAYS) {
            checkAnArray(sarr);
        }
    }

    private void checkAnArray(String[] arr) {
        print(EOL + "checkAnArray..");
        print("+array = " + Arrays.toString(arr));
        String enc = CodecUtils.encodeStringArray(arr);
        print("+encoded = " + enc);

        String[] backAgain = CodecUtils.decodeStringArray(enc);
        assertArrayEquals(AM_HUH, arr, backAgain);
        print(">> copy array = " + Arrays.toString(backAgain));
    }

    @Test
    public void specialArrayTestWithNullElement() {
        print(EOL + "specialArrayTestWithNullElement()");
        print("+array = " + Arrays.toString(S13));
        String enc = CodecUtils.encodeStringArray(S13);
        print("+encoded = " + enc);
        String[] backAgain = CodecUtils.decodeStringArray(enc);
        print(">> copy array = " + Arrays.toString(backAgain));
        int idx = 0;
        for (String orig: S13) {
            String expected = orig==null ? "" : orig;
            assertEquals(AM_HUH, expected, backAgain[idx++]);
        }
    }

    @Test
    public void allDelimiters() {
        print(EOL + "allDelimiters()");

        String[] stuff = {
                "a comma, is here",
                "how about ~ a tilde ~ or two?",
                "then, there is the semi-colon; which is used twice;",
                "And::The colon is also used~so there",
                "And a bang (!) character.",
        };
        verifyStuff(stuff, "allDelimiters()", null);
    }

    private static final String TWO_COMMAS = "abc,def,ghi";
    private static final String TWO_TILDES = "jkl~mno~pqr";
    private static final String TWO_SEMICOLONS = "stu;vwx;yzz";
    private static final String TWO_COLONS = "abc:def:ggg";
    private static final String TWO_BANGS = "jkl!mno!pqr";
    private static final String TWO_ESCAPES = "one@two@three";

    private static final String THREE_COMMAS = "a,b,c,d";
    private static final String THREE_TILDES = "j~k~l~m";
    private static final String THREE_SEMICOLONS = "s;t;u;v";
    private static final String THREE_COLONS = "w:x:y:z";
    private static final String THREE_BANGS = "n!o!p!q";

    @Test
    public void allButBangs() {
        String[] stuff = {TWO_COMMAS, TWO_TILDES, TWO_SEMICOLONS, TWO_COLONS, TWO_ESCAPES};
        verifyStuff(stuff, "allButBangs()", "!");
    }

    @Test
    public void allButColons() {
        String[] stuff = {TWO_COMMAS, TWO_TILDES, TWO_SEMICOLONS, TWO_BANGS, TWO_ESCAPES};
        verifyStuff(stuff, "allButColons()", ":");
    }

    @Test
    public void allButSemicolons() {
        String[] stuff = {TWO_COMMAS, TWO_TILDES, TWO_COLONS, TWO_BANGS, TWO_ESCAPES};
        verifyStuff(stuff, "allButSemicolons()", ";");
    }

    @Test
    public void allButTildes() {
        String[] stuff = {TWO_COMMAS, TWO_SEMICOLONS, TWO_COLONS, TWO_BANGS, TWO_ESCAPES};
        verifyStuff(stuff, "allButTildes()", "~");
    }

    @Test
    public void allButCommas() {
        String[] stuff = {TWO_TILDES, TWO_SEMICOLONS, TWO_COLONS, TWO_BANGS, TWO_ESCAPES};
        verifyStuff(stuff, "allButCommas()", ",");
    }

    @Test
    public void leastBangs() {
        String[] stuff = {THREE_COMMAS, THREE_TILDES, THREE_SEMICOLONS, THREE_COLONS, TWO_BANGS};
        verifyStuff(stuff, "leastBangs()", "!");
    }

    @Test
    public void leastColons() {
        String[] stuff = {THREE_COMMAS, THREE_TILDES, THREE_SEMICOLONS, TWO_COLONS, THREE_BANGS};
        verifyStuff(stuff, "leastColons()", ":");
    }

    @Test
    public void leastSemicolons() {
        String[] stuff = {THREE_COMMAS, THREE_TILDES, TWO_SEMICOLONS, THREE_COLONS, THREE_BANGS};
        verifyStuff(stuff, "leastSemicolons()", ";");
    }

    @Test
    public void leastTildes() {
        String[] stuff = {THREE_COMMAS, TWO_TILDES, THREE_SEMICOLONS, THREE_COLONS, THREE_BANGS};
        verifyStuff(stuff, "leastTildes()", "~");
    }

    @Test
    public void leastCommas() {
        String[] stuff = {TWO_COMMAS, THREE_TILDES, THREE_SEMICOLONS, THREE_COLONS, THREE_BANGS};
        verifyStuff(stuff, "leastCommas()", ",");
    }

    private void verifyStuff(String[] stuff, String method, String picked) {
        print(EOL + method);
        print("+array = " + Arrays.toString(stuff));
        String enc = CodecUtils.encodeStringArray(stuff);
        print("+encoded = " + enc);
        String[] backAgain = CodecUtils.decodeStringArray(enc);
        print(">> copy array = " + Arrays.toString(backAgain));
        assertArrayEquals(AM_HUH, stuff, backAgain);
        if (picked != null) {
            assertEquals(AM_HUH, enc.substring(enc.length()-1), picked);
        }
    }


    @Test
    public void stringList() {
        List<String> myList = Arrays.asList("one:two", "three;four", "alpha!beta");
        String enc = CodecUtils.encodeStringList(myList);
        print(EOL + "stringList()");
        print(" +enc: " + enc);
        List<String> copy = CodecUtils.decodeStringList(enc);
        assertEquals(AM_NEQ, myList, copy);
    }


    //======================================================================

    @Test (expected = NullPointerException.class)
    public void encPrimitiveNull() {
        CodecUtils.encodePrimitive(null);
    }

    private class SomeClass { }

    @Test (expected = IllegalArgumentException.class)
    public void encPrimitiveNonPrimitive() {
        CodecUtils.encodePrimitive(new SomeClass());
    }

    @Test (expected = NullPointerException.class)
    public void decPrimitiveNull() {
        CodecUtils.decodePrimitive(null);
    }



    @Test
    public void primitiveBoolean() {
        print(EOL + "primitiveBoolean()");
        checkPrimitive(true);
        checkPrimitive(false);
    }

    @Test
    public void primitiveChar() {
        print(EOL + "primitiveChar()");
        checkPrimitive('a');
        checkPrimitive('Z');
    }

    @Test
    public void primitiveByte() {
        print(EOL + "primitiveByte()");
        checkPrimitive((byte) 3);
        checkPrimitive((byte) 0);
        checkPrimitive((byte) -127);
        checkPrimitive((byte) -129);
        checkPrimitive((byte) 255);
    }

    @Test
    public void primitiveShort() {
        print(EOL + "primitiveShort()");
        checkPrimitive((short) 4);
        checkPrimitive((short) 0);
        checkPrimitive((short) 1);
        checkPrimitive((short) -1);
        checkPrimitive(Short.MIN_VALUE);
        checkPrimitive(Short.MAX_VALUE);
    }

    @Test
    public void primitiveInt() {
        print(EOL + "primitiveInt()");
        checkPrimitive(5);
        checkPrimitive(0);
        checkPrimitive(1);
        checkPrimitive(-1);
        checkPrimitive(Integer.MIN_VALUE);
        checkPrimitive(Integer.MAX_VALUE);
    }

    @Test
    public void primitiveLong() {
        print(EOL + "primitiveLong()");
        checkPrimitive((long) 6);
        checkPrimitive((long) 0);
        checkPrimitive((long) 1);
        checkPrimitive((long) -1);
        checkPrimitive(Long.MIN_VALUE);
        checkPrimitive(Long.MAX_VALUE);
    }

    @Test
    public void primitiveFloat() {
        print(EOL + "primitiveFloat()");
        checkPrimitive((float) 7.0);
        checkPrimitive((float) 0);
        checkPrimitive((float) 1);
        checkPrimitive((float) -1);
        checkPrimitive(Float.MIN_VALUE);
        checkPrimitive(Float.MAX_VALUE);
    }

    @Test
    public void primitiveDouble() {
        print(EOL + "primitiveDouble()");
        checkPrimitive(8.0);
        checkPrimitive((double) 0);
        checkPrimitive((double) 1);
        checkPrimitive((double) -1);
        checkPrimitive(Double.MIN_VALUE);
        checkPrimitive(Double.MAX_VALUE);
    }

    @Test
    public void primitiveString() {
        print(EOL + "primitiveString()");
        checkPrimitive("");
        checkPrimitive("A");
        checkPrimitive("AB");
        checkPrimitive("ABC");
    }

    
    private void checkPrimitive(Object p) {
        String enc = CodecUtils.encodePrimitive(p);
        Class<?> cls = p.getClass();
        print("  " + p);
        print("    p.enc = " + enc + " (" + cls + ")");
        Object v = CodecUtils.decodePrimitive(enc);
        assertEquals(AM_HUH, p, v);
    }

    //=== further checks on the decoding primitive method
    @Test
    public void decodePrimitiveBoolean() {
        Object v = CodecUtils.decodePrimitive("BT");    // BT is true
        assertEquals(AM_HUH, Boolean.TRUE, v);

        v = CodecUtils.decodePrimitive("BF");           // BF is false
        assertEquals(AM_HUH, Boolean.FALSE, v);

        v = CodecUtils.decodePrimitive("BQ");           // B<anything-else> is also false
        assertEquals(AM_HUH, Boolean.FALSE, v);
    }

    @Test
    public void decodePrimitiveInt() {
        Object v = CodecUtils.decodePrimitive("I1");
        assertEquals(AM_HUH, Integer.valueOf("1"), v);
    }

    @Test (expected = NumberFormatException.class)
    public void decodePrimitiveIntBad() {
        CodecUtils.decodePrimitive("I3.5");
    }

    @Test
    public void decodePrimitiveFloat() {
        Object v = CodecUtils.decodePrimitive("F3.5");
        assertEquals(AM_HUH, Float.valueOf("3.5"), v);
    }

    @Test (expected = IllegalArgumentException.class)
    public void decodeBadPrimitive1() {
        CodecUtils.decodePrimitive("X1");
    }

    @Test (expected = IllegalArgumentException.class)
    public void decodeBadPrimitive2() {
        CodecUtils.decodePrimitive("");
    }

    @Test (expected = IllegalArgumentException.class)
    public void decodeBadPrimitive3() {
        CodecUtils.decodePrimitive("B");
    }


    @Test
    public void magic() {
        String magic = CodecUtils.getMagic();
        assertEquals(AM_NEQ, CodecUtils.MAGIC, magic);
    }
}
