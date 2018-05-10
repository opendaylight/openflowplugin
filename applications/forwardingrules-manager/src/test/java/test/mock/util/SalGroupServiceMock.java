/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock.util;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class SalGroupServiceMock implements SalGroupService {
    private final List<AddGroupInput> addGroupCalls = new ArrayList<>();
    private final List<RemoveGroupInput> removeGroupCalls = new ArrayList<>();
    private final List<UpdateGroupInput> updateGroupCalls = new ArrayList<>();

    @Override
    public ListenableFuture<RpcResult<AddGroupOutput>> addGroup(AddGroupInput input) {
        addGroupCalls.add(input);
        return null;
    }

    @Override
    public ListenableFuture<RpcResult<RemoveGroupOutput>> removeGroup(RemoveGroupInput input) {
        removeGroupCalls.add(input);
        return null;
    }

    @Override
    public ListenableFuture<RpcResult<UpdateGroupOutput>> updateGroup(UpdateGroupInput input) {
        updateGroupCalls.add(input);
        return null;
    }

    public List<AddGroupInput> getAddGroupCalls() {
        return addGroupCalls;
    }

    public List<RemoveGroupInput> getRemoveGroupCalls() {
        return removeGroupCalls;
    }

    public List<UpdateGroupInput> getUpdateGroupCalls() {
        return updateGroupCalls;
    }
}
