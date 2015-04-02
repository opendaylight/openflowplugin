/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import com.google.common.base.Function;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.MeterConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import java.util.concurrent.Future;

public class SalMeterServiceImpl extends CommonService implements SalMeterService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalMeterServiceImpl.class);

    @Override
    public Future<RpcResult<AddMeterOutput>> addMeter(final AddMeterInput input) {
        return this.<AddMeterOutput, Void>handleServiceCall( PRIMARY_CONNECTION,
                 new Function<DataCrate<AddMeterOutput>,Future<RpcResult<Void>>>() {
                    @Override
                    public Future<RpcResult<Void>> apply(final DataCrate<AddMeterOutput> data) {
                        return convertAndSend(input, data);
                    }
                });
    }

    @Override
    public Future<RpcResult<UpdateMeterOutput>> updateMeter(final UpdateMeterInput input) {
        return this.<UpdateMeterOutput, Void>handleServiceCall( PRIMARY_CONNECTION,
                 new Function<DataCrate<UpdateMeterOutput>,Future<RpcResult<Void>>>() {
                    @Override
                    public Future<RpcResult<Void>> apply(final DataCrate<UpdateMeterOutput> data) {
                        return convertAndSend(input.getUpdatedMeter(), data);
                    }
                });
    }

    @Override
    public Future<RpcResult<RemoveMeterOutput>> removeMeter(final RemoveMeterInput input) {
        return this.<RemoveMeterOutput, Void>handleServiceCall( PRIMARY_CONNECTION,
                 new Function<DataCrate<RemoveMeterOutput>,Future<RpcResult<Void>>>() {
                    @Override
                    public Future<RpcResult<Void>> apply(final DataCrate<RemoveMeterOutput> data) {
                        return convertAndSend(input, data);
                    }
                });
    }

    <T> Future<RpcResult<Void>> convertAndSend(final Meter iputMeter, final DataCrate<T> data) {
        final MeterModInputBuilder ofMeterModInput = MeterConvertor.toMeterModInput(iputMeter, version);
        Xid xid = deviceContext.getNextXid();
        ofMeterModInput.setXid(xid.getValue());
        data.getRequestContext().setXid(xid);
        return provideConnectionAdapter(data.getiDConnection()).meterMod(ofMeterModInput.build());
    }
}
