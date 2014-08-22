/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.util.NamedThreadFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static org.opendaylight.util.NamedThreadFactory.namedThreads;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.junit.Assert.assertEquals;

/**
 * Test the named thread factory.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public class NamedThreadFactoryTest {

    private static class MyRunnable implements Runnable {
        String name = null;
        @Override
        public void run() {
            name = Thread.currentThread().getName();
        }
    }

    private ThreadFactory tf;

    @Before
    public void beforeTest() {
        NamedThreadFactory.poolNumber.set(1);
    }

    @Test
    public void single() throws InterruptedException, ExecutionException {
        tf = new NamedThreadFactory("MySingleThreadGroupName");
        ExecutorService es = newSingleThreadExecutor(tf);

        MyRunnable r1 = new MyRunnable();
        MyRunnable r2 = new MyRunnable();

        es.submit(r1).get(); // wait until done
        es.submit(r2).get(); // wait until done

        assertEquals("MySingleThreadGroupName-1-thread-1", r1.name);
        assertEquals("MySingleThreadGroupName-1-thread-1", r2.name);
    }

    @Test
    public void singleConvenience()
            throws InterruptedException, ExecutionException {
        ExecutorService es = newSingleThreadExecutor(namedThreads("Foo"));

        MyRunnable r1 = new MyRunnable();
        MyRunnable r2 = new MyRunnable();

        es.submit(r1).get(); // wait until done
        es.submit(r2).get(); // wait until done

        assertEquals("Foo-1-thread-1", r1.name);
        assertEquals("Foo-1-thread-1", r2.name);
    }

    @Test
    public void pool() throws InterruptedException, ExecutionException {
        tf = new NamedThreadFactory("GroupA");
        ExecutorService es1 = Executors.newFixedThreadPool(3, tf);
        MyRunnable r11 = new MyRunnable();
        MyRunnable r12 = new MyRunnable();
        MyRunnable r13 = new MyRunnable();

        tf = new NamedThreadFactory("GroupB");
        ExecutorService es2 = Executors.newFixedThreadPool(2, tf);
        MyRunnable r21 = new MyRunnable();
        MyRunnable r22 = new MyRunnable();

        es1.submit(r11).get(); // wait until done
        es1.submit(r12).get(); // wait until done
        es1.submit(r13).get(); // wait until done

        es2.submit(r21).get(); // wait until done
        es2.submit(r22).get(); // wait until done

        assertEquals("GroupA-1-thread-1", r11.name);
        assertEquals("GroupA-1-thread-2", r12.name);
        assertEquals("GroupA-1-thread-3", r13.name);

        assertEquals("GroupB-2-thread-1", r21.name);
        assertEquals("GroupB-2-thread-2", r22.name);
    }
}
