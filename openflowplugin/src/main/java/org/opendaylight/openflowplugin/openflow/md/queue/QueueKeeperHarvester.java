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
public class QueueKeeperHarvester<IN> implements Runnable, HarvesterHandle {
    private static Logger LOG = LoggerFactory.getLogger(QueueKeeperHarvester.class);

    private Enqueuer<QueueItem<IN>> enqueuer;
    private Collection<QueueKeeper<IN>> messageSources;

    private boolean finishing = false;
    private boolean starving;
    
    private Object harvestLock;

    
    /**
     * @param enqueuer
     * @param messageSources
     * @param harvestLock 
     */
    public QueueKeeperHarvester(Enqueuer<QueueItem<IN>> enqueuer,
            Collection<QueueKeeper<IN>> messageSources) {
        this.enqueuer = enqueuer;
        this.messageSources = messageSources;
        harvestLock = new Object();
    }

    @Override
    public void run() {
        while (! finishing ) {
            starving = true;
            for (QueueKeeper<IN> source : messageSources) {
                QueueItem<IN> qItem = source.poll();
                if (qItem != null) {
                    enqueuer.enqueueQueueItem(qItem);
                    starving = false;
                }
            }
            
            if (starving) {
                synchronized (harvestLock) {
                    try {
                        LOG.debug("messageHarvester is about to make a starve sleep");
                        harvestLock.wait();
                        LOG.debug("messageHarvester is waking up from a starve sleep");
                    } catch (InterruptedException e) {
                        LOG.warn("message harvester has been interrupted during starve sleep", e);
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
    }
    
    @Override
    public void ping() {
        if (starving) {
            LOG.debug("pinging message harvester in starve status");
            starving = false;
            synchronized (harvestLock) {
                harvestLock.notify();
            }
        }
    }
}
