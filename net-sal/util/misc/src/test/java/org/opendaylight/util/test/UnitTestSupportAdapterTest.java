/*
 * (c) Copyright 2010-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.test;

import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.AM_WRCL;
import static org.opendaylight.util.test.AbstractUnitTestSupportProxyTest.ID;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for UnitTestSupportAdapter.
 *
 * @author Simon Hunt
 */
public class UnitTestSupportAdapterTest {

    @Test
    public void firstTest() {
        // Check that this compiles, and we can call all the methods.
        UnitTestSupportAdapter adapter = new UnitTestSupportAdapter();
        adapter.clear();
        adapter.clear(ID.Foo);
        adapter.reset();
        adapter.reset(ID.Bar);
        adapter.set();
        adapter.set(ID.Baz);
        assertTrue(AM_WRCL, UnitTestSupport.class.isInstance(adapter));
    }

    @Test
    public void implementsUnitTestSupport() {
        UnitTestSupport uts = new UnitTestSupportAdapter();
        uts.clear();
        uts.clear(ID.Foo);
        uts.reset();
        uts.reset(ID.Bar);
        uts.set();
        uts.set(ID.Baz);
        assertTrue(AM_WRCL, UnitTestSupport.class.isInstance(uts));
    }
}
