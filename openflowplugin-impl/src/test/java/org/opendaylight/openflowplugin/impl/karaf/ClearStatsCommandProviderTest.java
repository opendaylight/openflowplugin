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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Function;
import org.junit.After;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.MessageIntelligenceAgencyImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 * Test for {@link ClearStatsCommandProvider}.
 */
public class ClearStatsCommandProviderTest extends AbstractKarafTest {
    private static final Function<String, Boolean> CHECK_NO_ACTIVITY_FUNCTION =
        input -> input.endsWith(": no activity detected");

    private final MessageIntelligenceAgency mi5 = new MessageIntelligenceAgencyImpl();
    private final ClearStatsCommandProvider clearStatsCommandProvider = new ClearStatsCommandProvider();

    @Override
    public void doSetUp() {
        clearStatsCommandProvider.messageIntelligenceAgency = mi5;
        when(cmdSession.getConsole()).thenReturn(console);
    }

    @After
    public void tearDown() {
        verify(console).print(anyString());
        mi5.resetStatistics();
    }

    /**
     * Test for {@link ClearStatsCommandProvider#doExecute()} when no stats were touched before.
     */
    @Test
    public void testDoExecute_clean() throws Exception {
        assertTrue(checkNoActivity(mi5.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));
        clearStatsCommandProvider.execute(cmdSession);

        assertTrue(checkNoActivity(mi5.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));
    }

    /**
     * Test for {@link ClearStatsCommandProvider#doExecute()} when stats were touched before.
     */
    @Test
    public void testDoExecute_dirty() throws Exception {
        assertTrue(checkNoActivity(mi5.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));
        mi5.spyMessage(OfHeader.class, MessageSpy.StatisticsGroup.FROM_SWITCH);
        assertFalse(checkNoActivity(mi5.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));

        clearStatsCommandProvider.execute(cmdSession);
        assertTrue(checkNoActivity(mi5.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));
    }
}
