/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.yangtools.yang.common.Uint8;

@NonNullByDefault
abstract sealed class AbstractFlowRpc permits AbstractAddFlow, AbstractRemoveFlow, AbstractUpdateFlow {
    private final DeviceFlowRegistry flowRegistry;
    private final Uint8 version;

    AbstractFlowRpc(final DeviceContext deviceContext) {
        flowRegistry = requireNonNull(deviceContext.getDeviceFlowRegistry());
        version = deviceContext.getDeviceInfo().getVersion();
    }

    protected final DeviceFlowRegistry flowRegistry() {
        return flowRegistry;
    }

    protected final Uint8 version() {
        return version;
    }
}
