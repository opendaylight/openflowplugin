/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.applications.frsync.SemaphoreKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link SemaphoreKeeperGuavaImpl}.
 */
public class SemaphoreKeeperGuavaImplTest {
    private static final Logger LOG = LoggerFactory.getLogger(SemaphoreKeeperGuavaImplTest.class);
    private SemaphoreKeeperGuavaImpl<String> semaphoreKeeper;
    private final String key = "11";

    @Before
    public void setUp() throws Exception {
        semaphoreKeeper = new SemaphoreKeeperGuavaImpl<>(1, true);
    }

    @Test
    public void testSummonGuard() throws Exception {
        Semaphore semaphore1 = semaphoreKeeper.summonGuard(key);
        final int g1FingerPrint = semaphore1.hashCode();
        Semaphore semaphore2 = semaphoreKeeper.summonGuard(key);
        final int g2FingerPrint = semaphore2.hashCode();

        Assert.assertSame(semaphore1, semaphore2);
        Assert.assertEquals(1, semaphore1.availablePermits());

        semaphore1.acquire();
        semaphore1.release();
        Assert.assertEquals(1, semaphore1.availablePermits());
        semaphore1 = null;
        System.gc();

        semaphore2.acquire();
        semaphore2.release();
        Assert.assertEquals(1, semaphore2.availablePermits());
        semaphore2 = null;
        Assert.assertEquals(g1FingerPrint, g2FingerPrint);

        System.gc();
        final Semaphore semaphore3 = semaphoreKeeper.summonGuard(key);
        Assert.assertNotEquals(g1FingerPrint, semaphore3.hashCode());
    }

    @Test
    public void testReleaseGuard() throws Exception {
        for (int total = 1; total <= 10; total++) {
            LOG.info("test run: {}", total);
            final Worker task = new Worker(semaphoreKeeper, key);

            final ExecutorService executorService = new ThreadPoolExecutor(5, 5,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>()) {
                @Override
                protected void afterExecute(final Runnable r, final Throwable t) {
                    super.afterExecute(r, t);
                    if (t != null) {
                        LOG.error("pool thread crashed", t);
                    }
                }
            };

            final int steps = 10;
            for (int i = 0; i < steps; i++) {
                executorService.submit(task);
            }
            Thread.sleep(50L);
            LOG.info("STARTING new serie");
            System.gc();

            for (int i = 0; i < steps; i++) {
                executorService.submit(task);
            }
            Thread.sleep(100L);
            System.gc();

            executorService.shutdown();
            final boolean terminated = executorService.awaitTermination(10, TimeUnit.SECONDS);
            if (!terminated) {
                LOG.warn("pool stuck, forcing termination");
                executorService.shutdownNow();
                Assert.fail("pool failed to finish gracefully");
            }

            final int counterSize = task.getCounterSize();
            LOG.info("final counter = {}", counterSize);
            Assert.assertEquals(20, counterSize);
        }
    }

    private static class Worker implements Runnable {
        private final SemaphoreKeeper<String> keeper;
        private final String key;
        private final ConcurrentMap<Integer, Integer> counter = new ConcurrentHashMap<>();
        private volatile int index = 0;

        public Worker(SemaphoreKeeper<String> keeper, final String key) {
            this.keeper = keeper;
            this.key = key;
        }

        @Override
        public void run() {
            try {
                final Semaphore guard = keeper.summonGuard(key);
                Thread.sleep(2L);
                guard.acquire();
                counter.putIfAbsent(index, 0);
                counter.put(index, counter.get(index) + 1);
                LOG.debug("queue: {} [{}] - {}", guard.getQueueLength(), guard.hashCode(), counter.size());
                index++;
                guard.release();
            } catch (Exception e) {
                LOG.warn("acquiring failed.. ", e);
            }
        }

        public int getCounterSize() {
            return counter.size();
        }
    }
}