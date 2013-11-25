/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.util.ArrayList;
import java.util.Collection;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutputBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.match.grouping.MatchBuilder;
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
}
