/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.supplier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.opendaylight.util.junit.TestTools.AM_UXS;
import static org.junit.Assert.*;

/**
 * Test of the base suppliers broker implementation.
 *
 * @author Thomas Vachuska
 */
public class AbstractSuppliersBrokerTest {

    private static class TestS implements Supplier {
        private final SupplierId id = new SupplierId("foo");

        @Override
        public SupplierId supplierId() {
            return id;
        }
    }

    private static class TestSS extends AbstractSupplierService {
        public TestSS(Supplier supplier) {
        }
    }

    private static class TestSB extends AbstractSuppliersBroker<TestS, TestSS> {
        @Override
        protected TestSS createSupplierService(TestS supplier) {
            return new TestSS(supplier);
        }
    }

    @Rule
    public ExpectedException exc = ExpectedException.none();

    @Test
    public void basics() {
        TestSB sb = new TestSB();
        assertEquals(AM_UXS, 0, sb.getSuppliers().size());

        TestS s = new TestS();
        SupplierService ss = sb.registerSupplier(s);
        assertEquals(AM_UXS, 1, sb.getSuppliers().size());
        assertTrue("supplier should be found", sb.getSuppliers().contains(s));

        ((AbstractSupplierService) ss).validate();
        sb.unregisterSupplier(s);
        assertFalse("supplier should not be found", sb.getSuppliers().contains(s));

        exc.expect(IllegalStateException.class);
        exc.expectMessage("Supplier service no longer valid");
        ((AbstractSupplierService) ss).validate();
    }


    @Test
    public void duplicateRegistration() {
        TestSB sb = new TestSB();
        TestS s = new TestS();
        SupplierService ss = sb.registerSupplier(s);
        assertTrue("supplier should be found", sb.getSuppliers().contains(s));
        assertSame("supplier service should be same", ss, sb.registerSupplier(s));
    }

}
