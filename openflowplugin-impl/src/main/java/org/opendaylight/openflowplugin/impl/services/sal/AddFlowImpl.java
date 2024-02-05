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
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowDescriptorFactory;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.impl.util.ErrorUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AddFlowImpl extends AbstractFlowRpc<AddFlowOutput> implements AddFlow {
    private static final Logger LOG = LoggerFactory.getLogger(AddFlowImpl.class);

    public AddFlowImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, convertorExecutor, AddFlowOutput.class);
    }

    @Override
    public ListenableFuture<RpcResult<AddFlowOutput>> invoke(final AddFlowInput input) {
        final var flowRegistryKey = FlowRegistryKeyFactory.create(deviceContext.getDeviceInfo().getVersion(), input);
        final var future = single.canUseSingleLayerSerialization() ? single.handleServiceCall(input)
            : multi.processFlowModInputBuilders(multi.toFlowModInputs(input));
        Futures.addCallback(future, new AddFlowCallback(input, flowRegistryKey),
            MoreExecutors.directExecutor());
        return future;
    }

    private final class AddFlowCallback implements FutureCallback<RpcResult<AddFlowOutput>> {
        private final AddFlowInput input;
        private final FlowRegistryKey flowRegistryKey;

        private AddFlowCallback(final AddFlowInput input, final FlowRegistryKey flowRegistryKey) {
            this.input = input;
            this.flowRegistryKey = flowRegistryKey;
        }

        @Override
        public void onSuccess(final RpcResult<AddFlowOutput> rpcResult) {
            if (!rpcResult.isSuccessful()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Flow add failed for flow={}, errors={}", input,
                            ErrorUtil.errorsToString(rpcResult.getErrors()));
                }
                return;
            }

            final DeviceFlowRegistry flowRegistry = deviceContext.getDeviceFlowRegistry();
            final FlowDescriptor flowDescriptor;
            final FlowRef flowRef = input.getFlowRef();
            if (flowRef != null) {
                final Uint8 tableId = input.getTableId();
                final FlowId flowId = flowRef.getValue().firstKeyOf(Flow.class).getId();
                flowDescriptor = FlowDescriptorFactory.create(tableId, flowId);

                // FIXME: this looks like an atomic operation
                flowRegistry.appendHistoryFlow(flowId, tableId, FlowGroupStatus.ADDED);
                flowRegistry.storeDescriptor(flowRegistryKey, flowDescriptor);
            } else {
                // FIXME: this looks like an atomic operation
                flowRegistry.store(flowRegistryKey);
                flowDescriptor = flowRegistry.retrieveDescriptor(flowRegistryKey);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Flow add with id={} finished without error", flowDescriptor.getFlowId().getValue());
            }
        }

        @Override
        public void onFailure(final Throwable throwable) {
            LOG.warn("Service call for adding flow={} failed", input, throwable);
        }
    }
}
