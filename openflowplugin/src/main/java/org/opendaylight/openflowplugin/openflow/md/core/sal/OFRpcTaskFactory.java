/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.GroupConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.MeterConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowAddedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupAddedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterAddedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInputBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;

import java.math.BigInteger;
import java.util.Collection;
import java.util.concurrent.Future;

/**
 *
 */
public abstract class OFRpcTaskFactory {

    /**
     * @param taskContext 
     * @param input 
     * @param cookie 
     * @return UpdateFlow task
     */
    public static OFRpcTask<AddFlowInput, RpcResult<UpdateFlowOutput>> createAddFlowTask(
            OFRpcTaskContext taskContext, AddFlowInput input, 
            SwitchConnectionDistinguisher cookie) {
        OFRpcTask<AddFlowInput, RpcResult<UpdateFlowOutput>> task = 
                new OFRpcTask<AddFlowInput, RpcResult<UpdateFlowOutput>>(taskContext, cookie, input) {
            
            @Override
            public ListenableFuture<RpcResult<UpdateFlowOutput>> call() {
                ListenableFuture<RpcResult<UpdateFlowOutput>> result = SettableFuture.create();
                
                Collection<RpcError> barrierErrors = OFRpcTaskUtil.manageBarrier(getTaskContext(), getInput().isBarrier(), getCookie());
                if (!barrierErrors.isEmpty()) {
                    OFRpcTaskUtil.wrapBarrierErrors(((SettableFuture<RpcResult<UpdateFlowOutput>>) result), barrierErrors);
                } else {
                    // Convert the AddFlowInput to FlowModInput
                    FlowModInputBuilder ofFlowModInput = FlowConvertor.toFlowModInput(getInput(), 
                            getVersion(), getSession().getFeatures().getDatapathId());
                    final Long xId = getSession().getNextXid();
                    ofFlowModInput.setXid(xId);
                    
                    Future<RpcResult<UpdateFlowOutput>> resultFromOFLib = 
                            getMessageService().flowMod(ofFlowModInput.build(), getCookie());
                    result = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
                    
                    OFRpcTaskUtil.hookFutureNotification(this, result, 
                            getRpcNotificationProviderService(), createFlowAddedNotification(xId, getInput()));
                }

                return result;
            }
        };
        
        return task;
    }

    /**
     * @param xId
     * @return
     */
    protected static NotificationComposer<FlowAdded> createFlowAddedNotification(
            final Long xId, final AddFlowInput input) {
        return new NotificationComposer<FlowAdded>() {
            @Override
            public FlowAdded compose() {
                FlowAddedBuilder newFlow = new FlowAddedBuilder((Flow) input);
                newFlow.setTransactionId(new TransactionId(BigInteger.valueOf(xId.intValue())));
                newFlow.setFlowRef(input.getFlowRef());
                return newFlow.build();
            }
        };
    }

    /**
     * @param taskContext 
     * @param input 
     * @param cookie 
     * @return UpdateFlow task
     */
    public static OFRpcTask<UpdateFlowInput, RpcResult<UpdateFlowOutput>> createUpdateFlowTask(
            OFRpcTaskContext taskContext, UpdateFlowInput input, 
            SwitchConnectionDistinguisher cookie) {
        
        OFRpcTask<UpdateFlowInput, RpcResult<UpdateFlowOutput>> task = 
                new OFRpcTask<UpdateFlowInput, RpcResult<UpdateFlowOutput>>(taskContext, cookie, input) {
            
            @Override
            public ListenableFuture<RpcResult<UpdateFlowOutput>> call() {
                ListenableFuture<RpcResult<UpdateFlowOutput>> result = null;
                Collection<RpcError> barrierErrors = OFRpcTaskUtil.manageBarrier(getTaskContext(), 
                        getInput().getUpdatedFlow().isBarrier(), getCookie());
                if (!barrierErrors.isEmpty()) {
                    OFRpcTaskUtil.wrapBarrierErrors(((SettableFuture<RpcResult<UpdateFlowOutput>>) result), barrierErrors);

                } else {
                    Flow flow = null;
                    Long xId = getSession().getNextXid();
                    boolean updatedFlow = (getInput().getUpdatedFlow().getMatch().equals(getInput().getOriginalFlow().getMatch())) &&
                            (getInput().getUpdatedFlow().getPriority().equals(getInput().getOriginalFlow().getPriority()));


                    if (updatedFlow == false) {
                        // if neither match nor priority matches, then we would need to remove the flow and add it
                        //remove flow
                        RemoveFlowInputBuilder removeflow = new RemoveFlowInputBuilder(getInput().getOriginalFlow());
                        FlowModInputBuilder ofFlowRemoveInput = FlowConvertor.toFlowModInput(removeflow.build(),
                                getVersion(),getSession().getFeatures().getDatapathId());
                        ofFlowRemoveInput.setXid(xId);
                        Future<RpcResult<UpdateFlowOutput>> resultFromOFLibRemove = getMessageService().
                                flowMod(ofFlowRemoveInput.build(), getCookie());
                        //add flow
                        AddFlowInputBuilder addFlow = new AddFlowInputBuilder(getInput().getUpdatedFlow());
                        flow = addFlow.build();
                    } else {
                        //update flow
                        flow = getInput().getUpdatedFlow();
                    }

                    FlowModInputBuilder ofFlowModInput = FlowConvertor.toFlowModInput(flow, getVersion(),
                            getSession().getFeatures().getDatapathId());

                    ofFlowModInput.setXid(xId);

                    Future<RpcResult<UpdateFlowOutput>> resultFromOFLib =
                            getMessageService().flowMod(ofFlowModInput.build(), getCookie());
                    result = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                    OFRpcTaskUtil.hookFutureNotification(this, result,
                            getRpcNotificationProviderService(), createFlowUpdatedNotification(xId, getInput()));
                }
                return result;
            }
        };
        return task;
    }

    /**
     * @param xId
     * @param input
     * @return
     */
    protected static NotificationComposer<FlowUpdated> createFlowUpdatedNotification(
            final Long xId, final UpdateFlowInput input) {
        return new NotificationComposer<FlowUpdated>() {
            @Override
            public FlowUpdated compose() {
                FlowUpdatedBuilder updFlow = new FlowUpdatedBuilder(input.getUpdatedFlow());
                updFlow.setTransactionId(new TransactionId(BigInteger.valueOf(xId.intValue())));
                updFlow.setFlowRef(input.getFlowRef());
                return updFlow.build();
            }
        };
    }
    
    /**
     * @param taskContext
     * @param input
     * @param cookie
     * @return update group task
     */
    public static OFRpcTask<AddGroupInput, RpcResult<UpdateGroupOutput>> createAddGroupTask(
            final OFRpcTaskContext taskContext, AddGroupInput input, 
            final SwitchConnectionDistinguisher cookie) {
        OFRpcTask<AddGroupInput, RpcResult<UpdateGroupOutput>> task = 
                new OFRpcTask<AddGroupInput, RpcResult<UpdateGroupOutput>>(taskContext, cookie, input) {
            
            @Override
            public ListenableFuture<RpcResult<UpdateGroupOutput>> call() {
                ListenableFuture<RpcResult<UpdateGroupOutput>> result = SettableFuture.create();
                
                Collection<RpcError> barrierErrors = OFRpcTaskUtil.manageBarrier(getTaskContext(), getInput().isBarrier(), getCookie());
                if (!barrierErrors.isEmpty()) {
                    OFRpcTaskUtil.wrapBarrierErrors(((SettableFuture<RpcResult<UpdateGroupOutput>>) result), barrierErrors);
                } else {
                    // Convert the AddGroupInput to GroupModInput
                    GroupModInputBuilder ofGroupModInput = GroupConvertor.toGroupModInput(getInput(), 
                            getVersion(), getSession().getFeatures().getDatapathId());
                    final Long xId = getSession().getNextXid();
                    ofGroupModInput.setXid(xId);
                    
                    Future<RpcResult<UpdateGroupOutput>> resultFromOFLib = getMessageService()
                            .groupMod(ofGroupModInput.build(), getCookie());
                    result = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
                    
                    OFRpcTaskUtil.hookFutureNotification(this, result, 
                            getRpcNotificationProviderService(), createGroupAddedNotification(xId, getInput()));
                }

                return result;
            }
        };
        
        return task;
    }
    

    /**
     * @param xId
     * @param input
     * @return
     */
    protected static NotificationComposer<GroupAdded> createGroupAddedNotification(
            final Long xId, final AddGroupInput input) {
        return new NotificationComposer<GroupAdded>() {
            @Override
            public GroupAdded compose() {
                GroupAddedBuilder groupMod = new GroupAddedBuilder((Group) input);
                groupMod.setTransactionId(new TransactionId(BigInteger.valueOf(xId.intValue())));
                groupMod.setGroupRef(input.getGroupRef());
                return groupMod.build();
            }
        };
    }

    /**
     * @param taskContext
     * @param input
     * @param cookie
     * @return update meter task
     */
    public static OFRpcTask<AddMeterInput, RpcResult<UpdateMeterOutput>> createAddMeterTask(
            OFRpcTaskContext taskContext, AddMeterInput input,
            SwitchConnectionDistinguisher cookie) {
        OFRpcTask<AddMeterInput, RpcResult<UpdateMeterOutput>> task = 
                new OFRpcTask<AddMeterInput, RpcResult<UpdateMeterOutput>>(taskContext, cookie, input) {
            
            @Override
            public ListenableFuture<RpcResult<UpdateMeterOutput>> call() {
                ListenableFuture<RpcResult<UpdateMeterOutput>> result = SettableFuture.create();
                
                Collection<RpcError> barrierErrors = OFRpcTaskUtil.manageBarrier(getTaskContext(), getInput().isBarrier(), getCookie());
                if (!barrierErrors.isEmpty()) {
                    OFRpcTaskUtil.wrapBarrierErrors(((SettableFuture<RpcResult<UpdateMeterOutput>>) result), barrierErrors);
                } else {
                    // Convert the AddGroupInput to GroupModInput
                    MeterModInputBuilder ofMeterModInput = MeterConvertor.toMeterModInput(getInput(), getVersion());
                    final Long xId = getSession().getNextXid();
                    ofMeterModInput.setXid(xId);
                    
                    Future<RpcResult<UpdateMeterOutput>> resultFromOFLib = getMessageService()
                            .meterMod(ofMeterModInput.build(), getCookie());
                    result = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
                    
                    OFRpcTaskUtil.hookFutureNotification(this, result, 
                            getRpcNotificationProviderService(), createMeterAddedNotification(xId, getInput()));
                }

                return result;
            }
        };
        
        return task;
        
    }

    /**
     * @param xId
     * @param input
     * @return
     */
    protected static NotificationComposer<MeterAdded> createMeterAddedNotification(
            final Long xId, final AddMeterInput input) {
        return new NotificationComposer<MeterAdded>() {
            @Override
            public MeterAdded compose() {
                MeterAddedBuilder meterMod = new MeterAddedBuilder((Meter) input);
                meterMod.setTransactionId(new TransactionId(BigInteger.valueOf(xId.intValue())));
                meterMod.setMeterRef(input.getMeterRef());
                return meterMod.build();
            }
        };
    }
    
    /**
     * @param taskContext 
     * @param input 
     * @param cookie 
     * @return UpdateFlow task
     */
    public static OFRpcTask<UpdateGroupInput, RpcResult<UpdateGroupOutput>> createUpdateGroupTask(
            OFRpcTaskContext taskContext, UpdateGroupInput input, 
            SwitchConnectionDistinguisher cookie) {
        OFRpcTask<UpdateGroupInput, RpcResult<UpdateGroupOutput>> task = 
                new OFRpcTask<UpdateGroupInput, RpcResult<UpdateGroupOutput>>(taskContext, cookie, input) {
            
            @Override
            public ListenableFuture<RpcResult<UpdateGroupOutput>> call() {
                ListenableFuture<RpcResult<UpdateGroupOutput>> result = null;
                Collection<RpcError> barrierErrors = OFRpcTaskUtil.manageBarrier(getTaskContext(), 
                        getInput().getUpdatedGroup().isBarrier(), getCookie());
                if (!barrierErrors.isEmpty()) {
                    OFRpcTaskUtil.wrapBarrierErrors(((SettableFuture<RpcResult<UpdateGroupOutput>>) result), barrierErrors);
                } else {
                    // Convert the UpdateGroupInput to GroupModInput
                    GroupModInputBuilder ofGroupModInput = GroupConvertor.toGroupModInput(
                            getInput().getUpdatedGroup(), getVersion(),
                            getSession().getFeatures().getDatapathId());
                    final Long xId = getSession().getNextXid();
                    ofGroupModInput.setXid(xId);
    
                    Future<RpcResult<UpdateGroupOutput>> resultFromOFLib = 
                            getMessageService().groupMod(ofGroupModInput.build(), getCookie());
                    result = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
                    
                    OFRpcTaskUtil.hookFutureNotification(this, result, 
                            getRpcNotificationProviderService(), createGroupUpdatedNotification(xId, getInput()));
                }
                return result;
            }
        };
        return task;
    }
    
    /**
     * @param xId
     * @param input
     * @return
     */
    protected static NotificationComposer<GroupUpdated> createGroupUpdatedNotification(
            final Long xId, final UpdateGroupInput input) {
        return new NotificationComposer<GroupUpdated>() {
            @Override
            public GroupUpdated compose() {
                GroupUpdatedBuilder groupMod = new GroupUpdatedBuilder(input.getUpdatedGroup());
                groupMod.setTransactionId(new TransactionId(BigInteger.valueOf(xId.intValue())));
                groupMod.setGroupRef(input.getGroupRef());
                return groupMod.build();
            }
        };
    }

    /**
     * @param taskContext 
     * @param input
     * @param cookie
     * @return update meter task 
     */
    public static OFRpcTask<UpdateMeterInput, RpcResult<UpdateMeterOutput>> createUpdateMeterTask(
            OFRpcTaskContext taskContext, UpdateMeterInput input,
            SwitchConnectionDistinguisher cookie) {
        OFRpcTask<UpdateMeterInput, RpcResult<UpdateMeterOutput>> task = 
                new OFRpcTask<UpdateMeterInput, RpcResult<UpdateMeterOutput>>(taskContext, cookie, input) {
            
            @Override
            public ListenableFuture<RpcResult<UpdateMeterOutput>> call() {
                ListenableFuture<RpcResult<UpdateMeterOutput>> result = null;
                Collection<RpcError> barrierErrors = OFRpcTaskUtil.manageBarrier(getTaskContext(), 
                        getInput().getUpdatedMeter().isBarrier(), getCookie());
                if (!barrierErrors.isEmpty()) {
                    OFRpcTaskUtil.wrapBarrierErrors(((SettableFuture<RpcResult<UpdateMeterOutput>>) result), barrierErrors);
                } else {
                    // Convert the UpdateMeterInput to MeterModInput
                    MeterModInputBuilder ofMeterModInput = MeterConvertor.toMeterModInput(
                            getInput().getUpdatedMeter(), getVersion());
                    final Long xId = getSession().getNextXid();
                    ofMeterModInput.setXid(xId);
    
                    Future<RpcResult<UpdateMeterOutput>> resultFromOFLib = 
                            getMessageService().meterMod(ofMeterModInput.build(), getCookie());
                    result = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
                    
                    OFRpcTaskUtil.hookFutureNotification(this, result,
                            getRpcNotificationProviderService(), createMeterUpdatedNotification(xId, getInput()));
                }
                return result;
            }
        };
        return task;
    }
    
    /**
     * @param xId
     * @param input
     * @return
     */
    protected static NotificationComposer<MeterUpdated> createMeterUpdatedNotification(
            final Long xId, final UpdateMeterInput input) {
        return new NotificationComposer<MeterUpdated>() {
            @Override
            public MeterUpdated compose() {
                MeterUpdatedBuilder meterMod = new MeterUpdatedBuilder(input.getUpdatedMeter());
                meterMod.setTransactionId(new TransactionId(BigInteger.valueOf(xId.intValue())));
                meterMod.setMeterRef(input.getMeterRef());
                return meterMod.build();
            }
        };
    }
}
