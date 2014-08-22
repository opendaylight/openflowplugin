/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.net.BigPortNumber.bpn;


/**
 * Unit tests for {@link DefaultHostLocation}.
 *
 * @author Shaun Wackerly
 * @author Simon Hunt
 */
public class DefaultHostLocationTest {

    private static final DeviceId id = DeviceId.valueOf("fingerprint");
    private static final InterfaceId intfId = InterfaceId.valueOf(bpn(1));

    private DefaultHostLocation hl;
    
    @Test
    public void basic() {
        hl = new DefaultHostLocation(id, intfId);
        long ts = System.currentTimeMillis();
        
        assertEquals(AM_NEQ, id, hl.elementId());
        assertEquals(AM_NEQ, intfId, hl.interfaceId());
        assertTrue("unexpected timestamp", ts >= hl.ts());
        assertTrue("unexpected timestamp", ts - 100 < hl.ts());
    }
    
    @Test(expected = NullPointerException.class)
    public void nullDeviceId() {
        new DefaultHostLocation(null, intfId);
    }

    @Test(expected = NullPointerException.class)
    public void nullInterfaceId() {
        new DefaultHostLocation(id, null);
    }
    
}
