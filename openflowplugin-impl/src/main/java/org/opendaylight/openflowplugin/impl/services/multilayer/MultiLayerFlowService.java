/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.multilayer;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public final class MultiLayerFlowService<O extends DataObject> extends AbstractSimpleService<FlowModInputBuilder, O> {

    private final ConvertorExecutor convertorExecutor;
    private final VersionDatapathIdConvertorData data;

    public MultiLayerFlowService(final RequestContextStack requestContextStack, final DeviceContext deviceContext, final Class<O> clazz, final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, clazz);
        this.convertorExecutor = convertorExecutor;
        data = new VersionDatapathIdConvertorData(getVersion());
        data.setDatapathId(getDatapathId());
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final FlowModInputBuilder input) throws ServiceException {
        input.setXid(xid.getValue());
        return input.build();
    }

    public List<FlowModInputBuilder> toFlowModInputs(final Flow input) {
        final Optional<List<FlowModInputBuilder>> flowModInputBuilders = convertorExecutor.convert(input, data);
        return flowModInputBuilders.orElse(Collections.emptyList());
    }

    public ListenableFuture<RpcResult<O>> processFlowModInputBuilders(final List<FlowModInputBuilder> ofFlowModInputs) {
        final List<ListenableFuture<RpcResult<O>>> partialFutures = new ArrayList<>(ofFlowModInputs.size());

        for (final FlowModInputBuilder flowModInputBuilder : ofFlowModInputs) {
            partialFutures.add(handleServiceCall(flowModInputBuilder));
        }

        final ListenableFuture<List<RpcResult<O>>> allFutures = Futures.successfulAsList(partialFutures);
        final SettableFuture<RpcResult<O>> finalFuture = SettableFuture.create();
        Futures.addCallback(allFutures, new FutureCallback<List<RpcResult<O>>>() {
            @Override
            public void onSuccess(final List<RpcResult<O>> results) {
                final ArrayList<RpcError> errors = new ArrayList();
                for (RpcResult<O> flowModResult : results) {
                    if (flowModResult == null) {
                        errors.add(RpcResultBuilder.newError(
                                RpcError.ErrorType.PROTOCOL, OFConstants.APPLICATION_TAG,
                                "unexpected flowMod result (null) occurred"));
                    } else if (!flowModResult.isSuccessful()) {
                        errors.addAll(flowModResult.getErrors());
                    }
                }

                final RpcResultBuilder<O> rpcResultBuilder;
                if (errors.isEmpty()) {
                    rpcResultBuilder = RpcResultBuilder.success();
                } else {
                    rpcResultBuilder = RpcResultBuilder.<O>failed().withRpcErrors(errors);
                }

                finalFuture.set(rpcResultBuilder.build());
            }

            @Override
            public void onFailure(final Throwable t) {
                RpcResultBuilder<O> rpcResultBuilder = RpcResultBuilder.failed();
                finalFuture.set(rpcResultBuilder.build());
            }
        });

        return finalFuture;
    }

}
