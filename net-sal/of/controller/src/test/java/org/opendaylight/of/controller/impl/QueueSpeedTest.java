/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.VId;
import org.opendaylight.util.RandomUtils;
import org.opendaylight.util.ThroughputTracker;
import org.opendaylight.util.junit.SlowerTests;
import org.opendaylight.util.net.MacRange;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertTrue;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Simple tests to prove performance of the queueing system.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
@Category(SlowerTests.class)
public class QueueSpeedTest extends AbstractControllerTest {

    private static final int MAX_SUPER_LONG_WAIT_MS = 30000;

    private static final String FMT_INTERRUPT = "<Interrupted> {}";

    private static final int QUEUE_COUNT = 16;
    private static final int CAPACITY = 10000000;
    private static final int DPID_COUNT = 100;

    private static final DataPathId KILL_PILL = dpid("0/000000:000000");
    private static final VId VID_0 = vid("0");
    
    private List<DataPathId> dpidPool = new ArrayList<DataPathId>();

    @BeforeClass
    public static void classSetUp() {
        Assume.assumeTrue(!isUnderCoverage() && !ignoreSpeedTests());
    }
    
    @Before
    public void setUp() {
        MacRange mr = MacRange.valueOf("0:0:0:0:*:*");
        for (int c=0; c<DPID_COUNT; c++)
            dpidPool.add(DataPathId.valueOf(VID_0, mr.random()));
    }

    /**
     * Slurps dpids off a queue as fast as possible; exits after a kill pill
     * is detected.
     */
    private class Slurper implements Runnable {

        private final ThroughputTracker tt = new ThroughputTracker();
        private final BlockingQueue<DataPathId> q;
        private final CountDownLatch gate;

        Slurper(BlockingQueue<DataPathId> q, CountDownLatch gate) {
            this.q = q;
            this.gate = gate;
        }

        @Override
        public void run() {
            int c = 0;
            while (true) {
                try {
                    DataPathId id = q.take();
                    if (id == KILL_PILL)
                        break;
                    c++;
                } catch (InterruptedException e) {
                    print(FMT_INTERRUPT, e);
                }
            }
            tt.add(c);
            tt.freeze();
            gate.countDown();
        }
    }

    private DataPathId dpidFromPool() {
        return RandomUtils.select(dpidPool);
    }

    // if we are not getting more than 1/4M items/second, something is amiss
    private static final double MIN_EXP_THROUGHPUT = 250000.0; // 1/4 Million

    private void results(ThroughputTracker tt) {
        DecimalFormat format = new DecimalFormat("#0,000");
        print("Throughput: " + format.format(tt.throughput()) + " items/sec");
        assertTrue("Not working fast enough!",
                   tt.throughput() > perfScale() * MIN_EXP_THROUGHPUT);
    }

    @Test
    public void basic() {
        beginTest("basic");
        BlockingQueue<DataPathId> q =
                new LinkedBlockingQueue<DataPathId>(CAPACITY);

        workersDone = new CountDownLatch(1);
        ExecutorService exec = Executors.newSingleThreadExecutor();
        Slurper sr = new Slurper(q, workersDone);
        exec.submit(sr);

        for (int i = 0; i < CAPACITY; i++)
            q.add(dpidFromPool());
        q.add(KILL_PILL);

        waitForWorkers(MAX_SUPER_LONG_WAIT_MS);

        results(sr.tt);
        endTest();
    }


    @Test
    public void demultiplex() {
        beginTest("demultiplex");
        @SuppressWarnings("unchecked")
        
        BlockingQueue<DataPathId> dq[] = new BlockingQueue[QUEUE_COUNT];
        for (int i = 0; i < QUEUE_COUNT; i++)
            dq[i] = new LinkedBlockingQueue<DataPathId>(CAPACITY);
        
        ThroughputTracker tta = new ThroughputTracker();
        for (int i = 0; i < CAPACITY; i++) {
            DataPathId dpid = dpidFromPool();
            dq[dpid.hashBucket(dq.length)].add(dpid);
        }

        Slurper[] slurps = new Slurper[dq.length];
        workersDone = new CountDownLatch(dq.length);

        ExecutorService dexecs = Executors.newFixedThreadPool(dq.length);
        ThroughputTracker ttb = new ThroughputTracker();
        int si = 0;
        for (BlockingQueue<DataPathId> q: dq) {
            slurps[si] = new Slurper(q, workersDone);
            dexecs.submit(slurps[si++]);
            q.add(KILL_PILL);
        }

        waitForWorkers(MAX_SUPER_LONG_WAIT_MS);
        for (Slurper s: slurps)
            ttb.add(s.tt.total());
        ttb.freeze();

        tta.add(ttb.total());
        tta.freeze();

        results(ttb); // dequeue
        results(tta); // enqueue / dequeue
        endTest();
    }


}
