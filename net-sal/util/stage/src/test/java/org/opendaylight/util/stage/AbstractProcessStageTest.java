/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

import static org.opendaylight.util.junit.TestTools.assertAfter;
import static org.opendaylight.util.junit.TestTools.delay;
import static org.opendaylight.util.stage.Stage.SIMULATED_PROCESSING_DELAY_MS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Suite of tests for the base process stage outlet implementation.
 * 
 * @author Thomas Vachuska
 */
public class AbstractProcessStageTest {

    private static class MyStage extends 
        AbstractProcessStage<Data.A, Data.B, Data.C> {
        
        @Override public boolean accept(final Data.A item) {
            if (!super.accept(item))
                return false;
            new Thread() {
                @Override public void run() {
                    delay(SIMULATED_PROCESSING_DELAY_MS);
                    String s = item.toString();
                    if (s.endsWith("!"))
                        discard(item);
                    else if (s.endsWith("?"))
                        branch(new Data.C(s));
                    else
                        forward(new Data.B(s));
                    tally(-1);
                }
            }.start();
            return true;
        }
        
    }

    private static final int STAGE_DELAY_MS = 100;
    private static final int STAGE_IDLE_MS = 120;
    
    private MyStage stage;

    private Bucket<Data.A> discards = new Bucket<Data.A>();
    private Bucket<Data.B> results = new Bucket<Data.B>();
    private Bucket<Data.C> branches = new Bucket<Data.C>();

    @Before
    public void setUp() {
        stage = new MyStage();
        stage.setOutlet(results);
        stage.setDiscardOutlet(discards);
        stage.setBranchOutlet(branches);
        
        assertSame("incorrect downstream outlet", results, stage.getOutlet());
        assertSame("incorrect discard outlet", discards, stage.getDiscardOutlet());
        assertSame("incorrect branch outlet", branches, stage.getBranchOutlet());
    }
    
    private void clearLists() {
        results.items.clear();
        branches.items.clear();
        discards.items.clear();
    }

    @Test
    public void feedStoppedStage() {
        assertTrue("stage should be stopped until started", stage.isStopped());
        boolean ok = stage.accept(new Data.A("foo"));
        assertTrue("stage should be empty still", stage.isEmpty());
        assertFalse("stopped stage should not accept items", ok);
        assertEquals("incorrect item count", 0, stage.size());
    }

    @Test
    public void startAndFeedOne() {
        stage.start();
        assertFalse("stage should not be stopped now", stage.isStopped());

        // Feed an item to the stage and make sure its in.
        boolean ok = stage.accept(new Data.A("foo"));
        assertTrue("started stage should accept items", ok);
        assertEquals("incorrect item count", 1, stage.size());
        assertFalse("stage should not be empty now", stage.isEmpty());

        // Pause and insist that the item is processed.
        assertAfter(STAGE_DELAY_MS, new Runnable() {
            @Override public void run() {
                assertEquals("incorrect results count", 1, results.items.size());
            }
        });
        assertTrue("stage should be empty again", stage.isEmpty());
    }

    @Test
    public void startFeedStop() {
        stage.setIdleTimeOut(STAGE_IDLE_MS);
        startAndFeedOne();
        
        boolean ok = stage.accept(new Data.A("bar"));
        assertTrue("started stage should accept items", ok);

        stage.stop();
        ok = stage.accept(new Data.A("denied"));
        assertFalse("stopped stage should not accept items", ok);

        assertTrue("stage should be stopped", stage.isStopped());
        assertFalse("stage should not be finished yet", stage.isFinished());
        assertFalse("stage should not be idle yet", stage.isIdle());
        
        assertAfter(STAGE_DELAY_MS, new Runnable() {
            @Override public void run() {
                assertTrue("stage should be finished", stage.isFinished());
            }
        });

        assertAfter(STAGE_IDLE_MS * 2, new Runnable() {
            @Override public void run() {
                assertTrue("stage should be idle", stage.isIdle());
            }
        });
    }

    @Test
    public void startFeedStopNow() {
        startAndFeedOne();
        stage.forceStop();
        assertTrue("stage should be stopped", stage.isStopped());
        
        // Pre-empt current thread just a bit to allow for hard stop of 
        // the backing executor service to take place
        delay(1);
        
        assertTrue("stage should be finished", stage.isFinished());
    }

    @Test
    public void branch() {
        startAndFeedOne();
        clearLists();
        stage.accept(new Data.A("foo?"));
        assertAfter(STAGE_DELAY_MS, new Runnable() {
            @Override public void run() {
                assertEquals("branching was expected", 1, branches.items.size());
                assertEquals("no result was expected", 0, results.items.size());
                assertEquals("no discard was expected", 0, discards.items.size());
            }
        });
    }

    @Test
    public void discard() {
        startAndFeedOne();
        clearLists();
        stage.accept(new Data.A("bar!"));
        assertAfter(STAGE_DELAY_MS, new Runnable() {
            @Override public void run() {
                assertEquals("discard was expected", 1, discards.items.size());
                assertEquals("no result was expected", 0, results.items.size());
                assertEquals("no branching was expected", 0, branches.items.size());
            }
        });
    }

}
