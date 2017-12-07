/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import org.mockito.Mock;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.OriginalGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.OriginalGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;

public class SalGroupServiceImplTest extends ServiceMocking {

    private static final Long DUMMY_GROUP_ID = 15L;

    @Mock
    DeviceGroupRegistry mockedDeviceGroupRegistry;

    SalGroupServiceImpl salGroupService;

    @Override
    public void initialization() {
        super.initialization();
        when(mockedDeviceContext.getDeviceGroupRegistry()).thenReturn(mockedDeviceGroupRegistry);
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        salGroupService = new SalGroupServiceImpl(mockedRequestContextStack, mockedDeviceContext, convertorManager);
    }

    @Test
    public void testAddGroup() throws Exception {
        addGroup();
    }

    private void addGroup() {
        final GroupId dummyGroupId = new GroupId(DUMMY_GROUP_ID);
        AddGroupInput addGroupInput = new AddGroupInputBuilder().setGroupId(dummyGroupId).build();

        this.<AddGroupOutput>mockSuccessfulFuture();

        salGroupService.addGroup(addGroupInput);
        verify(mockedRequestContextStack).createRequestContext();

    }

    @Test
    public void testUpdateGroup() throws Exception {
        updateGroup();
    }



    private void updateGroup() {
        final UpdatedGroup updatedGroup = new UpdatedGroupBuilder().setGroupId(new GroupId(DUMMY_GROUP_ID)).build();
        final OriginalGroup originalGroup = new OriginalGroupBuilder().setGroupId(new GroupId(DUMMY_GROUP_ID)).build();
        final UpdateGroupInput updateGroupInput = new UpdateGroupInputBuilder()
                .setUpdatedGroup(updatedGroup).setOriginalGroup(originalGroup).build();

        this.<UpdateGroupOutput>mockSuccessfulFuture();

        salGroupService.updateGroup(updateGroupInput);
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testRemoveGroup() throws Exception {
        removeGroup();
    }

    private void removeGroup() throws Exception {
        final GroupId dummyGroupId = new GroupId(DUMMY_GROUP_ID);
        RemoveGroupInput removeGroupInput = new RemoveGroupInputBuilder().setGroupId(dummyGroupId).build();

        this.<RemoveGroupOutput>mockSuccessfulFuture();

        salGroupService.removeGroup(removeGroupInput);
        verify(mockedRequestContextStack).createRequestContext();

    }
}
