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
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.verify;

import java.util.function.Function;
import org.junit.After;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.MessageIntelligenceAgencyImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 * Test for {@link ShowStatsCommandProvider}.
 */
public class ShowStatsCommandProviderTest extends AbstractKarafTest {
    private final MessageIntelligenceAgency messageIntelligenceAgency = new MessageIntelligenceAgencyImpl();
    private final ShowStatsCommandProvider showStatsCommandProvider = new ShowStatsCommandProvider();

    private static final Function<String, Boolean> CHECK_NO_ACTIVITY_FUNCTION =
        input -> input.endsWith(": no activity detected");

    @Override
    public void doSetUp() {
        showStatsCommandProvider.messageIntelligenceAgency = messageIntelligenceAgency;
    }

    @After
    public void tearDown() {
        // Pattern.DOTALL is set inline via "(?s)" at the beginning
        verify(console).print(matches("(?s).+"));
        messageIntelligenceAgency.resetStatistics();
    }

    /**
     * Test for {@link ShowEventTimesComandProvider#doExecute()} when no stats were touched before.
     */
    @Test
    public void testDoExecute_clean() throws Exception {
        assertTrue(checkNoActivity(messageIntelligenceAgency.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));
        showStatsCommandProvider.execute(cmdSession);
        assertTrue(checkNoActivity(messageIntelligenceAgency.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));
    }

    /**
     * Test for {@link ShowEventTimesComandProvider#doExecute()} when stats were touched before.
     */
    @Test
    public void testDoExecute_dirty() throws Exception {
        assertTrue(checkNoActivity(messageIntelligenceAgency.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));

        messageIntelligenceAgency.spyMessage(OfHeader.class, MessageSpy.StatisticsGroup.FROM_SWITCH);
        assertFalse(checkNoActivity(messageIntelligenceAgency.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));

        showStatsCommandProvider.execute(cmdSession);
        assertFalse(checkNoActivity(messageIntelligenceAgency.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));
    }
}
