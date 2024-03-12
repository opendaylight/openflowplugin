/*
 * Copyright (c) 2024 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.shell;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.SessionStatistics;

/**
 * Test for {@link ResetSessionStatsCommand}.
 */
class ResetSessionStatsCommandTest extends AbstractKarafTest {

    @InjectMocks
    private ResetSessionStatsCommand resetSessionStatsCommand;
    private static final Function<String, Boolean> CHECK_NO_ACTIVITY_FUNCTION = String::isEmpty;

    @Override
    protected void doBeforeEach() {
        SessionStatistics.resetAllCounters();
        assertNoActivity(SessionStatistics.provideStatistics(), CHECK_NO_ACTIVITY_FUNCTION);
    }

    /**
     * Test for {@link ResetSessionStatsCommand#execute()} when no stats were touched before.
     */
    @Test
    void resetNoActivity() {
        resetSessionStatsCommand.execute();
        verify(console, atLeastOnce()).println(anyString());
        assertNoActivity(SessionStatistics.provideStatistics(), CHECK_NO_ACTIVITY_FUNCTION);
    }

    /**
     * Test for {@link ResetSessionStatsCommand#execute()} when stats were touched before.
     */
    @Test
    void resetHavingActivity() {
        final String dummySessionId = "junitSessionId";
        SessionStatistics.countEvent(dummySessionId, SessionStatistics.ConnectionStatus.CONNECTION_CREATED);
        assertHasActivity(SessionStatistics.provideStatistics(), CHECK_NO_ACTIVITY_FUNCTION);

        resetSessionStatsCommand.execute();
        verify(console, atLeastOnce()).println(anyString());
        assertNoActivity(SessionStatistics.provideStatistics(), CHECK_NO_ACTIVITY_FUNCTION);
    }
}