/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology;

import org.opendaylight.net.model.*;
import org.opendaylight.util.net.BigPortNumber;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Set of tests of the topology graph adapter entities.
 *
 * @author Thomas Vachuska
 */
public class TopoGraphTest {

    private static final DeviceId D1 = DeviceId.valueOf("111");
    private static final DeviceId D2 = DeviceId.valueOf("222");
    private static final DeviceId D3 = DeviceId.valueOf("333");
    private static final DeviceId D4 = DeviceId.valueOf("444");

    private static final TopoVertex V1 = new TopoVertex(D1);
    private static final TopoVertex V1A = new TopoVertex(D1);
    private static final TopoVertex V2 = new TopoVertex(D2);
    private static final TopoVertex V3 = new TopoVertex(D3);
    private static final TopoVertex V4 = new TopoVertex(D4);

    private static final BigPortNumber P1 = BigPortNumber.bpn(1);
    private static final BigPortNumber P2 = BigPortNumber.bpn(2);
    private static final BigPortNumber P3 = BigPortNumber.bpn(3);
    private static final BigPortNumber P4 = BigPortNumber.bpn(4);

    private static final TopoEdge E1 = new TopoEdge(V1, V2, link(D1, P1, D2, P2));
    private static final TopoEdge E1A = new TopoEdge(V1A, V2, link(D1, P1, D2, P2));
    private static final TopoEdge E2 = new TopoEdge(V3, V4, link(D3, P3, D4, P4));

    private static Link link(DeviceId d1, BigPortNumber p1,
                             DeviceId d2, BigPortNumber p2) {
        return new DefaultLink(new DefaultConnectionPoint(d1, InterfaceId.valueOf(p1)),
                               new DefaultConnectionPoint(d2, InterfaceId.valueOf(p2)));
    }

    @Test
    public void basicVertex() {
        assertEquals("incorrect device id", D1, V1.deviceId());
        assertTrue("should be equal", V1.equals(V1A));
        assertEquals("incorrect hashCode", V1.hashCode(), V1A.hashCode());
        assertFalse("should not be equal", V1.equals(V2));
        assertTrue("incorrect toString", V1.toString().contains(D1.toString()));
    }

    @Test
    public void basicEdge() {
        assertEquals("incorrect src", V1, E1.src());
        assertEquals("incorrect dst", V2, E1.dst());
        assertEquals("incorrect link", link(D1, P1, D2, P2), E1.link());
        assertTrue("should be equal", E1.equals(E1A));
        assertEquals("incorrect hashCode", E1.hashCode(), E1A.hashCode());
        assertFalse("should not be equal", E1.equals(E2));
        assertTrue("incorrect toString", E1.toString().contains(D1.toString()));
        assertTrue("incorrect toString", E1.toString().contains(D2.toString()));
    }

    @Test(expected=IllegalArgumentException.class)
    public void incongruentSrc() {
        new TopoEdge(V3, V2, link(D1, P1, D2, P2));
    }

    @Test(expected=IllegalArgumentException.class)
    public void incongruentDst() {
        new TopoEdge(V1, V3, link(D1, P1, D2, P2));
    }

}
