/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.singlelayer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

public class SingleLayerMeterServiceTest extends ServiceMocking {
    private static final long METER_ID = 42;
    private SingleLayerMeterService<AddMeterOutput> service;

    @Override
    protected void setup() throws Exception {
        service = new SingleLayerMeterService<>(mockedRequestContextStack,
                mockedDeviceContext, AddMeterOutput.class);
    }

    @Test
    public void buildRequest() throws Exception {
        final AddMeterInput input = new AddMeterInputBuilder()
                .setMeterId(new MeterId(METER_ID))
                .build();

        final OfHeader ofHeader = service.buildRequest(DUMMY_XID, input);
        assertEquals(MeterMessage.class, ofHeader.getImplementedInterface());

        final MeterMessage result = MeterMessage.class.cast(ofHeader);

        assertEquals(MeterModCommand.OFPMCADD, result.getCommand());
        assertEquals(METER_ID, result.getMeterId().getValue().longValue());
    }
}
