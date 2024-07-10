/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2024 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FutureCallback;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupStatus;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowDescriptorFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class UpdateFlowCallback implements FutureCallback<RpcResult<UpdateFlowOutput>> {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateFlowCallback.class);

    private final UpdateFlowInput input;
    private final DeviceFlowRegistry flowRegistry;

    UpdateFlowCallback(final UpdateFlowInput input, final DeviceFlowRegistry flowRegistry) {
        this.input = requireNonNull(input);
        this.flowRegistry = requireNonNull(flowRegistry);
    }

    @Override
    public void onSuccess(final RpcResult<UpdateFlowOutput> updateFlowOutputRpcResult) {
        final UpdatedFlow updated = input.getUpdatedFlow();
        final OriginalFlow original = input.getOriginalFlow();
        final FlowRegistryKey origFlowRegistryKey = flowRegistry.createKey(original);
        final FlowRegistryKey updatedFlowRegistryKey = flowRegistry.createKey(updated);
        final FlowDescriptor origFlowDescriptor = flowRegistry.retrieveDescriptor(origFlowRegistryKey);

        final boolean isUpdate = origFlowDescriptor != null;
        final FlowDescriptor updatedFlowDescriptor;
        final FlowRef flowRef = input.getFlowRef();
        if (flowRef != null) {
            final Uint8 tableId = updated.getTableId();
            final FlowId flowId = ((DataObjectIdentifier<?>) flowRef.getValue()).toLegacy().firstKeyOf(Flow.class)
                .getId();
            // FIXME: this does not look right, we probably want better integration
            flowRegistry.appendHistoryFlow(flowId, tableId, FlowGroupStatus.MODIFIED);

            updatedFlowDescriptor = FlowDescriptorFactory.create(tableId, flowId);
        } else if (isUpdate) {
            updatedFlowDescriptor = origFlowDescriptor;
        } else {
            flowRegistry.store(updatedFlowRegistryKey);
            updatedFlowDescriptor = flowRegistry.retrieveDescriptor(updatedFlowRegistryKey);
        }

        if (isUpdate) {
            flowRegistry.addMark(origFlowRegistryKey);
            flowRegistry.storeDescriptor(updatedFlowRegistryKey, updatedFlowDescriptor);
        }
    }

    @Override
    public void onFailure(final Throwable throwable) {
        LOG.warn("Service call for updating flow={} failed", input, throwable);
    }
}