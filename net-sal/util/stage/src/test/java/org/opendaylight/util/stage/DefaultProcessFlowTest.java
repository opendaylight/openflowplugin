/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

import static org.opendaylight.util.junit.TestTools.AM_HUH;
import static org.opendaylight.util.junit.TestTools.AM_NOEX;
import static org.opendaylight.util.junit.TestTools.AM_UXS;
import static org.opendaylight.util.junit.TestTools.assertAfter;
import static org.opendaylight.util.junit.TestTools.print;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.opendaylight.util.stage.Stage.FirstStage;
import org.opendaylight.util.stage.Stage.FourthStage;
import org.opendaylight.util.stage.Stage.SecondStage;
import org.opendaylight.util.stage.Stage.ThirdStage;
import org.opendaylight.util.stage.Stage.Transmutor;

import org.junit.Before;
import org.junit.Test;

/**
 * Suite of tests for the default process flow implementation.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public class DefaultProcessFlowTest {

    protected static final String NOT_PART_OF_PF_MSG = "is not part of this process flow";

    private static final int TIME_TO_IDLE_MS = 200;

    protected FirstStage first;
    protected SecondStage second;
    protected ThirdStage third;
    
    protected ProcessFlow tpf;
    protected ProcessFlow ppf;

    /** Unterminated process flow fixture. */
    static class PartialProcessFlow extends DefaultProcessFlow {
        PartialProcessFlow() {
            addStageClass(Stage.One.class, Data.A.class, Data.B.class);
            addStageClass(Stage.Two.class, Data.B.class, Data.C.class);
        }
    }
    
    /** Terminated process flow fixture. */
    static class TerminatedProcessFlow extends PartialProcessFlow {
        TerminatedProcessFlow() {
            super();
            addStageClass(Stage.Three.class, Data.C.class, null);
        }
    }

    private Transmutor<?, ?, ?>[] getTransmutors() {
        return new Transmutor<?, ?, ?>[] { first, second, third };
    }

    @Before
    public void setUp() {
        tpf = new TerminatedProcessFlow();
        ppf = new PartialProcessFlow();
    }
    

    @Test
    public void stageBasics() {
        // Verify length of the flow and the correctness of stage order.
        assertEquals(AM_UXS, 3, tpf.getStageCount());
        
        List<Class<? extends ProcessStageOutlet<?, ?>>> stages = tpf.getStageClasses();
        assertEquals("incorrect flow length", 3, stages.size());
        assertSame("incorrect stage 1", Stage.One.class, stages.get(0));
        assertSame("incorrect stage 2", Stage.Two.class, stages.get(1));
        assertSame("incorrect stage 3", Stage.Three.class, stages.get(2));
    }

    @Test
    public void basics() {
        assertNull("there should be no stage outlets yet", tpf.getOutlet(Stage.One.class));
        assertFalse("stage 1 should not be executable yet", tpf.isExecutable(Stage.One.class));

        first = new FirstStage();
        tpf.add(Stage.One.class, first);
        assertSame("there should be stage outlet now", first, tpf.getOutlet(Stage.One.class));
        assertFalse("stage 1 should not be executable until connected", tpf.isExecutable(Stage.One.class));

        second = new SecondStage();
        tpf.add(Stage.Two.class, second);
        assertTrue("stage 1 should be executable now", tpf.isExecutable(Stage.One.class));
        assertFalse("stage 2 should not be executable until connected", tpf.isExecutable(Stage.Two.class));
        assertFalse("flow should not be executable yet", tpf.isExecutable());

        third = new ThirdStage();
        tpf.add(Stage.Three.class, third);
        assertTrue("stage 2 should be executable now", tpf.isExecutable(Stage.Two.class));
        assertTrue("stage 3 should be executable now (because it is terminal)", tpf.isExecutable(Stage.Three.class));
        assertTrue("flow should be executable now", tpf.isExecutable());
    }        

    @Test
    public void basicsOutOfOrder() {
        tpf.add(Stage.Two.class, new SecondStage());
        assertFalse("stage 1 should not be executable yet", tpf.isExecutable(Stage.One.class));
        assertFalse("stage 2 should not be executable until connected", tpf.isExecutable(Stage.Two.class));

        tpf.add(Stage.One.class, new FirstStage());
        assertTrue("stage 1 should be executable now", tpf.isExecutable(Stage.One.class));
        assertFalse("stage 2 should not be executable until connected", tpf.isExecutable(Stage.Two.class));
        assertFalse("flow should not yet be executable", tpf.isExecutable());

        tpf.add(Stage.Three.class, new ThirdStage());
        assertTrue("stage 2 should be executable now", tpf.isExecutable(Stage.Two.class));
        assertTrue("stage 3 should be executable now (because it is terminal)", tpf.isExecutable(Stage.Three.class));
        assertTrue("flow should be executable now", tpf.isExecutable());
    }

    @Test
    public void basicsMiddleSectionLast() {
        tpf.add(Stage.One.class, new FirstStage());
        tpf.add(Stage.Three.class, new ThirdStage());
        assertFalse("stage 1 should not be executable yet", tpf.isExecutable(Stage.One.class));
        assertTrue("stage 3 should be executable", tpf.isExecutable(Stage.Three.class));
        assertFalse("stage 2 should not be executable until present", tpf.isExecutable(Stage.Two.class));

        tpf.add(Stage.Two.class, new SecondStage());
        assertTrue("stage 1 should be executable now", tpf.isExecutable(Stage.One.class));
        assertTrue("stage 2 should be executable now", tpf.isExecutable(Stage.Two.class));
        assertTrue("flow should be executable now", tpf.isExecutable());
    }

    @Test
    public void duplicatedStage() {
        try {
            ppf.addStageClass(Stage.One.class, Data.A.class, Data.B.class);
            fail(AM_NOEX);
        } catch (IllegalStateException e) {
            assertTrue(AM_HUH, e.getMessage().contains("cannot be present twice"));
        }
    }

    @Test
    public void stageAfterTerminal() {
        try {
            tpf.addStageClass(Stage.Four.class, Data.B.class, Data.A.class);
            fail(AM_NOEX);
        } catch (IllegalStateException e) {
            assertTrue(AM_HUH, e.getMessage().contains("cannot be added after a terminal stage"));
        }
    }

    @Test
    public void nullAcceptedItemClass() {
        try {
            ppf.addStageClass(Stage.Three.class, null, null);
            fail(AM_NOEX);
        } catch (NullPointerException e) {
            assertTrue(AM_HUH, e.getMessage().contains("acceptedItemType must not be null"));
        }
    }

    @Test
    public void incongruousProcess() {
        try {
            ppf.addStageClass(Stage.Four.class, Data.B.class, null);
            fail(AM_NOEX);
        } catch (IllegalStateException e) {
            print(e.getMessage());
            assertTrue(AM_HUH, e.getMessage().contains("must consume items of type"));
        }
    }

    @Test
    public void noSuchStageAddition() {
        try {
            ppf.add(Stage.Three.class, new ThirdStage());
            fail(AM_NOEX);
        } catch (IllegalStateException e) {
            print(e.getMessage());
            assertTrue(AM_HUH, e.getMessage().contains(NOT_PART_OF_PF_MSG));
        }
    }

    @Test
    public void getOutletNoSuchStage() {
        try {
            tpf.add(Stage.Four.class, new FourthStage());
            fail(AM_NOEX);
        } catch (IllegalStateException e) {
            assertTrue(AM_HUH, e.getMessage().contains(NOT_PART_OF_PF_MSG));
        }
    }

    @Test
    public void isExecutableNoSuchStage() {
        try {
            tpf.isExecutable(Stage.Four.class);
            fail(AM_NOEX);
        } catch (IllegalStateException e) {
            assertTrue(AM_HUH, e.getMessage().contains(NOT_PART_OF_PF_MSG));
        }
    }


    @Test
    public void removeSecondStage() {
        // pre-assemble a flow
        basics();

        boolean removed = tpf.remove(Stage.Two.class, second);
        assertTrue(AM_HUH, removed);
        assertFalse("stage 1 should not be executable anymore", tpf.isExecutable(Stage.One.class));
        assertFalse("stage 2 should not be executable anymore", tpf.isExecutable(Stage.Two.class));
        assertFalse("the flow should not be executable anymore", tpf.isExecutable());
    }
    
    
    @Test
    public void start() {
        basics();
        assertTrue("flow should be stopped to start", tpf.isStopped());
        assertTrue("flow should be finished to start", tpf.isFinished());
        assertTrue("flow should be idle to start", tpf.isIdle());
        tpf.start();
        assertTrue("flow should be started now", !tpf.isStopped());
    }

    public void stop(boolean force) {
        if (force)
            tpf.forceStop();
        else
            tpf.stop();
        assertTrue("flow should be stopped now", tpf.isStopped());
    }

    @Test
    public void startStop() {
        start();
        stop(false);
    }
    
    @Test
    public void startForceStop() {
        start();
        stop(true);
    }

    @Test
    public void feedUnstarted() {
        basics();
        
        Data.A foo = new Data.A("Foo");
        Outlet<Data.A> inlet = tpf.getOutlet(Stage.One.class);
        
        boolean accepted = inlet.accept(foo);
        assertFalse("foo should not be accepted", accepted);
    }
    
    
    @Test
    public void startFeedStop() {
        start();
        
        Data.A foo = new Data.A("Foo");
        Outlet<Data.A> inlet = tpf.getOutlet(Stage.One.class);
        
        boolean accepted = inlet.accept(foo);
        assertTrue("foo should be accepted", accepted);
        
        // Start of 3 * 50ms(proc) + 25ms(idle) window
        assertTrue("process should not be finished now", !tpf.isFinished());
        assertTrue("process should not be idle now", !tpf.isIdle());
        
        // The item should be at only one of the three stages.
        assertEquals("incorrect load", 1, 
                     tpf.getStageLoad(Stage.One.class) +
                     tpf.getStageLoad(Stage.Two.class) + 
                     tpf.getStageLoad(Stage.Three.class));
        
        // TODO: Verify isEmpty and size/pending item count when the DefaultProcessFlow#getAsProcessStageOutlet is implemented;
        
        // --- end of 3 * 50ms(proc) + 25ms(idle) window
        // Now wait until the process becomes idle and finished. 
        assertAfter(3 * 50 + TIME_TO_IDLE_MS, new Runnable() {
            @Override
            public void run() {
                assertTrue("process should be idle now", tpf.isIdle());
            }
        });
        
        // Verify that the item passed through each stage straight through.
        for (Transmutor<?, ?, ?> t : getTransmutors())
            t.verifyLists(1, 1, 0, 0);
        
        // Now verify that it properly transmuted by the time it reached the
        // top of the each stage. We know that what comes out of the
        // third stage is just Void(s).
        Data.B b = second.items.get(0);
        assertEquals("incorrect transmutation", "B: a: foo", b.toString());
        Data.C c = third.items.get(0);
        assertEquals("incorrect transmutation", "C: B: A: FOO", c.toString());
        
        assertTrue("process should not be finished until stopped", !tpf.isFinished());
        stop(false);
        assertTrue("process should be stopped now", tpf.isStopped());
        assertTrue("process should be finished now", tpf.isFinished());
    }

}
