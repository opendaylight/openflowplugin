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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.MessageIntelligenceAgencyImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 * Test for {@link ShowStatsCommandProvider}.
 */
class ShowStatsCommandProviderTest extends AbstractKarafTest {
    private final MessageIntelligenceAgency messageIntelligenceAgency = new MessageIntelligenceAgencyImpl();

    @InjectMocks
    private ShowStatsCommandProvider showStatsCommand;

    private static final Function<String, Boolean> CHECK_NO_ACTIVITY_FUNCTION =
        input -> input.endsWith(": no activity detected");

    @Override
    protected void doBeforeEach() {
        showStatsCommand.messageIntelligenceAgency = messageIntelligenceAgency;
        messageIntelligenceAgency.resetStatistics();
        assertNoActivity(messageIntelligenceAgency.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION);
    }

    /**
     * Test for {@link ShowEventTimesComandProvider#execute()} when no stats were touched before.
     */
    @Test
    void showNoActivity() {
        showStatsCommand.execute();
        verify(console, never()).println();
        assertNoActivity(messageIntelligenceAgency.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION);
    }

    /**
     * Test for {@link ShowEventTimesComandProvider#execute()} when stats were touched before.
     */
    @Test
    void showHavingActivity() {
        messageIntelligenceAgency.spyMessage(OfHeader.class, MessageSpy.StatisticsGroup.FROM_SWITCH);
        assertHasActivity(messageIntelligenceAgency.provideIntelligence(),CHECK_NO_ACTIVITY_FUNCTION);

        showStatsCommand.execute();
        verify(console, atLeastOnce()).println(anyString());
        assertHasActivity(messageIntelligenceAgency.provideIntelligence(),CHECK_NO_ACTIVITY_FUNCTION);
    }
}
