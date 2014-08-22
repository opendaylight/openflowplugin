/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.util.Version;


/**
 * Version class is used to abstract a version string that can be compared.
 * 
 * @author Frank Wood
 */
public class VersionTest {

    @Test
    public void ascending() {
        Version[] v = {
            new Version(),
            new Version("A.00.00"),
            new Version("B"),
            new Version("B.00.00"),
            new Version("B.00.001"),
            new Version("B.00.001.1"),
            new Version("B.1"),
            new Version("B.1.1"),
            new Version("B.10"),
            new Version("B.10.01"),
            new Version("B.11"),
            new Version("B.11.01"),
            new Version("B.2"),
            new Version("C.00.00"),
            new Version("C.1.00"),
            new Version("C.01.01"),
        };
        
        for (int i=0; i<(v.length-1); i++) {
            assertTrue(v[i+1] + " should be > " + v[i], v[i+1].isGreater(v[i]));
            assertTrue(v[i+1] + " should be >= " + v[i], v[i+1].isGreaterOrEqual(v[i]));
            assertTrue(v[i] + " should be < " + v[i+1], v[i].isLess(v[i+1]));
            assertTrue(v[i] + " should be <= " + v[i+1], v[i].isLess(v[i+1]));
        }
    }

    @Test
    public void equalsGreaterLess() {
        Version v = new Version();
        Version vA0000a = new Version("A.00.00");
        Version vA0000b = new Version("A.00.00");
        Version vA000001a = new Version("A.00.0001");
        Version vA000001b = new Version("A.00.00.01");
        Version vA000001c = new Version("A.00.00.0");
        
        assertTrue(vA000001a + " should be > " + v, vA000001a.isGreater(v));
        assertTrue(vA000001a + " should be > " + vA0000b, vA000001a.isGreater(vA0000a));
        
        assertFalse(vA0000b + " should not be > " + vA0000a, vA0000b.isGreater(vA0000a));
        assertTrue(vA0000b + " should be >= " + vA0000a, vA0000b.isGreaterOrEqual(vA0000a));
        
        assertTrue(vA000001a + " should be >= " + vA0000a, vA000001a.isGreater(vA0000a));
        assertTrue(vA000001b + " should be < " + vA000001a, vA000001b.isLess(vA000001a));
        assertTrue(vA000001b + " should be > " + vA0000b, vA000001b.isGreater(vA0000b));
        
        assertTrue(vA000001c + " should be > " + vA0000b, vA000001c.isGreater(vA0000b));
        assertTrue(vA000001c + " should be < " + vA000001a, vA000001c.isLess(vA000001a));
        
        assertTrue(vA000001c + " should be <= " + vA000001a, vA000001c.isLessOrEqual(vA000001a));
    }    
    
    @Test
    public void equals() {
        Version[] v = {
            new Version(),
            new Version("A.00.00"),
            new Version("B"),
            new Version("B.00.00"),
            new Version("B.1"),
            new Version("A.00.00"),
            new Version("B.2"),
        };
        
        assertEquals(v[1], v[5]);
        assertNotSame(v[0], v[1]);
        assertNotSame(v[1], v[6]);
        
        assertEquals(v[1].hashCode(), v[5].hashCode());
        
        assertEquals("B.00.00", v[3].toString());
        
        assertFalse(v[0].equals(v[1]));
    }
    
    @Test
    public void cornerCases() {
        Version v = new Version("abc");
        assertTrue(v.isGreater(new Version()));
        assertTrue(v.isGreaterOrEqual(new Version()));
        assertFalse(v.isLess(new Version()));
        assertFalse(v.isLessOrEqual(new Version()));
        assertEquals(1, v.compareTo(null));
        assertEquals(false, v.equals(new Object()));
    }    
}
