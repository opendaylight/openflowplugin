/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;

abstract class AbstractDeviceRpc {
    final @NonNull DeviceContext deviceContext;

    AbstractDeviceRpc(final DeviceContext deviceContext) {
        this.deviceContext = requireNonNull(deviceContext);
    }
}
