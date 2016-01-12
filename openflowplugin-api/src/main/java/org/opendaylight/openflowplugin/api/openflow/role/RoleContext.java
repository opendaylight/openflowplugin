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

/**
 * Created by kramesha on 9/12/15.
 */
public interface RoleContext extends RoleChangeListener, DeviceContextClosedHandler, RequestContextStack {

    /**
     * Initialization method is responsible for a registration of
     * {@link org.opendaylight.controller.md.sal.common.api.clustering.Entity} and listen for notification from service.
     * {@link Future} returned object is used primary
     * for new connection initialization phase where we have to wait for actual Role.
     * The {@link Future} has to be canceled if device is in disconnected state or when
     * {@link org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService} returns
     * {@link CandidateAlreadyRegisteredException}
     * @return InitializationFuture for to know where first initial Election is done and we know role.
     * @throws CandidateAlreadyRegisteredException - connection has to be closed
     */
    Future<Void> initialization() throws CandidateAlreadyRegisteredException;

    void facilitateRoleChange(FutureCallback<Boolean> futureCallback);

}
