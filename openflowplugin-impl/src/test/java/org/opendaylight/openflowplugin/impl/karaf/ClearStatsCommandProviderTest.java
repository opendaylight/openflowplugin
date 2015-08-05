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
 * Test for {@link ClearStatsCommandProvider}
 */
public class ClearStatsCommandProviderTest extends AbstractKarafTest {

    private ClearStatsCommandProvider clearStatsCommandProvider;
    private static final Function<String, Boolean> CHECK_NO_ACTIVITY_FUNCTION = new Function<String, Boolean>() {
        @Nullable
        @Override
        public Boolean apply(String input) {
            return input.endsWith(": no activity detected");
        }
    };
    private MessageIntelligenceAgency mi5;

    public void doSetUp() {
        clearStatsCommandProvider = new ClearStatsCommandProvider();
        mi5 = OpenFlowPluginProviderImpl.getMessageIntelligenceAgency();
        mi5.resetStatistics();
        Mockito.when(cmdSession.getConsole()).thenReturn(console);
    }

    @After
    public void tearDown() {
        Mockito.verify(console).print(Matchers.anyString());
        mi5.resetStatistics();
    }

    /**
     * test for {@link ClearStatsCommandProvider#doExecute()} when no stats were touched before
     *
     * @throws Exception
     */
    @Test
    public void testDoExecute_clean() throws Exception {
        Assert.assertTrue(checkNoActivity(mi5.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));
        clearStatsCommandProvider.execute(cmdSession);

        Assert.assertTrue(checkNoActivity(mi5.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));
    }

    /**
     * test for {@link ClearStatsCommandProvider#doExecute()} when stats were touched before
     *
     * @throws Exception
     */
    @Test
    public void testDoExecute_dirty() throws Exception {
        final MessageIntelligenceAgency mi5 = OpenFlowPluginProviderImpl.getMessageIntelligenceAgency();
        Assert.assertTrue(checkNoActivity(mi5.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));
        mi5.spyMessage(OfHeader.class, MessageSpy.STATISTIC_GROUP.FROM_SWITCH);
        Assert.assertFalse(checkNoActivity(mi5.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));

        clearStatsCommandProvider.execute(cmdSession);
        Assert.assertTrue(checkNoActivity(mi5.provideIntelligence(), CHECK_NO_ACTIVITY_FUNCTION));
    }
}