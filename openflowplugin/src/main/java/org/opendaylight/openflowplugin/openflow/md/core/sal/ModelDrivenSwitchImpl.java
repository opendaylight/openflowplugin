package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.spi.LoggerFactory;
import org.opendaylight.openflowplugin.openflow.md.ModelDrivenSwitch;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.converter.MeterConverter;
import org.opendaylight.openflowplugin.openflow.md.core.session.MessageDispatchServiceImpl;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.openflowplugin.openflow.md.core.sal.converter.GroupConverter;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;

public class ModelDrivenSwitchImpl extends AbstractModelDrivenSwitch {

    public static final Logger LOG = org.slf4j.LoggerFactory
            .getLogger(ModelDrivenSwitchImpl.class);
    private final NodeId nodeId;

    protected ModelDrivenSwitchImpl(NodeId nodeId,
            InstanceIdentifier<Node> identifier, SessionContext context) {
        super(identifier, context);
        this.nodeId = nodeId;
    }

    @Override
    public Future<RpcResult<AddFlowOutput>> addFlow(AddFlowInput input) {
    	Future<RpcResult<AddFlowOutput>> result = null ;
        
       	// Convert the AddFlowInput to FlowModInput 
    	FlowModInput ofFlowpModInput = FlowConverter.toFlowModInput(input) ;
    	
    	// Call the RPC method on MessageDispatchService
    	    	
    	// For Flow provisioning, the SwitchConnectionDistinguisher is set to null so  
    	// the request can be routed through any connection to the switch
    	
    	SwitchConnectionDistinguisher cookie = null ;
    	
    	// TODO -- Remove the comment when appropriate return type is supported by layers below
    	
    	// Future<RpcResult<AddFlowOutput>> result = MessageDispatchServiceImpl.flowMod(ofFlowModInput, cookie) ;
    	
    	return result ;
    }

    @Override
    public Future<RpcResult<AddGroupOutput>> addGroup(AddGroupInput input) {
    	Future<RpcResult<AddGroupOutput>> result = null ;
        
       	// Convert the AddGroupInput to GroupModInput 
    	GroupModInput ofGroupModInput = GroupConverter.toGroupModInput(input) ;
    	
    	// Call the RPC method on MessageDispatchService
    	    	
    	// For Group provisioning, the SwitchConnectionDistinguisher is set to null so  
    	// the request can be routed through any connection to the switch
    	
    	SwitchConnectionDistinguisher cookie = null ;
    	
    	// TODO -- Remove the comment when appropriate return type is supported by layers below
    	
    	//Future<RpcResult<AddGroupOutput>> result = MessageDispatchServiceImpl.groupMod(ofGroupModInput, cookie) ;
    	
    	return result ;
    }

    @Override
    public Future<RpcResult<AddMeterOutput>> addMeter(AddMeterInput input) {
    	Future<RpcResult<AddMeterOutput>> result = null ;
        
       	// Convert the AddMeterInput to MeterModInput 
    	MeterModInput ofMeterModInput = MeterConverter.toMeterModInput( input );
    	
    	// Call the RPC method on MessageDispatchService
    	    	
    	// For Meter provisioning, the SwitchConnectionDistinguisher is set to null so  
    	// the request can be routed through any connection to the switch
    	
    	SwitchConnectionDistinguisher cookie = null ;
    	
    	// TODO -- Remove the comment when appropriate return type is supported by layers below
    	
    	//Future<RpcResult<AddMeterOutput>> result = MessageDispatchServiceImpl.meterMod(ofMeterModInput, cookie) ;
    	
    	return result ;
    }

    @Override
    public Future<RpcResult<Void>> removeFlow(RemoveFlowInput input) {
    	Future<RpcResult<RemoveFlowOutput>> result = null ;
        
       	// Convert the RemoveFlowInput to FlowModInput 
    	FlowModInput ofFlowpModInput = FlowConverter.toFlowModInput(input) ;
    	
    	// Call the RPC method on MessageDispatchService
    	    	
    	// For Flow provisioning, the SwitchConnectionDistinguisher is set to null so  
    	// the request can be routed through any connection to the switch
    	
    	SwitchConnectionDistinguisher cookie = null ;
    	
    	// TODO -- Remove the comment when appropriate return type is supported by layers below
    	
    	// Future<RpcResult<RemoveFlowOutput>> result = MessageDispatchServiceImpl.flowMod(ofFlowModInput, cookie) ;
    	
    	return result ;
    }

    @Override
    public Future<RpcResult<RemoveGroupOutput>> removeGroup(
            RemoveGroupInput input) {
    	Future<RpcResult<RemoveGroupOutput>> result = null ;
        
       	// Convert the RemoveGroupInput to GroupModInput 
    	GroupModInput ofGroupModInput = GroupConverter.toGroupModInput(input) ;
    	
    	// Call the RPC method on MessageDispatchService
    	    	
    	// For Group provisioning, the SwitchConnectionDistinguisher is set to null so  
    	// the request can be routed through any connection to the switch
    	
    	SwitchConnectionDistinguisher cookie = null ;
    	
    	// TODO -- Remove the comment when appropriate return type is supported by layers below
    	
    	//Future<RpcResult<RemoveGroupOutput>> result = MessageDispatchServiceImpl.groupMod(ofGroupModInput, cookie) ;
    	
    	return result ;
    }

    @Override
    public Future<RpcResult<RemoveMeterOutput>> removeMeter(
            RemoveMeterInput input) {
    	Future<RpcResult<RemoveMeterOutput>> result = null ;
        
    	// Convert the RemoveMeterInput to MeterModInput 
    	MeterModInput ofMeterModInput = MeterConverter.toMeterModInput( input );
    	
    	// Call the RPC method on MessageDispatchService
    	    	
    	// For Meter provisioning, the SwitchConnectionDistinguisher is set to null so  
    	// the request can be routed through any connection to the switch
    	
    	SwitchConnectionDistinguisher cookie = null ;
    	
    	// TODO -- Remove the comment when appropriate return type is supported by layers below
    	//Future<RpcResult<RemoveMeterOutput>> result = MessageDispatchServiceImpl.meterMod(ofMeterModInput, cookie) ;
    	
    	return result ;
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
    public Future<RpcResult<Void>> updateFlow(UpdateFlowInput input) {
    	Future<RpcResult<UpdateFlowOutput>> result = null ;
        
       	// Get only the UpdatedFlow and convert it to FlowModInput 
    	FlowModInput ofFlowpModInput = FlowConverter.toFlowModInput(input.getUpdatedFlow()) ;
    	
    	// Call the RPC method on MessageDispatchService
    	    	
    	// For Flow provisioning, the SwitchConnectionDistinguisher is set to null so  
    	// the request can be routed through any connection to the switch
    	
    	SwitchConnectionDistinguisher cookie = null ;
    	
    	// TODO -- Remove the comment when appropriate return type is supported by layers below
    	
    	// Future<RpcResult<RemoveFlowOutput>> result = MessageDispatchServiceImpl.flowMod(ofFlowModInput, cookie) ;
    	
    	return result ;
    }

    @Override
    public Future<RpcResult<UpdateGroupOutput>> updateGroup(
            UpdateGroupInput input) {
    	Future<RpcResult<UpdateGroupOutput>> result = null ;
        
       	// Get only the UpdatedGroup and convert it to GroupModInput 
    	GroupModInput ofGroupModInput = GroupConverter.toGroupModInput(input.getUpdatedGroup()) ;
    	
    	// Call the RPC method on MessageDispatchService
    	    	
    	// For Group provisioning, the SwitchConnectionDistinguisher is set to null so  
    	// the request can be routed through any connection to the switch
    	
    	SwitchConnectionDistinguisher cookie = null ;
    	
    	// TODO -- Remove the comment when appropriate return type is supported by layers below
    	
    	//Future<RpcResult<UpdateGroupOutput>> result = MessageDispatchServiceImpl.groupMod(ofGroupModInput, cookie) ;
    	
    	return result ;
    }

    @Override
    public Future<RpcResult<UpdateMeterOutput>> updateMeter(
            UpdateMeterInput input) {
    	
    	Future<RpcResult<UpdateMeterOutput>> result = null ;
        
    	// Get only the UpdatedMeter abnd convert it to MeterModInput 
    	MeterModInput ofMeterModInput = MeterConverter.toMeterModInput( input.getUpdatedMeter() );
    	
    	// Call the RPC method on MessageDispatchService
    	    	
    	// For Meter provisioning, the SwitchConnectionDistinguisher is set to null so  
    	// the request can be routed through any connection to the switch
    	
    	SwitchConnectionDistinguisher cookie = null ;
    	// TODO -- Remove the comment when appropriate return type is supported by layers below
    	//Future<RpcResult<UpdateMeterOutput>> result = MessageDispatchServiceImpl.meterMod(ofMeterModInput, cookie) ;
    	
    	return result ;
    }

    public NodeId getNodeId() {
        return nodeId;
    }
}
