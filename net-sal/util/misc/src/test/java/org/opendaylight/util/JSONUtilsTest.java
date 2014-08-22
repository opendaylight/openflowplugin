/*
 * (c) Copyright 2011-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.opendaylight.util.JSONUtils.Array;
import org.opendaylight.util.JSONUtils.Item;
import org.opendaylight.util.JSONUtils.Primitive;
import org.junit.Test;

import java.util.Set;

import static org.opendaylight.util.junit.TestTools.AM_HUH;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.AM_NSR;
import static org.opendaylight.util.junit.TestTools.AM_UXS;
import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.junit.TestTools.verifyEqual;
import static org.opendaylight.util.junit.TestTools.verifyNotEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Suite of JSONUtil tests.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 * @author Scott Simes
 */
public class JSONUtilsTest {

    private enum Stooges { MO, CURLY, LARRY }

    private Primitive primitive;
    private Item item;
    private Array array;

    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String BAZ = "baz";

    private static final int INT = 941;

    private static final long LONG = 1234567899L;

    private static final String M_BAD_PRIM = "incorrect primitive";
    private static final String M_BAD_ITEM = "incorrect item";
    private static final String M_BAD_ARRY = "incorrect array";

    private static enum Version {
        V_1_0,
        V_2_0,
        V_3_0
    }

    @Test
    public void toKey() {
        assertEquals(AM_NEQ, "v_1_0", JSONUtils.toKey(Version.V_1_0));
    }

    @Test
    public void fromKey() {
        assertEquals(AM_NEQ, Version.V_2_0,
                JSONUtils.fromKey(Version.class, "v_2_0"));
    }

    // === Primitive ===
    @Test
    public void primitive() {
        print(EOL + "primitive()");
        primitive = new Primitive(FOO);
        print(primitive);
        assertEquals(M_BAD_PRIM, "\"foo\"", primitive.toString());
        assertEquals(M_BAD_PRIM, "\"foo\"", primitive.inline().toString());

        primitive = new Primitive(LONG);
        print(primitive);
        assertEquals(M_BAD_PRIM, "\"1234567899\"", primitive.toString());
        assertEquals(M_BAD_PRIM, "\"1234567899\"", primitive.inline().toString());

        primitive = new Primitive(INT);
        print(primitive);
        assertEquals(M_BAD_PRIM, "\"941\"", primitive.toString());
        assertEquals(M_BAD_PRIM, "\"941\"", primitive.inline().toString());

        primitive = new Primitive(Stooges.LARRY);
        print(primitive);
        assertEquals(M_BAD_PRIM, "\"LARRY\"", primitive.toString());
        assertEquals(M_BAD_PRIM, "\"LARRY\"", primitive.inline().toString());

        primitive = new Primitive(true);
        print(primitive);
        assertEquals(M_BAD_PRIM, "true", primitive.toString());
        assertEquals(M_BAD_PRIM, "true", primitive.inline().toString());
    }

    @Test
    public void primitiveInline() {
        print(EOL + "primitiveInline()");
        primitive = new Primitive(FOO);
        assertFalse(AM_HUH, primitive.isInline());
        primitive.inline();
        assertTrue(AM_HUH, primitive.isInline());
    }

    @Test
    public void equality() {
        print(EOL + "equality()");
        Primitive foo1 = new Primitive(FOO);
        Primitive foo2 = new Primitive(FOO);
        verifyEqual(foo1, foo2);
        assertNotSame("same?", foo1, foo2);
        assertFalse(AM_HUH, foo1.isInline());
        assertFalse(AM_HUH, foo2.isInline());
        // flip inline on foo2
        foo2.inline();
        assertTrue(AM_HUH, foo2.isInline());
        print(foo1);
        print(foo2);
        print("foo1.equals(foo2) = " + foo1.equals(foo2));
        verifyEqual(foo1, foo2);

        Primitive bar = new Primitive(BAR);
        verifyNotEqual(bar, foo1);

        // from an enum and a string:
        foo1 = new Primitive(Stooges.CURLY);
        foo2 = new Primitive("CURLY");
        verifyEqual(foo1, foo2);

        foo1 = new Primitive(9999999999L);
        foo2 = new Primitive("9999999999");
        verifyEqual(foo1, foo2);

        foo1 = new Primitive(37);
        foo2 = new Primitive("37");
        verifyEqual(foo1, foo2);

        foo1 = new Primitive(true);
        foo2 = new Primitive(true);
        verifyEqual(foo1, foo2);

        // for completeness
        verifyEqual(foo1, foo1);
        verifyNotEqual(foo1, "BLAGH");
    }

    @Test(expected = NullPointerException.class)
    public void nullString() {
        primitive = new Primitive((String)null);
    }

    @Test(expected = NullPointerException.class)
    public void nullEnum() {
        primitive = new Primitive((Enum<?>)null);
    }


    // === Item ===

    @Test
    public void empty() {
        print(EOL + "empty()");
        item = new Item();
        print(item);
        assertEquals(M_BAD_ITEM, "{\n}\n", item.toString());
        assertEquals(M_BAD_ITEM, "{ }", item.inline().toString());
        assertEquals(AM_UXS, 0, item.size());
    }

    @Test
    public void itemEquality() {
        print(EOL + "itemEquality()");
        Item mo1 = new Item().add("M", "MO");
        Item mo2 = new Item().add("M", Stooges.MO);
        verifyEqual(mo1, mo2);

        item = new Item().add("L", Stooges.LARRY);
        verifyNotEqual(mo1, item);

        mo1.add("L", "LARRY").add("C", "CURLY");
        item.add("M", Stooges.MO).add("C", Stooges.CURLY);
        print(mo1);
        print(item);
        verifyEqual(mo1, item);
        print(" irrespective of inline setting...");
        item.inline();
        print(mo1);
        print(item);
        verifyEqual(mo1, item);

        // for completeness
        verifyEqual(item, item);
        verifyNotEqual(item, "DOH");
    }

    @Test
    public void itemEqualityOrderIndependent() {
        print(EOL + "itemEqualityOrderIndependent()");
        Item item1 = new Item().add("L", Stooges.LARRY).add("M", Stooges.MO).add("C", Stooges.CURLY);
        Item item2 = new Item().add("C", Stooges.CURLY).add("L", Stooges.LARRY).add("M", Stooges.MO);
        verifyEqual(item1, item2);
        assertEquals(AM_NEQ, item1.toString(), item2.toString());
    }


    @Test
    public void itemAddEnumPrimitive() {
        print(EOL + "itemAddEnumPrimitive()");
        item = new Item().add(Stooges.MO, new Primitive(2));
        print(item);
        assertEquals(M_BAD_ITEM, "{\n" +
                "  \"MO\": \"2\"\n" +
                "}\n", item.toString());
        assertEquals(M_BAD_ITEM, "{ \"MO\": \"2\" }", item.inline().toString());
        assertEquals(AM_UXS, 1, item.size());

        item = new Item().add(Stooges.MO, new Primitive(2));
        item.add(Stooges.LARRY, new Primitive(1));
        item.add(Stooges.CURLY, new Primitive(3));
        print(item);
        assertEquals(M_BAD_ITEM, "{\n" +
                "  \"CURLY\": \"3\",\n" +
                "  \"LARRY\": \"1\",\n" +
                "  \"MO\": \"2\"\n" +
                "}\n", item.toString());
        assertEquals(M_BAD_ITEM, "{ \"CURLY\": \"3\", \"LARRY\": \"1\", \"MO\": \"2\" }",
                     item.inline().toString());
        assertEquals(AM_UXS, 3, item.size());
    }

    @Test
    public void itemAddStringPrimitive() {
        print (EOL + "itemAddStringPrimitive()");
        item = new Item().add(FOO, new Primitive(LONG));
        print(item);
        assertEquals(M_BAD_ITEM, "{\n" +
                "  \"foo\": \"1234567899\"\n" +
                "}\n", item.toString());
        assertEquals(M_BAD_ITEM, "{ \"foo\": \"1234567899\" }", item.inline().toString());
        assertEquals(AM_UXS, 1, item.size());
    }

    @Test
    public void itemAddEnumString() {
        print(EOL + "itemAddEnumString()");
        item = new Item().add(Stooges.LARRY, BAR);
        print(item);
        assertEquals(M_BAD_ITEM, "{\n" +
                "  \"LARRY\": \"bar\"\n" +
                "}\n", item.toString());
        assertEquals(M_BAD_ITEM, "{ \"LARRY\": \"bar\" }", item.inline().toString());
        assertEquals(AM_UXS, 1, item.size());
    }

    @Test
    public void itemAddStringString() {
        print(EOL + "itemAddStringString()");
        item = new Item().add(BAZ, FOO);
        print(item);
        assertEquals(M_BAD_ITEM, "{\n" +
                "  \"baz\": \"foo\"\n" +
                "}\n", item.toString());
        assertEquals(M_BAD_ITEM, "{ \"baz\": \"foo\" }", item.inline().toString());
        assertEquals(AM_UXS, 1, item.size());
    }

    @Test
    public void itemAddEnumInt() {
        print(EOL + "itemAddEnumInt()");
        item = new Item().add(Stooges.MO, INT);
        print(item);
        assertEquals(M_BAD_ITEM, "{\n" +
                "  \"MO\": \"941\"\n" +
                "}\n", item.toString());
        assertEquals(M_BAD_ITEM, "{ \"MO\": \"941\" }", item.inline().toString());
        assertEquals(AM_UXS, 1, item.size());
    }

    @Test
    public void itemAddStringInt() {
        print(EOL + "itemAddStringInt()");
        item = new Item().add(BAR, INT);
        print(item);
        assertEquals(M_BAD_ITEM, "{\n" +
                "  \"bar\": \"941\"\n" +
                "}\n", item.toString());
        assertEquals(M_BAD_ITEM, "{ \"bar\": \"941\" }", item.inline().toString());
        assertEquals(AM_UXS, 1, item.size());
    }

    @Test
    public void itemAddEnumLong() {
        print(EOL + "itemAddEnumLong()");
        item = new Item().add(Stooges.LARRY, LONG);
        print(item);
        assertEquals(M_BAD_ITEM, "{\n" +
                "  \"LARRY\": \"1234567899\"\n" +
                "}\n", item.toString());
        assertEquals(M_BAD_ITEM, "{ \"LARRY\": \"1234567899\" }", item.inline().toString());
        assertEquals(AM_UXS, 1, item.size());
    }

    @Test
    public void itemAddStringLong() {
        print(EOL + "itemAddStringLong()");
        item = new Item().add(FOO, LONG);
        print(item);
        assertEquals(M_BAD_ITEM, "{\n" +
                "  \"foo\": \"1234567899\"\n" +
                "}\n", item.toString());
        assertEquals(M_BAD_ITEM, "{ \"foo\": \"1234567899\" }", item.inline().toString());
        assertEquals(AM_UXS, 1, item.size());
    }

    @Test
    public void itemAddEnumEnum() {
        print(EOL + "itemAddEnumEnum()");
        item = new Item().add(Stooges.LARRY, Stooges.MO);
        print(item);
        assertEquals(M_BAD_ITEM, "{\n" +
                "  \"LARRY\": \"MO\"\n" +
                "}\n", item.toString());
        assertEquals(M_BAD_ITEM, "{ \"LARRY\": \"MO\" }", item.inline().toString());
        assertEquals(AM_UXS, 1, item.size());
    }

    @Test
    public void itemAddEnumBoolean() {
        print(EOL + "itemAddEnumBoolean()");
        item = new Item().add(Stooges.LARRY, true);
        print(item);
        assertEquals(M_BAD_ITEM, "{\n  \"LARRY\": true\n}\n", item.toString());
        assertEquals(M_BAD_ITEM, "{ \"LARRY\": true }", item.inline().toString());
        assertEquals(AM_UXS, 1, item.size());
    }

    @Test
    public void itemAddStringBoolean() {
        print(EOL + "itemAddStringBoolean()");
        item = new Item().add(FOO, false);
        print(item);
        assertEquals(M_BAD_ITEM, "{\n  \"foo\": false\n}\n", item.toString());
        assertEquals(M_BAD_ITEM, "{ \"foo\": false }", item.inline().toString());
        assertEquals(AM_UXS, 1, item.size());
    }

    @Test
    public void itemAddStringEnum() {
        print(EOL + "itemAddStringEnum()");
        item = new Item().add(BAZ, Stooges.MO);
        print(item);
        assertEquals(M_BAD_ITEM, "{\n" +
                "  \"baz\": \"MO\"\n" +
                "}\n", item.toString());
        assertEquals(M_BAD_ITEM, "{ \"baz\": \"MO\" }", item.inline().toString());
        assertEquals(AM_UXS, 1, item.size());
    }

    @Test
    public void itemItem() {
        print(EOL + "itemItem()");
        item = new Item()
            .add("f", new Item().add("foo", "FOO"))
            .add("b", new Item().add("bar", "BAR"))
            .add("d", new Item().add("doh", "DOH"));
        print(item);
        assertEquals(M_BAD_ITEM, "{\n" +
                     "  \"b\": {\n    \"bar\": \"BAR\"\n  },\n" +
                     "  \"d\": {\n    \"doh\": \"DOH\"\n  },\n" +
                     "  \"f\": {\n    \"foo\": \"FOO\"\n  }\n}\n",
                     item.toString());
    }

    @Test
    public void unsafeData() {
        print(EOL + "unsafeData()");
        String fooString = "Foo" + "\n" + "\"Bar\"";
        item = new Item().add("data", fooString);
        print(item);
        String expected = "{\n" +
                "  \"data\": \"Foo\\n\\\"Bar\\\"\"\n" +
                "}\n";
        assertEquals("unsafe data failure", expected, item.toString());
    }

    @Test
    public void removeData() {
        print(EOL + "removeData()");
        item = new Item()
            .add("f", new Item().add("foo", "FOO"))
            .add("b", new Item().add("bar", "BAR"))
            .add("d", new Item().add("doh", "DOH"));
        print(item);
        assertEquals(M_BAD_ITEM, "{\n" +
                     "  \"b\": {\n    \"bar\": \"BAR\"\n  },\n" +
                     "  \"d\": {\n    \"doh\": \"DOH\"\n  },\n" +
                     "  \"f\": {\n    \"foo\": \"FOO\"\n  }\n}\n",
                     item.toString());

        item.remove("f").remove("b");
        print(item);
        assertEquals(M_BAD_ITEM, "{\n" +
                "  \"d\": {\n" +
                "    \"doh\": \"DOH\"\n" +
                "  }\n" +
                "}\n", item.toString());

        item.add(Stooges.MO, "Mr.Mo").add(Stooges.CURLY, "Mr.Curly").inline();
        print(item);
        assertEquals(M_BAD_ITEM, "{ \"CURLY\": \"Mr.Curly\", \"MO\": \"Mr.Mo\", \"d\": { \"doh\": \"DOH\" } }",
                     item.toString());

        item.remove(Stooges.MO);
        assertEquals(AM_UXS, 2, item.size());
        assertEquals(M_BAD_ITEM, "{ \"CURLY\": \"Mr.Curly\", \"d\": { \"doh\": \"DOH\" } }", item.toString());
    }

    @Test(expected = NullPointerException.class)
    public void itemRemoveNullEnum() {
        item = new Item().remove((Enum<?>) null);
    }

    @Test(expected = NullPointerException.class)
    public void itemRemoveNullString() {
        item = new Item().remove((String) null);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullEnumPrimitive1() {
        item = new Item().add((Enum<?>)null, (Primitive)null);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullEnumPrimitive2() {
        item = new Item().add((Enum<?>)null, new Primitive(1));
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullEnumPrimitive3() {
        item = new Item().add(Stooges.LARRY, (Primitive)null);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullStringPrimitive1() {
        item = new Item().add((String)null, (Primitive)null);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullStringPrimitive2() {
        item = new Item().add((String)null, new Primitive(2));
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullStringPrimitive3() {
        item = new Item().add(FOO, (Primitive)null);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullEnumString1() {
        item = new Item().add((Enum<?>)null, (String)null);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullEnumString2() {
        item = new Item().add((Enum<?>)null, BAR);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullEnumString3() {
        item = new Item().add(Stooges.MO, (String)null);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullStringString1() {
        item = new Item().add((String)null, (String)null);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullStringString2() {
        item = new Item().add((String)null, FOO);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullStringString3() {
        item = new Item().add(FOO, (String)null);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullEnumLong() {
        item = new Item().add((Enum<?>)null, 0L);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullEnumBoolean() {
        item = new Item().add((Enum<?>)null, true);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullStringBoolean() {
        item = new Item().add((String)null, false);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullStringLong() {
        item = new Item().add((String)null, 0L);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullEnumEnum1() {
        item = new Item().add((Enum<?>)null, (Enum<?>)null);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullEnumEnum2() {
        item = new Item().add((Enum<?>)null, Stooges.LARRY);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullEnumEnum3() {
        item = new Item().add(Stooges.CURLY, (Enum<?>)null);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullStringEnum1() {
        item = new Item().add((String)null, (Enum<?>)null);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullStringEnum2() {
        item = new Item().add((String)null, Stooges.MO);
    }

    @Test(expected = NullPointerException.class)
    public void itemAddNullStringEnum3() {
        item = new Item().add(FOO, (Enum<?>)null);
    }

    // === Array ===

    @Test
    public void emptyArray() {
        print(EOL + "emptyArray()");
        array = new Array();
        print(array);
        assertEquals(M_BAD_ARRY, "[\n" +
                "]", array.toString());
        assertEquals(M_BAD_ARRY, "[ ]", array.inline().toString());
        assertEquals(AM_UXS, 0, array.size());
    }

    @Test
    public void primitiveArray() {
        print(EOL + "primitiveArray()");
        array = new Array();
        array.add(FOO).add(new Primitive(BAR)).add(INT).add(Stooges.CURLY).add(LONG).add(true);
        print(array);
        assertEquals(M_BAD_ARRY,
                     "[\n  \"foo\",\n  \"bar\",\n  \"941\",\n  \"CURLY\",\n  \"1234567899\",\n  true\n]",
                     array.toString());

        assertEquals(M_BAD_ARRY, "[ \"foo\", \"bar\", \"941\", \"CURLY\", \"1234567899\", true ]",
                     array.inline().toString());
        assertEquals(AM_UXS, 6, array.size());
    }

    @Test
    public void arrayOfItems() {
        print(EOL + "arrayOfItems()");
        array = new Array();
        array.add(new Item[] { new Item(), new Item(), new Item() });
        print(array);
        assertEquals(M_BAD_ARRY, "[\n  {\n  },\n  {\n  },\n  {\n  }\n]",
                     array.toString());

        assertEquals(M_BAD_ARRY, "[ { }, { }, { } ]",
                     array.inline().toString());
    }

    @Test
    public void itemArray2() {
        print(EOL + "itemArray2()");
        array = new Array().add(new Item[] {
            new Item().add("foo", "FOO"),
            new Item().add("bar", "BAR"),
            new Item().add("two", 2),
            new Item().add("curly", Stooges.CURLY),
            new Item().add("ready", false)
        });
        print(array);
        assertEquals(M_BAD_ARRY, "[\n" +
                     "  {\n    \"foo\": \"FOO\"\n  },\n" +
                     "  {\n    \"bar\": \"BAR\"\n  },\n" +
                     "  {\n    \"two\": \"2\"\n  },\n" +
                     "  {\n    \"curly\": \"CURLY\"\n  },\n" +
                     "  {\n    \"ready\": false\n  }\n]",
                     array.toString());

        assertEquals(M_BAD_ARRY, "[ { \"foo\": \"FOO\" }, " +
                     "{ \"bar\": \"BAR\" }, { \"two\": \"2\" }, " +
                     "{ \"curly\": \"CURLY\" }, { \"ready\": false } ]",
                     array.inline().toString());
    }

    @Test
    public void orderDependent() {
        print(EOL + "orderDependent()");
        array = new Array().add(Stooges.LARRY).add(Stooges.MO).add(Stooges.CURLY);
        Array a2 = new Array().add(Stooges.CURLY).add(Stooges.LARRY).add(Stooges.MO);
        print(array);
        print(a2);
        verifyNotEqual(array, a2);
    }

    @Test
    public void arrayEquality() {
        print(EOL + "arrayEquality()");
        array = new Array().add(Stooges.LARRY).add(Stooges.MO).add(3);
        Array a2 = new Array().inline().add("LARRY").add("MO").add("3");
        print(array);
        print(a2);
        verifyEqual(array, a2);

        // for completeness
        verifyEqual(array, array);
        verifyNotEqual(array, "DOH");
    }

    @Test(expected = NullPointerException.class)
    public void arrayAddNullPrimitive() {
        array = new Array().add((Primitive)null);
    }

    @Test(expected = NullPointerException.class)
    public void arrayAddNullString() {
        array = new Array().add((String)null);
    }

    @Test(expected = NullPointerException.class)
    public void arrayAddNullEnum() {
        array = new Array().add((Enum<?>)null);
    }

    @Test(expected = NullPointerException.class)
    public void arrayAddNullPrimitiveArray() {
        array = new Array().add((Primitive[])null);
    }
    
    @Test
    public void getFromItem() {
        Item bag = new Item();
        Item t1 = new Item();
        bag.add("t", t1).add("n", 123).add("s", "foo");
        Item t = (Item) bag.get("t");
        assertSame(AM_NSR, t1, t);
    }

    @Test
    public void getFromItemNotFound() {
        Item bag = new Item();
        Item t1 = new Item();
        bag.add("t", t1).add("n", 123).add("s", "foo");
        Item t = (Item) bag.get("non");
        assertNull(AM_HUH, t);
    }
    
    @Test
    public void keysOfItem() {
        Item bag = new Item();
        Item t1 = new Item();
        bag.add("t", t1).add("n", 123).add("s", "foo");
        Set<String> keys = bag.keys();
        for (String k : new String[] { "t", "n", "s" })
            assertTrue("key '" + k + "' should be present", keys.contains(k));
    }

    @Test
    public void getFromArray() {
        Array bag = new Array();
        Item t1 = new Item();
        bag.add(321).add(t1).add(123).add("foo");
        Item t = (Item) bag.get(1);
        assertSame(AM_NSR, t1, t);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void getFromArrayIndexLarge() {
        Array bag = new Array().add(321).add(123).add("foo");
        bag.get(999);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void getFromArrayIndexNegative() {
        Array bag = new Array().add(321).add(123).add("foo");
        bag.get(-1);
    }

}
