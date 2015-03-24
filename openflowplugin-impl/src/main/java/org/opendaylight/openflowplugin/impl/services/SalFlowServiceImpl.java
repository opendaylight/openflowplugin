/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcFutureResultTransformFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcTask;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcTaskFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcTaskUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;

public class SalFlowServiceImpl extends CommonService implements SalFlowService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalFlowServiceImpl.class);

    public SalFlowServiceImpl(final RpcContext rpcContext, final short version, final BigInteger datapathId) {
        // TODO set cookie
        super(rpcContext, version, datapathId);
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

        final OFRpcTask<AddFlowInput, RpcResult<UpdateFlowOutput>> task = OFRpcTaskFactory.createAddFlowTask(
                rpcTaskContext, input, cookie);

        ListenableFuture<RpcResult<UpdateFlowOutput>> result = SettableFuture.create();

        // Convert the AddFlowInput to FlowModInput
        final List<FlowModInputBuilder> ofFlowModInputs = FlowConvertor.toFlowModInputs(input, version, datapathId);
        LOG.debug("Number of flows to push to switch: {}", ofFlowModInputs.size());
        result = chainFlowMods(ofFlowModInputs, 0, getTaskContext(), cookie);
        result = OFRpcTaskUtil.chainFutureBarrier(this, result);
        OFRpcTaskUtil.hookFutureNotification(this, result, getRpcNotificationProviderService(),
                createFlowAddedNotification(input));
        return result;

        final ListenableFuture<RpcResult<UpdateFlowOutput>> result = task.submit();

        return Futures.transform(result, OFRpcFutureResultTransformFactory.createForAddFlowOutput());

        final Future<RpcResult<AddFlowOutput>> result;
        // TODO: change default result to concrete future
        if (!rpcContext.isRequestContextCapacityEmpty()) {
            rpcContext.addNewRequest(result);
        }
        return result;
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
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

}
