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
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.opendaylight.util.net.IpAddress;

/**
 * Unit tests for AcceptLoop.
 *
 * @author Simon Hunt
 */
public class AcceptLoopTest extends AbstractNbioTest {

    private static final IpAddress LOOPBACK = IpAddress.LOOPBACK_IPv4;
    private static final int PORT = 9696; // some random port
//    private static final int PORT_TWO = 9698; // another port

    private static final SocketAddress SOCK_ADDR =
            new InetSocketAddress(LOOPBACK.toInetAddress(), PORT);

//    private static final SocketAddress SOCK_ADDR_TWO =
//            new InetSocketAddress(LOOPBACK.toInetAddress(), PORT_TWO);

    // concrete subclass to test the abstract superclass
    private static class MyAcceptLoop extends AcceptLoop {
        private final CountDownLatch loopStarted = new CountDownLatch(1);
        private final CountDownLatch loopFinished = new CountDownLatch(1);
        private final CountDownLatch runDone = new CountDownLatch(1);
        private final CountDownLatch ceaseLatch = new CountDownLatch(1);

        private int acceptCount = 0;

        MyAcceptLoop() throws IOException {
            super(SOCK_ADDR);
        }

        MyAcceptLoop(SocketAddress... addresses) throws IOException {
            super(addresses);
        }

        @Override
        protected void accept(ServerSocketChannel ssc) throws IOException {
            acceptCount++;
        }

        // overridden methods to allow for test instrumentation...

        @Override
        public void loop() throws IOException {
            loopStarted.countDown();
            super.loop();
            loopFinished.countDown();
        }

        @Override
        public void run() {
            super.run();
            runDone.countDown();
        }

        @Override
        public void cease() {
            super.cease();
            ceaseLatch.countDown();
        }
    }

//    private ExecutorService conns;
    private MyAcceptLoop myAccLoop;
    private AcceptLoop accLoop;

    @Override
    @Before
    public void setUp() {
        super.setUp();
//        conns = newCachedThreadPool(namedThreads("TestConns"));
    }

    @Test @Ignore("Doesn't shut down the socket")
    public void basic() throws IOException {
        print(EOL + "basic()");
        myAccLoop = new MyAcceptLoop();
        accLoop = myAccLoop;
        exec.execute(accLoop);
        waitForLatch(myAccLoop.loopStarted, "loopStarted");
        delay(200); // take a quick nap
        accLoop.cease();
        waitForLatch(myAccLoop.loopFinished, "loopFinished");
        waitForLatch(myAccLoop.runDone, "runDone");
        assertEquals(AM_NEQ, 0, myAccLoop.acceptCount);
        assertNull(AM_HUH, myAccLoop.sslContext());
    }

    // TODO: add unit tests that illustrate the processing of new connections
}
