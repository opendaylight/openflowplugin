/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * This JUnit test class tests the AbstractDefaultFacet class.
 *
 * @author Simon Hunt
 */
public class AbstractDefaultFacetTest {

    private static class SomeType extends DefaultDeviceType {
        protected SomeType() { super(null, null); }
    }

    private static class SomeOtherInfo extends AbstractDeviceInfo {
        public SomeOtherInfo(DefaultDeviceType deviceType) { super(deviceType); }

        @Override public DeviceInfo evolve() { return null; }
        @Override public int getGeneration() { return 0; }
        @Override public String exportData() { return null; }
        @Override public boolean importData(String data) { return false; }
    }

    private static class MyDefaultFacet extends AbstractDefaultFacet {
        public MyDefaultFacet(DeviceInfo context) { super(context); }
    }

    private SomeOtherInfo soi;
    private DefaultDeviceInfo ddi;

    @Before
    public void setUp() {
        DefaultDeviceType type = new SomeType();
        soi = new SomeOtherInfo(type);
        ddi = new DefaultDeviceInfo(type);
    }

    // == TESTS GO HERE ==
    @Test
    public void basic() {
        print(EOL + "basic()");
        Facet f = new MyDefaultFacet(ddi);
        assertNotNull("no facet", f);

        DeviceInfo di = f.getContext();
        assertTrue("context not downcast?", di instanceof DefaultDeviceInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongInfoClass() {
        print(EOL + "wrongInfoClass()");
        new MyDefaultFacet(soi);
        fail(AM_NOEX);
    }
}
