/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.AddFlowsDsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.AddFlowsRpcInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.BulkFlowGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.RemoveFlowsDsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.RemoveFlowsRpcInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.SalBulkFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.service.rev150608.remove.flows.ds.input.FlowRemoveBulk;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Simple implementation providing bulk flows operations.
 */
public class SalBulkFlowServiceImpl implements SalBulkFlowService {

    private final SalFlowService flowService;
    private final DataBroker dataBroker;

    public SalBulkFlowServiceImpl(SalFlowService flowService, DataBroker dataBroker) {
        this.flowService = Preconditions.checkNotNull(flowService);
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
    }


    @Override
    public Future<RpcResult<Void>> addFlowsDs(AddFlowsDsInput input) {
        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        for (BulkFlowGrouping flow : input.getBulkFlowItem()) {
            InstanceIdentifier<Flow> flowPath = (InstanceIdentifier<Flow>) flow.getFlowRef().getValue();
            writeTransaction.put(LogicalDatastoreType.CONFIGURATION, flowPath, new FlowBuilder(flow).build());
        }
        CheckedFuture<Void, TransactionCommitFailedException> submitFuture = writeTransaction.submit();
        return handleResultFuture(submitFuture);
    }

    @Override
    public Future<RpcResult<Void>> removeFlowsDs(RemoveFlowsDsInput input) {
        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        for (FlowRemoveBulk flow : input.getFlowRemoveBulk()) {
            InstanceIdentifier<Flow> flowPath = (InstanceIdentifier<Flow>) flow.getFlowRef().getValue();
            writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, flowPath);
        }
        CheckedFuture<Void, TransactionCommitFailedException> submitFuture = writeTransaction.submit();
        return handleResultFuture(submitFuture);
    }

    private ListenableFuture<RpcResult<Void>> handleResultFuture(CheckedFuture<Void, TransactionCommitFailedException> submitFuture) {
        final SettableFuture<RpcResult<Void>> rpcResult = SettableFuture.create();
        Futures.addCallback(submitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                rpcResult.set(RpcResultBuilder.success(result).build());
            }

            @Override
            public void onFailure(Throwable t) {
                RpcResultBuilder<Void> rpcResultBld = RpcResultBuilder.<Void>failed()
                        .withRpcErrors(Collections.singleton(
                                RpcResultBuilder.newError(RpcError.ErrorType.APPLICATION, null, t.getMessage())
                        ));
                rpcResult.set(rpcResultBld.build());
            }
        });
        return rpcResult;
    }

    private <T> ListenableFuture<RpcResult<Void>> handleResultFuture(ListenableFuture<List<T>> submitFuture) {
        final SettableFuture<RpcResult<Void>> rpcResult = SettableFuture.create();
        Futures.addCallback(submitFuture, new FutureCallback<List<T>>() {
            @Override
            public void onSuccess(List<T> result) {
                rpcResult.set(RpcResultBuilder.success((Void) null).build());
            }

            @Override
            public void onFailure(Throwable t) {
                RpcResultBuilder<Void> rpcResultBld = RpcResultBuilder.<Void>failed()
                        .withRpcErrors(Collections.singleton(
                                RpcResultBuilder.newError(RpcError.ErrorType.APPLICATION, null, t.getMessage())
                        ));
                rpcResult.set(rpcResultBld.build());
            }
        });
        return rpcResult;
    }

    @Override
    public Future<RpcResult<Void>> addFlowsRpc(AddFlowsRpcInput input) {
        List<ListenableFuture<RpcResult<AddFlowOutput>>> bulkResults = new ArrayList<>();

        for (BulkFlowGrouping flowAddBulk : input.getBulkFlowItem()) {
            AddFlowInputBuilder flowInputBuilder = new AddFlowInputBuilder(flowAddBulk);
            final InstanceIdentifier<?> flowPath = flowAddBulk.getFlowRef().getValue();
            flowInputBuilder.setNode(new NodeRef(flowPath.firstIdentifierOf(Node.class)));
            flowInputBuilder.setTableId(flowPath.firstKeyOf(Table.class, TableKey.class).getId());
            Future<RpcResult<AddFlowOutput>> rpcAddFlowResult = flowService.addFlow(flowInputBuilder.build());
            bulkResults.add(JdkFutureAdapters.listenInPoolThread(rpcAddFlowResult));
        }
        return handleResultFuture(Futures.allAsList(bulkResults));
    }

    @Override
    public Future<RpcResult<Void>> removeFlowsRpc(RemoveFlowsRpcInput input) {
        List<ListenableFuture<RpcResult<RemoveFlowOutput>>> bulkResults = new ArrayList<>();

        for (BulkFlowGrouping flowAddBulk : input.getBulkFlowItem()) {
            RemoveFlowInputBuilder flowInputBuilder = new RemoveFlowInputBuilder(flowAddBulk);
            final InstanceIdentifier<?> flowPath = flowAddBulk.getFlowRef().getValue();
            flowInputBuilder.setNode(new NodeRef(flowPath.firstIdentifierOf(Node.class)));
            flowInputBuilder.setTableId(flowPath.firstKeyOf(Table.class, TableKey.class).getId());
            Future<RpcResult<RemoveFlowOutput>> rpcAddFlowResult = flowService.removeFlow(flowInputBuilder.build());
            bulkResults.add(JdkFutureAdapters.listenInPoolThread(rpcAddFlowResult));
        }
        return handleResultFuture(Futures.allAsList(bulkResults));
    }
}
