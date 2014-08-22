/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.event;

import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.AM_UXS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test of the base event sink facility.
 *
 * @author Thomas Vachuska
 */
public class AbstractEventSinkTest {

    private static class TestEvent implements Event {
        final String msg;

        TestEvent(String msg) {
            this.msg = msg;
        }
    }

    private static class TestListener {
        String yolo = "";

        public void yo(String msg) {
            this.yolo = msg;
            if (msg.equals("Die"))
                throw new IllegalStateException();
        }
    }

    private static class TestSink
            extends AbstractEventSink<TestEvent, TestListener> {
        Throwable error = null;

        @Override
        protected void dispatch(TestEvent event, TestListener listener) {
            listener.yo(event.msg);
        }

        @Override
        protected void reportError(TestEvent event, TestListener listener, Throwable error) {
            this.error = error;
            super.reportError(event, listener, error);
        }
    }

    @Test
    public void basics() {
        TestSink sink = new TestSink();
        assertEquals(AM_UXS, 0, sink.getListeners().size());

        TestListener flake = new TestListener();
        sink.addListener(new TestListener());
        sink.addListener(flake);
        sink.addListener(new TestListener());

        assertEquals(AM_UXS, 3, sink.getListeners().size());

        // Send some messages and see everyone got them
        sink.dispatch(new TestEvent("Hello"));
        validate(sink, "Hello");
        sink.dispatch(new TestEvent("Crazy"));
        validate(sink, "Crazy");

        // Remove the flake listener and prove its gone
        sink.removeListener(flake);
        assertEquals(AM_UXS, 2, sink.getListeners().size());

        // Dispatch one more message and prove that flake did not get it but
        // others did.
        sink.dispatch(new TestEvent("World"));
        assertEquals(AM_NEQ, "Crazy", flake.yolo);
        validate(sink, "World");

        sink.clearListeners();
        assertEquals(AM_UXS, 0, sink.getListeners().size());
    }

    // Makes sure that all listeners registered the specified message.
    private void validate(TestSink sink, String msg) {
        for (TestListener tl : sink.getListeners())
            assertEquals(AM_NEQ, msg, tl.yolo);
    }

    @Test
    public void reportError() {
        TestSink sink = new TestSink();
        sink.addListener(new TestListener());
        sink.dispatch(new TestEvent("Die"));
        assertTrue("wrong error", sink.error instanceof IllegalStateException);
    }

}
