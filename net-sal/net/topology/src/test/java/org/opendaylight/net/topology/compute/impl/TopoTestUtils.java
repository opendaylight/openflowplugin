/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology.compute.impl;

import org.opendaylight.net.model.*;

import java.util.Arrays;
import java.util.Iterator;

import static org.opendaylight.util.net.BigPortNumber.bpn;

/**
 * Test utilities to help generate fake/mock data.
 *
 * @author Thomas Vachuska
 */
public class TopoTestUtils {

    // Creates an iterator of the specified devices
    public static Iterator<Device> devices(Device... dev) {
        return Arrays.asList(dev).iterator();
    }

    // Creates an iterator of the specified links
    public static Iterator<Link> links(Link... link) {
        return Arrays.asList(link).iterator();
    }

    // Creates a test device
    public static Device device(String id) {
        return new FakeDevice(DeviceId.valueOf(id));
    }

    // Creates a test device
    public static Link link(String id1, String id2) {
        return new DefaultLink(cp(id1, 1), cp(id2, 1), Link.Type.DIRECT_LINK);
    }

    // Creates a test connection point
    public static ConnectionPoint cp(String id, int p) {
        return new DefaultConnectionPoint(DeviceId.valueOf(id),
                                          InterfaceId.valueOf(bpn(p)));
    }

}
