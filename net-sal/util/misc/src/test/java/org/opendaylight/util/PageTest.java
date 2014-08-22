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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Set of test cases for the Page
 * 
 * @author Scott Simes
 * @author Fabiel Zuniga
 */
public class PageTest {

    private static final PageRequest PAGE_REQ = new PageRequest(2, 100);

    private static final long RECORD_COUNT = 200L;

    private static final long PAGE_COUNT = 10L;

    private static final String[] STRINGS = { "strOne", "strTwo", "strThree",
            "strFour", "strFive" };

    private static final Long[] LONGS = { 0L, 1L, 2L, 3L, 4L };

    @Test
    public void testBasics() {
        Page<String> strPage = new Page<String>(getStringDataList(),
                                                PAGE_REQ, RECORD_COUNT,
                                                PAGE_COUNT);

        assertEquals(AM_NEQ, strPage.getPageRequest(), PAGE_REQ);
        assertEquals(AM_NEQ, strPage.getPageCount(), PAGE_COUNT);
        assertEquals(AM_NEQ, strPage.getRecordCount(), RECORD_COUNT);
        assertEquals(AM_NEQ, strPage.getData().size(), STRINGS.length);
        assertFalse(strPage.isEmpty());
    }

    @Test
    public void testEmptyPage() {
        Page<Long> ePage = Page.emptyPage();
        assertEquals(AM_NEQ, ePage.getData().size(), 0);
        assertTrue(ePage.isEmpty());
    }

    @Test
    public void testConvertPage() {
        Page<Long> longPage = new Page<Long>(getLongDataList(), PAGE_REQ,
                                             RECORD_COUNT, PAGE_COUNT);

        Converter<Long, String> converter = new Converter<Long, String>() {
            @Override
            public String convert(Long source) {
                return String.valueOf(source);
            }
        };
        
        Page<String> strPage = longPage.convert(converter);

        assertEquals(AM_NEQ, strPage.getPageRequest(), PAGE_REQ);
        assertEquals(AM_NEQ, strPage.getPageCount(), PAGE_COUNT);
        assertEquals(AM_NEQ, strPage.getRecordCount(), RECORD_COUNT);
        assertEquals(AM_NEQ, strPage.getData().size(), LONGS.length);
    }
    
    @Test
    public void testNextPage() {
        PageRequest strPageRequest = new PageRequest(2);
        List<String> strList = getStringDataList();
        Page<String> first = new Page<String>(strList, strPageRequest, 
                strList.size(), 3);
        
        assertFalse(first.hasPrevious());
        assertTrue(first.hasNext());
        assertNull(first.getPreviousPageRequest());
    
        PageRequest nextPageRequest = first.getNextPageRequest();
        assertEquals("should be equal", 1, nextPageRequest.getPageIndex());
        assertEquals("should be equal", 2, nextPageRequest.getPageSize());
        
        Page<String> next = new Page<String>(strList, nextPageRequest, 
                                         strList.size(), 3);
        assertTrue(next.hasPrevious());
        assertTrue(next.hasNext());
        
        PageRequest prevPageRequest = next.getPreviousPageRequest();
        assertEquals("should be equal", 0, prevPageRequest.getPageIndex());
        assertEquals("should be equal", 2, prevPageRequest.getPageSize());
        
        
        PageRequest lastPageRequest = next.getNextPageRequest();
        assertEquals("should be equal", 2, lastPageRequest.getPageIndex());
        assertEquals("should be equal", 2, lastPageRequest.getPageSize());
        
        Page<String> last = new Page<String>(strList, lastPageRequest, 
                                         strList.size(), 3);
        assertTrue(last.hasPrevious());
        assertFalse(last.hasNext());
        assertNull(last.getNextPageRequest());
        
    }
    
    @Test
    public void pageIndexValidation() {
        try {
            new PageRequest(-1, 100);
            fail("Exception expected");
        } catch (Exception e) {
            assertTrue(IllegalArgumentException.class.isInstance(e));
        }
        
        try {
            new PageRequest(0);
            fail("Exception expected");
        } catch (Exception e) {
            assertTrue(IllegalArgumentException.class.isInstance(e));
        }
    }

    private List<String> getStringDataList() {
        List<String> strings = new ArrayList<String>();
        for (String string : STRINGS) {
            strings.add(string);
        }
        return strings;
    }

    private List<Long> getLongDataList() {
        List<Long> longList = new ArrayList<Long>();
        for (Long l : LONGS) {
            longList.add(l);
        }
        return longList;
    }
}
