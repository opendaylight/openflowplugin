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
import static org.junit.Assert.assertNull;

/**
 * Unit tests for the default event sink broker.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public class DefaultEventSinkBrokerTest {

    private static class FooEvent implements Event {}
    private static class BarEvent implements Event {}

    private static class Sink implements EventSink {
        @Override public void dispatch(Event event) {}
    }

    private static final Sink SINK = new Sink();

    DefaultEventSinkBroker b = new DefaultEventSinkBroker();

    @Test
    public void basics() {
        assertNull(AM_UXS, b.get(FooEvent.class));
        b.addSink(FooEvent.class, SINK);
        assertEquals(AM_NEQ, SINK, b.get(FooEvent.class));
        b.removeSink(FooEvent.class);
        assertNull(AM_UXS, b.get(FooEvent.class));
    }

    @Test
    public void dualSink() {
        b.addSink(FooEvent.class, SINK);
        b.addSink(BarEvent.class, SINK);
        assertEquals(AM_NEQ, SINK, b.get(FooEvent.class));
        assertEquals(AM_NEQ, SINK, b.get(BarEvent.class));

        b.removeSink(FooEvent.class);
        assertNull(AM_UXS, b.get(FooEvent.class));
        assertEquals(AM_NEQ, SINK, b.get(BarEvent.class));
    }

    // an interface-event extending Event
    private static interface EtherealEvent extends Event {}
    // a class implementing the interface-event
    private static class AiryEvent implements EtherealEvent {}
    // a class extending the class implementing the interface-event
    private static class VaporEvent extends AiryEvent {}

    @Test
    public void indirectBindings() {
        b.addSink(EtherealEvent.class, SINK);
        assertEquals(AM_NEQ, SINK, b.get(EtherealEvent.class));
        assertEquals(AM_NEQ, SINK, b.get(AiryEvent.class));
        assertEquals(AM_NEQ, SINK, b.get(VaporEvent.class));
    }
}
