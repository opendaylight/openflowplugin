/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import java.util.concurrent.Future;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;

public class SalMeterServiceImpl extends CommonService implements SalMeterService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalMeterServiceImpl.class);


    @Override
    public Future<RpcResult<AddMeterOutput>> addMeter(final AddMeterInput input) {
        // LOG.debug("Calling the MeterMod RPC method on MessageDispatchService");
        //
        // // use primary connection
        // final SwitchConnectionDistinguisher cookie = null;
        //
        // final OFRpcTask<AddMeterInput, RpcResult<UpdateMeterOutput>> task = OFRpcTaskFactory.createAddMeterTask(
        // rpcTaskContext, input, cookie);
        // final ListenableFuture<RpcResult<UpdateMeterOutput>> result = task.submit();
        //
        // return Futures.transform(result, OFRpcFutureResultTransformFactory.createForAddMeterOutput());
        return null;
    }

    @Override
    public Future<RpcResult<UpdateMeterOutput>> updateMeter(final UpdateMeterInput input) {
        // LOG.debug("Calling the MeterMod RPC method on MessageDispatchService");
        //
        // // use primary connection
        // final SwitchConnectionDistinguisher cookie = null;
        //
        // final OFRpcTask<UpdateMeterInput, RpcResult<UpdateMeterOutput>> task =
        // OFRpcTaskFactory.createUpdateMeterTask(
        // rpcTaskContext, input, cookie);
        // final ListenableFuture<RpcResult<UpdateMeterOutput>> result = task.submit();
        //
        // return result;
        return null;
    }

    @Override
    public Future<RpcResult<RemoveMeterOutput>> removeMeter(final RemoveMeterInput input) {
        // LOG.debug("Calling the Remove MeterMod RPC method on MessageDispatchService");
        //
        // final SwitchConnectionDistinguisher cookie = null;
        // final OFRpcTask<RemoveMeterInput, RpcResult<UpdateMeterOutput>> task =
        // OFRpcTaskFactory.createRemoveMeterTask(
        // rpcTaskContext, input, cookie);
        // final ListenableFuture<RpcResult<UpdateMeterOutput>> result = task.submit();
        //
        // return Futures.transform(result, OFRpcFutureResultTransformFactory.createForRemoveMeterOutput());
        return null;
    }

}
