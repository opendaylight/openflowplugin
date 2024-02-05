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
import org.opendaylight.openflowplugin.api.openflow.FlowGroupStatus;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.impl.util.ErrorUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RemoveFlowImpl extends AbstractFlowRpc<RemoveFlowOutput> implements RemoveFlow {
    private static final Logger LOG = LoggerFactory.getLogger(RemoveFlowImpl.class);

    public RemoveFlowImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, convertorExecutor, RemoveFlowOutput.class);
    }

    @Override
    public ListenableFuture<RpcResult<RemoveFlowOutput>> invoke(final RemoveFlowInput input) {
        final var future = single.canUseSingleLayerSerialization() ? single.handleServiceCall(input)
            : multi.processFlowModInputBuilders(multi.toFlowModInputs(input));

        Futures.addCallback(future, new RemoveFlowCallback(input), MoreExecutors.directExecutor());
        return future;
    }

    private final class RemoveFlowCallback implements FutureCallback<RpcResult<RemoveFlowOutput>> {
        private static final Uint8 OFPTT_ALL = Uint8.MAX_VALUE;

        private final RemoveFlowInput input;

        private RemoveFlowCallback(final RemoveFlowInput input) {
            this.input = input;
        }

        @Override
        public void onSuccess(final RpcResult<RemoveFlowOutput> result) {
            if (result.isSuccessful()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Flow remove finished without error for flow={}", input);
                }
                final DeviceFlowRegistry flowRegistry = deviceContext.getDeviceFlowRegistry();
                if (input.getTableId() != null && !input.getTableId().equals(OFPTT_ALL)) {
                    var flowRegistryKey =
                            FlowRegistryKeyFactory.create(deviceContext.getDeviceInfo().getVersion(), input);
                    flowRegistry.addMark(flowRegistryKey);

                    final FlowRef flowRef = input.getFlowRef();
                    if (flowRef != null) {
                        final FlowId flowId = flowRef.getValue().firstKeyOf(Flow.class).getId();
                        flowRegistry.appendHistoryFlow(flowId, input.getTableId(), FlowGroupStatus.REMOVED);
                    }
                } else {
                    flowRegistry.clearFlowRegistry();
                }
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Flow remove failed for flow={}, errors={}", input,
                        ErrorUtil.errorsToString(result.getErrors()));
            }
        }

        @Override
        public void onFailure(final Throwable throwable) {
            LOG.warn("Service call for removing flow={} failed", input, throwable);
        }
    }
}
