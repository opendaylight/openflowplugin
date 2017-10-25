/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.statistics;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General tests for StatisticsCounters class
 * @author madamjak
 *
 */
public class StatisticsCountersTest {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsCountersTest.class);
    private StatisticsCounters statCounters;

    /**
     * Initialize StatisticsCounters before each test, reset counters
     */
    @Before
    public void initTest(){
        statCounters = StatisticsCounters.getInstance();
        statCounters.startCounting(false, 0);
    }

    /**
     * Stop counting after each test
     */
    @After
    public void tierDown(){
        statCounters.stopCounting();
    }

    /**
     * Basic test of increment and reset counters
     */
    @Test
    public void testCounterAll() {
        int testCount = 4;
        for(CounterEventTypes cet : CounterEventTypes.values()){
            if(statCounters.isCounterEnabled(cet)){
                incrementCounter(cet,testCount);
                Assert.assertEquals("Wrong - bad counter value " + cet, testCount, statCounters.getCounter(cet).getCounterValue());
            } else {
                Assert.assertNull("Wrong - not enabled counter give not null value", statCounters.getCounter(cet));
            }

        }
        statCounters.resetCounters();
        for(CounterEventTypes cet : CounterEventTypes.values()){
            if(statCounters.isCounterEnabled(cet)){
                Assert.assertEquals("Wrong - bad counter value after reset " + cet, 0, statCounters.getCounter(cet).getCounterValue());
            }
        }
    }

    /**
     * Test to store current and last read value of counter (at least one counter has to be enabled)
     */
    @Test
    public void testCounterLastRead() {
        int testCount = 4;
        CounterEventTypes firstEnabledCET = null;
        for(CounterEventTypes  cet : CounterEventTypes.values()){
            if(statCounters.isCounterEnabled(cet)){
                firstEnabledCET = cet;
                break;
            }
        }
        if(firstEnabledCET == null){
            Assert.fail("No counter is enabled");
        }
        incrementCounter(firstEnabledCET,testCount);
        LOG.debug("Waiting to process event queue");
        Assert.assertEquals("Wrong - bad last read value.", 0,statCounters.getCounter(firstEnabledCET).getCounterLastReadValue());
        Assert.assertEquals("Wrong - bad value", testCount,statCounters.getCounter(firstEnabledCET).getCounterValue(false));
        Assert.assertEquals("Wrong - bad last read value.", 0,statCounters.getCounter(firstEnabledCET).getCounterLastReadValue());
        Assert.assertEquals("Wrong - bad last read value.", testCount,statCounters.getCounter(firstEnabledCET).getCounterValue());
        Assert.assertEquals("Wrong - bad last read value.", testCount,statCounters.getCounter(firstEnabledCET).getCounterLastReadValue());
        incrementCounter(firstEnabledCET,testCount);
        Assert.assertEquals("Wrong - bad last read value.", testCount,statCounters.getCounter(firstEnabledCET).getCounterLastReadValue());
        Assert.assertEquals("Wrong - bad last read value.", 2*testCount,statCounters.getCounter(firstEnabledCET).getCounterValue());
    }

    /**
     * Test start and stop log reporter
     */
    @Test
    public void testStartStopLogReporter(){
        int testDelay = 10000;
        statCounters.startLogReport(testDelay);
        Assert.assertTrue("Wrong - logRepoter is not running", statCounters.isRunLogReport());
        Assert.assertEquals("Wrong - bad logReportPeriod", testDelay, statCounters.getLogReportPeriod());
        statCounters.stopLogReport();
        Assert.assertFalse("Wrong - logRepoter is running", statCounters.isRunLogReport());
        statCounters.startLogReport(StatisticsCounters.MINIMAL_LOG_REPORT_PERIOD / 2);
        Assert.assertTrue("Wrong - logRepoter is not running", statCounters.isRunLogReport());
        Assert.assertEquals("Wrong - bad logReportPeriod", StatisticsCounters.MINIMAL_LOG_REPORT_PERIOD, statCounters.getLogReportPeriod());
        statCounters.stopCounting();
        Assert.assertFalse("Wrong - logRepoter is running", statCounters.isRunLogReport());
    }

    /**
     * Test start log report with bad logReportDealy
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLogReportBadPeriod(){
        statCounters.startLogReport(0);
    }

    /**
     * Test to get counter with null key
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetCounterbyNull(){
        statCounters.getCounter(null);
    }

    private void incrementCounter(CounterEventTypes cet, int count){
        if(!statCounters.isCounterEnabled(cet)){
            return;
        }
        for(int i = 0; i< count; i++){
            statCounters.incrementCounter(cet);
        }
    }
}
