/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcFutureResultTransformFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcTask;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcTaskFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;

public class SalFlowServiceImpl extends CommonService implements SalFlowService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalFlowServiceImpl.class);

    @Override
    public Future<RpcResult<AddFlowOutput>> addFlow(final AddFlowInput input) {
        LOG.debug("Calling the FlowMod RPC method on MessageDispatchService");
        // use primary connection
        final SwitchConnectionDistinguisher cookie = null;

        final OFRpcTask<AddFlowInput, RpcResult<UpdateFlowOutput>> task = OFRpcTaskFactory.createAddFlowTask(
                rpcTaskContext, input, cookie);
        final ListenableFuture<RpcResult<UpdateFlowOutput>> result = task.submit();

        return Futures.transform(result, OFRpcFutureResultTransformFactory.createForAddFlowOutput());
    }

    @Override
    public Future<RpcResult<RemoveFlowOutput>> removeFlow(final RemoveFlowInput input) {
        LOG.debug("Calling the removeFlow RPC method on MessageDispatchService");

        // use primary connection
        final SwitchConnectionDistinguisher cookie = null;
        final OFRpcTask<RemoveFlowInput, RpcResult<UpdateFlowOutput>> task = OFRpcTaskFactory.createRemoveFlowTask(
                rpcTaskContext, input, cookie);
        final ListenableFuture<RpcResult<UpdateFlowOutput>> result = task.submit();

        return Futures.transform(result, OFRpcFutureResultTransformFactory.createForRemoveFlowOutput());
    }

    @Override
    public Future<RpcResult<UpdateFlowOutput>> updateFlow(final UpdateFlowInput input) {
        LOG.debug("Calling the updateFlow RPC method on MessageDispatchService");

        // use primary connection
        final SwitchConnectionDistinguisher cookie = null;

        final OFRpcTask<UpdateFlowInput, RpcResult<UpdateFlowOutput>> task = OFRpcTaskFactory.createUpdateFlowTask(
                rpcTaskContext, input, cookie);
        final ListenableFuture<RpcResult<UpdateFlowOutput>> result = task.submit();

        return result;
    }
}
