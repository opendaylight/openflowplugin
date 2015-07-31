package org.opendaylight.openflowplugin.impl.services;

import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SalGroupServiceImplTest extends ServiceMocking {

    private static final Long DUMMY_GROUP_ID = 15L;
    public SalGroupService salGroupService;

    @Mock
    DeviceGroupRegistry mockedDeviceGroupRegistry;

    @Override
    public void initialization() {
        super.initialization();
        salGroupService = new SalGroupServiceImpl(mockedRequestContextStack, mockedDeviceContext);
        when(mockedDeviceContext.getDeviceGroupRegistry()).thenReturn(mockedDeviceGroupRegistry);
    }

    @Test
    public void testAddGroup() throws Exception {
        final GroupId dummyGroupId = new GroupId(DUMMY_GROUP_ID);
        AddGroupInput addGroupInput = new AddGroupInputBuilder().setGroupId(dummyGroupId).build();

        salGroupService.addGroup(addGroupInput);
        verify(mockedRequestContextStack).createRequestContext();
        verify(mockedDeviceGroupRegistry).store(eq(dummyGroupId));
    }

    @Test
    public void testUpdateGroup() throws Exception {
        UpdatedGroup mockedUptatedGroup = mock(UpdatedGroup.class);
        final UpdateGroupInput updateGroupInput = new UpdateGroupInputBuilder().setUpdatedGroup(mockedUptatedGroup).build();
        salGroupService.updateGroup(updateGroupInput);
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testRemoveGroup() throws Exception {
        final GroupId dummyGroupId = new GroupId(DUMMY_GROUP_ID);
        RemoveGroupInput removeGroupInput = new RemoveGroupInputBuilder().setGroupId(dummyGroupId).build();

        salGroupService.removeGroup(removeGroupInput);
        verify(mockedRequestContextStack).createRequestContext();
        verify(mockedDeviceGroupRegistry).markToBeremoved(eq(dummyGroupId));
    }
}