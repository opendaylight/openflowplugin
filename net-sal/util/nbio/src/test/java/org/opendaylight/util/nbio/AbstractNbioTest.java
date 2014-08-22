/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

import org.junit.Before;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.opendaylight.util.junit.TestTools.AM_UNEX;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.NamedThreadFactory.namedThreads;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.junit.Assert.fail;

/**
 * Base class for NBIO unit tests.
 *
 * @author Simon Hunt
 */
public abstract class AbstractNbioTest {

    protected static final long MAX_MS_WAIT = 500;

    /** Block on specified countdown latch. Return when countdown reaches
     * zero, or fail the test if the {@value #MAX_MS_WAIT} ms timeout expires.
     *
     * @param latch the latch
     * @param label an identifying label
     */
    protected void waitForLatch(CountDownLatch latch, String label) {
        try {
            boolean ok = latch.await(MAX_MS_WAIT, TimeUnit.MILLISECONDS);
            if (!ok)
                fail("Latch await timeout! [" + label + "]");
        } catch (InterruptedException e) {
            print("Latch interrupt [" + label + "] : " + e);
            fail(AM_UNEX);
        }
    }

    protected ExecutorService exec;

    @Before
    public void setUp() {
        exec = newSingleThreadExecutor(namedThreads("TestExec"));
    }

}
