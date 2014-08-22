/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology.compute.impl;

import org.junit.Test;
import org.opendaylight.net.model.DeviceId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test of the default topo cluster descriptor.
 *
 * @author Thomas Vachuska
 */
public class DefaultTopologyClusterTest {

    @Test
    public void basics() {
        DefaultTopologyCluster tc = new DefaultTopologyCluster(3, 33, 333);
        assertEquals("incorrect id", 3, tc.id());
        assertEquals("incorrect device count", 33, tc.deviceCount());
        assertEquals("incorrect link count", 333, tc.linkCount());
        assertTrue("incorrect to string", tc.toString().contains("3, "));

        DeviceId root = DeviceId.valueOf("00:00:12:23:56:78:90:ab");
        tc.setRoot(root);
        assertEquals("incorrect root", root, tc.root());
    }

}
