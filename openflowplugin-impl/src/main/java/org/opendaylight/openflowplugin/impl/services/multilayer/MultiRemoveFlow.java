/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.multilayer;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.services.sal.AbstractRemoveFlow;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public final class MultiRemoveFlow extends AbstractRemoveFlow {
    private final MultiLayerFlowService<RemoveFlowOutput> service;

    public MultiRemoveFlow(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final ConvertorExecutor convertorExecutor) {
        super(deviceContext);
        service = new MultiLayerFlowService<>(requestContextStack, deviceContext, RemoveFlowOutput.class,
            convertorExecutor);
    }

    @Override
    protected ListenableFuture<RpcResult<RemoveFlowOutput>> invokeImpl(final RemoveFlowInput input) {
        return service.processFlowModInputBuilders(service.toFlowModInputs(input));
    }
}
