/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.OriginalMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.UpdatedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yangtools.yang.common.Uint32;

public class SalMeterServiceImplTest extends ServiceMocking {
    private static final Uint32 DUMMY_METER_ID = Uint32.valueOf(15);
    private static final Uint32 DUMMY_METTER_ID = Uint32.valueOf(2000);

    @Mock
    private DeviceMeterRegistry mockedDeviceMeterRegistry;

    private AddMeterImpl addMeter;
    private RemoveMeterImpl removeMeter;
    private UpdateMeterImpl updateMeter;

    @Override
    protected void setup() {
        final var convertorManager = ConvertorManagerFactory.createDefaultManager();
        addMeter = new AddMeterImpl(mockedRequestContextStack, mockedDeviceContext, convertorManager);
        removeMeter = new RemoveMeterImpl(mockedRequestContextStack, mockedDeviceContext, convertorManager);
        updateMeter = new UpdateMeterImpl(mockedRequestContextStack, mockedDeviceContext, convertorManager);
    }

    @Test
    public void testAddMeter() {
        this.<AddMeterOutput>mockSuccessfulFuture();
        addMeter.invoke(new AddMeterInputBuilder().setMeterId(new MeterId(DUMMY_METER_ID)).build());
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testUpdateMeter() {
        this.<AddMeterOutput>mockSuccessfulFuture();
        updateMeter.invoke(new UpdateMeterInputBuilder()
            .setUpdatedMeter(new UpdatedMeterBuilder().setMeterId(new MeterId(DUMMY_METTER_ID)).build())
            .setOriginalMeter(new OriginalMeterBuilder().setMeterId(new MeterId(DUMMY_METTER_ID)).build())
            .build());
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testRemoveMeter() {
        this.<RemoveMeterOutput>mockSuccessfulFuture();
        removeMeter.invoke(new RemoveMeterInputBuilder().setMeterId(new MeterId(DUMMY_METER_ID)).build());
        verify(mockedRequestContextStack).createRequestContext();
    }
}