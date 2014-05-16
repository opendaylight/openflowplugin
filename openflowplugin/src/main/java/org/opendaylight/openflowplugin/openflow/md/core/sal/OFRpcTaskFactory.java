/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.session.TransactionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;

import java.math.BigInteger;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public abstract class OFRpcTaskFactory {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(OFRpcTaskFactory.class);
    /**
     * @param maxTimeout
     * @param maxTimeoutUnit
     * @param helper
     * @return UpdateFlow task
     */
    public static OFRpcTask<AddFlowInput, RpcResult<UpdateFlowOutput>> createAddFlowTask(
            final long maxTimeout, final TimeUnit maxTimeoutUnit, final OFRpcTaskHelper helper) {
        OFRpcTask<AddFlowInput, RpcResult<UpdateFlowOutput>> task =
                new OFRpcTask<AddFlowInput, RpcResult<UpdateFlowOutput>>() {

            @Override
            public void run() {
                helper.rawBarrierSend(maxTimeout, maxTimeoutUnit, getInput().isBarrier(), getCookie(), getResult());
                if (getResult().isDone()) {
                    return;
                }

                // Convert the AddFlowInput to FlowModInput
                FlowModInputBuilder ofFlowModInput = FlowConvertor.toFlowModInput(getInput(),
                        getVersion(), getSession().getFeatures().getDatapathId());
                Long xId = getSession().getNextXid();
                ofFlowModInput.setXid(xId);

                if (null != getRpcNotificationProviderService()) {
                    FlowAddedBuilder newFlow = new FlowAddedBuilder(
                            (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow) getInput());
                    newFlow.setTransactionId(new TransactionId(BigInteger.valueOf(xId.intValue())));
                    newFlow.setFlowRef(getInput().getFlowRef());
                    getRpcNotificationProviderService().publish(newFlow.build());
                }

                getSession().getbulkTransactionCache().put(new TransactionKey(xId), getInput());
                Future<RpcResult<UpdateFlowOutput>> resultFromOFLib =
                        getMessageService().flowMod(ofFlowModInput.build(), getCookie());
                OFRpcTaskHelper.chainFutures(resultFromOFLib, getResult());
            }
        };
        return task;
    }

    /**
     * @param maxTimeout
     * @param maxTimeoutUnit
     * @param helper
     * @return UpdateFlow task
     */
    public static OFRpcTask<UpdateFlowInput, RpcResult<UpdateFlowOutput>> createUpdateFlowTask(
            final long maxTimeout, final TimeUnit maxTimeoutUnit, final OFRpcTaskHelper helper) {
        OFRpcTask<UpdateFlowInput, RpcResult<UpdateFlowOutput>> task =
                new OFRpcTask<UpdateFlowInput, RpcResult<UpdateFlowOutput>>() {

            @Override
            public void run() {
                helper.rawBarrierSend(maxTimeout, maxTimeoutUnit, getInput().getUpdatedFlow().isBarrier(), getCookie(), getResult());
                if (getResult().isDone()) {
                    return;
                }

                boolean updatedFlow = (getInput().getUpdatedFlow().getMatch().equals(getInput().getOriginalFlow().getMatch())) &&
                        (getInput().getUpdatedFlow().getPriority().equals(getInput().getOriginalFlow().getPriority()));

                Long xId = getSession().getNextXid();

                if (updatedFlow == false) {
                    RemoveFlowInputBuilder removeflow = new RemoveFlowInputBuilder(getInput().getOriginalFlow());
                    FlowModInputBuilder ofFlowModInput = FlowConvertor.toFlowModInput(removeflow.build(), getVersion(),getSession().getFeatures().getDatapathId());
                    removeflow.setStrict(true);
                    ofFlowModInput.setXid(xId);
                    Future<RpcResult<UpdateFlowOutput>> resultFromOFLibRemove = getMessageService().flowMod(ofFlowModInput.build(), getCookie());

                    // Convert the AddFlowInput to FlowModInput
                    AddFlowInputBuilder addFlow = new AddFlowInputBuilder(getInput().getUpdatedFlow());
                    FlowModInputBuilder ofFlowModInputAdd = FlowConvertor.toFlowModInput(addFlow.build(), getVersion(), getSession().getFeatures().getDatapathId());
                    ofFlowModInputAdd.setXid(xId);
                    getSession().getbulkTransactionCache().put(new TransactionKey(xId), getInput());
                    Future<RpcResult<UpdateFlowOutput>> resultFromOFLib = getMessageService().flowMod(ofFlowModInputAdd.build(), getCookie());
                    OFRpcTaskHelper.chainFutures(resultFromOFLib, getResult());
                }
                else {
                    // Convert the AddFlowInput to FlowModInput
                    FlowModInputBuilder ofFlowModInput = FlowConvertor.toFlowModInput(getInput().getUpdatedFlow(),
                            getVersion(), getSession().getFeatures().getDatapathId());
                    ofFlowModInput.setXid(xId);

                    if (null != getRpcNotificationProviderService()) {
                        FlowAddedBuilder newFlow = new FlowAddedBuilder(
                                (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow) getInput());
                        newFlow.setTransactionId(new TransactionId(BigInteger.valueOf(xId.intValue())));
                        newFlow.setFlowRef(getInput().getFlowRef());
                        getRpcNotificationProviderService().publish(newFlow.build());
                    }

                    getSession().getbulkTransactionCache().put(new TransactionKey(xId), getInput());
                    Future<RpcResult<UpdateFlowOutput>> resultFromOFLib =
                            getMessageService().flowMod(ofFlowModInput.build(), getCookie());
                    OFRpcTaskHelper.chainFutures(resultFromOFLib, getResult());
                }
            }
        };
        return task;
    }
    
}
