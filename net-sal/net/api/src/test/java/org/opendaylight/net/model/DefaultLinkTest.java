/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.net.model.Link.Type;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;

import static org.opendaylight.util.junit.TestTools.print;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.net.model.InterfaceId.valueOf;

import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.net.supplier.SupplierId;

/**
 * Unit test for {@link DefaultLink}.
 *
 * @author Marjorie Krueger
 */
public class DefaultLinkTest {

    private static final SupplierId SID = new SupplierId("foobar");

    private static final DeviceId SRC_DEV = DeviceId.valueOf("42/00001e:123456");
    private static final DeviceId DST_DEV = DeviceId.valueOf("43/00001f:654321");

    private static final InterfaceId SRC_IF = valueOf(BigPortNumber.bpn(1));
    private static final InterfaceId DST_IF = valueOf(BigPortNumber.bpn(3));

    private static final ConnectionPoint SRC = new DefaultConnectionPoint(SRC_DEV, SRC_IF);
    private static final ConnectionPoint DST = new DefaultConnectionPoint(DST_DEV, DST_IF);

    @Test
    public void basic() {
        DefaultLink link = new DefaultLink(SRC, DST, Type.DIRECT_LINK);
        validate(link, null, SRC, DST, Type.DIRECT_LINK);

        link = new DefaultLink(SRC, DST);
        validate(link, null, SRC, DST, Type.MULTIHOP_LINK);

        link = new DefaultLink(SID, SRC, DST, Type.DIRECT_LINK);
        validate(link, SID, SRC, DST, Type.DIRECT_LINK);

        Link link2 = new DefaultLink(null, SRC, DST, Type.DIRECT_LINK);
        assertTrue("should be equal", link.equals(link2));
        assertEquals("incorrect hashCode", link.hashCode(), link2.hashCode());

        Link link3 = new DefaultLink(null, DST, SRC, Type.DIRECT_LINK);
        assertFalse("should be equal", link3.equals(link2));

        assertTrue("incorrect toString", link.toString().contains("DefaultLink{"));
    }

    private void validate(Link link, SupplierId sid,
                          ConnectionPoint src, ConnectionPoint dst, Type type) {
        assertEquals("incorrect link src", sid, link.supplierId());
        assertEquals("incorrect link src", src, link.src());
        assertEquals("incorrect link dst", dst, link.dst());
        assertEquals("incorrect link type", type, link.type());
    }

    @Test
    public void setInfo() {
        DefaultLink link = new DefaultLink(SRC, DST);
        validate(link, null, SRC, DST, Type.MULTIHOP_LINK);
        link.setType(Type.DIRECT_LINK);
        validate(link, null, SRC, DST, Type.DIRECT_LINK);
        assertSame("incorrect link type", Type.DIRECT_LINK, link.type());
    }

    @Test (expected = NullPointerException.class)
    public void nullSourceCP () {
        new DefaultLink(null, DST);
    }

    @Test (expected = NullPointerException.class)
    public void nullDestinationCP () {
        new DefaultLink(SRC, null);
    }

    @Test (expected = NullPointerException.class)
    public void nullCPs () {
        new DefaultLink(null, null);
    }
}
