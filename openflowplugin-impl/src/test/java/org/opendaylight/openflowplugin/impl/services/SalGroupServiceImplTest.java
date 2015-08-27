package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.OriginalGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.OriginalGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SalGroupServiceImplTest extends ServiceMocking {

    private static final Long DUMMY_GROUP_ID = 15L;

    @Mock
    DeviceGroupRegistry mockedDeviceGroupRegistry;

    @Override
    public void initialization() {
        super.initialization();
        when(mockedDeviceContext.getDeviceGroupRegistry()).thenReturn(mockedDeviceGroupRegistry);
    }

    @Test
    public void testAddGroup() throws Exception {
        addGroup(null);
    }

    @Test
    public void testAddGroupWithItemLifecycle() throws Exception {
        addGroup(mock(ItemLifecycleListener.class));
    }

    private void addGroup(final ItemLifecycleListener itemLifecycleListener) {
        SalGroupServiceImpl salGroupService = new SalGroupServiceImpl(mockedRequestContextStack, mockedDeviceContext);

        final GroupId dummyGroupId = new GroupId(DUMMY_GROUP_ID);
        AddGroupInput addGroupInput = new AddGroupInputBuilder().setGroupId(dummyGroupId).build();

        ListenableFuture<RpcResult<AddGroupOutput>> dummySuccessfulFuture = Futures.immediateFuture(RpcResultBuilder.success((AddGroupOutput) null).build());
        when(mockedRequestContext.getFuture()).thenReturn(dummySuccessfulFuture);

        if (itemLifecycleListener != null) {
            salGroupService.setItemLifecycleListener(itemLifecycleListener);
        }

        salGroupService.addGroup(addGroupInput);
        verify(mockedRequestContextStack).createRequestContext();
        verify(mockedDeviceGroupRegistry).store(eq(dummyGroupId));

        if (itemLifecycleListener != null) {
            verify(itemLifecycleListener).onAdded(Matchers.<KeyedInstanceIdentifier<Group, GroupKey>>any(),Matchers.<Group>any());
        }
    }

    @Test
    public void testUpdateGroup() throws Exception {
        updateGroup(null);
    }

    @Test
    public void testUpdateGroupWithItemLifecycle() throws Exception {
        updateGroup(mock(ItemLifecycleListener.class));
    }

    private void updateGroup(final ItemLifecycleListener itemLifecycleListener) {
        SalGroupServiceImpl salGroupService = new SalGroupServiceImpl(mockedRequestContextStack, mockedDeviceContext);
        final UpdatedGroup updatedGroup = new UpdatedGroupBuilder().setGroupId(new GroupId(DUMMY_GROUP_ID)).build();
        final OriginalGroup originalGroup = new OriginalGroupBuilder().setGroupId(new GroupId(DUMMY_GROUP_ID)).build();
        final UpdateGroupInput updateGroupInput = new UpdateGroupInputBuilder().setUpdatedGroup(updatedGroup).setOriginalGroup(originalGroup).build();

        ListenableFuture<RpcResult<UpdateGroupOutput>> dummySuccessfulFuture = Futures.immediateFuture(RpcResultBuilder.success((UpdateGroupOutput) null).build());
        when(mockedRequestContext.getFuture()).thenReturn(dummySuccessfulFuture);

        if (itemLifecycleListener != null) {
            salGroupService.setItemLifecycleListener(itemLifecycleListener);
        }

        salGroupService.updateGroup(updateGroupInput);
        verify(mockedRequestContextStack).createRequestContext();

        if (itemLifecycleListener != null) {
            verify(itemLifecycleListener).onAdded(Matchers.<KeyedInstanceIdentifier<Group, GroupKey>>any(),Matchers.<Group>any());
            verify(itemLifecycleListener).onRemoved(Matchers.<KeyedInstanceIdentifier<Group, GroupKey>>any());
        }
    }


    @Test
    public void testRemoveGroup() throws Exception {
        removeGroup(null);
    }

    @Test
    public void testRemoveGroupWithItemLifecycle() throws Exception {
        removeGroup(mock(ItemLifecycleListener.class));
    }

    private void removeGroup(final ItemLifecycleListener itemLifecycleListener) throws Exception {
        SalGroupServiceImpl salGroupService = new SalGroupServiceImpl(mockedRequestContextStack, mockedDeviceContext);
        final GroupId dummyGroupId = new GroupId(DUMMY_GROUP_ID);
        RemoveGroupInput removeGroupInput = new RemoveGroupInputBuilder().setGroupId(dummyGroupId).build();

        ListenableFuture<RpcResult<RemoveGroupOutput>> dummySuccessfulFuture = Futures.immediateFuture(RpcResultBuilder.success((RemoveGroupOutput) null).build());
        when(mockedRequestContext.getFuture()).thenReturn(dummySuccessfulFuture);


        if (itemLifecycleListener != null) {
            salGroupService.setItemLifecycleListener(itemLifecycleListener);
        }

        salGroupService.removeGroup(removeGroupInput);
        verify(mockedRequestContextStack).createRequestContext();
        verify(mockedDeviceGroupRegistry).markToBeremoved(eq(dummyGroupId));

        if (itemLifecycleListener != null) {
            verify(itemLifecycleListener).onRemoved(Matchers.<KeyedInstanceIdentifier<Group, GroupKey>>any());
        }
    }
}