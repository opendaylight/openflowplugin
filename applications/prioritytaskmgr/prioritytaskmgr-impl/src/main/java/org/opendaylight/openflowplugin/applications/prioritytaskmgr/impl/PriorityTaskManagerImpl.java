/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.prioritytaskmgr.impl;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.monitor.IPriorityBasedTaskMonitor;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.monitor.PriorityBasedTaskMonitor;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.IPriorityBasedTaskFactory;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.IPriorityTaskManager;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.task.EntityLifecycleState;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.task.EntityState;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.taskhandler.PriorityBasedTaskExecutor;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.utils.PriorityBasedTaskManagerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by efiijjp on 2/13/2016.
 */
public class PriorityTaskManagerImpl implements BindingAwareProvider, IPriorityTaskManager, AutoCloseable {


    private static Map<String, Set<IPriorityBasedTaskFactory>> typeBasedPriorityTaskFactorySetMap =
            new ConcurrentHashMap<>();

    private static Map<String, PriorityBasedTaskExecutor> ongoingExecutions =
            new ConcurrentHashMap<>();
    private IPriorityBasedTaskMonitor taskMonitor = new PriorityBasedTaskMonitor();

    //TODO: We do not have a common location to source this pool from for now. When we move to
    //JDK8, we can get it from system-wide pool
    private ForkJoinPool fjPool =
            new ForkJoinPool(PriorityBasedTaskManagerConstants.PRIORITY_TASK_EXECUTOR_THREAD_POOL_SIZE);


    private static final Logger LOG = LoggerFactory.getLogger(PriorityTaskManagerImpl.class);

    @Override
    public void onSessionInitiated(BindingAwareBroker.ProviderContext providerContext) {
        LOG.info("PriorityTaskManagerImpl initiated");
    }


    @Override
    public void registerPriorityBasedTaskFactory(IPriorityBasedTaskFactory taskFactory,
                                                 String taskFactoryType) {

        //synchronize this to allow multiple modules concurrently registering multiple factories across
        //types
        synchronized (typeBasedPriorityTaskFactorySetMap) {
            if (typeBasedPriorityTaskFactorySetMap.get(taskFactoryType) != null) {
                LOG.info("Found TaskFactorySet of type - {}", taskFactoryType);
                typeBasedPriorityTaskFactorySetMap.get(taskFactoryType).add(taskFactory);
                LOG.info("Registered TaskFactory - {} of type - {} for ", taskFactory.getClass().getName(),
                        taskFactoryType);
            } else {
                Set<IPriorityBasedTaskFactory> priorityTaskFactorySet = new HashSet<>();
                priorityTaskFactorySet.add(taskFactory);
                LOG.info("Created TaskFactorySet of type - {}", taskFactoryType);
                typeBasedPriorityTaskFactorySetMap.put(taskFactoryType, priorityTaskFactorySet);
                LOG.info("Registered TaskFactory - {} of type - {} for ", taskFactory.getClass().getName(),
                        taskFactoryType);
            }
        }
    }

    @Override
    public void sendEntityState(EntityState entityState, String actionType) {

        if (typeBasedPriorityTaskFactorySetMap.isEmpty()){
            LOG.warn("No TaskFactory of any type Registered by any applications. Aborting processing");
            return;
        }
        else {
            if (typeBasedPriorityTaskFactorySetMap.get(actionType) == null
                    || typeBasedPriorityTaskFactorySetMap.get(actionType).isEmpty()){
                LOG.warn("No Matching TaskFactory for ActionType {} Registered by any applications. Aborting processing", actionType);
                return;
            }
        }

        EntityLifecycleState entityStatus = entityState.getLifecycleState();

        LOG.info("EntityState update received for entity {} wih state {}",
                entityState.getEntityContext(),
                entityStatus.name());

        switch(entityStatus){
            case ASSOCIATED:
                if (ongoingExecutions.get(entityState.getEntityContext()) == null){
                    LOG.info("Executing ASSOCIATED entity-state handler for entity {} wih state {}",
                            entityState.getEntityContext(),
                            entityStatus.name());
                    PriorityBasedTaskExecutor taskExecutor =
                            new PriorityBasedTaskExecutor(entityState.getEntityContext(),
                                    typeBasedPriorityTaskFactorySetMap.get(actionType),
                                    taskMonitor,
                                    fjPool);
                    ongoingExecutions.put(entityState.getEntityContext(),taskExecutor );
                    LOG.info("ASSOCIATED : Starting Root Task for entity {} wih state {}",
                            entityState.getEntityContext(),
                            entityStatus.name());
                    taskExecutor.start();
                }
                else{
                    // just to troubleshoot weird duplicate events
                    LOG.warn("Duplicate ASSOCIATED event detected for entity {}", entityState.getEntityContext());
                }
                break;
            case DISSOCIATED:
                if (ongoingExecutions.get(entityState.getEntityContext()) != null){
                    LOG.info("Executing DISSOCIATED entity-state for entity {} wih state {}",
                            entityState.getEntityContext(),
                            entityStatus.name());
                    ongoingExecutions.get(entityState.getEntityContext()).cancel();
                    ongoingExecutions.remove(entityState.getEntityContext());
                    LOG.info("DISSOCIATED : Cleanup for entity {} wih state {}",
                            entityState.getEntityContext(),
                            entityStatus.name());
                }
                else{
                    // just to troubleshoot weird duplicate events
                    LOG.warn("DISSOCIATED event detected without previous ASSOCIATED event for entity {}", entityState.getEntityContext());

                }
                break;
            default:
                LOG.error("Received EntityStatus which cannot be processed !");
        }
    }

    @Override
    public void close() throws Exception {
        LOG.warn("PriorityTaskManagerImpl Closed");

    }

}
