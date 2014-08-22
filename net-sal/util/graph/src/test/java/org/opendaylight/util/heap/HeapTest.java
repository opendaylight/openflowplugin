/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.heap;

import static org.opendaylight.util.graph.Tools.*;
import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

/**
 * Suite of tests for the heap data structure.
 *
 * @author Thomas Vachuska
 */
public class HeapTest {
    
    public Integer[] data = new Integer[] { 5, 6, 7, 4, 3, 2, 1, 0, 0, 0};
    
    void dump(Heap<?> h) {
        Iterator<?> hi = h.iterator();
        System.out.print("[");
        while (hi.hasNext()) {
            System.out.print(hi.next());
            if (hi.hasNext())
                System.out.print(", ");
        }
        System.out.println("]");
    }
    
    @Test
    public void empty() {
        Heap<Integer> h = new Heap<Integer>(data, 0, DESCENDING);
        assertEquals("incorrect size", 0, h.size());
        assertEquals("incorrect extreme", null, h.extreme());
        assertEquals("incorrect extreme", null, h.extractExtreme());
        assertTrue("heap should be empty", h.isEmpty());
    }
    
    @Test
    public void min() {
        Heap<Integer> h = new Heap<Integer>(data, 7, DESCENDING);
        assertEquals("incorrect size", 7, h.size());
        assertEquals("incorrect min", 1, (int) h.extreme());
        assertFalse("heap should be empty", h.isEmpty());
    }
    
    @Test
    public void max() {
        Heap<Integer> h = new Heap<Integer>(data, 7, ASCENDING);
        assertEquals("incorrect size", 7, h.size());
        assertEquals("incorrect max", 7, (int) h.extreme());
        assertFalse("heap should be empty", h.isEmpty());
    }
    
    @Test
    public void minExtract() {
        Heap<Integer> h = new Heap<Integer>(data, 7, DESCENDING);
        for (int i = 1; i <= 7; i++) {
            assertEquals("incorrect min", i, (int) h.extractExtreme());
            assertEquals("incorrect size", 7 - i, h.size());
        }
        assertTrue("heap should be empty", h.isEmpty());
    }
    
    @Test
    public void maxExtract() {
        Heap<Integer> h = new Heap<Integer>(data, 7, ASCENDING);
        for (int i = 7; i >= 1; i--) {
            assertEquals("incorrect max", i, (int) h.extractExtreme());
            assertEquals("incorrect size", i - 1, h.size());
        }
        assertTrue("heap should be empty", h.isEmpty());
    }

    @Test
    public void minInsertExtreme() {
        Heap<Integer> h = new Heap<Integer>(data, 7, DESCENDING);
        h.insert(0);
        assertEquals("incorrect size", 8, h.size());
        for (int i = 0; i <= 7; i++) {
            assertEquals("incorrect min", i, (int) h.extractExtreme());
            assertEquals("incorrect size", 7 - i, h.size());
        }
        assertTrue("heap should be empty", h.isEmpty());
    }
    
    @Test
    public void maxInsertExtreme() {
        Heap<Integer> h = new Heap<Integer>(data, 7, ASCENDING);
        h.insert(8);
        assertEquals("incorrect size", 8, h.size());
        for (int i = 8; i >= 1; i--) {
            assertEquals("incorrect max", i, (int) h.extractExtreme());
            assertEquals("incorrect size", i - 1, h.size());
        }
    }
    
    @Test
    public void iterator() {
        Heap<Integer> h = new Heap<Integer>(data, 3, ASCENDING);
        Iterator<Integer> hi = h.iterator();
        assertEquals("wrong item", 7, (int) hi.next());
        assertEquals("wrong item", 6, (int) hi.next());
        assertEquals("wrong item", 5, (int) hi.next());
        assertFalse("unexpected item", hi.hasNext());
    }
    
    @Test
    public void maxInsert() {
        Heap<Integer> h = new Heap<Integer>(data, 7, ASCENDING);
        h.insert(4);
        assertEquals("incorrect size", 8, h.size());
        assertEquals("incorrect max", 7, (int) h.extractExtreme());
    }
    
    @Test
    public void minInsert() {
        Heap<Integer> h = new Heap<Integer>(data, 7, DESCENDING);
        h.insert(4);
        assertEquals("incorrect size", 8, h.size());
        assertEquals("incorrect min", 1, (int) h.extractExtreme());
    }
    
    @Test(expected=IllegalStateException.class)
    public void overflow() {
        Heap<Integer> h = new Heap<Integer>(data, data.length, ASCENDING);
        h.insert(8);
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void iteratorRemove() {
        Heap<Integer> h = new Heap<Integer>(data, data.length, ASCENDING);
        h.iterator().remove();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void badSize() {
        new Heap<Integer>(data, data.length + 1, ASCENDING);
    }

}
