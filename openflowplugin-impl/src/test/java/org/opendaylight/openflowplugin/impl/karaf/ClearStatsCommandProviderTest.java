/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.karaf;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.MessageIntelligenceAgencyImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 * Test for {@link ClearStatsCommandProvider}.
 */
class ClearStatsCommandProviderTest extends AbstractKarafTest {
    private static final Function<String, Boolean> CHECK_NO_ACTIVITY_FUNCTION =
        input -> input.endsWith(": no activity detected");

    private final MessageIntelligenceAgency messageIntelligenceAgency = new MessageIntelligenceAgencyImpl();

    @InjectMocks
    private ClearStatsCommandProvider clearStatsCommand;

    @Override
    protected void doBeforeEach() {
        clearStatsCommand.messageIntelligenceAgency = messageIntelligenceAgency;
        messageIntelligenceAgency.resetStatistics();
        assertNoActivity(messageIntelligenceAgency.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION);
    }

    /**
     * Test for {@link ClearStatsCommandProvider#execute()} when no stats were touched before.
     */
    @Test
    void clearNoActivity() {
        clearStatsCommand.execute();
        verify(console).println(anyString());
        assertNoActivity(messageIntelligenceAgency.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION);
    }

    /**
     * Test for {@link ClearStatsCommandProvider#execute()} when stats were touched before.
     */
    @Test
    void clearHavingActivity() {
        messageIntelligenceAgency.spyMessage(OfHeader.class, MessageSpy.StatisticsGroup.FROM_SWITCH);
        assertHasActivity(messageIntelligenceAgency.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION);
        clearStatsCommand.execute();
        verify(console).println(anyString());
        assertNoActivity(messageIntelligenceAgency.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION);
    }
}
