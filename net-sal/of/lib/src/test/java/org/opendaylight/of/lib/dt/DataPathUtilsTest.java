/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.dt;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for DataPathUtils.
 *
 * @author Simon Hunt
 */
public class DataPathUtilsTest {

    private static final class ConcreteUtils extends DataPathUtils {}

    private static final DataPathInfo DPI = new DataPathInfoAdapter();
    private static final String DPID_STR = "3/011222:333444";

    private DataPathUtils utils;
    private DataPathId dpid;

    @Before
    public void setUp() {
        utils = new ConcreteUtils();
    }

    @Test
    public void basic() {
        print(EOL + "basic");
        dpid = DataPathId.valueOf(DPID_STR);
        assertNull(AM_HUH, dpid.memento);
        assertNull(AM_HUH, utils.getMemento(dpid));

        utils.attachMemento(dpid, DPI);
        assertEquals(AM_NEQ, DPI, dpid.memento);
        assertEquals(AM_NEQ, DPI, utils.getMemento(dpid));

        utils.detachMemento(dpid);
        assertNull(AM_HUH, dpid.memento);
        assertNull(AM_HUH, utils.getMemento(dpid));
    }
}
