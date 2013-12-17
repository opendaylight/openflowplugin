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
import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.openflowplugin.openflow.md.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.GroupConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.MeterConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.PortConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.TableFeaturesConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchReactor;
import org.opendaylight.openflowplugin.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllNodeConnectorStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowTableStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowTableStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetNodeConnectorStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.Port;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter._case.MultipartRequestMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter.config._case.MultipartRequestMeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table._case.MultipartRequestTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.MultipartRequestTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.multipart.request.table.features.TableFeatures;
//import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.GetPortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllPortsStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllPortsStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllPortsStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetPortStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetPortStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetPortStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutputBuilder;
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
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the group stats
        MultipartRequestGroupCaseBuilder caseBuilder = new MultipartRequestGroupCaseBuilder();
        MultipartRequestGroupBuilder mprGroupBuild = new MultipartRequestGroupBuilder();
        mprGroupBuild.setGroupId(new GroupId(BinContent.intToUnsignedLong(Group.OFPGALL.getIntValue())));
        caseBuilder.setMultipartRequestGroup(mprGroupBuild.build());
        
        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(caseBuilder.build());

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
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the group description stats
        MultipartRequestGroupDescCaseBuilder mprGroupDescBuild = new MultipartRequestGroupDescCaseBuilder();

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
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the group description stats
        MultipartRequestGroupFeaturesCaseBuilder mprGroupFeaturesBuild = new MultipartRequestGroupFeaturesCaseBuilder();

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
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the group stats
        MultipartRequestGroupCaseBuilder caseBuilder = new MultipartRequestGroupCaseBuilder();
        MultipartRequestGroupBuilder mprGroupBuild = new MultipartRequestGroupBuilder();
        mprGroupBuild.setGroupId(new GroupId(input.getGroupId().getValue()));
        caseBuilder.setMultipartRequestGroup(mprGroupBuild.build());
        
        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(caseBuilder.build());

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
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the meter stats
        MultipartRequestMeterConfigCaseBuilder caseBuilder = new MultipartRequestMeterConfigCaseBuilder();
        MultipartRequestMeterConfigBuilder mprMeterConfigBuild = new MultipartRequestMeterConfigBuilder();
        mprMeterConfigBuild.setMeterId(new MeterId(BinContent.intToUnsignedLong(Meter.OFPMALL.getIntValue())));
        caseBuilder.setMultipartRequestMeterConfig(mprMeterConfigBuild.build());
        
        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(caseBuilder.build());

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
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the meter stats
        MultipartRequestMeterCaseBuilder caseBuilder = new MultipartRequestMeterCaseBuilder();
        MultipartRequestMeterBuilder mprMeterBuild = new MultipartRequestMeterBuilder();
        mprMeterBuild.setMeterId(new MeterId(BinContent.intToUnsignedLong(Meter.OFPMALL.getIntValue())));
        caseBuilder.setMultipartRequestMeter(mprMeterBuild.build());
        
        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(caseBuilder.build());

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
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the group description stats
        MultipartRequestMeterFeaturesCaseBuilder mprMeterFeaturesBuild = new MultipartRequestMeterFeaturesCaseBuilder();

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
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the meter stats
        MultipartRequestMeterCaseBuilder caseBuilder = new MultipartRequestMeterCaseBuilder();
        MultipartRequestMeterBuilder mprMeterBuild = new MultipartRequestMeterBuilder();
        //Select specific meter
        mprMeterBuild.setMeterId(new MeterId(input.getMeterId().getValue()));
        caseBuilder.setMultipartRequestMeter(mprMeterBuild.build());
        
        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(caseBuilder.build());

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
    
    @Override
    public Future<RpcResult<GetAllPortsStatisticsOutput>> getAllPortsStatistics(GetAllPortsStatisticsInput arg0) {

        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.info("Prepare port statistics request for all ports of node {} - TrasactionId - {}",arg0.getNode().getValue(),xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPPORTSTATS);
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body to fetch stats for all the port of the node
        MultipartRequestPortStatsCaseBuilder caseBuilder = new MultipartRequestPortStatsCaseBuilder();
        MultipartRequestPortStatsBuilder mprPortStatsBuilder = new MultipartRequestPortStatsBuilder();
        //Select all ports 
        mprPortStatsBuilder.setPortNo(OFConstants.OFPP_ANY);
        caseBuilder.setMultipartRequestPortStats(mprPortStatsBuilder.build());
        
        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(caseBuilder.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send port statistics request :{}",mprPortStatsBuilder.build().toString());
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        GetAllPortsStatisticsOutputBuilder output = new GetAllPortsStatisticsOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetAllPortsStatisticsOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<GetPortStatisticsOutput>> getPortStatistics(GetPortStatisticsInput arg0) {
        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.info("Prepare port statistics request for port {} of node {} - TrasactionId - {}",arg0.getNodeConnectorId(), arg0.getNode().getValue(),xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPPORTSTATS);
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body to fetch stats for all the port of the node
        MultipartRequestPortStatsCaseBuilder caseBuilder = new MultipartRequestPortStatsCaseBuilder();
        MultipartRequestPortStatsBuilder mprPortStatsBuilder = new MultipartRequestPortStatsBuilder();

        //Set specific port 
        mprPortStatsBuilder.setPortNo(InventoryDataServiceUtil.portNumberfromNodeConnectorId(arg0.getNodeConnectorId()));
        caseBuilder.setMultipartRequestPortStats(mprPortStatsBuilder.build());
        
        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(caseBuilder.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send port statistics request :{}",mprPortStatsBuilder.build().toString());
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        GetPortStatisticsOutputBuilder output = new GetPortStatisticsOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetPortStatisticsOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);
    }

    private TransactionId generateTransactionId(Long xid){
        String stringXid =xid.toString();
        BigInteger bigIntXid = new BigInteger( stringXid );
        return new TransactionId(bigIntXid);

    }

    @Override
    public Future<RpcResult<GetPortOutput>> getPort() {
	// TODO Auto-generated method stub
	return null;
    }
    

    @Override
    public Future<RpcResult<UpdatePortOutput>> updatePort(UpdatePortInput input) {
	PortModInput ofPortModInput = null ;
	RpcResult<UpdatePortOutput> rpcResultFromOFLib = null ;
		
				
	// For Flow provisioning, the SwitchConnectionDistinguisher is set to null so  
    	// the request can be routed through any connection to the switch
    	
    	SwitchConnectionDistinguisher cookie = null ;
    	
	// NSF sends a list of port and the ModelDrivenSwitch will 
    	// send one port at a time towards the switch ( mutiple RPCs calls)
	List<Port> inputPorts = input.getUpdatedPort().getPort().getPort() ;
		
	// Get the Xid. The same Xid has to be sent in all the RPCs
	Long Xid = sessionContext.getNextXid();
		
	for( Port inputPort : inputPorts) {
		   
	    // Convert the UpdateGroupInput to GroupModInput 
	    ofPortModInput = PortConvertor.toPortModInput(inputPort, version) ;
					    	 	
	    // Insert the Xid ( transaction Id) before calling the RPC on the OFLibrary
		    	
	    PortModInputBuilder mdInput = new PortModInputBuilder();
	    mdInput.setXid(Xid);
	    mdInput.setVersion(ofPortModInput.getVersion()) ;
	    mdInput.setPortNo(ofPortModInput.getPortNo()) ;
	    mdInput.setMaskV10(ofPortModInput.getMaskV10()) ;
	    mdInput.setMask(ofPortModInput.getMask()) ;
	    mdInput.setHwAddress(ofPortModInput.getHwAddress());
	    mdInput.setConfigV10(ofPortModInput.getConfigV10()) ;
	    mdInput.setConfig(ofPortModInput.getConfig()) ;
	    mdInput.setAdvertiseV10(ofPortModInput.getAdvertiseV10()) ;
	    mdInput.setAdvertise(ofPortModInput.getAdvertise()) ;

	    LOG.debug("Calling the PortMod RPC method on MessageDispatchService");
	    Future<RpcResult<UpdatePortOutput>> resultFromOFLib = messageService.portMod(ofPortModInput, cookie) ;

	    try { 
	        rpcResultFromOFLib = resultFromOFLib.get();
	    } catch( Exception ex ) {
	        LOG.error( " Error while getting result for updatePort RPC" + ex.getMessage());
	    }

	    // The Future response value for all the RPCs except the last one is ignored

	}
	//Extract the Xid only from the Future for the last RPC and
	// send it back to the NSF
	UpdatePortOutput updatePortOutputOFLib = rpcResultFromOFLib.getResult() ;
	
	UpdatePortOutputBuilder updatePortOutput = new UpdatePortOutputBuilder() ;
	updatePortOutput.setTransactionId(updatePortOutputOFLib.getTransactionId()) ;
	UpdatePortOutput result = updatePortOutput.build();
	
	Collection<RpcError> errors = rpcResultFromOFLib.getErrors() ;
	RpcResult<UpdatePortOutput> rpcResult = Rpcs.getRpcResult(true, result, errors); 
	
	LOG.debug("Returning the Update Group RPC result to MD-SAL");
	return Futures.immediateFuture(rpcResult);

    }
    
    @Override
    public Future<RpcResult<UpdateTableOutput>> updateTable(
                        UpdateTableInput input) {

        // Get the Xid. The same Xid has to be sent in all the Multipart requests
        Long xid = this.getSessionContext().getNextXid();

        LOG.debug("Prepare the Multipart Table Mod requests for Transaction Id {} ",xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPTABLEFEATURES);
        mprInput.setVersion((short)0x04);
        mprInput.setXid(xid);

        //Convert the list of all MD-SAL table feature object into OF library object
        List<TableFeatures> ofTableFeatureList = TableFeaturesConvertor.toTableFeaturesRequest(input.getUpdatedTable()) ;
        

        MultipartRequestTableFeaturesCaseBuilder caseRequest = new MultipartRequestTableFeaturesCaseBuilder();
        MultipartRequestTableFeaturesBuilder tableFeaturesRequest = new MultipartRequestTableFeaturesBuilder();

        mprInput.setFlags(new MultipartRequestFlags(true));
        
        tableFeaturesRequest.setTableFeatures(ofTableFeatureList) ;
        
        //Set request body to main multipart request
        caseRequest.setMultipartRequestTableFeatures(tableFeaturesRequest.build());
        mprInput.setMultipartRequestBody(caseRequest.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send Table Feature request :{}",ofTableFeatureList);
        this.messageService.multipartRequest(mprInput.build(), null);
        
        
        //Extract the Xid only from the Future for the last RPC and
        // send it back to the NSF
        LOG.debug("Returning the result and transaction id to NSF");
        LOG.debug("Return results and transaction id back to caller");
        UpdateTableOutputBuilder output = new UpdateTableOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<UpdateTableOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<GetAllFlowStatisticsFromFlowTableOutput>> getAllFlowStatisticsFromFlowTable(
            GetAllFlowStatisticsFromFlowTableInput arg0) {

        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.debug("Prepare statistics request to get flow stats for switch tables {} - Transaction id - {}"
                ,arg0.getTableId().getValue(),xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPFLOW);
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the group stats
        MultipartRequestFlowCaseBuilder multipartRequestFlowCaseBuilder  = new MultipartRequestFlowCaseBuilder (); 
        MultipartRequestFlowBuilder mprFlowRequestBuilder = new MultipartRequestFlowBuilder();
        mprFlowRequestBuilder.setTableId(arg0.getTableId().getValue());
        mprFlowRequestBuilder.setOutPort(OFConstants.OFPP_ANY);
        mprFlowRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
        mprFlowRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
        mprFlowRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
        
        if(version == OFConstants.OFP_VERSION_1_0){
            LOG.info("Target node is running openflow version 1.0");
            FlowWildcardsV10 wildCard = new FlowWildcardsV10(true,false,false,false,false,false,false,false,false,false,false);
            mprFlowRequestBuilder.setMatchV10(new MatchV10Builder().setWildcards(wildCard).build());
        }
        if(version == OFConstants.OFP_VERSION_1_3){
            LOG.info("Target node is running openflow version 1.3+");
            mprFlowRequestBuilder.setMatch(new MatchBuilder().setType(OxmMatchType.class).build());
        }


        //Set request body to main multipart request
        multipartRequestFlowCaseBuilder.setMultipartRequestFlow(mprFlowRequestBuilder.build());
        mprInput.setMultipartRequestBody(multipartRequestFlowCaseBuilder.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send flow statistics request to the switch :{}",mprFlowRequestBuilder);
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        LOG.debug("Return results and transaction id back to caller");
        GetAllFlowStatisticsFromFlowTableOutputBuilder output = 
                new GetAllFlowStatisticsFromFlowTableOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));
        output.setFlowAndStatisticsMapList(null);

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetAllFlowStatisticsFromFlowTableOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>> getAllFlowsStatisticsFromAllFlowTables(
            GetAllFlowsStatisticsFromAllFlowTablesInput arg0) {
        
        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.info("Prepare statistics request to get flow stats of all switch tables - Transaction id - {}",xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPFLOW);
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the group stats
        MultipartRequestFlowCaseBuilder  multipartRequestFlowCaseBuilder = new MultipartRequestFlowCaseBuilder();
        MultipartRequestFlowBuilder mprFlowRequestBuilder = new MultipartRequestFlowBuilder();
        mprFlowRequestBuilder.setTableId(OFConstants.OFPTT_ALL);
        mprFlowRequestBuilder.setOutPort(OFConstants.OFPP_ANY);
        mprFlowRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
        mprFlowRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
        mprFlowRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
        mprFlowRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
        
        //TODO: repeating code
        if(version == OFConstants.OFP_VERSION_1_0){
            FlowWildcardsV10 wildCard = new FlowWildcardsV10(true,false,false,false,false,false,false,false,false,false,false);
            mprFlowRequestBuilder.setMatchV10(new MatchV10Builder().setWildcards(wildCard).build());
        }
        if(version == OFConstants.OFP_VERSION_1_3){
            mprFlowRequestBuilder.setMatch(new MatchBuilder().setType(OxmMatchType.class).build());
        }
        //Set request body to main multipart request
        multipartRequestFlowCaseBuilder.setMultipartRequestFlow(mprFlowRequestBuilder.build());
        mprInput.setMultipartRequestBody(multipartRequestFlowCaseBuilder.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send flow statistics request to the switch :{}",mprFlowRequestBuilder);
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        GetAllFlowsStatisticsFromAllFlowTablesOutputBuilder output = 
                new GetAllFlowsStatisticsFromAllFlowTablesOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));
        output.setFlowAndStatisticsMapList(null);

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);

    }

    @Override
    public Future<RpcResult<GetFlowStatisticsFromFlowTableOutput>> getFlowStatisticsFromFlowTable(
            GetFlowStatisticsFromFlowTableInput arg0) {
        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.info("Prepare statistics request to get stats for flow {} for switch tables {} - Transaction id - {}"
                ,arg0.getMatch().toString(),arg0.getTableId(),xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPFLOW);
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the group stats
        MultipartRequestFlowCaseBuilder  multipartRequestFlowCaseBuilder = new MultipartRequestFlowCaseBuilder();
        MultipartRequestFlowBuilder mprFlowRequestBuilder = new MultipartRequestFlowBuilder();
        mprFlowRequestBuilder.setTableId(arg0.getTableId());
        mprFlowRequestBuilder.setOutPort(arg0.getOutPort().longValue());
        mprFlowRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
        mprFlowRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
        mprFlowRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);

        // convert and inject match
        MatchReactor.getInstance().convert(arg0.getMatch(), version, mprFlowRequestBuilder);
        //TODO: repeating code
        if(version == OFConstants.OFP_VERSION_1_3){
            mprFlowRequestBuilder.setCookie(arg0.getCookie());
            mprFlowRequestBuilder.setCookieMask(arg0.getCookieMask());
            mprFlowRequestBuilder.setOutGroup(arg0.getOutGroup());
        }

        //Set request body to main multipart request
        multipartRequestFlowCaseBuilder.setMultipartRequestFlow(mprFlowRequestBuilder.build());
        mprInput.setMultipartRequestBody(multipartRequestFlowCaseBuilder.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send flow statistics request to the switch :{}",mprFlowRequestBuilder);
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        GetFlowStatisticsFromFlowTableOutputBuilder output = 
                new GetFlowStatisticsFromFlowTableOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));
        output.setFlowAndStatisticsMapList(null);

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetFlowStatisticsFromFlowTableOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>> getAggregateFlowStatisticsFromFlowTableForAllFlows(
            GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput arg0) {
        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.info("Prepare aggregate flow statistics request to get aggregate flow stats for all the flow installed on switch table {} - Transaction id - {}"
                ,arg0.getTableId().getValue(),xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPAGGREGATE);
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the group stats
        MultipartRequestAggregateCaseBuilder multipartRequestAggregateCaseBuilder  = new MultipartRequestAggregateCaseBuilder (); 
        MultipartRequestAggregateBuilder mprAggregateRequestBuilder = new MultipartRequestAggregateBuilder();
        mprAggregateRequestBuilder.setTableId(arg0.getTableId().getValue());
        mprAggregateRequestBuilder.setOutPort(OFConstants.OFPP_ANY);
        mprAggregateRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
        mprAggregateRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
        mprAggregateRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
        
        //TODO: repeating code
        if(version == OFConstants.OFP_VERSION_1_0){
            FlowWildcardsV10 wildCard = new FlowWildcardsV10(true,false,false,false,false,false,false,false,false,false,false);
            mprAggregateRequestBuilder.setMatchV10(new MatchV10Builder().setWildcards(wildCard).build());
        }
        if(version == OFConstants.OFP_VERSION_1_3){
            mprAggregateRequestBuilder.setMatch(new MatchBuilder().setType(OxmMatchType.class).build());
        }


        //Set request body to main multipart request
        multipartRequestAggregateCaseBuilder.setMultipartRequestAggregate(mprAggregateRequestBuilder.build());
        mprInput.setMultipartRequestBody(multipartRequestAggregateCaseBuilder.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send request to the switch :{}",multipartRequestAggregateCaseBuilder.build().toString());
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutputBuilder output = 
                new GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> getAggregateFlowStatisticsFromFlowTableForGivenMatch(
            GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput arg0) {

        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.debug("Prepare agregate statistics request to get aggregate stats for flows matching {} and installed in flow tables {} - Transaction id - {}"
                ,arg0.getMatch().toString(),arg0.getTableId(),xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPAGGREGATE);
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the group stats
        MultipartRequestAggregateCaseBuilder multipartRequestAggregateCaseBuilder  = new MultipartRequestAggregateCaseBuilder (); 
        MultipartRequestAggregateBuilder mprAggregateRequestBuilder = new MultipartRequestAggregateBuilder();
        mprAggregateRequestBuilder.setTableId(arg0.getTableId());
        mprAggregateRequestBuilder.setOutPort(arg0.getOutPort().longValue());
        mprAggregateRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
        mprAggregateRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
        mprAggregateRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);


        MatchReactor.getInstance().convert(arg0.getMatch(), version, mprAggregateRequestBuilder);
        //TODO: repeating code
        if(version == OFConstants.OFP_VERSION_1_3){
            mprAggregateRequestBuilder.setCookie(arg0.getCookie());
            mprAggregateRequestBuilder.setCookieMask(arg0.getCookieMask());
            mprAggregateRequestBuilder.setOutGroup(arg0.getOutGroup());
        }

        //Set request body to main multipart request
        multipartRequestAggregateCaseBuilder.setMultipartRequestAggregate(mprAggregateRequestBuilder.build());
        mprInput.setMultipartRequestBody(multipartRequestAggregateCaseBuilder.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send request to the switch :{}",multipartRequestAggregateCaseBuilder.build().toString());
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder output = 
                new GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<GetFlowTablesStatisticsOutput>> getFlowTablesStatistics(GetFlowTablesStatisticsInput arg0) {
        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.info("Prepare flow table statistics request to get flow table stats for all tables " +
        		"from node {}- Transaction id - {}",arg0.getNode(),xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPTABLE);
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body for fetch all the group stats
        MultipartRequestTableCaseBuilder multipartRequestTableCaseBuilder  = new MultipartRequestTableCaseBuilder (); 
        MultipartRequestTableBuilder multipartRequestTableBuilder = new MultipartRequestTableBuilder();
        multipartRequestTableBuilder.setEmpty(true);
        multipartRequestTableCaseBuilder.setMultipartRequestTable(multipartRequestTableBuilder.build());
        
        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(multipartRequestTableCaseBuilder.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send request to the switch :{}",multipartRequestTableCaseBuilder.build().toString());
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        GetFlowTablesStatisticsOutputBuilder output = new GetFlowTablesStatisticsOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetFlowTablesStatisticsOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<GetAllQueuesStatisticsFromAllPortsOutput>> getAllQueuesStatisticsFromAllPorts(
            GetAllQueuesStatisticsFromAllPortsInput arg0) {
        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.info("Prepare queue statistics request to collect stats for all queues attached to all the ports of node {} - TrasactionId - {}",arg0.getNode().getValue(),xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPQUEUE);
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body to fetch stats for all the port of the node
        MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
        MultipartRequestQueueBuilder mprQueueBuilder = new MultipartRequestQueueBuilder();
        //Select all ports 
        mprQueueBuilder.setPortNo(OFConstants.OFPP_ANY);
        //Select all the ports
        mprQueueBuilder.setQueueId(OFConstants.OFPQ_ANY);
        
        caseBuilder.setMultipartRequestQueue(mprQueueBuilder.build());
        
        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(caseBuilder.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send queue statistics request :{}",mprQueueBuilder.build().toString());
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        GetAllQueuesStatisticsFromAllPortsOutputBuilder output = new GetAllQueuesStatisticsFromAllPortsOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));
        output.setQueueIdAndStatisticsMap(null);

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetAllQueuesStatisticsFromAllPortsOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<GetAllQueuesStatisticsFromGivenPortOutput>> getAllQueuesStatisticsFromGivenPort(
            GetAllQueuesStatisticsFromGivenPortInput arg0) {
        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.info("Prepare queue statistics request to collect stats for " +
        		"all queues attached to given port {} of node {} - TrasactionId - {}",arg0.getNodeConnectorId().toString(),arg0.getNode().getValue(),xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPQUEUE);
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body to fetch stats for all the port of the node
        MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
        MultipartRequestQueueBuilder mprQueueBuilder = new MultipartRequestQueueBuilder();
        //Select all queues
        mprQueueBuilder.setQueueId(OFConstants.OFPQ_ANY);
        //Select specific port
        mprQueueBuilder.setPortNo(InventoryDataServiceUtil.portNumberfromNodeConnectorId(arg0.getNodeConnectorId()));
        
        caseBuilder.setMultipartRequestQueue(mprQueueBuilder.build());
        
        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(caseBuilder.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send queue statistics request :{}",mprQueueBuilder.build().toString());
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        GetAllQueuesStatisticsFromGivenPortOutputBuilder output = new GetAllQueuesStatisticsFromGivenPortOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));
        output.setQueueIdAndStatisticsMap(null);

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetAllQueuesStatisticsFromGivenPortOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<GetQueueStatisticsFromGivenPortOutput>> getQueueStatisticsFromGivenPort(
            GetQueueStatisticsFromGivenPortInput arg0) {
        //Generate xid to associate it with the request
        Long xid = this.getSessionContext().getNextXid();

        LOG.info("Prepare queue statistics request to collect stats for " +
                        "given queue attached to given port {} of node {} - TrasactionId - {}",arg0.getQueueId().toString(),arg0.getNodeConnectorId().toString(),arg0.getNode().getValue(),xid);

        // Create multipart request header
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPQUEUE);
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));

        // Create multipart request body to fetch stats for all the port of the node
        MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
        MultipartRequestQueueBuilder mprQueueBuilder = new MultipartRequestQueueBuilder();
        //Select specific queue
        mprQueueBuilder.setQueueId(arg0.getQueueId().getValue());
        //Select specific port 
        mprQueueBuilder.setPortNo(InventoryDataServiceUtil.portNumberfromNodeConnectorId(arg0.getNodeConnectorId()));
        
        caseBuilder.setMultipartRequestQueue(mprQueueBuilder.build());
        
        //Set request body to main multipart request
        mprInput.setMultipartRequestBody(caseBuilder.build());

        //Send the request, no cookies associated, use any connection
        LOG.debug("Send queue statistics request :{}",mprQueueBuilder.build().toString());
        this.messageService.multipartRequest(mprInput.build(), null);

        // Prepare rpc return output. Set xid and send it back.
        GetQueueStatisticsFromGivenPortOutputBuilder output = new GetQueueStatisticsFromGivenPortOutputBuilder();
        output.setTransactionId(generateTransactionId(xid));
        output.setQueueIdAndStatisticsMap(null);

        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<GetQueueStatisticsFromGivenPortOutput> rpcResult = Rpcs.getRpcResult(true, output.build(), errors);
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public Future<RpcResult<GetAllFlowStatisticsOutput>> getAllFlowStatistics(GetAllFlowStatisticsInput arg0) {
        //TODO: Depricated, need to clean it up. Sal-Compatibility layes is dependent on it.
        // Once sal-compatibility layer is fixed this rpc call can be removed from yang file 
        return null;
    }

    @Override
    public Future<RpcResult<GetAllNodeConnectorStatisticsOutput>> getAllNodeConnectorStatistics(
            GetAllNodeConnectorStatisticsInput arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<GetFlowStatisticsOutput>> getFlowStatistics(GetFlowStatisticsInput arg0) {
        //TODO: Depricated, need to clean it up. Sal-Compatibility layes is dependent on it.
        // Once sal-compatibility layer is fixed this rpc call can be removed from yang file 
        return null;
    }

    @Override
    public Future<RpcResult<GetFlowTableStatisticsOutput>> getFlowTableStatistics(GetFlowTableStatisticsInput arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<GetNodeConnectorStatisticsOutput>> getNodeConnectorStatistics(
            GetNodeConnectorStatisticsInput arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
