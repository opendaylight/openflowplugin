/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.opendaylight.util.net.BigPortNumber.bpn;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Set of tests of the default path implementation.
 *
 * @author Thomas Vachuska
 */
public class DefaultPathTest {

    private static ConnectionPoint cp(String id, int port) {
        return new DefaultConnectionPoint(DeviceId.valueOf(id),
                                          InterfaceId.valueOf(bpn(port)));
    }

    @Test
    public void basic() {
        List<Link> links = new ArrayList<>();
        links.add(new DefaultLink(cp("A", 1), cp("B", 2)));
        links.add(new DefaultLink(cp("B", 1), cp("C", 3)));
        DefaultPath p1 = new DefaultPath(links);
        DefaultPath p2 = new DefaultPath(links);

        assertEquals("incorrect src", cp("A", 1), p1.src());
        assertEquals("incorrect dst", cp("C", 3), p1.dst());
        assertEquals("incorrect links", links, p1.links());
        assertTrue("should be equal", p1.equals(p2));
        assertEquals("incorrect hashCode", p1.hashCode(), p2.hashCode());
    }

}
