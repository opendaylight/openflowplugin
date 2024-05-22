/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.karaf;

import static org.mockito.ArgumentMatchers.anyString;

import java.util.function.Function;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.DefaultEventsTimeCounter;

/**
 * Test for {@link  ResetEventTimesComandProvider}.
 */
public class ResetEventTimesComandProviderTest extends AbstractKarafTest {

    private ResetEventTimesComandProvider resetEventTimesComandProvider;
    private static final Function<String, Boolean> CHECK_NO_ACTIVITY_FUNCTION = String::isEmpty;

    @Override
    public void doSetUp() {
        resetEventTimesComandProvider = new ResetEventTimesComandProvider();
        DefaultEventsTimeCounter.resetAllCounters();
    }

    @After
    public void tearDown() {
        Mockito.verify(console).print(anyString());
        DefaultEventsTimeCounter.resetAllCounters();
    }

    /**
     * Test for {@link ResetEventTimesComandProvider#doExecute()} when no stats were touched before.
     */
    @Test
    public void testDoExecute_clean() throws Exception {
        Assert.assertTrue(checkNoActivity(DefaultEventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));
        resetEventTimesComandProvider.execute(cmdSession);
        Assert.assertTrue(checkNoActivity(DefaultEventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));
    }

    /**
     * Test for {@link ResetEventTimesComandProvider#doExecute()} when stats were touched before.
     */
    @Test
    public void testDoExecute_dirty() throws Exception {
        final EventIdentifier dummyEvent = new EventIdentifier("junit", "junitDevice");
        Assert.assertTrue(checkNoActivity(DefaultEventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));

        DefaultEventsTimeCounter.markStart(dummyEvent);
        DefaultEventsTimeCounter.markEnd(dummyEvent);
        Assert.assertFalse(checkNoActivity(DefaultEventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));

        resetEventTimesComandProvider.execute(cmdSession);
        Assert.assertTrue(checkNoActivity(DefaultEventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));
    }
}