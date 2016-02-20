/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link TimeCounter}.
 */
public class TimeCounterTest {

    private static final Logger LOG = LoggerFactory.getLogger(TimeCounterTest.class);
    private TimeCounter timeCounter;

    @Before
    public void setUp() throws Exception {
        timeCounter = new TimeCounter();
    }

    //TODO: Recheck this 'annoying' testl
    @Test
    public void testGetAverageTimeBetweenMarks() throws Exception {
        Assert.assertEquals(0, timeCounter.getAverageTimeBetweenMarks());
        timeCounter.markStart();
        Assert.assertEquals(0, timeCounter.getAverageTimeBetweenMarks());

        zzz(2L);
        timeCounter.addTimeMark();
        Assert.assertEquals(2, timeCounter.getAverageTimeBetweenMarks());

        zzz(2L);
        timeCounter.addTimeMark();
        Assert.assertEquals(2, timeCounter.getAverageTimeBetweenMarks());

        zzz(5L);
        timeCounter.addTimeMark();
        Assert.assertEquals(3, timeCounter.getAverageTimeBetweenMarks());
    }

    private void zzz(long length) {
        try {
            Thread.sleep(length);
        } catch (InterruptedException e) {
            LOG.error("processing sleep interrupted", e);
        }
    }
}