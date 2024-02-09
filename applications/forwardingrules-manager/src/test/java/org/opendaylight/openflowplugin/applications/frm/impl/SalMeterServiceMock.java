/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class SalMeterServiceMock implements SalMeterService {
    private final List<AddMeterInput> addMeterCalls = new ArrayList<>();
    private final List<RemoveMeterInput> removeMeterCalls = new ArrayList<>();
    private final List<UpdateMeterInput> updateMeterCalls = new ArrayList<>();

    @Override
    public ListenableFuture<RpcResult<AddMeterOutput>> addMeter(AddMeterInput input) {
        addMeterCalls.add(input);
        return RpcResultBuilder.success(new AddMeterOutputBuilder().build()).buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<RemoveMeterOutput>> removeMeter(RemoveMeterInput input) {
        removeMeterCalls.add(input);
        return RpcResultBuilder.success(new RemoveMeterOutputBuilder().build()).buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<UpdateMeterOutput>> updateMeter(UpdateMeterInput input) {
        updateMeterCalls.add(input);
        return RpcResultBuilder.success(new UpdateMeterOutputBuilder().build()).buildFuture();
    }

    public List<AddMeterInput> getAddMeterCalls() {
        return addMeterCalls;
    }

    public List<RemoveMeterInput> getRemoveMeterCalls() {
        return removeMeterCalls;
    }

    public List<UpdateMeterInput> getUpdateMeterCalls() {
        return updateMeterCalls;
    }
}
