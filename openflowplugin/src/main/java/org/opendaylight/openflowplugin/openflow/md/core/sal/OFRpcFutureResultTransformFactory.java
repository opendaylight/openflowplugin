/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.util.Collection;

import org.opendaylight.controller.sal.common.util.Rpcs;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * collection of transformation functions dedicated to rpc future results  
 */
public abstract class OFRpcFutureResultTransformFactory {
    
    protected static Logger LOG = LoggerFactory
            .getLogger(OFRpcFutureResultTransformFactory.class);

    /**
     * @return translator from {@link UpdateFlowOutput} to {@link AddFlowOutput}
     */
    public static Function<RpcResult<UpdateFlowOutput>,RpcResult<AddFlowOutput>> createForAddFlowOutput() {
        return new Function<RpcResult<UpdateFlowOutput>,RpcResult<AddFlowOutput>>() {

            @Override
            public RpcResult<AddFlowOutput> apply(final RpcResult<UpdateFlowOutput> input) {

                UpdateFlowOutput updateFlowOutput = input.getResult();

                AddFlowOutputBuilder addFlowOutput = new AddFlowOutputBuilder();
                addFlowOutput.setTransactionId(updateFlowOutput.getTransactionId());
                AddFlowOutput result = addFlowOutput.build();

                RpcResult<AddFlowOutput> rpcResult = assembleRpcResult(input, result);
                LOG.debug("Returning the Add Flow RPC result to MD-SAL");
                return rpcResult;
            }

        };
    }
    
    
    /**
     * @param input
     * @param result
     * @return
     */
    protected static <E> RpcResult<E> assembleRpcResult(RpcResult<?> input, E result) {
        Collection<RpcError> errors = input.getErrors();
        RpcResult<E> rpcResult = Rpcs.getRpcResult(input.isSuccessful(), result, errors);
        return rpcResult;
    }
}
