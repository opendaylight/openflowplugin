/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.controller.sal.common.util.Rpcs;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.GroupConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.MeterConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;

import com.google.common.util.concurrent.Futures;

/**
 * RPC implementation of MD-switch
 */
public class ModelDrivenSwitchImpl extends AbstractModelDrivenSwitch {

    private static final Logger LOG = org.slf4j.LoggerFactory
            .getLogger(ModelDrivenSwitchImpl.class);
    private final NodeId nodeId;
    private final IMessageDispatchService messageService ;
    private short version = 0;

    protected ModelDrivenSwitchImpl(NodeId nodeId,
            InstanceIdentifier<Node> identifier, SessionContext context) {
        super(identifier, context);
        this.nodeId = nodeId;
        messageService = sessionContext.getMessageDispatchService() ;
        version = context.getPrimaryConductor().getVersion();
    }

    @Override
    public Future<RpcResult<AddFlowOutput>> addFlow(AddFlowInput input) {
    	// Convert the AddFlowInput to FlowModInput
        FlowModInput ofFlowModInput = FlowConvertor.toFlowModInput(input, version);

    	// For Flow provisioning, the SwitchConnectionDistinguisher is set to null so
    	// the request can be routed through any connection to the switch

    	SwitchConnectionDistinguisher cookie = null ;

    	LOG.debug("Calling the FlowMod RPC method on MessageDispatchService");
       	Future<RpcResult<UpdateFlowOutput>> resultFromOFLib = messageService.flowMod(ofFlowModInput, cookie) ;

       	RpcResult<UpdateFlowOutput> rpcResultFromOFLib = null ;

    	try {
    		rpcResultFromOFLib = resultFromOFLib.get();
    	} catch( Exception ex ) {
    		LOG.error( " Error while getting result for AddFlow RPC" + ex.getMessage());
    	}

    	UpdateFlowOutput updateFlowOutput = rpcResultFromOFLib.getResult() ;

    	AddFlowOutputBuilder addFlowOutput = new AddFlowOutputBuilder() ;
    	addFlowOutput.setTransactionId(updateFlowOutput.getTransactionId()) ;
    	AddFlowOutput result = addFlowOutput.build();

    	Collection<RpcError> errors = rpcResultFromOFLib.getErrors() ;
        RpcResult<AddFlowOutput> rpcResult = Rpcs.getRpcResult(true, result, errors);

    	LOG.debug("Returning the Add Flow RPC result to MD-SAL");
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<AddGroupOutput>> addGroup(AddGroupInput input) {
    	// Convert the AddGroupInput to GroupModInput
        GroupModInput ofGroupModInput = GroupConvertor.toGroupModInput(input, version);


    	// For Flow provisioning, the SwitchConnectionDistinguisher is set to null so
    	// the request can be routed through any connection to the switch

    	SwitchConnectionDistinguisher cookie = null ;

    	LOG.debug("Calling the GroupMod RPC method on MessageDispatchService");
    	Future<RpcResult<UpdateGroupOutput>> resultFromOFLib = messageService.groupMod(ofGroupModInput, cookie) ;

    	RpcResult<UpdateGroupOutput> rpcResultFromOFLib = null ;

    	try {
    		rpcResultFromOFLib = resultFromOFLib.get();
    	} catch( Exception ex ) {
    		LOG.error( " Error while getting result for AddGroup RPC" + ex.getMessage());
    	}

    	UpdateGroupOutput updateGroupOutput = rpcResultFromOFLib.getResult() ;

    	AddGroupOutputBuilder addGroupOutput = new AddGroupOutputBuilder() ;
    	addGroupOutput.setTransactionId(updateGroupOutput.getTransactionId()) ;
    	AddGroupOutput result = addGroupOutput.build();

    	Collection<RpcError> errors = rpcResultFromOFLib.getErrors() ;
        RpcResult<AddGroupOutput> rpcResult = Rpcs.getRpcResult(true, result, errors);

        LOG.debug("Returning the Add Group RPC result to MD-SAL");
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<AddMeterOutput>> addMeter(AddMeterInput input) {
    	// Convert the AddMeterInput to MeterModInput
        MeterModInput ofMeterModInput = MeterConvertor.toMeterModInput(input, version);


    	// For Meter provisioning, the SwitchConnectionDistinguisher is set to null so
    	// the request can be routed through any connection to the switch

    	SwitchConnectionDistinguisher cookie = null ;

    	LOG.debug("Calling the MeterMod RPC method on MessageDispatchService");
    	Future<RpcResult<UpdateMeterOutput>> resultFromOFLib = messageService.meterMod(ofMeterModInput, cookie) ;

    	RpcResult<UpdateMeterOutput> rpcResultFromOFLib = null ;

    	try {
    		rpcResultFromOFLib = resultFromOFLib.get();
    	} catch( Exception ex ) {
    		LOG.error( " Error while getting result for AddMeter RPC" + ex.getMessage());
    	}

    	UpdateMeterOutput updateMeterOutput = rpcResultFromOFLib.getResult() ;

    	AddMeterOutputBuilder addMeterOutput = new AddMeterOutputBuilder() ;
    	addMeterOutput.setTransactionId(updateMeterOutput.getTransactionId()) ;
    	AddMeterOutput result = addMeterOutput.build();

    	Collection<RpcError> errors = rpcResultFromOFLib.getErrors() ;
        RpcResult<AddMeterOutput> rpcResult = Rpcs.getRpcResult(true, result, errors);

        LOG.debug("Returning the Add Meter RPC result to MD-SAL");
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<RemoveFlowOutput>> removeFlow(RemoveFlowInput input) {
    	// Convert the RemoveFlowInput to FlowModInput
        FlowModInput ofFlowModInput = FlowConvertor.toFlowModInput(input, version);


    	// For Flow provisioning, the SwitchConnectionDistinguisher is set to null so
    	// the request can be routed through any connection to the switch

    	SwitchConnectionDistinguisher cookie = null ;

    	LOG.debug("Calling the FlowMod RPC method on MessageDispatchService");
       	Future<RpcResult<UpdateFlowOutput>> resultFromOFLib = messageService.flowMod(ofFlowModInput, cookie) ;

       	RpcResult<UpdateFlowOutput> rpcResultFromOFLib = null ;

    	try {
    		rpcResultFromOFLib = resultFromOFLib.get();
    	} catch( Exception ex ) {
    		LOG.error( " Error while getting result for remove Flow RPC" + ex.getMessage());
    	}

    	UpdateFlowOutput updateFlowOutput = rpcResultFromOFLib.getResult() ;

    	RemoveFlowOutputBuilder removeFlowOutput = new RemoveFlowOutputBuilder() ;
    	removeFlowOutput.setTransactionId(updateFlowOutput.getTransactionId()) ;
    	RemoveFlowOutput result = removeFlowOutput.build();

    	Collection<RpcError> errors = rpcResultFromOFLib.getErrors() ;
        RpcResult<RemoveFlowOutput> rpcResult = Rpcs.getRpcResult(true, result, errors);

    	LOG.debug("Returning the Remove Flow RPC result to MD-SAL");
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<RemoveGroupOutput>> removeGroup(
            RemoveGroupInput input) {
    	// Convert the RemoveGroupInput to GroupModInput
        GroupModInput ofGroupModInput = GroupConvertor.toGroupModInput(input, version);


    	// For Flow provisioning, the SwitchConnectionDistinguisher is set to null so
    	// the request can be routed through any connection to the switch

    	SwitchConnectionDistinguisher cookie = null ;

    	LOG.debug("Calling the GroupMod RPC method on MessageDispatchService");
    	Future<RpcResult<UpdateGroupOutput>> resultFromOFLib = messageService.groupMod(ofGroupModInput, cookie) ;

    	RpcResult<UpdateGroupOutput> rpcResultFromOFLib = null ;

    	try {
    		rpcResultFromOFLib = resultFromOFLib.get();
    	} catch( Exception ex ) {
    		LOG.error( " Error while getting result for RemoveGroup RPC" + ex.getMessage());
    	}

    	UpdateGroupOutput updateGroupOutput = rpcResultFromOFLib.getResult() ;

    	RemoveGroupOutputBuilder removeGroupOutput = new RemoveGroupOutputBuilder() ;
    	removeGroupOutput.setTransactionId(updateGroupOutput.getTransactionId()) ;
    	RemoveGroupOutput result = removeGroupOutput.build();

    	Collection<RpcError> errors = rpcResultFromOFLib.getErrors() ;
        RpcResult<RemoveGroupOutput> rpcResult = Rpcs.getRpcResult(true, result, errors);

        LOG.debug("Returning the Remove Group RPC result to MD-SAL");
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<RemoveMeterOutput>> removeMeter(
            RemoveMeterInput input) {
    	// Convert the RemoveMeterInput to MeterModInput
        MeterModInput ofMeterModInput = MeterConvertor.toMeterModInput(input, version);


    	// For Meter provisioning, the SwitchConnectionDistinguisher is set to null so
    	// the request can be routed through any connection to the switch

    	SwitchConnectionDistinguisher cookie = null ;

    	LOG.debug("Calling the MeterMod RPC method on MessageDispatchService");
    	Future<RpcResult<UpdateMeterOutput>> resultFromOFLib = messageService.meterMod(ofMeterModInput, cookie) ;

    	RpcResult<UpdateMeterOutput> rpcResultFromOFLib = null ;

    	try {
    		rpcResultFromOFLib = resultFromOFLib.get();
    	} catch( Exception ex ) {
    		LOG.error( " Error while getting result for RemoveMeter RPC" + ex.getMessage());
    	}

    	UpdateMeterOutput updatemeterOutput = rpcResultFromOFLib.getResult() ;

    	RemoveMeterOutputBuilder removeMeterOutput = new RemoveMeterOutputBuilder() ;
    	removeMeterOutput.setTransactionId(updatemeterOutput.getTransactionId()) ;
    	RemoveMeterOutput result = removeMeterOutput.build();

    	Collection<RpcError> errors = rpcResultFromOFLib.getErrors() ;
        RpcResult<RemoveMeterOutput> rpcResult = Rpcs.getRpcResult(true, result, errors);

        LOG.debug("Returning the Remove Meter RPC result to MD-SAL");
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<Void>> transmitPacket(TransmitPacketInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    private FlowModInputBuilder toFlowModInputBuilder(Flow source) {
        FlowModInputBuilder target = new FlowModInputBuilder();
        target.setCookie(source.getCookie());
        target.setIdleTimeout(source.getIdleTimeout());
        target.setHardTimeout(source.getHardTimeout());
        target.setMatch(toMatch(source.getMatch()));

        return target;
    }

    private Match toMatch(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match match) {
        MatchBuilder target = new MatchBuilder();

        target.setMatchEntries(toMatchEntries(match));

        return null;
    }

    private List<MatchEntries> toMatchEntries(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match match) {
        List<MatchEntries> entries = new ArrayList<>();

        return null;
    }

    @Override
    public Future<RpcResult<UpdateFlowOutput>> updateFlow(UpdateFlowInput input) {
    	// Convert the UpdateFlowInput to FlowModInput
        FlowModInput ofFlowModInput = FlowConvertor.toFlowModInput(input.getUpdatedFlow(), version);

    	// Call the RPC method on MessageDispatchService

    	// For Flow provisioning, the SwitchConnectionDistinguisher is set to null so
    	// the request can be routed through any connection to the switch

    	SwitchConnectionDistinguisher cookie = null ;

    	LOG.debug("Calling the FlowMod RPC method on MessageDispatchService");
       	Future<RpcResult<UpdateFlowOutput>> resultFromOFLib = messageService.flowMod(ofFlowModInput, cookie) ;

       	RpcResult<UpdateFlowOutput> rpcResultFromOFLib = null ;

    	try {
    		rpcResultFromOFLib = resultFromOFLib.get();
    	} catch( Exception ex ) {
    		LOG.error( " Error while getting result for UpdateFlow RPC" + ex.getMessage());
    	}

    	UpdateFlowOutput updateFlowOutputOFLib = rpcResultFromOFLib.getResult() ;

    	UpdateFlowOutputBuilder updateFlowOutput = new UpdateFlowOutputBuilder() ;
    	updateFlowOutput.setTransactionId(updateFlowOutputOFLib.getTransactionId()) ;
    	UpdateFlowOutput result = updateFlowOutput.build();

    	Collection<RpcError> errors = rpcResultFromOFLib.getErrors() ;
        RpcResult<UpdateFlowOutput> rpcResult = Rpcs.getRpcResult(true, result, errors);

    	LOG.debug("Returning the Update Flow RPC result to MD-SAL");
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<UpdateGroupOutput>> updateGroup(
            UpdateGroupInput input) {
    	// Convert the UpdateGroupInput to GroupModInput
        GroupModInput ofGroupModInput = GroupConvertor.toGroupModInput(input.getUpdatedGroup(), version);


    	// For Flow provisioning, the SwitchConnectionDistinguisher is set to null so
    	// the request can be routed through any connection to the switch

    	SwitchConnectionDistinguisher cookie = null ;

    	LOG.debug("Calling the GroupMod RPC method on MessageDispatchService");
    	Future<RpcResult<UpdateGroupOutput>> resultFromOFLib = messageService.groupMod(ofGroupModInput, cookie) ;

    	RpcResult<UpdateGroupOutput> rpcResultFromOFLib = null ;

    	try {
    		rpcResultFromOFLib = resultFromOFLib.get();
    	} catch( Exception ex ) {
    		LOG.error( " Error while getting result for updateGroup RPC" + ex.getMessage());
    	}

    	UpdateGroupOutput updateGroupOutputOFLib = rpcResultFromOFLib.getResult() ;

    	UpdateGroupOutputBuilder updateGroupOutput = new UpdateGroupOutputBuilder() ;
    	updateGroupOutput.setTransactionId(updateGroupOutputOFLib.getTransactionId()) ;
    	UpdateGroupOutput result = updateGroupOutput.build();

    	Collection<RpcError> errors = rpcResultFromOFLib.getErrors() ;
        RpcResult<UpdateGroupOutput> rpcResult = Rpcs.getRpcResult(true, result, errors);

        LOG.debug("Returning the Update Group RPC result to MD-SAL");
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<UpdateMeterOutput>> updateMeter(
            UpdateMeterInput input) {
    	// Convert the UpdateMeterInput to MeterModInput
        MeterModInput ofMeterModInput = MeterConvertor.toMeterModInput(input.getUpdatedMeter(), version);


    	// For Meter provisioning, the SwitchConnectionDistinguisher is set to null so
    	// the request can be routed through any connection to the switch

    	SwitchConnectionDistinguisher cookie = null ;

    	LOG.debug("Calling the MeterMod RPC method on MessageDispatchService");
    	Future<RpcResult<UpdateMeterOutput>> resultFromOFLib = messageService.meterMod(ofMeterModInput, cookie) ;

    	RpcResult<UpdateMeterOutput> rpcResultFromOFLib = null ;

    	try {
    		rpcResultFromOFLib = resultFromOFLib.get();
    	} catch( Exception ex ) {
    		LOG.error( " Error while getting result for UpdateMeter RPC" + ex.getMessage());
    	}

    	UpdateMeterOutput updateMeterOutputFromOFLib = rpcResultFromOFLib.getResult() ;

    	UpdateMeterOutputBuilder updateMeterOutput = new UpdateMeterOutputBuilder() ;
    	updateMeterOutput.setTransactionId(updateMeterOutputFromOFLib.getTransactionId()) ;
    	UpdateMeterOutput result = updateMeterOutput.build();

    	Collection<RpcError> errors = rpcResultFromOFLib.getErrors() ;
        RpcResult<UpdateMeterOutput> rpcResult = Rpcs.getRpcResult(true, result, errors);

        LOG.debug("Returning the Update Meter RPC result to MD-SAL");
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }

    /*
     * Methods for requesting statistics from switch
     */
    @Override
    public Future<RpcResult<GetAllGroupStatisticsOutput>> getAllGroupStatistics(GetAllGroupStatisticsInput input) {

        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.debug("Prepare statistics request for all the groups - Transaction id - {}",xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPGROUP);
        mprInput.setVersion((short)0x04);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the group stats
        MultipartRequestGroupBuilder mprGroupBuild = new MultipartRequestGroupBuilder();
        mprGroupBuild.setGroupId((long) Group.OFPGALL.getIntValue());

        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(mprGroupBuild.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send group statistics request to the switch :{}",mprGroupBuild);
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        LOG.debug("Return results and transaction id back to caller");
        GetAllGroupStatisticsOutputBuilder output = new GetAllGroupStatisticsOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));
        output.setGroupStats(null);

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetAllGroupStatisticsOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);

    }

    @Override
    public Future<RpcResult<GetGroupDescriptionOutput>> getGroupDescription(GetGroupDescriptionInput input) {

        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.debug("Prepare group description statistics request - Transaction id - {}",xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPGROUPDESC);
        mprInput.setVersion((short)0x04);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the group description stats
        MultipartRequestGroupDescBuilder mprGroupDescBuild = new MultipartRequestGroupDescBuilder();

        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(mprGroupDescBuild.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send group desciption statistics request to switch : {}",mprGroupDescBuild);
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        LOG.debug("Return results and transaction id back to caller");
        GetGroupDescriptionOutputBuilder output = new GetGroupDescriptionOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));
        output.setGroupDescStats(null);

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetGroupDescriptionOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);

    }

    @Override
    public Future<RpcResult<GetGroupFeaturesOutput>> getGroupFeatures(GetGroupFeaturesInput input) {

        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.debug("Prepare group features statistics request - Transaction id - {}",xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPGROUPFEATURES);
        mprInput.setVersion((short)0x04);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the group description stats
        MultipartRequestGroupFeaturesBuilder mprGroupFeaturesBuild = new MultipartRequestGroupFeaturesBuilder();

        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(mprGroupFeaturesBuild.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send group features statistics request :{}",mprGroupFeaturesBuild);
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        LOG.debug("Return results and transaction id back to caller");
        GetGroupFeaturesOutputBuilder output = new GetGroupFeaturesOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetGroupFeaturesOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<GetGroupStatisticsOutput>> getGroupStatistics(GetGroupStatisticsInput input) {

        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.debug("Prepare statistics request for node {} group ({}) - Transaction id - {}",input.getNode(),input.getGroupId(),xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPGROUP);
        mprInput.setVersion((short)0x04);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the group stats
        MultipartRequestGroupBuilder mprGroupBuild = new MultipartRequestGroupBuilder();
        mprGroupBuild.setGroupId(input.getGroupId().getValue());

        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(mprGroupBuild.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send group statistics request :{}",mprGroupBuild);
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        LOG.debug("Return results and transaction id back to caller");
        GetGroupStatisticsOutputBuilder output = new GetGroupStatisticsOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));
        output.setGroupStats(null);

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetGroupStatisticsOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<GetAllMeterConfigStatisticsOutput>> getAllMeterConfigStatistics(
            GetAllMeterConfigStatisticsInput input) {

        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.debug("Prepare config request for all the meters - Transaction id - {}",xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPMETERCONFIG);
        mprInput.setVersion((short)0x04);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the meter stats
        MultipartRequestMeterConfigBuilder mprMeterConfigBuild = new MultipartRequestMeterConfigBuilder();
        mprMeterConfigBuild.setMeterId((long) Meter.OFPMALL.getIntValue());

        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(mprMeterConfigBuild.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send meter statistics request :{}",mprMeterConfigBuild);
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        LOG.debug("Return results and transaction id back to caller");
        GetAllMeterConfigStatisticsOutputBuilder output = new GetAllMeterConfigStatisticsOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));
        output.setMeterConfigStats(null);

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetAllMeterConfigStatisticsOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<GetAllMeterStatisticsOutput>> getAllMeterStatistics(GetAllMeterStatisticsInput input) {

        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.debug("Prepare statistics request for all the meters - Transaction id - {}",xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPMETER);
        mprInput.setVersion((short)0x04);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the meter stats
        MultipartRequestMeterBuilder mprMeterBuild = new MultipartRequestMeterBuilder();
        mprMeterBuild.setMeterId((long) Meter.OFPMALL.getIntValue());

        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(mprMeterBuild.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send meter statistics request :{}",mprMeterBuild);
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        LOG.debug("Return results and transaction id back to caller");
        GetAllMeterStatisticsOutputBuilder output = new GetAllMeterStatisticsOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));
        output.setMeterStats(null);

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetAllMeterStatisticsOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<GetMeterFeaturesOutput>> getMeterFeatures(GetMeterFeaturesInput input) {

        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.debug("Prepare features statistics request for all the meters - Transaction id - {}",xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPMETERFEATURES);
        mprInput.setVersion((short)0x04);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the group description stats
        MultipartRequestMeterFeaturesBuilder mprMeterFeaturesBuild = new MultipartRequestMeterFeaturesBuilder();

        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(mprMeterFeaturesBuild.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send meter features statistics request :{}",mprMeterFeaturesBuild);
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        LOG.debug("Return results and transaction id back to caller");
        GetMeterFeaturesOutputBuilder output = new GetMeterFeaturesOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetMeterFeaturesOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<GetMeterStatisticsOutput>> getMeterStatistics(GetMeterStatisticsInput input) {
        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.debug("Preprae statistics request for Meter ({}) - Transaction id - {}",input.getMeterId().getValue(),xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPMETER);
        mprInput.setVersion((short)0x04);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the meter stats
        MultipartRequestMeterBuilder mprMeterBuild = new MultipartRequestMeterBuilder();
        //Select specific meter
        mprMeterBuild.setMeterId(input.getMeterId().getValue());

        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(mprMeterBuild.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send meter statistics request :{}",mprMeterBuild);
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        LOG.debug("Return results and transaction id back to caller");
        GetMeterStatisticsOutputBuilder output = new GetMeterStatisticsOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));
        output.setMeterStats(null);

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetMeterStatisticsOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);
    }

    private TransactionId generateTransactionId(Long xid){
        String stringXid =xid.toString();
        BigInteger bigIntXid = new BigInteger( stringXid );
        return new TransactionId(bigIntXid);

    }
}
