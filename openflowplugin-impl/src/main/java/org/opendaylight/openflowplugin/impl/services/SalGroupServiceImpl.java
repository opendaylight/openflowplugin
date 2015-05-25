/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class SalGroupServiceImpl implements SalGroupService {
    private final GroupService<AddGroupInput, AddGroupOutput> addGroup;
    private final GroupService<Group, UpdateGroupOutput> updateGroup;
    private final GroupService<RemoveGroupInput, RemoveGroupOutput> removeGroup;

    public SalGroupServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        addGroup = new GroupService<>(requestContextStack, deviceContext, AddGroupOutput.class);
        updateGroup = new GroupService<>(requestContextStack, deviceContext, UpdateGroupOutput.class);
        removeGroup = new GroupService<>(requestContextStack, deviceContext, RemoveGroupOutput.class);
    }

    @Override
    public Future<RpcResult<AddGroupOutput>> addGroup(final AddGroupInput input) {
        addGroup.getDeviceContext().getDeviceGroupRegistry().store(input.getGroupId());
        return addGroup.handleServiceCall(input);
    }

    @Override
    public Future<RpcResult<UpdateGroupOutput>> updateGroup(final UpdateGroupInput input) {
        return updateGroup.handleServiceCall(input.getUpdatedGroup());
    }

    @Override
    public Future<RpcResult<RemoveGroupOutput>> removeGroup(final RemoveGroupInput input) {
        removeGroup.getDeviceContext().getDeviceGroupRegistry().markToBeremoved(input.getGroupId());
        return removeGroup.handleServiceCall(input);
    }
}
