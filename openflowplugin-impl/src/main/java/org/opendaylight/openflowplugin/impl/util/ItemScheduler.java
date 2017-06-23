/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemScheduler<K, V> implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ItemScheduler.class);

    private final HashedWheelTimer hashedWheelTimer;
    private final Consumer<V> action;
    private final long timeoutMillis;
    private final long toleranceMillis;
    private final Map<K, V> items = Collections.synchronizedMap(new HashMap<>());
    private final Map<K, V> queue = Collections.synchronizedMap(new HashMap<>());
    private final Object scheduleLock = new Object();

    private volatile long startTime = -1;
    private volatile Timeout runningTimeout;

    /**
     * Instantiates a new Item scheduler.
     *
     * @param hashedWheelTimer the hashed wheel timer
     * @param timeoutMillis    the timeout millis
     * @param toleranceMillis  the tolerance millis
     * @param action           the action
     */
    public ItemScheduler(final HashedWheelTimer hashedWheelTimer,
                         final long timeoutMillis,
                         final long toleranceMillis,
                         final Consumer<V> action) {
        this.hashedWheelTimer = hashedWheelTimer;
        this.action = action;
        this.timeoutMillis = timeoutMillis;
        this.toleranceMillis = toleranceMillis;
    }

    /**
     * Start scheduler timeout if it is not already running and if there are any items scheduled
     */
    public void startIfNotRunning() {
        synchronized (scheduleLock) {
            if (Objects.nonNull(runningTimeout) || (items.isEmpty() && queue.isEmpty())) {
                LOG.debug("Scheduler {} is already running or nothing is scheduled, skipping start.", this);
                return;
            }

            startTime = System.currentTimeMillis();
            LOG.debug("Scheduler {} started with configured timeout {}ms and scheduling tolerance {}ms.",
                    this, timeoutMillis, toleranceMillis);

            runningTimeout = hashedWheelTimer.newTimeout((timeout) -> {
                synchronized (scheduleLock) {
                    LOG.debug("Running configured action on {} scheduled items for scheduler {}. There are {} items left in queue.",
                            items.size(), this, queue.size());
                    items.forEach((key, item) -> action.accept(item));
                    items.clear();
                    items.putAll(queue);
                    queue.clear();
                    close();
                }

                startIfNotRunning();
            }, timeoutMillis, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Schedule item for processing
     *
     * @param key the item key
     * @param item the item
     */
    public void add(final K key, final V item) {
        synchronized (scheduleLock) {
            final long currentTime = System.currentTimeMillis();

            if (currentTime - toleranceMillis <= startTime) {
                LOG.debug("Adding {} to scheduled items for scheduler {}.", key, this);
                items.put(key, item);
            } else {
                LOG.debug("Adding {} to scheduling queue for scheduler {}.", key, this);
                queue.put(key, item);
            }
        }
    }

    /**
     * Remove item for processing
     * @param key the item key
     */
    public void remove(final K key) {
        synchronized (scheduleLock) {
            LOG.debug("Removing {} from scheduled items and queue for scheduler {}", key, this);
            items.remove(key);
            queue.remove(key);

            if (items.isEmpty() && queue.isEmpty()) {
                close();
            }
        }
    }

    @Override
    public void close() {
        LOG.debug("Closing scheduler {} and cancelling all running tasks.", this);
        startTime = -1;

        if (Objects.nonNull(runningTimeout)) {
            runningTimeout.cancel();
            runningTimeout = null;
        }
    }
}
