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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RemoveGroupImpl extends AbstractGroupRpc<RemoveGroupInput, RemoveGroupOutput>
        implements RemoveGroup {
    private static final Logger LOG = LoggerFactory.getLogger(RemoveGroupImpl.class);

    public RemoveGroupImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, convertorExecutor, RemoveGroupOutput.class);
    }

    @Override
    public ListenableFuture<RpcResult<RemoveGroupOutput>> invoke(final RemoveGroupInput input) {
        final var resultFuture = single.canUseSingleLayerSerialization() ? single.handleServiceCall(input)
            : multi.handleServiceCall(input);

        Futures.addCallback(resultFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(final RpcResult<RemoveGroupOutput> result) {
                if (result.isSuccessful()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Group remove with id={} finished without error", input.getGroupId().getValue());
                        deviceContext.getDeviceGroupRegistry().appendHistoryGroup(input.getGroupId(),
                            input.getGroupType(), FlowGroupStatus.REMOVED);
                    }
                } else {
                    LOG.warn("Group remove with id={} failed, errors={}", input.getGroupId().getValue(),
                        ErrorUtil.errorsToString(result.getErrors()));
                    LOG.debug("Group input={}", input);
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("Service call for removing group={} failed", input.getGroupId().getValue(), throwable);
            }
        }, MoreExecutors.directExecutor());
        return resultFuture;
    }
}
