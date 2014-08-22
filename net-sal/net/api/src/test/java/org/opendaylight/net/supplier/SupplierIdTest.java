/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.supplier;

import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test of the supplier ID implementation.
 *
 * @author Thomas Vachuska
 */
public class SupplierIdTest {

    @Test
    public void basics() {
        SupplierId a = new SupplierId("org.opendaylight.util.test.id");
        SupplierId b = new SupplierId("org.opendaylight.util.test.id");
        SupplierId c = new SupplierId("org.opendaylight.util.test.aid");

        assertEquals(AM_NEQ, a, b);
        assertEquals(AM_NEQ, a.hashCode(), b.hashCode());
        assertFalse(AM_NEQ, a.equals(c));
        assertEquals(AM_NEQ, "org.opendaylight.util.test.id", a.toString());
    }

}
