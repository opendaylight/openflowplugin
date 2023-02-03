/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.karaf;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.EventsTimeCounter;

/**
 * Test for {@link  ResetEventTimesComandProvider}.
 */
public class ResetEventTimesComandProviderTest extends AbstractKarafTest {
    private ResetEventTimesComandProvider resetEventTimesComandProvider;

    @Override
    public void doSetUp() {
        resetEventTimesComandProvider = new ResetEventTimesComandProvider();
        EventsTimeCounter.resetAllCounters();
    }

    @After
    public void tearDown() {
        verify(console).println(anyString());
        EventsTimeCounter.resetAllCounters();
    }

    /**
     * Test for {@link ResetEventTimesComandProvider#doExecute()} when no stats were touched before.
     */
    @Test
    public void testDoExecute_clean() throws Exception {
        assertTrue(checkNoActivity(EventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));
        resetEventTimesComandProvider.execute(console);
        assertTrue(checkNoActivity(EventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));
    }

    /**
     * Test for {@link ResetEventTimesComandProvider#doExecute()} when stats were touched before.
     */
    @Test
    public void testDoExecute_dirty() throws Exception {
        final EventIdentifier dummyEvent = new EventIdentifier("junit", "junitDevice");
        assertTrue(checkNoActivity(EventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));

        EventsTimeCounter.markStart(dummyEvent);
        EventsTimeCounter.markEnd(dummyEvent);
        assertFalse(checkNoActivity(EventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));

        resetEventTimesComandProvider.execute(console);
        assertTrue(checkNoActivity(EventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));
    }
}