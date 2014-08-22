/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

import static org.opendaylight.util.junit.TestTools.AM_HUH;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.delay;
import static org.opendaylight.util.junit.TestTools.print;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for SelectLoop.
 *
 * @author Simon Hunt
 */
public class SelectLoopTest extends AbstractNbioTest {

    private static final String E_TEST_IOE_OPEN =
            "Test I/O Exception OPEN - expected in this unit test";
    private static final String E_TEST_IOE_LOOP =
            "Test I/O Exception LOOP - expected in this unit test";
    private static final int LOOP_DELAY = 10;

    // concrete subclass of class under test, with test instrumentation.
    private static class MySelectLoop extends SelectLoop {
        private final Selector mockSelector;
        private boolean errorOnOpen = false;
        private int errorAfterIterations = 0;
        private int count;
        private CountDownLatch runDone = new CountDownLatch(1);
        private CountDownLatch loopLatch;
        private CountDownLatch ceaseLatch = new CountDownLatch(1);

        // use default selector
        public MySelectLoop() throws IOException {
            this.mockSelector = null;
        }

        // use custom selector
        public MySelectLoop(MockSelector mockSelector) throws IOException {
            this.mockSelector = mockSelector;
        }

        @Override
        public String toString() {
            return "{MSL: stopped=" + stopped + ", count=" + count +
                    ", sel=" + selector + "}";
        }

        @Override
        protected void loop() throws IOException {
            while (!stopped) {
                print("looping {}", count++);
                if (errorAfterIterations != 0 && errorAfterIterations >= count)
                    throw new IOException(E_TEST_IOE_LOOP);

                delay(LOOP_DELAY);
                if (loopLatch != null)
                    loopLatch.countDown();
            }
        }

        @Override
        public void run() {
            super.run();
            runDone.countDown();
        }

        @Override
        protected Selector openSelector() throws IOException {
            if (errorOnOpen)
                throw new IOException(E_TEST_IOE_OPEN);
            return mockSelector != null ? mockSelector : super.openSelector();
        }

        @Override
        public void cease() {
            super.cease();
            ceaseLatch.countDown();
        }
    }

    // reference by concrete test class
    private MySelectLoop myLoop;
    // reference by abstract class under test
    private SelectLoop loop;

    private MockSelector mockSelector;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        mockSelector = new MockSelector();
    }

    private void waitForLoopLatch() {
        waitForLatch(myLoop.loopLatch, "loop");
    }

    private void waitForCeaseLatch() {
        waitForLatch(myLoop.ceaseLatch, "cease");
    }

    private void waitForRunDone() {
        waitForLatch(myLoop.runDone, "run");
    }


    @Test
    public void basic() throws IOException {
        print(EOL + "basic()");
        loop = new MySelectLoop();
        print(loop);
        assertNotNull("selector", loop.selector);
        assertFalse("stopped", loop.stopped);
        assertNull("error set", loop.getError());
        assertEquals(AM_NEQ, SelectLoop.DEFAULT_TIMEOUT, loop.timeout);
    }

    @Test @Ignore("moved selector to IO loop constructor... need to fix")
    public void runAndCeaseWithMockSelector() throws IOException {
        print(EOL + "runAndCeaseWithMockSelector()");
        myLoop = new MySelectLoop(mockSelector);
        myLoop.loopLatch = new CountDownLatch(3);
        loop = myLoop;
        print(loop);

        exec.execute(loop);
        waitForLoopLatch();
        print(loop);
        assertNotNull("no selector", loop.selector);
        assertTrue("not looping?", myLoop.count >= 3);
        assertFalse(AM_HUH, loop.stopped);

        loop.cease();
        waitForCeaseLatch();
        print(loop);
        int stoppedCount = myLoop.count;
        assertTrue(AM_HUH, loop.stopped);
        delay(LOOP_DELAY * 2);
        assertEquals("loop still going?", stoppedCount, myLoop.count);
        assertNull(AM_HUH, loop.getError());
        assertEquals(AM_NEQ, 1, mockSelector.wakeUpCount);
    }

    @Test @Ignore("moved selector to IO loop constructor... need to fix")
    public void ioErrorOnOpen() throws IOException {
        print(EOL + "ioErrorOnOpen()");
        myLoop = new MySelectLoop(mockSelector);
        myLoop.errorOnOpen = true;
        // TODO: consider using a TestLogger to assert the log messages
        loop = myLoop;
        print(loop);

        exec.execute(loop);
        waitForRunDone();
        print(loop);
        assertEquals(AM_NEQ, 0, myLoop.count);
        assertTrue(AM_HUH, loop.stopped);
        assertEquals(AM_NEQ, E_TEST_IOE_OPEN, loop.getError().getMessage());
    }

    @Test
    public void ioErrorInLoop() throws IOException {
        print(EOL + "ioErrorInLoop()");
        myLoop = new MySelectLoop();
        myLoop.errorAfterIterations = 2;
        // TODO: consider using a TestLogger to assert the log messages
        loop = myLoop;
        print(loop);

        exec.execute(loop);
        waitForRunDone();

        print(loop);
        assertTrue(AM_HUH, loop.stopped);
        assertEquals(AM_NEQ, E_TEST_IOE_LOOP, loop.getError().getMessage());
    }
}
