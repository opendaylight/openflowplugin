/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.junit.Assert.*;

import org.junit.Test;


/**
 * Set of test cases for the One Based Page Request
 *
 * @author Fabiel Zuniga
 * @author Scott Simes
 */
public class OneBasedPageRequestTest {
    private static final int PAGE_INDEX = 18;

    private static final int PAGE_SIZE = 500;

    @Test
    public void basic() {
        OneBasedPageRequest obpr = new OneBasedPageRequest(PAGE_INDEX, PAGE_SIZE);
        assertEquals(AM_NEQ, obpr.getPageIndex(), PAGE_INDEX);
        assertEquals(AM_NEQ, obpr.getPageSize(), PAGE_SIZE);
    }
    
    @Test
    public void testFirstPage() {
        OneBasedPageRequest obpr = new OneBasedPageRequest(PAGE_SIZE);
        assertEquals(AM_NEQ, obpr.getPageIndex(), 1);
        assertEquals(AM_NEQ, obpr.getPageSize(), PAGE_SIZE);
    }
    
    @Test
    public void testPageIndexParam() {
        try {
            new OneBasedPageRequest(0, PAGE_SIZE);
            fail("Expected IllegalArgumentException");
        } catch(Exception e) {
            assertTrue(IllegalArgumentException.class.isInstance(e));
        }
    }
    
    @Test
    public void pageSizeParam() {
        try {
            new OneBasedPageRequest(1, 0);
        } catch(Exception e) {
            assertTrue(IllegalArgumentException.class.isInstance(e));
        }
    }
    
    @Test
    public void toPageRequest() {
        OneBasedPageRequest obpr = new OneBasedPageRequest(PAGE_INDEX, PAGE_SIZE);
        PageRequest pr = obpr.toPageRequest();
        assertEquals(AM_NEQ, pr.getPageIndex(), (PAGE_INDEX -1));
        assertEquals(AM_NEQ, pr.getPageSize(), PAGE_SIZE);
        
        obpr = new OneBasedPageRequest(PAGE_SIZE);
        pr = obpr.toPageRequest();
        assertEquals(AM_NEQ, pr.getPageIndex(), 0);
    }
    
    @Test
    public void fromPageRequest() {
        PageRequest pr = new PageRequest(PAGE_SIZE);
        OneBasedPageRequest obpr = new OneBasedPageRequest(pr);
        assertEquals(AM_NEQ, obpr.getPageIndex(), 1);
        assertEquals(AM_NEQ, obpr.getPageSize(), PAGE_SIZE);
        
        pr = new PageRequest(PAGE_INDEX, PAGE_SIZE);
        obpr = new OneBasedPageRequest(pr);
        assertEquals(AM_NEQ, obpr.getPageIndex(), (PAGE_INDEX + 1));
    }
}
