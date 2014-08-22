/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

import static org.junit.Assert.assertEquals;

import org.opendaylight.util.ThroughputTracker;

import org.junit.Before;
import org.junit.Test;

/**
 * Suite of tests for the metering fan-out outlet functionality.
 *
 * @author Thomas Vachuska
 */
public class MeteringFanoutOutletTest extends FanoutOutletTest {

    private MeteringFanoutOutlet<String> mfo;
    
    @Override
    @Before
    public void setUp() throws Exception {
        mfo = new MeteringFanoutOutlet<String>();
        fo = mfo;
    }
    
    @Test
    public void meter() {
        basics();
        ThroughputTracker tt = mfo.getTracker();
        assertEquals("incorrect number of items", 2, tt.total());
    }

}
