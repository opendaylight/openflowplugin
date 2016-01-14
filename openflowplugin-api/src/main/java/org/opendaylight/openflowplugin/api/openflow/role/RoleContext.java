/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.role;

import com.google.common.util.concurrent.FutureCallback;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceContextClosedHandler;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;

/**
 * Created by kramesha on 9/12/15.
 */
public interface RoleContext extends RoleChangeListener, DeviceContextClosedHandler, RequestContextStack {

    void facilitateRoleChange(FutureCallback<Boolean> futureCallback);

    /**
     * Initialization method is responsible for a registration of
     * {@link org.opendaylight.controller.md.sal.common.api.clustering.Entity}
     * and listen for notification from service. {@link Future} returned object is used primary
     * for new connection initialization phase where we have to wait for actual Role.
     * The {@link Future} has to be canceled if device is in disconnected state or when
     * {@link org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService} returns
     * {@link org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException}
     * @return InitializationFuture for to know where first initial Election is done and we know role.
     * @throws CandidateAlreadyRegisteredException - we have registered Entity so drop actual connection
     */
    Future<OfpRole> initialization() throws CandidateAlreadyRegisteredException;

}
