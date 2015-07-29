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
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.EventsTimeCounter;

/**
 * Test for {@link ShowEventTimesComandProvider}
 */
public class ShowEventTimesComandProviderTest extends AbstractKarafTest {

    private ShowEventTimesComandProvider showEventTimesComandProvider;
    private static final Function<String, Boolean> CHECK_NO_ACTIVITY_FUNCTION = new Function<String, Boolean>() {
        @Nullable
        @Override
        public Boolean apply(String input) {
            return input.isEmpty();
        }
    };

    @Override

    public void doSetUp() {
        showEventTimesComandProvider = new ShowEventTimesComandProvider();
        EventsTimeCounter.resetAllCounters();
    }

    @After
    public void tearDown() {
        EventsTimeCounter.resetAllCounters();
    }

    /**
     * test for {@link ShowEventTimesComandProvider#doExecute()} when no stats were touched before
     *
     * @throws Exception
     */
    @Test
    public void testDoExecute_clean() throws Exception {
        Assert.assertTrue(checkNoActivity(EventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));
        showEventTimesComandProvider.execute(cmdSession);
        Assert.assertTrue(checkNoActivity(EventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));
        Mockito.verify(console).print("");
    }

    /**
     * test for {@link ShowEventTimesComandProvider#doExecute()} when stats were touched before
     *
     * @throws Exception
     */
    @Test
    public void testDoExecute_dirty() throws Exception {
        final EventIdentifier dummyEvent = new EventIdentifier("junit", "junitDevice");
        Assert.assertTrue(checkNoActivity(EventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));

        EventsTimeCounter.markStart(dummyEvent);
        EventsTimeCounter.markEnd(dummyEvent);
        Assert.assertFalse(checkNoActivity(EventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));

        showEventTimesComandProvider.execute(cmdSession);
        Assert.assertFalse(checkNoActivity(EventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));
        Mockito.verify(console).print(Matchers.contains("junitDevice"));
    }
}