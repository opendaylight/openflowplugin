/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.openflowplugin.applications.prioritytaskmgr.api;

import org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.task.EntityState;

public interface IPriorityTaskManager {




    /**
     * Create a list of recursive actions to be performed for a given entityContext (switch to be specific)
     * for a given task-execution priority
     * @param taskFactory - Task Factory to be implemented by the applications
     * @param taskFactoryType - Unique Task Factory type name
     */
     public void registerPriorityBasedTaskFactory(IPriorityBasedTaskFactory taskFactory,
                                                  String taskFactoryType);

    /**
     * This interface is used to send the entity-state so that priority-based tasks for a given entity could be
     * either started or cancelled
     * @param entityState - represents entityContext and status (ASSOCIATED or DISSOCIATED)
     * @param actionType - this indicates which type of action is to be invoked corresponding to entityState
     */
    public void sendEntityState(EntityState entityState, String actionType);

 }





