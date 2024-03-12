/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.karaf;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.SessionStatistics;

/**
 * Test for {@link ResetSessionStatsComandProvider}.
 */
class ResetSessionStatsComandProviderTest extends AbstractKarafTest {
    @InjectMocks
    private ResetSessionStatsComandProvider resetSessionStatsCommand;
    private static final Function<String, Boolean> CHECK_NO_ACTIVITY_FUNCTION = String::isEmpty;

    @Override
    protected void doBeforeEach() {
        SessionStatistics.resetAllCounters();
        assertNoActivity(SessionStatistics.provideStatistics(), CHECK_NO_ACTIVITY_FUNCTION);
    }

    /**
     * Test for {@link ResetSessionStatsComandProvider#execute()} when no stats were touched before.
     */
    @Test
    void resetNoActivity() {
        resetSessionStatsCommand.execute();
        verify(console, atLeastOnce()).println(anyString());
        assertNoActivity(SessionStatistics.provideStatistics(), CHECK_NO_ACTIVITY_FUNCTION);
    }

    /**
     * Test for {@link ResetSessionStatsComandProvider#execute()} when stats were touched before.
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