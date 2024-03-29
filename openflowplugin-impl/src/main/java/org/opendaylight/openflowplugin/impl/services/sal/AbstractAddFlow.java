/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2024 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public abstract non-sealed class AbstractAddFlow extends AbstractFlowRpc implements AddFlow {
    protected AbstractAddFlow(final DeviceContext deviceContext) {
        super(deviceContext);
    }

    @Override
    public final ListenableFuture<RpcResult<AddFlowOutput>> invoke(final AddFlowInput input) {
        final var flowRegistry = flowRegistry();
        final var flowRegistryKey = flowRegistry.createKey(input);
        final var future = invokeImpl(input);
        Futures.addCallback(future, new AddFlowCallback(input, flowRegistry, flowRegistryKey),
            MoreExecutors.directExecutor());
        return future;
    }

    protected abstract @NonNull ListenableFuture<RpcResult<AddFlowOutput>> invokeImpl(@NonNull AddFlowInput input);
}
