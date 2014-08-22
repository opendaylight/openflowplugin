/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.cache;

import org.opendaylight.util.NamedThreadFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Extends the functionality of {@link NotedAgeOutHashMap} to allow listeners
 * to register for value removal events.
 *
 * @param <K> the key class
 * @param <V> the value class
 *
 * @author Simon Hunt
 */
public class VocalAgeOutHashMap<K, V> extends NotedAgeOutHashMap<K, V> {

    private static final ThreadFactory TF =
            new NamedThreadFactory("VocalAgeOutHashMap");

    // our listeners
    private final CopyOnWriteArraySet<VocalMapListener<V>> listeners =
            new CopyOnWriteArraySet<VocalMapListener<V>>();

    private final ScheduledExecutorService exec = newScheduledThreadPool(1, TF);

    // FIXME: make this configurable
    private static final long PERIOD_MS = 200; // once per 1/5 second


    // Task that prunes the map and alerts any listeners to items thus removed
    private class MapPruner implements Runnable {
        @Override
        public void run() {
            prune();
            Set<V> removed = clearDeadwood();
            if (!listeners.isEmpty() && !removed.isEmpty()) {
                for (VocalMapListener<V> vml: listeners) {
                    // safety first...
                    try {
                        vml.valuesRemoved(new HashSet<V>(removed));
                    } catch (Exception e) {
                        // Broken callback
                    }
                }
            }
        }
    }

    /**
     * Constructs a map with the initial age-out set to the default
     * number of milliseconds.
     * @see AgeOutHashMap#DEFAULT_AGE_OUT_MS
     */
    public VocalAgeOutHashMap() { }

    /**
     * Constructs a map with the specified initial age-out value (ms).
     * An exception will be thrown if the given value is below the allowed
     * minimum.
     *
     * @param ageOutMs the initial age-out value
     * @throws IllegalArgumentException if ageOutMs is less than the allowed
     *          minimum
     * @see AgeOutHashMap#MIN_AGE_OUT_MS
     */
    public VocalAgeOutHashMap(long ageOutMs) {
        super(ageOutMs);
    }

    /**
     * Activates the vocal map, scheduling the map pruner to run at the
     * configured interval.
     */
    public void activate() {
        exec.scheduleAtFixedRate(new MapPruner(),
                PERIOD_MS, PERIOD_MS, MILLISECONDS);

    }

    /**
     * Deactivates the vocal map, stopping the map pruner from running.
     */
    public void deactivate() {
        exec.shutdown();
        try {
            exec.awaitTermination(PERIOD_MS, MILLISECONDS);
        } catch (InterruptedException e) {
            // e.printStackTrace();
        }
    }

    /**
     * Adds the specified listener to this map.
     *
     * @param listener the listener to be added
     */
    public void addListener(VocalMapListener<V> listener) {
        listeners.add(listener);
    }

    /**
     * Removes the specified listener from this map.
     *
     * @param listener the listener to be removed
     */
    public void removeListener(VocalMapListener<V> listener) {
        listeners.remove(listener);
    }
}
