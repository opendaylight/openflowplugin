/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.util.net.IpAddress;

import java.text.DecimalFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * High-level unit test of the stand-alone server/client speed test facilities.
 *
 * @author Thomas Vachuska
 */
public class StandaloneSpeedTest {
    
    private static final int MILLION = 1000000;
    private static final int TIMEOUT = 600;
    
    private static final IpAddress IP = IpAddress.LOOPBACK_IPv4;
    private static final int THREADS = 12;
    private static final int MSG_COUNT = 20 * MILLION;
    
    private static final long MIN_MPS = 2 * MILLION;
    
    @Before
    public void warmUp() throws Exception {
        setUpLogger();
        run(MILLION, 15, 0);
    }
     
    @After
    public void warmDown() {
        restoreLogger();
    }
    
    @Ignore @Test
    public void basic() throws Exception {
        assumeTrue(!ignoreSpeedTests() && !isUnderCoverage());
        run(MSG_COUNT, TIMEOUT, MIN_MPS);
    }
     
    
    private void run(int count, int timeout, double mps) throws Exception {
        DecimalFormat f = new DecimalFormat("#,##0");
        System.out.print(f.format(count * THREADS) + 
                         (mps > 0.0 ? " messages: " : " message warm-up: "));

        StandaloneSpeedServer sss = 
                new StandaloneSpeedServer(IP, THREADS);

        StandaloneSpeedClient ssc = 
                new StandaloneSpeedClient(IP, THREADS, count);

        sss.start();
        ssc.start();

        ssc.await((int) (timeout / perfScale()));
        ssc.report();

        sss.stop();
        sss.report();

        System.out.println(f.format(ssc.messages.throughput()) + " mps");               

        // Make sure client sent everything.
        assertEquals("incorrect client message count sent", 
                   (long) count * THREADS + (3 * THREADS), ssc.messages.total());

        // Make sure speeds were reasonable.
        if (mps > 0.0)
            assertAboveThreshold("insufficient client speed", mps, 
                                 ssc.messages.throughput());
    }

}
