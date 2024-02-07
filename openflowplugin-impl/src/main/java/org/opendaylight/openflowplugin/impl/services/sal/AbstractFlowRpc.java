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

@NonNullByDefault
abstract sealed class AbstractFlowRpc permits AbstractAddFlow, AbstractRemoveFlow, AbstractUpdateFlow {
    private final DeviceFlowRegistry flowRegistry;

    AbstractFlowRpc(final DeviceContext deviceContext) {
        flowRegistry = requireNonNull(deviceContext.getDeviceFlowRegistry());
    }

    protected final DeviceFlowRegistry flowRegistry() {
        return flowRegistry;
    }
}
