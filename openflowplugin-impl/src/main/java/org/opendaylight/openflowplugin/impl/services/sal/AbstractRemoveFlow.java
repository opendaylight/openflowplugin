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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public abstract non-sealed class AbstractRemoveFlow extends AbstractFlowRpc implements RemoveFlow {
    protected AbstractRemoveFlow(final DeviceContext deviceContext) {
        super(deviceContext);
    }

    @Override
    public final ListenableFuture<RpcResult<RemoveFlowOutput>> invoke(final RemoveFlowInput input) {
        final var future = invokeImpl(input);
        Futures.addCallback(future, new RemoveFlowCallback(input, flowRegistry()), MoreExecutors.directExecutor());
        return future;
    }

    protected abstract @NonNull ListenableFuture<RpcResult<RemoveFlowOutput>> invokeImpl(
        @NonNull RemoveFlowInput input);
}
