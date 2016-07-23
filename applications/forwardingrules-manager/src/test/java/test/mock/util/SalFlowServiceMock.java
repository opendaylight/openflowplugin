/*
 * Copyright (c) 2014, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class SalFlowServiceMock implements SalFlowService{
    private List<AddFlowInput> addFlowCalls = new ArrayList<>();
    private List<RemoveFlowInput> removeFlowCalls = new ArrayList<>();
    private List<UpdateFlowInput> updateFlowCalls = new ArrayList<>();

    @Override
    public Future<RpcResult<AddFlowOutput>> addFlow(AddFlowInput input) {
        addFlowCalls.add(input);
        return null;
    }


    @Override
    public Future<RpcResult<RemoveFlowOutput>> removeFlow(RemoveFlowInput input) {
        removeFlowCalls.add(input);
        return null;
    }

    @Override
    public Future<RpcResult<UpdateFlowOutput>> updateFlow(UpdateFlowInput input) {
        updateFlowCalls.add(input);
        return null;
    }

    public List<AddFlowInput> getAddFlowCalls() {
        return addFlowCalls;
    }

    public List<RemoveFlowInput> getRemoveFlowCalls() {
        return removeFlowCalls;
    }

    public List<UpdateFlowInput> getUpdateFlowCalls() {
        return updateFlowCalls;
    }
}
