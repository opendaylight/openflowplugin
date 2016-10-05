/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.prioritytaskmgr.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Expose this via JMX so that we can track the progress and observe errors if any
 *
 * Created by efiijjp on 2/18/2016.
 */
public class PriorityBasedTaskMonitor implements IPriorityBasedTaskMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(PriorityBasedTaskMonitor.class);

    @Override
    public void onTaskEntityContextStarted(String entityContext) {
        LOG.info("Thread - {} : Root Task Started for entity {}", Thread.currentThread().getName(), entityContext);

    }

    @Override
    public void onTaskPriorityStarted(String entityContext, String factoryName, int priority) {
        LOG.info("Thread - {} : Root Task -> Priority Started for entity {} with factoryName {} and priority {}",
                Thread.currentThread().getName(), entityContext, factoryName, priority);

    }

    @Override
    public void onTaskStarted(String entityContext, String factoryName, int priority) {
        LOG.info("Thread - {} : Root Task -> Priority {} -> Task Started for entity {} with factoryName {}",
                Thread.currentThread().getName(), entityContext, factoryName, priority);
    }

    @Override
    public void onTaskCompleted(String entityContext, String factoryName, int priority) {
        LOG.info("Thread - {} : Root Task -> Priority {} -> Task Completed for entity {} with factoryName {}",
                Thread.currentThread().getName(), entityContext, factoryName, priority);
    }

    @Override
    public void onTaskPriorityCompleted(String entityContext, String factoryName, int priority) {
        LOG.info("Thread - {} : Root Task -> Priority Completed for entity {} with factoryName {} and priority {}",
                Thread.currentThread().getName(), entityContext, factoryName, priority);
    }

    @Override
    public void onTaskEntityContextCompleted(String entityContext) {
        LOG.info("Thread - {} : Root Task Completed for entity {}",
                Thread.currentThread().getName(), entityContext);
    }
}
