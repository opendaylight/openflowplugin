/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.event;

import org.junit.Test;

import java.util.List;
import java.util.Timer;

import static org.opendaylight.util.junit.TestTools.delay;
import static org.junit.Assert.assertEquals;

/**
 * Test of the event batcher.
 *
 * @author Thomas Vachuska
 */
public class AbstractEventBatcherTest {

    private static final int MAX_EVENTS = 5;
    private static final int MAX_MS = 200;
    private static final int IDLE_MS = 50;

    // FIXME: use a synthetic timer rather than a real one to avoid time-stretch induced failures, which may happen on slower machines
    private final Timer timer = new Timer();
    private final TestBatcher tb = new TestBatcher();

    private class TestBatcher extends AbstractEventBatcher<String> {

        String sequence;

        public TestBatcher() {
            super(MAX_EVENTS, MAX_MS, IDLE_MS, timer);
        }

        @Override
        protected void processBatch(List<String> batch) {
            sequence = "";
            for (String s : batch)
                sequence = sequence + s;
        }
    }

    @Test
    public void basics() {
        assertEquals("incorrect maxEvents", MAX_EVENTS, tb.maxEvents());
        assertEquals("incorrect maxMs", MAX_MS, tb.maxMs());
        assertEquals("incorrect idleMs", IDLE_MS, tb.idleMs());
        assertEquals("incorrect timer", timer, tb.timer());
    }

    @Test
    public void maxBatch() {
        tb.submit("1");
        tb.submit("2");
        delay(IDLE_MS / 4);
        tb.submit("3");
        tb.submit("4");
        delay(IDLE_MS / 4);
        tb.submit("5");

        delay(IDLE_MS / 4);
        tb.submit("6");
        assertEquals("incorrect batch", "12345", tb.sequence);
    }

    @Test
    public void idleBatch() {
        tb.submit("1");
        tb.submit("2");

        delay(IDLE_MS * 2);
        tb.submit("3");
        assertEquals("incorrect batch", "12", tb.sequence);
    }

    @Test
    public void maxTime() {
        tb.submit("1");
        delay(IDLE_MS / 2);
        tb.submit("2");
        delay(IDLE_MS / 2);
        tb.submit("3");
        delay(IDLE_MS / 2);
        tb.submit("4");
        delay(IDLE_MS / 2);
        tb.submit("5");
        delay(IDLE_MS / 2);
        tb.submit("6");
        delay(IDLE_MS / 2);
        tb.submit("7");
        delay(IDLE_MS / 2);
        tb.submit("8");
        delay(IDLE_MS / 2);
        tb.submit("9");

        assertEquals("incorrect batch", "12345", tb.sequence);

        delay(IDLE_MS * 2);
        assertEquals("incorrect batch", "6789", tb.sequence);
    }

    @Test
    public void repeat() {
        tb.submit("1");
        delay(IDLE_MS / 2);
        tb.submit("2");
        delay(IDLE_MS * 2);
        assertEquals("incorrect batch", "12", tb.sequence);

        tb.submit("3");
        delay(IDLE_MS / 2);
        tb.submit("4");
        delay(IDLE_MS * 2);
        assertEquals("incorrect batch", "34", tb.sequence);

        tb.submit("5");
        delay(IDLE_MS / 2);
        tb.submit("6");
        delay(IDLE_MS * 2);
        assertEquals("incorrect batch", "56", tb.sequence);
    }

}
