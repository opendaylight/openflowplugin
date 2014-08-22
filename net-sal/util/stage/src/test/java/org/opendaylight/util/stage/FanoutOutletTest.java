/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Suite of tests for the fan-out outlet functionality.
 *
 * @author Thomas Vachuska
 */
public class FanoutOutletTest {

    private static final String YO = "yo";
    private static final String HEY = "hey";

    protected FanoutOutlet<String> fo;
    
    @Before
    public void setUp() throws Exception {
        fo = new FanoutOutlet<String>();
    }

    @Test
    public void empty() {
        assertFalse("empty fanout should not accept items", fo.accept(YO));
        assertEquals("incorrect size", 0, fo.size());
        assertTrue("incorrect outlet set", fo.getOutlets().isEmpty());
    }


    @Test
    public void basics() {
        TestOutlet a = new TestOutlet();
        assertTrue("outlet addition should succeed", fo.add(a));
        assertFalse("redundant outlet addition should fail", fo.add(a));
        assertEquals("incorrect size", 1, fo.size());
        
        TestOutlet b = new TestOutlet();
        assertTrue("outlet addition should succeed", fo.add(b));
        assertEquals("incorrect size", 2, fo.size());

        assertTrue("item should be accepted", fo.accept(YO));
        assertTrue("item should be forwarded to outlet A", a.list.contains(YO));
        assertTrue("item should be forwarded to outlet B", b.list.contains(YO));
        
        assertTrue("outlet removal should succeed", fo.remove(b));
        assertEquals("incorrect size", 1, fo.size());
        assertFalse("redundant outlet removal should fail", fo.remove(b));
        assertEquals("incorrect size", 1, fo.size());

        assertTrue("item should be accepted", fo.accept(HEY));
        assertTrue("item should be forwarded to outlet A", a.list.contains(HEY));
        assertFalse("item should not be forwarded to outlet B", b.list.contains(HEY));
    }
    
    /**
     * Test fixture outlet
     */
    protected static class TestOutlet implements Outlet<String> {
        
        List<String> list = new ArrayList<String>();

        @Override
        public boolean accept(String item) {
            return list.add(item);
        }
    }

}
