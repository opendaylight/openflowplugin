/*
 * (c) Copyright 2010,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread factory implementation that provides a distinct thread group name.
 * <p>
 * Usage example:
 * <pre>
 * Executors.newSingleThreadExecutor(new NamedThreadFactory("MyThreads"));
 * </pre>
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public class NamedThreadFactory implements ThreadFactory {

    private static final String E_NULL_PARAM = "groupPrefix cannot be null";
    private static final String DASH = "-";
    private static final String PREFIX = "-thread-";

    static final AtomicInteger poolNumber = new AtomicInteger(1);

    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    /**
     * Constructor that creates a thread factory using the given group name.
     *
     * @param groupName thread group prefix name
     */
    public NamedThreadFactory(String groupName) {
        if (null == groupName)
            throw new NullPointerException(E_NULL_PARAM);

        SecurityManager s = System.getSecurityManager();

        this.group = (s != null) ? s.getThreadGroup()
                                 : Thread.currentThread().getThreadGroup();
        this.namePrefix =
                groupName + DASH + poolNumber.getAndIncrement() + PREFIX;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }

    /**
     * Returns a named thread factory instance with the given group name.
     * <p>
     * This convenience method, used with a static import, makes for more
     * readable code:
     * <pre>
     * import static org.opendaylight.util.NamedThreadFactory.namedThreads;
     * import static java.util.concurrent.Executors.newSingleThreadExecutor;
     * ...
     * ExecutorService es = newSingleThreadExecutor(namedThreads("MyThreads"));
     * </pre>
     *
     * @param groupName the thread group name
     * @return the thread factory
     */
    public static ThreadFactory namedThreads(String groupName) {
        return new NamedThreadFactory(groupName);
    }
}
