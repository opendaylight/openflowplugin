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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.opendaylight.util.stage.Stage.FirstStage;
import org.opendaylight.util.stage.Stage.SecondStage;
import org.opendaylight.util.stage.Stage.ThirdStage;

import org.junit.Before;
import org.junit.Test;

/**
 * Set of tests for the tappable, fan-out process flow variant.
 *
 * @author Thomas Vachuska
 */
public class DefaultTappableProcessFlowTest extends DefaultFanoutProcessFlowTest {
    
    protected TappableProcessFlow ttpf;
    protected TappableProcessFlow ptpf;
    
    /** Unterminated process flow fixture. */
    static class PartialTappableProcessFlow extends DefaultTappableProcessFlow {
        PartialTappableProcessFlow() {
            addStageClass(Stage.One.class, Data.A.class, Data.B.class);
            addStageClass(Stage.Two.class, Data.B.class, Data.C.class);
        }
    }
    
    /** Terminated process flow fixture. */
    static class TerminatedTappableProcessFlow extends PartialTappableProcessFlow {
        TerminatedTappableProcessFlow() {
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
        tpf = tfpf = ttpf = new TerminatedTappableProcessFlow();
        ppf = pfpf = ptpf = new PartialTappableProcessFlow();
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
        assertTrue("there should be a stage outlet now", ttpf.getOutlets(Stage.One.class).contains(first));
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
        assertFalse("there should be no stage outlet anymore", ttpf.getOutlets(Stage.Two.class).contains(second));
        assertFalse("stage 2 should not be executable anymore", tpf.isExecutable(Stage.Two.class));
        assertFalse("the flow should not be executable anymore", tpf.isExecutable());
    }
    
    @Test
    public void taps() {
        basics();
        
        assertEquals("incorrect tap count", 0, 
                     ttpf.getTapCount(Stage.One.class, first));
        assertTrue("taps should be empty", 
                   ttpf.getTaps(Stage.One.class, first).isEmpty());
        
        Bucket<Data.B> tb1 = new Bucket<Data.B>();
        ttpf.addTap(Stage.One.class, first, tb1);
        assertEquals("incorrect tap count", 1, 
                     ttpf.getTapCount(Stage.One.class, first));
        assertFalse("taps should not be empty", 
                    ttpf.getTaps(Stage.One.class, first).isEmpty());
        assertTrue("tap 1 should be in the list of taps",
                   ttpf.getTaps(Stage.One.class, first).contains(tb1));
        
        Bucket<Data.B> tb2 = new Bucket<Data.B>();
        assertFalse("should not be able to remove tap that has not been added",
                    ttpf.removeTap(Stage.One.class, first, tb2));
        ttpf.addTap(Stage.One.class, first, tb2);
        assertEquals("incorrect tap count", 2, 
                     ttpf.getTapCount(Stage.One.class, first));
        
        // Make sure the taps are correct
        assertTrue("tap 1 should be in the list of taps",
                   ttpf.getTaps(Stage.One.class, first).contains(tb1));
        assertTrue("tap 2 should be in the list of taps",
                   ttpf.getTaps(Stage.One.class, first).contains(tb2));
        
        assertTrue("should be able to remove tap",
                   ttpf.removeTap(Stage.One.class, first, tb1));
        assertEquals("incorrect tap count", 1, 
                     ttpf.getTapCount(Stage.One.class, first));
    }

    @Test
    public void invalidTapRemoval() {
        basics();
        Outlet<Data.B> secondStageOutlet = ttpf.getOutlet(Stage.Two.class);
        try {
            ttpf.removeTap(Stage.One.class, first, secondStageOutlet);
            fail("removal if a downstream outlet from the list of taps should fail"); 
        } catch (IllegalArgumentException e) {
            assertTrue("incorrect message", 
                       e.getMessage().contains("not a tap outlet"));
        }
    }
    
}
