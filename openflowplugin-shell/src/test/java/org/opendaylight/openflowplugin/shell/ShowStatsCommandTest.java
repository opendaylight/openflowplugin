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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.OpenFlowPluginProviderImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 * Test for {@link ShowStatsCommand}.
 */
class ShowStatsCommandTest extends AbstractKarafTest {

    @InjectMocks
    private ShowStatsCommand showStatsCommand;
    private MessageIntelligenceAgency messageIntelligenceAgency;

    private static final Function<String, Boolean> CHECK_NO_ACTIVITY_FUNCTION =
        input -> input.endsWith(": no activity detected");

    @Override
    protected void doBeforeEach() {
        messageIntelligenceAgency = OpenFlowPluginProviderImpl.getMessageIntelligenceAgency();
        messageIntelligenceAgency.resetStatistics();
        assertNoActivity(messageIntelligenceAgency.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION);
    }

    /**
     * Test for {@link ShowEventTimesCommand#execute()} when no stats were touched before.
     */
    @Test
    void showNoActivity() {
        showStatsCommand.execute();
        verify(console, never()).println();
        assertNoActivity(messageIntelligenceAgency.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION);
    }

    /**
     * Test for {@link ShowEventTimesCommand#execute()} when stats were touched before.
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
