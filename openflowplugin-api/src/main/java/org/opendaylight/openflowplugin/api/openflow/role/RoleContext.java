/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.role;

import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;

/**
 * Role context for change role on cluster
 */
public interface RoleContext extends  RequestContextStack, AutoCloseable, OFPContext {

    /**
     * Initialization method is responsible for a registration of
     * {@link org.opendaylight.controller.md.sal.common.api.clustering.Entity} and listener
     * for notification from service
     * {@link org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService}
     * returns Role which has to be applied for responsible Device Context suite. Any Exception
     * state has to close Device connection channel.
     * @return true if initialization done ok
     */
    boolean initialization();

    /**
     * Termination method is responsible for an unregistration of
     * {@link org.opendaylight.controller.md.sal.common.api.clustering.Entity} and listener
     * for notification from service
     * {@link org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService}
     * returns notification "Someone else take Leadership" or "I'm last"
     * and we need to clean Oper. DS.
     */
    void unregisterAllCandidates();

    /**
     * Setter for sal role service
     * @param salRoleService
     */
    void setSalRoleService(@Nonnull final SalRoleService salRoleService);

    /**
     * Getter for sal role service
     * @return
     */
    SalRoleService getSalRoleService();

    /**
     * Getter for main entity
     * @return
     */
    Entity getEntity();

    /**
     * Getter for tx entity
     * @return
     */
    Entity getTxEntity();

    /**
     * Returns true if main entity is registered
     * @return
     */
    boolean isMainCandidateRegistered();

    /**
     * Returns true if tx entity is registered
     * @return
     */
    boolean isTxCandidateRegistered();

    /**
     * Register candidate depending on parameter
     * @param entity
     * @return true is registration was successful
     */
    boolean registerCandidate(final Entity entity);

    /**
     * Unregister candidate depending on parameter
     * @param entity
     * @return true is registration was successful
     */
    boolean unregisterCandidate(final Entity entity);

    /**
     * Returns true if we hold both registrations
     * @return
     */
    boolean isMaster();

    @Override
    void close();
}
