/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiLayerFlowService;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerFlowService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yangtools.yang.binding.RpcOutput;

abstract class AbstractFlowRpc<O extends RpcOutput> extends AbstractDeviceRpc {
    final @NonNull MultiLayerFlowService<O> multi;
    final @NonNull SingleLayerFlowService<O> single;

    AbstractFlowRpc(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final ConvertorExecutor convertorExecutor, final Class<O> output) {
        super(deviceContext);
        multi = new MultiLayerFlowService<>(requestContextStack, deviceContext, output, convertorExecutor);
        single = new SingleLayerFlowService<>(requestContextStack, deviceContext, output);
    }
}
