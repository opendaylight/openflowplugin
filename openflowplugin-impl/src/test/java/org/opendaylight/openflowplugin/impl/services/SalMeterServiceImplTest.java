package org.opendaylight.openflowplugin.impl.services;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.UpdatedMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;

public class SalMeterServiceImplTest extends ServiceMocking {

    private static final Long DUMMY_METER_ID = 15L;
    public SalMeterService salMeterService;

    @Mock
    DeviceMeterRegistry mockedDeviceMeterRegistry;

    @Override
    public void initialization() {
        super.initialization();
        salMeterService = new SalMeterServiceImpl(mockedRequestContextStack, mockedDeviceContext);
        when(mockedDeviceContext.getDeviceMeterRegistry()).thenReturn(mockedDeviceMeterRegistry);
    }

    @Test
    public void testAddMeter() throws Exception {
        final MeterId dummyMeterId = new MeterId(DUMMY_METER_ID);
        AddMeterInput addMeterInput = new AddMeterInputBuilder().setMeterId(dummyMeterId).build();

        salMeterService.addMeter(addMeterInput);
        verify(mockedRequestContextStack).createRequestContext();
        verify(mockedDeviceMeterRegistry).store(eq(dummyMeterId));
    }

    @Test
    public void testUpdateMeter() throws Exception {
        final UpdatedMeter mockedUpdatedMeter = mock(UpdatedMeter.class);
        Mockito.when(mockedUpdatedMeter.getMeterId()).thenReturn(new MeterId(43L));
        Mockito.doReturn(UpdatedMeter.class).when(mockedUpdatedMeter).getImplementedInterface();

        final UpdateMeterInput updateMeterInput = new UpdateMeterInputBuilder().setUpdatedMeter(mockedUpdatedMeter).build();
        salMeterService.updateMeter(updateMeterInput);
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testRemoveMeter() throws Exception {
        final MeterId dummyMeterId = new MeterId(DUMMY_METER_ID);
        RemoveMeterInput removeMeterInput = new RemoveMeterInputBuilder().setMeterId(dummyMeterId).build();

        salMeterService.removeMeter(removeMeterInput);
        verify(mockedRequestContextStack).createRequestContext();
        verify(mockedDeviceMeterRegistry).markToBeremoved(eq(dummyMeterId));
    }
}