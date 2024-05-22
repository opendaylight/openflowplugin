/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.karaf;

import static org.mockito.ArgumentMatchers.contains;

import java.util.function.Function;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.DefaultEventsTimeCounter;

/**
 * Test for {@link ShowEventTimesComandProvider}.
 */
public class ShowEventTimesComandProviderTest extends AbstractKarafTest {

    private ShowEventTimesComandProvider showEventTimesComandProvider;
    private static final Function<String, Boolean> CHECK_NO_ACTIVITY_FUNCTION = String::isEmpty;

    @Override

    public void doSetUp() {
        showEventTimesComandProvider = new ShowEventTimesComandProvider();
        DefaultEventsTimeCounter.resetAllCounters();
    }

    @After
    public void tearDown() {
        DefaultEventsTimeCounter.resetAllCounters();
    }

    /**
     * Test for {@link ShowEventTimesComandProvider#doExecute()} when no stats were touched before.
     */
    @Test
    public void testDoExecute_clean() throws Exception {
        Assert.assertTrue(checkNoActivity(DefaultEventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));
        showEventTimesComandProvider.execute(cmdSession);
        Assert.assertTrue(checkNoActivity(DefaultEventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));
        Mockito.verify(console).print("");
    }

    /**
     * Test for {@link ShowEventTimesComandProvider#doExecute()} when stats were touched before.
     */
    @Test
    public void testDoExecute_dirty() throws Exception {
        final EventIdentifier dummyEvent = new EventIdentifier("junit", "junitDevice");
        Assert.assertTrue(checkNoActivity(DefaultEventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));

        DefaultEventsTimeCounter.markStart(dummyEvent);
        DefaultEventsTimeCounter.markEnd(dummyEvent);
        Assert.assertFalse(checkNoActivity(DefaultEventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));

        showEventTimesComandProvider.execute(cmdSession);
        Assert.assertFalse(checkNoActivity(DefaultEventsTimeCounter.provideTimes(), CHECK_NO_ACTIVITY_FUNCTION));
        Mockito.verify(console).print(contains("junitDevice"));
    }
}