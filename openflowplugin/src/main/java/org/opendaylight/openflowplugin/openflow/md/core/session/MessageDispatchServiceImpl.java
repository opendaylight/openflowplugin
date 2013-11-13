package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Future;

 import org.opendaylight.controller.sal.common.util.Rpcs;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInput;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;

/**
 * message dispatch service to send the message to switch.
 *
 * @author AnilGujele
 *
 */
public class MessageDispatchServiceImpl implements IMessageDispatchService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageDispatchServiceImpl.class);

    private SessionContext session;

    /**
     * constructor
     *
     * @param session
     *            - MessageDispatchService for this session
     */
    public MessageDispatchServiceImpl(SessionContext session) {
        this.session = session;
    }

    /**
     * get proper connection adapter to send the message to switch.
     *
     * @param - cookie to identify the right connection, it can be null also.
     * @return connectionAdapter associated with cookie, otherwise return best
     *         suitable connection.
     *
     */

    private ConnectionAdapter getConnectionAdapter(SwitchConnectionDistinguisher cookie) {

        if (!session.isValid()) {
            LOG.warn("Session for the cookie {} is invalid.", cookie);
            throw new IllegalArgumentException("Session for the cookie is invalid.");
        }
        LOG.debug("finding connecton for cookie value {}. ", cookie);
        // set main connection as default
        ConnectionAdapter connectionAdapter = session.getPrimaryConductor().getConnectionAdapter();
        if (null != cookie) {
            ConnectionConductor conductor = session.getAuxiliaryConductor(cookie);
            // check if auxiliary connection exist
            if (null != conductor) {
                LOG.debug("found auxiliary connection for the cookie.");
                connectionAdapter = conductor.getConnectionAdapter();
            }
        } else {
            // TODO: pick connection to utilize all the available connection.
        }
        return connectionAdapter;
    }

    @Override
    public Future<RpcResult<BarrierOutput>> barrier(BarrierInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).barrier(input);
    }

    @Override
    public Future<RpcResult<Void>> experimenter(ExperimenterInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).experimenter(input);
    }

    @Override
    public Future<RpcResult<UpdateFlowOutput>> flowMod(FlowModInput input, SwitchConnectionDistinguisher cookie) {
        
        // Set Xid before invoking RPC on OFLibrary
        // TODO : Cleaner approach is to use a copy constructor once it is implemented 
        Long Xid = session.getNextXid();
        FlowModInputBuilder mdInput = new FlowModInputBuilder();
        mdInput.setXid(Xid);
        mdInput.setBufferId(input.getBufferId());
        mdInput.setCommand(input.getCommand());
        mdInput.setCookie(input.getCookie());
        mdInput.setCookieMask(input.getCookieMask());
        mdInput.setFlags(input.getFlags());
        mdInput.setHardTimeout(input.getHardTimeout());
        mdInput.setIdleTimeout(input.getHardTimeout());
        mdInput.setMatch(input.getMatch());
        mdInput.setOutGroup(input.getOutGroup());
        mdInput.setOutPort(input.getOutPort());
        mdInput.setPriority(input.getPriority());
        mdInput.setTableId(input.getTableId());
        mdInput.setVersion(input.getVersion());
        Future<RpcResult<Void>> response = getConnectionAdapter(cookie).flowMod(mdInput.build());
        
        // Send the same Xid back to caller - MessageDrivenSwitch
        UpdateFlowOutputBuilder flowModOutput = new UpdateFlowOutputBuilder();
        flowModOutput.setXid(Xid);
        UpdateFlowOutput result = flowModOutput.build();
        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<UpdateFlowOutput> rpcResult = Rpcs.getRpcResult(true, result, errors);
        
        // solution 1: sending directly and hooking listener to get error
        // hookup listener to catch the possible error with no reference to returned future-object
        return Futures.immediateFuture(rpcResult);
        
    }

    @Override
    public Future<RpcResult<GetAsyncOutput>> getAsync(GetAsyncInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).getAsync(input);
    }

    @Override
    public Future<RpcResult<GetConfigOutput>> getConfig(GetConfigInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).getConfig(input);
    }

    @Override
    public Future<RpcResult<GetFeaturesOutput>> getFeatures(GetFeaturesInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).getFeatures(input);
    }

    @Override
    public Future<RpcResult<GetQueueConfigOutput>> getQueueConfig(GetQueueConfigInput input,
            SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).getQueueConfig(input);
    }

    @Override
    public Future<RpcResult<UpdateGroupOutput>> groupMod(GroupModInput input, SwitchConnectionDistinguisher cookie) {
        
        // Set Xid before invoking RPC on OFLibrary
        // TODO : Cleaner approach is to use a copy constructor once it is implemented
        Long Xid = session.getNextXid();
        GroupModInputBuilder mdInput = new GroupModInputBuilder();
        mdInput.setXid(Xid);
        mdInput.setBuckets(input.getBuckets());
        mdInput.setCommand(input.getCommand());
        mdInput.setGroupId(input.getGroupId());
        mdInput.setType(input.getType());
        mdInput.setVersion(input.getVersion());
        Future<RpcResult<Void>> response = getConnectionAdapter(cookie).groupMod(mdInput.build());
        
        // Send the same Xid back to caller - MessageDrivenSwitch
        UpdateGroupOutputBuilder groupModOutput = new UpdateGroupOutputBuilder();
        groupModOutput.setXid(Xid);
        UpdateGroupOutput result = groupModOutput.build();
        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<UpdateGroupOutput> rpcResult = Rpcs.getRpcResult(true, result, errors);
        
        // solution 1: sending directly and hooking listener to get error
        // hookup listener to catch the possible error with no reference to returned future-object
        return Futures.immediateFuture(rpcResult);
        
    }

    @Override
    public Future<RpcResult<UpdateMeterOutput>> meterMod(MeterModInput input, SwitchConnectionDistinguisher cookie) {
        
        // Set Xid before invoking RPC on OFLibrary
        // TODO : Cleaner approach is to use a copy constructor once it is implemented
        Long Xid = session.getNextXid();
        MeterModInputBuilder mdInput = new MeterModInputBuilder();
        mdInput.setXid(Xid);
        mdInput.setBands(input.getBands());
        mdInput.setCommand(input.getCommand());
        mdInput.setFlags(input.getFlags());
        mdInput.setMeterId(input.getMeterId());
        mdInput.setVersion(input.getVersion());
        mdInput.setVersion(input.getVersion());
        Future<RpcResult<Void>> response = getConnectionAdapter(cookie).meterMod(mdInput.build());
        
        // Send the same Xid back to caller - MessageDrivenSwitch
        UpdateMeterOutputBuilder meterModOutput = new UpdateMeterOutputBuilder();
        meterModOutput.setXid(Xid);
        UpdateMeterOutput result = meterModOutput.build();
        Collection<RpcError> errors = Collections.emptyList();
        RpcResult<UpdateMeterModOutput> rpcResult = Rpcs.getRpcResult(true, result, errors);
        
        // solution 1: sending directly and hooking listener to get error
        // hookup listener to catch the possible error with no reference to returned future-object
        return Futures.immediateFuture(rpcResult);
        
    }

    @Override
    public Future<RpcResult<Void>> packetOut(PacketOutInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).packetOut(input);
    }

    @Override
    public Future<RpcResult<Void>> portMod(PortModInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).portMod(input);
    }

    @Override
    public Future<RpcResult<RoleRequestOutput>> roleRequest(RoleRequestInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).roleRequest(input);
    }

    @Override
    public Future<RpcResult<Void>> setAsync(SetAsyncInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).setAsync(input);
    }

    @Override
    public Future<RpcResult<Void>> setConfig(SetConfigInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).setConfig(input);
    }

    @Override
    public Future<RpcResult<Void>> tableMod(TableModInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).tableMod(input);
    }

}
