/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.prioritytaskmgr.taskhandler;

import org.opendaylight.openflowplugin.applications.prioritytaskmgr.monitor.IPriorityBasedTaskMonitor;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.IPriorityBasedTaskFactory;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.utils.PriorityBasedTaskManagerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Created by efiijjp on 2/17/2016.
 */
public class PriorityBasedTaskExecutor {


    private String entityContext;
    private Set<IPriorityBasedTaskFactory> taskFactorySet;
    private IPriorityBasedTaskMonitor taskMonitor;
    private ForkJoinPool fjPool;
    private PriorityBasedTaskTree entityTaskTree;

    private static final Logger LOG = LoggerFactory.getLogger(PriorityBasedTaskExecutor.class);



    public PriorityBasedTaskExecutor(String entityContext,
                                     Set<IPriorityBasedTaskFactory> taskFactorySet,
                                     IPriorityBasedTaskMonitor taskMonitor,
                                     ForkJoinPool fjPool) {
        this.entityContext = entityContext;
        this.taskFactorySet = taskFactorySet;
        this.taskMonitor = taskMonitor;
        this.fjPool = fjPool;
    }

    public void start(){
        LOG.info("Starting PriorityBasedTaskTree execution for entityContext {}", entityContext);
        entityTaskTree = new PriorityBasedTaskTree();
        try {
            fjPool.invoke(entityTaskTree);
        }
        catch(Exception ex){
            LOG.error("Exception in PriorityBasedTaskTree execution for entityContext {}", entityContext, ex);
        }

    }

    public void cancel(){
        if (!entityTaskTree.isDone()){
            LOG.info("Cancelling undone PriorityBasedTaskTree execution for entityContext {}", entityContext);
            entityTaskTree.cancel(true);
        }

    }


    private class PriorityBasedTaskTree extends RecursiveAction {

        @Override
        protected void compute() {
            taskMonitor.onTaskEntityContextStarted(entityContext);
            for (int i = 0; i< PriorityBasedTaskManagerConstants.MAX_PRIORITY_LEVEL; i++) {
                for (IPriorityBasedTaskFactory priorityTaskFactory : taskFactorySet) {
                    taskMonitor.onTaskPriorityStarted(entityContext, priorityTaskFactory.getClass().getName(), i);
                    processTasks(priorityTaskFactory, i);
                    taskMonitor.onTaskPriorityCompleted(entityContext, priorityTaskFactory.getClass().getName(), i);
                }
            }
            taskMonitor.onTaskEntityContextCompleted(entityContext);
        }

        private void processTasks(IPriorityBasedTaskFactory taskFactory, int priority){
            List<RecursiveAction> actions = taskFactory.create(priority, entityContext);
            if (actions != null && !actions.isEmpty()){
                for (RecursiveAction action : actions){
                    if (!isCancelled()) {
                        taskMonitor.onTaskStarted(entityContext, taskFactory.getClass().getName(), priority);
                        action.fork();
                    }
                }
                for (RecursiveAction action : actions){
                    action.join();
                    taskMonitor.onTaskCompleted(entityContext, taskFactory.getClass().getName(), priority);
                }
            }
        }
    }

}
