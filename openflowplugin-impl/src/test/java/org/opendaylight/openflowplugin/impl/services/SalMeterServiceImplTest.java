package org.opendaylight.openflowplugin.impl.services;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.OriginalMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.OriginalMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.UpdatedMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.UpdatedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class SalMeterServiceImplTest extends ServiceMocking {

    private static final Long DUMMY_METER_ID = 15L;
    private static final Long DUMMY_METTER_ID = 2000L;

    @Mock
    DeviceMeterRegistry mockedDeviceMeterRegistry;

    @Override
    public void initialization() {
        super.initialization();
        when(mockedDeviceContext.getDeviceMeterRegistry()).thenReturn(mockedDeviceMeterRegistry);
    }

    @Test
    public void testAddMeter() throws Exception {
        addMeter(null);
    }

    @Test
    public void testAddMeterWithItemLifecycle() throws Exception {
        addMeter(mock(ItemLifecycleListener.class));
    }

    private void addMeter(final ItemLifecycleListener itemLifecycleListener) {
        final SalMeterServiceImpl salMeterService = new SalMeterServiceImpl(mockedRequestContextStack, mockedDeviceContext);
        final MeterId dummyMeterId = new MeterId(DUMMY_METER_ID);
        AddMeterInput addMeterInput = new AddMeterInputBuilder().setMeterId(dummyMeterId).build();

        this.<AddMeterOutput>mockSuccessfulFuture();

        if (itemLifecycleListener != null) {
            salMeterService.setItemLifecycleListener(itemLifecycleListener);
        }

        salMeterService.addMeter(addMeterInput);
        verify(mockedRequestContextStack).createRequestContext();
        verify(mockedDeviceMeterRegistry).store(eq(dummyMeterId));

        if (itemLifecycleListener != null) {
            verify(itemLifecycleListener).onAdded(Matchers.<KeyedInstanceIdentifier<Meter, MeterKey>>any(),Matchers.<Meter>any());
        }
    }

    @Test
    public void testUpdateMeter() throws Exception {
        updateMeter(null);
    }

    @Test
    public void testUpdateMeterWithItemLifecycle() throws Exception {
        updateMeter(mock(ItemLifecycleListener.class));
    }

    private void updateMeter(final ItemLifecycleListener itemLifecycleListener) throws Exception {
        final SalMeterServiceImpl salMeterService = new SalMeterServiceImpl(mockedRequestContextStack, mockedDeviceContext);
        final UpdatedMeter dummyUpdatedMeter = new UpdatedMeterBuilder().setMeterId(new MeterId(DUMMY_METTER_ID)).build();
        final OriginalMeter dummyOriginalMeter = new OriginalMeterBuilder().setMeterId(new MeterId(DUMMY_METTER_ID)).build();

        final UpdateMeterInput updateMeterInput = new UpdateMeterInputBuilder().setUpdatedMeter(dummyUpdatedMeter).setOriginalMeter(dummyOriginalMeter).build();

        this.<AddMeterOutput>mockSuccessfulFuture();

        if (itemLifecycleListener != null) {
            salMeterService.setItemLifecycleListener(itemLifecycleListener);
        }

        salMeterService.updateMeter(updateMeterInput);
        verify(mockedRequestContextStack).createRequestContext();

        if (itemLifecycleListener != null) {
            verify(itemLifecycleListener).onAdded(Matchers.<KeyedInstanceIdentifier<Meter, MeterKey>>any(),Matchers.<Meter>any());
            verify(itemLifecycleListener).onRemoved(Matchers.<KeyedInstanceIdentifier<Meter, MeterKey>>any());
        }
    }

    @Test
    public void testRemoveMeter() throws Exception {
        removeMeter(null);
    }

    @Test
    public void testRemoveMeterWithItemLifecycle() throws Exception {
        removeMeter(mock(ItemLifecycleListener.class));
    }

    private void removeMeter(final ItemLifecycleListener itemLifecycleListener) throws Exception {
        final SalMeterServiceImpl salMeterService = new SalMeterServiceImpl(mockedRequestContextStack, mockedDeviceContext);
        final MeterId dummyMeterId = new MeterId(DUMMY_METER_ID);
        RemoveMeterInput removeMeterInput = new RemoveMeterInputBuilder().setMeterId(dummyMeterId).build();

        this.<RemoveMeterOutput>mockSuccessfulFuture();

        if (itemLifecycleListener != null) {
            salMeterService.setItemLifecycleListener(itemLifecycleListener);
        }

        salMeterService.removeMeter(removeMeterInput);
        verify(mockedRequestContextStack).createRequestContext();
        verify(mockedDeviceMeterRegistry).markToBeremoved(eq(dummyMeterId));

        if (itemLifecycleListener != null) {
            verify(itemLifecycleListener).onRemoved(Matchers.<KeyedInstanceIdentifier<Meter, MeterKey>>any());
        }
    }
}