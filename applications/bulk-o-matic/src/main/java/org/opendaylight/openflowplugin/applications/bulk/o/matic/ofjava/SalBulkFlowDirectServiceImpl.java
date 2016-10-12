/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.bulk.o.matic.ofjava;

import com.google.common.util.concurrent.Futures;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.direct.service.rev161011.FlowRpcAddMultipleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.direct.service.rev161011.FlowRpcAddTestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.direct.service.rev161011.SalBulkFlowDirectService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class SalBulkFlowDirectServiceImpl implements SalBulkFlowDirectService {

    private final FlowWriterDirectRpc flowWriter;

    public SalBulkFlowDirectServiceImpl(final DataBroker dataBroker,
                                        final OutboundQueue outboundQueue) {
        this.flowWriter = new FlowWriterDirectRpc(dataBroker, outboundQueue, new ForkJoinPool());
    }

    @Override
    public Future<RpcResult<Void>> flowRpcAddMultiple(FlowRpcAddMultipleInput input) {
        flowWriter.addFlows(
                input.getFlowCount().intValue(),
                input.getRpcBatchSize().intValue());

        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
    }

    @Override
    public Future<RpcResult<Void>> flowRpcAddTest(FlowRpcAddTestInput input) {
        flowWriter.addFlow(
                input.getDpnId(),
                input.getFlowCount().intValue(),
                input.getRpcBatchSize().intValue());

        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
    }
}
