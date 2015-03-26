/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import java.math.BigInteger;
import java.util.concurrent.Future;
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

public class SalMeterServiceImpl extends CommonService implements SalMeterService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalMeterServiceImpl.class);

    @Override
    public Future<RpcResult<AddMeterOutput>> addMeter(final AddMeterInput input) {
        return ServiceCallProcessingUtil.<AddMeterOutput> handleServiceCall(rpcContext, PRIMARY_CONNECTION,
                provideWaitTime(), new Function<Void>() {
                    @Override
                    public Future<RpcResult<Void>> apply(final BigInteger IDConnection) {
                        return convertAndSend(input, IDConnection);
                    }
                });
    }

    @Override
    public Future<RpcResult<UpdateMeterOutput>> updateMeter(final UpdateMeterInput input) {
        return ServiceCallProcessingUtil.<UpdateMeterOutput> handleServiceCall(rpcContext, PRIMARY_CONNECTION,
                provideWaitTime(), new Function<Void>() {
                    @Override
                    public Future<RpcResult<Void>> apply(final BigInteger IDConnection) {
                        return convertAndSend(input.getUpdatedMeter(), IDConnection);
                    }
                });
    }

    @Override
    public Future<RpcResult<RemoveMeterOutput>> removeMeter(final RemoveMeterInput input) {
        return ServiceCallProcessingUtil.<RemoveMeterOutput> handleServiceCall(rpcContext, PRIMARY_CONNECTION,
                provideWaitTime(), new Function<Void>() {
                    @Override
                    public Future<RpcResult<Void>> apply(final BigInteger IDConnection) {
                        return convertAndSend(input, IDConnection);
                    }
                });
    }

    Future<RpcResult<Void>> convertAndSend(final Meter iputMeter, final BigInteger IDConnection) {
        final MeterModInputBuilder ofMeterModInput = MeterConvertor.toMeterModInput(iputMeter, version);
        ofMeterModInput.setXid(deviceContext.getNextXid().getValue());
        return provideConnectionAdapter(IDConnection).meterMod(ofMeterModInput.build());
    }
}
