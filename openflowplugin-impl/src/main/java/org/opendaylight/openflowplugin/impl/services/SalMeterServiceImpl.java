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
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class SalMeterServiceImpl implements SalMeterService {
    private final MeterService<AddMeterInput, AddMeterOutput> addMeter;
    private final MeterService<Meter, UpdateMeterOutput> updateMeter;
    private final MeterService<RemoveMeterInput, RemoveMeterOutput> removeMeter;

    public SalMeterServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        addMeter = new MeterService<>(requestContextStack, deviceContext, AddMeterOutput.class);
        updateMeter = new MeterService<>(requestContextStack, deviceContext, UpdateMeterOutput.class);
        removeMeter = new MeterService<>(requestContextStack, deviceContext, RemoveMeterOutput.class);
    }

    @Override
    public Future<RpcResult<AddMeterOutput>> addMeter(final AddMeterInput input) {
        addMeter.getDeviceContext().getDeviceMeterRegistry().store(input.getMeterId());

        return addMeter.handleServiceCall(input);
    }

    @Override
    public Future<RpcResult<UpdateMeterOutput>> updateMeter(final UpdateMeterInput input) {
        return updateMeter.handleServiceCall(input.getUpdatedMeter());
    }

    @Override
    public Future<RpcResult<RemoveMeterOutput>> removeMeter(final RemoveMeterInput input) {
        removeMeter.getDeviceContext().getDeviceMeterRegistry().markToBeremoved(input.getMeterId());
        return removeMeter.handleServiceCall(input);
    }
}
