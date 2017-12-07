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
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.OriginalMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.OriginalMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.UpdatedMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.UpdatedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;

public class SalMeterServiceImplTest extends ServiceMocking {

    private static final Long DUMMY_METER_ID = 15L;
    private static final Long DUMMY_METTER_ID = 2000L;

    @Mock
    DeviceMeterRegistry mockedDeviceMeterRegistry;

    SalMeterServiceImpl salMeterService;

    @Override
    public void initialization() {
        super.initialization();
        when(mockedDeviceContext.getDeviceMeterRegistry()).thenReturn(mockedDeviceMeterRegistry);
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        salMeterService = new SalMeterServiceImpl(mockedRequestContextStack, mockedDeviceContext, convertorManager);
    }

    @Test
    public void testAddMeter() throws Exception {
        addMeter();
    }


    private void addMeter() {
        final MeterId dummyMeterId = new MeterId(DUMMY_METER_ID);
        AddMeterInput addMeterInput = new AddMeterInputBuilder().setMeterId(dummyMeterId).build();

        this.<AddMeterOutput>mockSuccessfulFuture();


        salMeterService.addMeter(addMeterInput);
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testUpdateMeter() throws Exception {
        updateMeter();
    }

    private void updateMeter() throws Exception {
        final UpdatedMeter dummyUpdatedMeter = new UpdatedMeterBuilder()
                .setMeterId(new MeterId(DUMMY_METTER_ID)).build();
        final OriginalMeter dummyOriginalMeter = new OriginalMeterBuilder()
                .setMeterId(new MeterId(DUMMY_METTER_ID)).build();

        final UpdateMeterInput updateMeterInput = new UpdateMeterInputBuilder()
                .setUpdatedMeter(dummyUpdatedMeter).setOriginalMeter(dummyOriginalMeter).build();

        this.<AddMeterOutput>mockSuccessfulFuture();


        salMeterService.updateMeter(updateMeterInput);
        verify(mockedRequestContextStack).createRequestContext();

    }

    @Test
    public void testRemoveMeter() throws Exception {
        removeMeter();
    }


    private void removeMeter() throws Exception {
        final MeterId dummyMeterId = new MeterId(DUMMY_METER_ID);
        RemoveMeterInput removeMeterInput = new RemoveMeterInputBuilder().setMeterId(dummyMeterId).build();

        this.<RemoveMeterOutput>mockSuccessfulFuture();

        salMeterService.removeMeter(removeMeterInput);
        verify(mockedRequestContextStack).createRequestContext();
    }
}
