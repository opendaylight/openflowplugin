/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock.util;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutputBuilder;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class SalFlowRpcsMock {
    private final List<AddFlowInput> addFlowCalls = new ArrayList<>();
    private final List<RemoveFlowInput> removeFlowCalls = new ArrayList<>();
    private final List<UpdateFlowInput> updateFlowCalls = new ArrayList<>();

    private ListenableFuture<RpcResult<AddFlowOutput>> addFlow(AddFlowInput input) {
        addFlowCalls.add(input);
        return RpcResultBuilder.success(new AddFlowOutputBuilder().build()).buildFuture();
    }

    private ListenableFuture<RpcResult<RemoveFlowOutput>> removeFlow(RemoveFlowInput input) {
        removeFlowCalls.add(input);
        return RpcResultBuilder.success(new RemoveFlowOutputBuilder().build()).buildFuture();
    }

    private ListenableFuture<RpcResult<UpdateFlowOutput>> updateFlow(UpdateFlowInput input) {
        updateFlowCalls.add(input);
        return RpcResultBuilder.success(new UpdateFlowOutputBuilder().build()).buildFuture();
    }

    public ClassToInstanceMap<Rpc<?,?>> getRpcClassToInstanceMap() {
        return ImmutableClassToInstanceMap.<Rpc<?, ?>>builder()
            .put(AddFlow.class, this::addFlow)
            .put(RemoveFlow.class, this::removeFlow)
            .put(UpdateFlow.class, this::updateFlow)
            .build();
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
