/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pkt.impl;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.of.controller.ErrorEvent;
import org.opendaylight.of.controller.MessageEvent;
import org.opendaylight.of.controller.RoleAdvisor;
import org.opendaylight.of.controller.impl.ListenerService;
import org.opendaylight.of.controller.impl.ListenerServiceAdapter;
import org.opendaylight.of.controller.pkt.*;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.msg.OpenflowMessage;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.packet.ProtocolId;

import java.util.*;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;
import static org.opendaylight.of.controller.pkt.SequencedPacketListenerRole.*;
import static org.opendaylight.of.controller.pkt.impl.Sequencer.E_ALTITUDE_CLAIMED;
import static org.opendaylight.of.controller.pkt.impl.Sequencer.E_NEG_ALTITUDE;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.packet.ProtocolId.*;

/**
 * Unit tests for {@link Sequencer}.
 *
 * @author Scott Simes
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public class SequencerTest extends AbstractSequencerTest {

    private static final String EVENT_NOT_SEEN = "Event not seen (but should'a)";
    private static final String EVENT_SEEN = "Event seen (but shouldn'a)";

    // Need to access protected init() method in superclass
    private static class TestSequencer extends Sequencer {
        void initialize(ListenerService ls, RoleAdvisor ra) {
            super.init(ls, ra);
        }

        void processAnEvent(MessageEvent someEvent) {
            processPacket(someEvent);
        }
    }

    // ListenerService fixture to fake out the controller piece
    private static class FakeController extends ListenerServiceAdapter {
        private final boolean broken;
        private DataPathId lastDpid;
        private int lastAuxId;

        FakeController(boolean broken) {
            this.broken = broken;
        }

        @Override
        public void send(OpenflowMessage msg, DataPathId dpid, int auxId)
                throws OpenflowException {
            lastDpid = dpid;
            lastAuxId = auxId;
            print("{}SENDING... {} to {},aux={}{}", EOL, msg, dpid, auxId, EOL);
            if (broken) {
                print("***** But we are broken *****");
                throw new RuntimeException("Bad Controller!!");
            }
        }

        @Override
        public void send(OpenflowMessage msg, DataPathId dpid)
                throws OpenflowException {
            send(msg, dpid, 0);
        }
    }

    /** A test monitor, to count the number of events processed by the
     * packet processing chain, so we can synchronize the test thread with
     * the processing threads.
     */
    private static class Observer extends SequencedPacketAdapter {
        private CountDownLatch latch;

        private void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void event(MessageContext context) {
            if (latch != null)
                latch.countDown();
        }
    }

    /** Fake role advisor service. */
    private static class TestRoleAdvisor implements RoleAdvisor {
        @Override
        public boolean isMasterFor(DataPathId dpid) {
            return dpid.equals(DPID);
        }
    }

    /** Our test listener class, which we can instrument to assert stuff
     * that happens.
     */
    private static class TestListener implements SequencedPacketListener {
        final String name;
        private int eventCount = 0;
        private int errorCount = 0;
        private boolean willHandle = false;
        private boolean contextWasHandled;
        private boolean contextSendFailed;
        private ErrorEvent err;
        private DataPathId lastDpid;

        TestListener(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "{TestListener:\"" + name + "\", #events=" + eventCount + "}";
        }

        @Override
        public void event(MessageContext context) {
            contextWasHandled = context.isHandled();
            contextSendFailed = context.failedToSend();
            eventCount++;
            lastDpid = context.srcEvent().dpid();
            if (willHandle)
                context.packetOut().send();
        }

        @Override
        public void errorEvent(ErrorEvent event) {
            errorCount++;
            err = event;
        }
    }

    private static final int HIGH_ALTITUDE = 123;
    private static final int MID_ALTITUDE = 59;
    private static final int LOW_ALTITUDE = 27;

    private TestListener high;
    private TestListener mid;
    private TestListener low;
    private TestRoleAdvisor ra;
    private Observer observer;

    private FakeController fake;
    private TestSequencer seq;

    @Before
    public void setUp() {
        seq = new TestSequencer();
        high = new TestListener("high");
        mid = new TestListener("mid");
        low = new TestListener("low");
        ra = new TestRoleAdvisor();
        observer = new Observer();
        fake = new FakeController(false);
        seq.initialize(fake, ra);
    }

    private void assertCounts(int expAdv, int expDir, int expObs) {
        print(seq);
        assertEquals(AM_NEQ, expAdv, seq.getListenerCount(ADVISOR));
        assertEquals(AM_NEQ, expDir, seq.getListenerCount(DIRECTOR));
        assertEquals(AM_NEQ, expObs, seq.getListenerCount(OBSERVER));
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        assertCounts(0, 0, 0);

        seq.addPacketListener(low, DIRECTOR, LOW_ALTITUDE);
        seq.addPacketListener(high, DIRECTOR, HIGH_ALTITUDE);
        seq.addPacketListener(mid, DIRECTOR, MID_ALTITUDE);
        assertCounts(0, 3, 0);

        seq.removePacketListener(mid);
        assertCounts(0, 2, 0);
    }


    @Test
    public void messageIn() {
        print(EOL + "messageIn()");
        seq.addPacketListener(high, ADVISOR, HIGH_ALTITUDE);
        seq.addPacketListener(mid, DIRECTOR, MID_ALTITUDE);
        seq.addPacketListener(low, OBSERVER, LOW_ALTITUDE);
        seq.addPacketListener(observer, OBSERVER, 0);
        assertCounts(1, 1, 2);

        eventsProcessed = new CountDownLatch(1);
        observer.setLatch(eventsProcessed);
        mid.willHandle = true;
        seq.processAnEvent(SOME_EVENT);
        waitForEvents();

        print(SOME_EVENT);
        print(high);
        print(mid);
        print(low);
        assertEquals(AM_NEQ, 1, high.eventCount);
        assertEquals(AM_NEQ, 1, mid.eventCount);
        assertEquals(AM_NEQ, 1, low.eventCount);

        assertEquals(AM_NEQ, false, high.contextWasHandled);
        assertEquals(AM_NEQ, false, mid.contextWasHandled);
        assertEquals(AM_NEQ, true, low.contextWasHandled);
    }

    @Test
    public void duplicateAltitude() {
        print(EOL + "duplicateAltitude()");
        TestListener thingOne = new TestListener("Thing 1");
        TestListener thingTwo = new TestListener("Thing 2");
        seq.addPacketListener(thingOne, ADVISOR, 42);
        seq.addPacketListener(thingTwo, DIRECTOR, 42);
        assertCounts(1, 1, 0);
        // same altitude in different roles is just fine.

        seq.removePacketListener(thingTwo);
        assertCounts(1, 0, 0);

        try {
            seq.addPacketListener(thingTwo, ADVISOR, 42);
            fail(AM_NOEX);
        } catch (IllegalStateException e) {
            print("EX> {}", e);
            assertTrue(AM_WREXMSG, e.getMessage().startsWith(E_ALTITUDE_CLAIMED));
        }
    }

    @Test
    public void duplicateAltitudeTwo() {
        print(EOL + "duplicateAltitudeTwo()");
        seq.addPacketListener(high, OBSERVER, HIGH_ALTITUDE);
        seq.addPacketListener(mid, OBSERVER, MID_ALTITUDE);
        seq.addPacketListener(low, OBSERVER, LOW_ALTITUDE);
        seq.addPacketListener(new TestListener("L1"), OBSERVER, 42);
        assertCounts(0, 0, 4);
        try {
            seq.addPacketListener(new TestListener("L2"), OBSERVER, 42);
            fail(AM_NOEX);
        } catch (IllegalStateException e) {
            print("EX> {}", e);
            assertTrue(AM_WREXMSG, e.getMessage().startsWith(E_ALTITUDE_CLAIMED));
        }
    }

    @Test
    public void negativeAltitude() {
        print(EOL + "negativeAltitude()");
        TestListener neg = new TestListener("Neg");
        try {
            seq.addPacketListener(neg, ADVISOR, -3);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(FMT_EX, e);
            assertTrue(AM_WREXMSG, e.getMessage().startsWith(E_NEG_ALTITUDE));
        }
    }

    @Test(expected = NullPointerException.class)
    public void nullListener() {
        seq.addPacketListener(null, ADVISOR, 0);
    }

    @Test(expected = NullPointerException.class)
    public void nullRole() {
        seq.addPacketListener(mid, null, 0);
    }

    // ======================================================================
    // === Now for a more complete test - first program some test listeners....
    private static final int EXPER_HINT_TYPE = -3;

    private static class TestAdvisor extends TestListener {
        TestAdvisor() { super("Advisor"); }

        @Override
        public void event(MessageContext context) {
            super.event(context);
            printContext(this.name, context);
            assertFalse(AM_HUH, context.isHandled());
            assertNull(AM_HUH, context.getCompletedPacketOut());
            assertArrayEquals(AM_NEQ, EXP_PROTOCOLS,
                    context.getProtocols().toArray());

            // Let's assume we examined the packet and add a hint
            context.addHint(HintFactory.createHint(EXPER_HINT_TYPE));
        }
    }

    private static class TestDirectorOne extends TestListener {
        TestDirectorOne() { super("Director-One"); }

        @Override
        public void event(MessageContext context) {
            super.event(context);
            printContext(this.name, context);
            assertFalse(AM_HUH, context.isHandled());
            assertNull(AM_HUH, context.getCompletedPacketOut());

            // Verify we have the expected hints (packet-type, experimenter)
            List<Hint> hints = context.getHints();
            assertEquals(AM_UXS, 1, hints.size());

            Hint h = hints.get(0);
            assertEquals(AM_NEQ, null, h.getType());
            assertEquals(AM_NEQ, EXPER_HINT_TYPE, h.getEncodedType());
            assertEquals(AM_WRCL, ExperimenterHint.class, h.getClass());

            // Let's assume that we figured out what to do, and we want to
            //  add an OUTPUT action to the packet out
            ProtocolVersion pv = context.getVersion();
            BigPortNumber outPort = bpn(3);

            context.packetOut().addAction(
                    createAction(pv, ActionType.OUTPUT, outPort)
            );
            // we are going to tell the sequencer that we have handled the
            //  packet-in, and that the packet-out is ready to go
            context.packetOut().send();
        }
    }

    private static class TestDirectorTwo extends TestListener {
        TestDirectorTwo() { super("Director-Two"); }

        @Override
        public void event(MessageContext context) {
            super.event(context);
            printContext(this.name, context);
            assertTrue(AM_HUH, context.isHandled());
            assertNotNull(AM_HUH, context.getCompletedPacketOut());

            // we should also be able to find out who handled the packet:
            validateHandlerHint(context);

            // At this point, there is nothing we, as a director, can do, since
            // the packet-out has already been dispatched.
            ProtocolVersion pv = context.getVersion();
            BigPortNumber outPort = bpn(7);
            try {
                context.packetOut().addAction(
                        createAction(pv, ActionType.OUTPUT, outPort)
                );
                fail(AM_NOEX);
            } catch (IllegalStateException e) {
                print(FMT_EX, e);
            }
        }
    }

    private static class TestObserver extends TestListener {
        TestObserver() { super("Observer"); }

        @Override
        public void event(MessageContext context) {
            super.event(context);
            printContext(this.name, context);
            assertTrue(AM_HUH, context.isHandled());
            assertNotNull(AM_HUH, context.getCompletedPacketOut());

            // we should also be able to find out who handled the packet:
            validateHandlerHint(context);

            // we should be able to observe everything that happened with
            // this packet-in and resulting packet-out
            // TODO: add further asserts of import
        }
    }

    private static void printContext(String name, MessageContext ctx) {
        print("{}: {}{}", name, ctx.toDebugString(), EOL);
    }

    private static void validateHandlerHint(MessageContext ctx) {
        HandlerHint handlerHint = null;
        for (Hint h: ctx.getHints())
            if (h.getType() == HintType.HANDLER) {
                handlerHint = (HandlerHint) h;
                break;
            }
        assertNotNull("No handler hint", handlerHint);
        assertEquals(AM_WRCL, TestDirectorOne.class,
                handlerHint.getHandlerClass());
    }


    @Test
    public void moreComplexTest() {
        print(EOL + "moreComplexTest()");
        TestListener adv = new TestAdvisor();
        TestListener dir1 = new TestDirectorOne();
        TestListener dir2 = new TestDirectorTwo();
        TestListener obs = new TestObserver();

        seq.addPacketListener(adv, ADVISOR, 5);
        seq.addPacketListener(dir1, DIRECTOR, 20);
        seq.addPacketListener(dir2, DIRECTOR, 15);
        seq.addPacketListener(obs, OBSERVER, 1);
        seq.addPacketListener(observer, OBSERVER, 0);
        assertCounts(1, 2, 2);
        assertEquals(AM_NEQ, 0, obs.eventCount);

        eventsProcessed = new CountDownLatch(1);
        observer.setLatch(eventsProcessed);
        // send some event down the path
        seq.processAnEvent(SOME_EVENT);
        waitForEvents();
        assertEquals(EVENT_NOT_SEEN, 1, obs.eventCount);

        Collection<SplMetric> metrics = seq.getSplMetrics();
        for (SplMetric m: metrics)
            print(m);
        assertEquals(AM_UXS, 5, metrics.size());
    }

    private static class BrokenAdvisor extends TestListener {
        BrokenAdvisor() { super("Broken-Advisor"); }
        @Override
        public void event(MessageContext context) {
            throw new RuntimeException("Oopsies!");
        }
    }

    @Test
    public void brokenAdvisor() {
        print(EOL + "brokenAdvisor()");
        TestListener adv = new TestAdvisor();
        TestListener dir1 = new TestDirectorOne();
        TestListener dir2 = new TestDirectorTwo();
        TestListener obs = new TestObserver();

        seq.addPacketListener(adv, ADVISOR, 5);
        seq.addPacketListener(new BrokenAdvisor(), ADVISOR, 0);
        seq.addPacketListener(dir1, DIRECTOR, 20);
        seq.addPacketListener(dir2, DIRECTOR, 15);
        seq.addPacketListener(obs, OBSERVER, 1);
        seq.addPacketListener(observer, OBSERVER, 0);
        assertCounts(2, 2, 2);
        assertEquals(AM_NEQ, 0, obs.eventCount);

        eventsProcessed = new CountDownLatch(1);
        observer.setLatch(eventsProcessed);
        // send some event down the path
        seq.processAnEvent(SOME_EVENT);
        waitForEvents();
        assertEquals(EVENT_NOT_SEEN, 1, obs.eventCount);
    }

    @Test
    public void advisorHandledFlagIsIgnored() {
        print(EOL + "advisorHandledFlagIsIgnored()");
        mid.willHandle = true;
        seq.addPacketListener(mid, ADVISOR, 0);
        seq.addPacketListener(low, OBSERVER, 10);
        seq.addPacketListener(observer, OBSERVER, 0);
        assertCounts(1, 0, 2);

        eventsProcessed = new CountDownLatch(1);
        observer.setLatch(eventsProcessed);
        // send some event down the path
        seq.processAnEvent(SOME_EVENT);
        waitForEvents();
        assertEquals(EVENT_NOT_SEEN, 1, low.eventCount);
        assertFalse(AM_HUH, low.contextWasHandled);
    }

    @Test
    public void observerHandledFlagIsIgnored() {
        print(EOL + "observerHandledFlagIsIgnored()");
        mid.willHandle = true;
        seq.addPacketListener(mid, OBSERVER, 3);
        seq.addPacketListener(low, OBSERVER, 1);
        seq.addPacketListener(observer, OBSERVER, 0);
        assertCounts(0, 0, 3);

        eventsProcessed = new CountDownLatch(1);
        observer.setLatch(eventsProcessed);
        // send some event down the path
        seq.processAnEvent(SOME_EVENT);
        waitForEvents();
        assertEquals(EVENT_NOT_SEEN, 1, low.eventCount);
        assertFalse(AM_HUH, low.contextWasHandled);
    }


    @Test
    public void sendFailedAndAnErrorEventGenerated() {
        print(EOL + "sendFailedAndAnErrorEventGenerated()");
        fake = new FakeController(true); // a broken one
        seq.initialize(fake, ra);
        mid.willHandle = true;
        seq.addPacketListener(mid, DIRECTOR, 0);
        seq.addPacketListener(low, OBSERVER, 2);
        seq.addPacketListener(observer, OBSERVER, 0);
        assertCounts(0, 1, 2);

        eventsProcessed = new CountDownLatch(1);
        observer.setLatch(eventsProcessed);
        // send some event down the path
        seq.processAnEvent(SOME_EVENT);
        waitForEvents();

        assertEquals("No error event", 1, mid.errorCount);
        print("Error was: {}", mid.err);

        assertEquals(EVENT_NOT_SEEN, 1, low.eventCount);
        assertEquals("error seen by observer", 0, low.errorCount);
        assertTrue(AM_HUH, low.contextWasHandled);
        assertTrue(AM_HUH, low.contextSendFailed);
    }

    private static final Set<ProtocolId> LOW_INTEREST =
            new HashSet<>(Arrays.asList(TCP));

    private static final Set<ProtocolId> MID_INTEREST =
            new HashSet<>(Arrays.asList(IP));

    private static final Set<ProtocolId> HIGH_INTEREST =
            new HashSet<>(Arrays.asList(LLDP));


    @Test
    public void differingInterests() {
        print(EOL + "differingInterests()");
        // NOTE: sample event packet protocols are [ETHERNET, IP, TCP]
        seq.addPacketListener(low, OBSERVER, LOW_ALTITUDE, LOW_INTEREST);
        seq.addPacketListener(mid, OBSERVER, MID_ALTITUDE, MID_INTEREST);
        seq.addPacketListener(high, OBSERVER, HIGH_ALTITUDE, HIGH_INTEREST);
        seq.addPacketListener(observer, OBSERVER, 0);
        assertCounts(0, 0, 4);

        eventsProcessed = new CountDownLatch(1);
        observer.setLatch(eventsProcessed);
        // send some event down the path
        seq.processAnEvent(SOME_EVENT);
        waitForEvents();
        assertEquals(EVENT_NOT_SEEN, 1, low.eventCount);
        assertEquals(EVENT_NOT_SEEN, 1, mid.eventCount);
        assertEquals(EVENT_SEEN, 0, high.eventCount);

        // re-register high listener with new interest set
        seq.addPacketListener(high, OBSERVER, HIGH_ALTITUDE, LOW_INTEREST);
        eventsProcessed = new CountDownLatch(1);
        observer.setLatch(eventsProcessed);
        // send some event down the path
        seq.processAnEvent(SOME_EVENT);
        waitForEvents();
        assertEquals(EVENT_NOT_SEEN, 2, low.eventCount);
        assertEquals(EVENT_NOT_SEEN, 2, mid.eventCount);
        assertEquals(EVENT_NOT_SEEN, 1, high.eventCount);
    }


    @Test
    public void slave() {
        print(EOL + "slave()");

        TestListener tl = new TestListener("BOB");
        seq.addPacketListener(tl, DIRECTOR, MID_ALTITUDE);
        seq.addPacketListener(observer, OBSERVER, 0);
        assertCounts(0, 1, 1);

        eventsProcessed = new CountDownLatch(1);
        observer.setLatch(eventsProcessed);

        seq.processAnEvent(SOME_OTHER_EVENT);
        seq.processAnEvent(SOME_EVENT);
        waitForEvents();

        assertEquals(AM_NEQ, 1, tl.eventCount);
        assertEquals(AM_NEQ, DPID, tl.lastDpid);
    }

    @Test
    public void auxChannels() {
        print(EOL + "auxChannels()");

        TestListener tl = new TestListener("Worthington-Smythe");
        tl.willHandle = true;
        seq.addPacketListener(tl, DIRECTOR, MID_ALTITUDE);
        seq.addPacketListener(observer, OBSERVER, 0);
        assertCounts(0, 1, 1);

        auxCheck(SOME_EVENT, 0);
        auxCheck(SOME_EVENT_AUX_1, 1);
        auxCheck(SOME_EVENT_AUX_2, 2);
        assertEquals(AM_NEQ, 3, tl.eventCount);
    }

    private void auxCheck(MessageEvent m, int expAuxId) {
        eventsProcessed = new CountDownLatch(1);
        observer.setLatch(eventsProcessed);
        seq.processAnEvent(m);
        waitForEvents();
        assertEquals(AM_NEQ, DPID, fake.lastDpid);
        assertEquals(AM_NEQ, expAuxId, fake.lastAuxId);
    }
}
