/*
 * (c) Copyright 2010-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.test;

import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.AM_HUH;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;

/**
 * Unit tests for UnitTestSupportProxy.
 *
 * @author Simon Hunt
 */
public class UnitTestSupportProxyTest extends AbstractUnitTestSupportProxyTest {

    // In the following tests, we do NOT allow the proxy call to proceed
    // from a non-setup/teardown method

    @Test (expected = RuntimeException.class)
    public void tryToClear() {
        MyUTS mine = new MyUTS();
        assertFalse(AM_HUH, mine.wasCleared());

        UnitTestSupport proxy = UnitTestSupportProxy.injectProxy(mine);
        assertNotSame(AM_HUH, mine, proxy);

        assertFalse(AM_HUH, mine.wasCleared());
        proxy.clear(); // this should throw a runtime exception
    }

    @Test (expected = RuntimeException.class)
    public void tryToClearFoo() {
        MyUTS mine = new MyUTS();
        assertFalse(AM_HUH, mine.wasCleared());

        UnitTestSupport proxy = UnitTestSupportProxy.injectProxy(mine);
        assertNotSame(AM_HUH, mine, proxy);

        assertFalse(AM_HUH, mine.wasCleared());
        proxy.clear(ID.Foo); // this should throw a runtime exception
    }

    @Test (expected = RuntimeException.class)
    public void tryToReset() {
        MyUTS mine = new MyUTS();
        assertFalse(AM_HUH, mine.wasReset());

        UnitTestSupport proxy = UnitTestSupportProxy.injectProxy(mine);
        assertNotSame(AM_HUH, mine, proxy);

        assertFalse(AM_HUH, mine.wasReset());
        proxy.reset(); // this should throw a runtime exception
    }

    @Test (expected = RuntimeException.class)
    public void tryToResetBar() {
        MyUTS mine = new MyUTS();
        assertFalse(AM_HUH, mine.wasReset());

        UnitTestSupport proxy = UnitTestSupportProxy.injectProxy(mine);
        assertNotSame(AM_HUH, mine, proxy);

        assertFalse(AM_HUH, mine.wasReset());
        proxy.reset(ID.Bar); // this should throw a runtime exception
    }

    @Test (expected = RuntimeException.class)
    public void tryToSet() {
        MyUTS mine = new MyUTS();
        assertFalse(AM_HUH, mine.wasSet());

        UnitTestSupport proxy = UnitTestSupportProxy.injectProxy(mine);
        assertNotSame(AM_HUH, mine, proxy);

        assertFalse(AM_HUH, mine.wasSet());
        proxy.set(); // this should throw a runtime exception
    }

    @Test (expected = RuntimeException.class)
    public void tryToSetBaz() {
        MyUTS mine = new MyUTS();
        assertFalse(AM_HUH, mine.wasSet());

        UnitTestSupport proxy = UnitTestSupportProxy.injectProxy(mine);
        assertNotSame(AM_HUH, mine, proxy);

        assertFalse(AM_HUH, mine.wasSet());
        proxy.set(ID.Baz); // this should throw a runtime exception
    }
}
