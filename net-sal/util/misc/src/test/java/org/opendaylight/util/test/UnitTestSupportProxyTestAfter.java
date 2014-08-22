/*
 * (c) Copyright 2010-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.test;

import org.junit.After;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.AM_HUH;
import static org.junit.Assert.*;

/**
 * Unit tests for UnitTestSupportProxy.
 *
 * @author Simon Hunt
 */
public class UnitTestSupportProxyTestAfter
        extends AbstractUnitTestSupportProxyTest {

    // allow the proxy call from '@After' annotated method
    @After
    public void tearDown() {
        MyUTS mine = new MyUTS();
        UnitTestSupport proxy = UnitTestSupportProxy.injectProxy(mine);
        assertNotSame(AM_HUH, mine, proxy);

        assertFalse(AM_HUH, mine.wasCleared());
        proxy.clear();
        assertTrue(AM_HUH, mine.wasCleared());

        assertFalse(AM_HUH, mine.wasCleared(ID.Foo));
        proxy.clear(ID.Foo);
        assertTrue(AM_HUH, mine.wasCleared(ID.Foo));
        assertFalse(AM_HUH, mine.wasCleared(ID.Bar));

        assertFalse(AM_HUH, mine.wasReset());
        proxy.reset();
        assertTrue(AM_HUH, mine.wasReset());

        assertFalse(AM_HUH, mine.wasReset(ID.Bar));
        proxy.reset(ID.Bar);
        assertTrue(AM_HUH, mine.wasReset(ID.Bar));
        assertFalse(AM_HUH, mine.wasReset(ID.Baz));

        assertFalse(AM_HUH, mine.wasSet());
        proxy.set();
        assertTrue(AM_HUH, mine.wasSet());

        assertFalse(AM_HUH, mine.wasSet(ID.Baz));
        proxy.set(ID.Baz);
        assertTrue(AM_HUH, mine.wasSet(ID.Baz));
        assertFalse(AM_HUH, mine.wasSet(ID.Foo));
    }

    @Test
    public void dummy() {
        // we need some test, so that the @After method is called.
        assertTrue(AM_HUH, true);
    }
}
