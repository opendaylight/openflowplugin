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
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.OpenFlowPluginProviderImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 * Test for {@link ClearStatsCommand}.
 */
class ClearStatsCommandTest extends AbstractKarafTest {

    private static final Function<String, Boolean> CHECK_NO_ACTIVITY_FUNCTION =
        input -> input.endsWith(": no activity detected");

    @InjectMocks
    private ClearStatsCommand clearStatsCommand;
    private MessageIntelligenceAgency mi5;

    @Override
    protected void doBeforeEach() {
        mi5 = OpenFlowPluginProviderImpl.getMessageIntelligenceAgency();
        mi5.resetStatistics();
        assertNoActivity(mi5.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION);
    }

    /**
     * Test for {@link ClearStatsCommand#execute()} when no stats were touched before.
     */
    @Test
    void clearNoActivity() {
        clearStatsCommand.execute();
        verify(console).println(anyString());
        assertNoActivity(mi5.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION);
    }

    /**
     * Test for {@link ClearStatsCommand#execute()} when stats were touched before.
     */
    @Test
    void clearHavingActivity() {
        mi5.spyMessage(OfHeader.class, MessageSpy.StatisticsGroup.FROM_SWITCH);
        assertHasActivity(mi5.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION);
        clearStatsCommand.execute();
        verify(console).println(anyString());
        assertNoActivity(mi5.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION);
    }
}
