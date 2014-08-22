/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.of.lib.err.ECodeBadRequest;
import org.opendaylight.of.lib.err.ErrorType;

import java.util.Set;

import static junit.framework.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageFutureBag.BagResult.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for MessageFutureBag.
 *
 * @author Simon Hunt
 */
public class MessageFutureBagTest {

    private static final String AM_MF = "Missing future";

    private static final RuntimeException CAUSE =
            new RuntimeException("Foo Problem");

    private static OpenflowMessage createEcho() {
        // bar bar ...
        return create(V_1_0, MessageType.ECHO_REPLY).toImmutable();
    }

    private static OfmError createError(OpenflowMessage request) {
        return (OfmError) ((OfmMutableError) create(REPLY_1, MessageType.ERROR))
                        .errorType(ErrorType.BAD_REQUEST)
                        .errorCode(ECodeBadRequest.BAD_STAT)
                        .toImmutable();
    }

    private static final OpenflowMessage REPLY_1 = createEcho();
    private static final OfmError ERROR_1 = createError(REPLY_1);
    private static final OpenflowMessage REPLY_2 = createEcho();
    private static final OfmError ERROR_2 = createError(REPLY_2);
    private static final OpenflowMessage REPLY_3 = createEcho();
    private static final OfmError ERROR_3 = createError(REPLY_3);
    private static final OpenflowMessage REPLY_4 = createEcho();
    private static final OfmError ERROR_4 = createError(REPLY_4);


    private static final long WAIT = 10;

    MessageFuture f1;
    MessageFuture f2;
    MessageFuture f3;
    MessageFuture f4;
    MessageFutureBag bag;

    @Before
    public void setUp() {
        // We're using the reply/request interchangeably for simplicity.
        f1 = new DefaultMessageFuture(REPLY_1);
        f2 = new DefaultMessageFuture(REPLY_2);
        f3 = new DefaultMessageFuture(REPLY_3);
        f4 = new DefaultMessageFuture(REPLY_4);
        // NOTE : we are INTENTIONALLY adding only futures 1, 2, and 3...
        bag = new MessageFutureBag(f1, f2, f3);
}
    private MessageFutureBag emptyBag() {
        MessageFutureBag bag = new MessageFutureBag();
        assertEquals(AM_NEQ, UNSATISFIED, bag.result());
        assertEquals(AM_UXS, 0, bag.size());
        return bag;
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        print(bag);
        assertEquals(AM_NEQ, UNSATISFIED, bag.result());
        assertEquals(AM_UXS, 3, bag.size());
        Set<MessageFuture> f = bag.futures();
        assertTrue(AM_MF, f.contains(f1));
        assertTrue(AM_MF, f.contains(f2));
        assertTrue(AM_MF, f.contains(f3));
    }

    @Test
    public void empty() {
        print(EOL + "empty()");
        bag = emptyBag();
        print(bag);
    }

    @Test
    public void addSome() {
        print(EOL + "addSome()");
        bag = new MessageFutureBag(f1);
        print(bag);
        assertEquals(AM_NEQ, UNSATISFIED, bag.result());
        assertEquals(AM_UXS, 1, bag.size());
        bag.add(f3, f2);
        assertEquals(AM_UXS, 3, bag.size());
        Set<MessageFuture> f = bag.futures();
        assertTrue(AM_MF, f.contains(f1));
        assertTrue(AM_MF, f.contains(f2));
        assertTrue(AM_MF, f.contains(f3));
    }

    @Test
    public void successInterruptable() {
        print(EOL + "successInterruptable()");
        f1.setSuccess(REPLY_1);
        f2.setSuccess(REPLY_2);
        f3.setSuccess(REPLY_3);
        try {
            bag.await();
        } catch (InterruptedException e) {
            print(e);
            fail("bag.await() interrupted");
        }
        print(bag);
        assertEquals(AM_NEQ, SUCCESS, bag.result());
    }

    @Test
    public void success() {
        print(EOL + "success()");
        f1.setSuccess(REPLY_1);
        f2.setSuccess(REPLY_2);
        f3.setSuccess(REPLY_3);
        bag.awaitUninterruptibly();
        print(bag);
        assertEquals(AM_NEQ, SUCCESS, bag.result());
    }

    @Test
    public void successLimitedWait() {
        print(EOL + "successLimitedWait()");
        f1.setSuccess(REPLY_1);
        f2.setSuccess(REPLY_2);
        f3.setSuccess(REPLY_3);
        boolean ok = bag.awaitUninterruptibly(WAIT);
        print(bag);
        assertTrue(AM_HUH, ok);
        assertEquals(AM_NEQ, SUCCESS, bag.result());
    }

    @Test
    public void timedOutWait() {
        print(EOL + "timedOutWait()");
        f2.setSuccess(REPLY_2);
        f3.setSuccess(REPLY_3);
        boolean ok = bag.awaitUninterruptibly(WAIT);
        print(bag);
        assertFalse(AM_HUH, ok);
        assertEquals(AM_NEQ, TIMEOUT, bag.result());
    }

    @Test
    public void successWithErrors() {
        print(EOL + "successWithErrors()");
        f1.setSuccess(REPLY_1);
        f2.setFailure(ERROR_2);
        f3.setSuccess(REPLY_3);
        bag.awaitUninterruptibly();
        print(bag);
        assertEquals(AM_NEQ, SUCCESS_WITH_ERRORS, bag.result());
    }

    @Test
    public void successWithErrorsWait() {
        print(EOL + "successWithErrorsWait()");
        f1.setSuccess(REPLY_1);
        f2.setFailure(ERROR_2);
        f3.setSuccess(REPLY_3);
        boolean ok = bag.awaitUninterruptibly(WAIT);
        print(bag);
        assertTrue(AM_HUH, ok);
        assertEquals(AM_NEQ, SUCCESS_WITH_ERRORS, bag.result());
    }

    @Test
    public void successWithExceptionsNoErrors() {
        print(EOL + "successWithExceptionsNoErrors()");
        f1.setSuccess(REPLY_1);
        f2.setSuccess(REPLY_2);
        f3.setFailure(CAUSE);
        bag.awaitUninterruptibly();
        print(bag);
        assertEquals(AM_NEQ, SUCCESS_WITH_EXCEPTIONS, bag.result());
    }

    @Test
    public void successWithExceptionsAndErrors() {
        print(EOL + "successWithExceptionsAndErrors()");
        f1.setSuccess(REPLY_1);
        f2.setFailure(ERROR_2);
        f3.setFailure(CAUSE);
        bag.awaitUninterruptibly();
        print(bag);
        assertEquals(AM_NEQ, SUCCESS_WITH_EXCEPTIONS, bag.result());
    }

    @Test
    public void failureWithErrors() {
        print(EOL + "failureWithErrors()");
        f1.setFailure(ERROR_1);
        f2.setFailure(ERROR_2);
        f3.setFailure(ERROR_3);
        bag.awaitUninterruptibly();
        print(bag);
        assertEquals(AM_NEQ, FAILED_WITH_ERRORS, bag.result());
    }

    @Test
    public void failureWithExceptions() {
        print(EOL + "failureWithExceptions()");
        f1.setFailure(ERROR_1);
        f2.setFailure(ERROR_2);
        f3.setFailure(CAUSE);
        bag.awaitUninterruptibly();
        print(bag);
        assertEquals(AM_NEQ, FAILED_WITH_EXCEPTIONS, bag.result());
    }

    @Test
    public void failureWithAllExceptions() {
        print(EOL + "failureWithAllExceptions()");
        f1.setFailure(CAUSE);
        f2.setFailure(CAUSE);
        f3.setFailure(CAUSE);
        bag.awaitUninterruptibly();
        print(bag);
        assertEquals(AM_NEQ, FAILED_WITH_EXCEPTIONS, bag.result());
    }

    @Test(expected = IllegalStateException.class)
    public void noFutures() {
        bag = new MessageFutureBag();
        try {
            bag.await();
        } catch (InterruptedException e) {
            fail(AM_UNEX);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void noFuturesUninterruptibly() {
        bag = emptyBag();
        bag.awaitUninterruptibly();
    }

    @Test(expected = IllegalStateException.class)
    public void noFuturesUninterruptiblyWithTimeout() {
        bag = emptyBag();
        bag.awaitUninterruptibly(WAIT);
    }

}
