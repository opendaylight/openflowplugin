/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.karaf;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.SessionStatistics;

/**
 * Test for {@link ShowSessionStatsCommand}.
 */
class ShowSessionStatsCommandTest extends AbstractCommandTest {
    private static final Function<String, Boolean> CHECK_NO_ACTIVITY_FUNCTION = String::isEmpty;

    @InjectMocks
    private ShowSessionStatsCommand showSessionStatsCommand;

    @Override
    protected void doBeforeEach() {
        SessionStatistics.resetAllCounters();
        assertNoActivity(SessionStatistics.provideStatistics(), CHECK_NO_ACTIVITY_FUNCTION);
    }

    /**
     * Test for {@link ShowEventTimesCommand#execute()} when no stats were touched before.
     */
    @Test
    void showNoActivity() {
        showSessionStatsCommand.execute();
        verify(console, never()).println(anyString());
        assertNoActivity(SessionStatistics.provideStatistics(), CHECK_NO_ACTIVITY_FUNCTION);
    }

    /**
     * Test for {@link ShowEventTimesCommand#execute()} when stats were touched before.
     */
    @Test
    void showHavingActivity() {
        final var dummySessionId = "junitSessionId";
        SessionStatistics.countEvent(dummySessionId, SessionStatistics.ConnectionStatus.CONNECTION_CREATED);
        assertHasActivity(SessionStatistics.provideStatistics(), CHECK_NO_ACTIVITY_FUNCTION);

        showSessionStatsCommand.execute();
        verify(console, atLeastOnce()).println(contains(dummySessionId));
        assertHasActivity(SessionStatistics.provideStatistics(), CHECK_NO_ACTIVITY_FUNCTION);
    }
}