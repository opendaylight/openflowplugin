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
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UpdateMeterImpl extends AbstractMeterRpc<Meter, UpdateMeterOutput> implements UpdateMeter {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateMeterImpl.class);

    public UpdateMeterImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, convertorExecutor, UpdateMeterOutput.class);
    }

    @Override
    public ListenableFuture<RpcResult<UpdateMeterOutput>> invoke(final UpdateMeterInput input) {
        final var resultFuture = single.canUseSingleLayerSerialization()
            ? single.handleServiceCall(input.getUpdatedMeter())
            : multi.handleServiceCall(input.getUpdatedMeter());

        Futures.addCallback(resultFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(final RpcResult<UpdateMeterOutput> result) {
                if (result.isSuccessful()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Meter update with id={} finished without error",
                            input.getOriginalMeter().getMeterId());
                    }
                } else {
                    LOG.warn("Meter update with id={} failed, errors={}", input.getOriginalMeter().getMeterId(),
                        ErrorUtil.errorsToString(result.getErrors()));
                    LOG.debug("Meter input={}", input.getUpdatedMeter());
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("Service call for updating meter={} failed", input.getOriginalMeter().getMeterId(), throwable);
            }
        }, MoreExecutors.directExecutor());
        return resultFuture;
    }
}
