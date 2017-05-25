/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.util;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ItemScheduler<K, V> implements AutoCloseable {
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
        if (Objects.nonNull(runningTimeout) || items.isEmpty()) {
            return;
        }

        startTime = System.currentTimeMillis();
        runningTimeout = hashedWheelTimer.newTimeout((timeout) -> {
            synchronized (scheduleLock) {
                items.forEach((key, item) -> action.accept(item));
                items.clear();
                items.putAll(queue);
                queue.clear();
            }

            reset();
            startIfNotRunning();
        }, timeoutMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedule item for processing
     *
     * @param key the item key
     * @param item the item
     */
    public void add(final K key, final V item) {
        synchronized (scheduleLock) {
            final long time = System.currentTimeMillis();

            if (time - toleranceMillis > startTime) {
                items.put(key, item);
            } else {
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
            items.remove(key);
            queue.remove(key);
        }
    }

    @Override
    public void close() throws Exception {
        if (Objects.nonNull(runningTimeout)) {
            runningTimeout.cancel();
        }

        reset();
    }

    private void reset() {
        startTime = -1;
        runningTimeout = null;
    }
}
