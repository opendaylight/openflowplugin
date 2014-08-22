/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Abstract mechanism for batching events.
 *
 * @author Thomas Vachuska
 */
public abstract class AbstractEventBatcher<E> {

    private final int maxEvents;
    private final long maxMs;
    private final long idleMs;
    private final Timer timer;

    private BatchProcessor idleTask;
    private BatchProcessor maxTask;

    private List<E> events = new ArrayList<>();

    /**
     * Creates an event batcher.
     *
     * @param maxEvents maximum number of events that should be batched
     * @param maxMs     maximum number of millis since arrival of the first event
     * @param idleMs    maximum number of millis since arrival of the most recent
     *                  event
     * @param timer     timer to be used
     */
    protected AbstractEventBatcher(int maxEvents, long maxMs, long idleMs,
                                   Timer timer) {
        this.maxEvents = maxEvents;
        this.maxMs = maxMs;
        this.idleMs = idleMs;
        this.timer = timer;
    }

    /**
     * Processes the given batch of events.
     *
     * @param batch ordered batch of events
     */
    protected abstract void processBatch(List<E> batch);

    /**
     * Submits the given event.
     *
     * @param event event to be added
     */
    public synchronized void submit(E event) {
        idleTask = cancelIfSet(idleTask);
        events.add(event);

        if (events.size() == maxEvents) {
            maxTask = cancelIfSet(maxTask);
            schedule(1);
        } else {
            idleTask = schedule(idleMs);
            if (events.size() == 1)
                maxTask = schedule(maxMs);
        }
    }

    // Schedules a new batch finalization task
    private BatchProcessor schedule(long ms) {
        BatchProcessor task = new BatchProcessor();
        timer.schedule(task, ms);
        return task;
    }

    // Safely cancels the given finalization task
    private BatchProcessor cancelIfSet(BatchProcessor task) {
        if (task != null)
            task.cancel();
        return task;
    }

    /**
     * Returns the maximum number of events to be batched.
     *
     * @return number of events
     */
    public int maxEvents() {
        return maxEvents;
    }

    /**
     * Returns the maximum number of milliseconds from the first event in the
     * batch that can expire before the batch is finalized and processed.
     *
     * @return number of milliseconds
     */
    public long maxMs() {
        return maxMs;
    }

    /**
     * Returns the maximum number of milliseconds from the most recent event
     * that can expire before the batch is finalized and processed.
     *
     * @return number of milliseconds
     */
    public long idleMs() {
        return idleMs;
    }

    /**
     * Returns the backing timer used to enforce maxMs and idleMs times.
     *
     * @return backing timer
     */
    public Timer timer() {
        return timer;
    }

    // Finalizes the current batch and starts a new one
    private synchronized List<E> makeBatch() {
        List<E> batch = events;
        events = new ArrayList<>();
        return batch;
    }

    // Tasks to invoke batch finalization and processing
    private class BatchProcessor extends TimerTask {
        @Override public void run() {
            idleTask = cancelIfSet(idleTask);
            maxTask = cancelIfSet(maxTask);
            processBatch(makeBatch());
        }
    }

}
