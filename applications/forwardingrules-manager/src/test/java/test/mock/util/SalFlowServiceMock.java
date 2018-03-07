/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.*;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class SalFlowServiceMock implements SalFlowService {
    private final List<AddFlowInput> addFlowCalls = new ArrayList<>();
    private final List<RemoveFlowInput> removeFlowCalls = new ArrayList<>();
    private final List<UpdateFlowInput> updateFlowCalls = new ArrayList<>();

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
