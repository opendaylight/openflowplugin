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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
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
     * @param input
     * @param result
     * @return
     */
    protected static <E> RpcResult<E> assembleRpcResult(RpcResult<?> input, E result) {
        Collection<RpcError> errors = input.getErrors();
        RpcResult<E> rpcResult = Rpcs.getRpcResult(input.isSuccessful(), result, errors);
        return rpcResult;
    }

    /**
     * @return translator from {@link UpdateFlowOutput} to {@link AddFlowOutput}
     */
    public static Function<RpcResult<UpdateFlowOutput>,RpcResult<AddFlowOutput>> createForAddFlowOutput() {
        return new Function<RpcResult<UpdateFlowOutput>,RpcResult<AddFlowOutput>>() {

            @Override
            public RpcResult<AddFlowOutput> apply(RpcResult<UpdateFlowOutput> input) {

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
     * @return translator from {@link UpdateFlowOutput} to {@link RemoveFlowOutput}
     */
    public static Function<RpcResult<UpdateFlowOutput>,RpcResult<RemoveFlowOutput>> createForRemoveFlowOutput() {
        return new Function<RpcResult<UpdateFlowOutput>,RpcResult<RemoveFlowOutput>>() {

            @Override
            public RpcResult<RemoveFlowOutput> apply(RpcResult<UpdateFlowOutput> input) {

                UpdateFlowOutput updateFlowOutput = input.getResult();

                RemoveFlowOutputBuilder removeFlowOutput = new RemoveFlowOutputBuilder();
                removeFlowOutput.setTransactionId(updateFlowOutput.getTransactionId());
                RemoveFlowOutput result = removeFlowOutput.build();

                RpcResult<RemoveFlowOutput> rpcResult = assembleRpcResult(input, result);
                LOG.debug("Returning the Add Flow RPC result to MD-SAL");
                return rpcResult;
            }

        };
    }
    
    /**
     * @return translator from {@link UpdateGroupOutput} to {@link AddGroupOutput}
     */
    public static Function<RpcResult<UpdateGroupOutput>, RpcResult<AddGroupOutput>> createForAddGroupOutput() {
        return new Function<RpcResult<UpdateGroupOutput>,RpcResult<AddGroupOutput>>() {

            @Override
            public RpcResult<AddGroupOutput> apply(final RpcResult<UpdateGroupOutput> input) {
                UpdateGroupOutput updateGroupOutput = input.getResult();
                
                AddGroupOutputBuilder addGroupOutput = new AddGroupOutputBuilder();
                addGroupOutput.setTransactionId(updateGroupOutput.getTransactionId());
                AddGroupOutput result = addGroupOutput.build();

                RpcResult<AddGroupOutput> rpcResult = assembleRpcResult(input, result);
                LOG.debug("Returning the Add Group RPC result to MD-SAL");
                return rpcResult;
            }
        };
    }
    
    /**
     * @return
     */
    public static Function<RpcResult<UpdateGroupOutput>,RpcResult<RemoveGroupOutput>> createForRemoveGroupOutput() {
        return new Function<RpcResult<UpdateGroupOutput>,RpcResult<RemoveGroupOutput>>() {

            @Override
            public RpcResult<RemoveGroupOutput> apply(RpcResult<UpdateGroupOutput> input) {

                UpdateGroupOutput updateGroupOutput = input.getResult();

                RemoveGroupOutputBuilder removeGroupOutput = new RemoveGroupOutputBuilder();
                removeGroupOutput.setTransactionId(updateGroupOutput.getTransactionId());
                RemoveGroupOutput result = removeGroupOutput.build();

                RpcResult<RemoveGroupOutput> rpcResult = assembleRpcResult(input, result);
                LOG.debug("Returning the Add Flow RPC result to MD-SAL");
                return rpcResult;
            }

        };
    }
    
    /**
     * @return translator from {@link UpdateMeterOutput} to {@link AddMeterOutput}
     */
    public static Function<RpcResult<UpdateMeterOutput>, RpcResult<AddMeterOutput>> createForAddMeterOutput() {
        return new Function<RpcResult<UpdateMeterOutput>,RpcResult<AddMeterOutput>>() {

            @Override
            public RpcResult<AddMeterOutput> apply(final RpcResult<UpdateMeterOutput> input) {
                UpdateMeterOutput updateMeterOutput = input.getResult();
                
                AddMeterOutputBuilder addMeterOutput = new AddMeterOutputBuilder();
                addMeterOutput.setTransactionId(updateMeterOutput.getTransactionId());
                AddMeterOutput result = addMeterOutput.build();

                RpcResult<AddMeterOutput> rpcResult = assembleRpcResult(input, result);
                LOG.debug("Returning the Add Meter RPC result to MD-SAL");
                return rpcResult;
            }
        };
    }
    
    
    /**
     * @return
     */
    public static Function<RpcResult<UpdateMeterOutput>, RpcResult<RemoveMeterOutput>> createForRemoveMeterOutput() {
        return new Function<RpcResult<UpdateMeterOutput>,RpcResult<RemoveMeterOutput>>() {

            @Override
            public RpcResult<RemoveMeterOutput> apply(final RpcResult<UpdateMeterOutput> input) {
                UpdateMeterOutput updateMeterOutput = input.getResult();
                
                RemoveMeterOutputBuilder removeMeterOutput = new RemoveMeterOutputBuilder();
                removeMeterOutput.setTransactionId(updateMeterOutput.getTransactionId());
                RemoveMeterOutput result = removeMeterOutput.build();

                RpcResult<RemoveMeterOutput> rpcResult = assembleRpcResult(input, result);
                LOG.debug("Returning the Add Meter RPC result to MD-SAL");
                return rpcResult;
            }
        };
    }
    
    
    
    
}
