/*
 * Copyright (c) 2024 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.shell;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.EventsTimeCounter;

/**
 * Test for {@link ShowEventTimesCommand}.
 */
class ShowEventTimesCommandTest extends AbstractKarafTest {

    private static final Function<String, Boolean> CHECK_NO_ACTIVITY_FUNCTION = String::isEmpty;

    @InjectMocks
    private ShowEventTimesCommand showEventTimesCommand;

    @Override
    protected void doBeforeEach() {
        EventsTimeCounter.resetAllCounters();
        assertNoActivity(EventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION);
    }

    /**
     * Test for {@link ShowEventTimesCommand#execute()} when no stats were touched before.
     */
    @Test
    void showNoActivity() {
        showEventTimesCommand.execute();
        verify(console, never()).println(anyString());
        assertNoActivity(EventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION);
    }

    /**
     * Test for {@link ShowEventTimesCommand#execute()} when stats were touched before.
     */
    @Test
    void showHavingActivity() {
        final var dummyEvent = new EventIdentifier("junit", "junitDevice");
        EventsTimeCounter.markStart(dummyEvent);
        EventsTimeCounter.markEnd(dummyEvent);
        assertHasActivity(EventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION);

        showEventTimesCommand.execute();
        verify(console).println(contains("junitDevice"));
    }
}