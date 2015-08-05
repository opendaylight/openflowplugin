/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.karaf;

import com.google.common.base.Function;
import javax.annotation.Nullable;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.OpenFlowPluginProviderImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 * Test for {@link ShowStatsCommandProvider}.
 */
public class ShowStatsCommandProviderTest extends AbstractKarafTest {

    private ShowStatsCommandProvider showStatsCommandProvider;
    private MessageIntelligenceAgency messageIntelligenceAgency;

    private static final Function<String, Boolean> CHECK_NO_ACTIVITY_FUNCTION = new Function<String, Boolean>() {
        @Nullable
        @Override
        public Boolean apply(String input) {
            return input.endsWith(": no activity detected");
        }
    };

    @Override
    public void doSetUp() {
        showStatsCommandProvider = new ShowStatsCommandProvider();
        messageIntelligenceAgency = OpenFlowPluginProviderImpl.getMessageIntelligenceAgency();
        messageIntelligenceAgency.resetStatistics();
    }

    @After
    public void tearDown() throws Exception {
        // Pattern.DOTALL is set inline via "(?s)" at the beginning
        Mockito.verify(console).print(Matchers.matches("(?s).+"));
        messageIntelligenceAgency.resetStatistics();
    }

    /**
     * test for {@link ShowEventTimesComandProvider#doExecute()} when no stats were touched before
     *
     * @throws Exception
     */
    @Test
    public void testDoExecute_clean() throws Exception {
        Assert.assertTrue(checkNoActivity(messageIntelligenceAgency.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));
        showStatsCommandProvider.execute(cmdSession);
        Assert.assertTrue(checkNoActivity(messageIntelligenceAgency.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));
    }

    /**
     * test for {@link ShowEventTimesComandProvider#doExecute()} when stats were touched before
     *
     * @throws Exception
     */
    @Test
    public void testDoExecute_dirty() throws Exception {
        Assert.assertTrue(checkNoActivity(messageIntelligenceAgency.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));

        messageIntelligenceAgency.spyMessage(OfHeader.class, MessageSpy.STATISTIC_GROUP.FROM_SWITCH);
        Assert.assertFalse(checkNoActivity(messageIntelligenceAgency.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));

        showStatsCommandProvider.execute(cmdSession);
        Assert.assertFalse(checkNoActivity(messageIntelligenceAgency.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));
    }
}