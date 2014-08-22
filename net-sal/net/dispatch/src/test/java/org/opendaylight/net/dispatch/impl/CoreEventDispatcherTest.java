/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.dispatch.impl;

import org.opendaylight.util.event.Event;
import org.opendaylight.util.event.EventSink;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.assertAfter;
import static org.junit.Assert.assertSame;

/**
 * Test of the event dispatching mechanism.
 *
 * @author Thomas Vachuska
 */
public class CoreEventDispatcherTest {
    
    private CoreEventDispatcher eds;

    private static class KitchenEvent implements Event {}
    private static class BarEvent extends KitchenEvent {}
    private static class Fight extends BarEvent {}
    
    private static class KitchenSink implements EventSink {
        Event pint;
        @Override
        public void dispatch(Event event) {
            pint = event;
        }
    }
    
    private static class BarSink extends KitchenSink {}
    
    private static class DamagedSink extends KitchenSink {
        @Override
        public void dispatch(Event event) {
            throw new NullPointerException("Klang!");
        }
        
    }

    @Before
    public void setUp() {
        eds = new CoreEventDispatcher();
        eds.activate();
    }
    
    @After
    public void tearDown() {
        eds.deactivate();
    }

    @Test
    public void basics() {
        KitchenSink ks = new KitchenSink();
        BarSink bs = new BarSink();

        eds.addSink(KitchenEvent.class, ks);
        eds.addSink(BarEvent.class, bs);
        eds.addSink(Fight.class, new DamagedSink());
        
        postAndVerify(new KitchenEvent(), ks);
        postAndVerify(new BarEvent(), bs);
        
        // Now try to break the dispatcher.
        eds.post(new Fight());

        // Make sure both sinks are still able to get events.
        postAndVerify(new KitchenEvent(), ks);
        postAndVerify(new BarEvent(), bs);
        
        // Close up the bar and try to post an event to it.
        eds.removeSink(BarEvent.class);
        eds.post(new BarEvent());

        // Make sure the remaining sink is still able to get events.
        postAndVerify(new KitchenEvent(), ks);
        
    }
    
    // Posts an event and verifies that the given sink got it eventually
    private void postAndVerify(final KitchenEvent e, final KitchenSink s) {
        eds.post(e);
        assertAfter(1000, new Runnable() {
            @Override public void run() {
                assertSame(AM_NEQ, e, s.pint);
            }
        });
    }

}
