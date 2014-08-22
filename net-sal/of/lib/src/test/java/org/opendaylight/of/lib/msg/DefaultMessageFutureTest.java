/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.AbstractTest;
import org.opendaylight.of.lib.err.ECodeBadRequest;
import org.opendaylight.of.lib.err.ErrorType;
import org.opendaylight.util.TimeUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageFuture.Result.*;
import static org.opendaylight.util.NamedThreadFactory.namedThreads;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for DefaultMessageFuture.
 *
 * @author Simon Hunt
 */
public class DefaultMessageFutureTest extends AbstractTest {

    private static final TimeUtils TIME = TimeUtils.getInstance();
    private static final long MAX_LATCH_WAIT_MS = 200;

    private static final OpenflowMessage REQUEST =
            create(V_1_0, MessageType.ECHO_REQUEST).toImmutable();
    private static final OpenflowMessage REPLY =
            create(REQUEST, MessageType.ECHO_REPLY).toImmutable();

    private static final long XID = REPLY.getXid();

    private static final OfmError ERROR = (OfmError)
            ((OfmMutableError) create(REQUEST, MessageType.ERROR))
                    .errorType(ErrorType.BAD_REQUEST)
                    .errorCode(ECodeBadRequest.BAD_STAT)
                    .toImmutable();
    private static final RuntimeException EXCEPT =
            new RuntimeException("Foo Problem");

    private MessageFuture future;
    private CountDownLatch signal;

    private static final ExecutorService exec =
            Executors.newCachedThreadPool(namedThreads("TestPool"));

    // === define a class that will listen for future satisfaction
    private static class FutureListener implements Runnable {
        private final String name;
        private final MessageFuture future;
        private final CountDownLatch latch;
        private final long maxWaitMs;
        private MessageFuture.Result actual;
        private long startMs;
        private long doneMs;

        private FutureListener(String name, MessageFuture future,
                               CountDownLatch signal) {
            this.name = name;
            this.future = future;
            this.latch = signal;
            this.maxWaitMs = -1;
        }

        public FutureListener(String name, MessageFuture future,
                              CountDownLatch signal,
                              long maxWaitMs) {
            this.name = name;
            this.future = future;
            this.latch = signal;
            this.maxWaitMs = maxWaitMs;
        }

        @Override
        public void run() {
            startMs = TIME.currentTimeMillis();
            print("{} started at {}", this, TIME.hhmmssnnn());

            if (maxWaitMs < 0)
                future.awaitUninterruptibly();
            else
                future.awaitUninterruptibly(maxWaitMs);

            doneMs = TIME.currentTimeMillis();
            print("  Future: {}", future);
            actual = future.result();
            print("{} done at {}", this, TIME.hhmmssnnn());
            latch.countDown();
        }

        private long duration() {
            return doneMs > 0 ? doneMs - startMs : 0;
        }

        @Override
        public String toString() {
            return "{FListener " + name + " actual=" + actual +
                    " dur=" + duration() + "}";
        }
    }

    //============================

    @Test(expected = IllegalArgumentException.class)
    public void xidOfZero() {
        MutableMessage mm = create(V_1_0, MessageType.ECHO_REQUEST);
        mm.clearXid();
        new DefaultMessageFuture(mm.toImmutable());
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        future = new DefaultMessageFuture(REQUEST);
        print(future);
        assertEquals(AM_NEQ, REQUEST, future.request());
        assertEquals(AM_NEQ, XID, future.xid());
        assertEquals(AM_NEQ, UNSATISFIED, future.result());
        assertNull(AM_HUH, future.reply());
        assertNull(AM_HUH, future.cause());
    }

    @Test
    public void success() {
        print(EOL + "success()");
        future = new DefaultMessageFuture(REQUEST);
        future.setSuccess(REPLY);
        print(future);
        assertEquals(AM_NEQ, XID, future.xid());
        assertEquals(AM_NEQ, SUCCESS, future.result());
        assertEquals(AM_NEQ, REPLY, future.reply());
        assertNull(AM_HUH, future.cause());
    }

    @Test
    public void successNoReply() {
        print(EOL + "successNoReply()");
        future = new DefaultMessageFuture(REQUEST);
        future.setSuccess();
        print(future);
        assertEquals(AM_NEQ, XID, future.xid());
        assertEquals(AM_NEQ, SUCCESS_NO_REPLY, future.result());
        assertNull(AM_HUH, future.reply());
        assertNull(AM_HUH, future.cause());
    }

    @Test
    public void ofmError() {
        print(EOL + "ofmError()");
        future = new DefaultMessageFuture(REQUEST);
        future.setFailure(ERROR);
        print(future);
        assertEquals(AM_NEQ, XID, future.xid());
        assertEquals(AM_NEQ, OFM_ERROR, future.result());
        assertEquals(AM_NEQ, ERROR, future.reply());
        assertNull(AM_HUH, future.cause());
    }

    @Test
    public void exception() {
        print(EOL + "exception()");
        future = new DefaultMessageFuture(REQUEST);
        future.setFailure(EXCEPT);
        print(future);
        assertEquals(AM_NEQ, XID, future.xid());
        assertEquals(AM_NEQ, EXCEPTION, future.result());
        assertNull(AM_NEQ, future.reply());
        assertEquals(AM_NEQ, EXCEPT, future.cause());
    }
    
    @Test
    public void isSuccess() {
        print(EOL + "isSuccess()");

        future = new DefaultMessageFuture(REQUEST);
        future.setFailure(EXCEPT);
        print(future);
        assertFalse(AM_NEQ, future.result().isSuccess());

        future = new DefaultMessageFuture(REQUEST);
        future.setFailure(ERROR);
        print(future);
        assertFalse(AM_NEQ, future.result().isSuccess());

        future = new DefaultMessageFuture(REQUEST);
        future.setSuccess();
        print(future);
        assertTrue(AM_NEQ, future.result().isSuccess());

        future = new DefaultMessageFuture(REQUEST);
        future.setSuccess(REPLY);
        print(future);
        assertTrue(AM_NEQ, future.result().isSuccess());
    }

    // unit tests for listeners

    private void waitForSignal() {
        try {
           if(!signal.await(MAX_LATCH_WAIT_MS, TimeUnit.MILLISECONDS))
               fail("Waiting for countdown latch timed-out");

        } catch (InterruptedException e) {
            fail("countdown latch interrupted");
        }
    }

    private FutureListener newListener(String name) {
        return new FutureListener(name, future, signal);
    }

    private FutureListener newListener(String name, long maxWaitMs) {
        return new FutureListener(name, future, signal, maxWaitMs);
    }


    @Test
    public void basicListen() {
        print(EOL + "basicListen()");
        future = new DefaultMessageFuture(REQUEST);
        signal = new CountDownLatch(1);
        FutureListener fl = newListener("Bob");
        exec.execute(fl);

        // let's wait a little before satisfying the future
        delay(24);
        future.setSuccess();
        waitForSignal();
        // NOTE: Don't do time-based assertions, because of machine differences
        assertEquals(AM_NEQ, SUCCESS_NO_REPLY, fl.actual);
    }

    @Test
    public void threeListeners() {
        print(EOL + "threeListeners()");
        future = new DefaultMessageFuture(REQUEST);
        signal = new CountDownLatch(3);
        FutureListener f1 = newListener("Bob");
        FutureListener f2 = newListener("Joe");
        FutureListener f3 = newListener("Dan");
        exec.execute(f1);
        exec.execute(f2);
        exec.execute(f3);

        // let's wait a little before satisfying the future
        delay(24);
        future.setSuccess(REPLY);
        waitForSignal();
        // NOTE: Don't do time-based assertions, because of machine differences
        assertEquals(AM_NEQ, SUCCESS, f1.actual);
        assertEquals(AM_NEQ, SUCCESS, f2.actual);
        assertEquals(AM_NEQ, SUCCESS, f3.actual);
    }

    @Test
    public void listenButTimeout() {
        print(EOL + "listenButTimeout()");
        future = new DefaultMessageFuture(REQUEST);
        signal = new CountDownLatch(1);
        FutureListener fl = newListener("Sam", 5);
        exec.execute(fl);

        // let's wait a little before satisfying the future
        delay(24);
        future.setSuccess();
        waitForSignal();
        // NOTE: Don't do time-based assertions, because of machine differences
        assertEquals(AM_NEQ, UNSATISFIED, fl.actual);
    }

    @Test
    public void alreadySatisfied() {
        print(EOL + "alreadySatisfied()");
        future = new DefaultMessageFuture(REQUEST);
        future.setSuccess();
        signal = new CountDownLatch(1);
        FutureListener fl = newListener("Foo");
        exec.execute(fl);
        waitForSignal();
        // should return immediately
        // NOTE: Don't do time-based assertions, because of machine differences
        assertEquals(AM_NEQ, SUCCESS_NO_REPLY, fl.actual);
    }

    private static final String NONE = "(none)";

    @Test
    public void problemStringUnsatisfied() {
        print(EOL + "problemStringUnsatisfied()");
        future = new DefaultMessageFuture(REQUEST);
        print(future.problemString());
        assertEquals(AM_NEQ, NONE, future.problemString());
    }

    @Test
    public void problemStringError() {
        print(EOL + "problemStringError()");
        future = new DefaultMessageFuture(REQUEST);
        future.setFailure(ERROR);
        print(future.problemString());
        assertEquals(AM_NEQ, ERROR.toString(), future.problemString());
    }

    @Test
    public void problemStringException() {
        print(EOL + "problemStringException()");
        future = new DefaultMessageFuture(REQUEST);
        future.setFailure(EXCEPT);
        print(future.problemString());
        assertEquals(AM_NEQ, EXCEPT.toString(), future.problemString());
    }
}
