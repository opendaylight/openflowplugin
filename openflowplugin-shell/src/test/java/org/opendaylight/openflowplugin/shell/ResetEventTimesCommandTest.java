/*
 * Copyright (c) 2024 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.shell;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.EventsTimeCounter;

/**
 * Test for {@link  ResetEventTimesComand}.
 */
class ResetEventTimesCommandTest extends AbstractKarafTest {

    @InjectMocks
    private ResetEventTimesComand resetEventTimesCommand;
    private static final Function<String, Boolean> CHECK_NO_ACTIVITY_FUNCTION = String::isEmpty;

    @Override
    protected void doBeforeEach() {
        EventsTimeCounter.resetAllCounters();
        assertNoActivity(EventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION);
    }

    /**
     * Test for {@link ResetEventTimesComand#execute()} when no stats were touched before.
     */
    @Test
    void resetNoActivity() {
        resetEventTimesCommand.execute();
        verify(console).println(anyString());
        assertNoActivity(EventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION);
    }

    /**
     * Test for {@link ResetEventTimesComand#execute()} when stats were touched before.
     */
    @Test
    void resetHavingActivity() {
        final var dummyEvent = new EventIdentifier("junit", "junitDevice");
        EventsTimeCounter.markStart(dummyEvent);
        EventsTimeCounter.markEnd(dummyEvent);
        assertHasActivity(EventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION);

        resetEventTimesCommand.execute();
        verify(console).println(anyString());
        assertNoActivity(EventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION);
    }
}