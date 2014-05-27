/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <IN> 
 * 
 */
public class QueueKeeperHarvester<IN> implements Runnable {
    private static Logger LOG = LoggerFactory.getLogger(QueueKeeperHarvester.class);

    private Enqueuer<QueueItem<IN>> enqueuer;
    private Collection<QueueKeeper<IN>> messageSources;

    private boolean finished = false;

    private Object harvestLock;
    
    /**
     * @param enqueuer
     * @param messageSources
     * @param harvestLock 
     */
    public QueueKeeperHarvester(Enqueuer<QueueItem<IN>> enqueuer,
            Collection<QueueKeeper<IN>> messageSources, Object harvestLock) {
        this.enqueuer = enqueuer;
        this.messageSources = messageSources;
        this.harvestLock = harvestLock;
    }

    @Override
    public void run() {
        while (! finished ) {
            for (QueueKeeper<IN> source : messageSources) {
                QueueItem<IN> qItem = source.poll();
                if (qItem != null) {
                    enqueuer.enqueueQueueItem(qItem);
                }
            }
            try {
                harvestLock.wait();
            } catch (InterruptedException e) {
                LOG.warn("message source harvester was interrupted", e);
            }
        }
    }
    
    /**
     * finish harvester
     */
    public void finish() {
        this.finished = true;
    }
}
