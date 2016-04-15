/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.role;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.RoleChangeListener;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.impl.services.SalRoleServiceImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * @author Jozef Bacigal
 *         Date: 14.4.2016
 */
public class RoleContextBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(RoleContextBuilder.class);

    private RoleContext roleContext;

    private RoleContextBuilder(){
        //Not allowed
    }

    public RoleContextBuilder(final NodeId nodeId, final EntityOwnershipService entityOwnershipService, final Entity entity, final Entity txEntity) {
        this.roleContext = new RoleContextImpl(nodeId, entityOwnershipService, entity, txEntity);
    }

    public RoleContext build(){
        return roleContext;
    }

    public RoleContextBuilder setSalRoleService(@Nonnull final DeviceContext deviceContext) {
        Preconditions.checkNotNull(deviceContext);
        this.roleContext.setSalRoleService(new SalRoleServiceImpl(roleContext, deviceContext));
        return this;
    }

    public RoleContextBuilder addListener(final RoleChangeListener listener) {
        if (null != listener) {
            this.roleContext.addListener(listener);
        }
        return this;
    }

}
