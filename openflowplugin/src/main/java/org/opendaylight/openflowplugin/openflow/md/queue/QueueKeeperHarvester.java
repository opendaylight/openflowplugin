/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.Collection;
import org.opendaylight.openflowplugin.api.openflow.md.queue.Enqueuer;
import org.opendaylight.openflowplugin.api.openflow.md.queue.HarvesterHandle;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueItem;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <IN> Type of queue keeper harvester
 *
 */
public class QueueKeeperHarvester<IN> implements Runnable, HarvesterHandle {
    private static final Logger LOG = LoggerFactory.getLogger(QueueKeeperHarvester.class);

    private final Collection<QueueKeeper<IN>> messageSources;
    private final Enqueuer<QueueItem<IN>> enqueuer;
    private final Object harvestLock = new Object();
    private volatile boolean finishing = false;
    private volatile boolean wakeMe = false;

    /**
     * @param enqueuer queue enqueuer
     * @param messageSources source of message
     */
    public QueueKeeperHarvester(final Enqueuer<QueueItem<IN>> enqueuer,
            final Collection<QueueKeeper<IN>> messageSources) {
        this.enqueuer = enqueuer;
        this.messageSources = messageSources;
    }

    @Override
    public void run() {
        while (!finishing) {
            boolean starving = true;
            for (QueueKeeper<IN> source : messageSources) {
                QueueItem<IN> qItem = source.poll();
                if (qItem != null) {
                    starving = false;
                    enqueuer.enqueueQueueItem(qItem);
                }
            }

            if (starving) {
                LOG.trace("messageHarvester is about to make a starve sleep");
                synchronized (harvestLock) {
                    wakeMe = true;
                    try {
                        this.harvestLock.wait();
                        LOG.trace("messageHarvester is waking up from a starve sleep");
                    } catch (InterruptedException e) {
                        LOG.warn("message harvester has been interrupted during starve sleep", e);
                    } finally {
                        wakeMe = false;
                    }
                }
            }
        }
    }

    /**
     * finish harvester
     */
    public void shutdown() {
        this.finishing = true;
        ping();
    }

    @Override
    public void ping() {
        if (wakeMe) {
            synchronized (harvestLock) {
                // Might've raced while waiting for lock, so need to recheck
                if (wakeMe) {
                    LOG.debug("pinging message harvester in starve status");
                    harvestLock.notify();
                }
            }
        }
    }
}
