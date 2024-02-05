/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2024 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.util.ErrorUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RemoveMeterImpl extends AbstractMeterRpc<RemoveMeterInput, RemoveMeterOutput>
        implements RemoveMeter {
    private static final Logger LOG = LoggerFactory.getLogger(RemoveMeterImpl.class);

    public RemoveMeterImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, convertorExecutor, RemoveMeterOutput.class);
    }

    @Override
    public ListenableFuture<RpcResult<RemoveMeterOutput>> invoke(final RemoveMeterInput input) {
        final var resultFuture = single.canUseSingleLayerSerialization() ? single.handleServiceCall(input)
            : multi.handleServiceCall(input);

        Futures.addCallback(resultFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(final RpcResult<RemoveMeterOutput> result) {
                if (result.isSuccessful()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Meter remove with id={} finished without error", input.getMeterId());
                    }
                } else {
                    LOG.warn("Meter remove with id={} failed, errors={}", input.getMeterId(),
                        ErrorUtil.errorsToString(result.getErrors()));
                    LOG.debug("Meter input={}", input);
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("Service call for removing meter={} failed", input.getMeterId(), throwable);
            }
        }, MoreExecutors.directExecutor());
        return resultFuture;
    }
}
