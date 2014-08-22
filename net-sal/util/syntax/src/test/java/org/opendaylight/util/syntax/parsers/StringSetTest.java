/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import java.io.Serializable;
import junit.framework.*;

/**
 * JUnit tests to verify functionality of StringSet class.
 *
 * @author Thomas Vachuska
 */
public class StringSetTest extends TestCase {

    private static final String[] GOOD = 
        new String[]{"foo", "bar", "foobar"};
    private static final String[] UGLY = 
        new String[]{"Foo", "FOO", "fo", "foob", "b", "abar"};

    public StringSetTest(String testName) {
        super(testName);
    }

    private void test(StringSet ss, String n, int l, String s[], String e[]) {
        assertEquals("length is incorrect", l, ss.getValues().size());
        assertEquals("name is incorrect", n, ss.getName());

        if (s != null) {
            for (int i = 0; i < s.length; i++)
                assertTrue(s[i] + " should be there", ss.contains(s[i]));
        }

        if (e != null) {
            for (int i = 0; i < e.length; i++)
                assertTrue(e[i] + " should not be there", !ss.contains(e[i]));
        }
    }

    public void testBasics() {
        test(new StringSet(new String[]{"foo", "bar", "foobar"}),
             "foo", 3, GOOD, UGLY);
    }

    public void testSimpleTokens() {
        test(new StringSet("foo|bar|foobar", StringSet.VALUE_DELIMITER),
             "foo", 3, GOOD, UGLY);
        test(new StringSet("foo/bar/foobar", "/"),
             "foo", 3, GOOD, UGLY);
    }

    public void testAbbreviatedTokens() {
        test(new StringSet("foo*bar|ba*r|fool", "|", "*"),
             "foo*bar|ba*r|fool", 7,
             new String[]{"foo", "foob", "fooba", "foobar", "fool",
                          "ba", "bar"},
             new String[]{"foobarU", "b", "barX", "foo*bar", "ba*r",
                          "Foo", "FOO"});
        test(new StringSet ("foo~bar/b~ar/fool", "/", "~"),
             "foo~bar|b~ar|fool", 8,
             new String[]{"foo", "foob", "fooba", "foobar", "fool",
                          "ba", "bar"},
             new String[]{"foobarU", "f", "barX", "foo~bar", "ba~r",
                          "Foo", "FOO"});
    }

    public void testAddRemove() {
        StringSet ss = new StringSet("foo*bar|ba*r", 
                                     StringSet.VALUE_DELIMITER,
                                     StringSet.ABBREV_DELIMITER);
        test(ss, "foo*bar|ba*r", 6, new String[]{"foo"}, new String[]{"fool"});
        ss.add("fool");
        test(ss, "foo*bar|ba*r", 7, new String[]{"foo", "fool"}, null);
        ss.remove("fool");
        test(ss, "foo*bar|ba*r", 6, new String[]{"foo"}, new String[]{"fool"});
    }

    public void testCaseIndependence() {
        StringSet ss = new StringSet("foo*bar|ba*r",  "|", "*");
        assertTrue("Foo should be in", ss.containsIgnoreCase("Foo"));
        assertTrue("FOO should be in", ss.containsIgnoreCase("FOO"));
        assertTrue("foo should be in", ss.containsIgnoreCase("foo"));
        assertTrue("FOOT should not be in", !ss.containsIgnoreCase("FOOT"));
    }

    public void testToString() {
        StringSet ss = new StringSet("foo*bar|ba*r|fool", "|", "*");
        assertEquals("toString is incorrect length", 
                     "|foo|foob|fooba|foobar|ba|bar|fool|".length(),
                     ss.toString().length());
    }

    public void testSerializable() {
        assertTrue("StringSet must be serializable",
                    Serializable.class.isAssignableFrom(StringSet.class));
        assertEquals("string set is not empty", 
                     "|", new StringSet().toString());
    }

}
