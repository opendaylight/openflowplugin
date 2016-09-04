/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.task;

/**
 * Created by efiijjp on 2/17/2016.
 */
public class EntityState {



    private String entityContext;
    private EntityLifecycleState lifecycleState;

    public EntityState(String entityContext, EntityLifecycleState lifecycleState) {
        this.entityContext = entityContext;
        this.lifecycleState = lifecycleState;
    }

    public String getEntityContext() {
        return entityContext;
    }

    public EntityLifecycleState getLifecycleState() {
        return lifecycleState;
    }
}
