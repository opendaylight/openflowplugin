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
import org.opendaylight.openflowplugin.impl.util.FlowCreatorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yangtools.yang.common.RpcResult;

public abstract non-sealed class AbstractUpdateFlow extends AbstractFlowRpc implements UpdateFlow {
    private final FlowCreatorUtil util;

    protected AbstractUpdateFlow(final DeviceContext deviceContext) {
        super(deviceContext);
        util = FlowCreatorUtil.forVersion(deviceContext.getDeviceInfo().getVersion());
    }

    @Override
    public final ListenableFuture<RpcResult<UpdateFlowOutput>> invoke(final UpdateFlowInput input) {
        final var original = input.getOriginalFlow();
        final var updated = input.getUpdatedFlow();

        final var future = util.canModifyFlow(original, updated) ? modify(updated) : update(original, updated);
        Futures.addCallback(future, new UpdateFlowCallback(input, flowRegistry()), MoreExecutors.directExecutor());
        return future;
    }

    protected abstract @NonNull ListenableFuture<RpcResult<UpdateFlowOutput>> modify(@NonNull UpdatedFlow updated);

    protected abstract @NonNull ListenableFuture<RpcResult<UpdateFlowOutput>> update(@NonNull OriginalFlow original,
        @NonNull UpdatedFlow updated);
}
