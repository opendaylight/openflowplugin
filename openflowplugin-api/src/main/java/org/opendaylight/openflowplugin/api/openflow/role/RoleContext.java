/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.role;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;

/**
 * Created by kramesha on 9/12/15.
 */
public interface RoleContext extends RoleChangeListener, RequestContextStack {

    /**
     * Initialization method is responsible for a registration of
     * {@link org.opendaylight.controller.md.sal.common.api.clustering.Entity} and listener
     * for notification from service
     * {@link org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService}
     * returns Role which has to be applied for responsible Device Context suite. Any Exception
     * state has to close Device connection channel.
     */
    void initializationRoleContext();

    /**
     * Termination method is responsible for an unregistrion of
     * {@link org.opendaylight.controller.md.sal.common.api.clustering.Entity} and listener
     * for notification from service
     * {@link org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService}
     * returns notification "Someone else take Leadership" or "I'm last"
     * and we need to clean Oper. DS.
     */
    void terminationRoleContext();

    @Override
    void close();

    DeviceContext getDeviceContext();

    OfpRole getClusterRole();

}
