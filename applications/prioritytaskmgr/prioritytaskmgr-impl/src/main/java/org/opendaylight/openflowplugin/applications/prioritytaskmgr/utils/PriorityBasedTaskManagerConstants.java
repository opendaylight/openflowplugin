/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.prioritytaskmgr.utils;

/**
 * Created by efiijjp on 3/21/2016.
 */
public class PriorityBasedTaskManagerConstants {

    public static final int PRIORITY_TASK_EXECUTOR_THREAD_POOL_SIZE
            = Integer.getInteger("priority.task.coordinator.poolsize", 4);;
    public static final int MAX_PRIORITY_LEVEL = Integer.getInteger("priority.task.coordinator.level.max", 5);
}
