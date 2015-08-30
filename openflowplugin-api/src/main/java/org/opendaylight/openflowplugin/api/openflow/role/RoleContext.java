/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.role;

import com.google.common.util.concurrent.FutureCallback;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceContextClosedHandler;

/**
 * Created by kramesha on 9/12/15.
 */
public interface RoleContext extends RoleChangeListener, DeviceContextClosedHandler, RequestContextStack {

    void facilitateRoleChange(FutureCallback<Boolean> futureCallback);

}
