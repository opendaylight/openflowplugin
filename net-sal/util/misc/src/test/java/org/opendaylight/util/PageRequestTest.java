/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.opendaylight.util.junit.EqualityTester;

/**
 * Set of test cases for the Page Request
 * 
 * @author Scott Simes
 * @author Fabiel Zuniga
 */
public class PageRequestTest {

    private static final int PAGE_INDEX = 18;

    private static final int PAGE_SIZE = 500;

    @Test
    public void testBasic() {
        PageRequest pr = new PageRequest(PAGE_INDEX, PAGE_SIZE);
        assertEquals(AM_NEQ, pr.getPageIndex(), PAGE_INDEX);
        assertEquals(AM_NEQ, pr.getPageSize(), PAGE_SIZE);
    }
    
    @Test
    public void testFirstPage() {
        PageRequest pr = new PageRequest(PAGE_SIZE);
        assertEquals(AM_NEQ, pr.getPageIndex(), 0);
        assertEquals(AM_NEQ, pr.getPageSize(), PAGE_SIZE);
    }
    
    @Test
    public void testPageIndexParam() {
        try {
            new PageRequest(-1, PAGE_SIZE);
            fail("Expected IllegalArgumentException");
        } catch(Exception e) {
            assertTrue(IllegalArgumentException.class.isInstance(e));
        }
    }
    
    @Test
    public void pageSizeParam() {
        try {
            new PageRequest(0, 0);
        } catch (Exception e) {
            assertTrue(IllegalArgumentException.class.isInstance(e));
        }
    }
    
    @Test
    public void testEqualsAndHashCode() {
        PageRequest baseObjToTest = new PageRequest(PAGE_INDEX, PAGE_SIZE);
        PageRequest equalsToBase1 = new PageRequest(PAGE_INDEX, PAGE_SIZE);
        PageRequest equalsToBase2 = new PageRequest(PAGE_INDEX, PAGE_SIZE);
        PageRequest unequalToBase1 = new PageRequest(2, PAGE_SIZE);
        PageRequest unequalToBase2 = new PageRequest(PAGE_INDEX, 20);
        
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase2);
    }
}
