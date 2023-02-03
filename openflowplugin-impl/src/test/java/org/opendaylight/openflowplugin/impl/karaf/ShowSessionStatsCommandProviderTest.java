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
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Test;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.SessionStatistics;

/**
 * Test for {@link ShowSessionStatsCommandProvider}.
 */
public class ShowSessionStatsCommandProviderTest extends AbstractKarafTest {
    private ShowSessionStatsCommandProvider showSessionStatsCommandProvider;

    @Override
    public void doSetUp() {
        showSessionStatsCommandProvider = new ShowSessionStatsCommandProvider();
        SessionStatistics.resetAllCounters();
    }

    @After
    public void tearDown() {
        SessionStatistics.resetAllCounters();
    }

    /**
     * Test for {@link ShowEventTimesComandProvider#doExecute()} when no stats were touched before.
     */
    @Test
    public void testDoExecute_clean() throws Exception {
        assertTrue(checkNoActivity(SessionStatistics.provideStatistics(), CHECK_NO_ACTIVITY_FUNCTION));
        showSessionStatsCommandProvider.execute(console);
        assertTrue(checkNoActivity(SessionStatistics.provideStatistics(), CHECK_NO_ACTIVITY_FUNCTION));
        verify(console).print("");
    }

    /**
     * Test for {@link ShowEventTimesComandProvider#doExecute()} when stats were touched before.
     */
    @Test
    public void testDoExecute_dirty() throws Exception {
        final String dummySessionId = "junitSessionId";
        assertTrue(checkNoActivity(SessionStatistics.provideStatistics(), CHECK_NO_ACTIVITY_FUNCTION));

        SessionStatistics.countEvent(dummySessionId, SessionStatistics.ConnectionStatus.CONNECTION_CREATED);
        assertFalse(checkNoActivity(SessionStatistics.provideStatistics(), CHECK_NO_ACTIVITY_FUNCTION));

        showSessionStatsCommandProvider.execute(console);
        assertFalse(checkNoActivity(SessionStatistics.provideStatistics(), CHECK_NO_ACTIVITY_FUNCTION));
        verify(console).print(contains(dummySessionId));
    }
}