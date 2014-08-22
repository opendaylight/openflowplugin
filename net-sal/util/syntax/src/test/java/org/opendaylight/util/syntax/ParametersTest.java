/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * JUnit tests to verify functionality of Parameters class.
 *
 * @author Thomas Vachuska
 */
public class ParametersTest implements Serializable {

    private static final long serialVersionUID = 1L;

    private static class Foo implements Serializable {
        private static final long serialVersionUID = 3205465918117042378L;
        public Foo() {}
    }

    @Test
    public void testBasics() {
        Parameters ps = new Parameters();
        assertTrue("foo should not be present", !ps.isPresent("foo"));
        assertTrue("foo should not be indexed", !ps.isIndexed("foo"));
        assertEquals("incorrect count of foo:", 0, ps.getOccurrences("foo"));
        assertEquals("foo should not be there:", null, ps.get("foo"));
        assertEquals("foo should not be there:", null, ps.get("foo", 1));
        assertEquals("incorrect size:", 0, ps.size());
    }

    @Test
    public void testAddRemove() {
        Parameters ps = new Parameters();
        Foo f = new Foo();
        ps.add("foo", f, false);
        assertTrue("foo should be present", ps.isPresent("foo"));
        assertTrue("foo should not be indexed", !ps.isIndexed("foo"));
        assertEquals("incorrect count of foo:", 1, ps.getOccurrences("foo"));
        assertEquals("foo should be there:", f, ps.get("foo"));
        assertEquals("incorrect size:", 1, ps.size());

        Foo b0 = new Foo();
        ps.add("bar", b0, true);
        assertTrue("bar should be present", ps.isPresent("bar"));
        assertTrue("bar should be indexed", ps.isIndexed("bar"));
        assertEquals("incorrect count of bar:", 1, ps.getOccurrences("bar"));
        assertEquals("bar#0 should be there:", b0, ps.get("bar", 0));
        assertEquals("incorrect size:", 2, ps.size());

        Foo b1 = new Foo();
        ps.add("bar", b1, true);
        assertTrue("bar should still be present", ps.isPresent("bar"));
        assertTrue("bar should be indexed", ps.isIndexed("bar"));
        assertEquals("incorrect count of bar:", 2, ps.getOccurrences("bar"));
        assertEquals("bar#0 should still be there:", b0, ps.get("bar", 0));
        assertEquals("bar#1 should be there:", b1, ps.get("bar", 1));
        assertEquals("incorrect size:", 3, ps.size());
    }

    @Test
    public void testGetList() {
        Parameters ps = new Parameters();
        Serializable[] foos = new Serializable[] {
            new Foo(), new Foo(), new Foo() 
        };
        for (int i = 0; i < foos.length; i++)
            ps.add("foo", foos[i], true);
        Object[] bars = ps.getList("foo").toArray();
        assertEquals("list is incorrect length:", foos.length, bars.length);
        for (int i = 0; i < foos.length; i++)
            assertEquals("list is in incorrect order:", foos[i], bars[i]);

        Foo b = new Foo();
        ps.add("bar", b, false);
        List<Object> v = ps.getList("bar");
        assertNotNull("list should not be null", v);
        assertEquals("list is incorrect length:", 1, v.size());

        assertTrue("list should be null", ps.getList("foobar") == null);
    }

    @Test
    public void testOddballs() {
        Parameters ps = new Parameters();
        for (int i = 0; i < 10; i++)
            ps.add("foo", new Foo(), true);
        assertEquals("incorrect size:", 10, ps.getOccurrences("foo"));

        Parameters cp = new Parameters();
        cp.add(ps);
        assertEquals("incorrect size:", 10, cp.getOccurrences("foo"));

        Iterator<String> it = cp.getNames().iterator();
        int count = 0;
        while (it.hasNext()) {
            String n = it.next();
            assertTrue(n + " should be present", cp.isPresent(n));
            count++;
        }
        assertEquals("incorrect iterator length:", 10, count);
    }


    @Test
    public void testDefaultConstructor() {
        new Parameters(); // Make sure that default constructor exists.
    }

}

