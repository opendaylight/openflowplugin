/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Future;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiLayerGroupService;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerGroupService;
import org.opendaylight.openflowplugin.impl.util.ErrorUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SalGroupServiceImpl implements SalGroupService {
    private static final Logger LOG = LoggerFactory.getLogger(SalGroupServiceImpl.class);
    private final MultiLayerGroupService<AddGroupInput, AddGroupOutput> addGroup;
    private final MultiLayerGroupService<Group, UpdateGroupOutput> updateGroup;
    private final MultiLayerGroupService<RemoveGroupInput, RemoveGroupOutput> removeGroup;
    private final SingleLayerGroupService<AddGroupOutput> addGroupMessage;
    private final SingleLayerGroupService<UpdateGroupOutput> updateGroupMessage;
    private final SingleLayerGroupService<RemoveGroupOutput> removeGroupMessage;
    private final DeviceContext deviceContext;

    public SalGroupServiceImpl(final RequestContextStack requestContextStack,
                               final DeviceContext deviceContext, final ConvertorExecutor convertorExecutor) {
        addGroup = new MultiLayerGroupService<>(requestContextStack, deviceContext,
                AddGroupOutput.class, convertorExecutor);
        updateGroup = new MultiLayerGroupService<>(requestContextStack, deviceContext,
                UpdateGroupOutput.class, convertorExecutor);
        removeGroup = new MultiLayerGroupService<>(requestContextStack, deviceContext,
                RemoveGroupOutput.class, convertorExecutor);

        addGroupMessage = new SingleLayerGroupService<>(requestContextStack,
                deviceContext, AddGroupOutput.class);
        updateGroupMessage = new SingleLayerGroupService<>(requestContextStack,
                deviceContext, UpdateGroupOutput.class);
        removeGroupMessage = new SingleLayerGroupService<>(requestContextStack,
                deviceContext, RemoveGroupOutput.class);
        this.deviceContext = deviceContext;
    }

    @Override
    public Future<RpcResult<AddGroupOutput>> addGroup(final AddGroupInput input) {
        final ListenableFuture<RpcResult<AddGroupOutput>> resultFuture = addGroupMessage
                .canUseSingleLayerSerialization()
            ? addGroupMessage.handleServiceCall(input)
            : addGroup.handleServiceCall(input);

        Futures.addCallback(resultFuture, new FutureCallback<RpcResult<AddGroupOutput>>() {
            @Override
            public void onSuccess(RpcResult<AddGroupOutput> result) {
                if (result.isSuccessful()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Group add with id={} finished without error", input.getGroupId().getValue());
                    }
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Group add with id={} failed, errors={}", input.getGroupId().getValue(),
                            ErrorUtil.errorsToString(result.getErrors()));
                    }
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.warn("Service call for adding group={} failed, reason: {}", input.getGroupId().getValue(),
                        throwable);
            }
        });
        return resultFuture;
    }


    @Override
    public Future<RpcResult<UpdateGroupOutput>> updateGroup(final UpdateGroupInput input) {
        final ListenableFuture<RpcResult<UpdateGroupOutput>> resultFuture = updateGroupMessage
                .canUseSingleLayerSerialization()
            ? updateGroupMessage.handleServiceCall(input.getUpdatedGroup())
            : updateGroup.handleServiceCall(input.getUpdatedGroup());

        Futures.addCallback(resultFuture, new FutureCallback<RpcResult<UpdateGroupOutput>>() {
            @Override
            public void onSuccess(RpcResult<UpdateGroupOutput> result) {
                if (result.isSuccessful()) {
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
            public void onFailure(Throwable throwable) {
                LOG.warn("Service call for updating group={} failed, reason: {}",
                        input.getOriginalGroup().getGroupId(), throwable);
            }
        });
        return resultFuture;
    }

    @Override
    public Future<RpcResult<RemoveGroupOutput>> removeGroup(final RemoveGroupInput input) {
        final ListenableFuture<RpcResult<RemoveGroupOutput>> resultFuture = removeGroupMessage
                .canUseSingleLayerSerialization()
            ? removeGroupMessage.handleServiceCall(input)
            : removeGroup.handleServiceCall(input);

        Futures.addCallback(resultFuture, new FutureCallback<RpcResult<RemoveGroupOutput>>() {
            @Override
            public void onSuccess(RpcResult<RemoveGroupOutput> result) {
                if (result.isSuccessful()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Group remove with id={} finished without error", input.getGroupId().getValue());
                    }
                } else {
                    LOG.warn("Group remove with id={} failed, errors={}", input.getGroupId().getValue(),
                        ErrorUtil.errorsToString(result.getErrors()));
                    LOG.debug("Group input={}", input);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.warn("Service call for removing group={} failed, reason: {}",
                        input.getGroupId().getValue(), throwable);
            }
        });
        return resultFuture;
    }
}
