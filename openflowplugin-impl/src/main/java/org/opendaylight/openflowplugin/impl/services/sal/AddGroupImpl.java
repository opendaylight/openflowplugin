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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AddGroupImpl extends AbstractGroupRpc<AddGroupInput, AddGroupOutput> implements AddGroup {
    private static final Logger LOG = LoggerFactory.getLogger(AddGroupImpl.class);

    public AddGroupImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, convertorExecutor, AddGroupOutput.class);
    }

    @Override
    public ListenableFuture<RpcResult<AddGroupOutput>> invoke(final AddGroupInput input) {
        final var resultFuture = single.canUseSingleLayerSerialization() ? single.handleServiceCall(input)
            : multi.handleServiceCall(input);
        Futures.addCallback(resultFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(final RpcResult<AddGroupOutput> result) {
                if (result.isSuccessful()) {
                    LOG.debug("adding group successful {}", input.getGroupId());
                    deviceContext.getDeviceGroupRegistry().appendHistoryGroup(input.getGroupId(), input.getGroupType(),
                        FlowGroupStatus.ADDED);
                } else if (LOG.isDebugEnabled()) {
                    LOG.debug("Group add with id={} failed, errors={}", input.getGroupId().getValue(),
                        ErrorUtil.errorsToString(result.getErrors()));
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("Service call for adding group={} failed",
                          input.getGroupId().getValue(),
                          throwable);
            }
        }, MoreExecutors.directExecutor());
        return resultFuture;
    }
}
