/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.opendaylight.util.junit.TestTools.assertAfter;
import static org.opendaylight.util.junit.TestTools.delay;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

/**
 * Test to make sure Kujo is properly trained.
 *
 * @author Thomas Vachuska
 */
public class KujoTest {
    
    /**
     * Interruptable task fixture.
     */
    private static class StrawDummy extends Task {

        protected StrawDummy(String name) {
            super(name);
        }

        @Override
        public void run() {
            delay(10000);
        }
    }

    /**
     * Un-interruptable task fixture.
     */
    private static class StubbornDummy extends Task {

        protected StubbornDummy(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (!stopped()) {
                delay(10000);
            }
        }
    }

    private Task gd = new StrawDummy("good");
    private Task bd = new StrawDummy("bad");
    private Task ud = new StrawDummy("ugly");
    

    @After
    public void tearDown() {
        gd.cease();
        bd.cease();
        ud.cease();
        
        gd.interrupt();
        bd.interrupt();
        ud.interrupt();
    }

    @Test
    public void bite() {
        gd = new StrawDummy("good");
        bd = new StrawDummy("bad");
        ud = new StrawDummy("ugly");
        
        gd.start();
        bd.start();
        ud.start();
        
        Kujo.bite("bad|ugly");
        assertAfter(2000, new Runnable() {
            @Override
            public void run() {
                assertFalse("bad dummy should not be alive", bd.isAlive());
                assertFalse("ugly dummy should not be alive", ud.isAlive());
            }
        });
        assertTrue("good dummy should be alive", gd.isAlive());
    }

    @Test
    public void kill() {
        gd = new StubbornDummy("good");
        bd = new StubbornDummy("bad");
        ud = new StubbornDummy("ugly");
        
        gd.start();
        bd.start();
        ud.start();
        
        // Bite should have no effect on stubborn dummies
        Kujo.bite("bad|ugly");
        delay(500);
        assertTrue("bad dummy should be alive", bd.isAlive());
        assertTrue("ugly dummy should be alive", ud.isAlive());
        assertTrue("good dummy should be alive", gd.isAlive());

        // Kill should definitely have an effect on stubborn dummies
        Kujo.kill("bad|ugly");
        assertAfter(2000, new Runnable() {
            @Override
            public void run() {
                assertFalse("bad dummy should not be alive", bd.isAlive());
                assertFalse("ugly dummy should not be alive", ud.isAlive());
            }
        });
        assertTrue("good dummy should be alive", gd.isAlive());
    }


}
