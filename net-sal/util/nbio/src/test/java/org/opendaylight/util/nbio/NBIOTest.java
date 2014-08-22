/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

import org.junit.Ignore;
import org.opendaylight.util.junit.TestTools;
import org.opendaylight.util.junit.PerformanceTests;
import org.opendaylight.util.Task;
import org.opendaylight.util.net.IpAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.text.DecimalFormat;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

/**
 * High-level unit test of the NBIO facilities.
 *
 * @author Thomas Vachuska
 */
@Category(PerformanceTests.class)
public class NBIOTest {
    
    private static final int MILLION = 1000000;
    private static final int TIMEOUT = 60;
    
    private static final IpAddress IP = IpAddress.LOOPBACK_IPv4;
    private static final int THREADS = 6;
    private static final int MSG_COUNT = 20 * MILLION;
    private static final int MSG_SIZE = 128;
    
    private static final long MIN_MPS = 10 * MILLION;
    
    @Before
    public void warmUp() throws Exception {
        try {
            setUpLogger();
            run(MILLION, MSG_SIZE, 15, 0);
        } catch (Throwable e) {
            System.err.println("Failed warmup but moving on.");
            e.printStackTrace();
        }
    }
     
    @After
    public void warmDown() throws Exception {
        restoreLogger();
    }
     
    @Ignore
    @Test
    public void basic() throws Exception {
        assumeTrue(!ignoreSpeedTests() && !isUnderCoverage());
        run(MSG_COUNT, MSG_SIZE, TIMEOUT, MIN_MPS);
    }
     
    
    private void run(int count, int size, int timeout, double mps) throws Exception {
        DecimalFormat f = new DecimalFormat("#,##0");
        System.out.print(f.format(count * THREADS) + 
                         (mps > 0.0 ? " messages: " : " message warm-up: "));

        // Setup the test on a random port to avoid intermittent test failures
        // due to the port being already bound.
        int port = StandaloneSpeedServer.PORT + TestTools.random().nextInt(100);

        StandaloneSpeedServer sss = 
                new StandaloneSpeedServer(IP, THREADS, size, port);
        
        StandaloneSpeedClient ssc = 
                new StandaloneSpeedClient(IP, THREADS, count, size, port);
        
        sss.start();
        ssc.start();
        Task.delay(250);       // give the server and client a chance to go

        ssc.await((int) (timeout / perfScale()));
        ssc.report();
        
        Task.delay(1000);
        sss.stop();
        sss.report();
        
        // Note that the client and server will have potentially significantly
        // differing rates. This is due to the wide variance in how tightly 
        // the throughput tracking starts & stops relative to to the short 
        // test duration.
        System.out.println(f.format(ssc.messages.throughput()) + " mps");
        
        // Make sure client sent everything.
        assertEquals("incorrect client message count sent", 
                     (long) count * THREADS, ssc.messages.total());
        assertEquals("incorrect client bytes count sent", 
                     (long) size * count * THREADS, ssc.bytes.total());

        // Make sure server received everything.
        assertEquals("incorrect server message count received", 
                     (long) count * THREADS, sss.messages.total());
        assertEquals("incorrect server bytes count received", 
                     (long) size * count * THREADS, sss.bytes.total());
        
        // Make sure speeds were reasonable.
        if (mps > 0.0) {
            assertAboveThreshold("insufficient client speed", mps, 
                                 ssc.messages.throughput());
            assertAboveThreshold("insufficient server speed", mps/2, 
                                 sss.messages.throughput());
        }
    }

}
