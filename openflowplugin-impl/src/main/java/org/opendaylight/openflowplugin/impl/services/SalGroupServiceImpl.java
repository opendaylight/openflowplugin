/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import java.util.concurrent.Future;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.rpc.ItemLifeCycleSource;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowDescriptorFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class SalGroupServiceImpl implements SalGroupService, ItemLifeCycleSource {
    private static final Logger LOG = LoggerFactory.getLogger(SalGroupServiceImpl.class);
    private final GroupService<AddGroupInput, AddGroupOutput> addGroup;
    private final GroupService<Group, UpdateGroupOutput> updateGroup;
    private final GroupService<RemoveGroupInput, RemoveGroupOutput> removeGroup;
    private final DeviceContext deviceContext;
    private ItemLifecycleListener itemLifecycleListener;

    public SalGroupServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        this.deviceContext = deviceContext;
        addGroup = new GroupService<>(requestContextStack, deviceContext, AddGroupOutput.class);
        updateGroup = new GroupService<>(requestContextStack, deviceContext, UpdateGroupOutput.class);
        removeGroup = new GroupService<>(requestContextStack, deviceContext, RemoveGroupOutput.class);
    }

    @Override
    public void setItemLifecycleListener(@Nullable ItemLifecycleListener itemLifecycleListener) {
        this.itemLifecycleListener = itemLifecycleListener;
    }

    @Override
    public Future<RpcResult<AddGroupOutput>> addGroup(final AddGroupInput input) {
        addGroup.getDeviceContext().getDeviceGroupRegistry().store(input.getGroupId());
        final ListenableFuture<RpcResult<AddGroupOutput>> resultFuture = addGroup.handleServiceCall(input);
        Futures.addCallback(resultFuture, new FutureCallback<RpcResult<AddGroupOutput>>() {
            @Override
            public void onSuccess(RpcResult<AddGroupOutput> result) {
                if (result.isSuccessful()) {
                    if(LOG.isDebugEnabled()) {
                        LOG.debug("group add finished without error, id={} from device {}", input.getGroupId().getValue(),
                            deviceContext.getPrimaryConnectionContext().getConnectionAdapter().getRemoteAddress());
                    }
                    addIfNecessaryToDS(input.getGroupId(), input);
                } else {
                    if(LOG.isDebugEnabled()) {
                        LOG.debug("group add finished with error, id={} from device {}", input.getGroupId().getValue(),
                            deviceContext.getPrimaryConnectionContext().getConnectionAdapter().getRemoteAddress());
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if(LOG.isDebugEnabled()) {
                    LOG.debug("group add failed, id={} from device {}", input.getGroupId().getValue(),
                        deviceContext.getPrimaryConnectionContext().getConnectionAdapter().getRemoteAddress());
                }
                LOG.error("group add failed for id={}. Exception: {}", input.getGroupId().getValue(), t);
            }
        });

        return resultFuture;
    }


    @Override
    public Future<RpcResult<UpdateGroupOutput>> updateGroup(final UpdateGroupInput input) {
        final ListenableFuture<RpcResult<UpdateGroupOutput>> resultFuture = updateGroup.handleServiceCall(input.getUpdatedGroup());
        Futures.addCallback(resultFuture, new FutureCallback<RpcResult<UpdateGroupOutput>>() {
            @Override
            public void onSuccess(@Nullable RpcResult<UpdateGroupOutput> result) {
                if (result.isSuccessful()) {
                    if(LOG.isDebugEnabled()) {
                        LOG.debug("group update finished without error, id={} from device {}", input.getOriginalGroup().getGroupId(),
                            deviceContext.getPrimaryConnectionContext().getConnectionAdapter().getRemoteAddress());
                    }
                    removeIfNecessaryFromDS(input.getOriginalGroup().getGroupId());
                    addIfNecessaryToDS(input.getUpdatedGroup().getGroupId(), input.getUpdatedGroup());
                } else {
                    if(LOG.isDebugEnabled()) {
                        LOG.debug("group update finished with error, id={} from device {}", input.getOriginalGroup().getGroupId(),
                            deviceContext.getPrimaryConnectionContext().getConnectionAdapter().getRemoteAddress());
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if(LOG.isDebugEnabled()) {
                    LOG.debug("group update failed, id={} from device {}", input.getOriginalGroup().getGroupId(),
                        deviceContext.getPrimaryConnectionContext().getConnectionAdapter().getRemoteAddress());
                }
                LOG.debug("Group update failed for id={}. Exception: {}", input.getOriginalGroup().getGroupId(), t);
            }
        });
        return resultFuture;
    }

    @Override
    public Future<RpcResult<RemoveGroupOutput>> removeGroup(final RemoveGroupInput input) {
        removeGroup.getDeviceContext().getDeviceGroupRegistry().markToBeremoved(input.getGroupId());
        final ListenableFuture<RpcResult<RemoveGroupOutput>> resultFuture = removeGroup.handleServiceCall(input);
        Futures.addCallback(resultFuture, new FutureCallback<RpcResult<RemoveGroupOutput>>() {
            @Override
            public void onSuccess(@Nullable RpcResult<RemoveGroupOutput> result) {
                if (result.isSuccessful()) {
                    if(LOG.isDebugEnabled()) {
                        LOG.debug("group remove finished without error, id={} from device {}", input.getGroupId(),
                            deviceContext.getPrimaryConnectionContext().getConnectionAdapter().getRemoteAddress());
                    }

                    removeIfNecessaryFromDS(input.getGroupId());
                } else  {
                    if(LOG.isDebugEnabled()) {
                        LOG.debug("group remove finished with error, id={} from device {}", input.getGroupId(),
                            deviceContext.getPrimaryConnectionContext().getConnectionAdapter().getRemoteAddress());
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if(LOG.isDebugEnabled()) {
                    LOG.debug("group remove failed, id={} from device {}", input.getGroupId(),
                        deviceContext.getPrimaryConnectionContext().getConnectionAdapter().getRemoteAddress());
                }
                LOG.error("Group remove failed for id={}. Exception: {}", input.getGroupId(), t);
            }
        });
        return resultFuture;
    }

    private void removeIfNecessaryFromDS(final GroupId groupId) {
        if (itemLifecycleListener != null) {
            KeyedInstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group, GroupKey> groupPath
                    = createGroupPath(groupId,
                    deviceContext.getDeviceState().getNodeInstanceIdentifier());
            itemLifecycleListener.onRemoved(groupPath);
        }
    }

    private void addIfNecessaryToDS(final GroupId groupId, final Group data) {
        if (itemLifecycleListener != null) {
            KeyedInstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group, GroupKey> groupPath
                    = createGroupPath(groupId,
                    deviceContext.getDeviceState().getNodeInstanceIdentifier());
            itemLifecycleListener.onAdded(groupPath, new GroupBuilder(data).build());
        }
    }

    static KeyedInstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group, GroupKey> createGroupPath(final GroupId groupId, final KeyedInstanceIdentifier<Node, NodeKey> nodePath) {
        return nodePath.augmentation(FlowCapableNode.class).child(org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group.class, new GroupKey(groupId));
    }
}
