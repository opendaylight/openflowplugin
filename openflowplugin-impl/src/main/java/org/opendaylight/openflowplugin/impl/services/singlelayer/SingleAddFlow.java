/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.singlelayer;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.impl.services.sal.AbstractAddFlow;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint8;

public final class SingleAddFlow extends AbstractAddFlow {
    private final SingleLayerFlowService<AddFlowOutput> service;

    public SingleAddFlow(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final ConvertorExecutor convertorExecutor) {
        service = new SingleLayerFlowService<>(requestContextStack, deviceContext, AddFlowOutput.class);
    }

    @Override
    protected Uint8 version() {
        return service.getDeviceInfo().getVersion();
    }

    @Override
    protected DeviceFlowRegistry flowRegistry() {
        return service.getDeviceRegistry().getDeviceFlowRegistry();
    }

    @Override
    protected ListenableFuture<RpcResult<AddFlowOutput>> invokeImpl(final AddFlowInput input) {
        return service.handleServiceCall(input);
    }
}
