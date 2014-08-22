/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology;

import org.opendaylight.net.model.*;
import org.easymock.EasyMock;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Set of tests of the default topology event implementation.
 *
 * @author Thomas Vachuska
 */
public class DefaultTopologyEventTest {

    private Topology topology;

    @Test
    public void withoutReasons() {
        topology = EasyMock.createMock(Topology.class);
        TopologyEvent te = new DefaultTopologyEvent(topology, null);
        assertSame("incorrect subject", topology, te.subject());
        assertEquals("incorrect type", TopologyEvent.Type.TOPOLOGY_CHANGED, te.type());
        assertNull("no reasons expected", te.reasons());
    }

    @Test
    public void withReasons() {
        topology = EasyMock.createMock(Topology.class);
        TopologyEvent te = new DefaultTopologyEvent(topology, new ArrayList<ModelEvent>());
        assertSame("incorrect subject", topology, te.subject());
        assertEquals("incorrect type", TopologyEvent.Type.TOPOLOGY_CHANGED, te.type());
        assertNotNull("reasons expected", te.reasons());
    }

}
