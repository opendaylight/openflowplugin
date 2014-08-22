/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

import static org.opendaylight.util.junit.TestTools.assertAfter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Suite of tests for the base process stage outlet implementation.
 * 
 * @author Thomas Vachuska
 */
public class ExecutorDrivenProcessStageTest {

    private Stage.FirstStage stage;

    private Bucket<Data.A> discards = new Bucket<Data.A>();
    private Bucket<Data.B> results = new Bucket<Data.B>();

    @Before
    public void setUp() {
        stage = new Stage.FirstStage();
        stage.setOutlet(results);
        stage.setDiscardOutlet(discards);
        stage.setCorePoolSize(2);
        stage.setMaxPoolSize(4);
        
        assertSame("incorrect downstream outlet", results, stage.getOutlet());
        assertSame("incorrect discard outlet", discards, stage.getDiscardOutlet());
    }
    
    @After
    public void tearDown() {
        stage.forceStop();
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

        // Insist that the item is processed after a sufficient
        assertAfter(100, new Runnable() {
            @Override public void run() {
                assertEquals("incorrect results count", 1, results.items.size());
                assertTrue("stage should be empty again", stage.isEmpty());
            }
        });
    }

    @Test
    public void startFeedStop() {
        stage.setIdleTimeOut(120);
        startAndFeedOne();
        
        // Feed it an item (to restart the idle timeout)
        stage.accept(new Data.A("bar"));
        stage.stop();
        assertTrue("stage should be stopped", stage.isStopped());
        assertFalse("stage should not be finished yet", stage.isFinished());
        assertFalse("stage should not be idle yet", stage.isIdle());

        // Insist that the item is processed after a sufficient delay which
        // corresponds to the idle timeout + processing delay (+ extra)
        int delay = (int) stage.getIdleTimeOut() + stage.processingDelay + 100;
        
        assertAfter(delay, new Runnable() {
            @Override public void run() {
                assertTrue("stage should be finished", stage.isFinished());
                assertTrue("stage should be idle", stage.isIdle());
            }
        });
    }

    @Test
    public void startFeedStopNow() {
        startAndFeedOne();
        stage.forceStop();
        assertTrue("stage should be stopped", stage.isStopped());
        assertTrue("stage should be finished", stage.isFinished());
    }

    @Test
    public void customExecutor() {
        ExecutorService es = Executors.newSingleThreadExecutor();
        stage.setExecutorService(es);
        assertSame("incorrect executor service", es, stage.getExecutorService());
    }

    @Test(expected=IllegalStateException.class)
    public void lateSetCustomExecutor() {
        stage.start();
        stage.setExecutorService(Executors.newSingleThreadExecutor());
    }

}
