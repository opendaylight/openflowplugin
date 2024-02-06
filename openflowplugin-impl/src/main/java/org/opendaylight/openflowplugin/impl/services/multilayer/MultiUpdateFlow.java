/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2024 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.multilayer;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.services.sal.AbstractUpdateFlow;
import org.opendaylight.openflowplugin.impl.util.FlowCreatorUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;

public final class MultiUpdateFlow extends AbstractUpdateFlow {
    private final MultiLayerFlowService<UpdateFlowOutput> service;

    public MultiUpdateFlow(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final ConvertorExecutor convertorExecutor) {
        super(deviceContext);
        service = new MultiLayerFlowService<>(requestContextStack, deviceContext, UpdateFlowOutput.class,
            convertorExecutor);
    }

    @Override
    protected ListenableFuture<RpcResult<UpdateFlowOutput>> invokeImpl(final UpdateFlowInput input) {
        final var updated = input.getUpdatedFlow();
        final var original = input.getOriginalFlow();

        if (FlowCreatorUtil.canModifyFlow(original, updated, version())) {
            return service.processFlowModInputBuilders(service.toFlowModInputs(updated));
        }

        final var allFlowMods = new ArrayList<FlowModInputBuilder>();
        // We would need to remove original and add updated.
        // remove flow should be the first
        allFlowMods.addAll(service.toFlowModInputs(new RemoveFlowInputBuilder(original).build()));
        allFlowMods.addAll(service.toFlowModInputs(new AddFlowInputBuilder(updated).build()));
        return service.processFlowModInputBuilders(allFlowMods);
    }
}
