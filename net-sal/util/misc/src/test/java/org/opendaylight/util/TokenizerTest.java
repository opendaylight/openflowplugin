/*
 * (c) Copyright 2008 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.NoSuchElementException;

import org.junit.Test;
import org.opendaylight.util.Tokenizer;

/**
 * Suite of tests for the circular tokenizer utility.
 *
 * @author Thomas Vachuska
 */
public class TokenizerTest {

    @Test
    public void testBasics() {
        Tokenizer t = new Tokenizer("a,2,3", ",");
        assertFalse("should not be circular", t.circular());
        assertEquals("incorrect string", "a,2,3", t.string());
        assertEquals("incorrect delimiters", ",", t.delimiters());
        assertEquals("incorrect token", "a", t.next());
        assertEquals("incorrect token", 2, t.nextInt());
        assertEquals("incorrect token", 3L, t.nextLong());
        assertFalse("should not have any more tokens", t.hasNext());
        
        t.reset();
        assertEquals("incorrect token", "a", t.next());
    }
    
    @Test
    public void testCircular() {
        Tokenizer t = new Tokenizer("a,2,3", ",", true);
        assertTrue("should be circular", t.circular());
        assertEquals("incorrect string", "a,2,3", t.string());
        assertEquals("incorrect delimiters", ",", t.delimiters());
        assertEquals("incorrect token", "a", t.next());
        assertEquals("incorrect token", 2, t.nextInt());
        assertEquals("incorrect token", 3L, t.nextLong());
        assertTrue("should always have more tokens", t.hasNext());
        assertEquals("incorrect token", "a", t.next());
        assertEquals("incorrect token", 2, t.nextInt());
        assertEquals("incorrect token", 3L, t.nextLong());
        assertTrue("should always have more tokens", t.hasNext());
        assertEquals("incorrect token", "a", t.next());
        assertTrue("should always have more tokens", t.hasNext());
    }

    @Test
    public void testLimit() {
        Tokenizer t = new Tokenizer("a", ",");
        assertEquals("incorrect token", "a", t.next());
        assertFalse("should not have any more tokens", t.hasNext());
        
        try {
            t.next();
            fail("should not get more elements");
        } catch (NoSuchElementException e) {
        }
    }
    
    @Test
    public void testIndexOfSubstr() {
        String s = "abcd:1234";
        int i = s.indexOf(':');
        assertEquals("wrong index", 4, i);
        assertEquals("wrong part one", "abcd", s.substring(0, i));
        assertEquals("wrong part two", "1234", s.substring(i + 1));
    }
    
}
