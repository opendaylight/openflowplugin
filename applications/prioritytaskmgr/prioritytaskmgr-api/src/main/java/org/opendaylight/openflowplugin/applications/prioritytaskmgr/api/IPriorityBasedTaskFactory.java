/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.openflowplugin.applications.prioritytaskmgr.api;

import org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.task.PriorityBasedTask;

import java.util.List;
import java.util.concurrent.RecursiveAction;

/**
 * Interface to be implemented by applications who create Tasks collection based on specific priority
 * Upon specific events (eg. node-connected, port-up etc) PriorityTaskManager invokes this
 * interface and expects applications to supply collection of tasks as per priority and event-context
 *
 * Applications have to convert entityContext appropriately. Eg. for entityContext 'openflow:1', application
 * may want to build a Node or FlowCapableNode as per BA definitions and use the same for adding task-logic
 *
 *
 * Created by efiijjp on 2/13/2016.
 */
public interface IPriorityBasedTaskFactory {

    /**
     * Create a list of recursive actions to be performed for a given entityContext (switch to be specific)
     * for a given task-execution priority
     * IMPORTANT NOTE : Classes providing the {@code List<RecursiveAction> } MUST catch runtime exceptions
     * in compute() method and log them properly to make debugging easier
     * @param priority the priority
     * @param entityContext the priority
     */
    public List<RecursiveAction> create(int priority, String entityContext);

}
