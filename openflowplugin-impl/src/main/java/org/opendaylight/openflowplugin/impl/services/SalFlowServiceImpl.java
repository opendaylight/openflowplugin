/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcFutureResultTransformFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowConvertor;
import org.opendaylight.openflowplugin.openflow.md.util.FlowCreatorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;

public class SalFlowServiceImpl extends CommonService implements SalFlowService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalFlowServiceImpl.class);

    public SalFlowServiceImpl(final RpcContext rpcContext, final short version, final BigInteger datapathId,
            final IMessageDispatchService service, final Xid xid, final SwitchConnectionDistinguisher cookie) {
        // TODO set cookie
        super(rpcContext, version, datapathId, service, xid, cookie);
    }

    public SalFlowServiceImpl(final RpcContext rpcContext) {
        this.rpcContext = rpcContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService#addFlow(org.opendaylight.
     * yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput)
     */
    @Override
    public Future<RpcResult<AddFlowOutput>> addFlow(final AddFlowInput input) {
        LOG.debug("Calling the FlowMod RPC method on MessageDispatchService");
        // use primary connection
        final SwitchConnectionDistinguisher cookie = null;

        ListenableFuture<RpcResult<UpdateFlowOutput>> result = SettableFuture.create();

        // Convert the AddFlowInput to FlowModInput
        final List<FlowModInputBuilder> ofFlowModInputs = FlowConvertor.toFlowModInputs(input, version, datapathId);
        LOG.debug("Number of flows to push to switch: {}", ofFlowModInputs.size());
        result = chainFlowMods(ofFlowModInputs, 0, xid, cookie);
        result = chainFutureBarrier(result);
        hookFutureNotification(result, notificationProviderService, createFlowAddedNotification(input));

        return Futures.transform(result, OFRpcFutureResultTransformFactory.createForAddFlowOutput());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService#removeFlow(org.opendaylight
     * .yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput)
     */
    @Override
    public Future<RpcResult<RemoveFlowOutput>> removeFlow(final RemoveFlowInput input) {
        ListenableFuture<RpcResult<UpdateFlowOutput>> result = SettableFuture.create();

        // Convert the AddFlowInput to FlowModInput
        final FlowModInputBuilder ofFlowModInput = FlowConvertor.toFlowModInput(input, version, datapathId);
        final Long xId = rpcContext.getDeviceContext().getNextXid().getValue();
        ofFlowModInput.setXid(xId);

        final Future<RpcResult<UpdateFlowOutput>> resultFromOFLib = messageService.flowMod(ofFlowModInput.build(),
                cookie);
        result = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

        result = chainFutureBarrier(result);
        hookFutureNotification(result, notificationProviderService, createFlowRemovedNotification(input));

        return Futures.transform(result, OFRpcFutureResultTransformFactory.createForRemoveFlowOutput());

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService#updateFlow(org.opendaylight
     * .yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput)
     */
    @Override
    public Future<RpcResult<UpdateFlowOutput>> updateFlow(final UpdateFlowInput input) {
        ListenableFuture<RpcResult<UpdateFlowOutput>> result = null;

        final UpdateFlowInput in = input;
        final UpdatedFlow updated = in.getUpdatedFlow();
        final OriginalFlow original = in.getOriginalFlow();

        final List<FlowModInputBuilder> allFlowMods = new ArrayList<>();
        List<FlowModInputBuilder> ofFlowModInputs;

        if (!FlowCreatorUtil.canModifyFlow(original, updated, version)) {
            // We would need to remove original and add updated.

            // remove flow
            final RemoveFlowInputBuilder removeflow = new RemoveFlowInputBuilder(original);
            final List<FlowModInputBuilder> ofFlowRemoveInput = FlowConvertor.toFlowModInputs(removeflow.build(),
                    version, datapathId);
            // remove flow should be the first
            allFlowMods.addAll(ofFlowRemoveInput);
            final AddFlowInputBuilder addFlowInputBuilder = new AddFlowInputBuilder(updated);
            ofFlowModInputs = FlowConvertor.toFlowModInputs(addFlowInputBuilder.build(), version, datapathId);
        } else {
            ofFlowModInputs = FlowConvertor.toFlowModInputs(updated, version, datapathId);
        }

        allFlowMods.addAll(ofFlowModInputs);
        LOG.debug("Number of flows to push to switch: {}", allFlowMods.size());
        result = chainFlowMods(allFlowMods, 0, xid, cookie);

        result = chainFutureBarrier(result);
        hookFutureNotification(result, notificationProviderService, createFlowUpdatedNotification(in));
        return result;
    }

}
