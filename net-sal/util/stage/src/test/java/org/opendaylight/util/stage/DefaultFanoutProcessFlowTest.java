/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

import static org.opendaylight.util.junit.TestTools.AM_HUH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.opendaylight.util.ThroughputTracker;
import org.opendaylight.util.stage.Stage.FirstStage;
import org.opendaylight.util.stage.Stage.SecondStage;
import org.opendaylight.util.stage.Stage.ThirdStage;

import org.junit.Before;
import org.junit.Test;

/**
 * Set of tests for the fan-out process flow variant.
 *
 * @author Thomas Vachuska
 */
public class DefaultFanoutProcessFlowTest extends DefaultProcessFlowTest {
    
    protected FanoutProcessFlow tfpf;
    protected FanoutProcessFlow pfpf;
    
    /** Unterminated process flow fixture. */
    static class PartialFanoutProcessFlow extends DefaultFanoutProcessFlow {
        PartialFanoutProcessFlow() {
            addStageClass(Stage.One.class, Data.A.class, Data.B.class);
            addStageClass(Stage.Two.class, Data.B.class, Data.C.class);
        }
    }
    
    /** Terminated process flow fixture. */
    static class TerminatedFanoutProcessFlow extends PartialFanoutProcessFlow {
        TerminatedFanoutProcessFlow() {
            super();
            addStageClass(Stage.Three.class, Data.C.class, null);
        }
    }
    
    @Override
    @Before
    public void setUp() {
        // Create fan-out flow test fixtures, and preserve a way to view them
        // as such, while allowing the superclass test to view them as simple
        // process flows.
        tpf = tfpf = new TerminatedFanoutProcessFlow();
        ppf = pfpf = new PartialFanoutProcessFlow();
    }
    
    /**
     * Overrides the semantics to fit the behaviour of the fanout process flow.
     */
    @Override
    @Test
    public void basics() {
        assertFalse("stage 1 should not be executable yet", tpf.isExecutable(Stage.One.class));

        first = new FirstStage();
        tpf.add(Stage.One.class, first);
        assertTrue("there should be a stage outlet now", tfpf.getOutlets(Stage.One.class).contains(first));
        assertTrue("stage 1 should be executable", tpf.isExecutable(Stage.One.class));

        second = new SecondStage();
        tpf.add(Stage.Two.class, second);
        assertTrue("stage 1 should be executable now", tpf.isExecutable(Stage.One.class));
        assertTrue("stage 2 should be executable", tpf.isExecutable(Stage.Two.class));
        assertFalse("flow should not be executable yet", tpf.isExecutable());

        third = new ThirdStage();
        tpf.add(Stage.Three.class, third);
        assertTrue("stage 2 should be executable now", tpf.isExecutable(Stage.Two.class));
        assertTrue("stage 3 should be executable now (because it is terminal)", tpf.isExecutable(Stage.Three.class));
        assertTrue("flow should be executable now", tpf.isExecutable());
    }       
    
    @Override
    @Test
    public void basicsOutOfOrder() {
        tpf.add(Stage.Two.class, new SecondStage());
        assertFalse("stage 1 should not be executable yet", tpf.isExecutable(Stage.One.class));
        assertTrue("stage 2 should be executable", tpf.isExecutable(Stage.Two.class));

        tpf.add(Stage.One.class, new FirstStage());
        assertTrue("stage 1 should be executable now", tpf.isExecutable(Stage.One.class));
        assertTrue("stage 2 should not be executable", tpf.isExecutable(Stage.Two.class));
        assertFalse("flow should not yet be executable", tpf.isExecutable());

        tpf.add(Stage.Three.class, new ThirdStage());
        assertTrue("stage 2 should be executable now", tpf.isExecutable(Stage.Two.class));
        assertTrue("stage 3 should be executable now (because it is terminal)", tpf.isExecutable(Stage.Three.class));
        assertTrue("flow should be executable now", tpf.isExecutable());
    }

    @Override
    @Test
    public void basicsMiddleSectionLast() {
        tpf.add(Stage.One.class, new FirstStage());
        tpf.add(Stage.Three.class, new ThirdStage());
        assertTrue("stage 1 should be executable", tpf.isExecutable(Stage.One.class));
        assertTrue("stage 3 should be executable", tpf.isExecutable(Stage.Three.class));
        assertFalse("stage 2 should not be executable until present", tpf.isExecutable(Stage.Two.class));

        tpf.add(Stage.Two.class, new SecondStage());
        assertTrue("stage 1 should be executable now", tpf.isExecutable(Stage.One.class));
        assertTrue("stage 2 should be executable now", tpf.isExecutable(Stage.Two.class));
        assertTrue("flow should be executable now", tpf.isExecutable());
    }

    @Override
    @Test
    public void removeSecondStage() {
        basics();

        boolean removed = tpf.remove(Stage.Two.class, second);
        assertTrue(AM_HUH, removed);
        assertTrue("stage 1 should still be executable", tpf.isExecutable(Stage.One.class));
        assertFalse("there should be no stage outlet anymore", tfpf.getOutlets(Stage.Two.class).contains(second));
        assertFalse("stage 2 should not be executable anymore", tpf.isExecutable(Stage.Two.class));
        assertFalse("the flow should not be executable anymore", tpf.isExecutable());
    }

    @Override
    @Test
    public void startFeedStop() {
        super.startFeedStop();
        
        ThroughputTracker tt = tfpf.getStageTracker(Stage.Two.class);
        assertNotNull("we should get a valid tracker", tt);
        assertEquals("incorrect tracker count", 1, tt.total());
    }
    
}
