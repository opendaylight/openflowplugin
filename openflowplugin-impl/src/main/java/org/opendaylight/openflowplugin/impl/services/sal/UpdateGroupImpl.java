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
import org.opendaylight.openflowplugin.impl.util.ErrorUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UpdateGroupImpl extends AbstractGroupRpc<Group, UpdateGroupOutput> implements UpdateGroup {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateGroupImpl.class);

    public UpdateGroupImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, convertorExecutor, UpdateGroupOutput.class);
    }

    @Override
    public ListenableFuture<RpcResult<UpdateGroupOutput>> invoke(final UpdateGroupInput input) {
        final var resultFuture = single.canUseSingleLayerSerialization()
            ? single.handleServiceCall(input.getUpdatedGroup())
            : multi.handleServiceCall(input.getUpdatedGroup());

        Futures.addCallback(resultFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(final RpcResult<UpdateGroupOutput> result) {
                if (result.isSuccessful()) {
                    UpdatedGroup updatedGroup = input.getUpdatedGroup();
                    deviceContext.getDeviceGroupRegistry().appendHistoryGroup(
                        updatedGroup.getGroupId(), updatedGroup.getGroupType(), FlowGroupStatus.MODIFIED);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Group update with original id={} finished without error",
                            input.getOriginalGroup().getGroupId().getValue());
                    }
                } else {
                    LOG.warn("Group update with original id={} failed, errors={}",
                        input.getOriginalGroup().getGroupId(), ErrorUtil.errorsToString(result.getErrors()));
                    LOG.debug("Group input={}", input.getUpdatedGroup());
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("Service call for updating group={} failed", input.getOriginalGroup().getGroupId(), throwable);
            }
        }, MoreExecutors.directExecutor());
        return resultFuture;
    }
}
