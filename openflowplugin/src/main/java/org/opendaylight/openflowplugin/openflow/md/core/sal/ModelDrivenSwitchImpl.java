package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.spi.LoggerFactory;
import org.opendaylight.openflowplugin.openflow.md.ModelDrivenSwitch;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.flow.mod.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.flow.mod.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;

public class ModelDrivenSwitchImpl extends AbstractModelDrivenSwitch {

    public static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ModelDrivenSwitchImpl.class);
    private final NodeId nodeId;

    protected ModelDrivenSwitchImpl(NodeId nodeId, InstanceIdentifier<Node> identifier, SessionContext context) {
        super(identifier, context);
        this.nodeId = nodeId;
    }

    @Override
    public Future<RpcResult<Void>> addFlow(AddFlowInput input) {
        // TODO Auto-generated method stub

        return null;
    }

    @Override
    public Future<RpcResult<Void>> addGroup(AddGroupInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> addMeter(AddMeterInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> removeFlow(RemoveFlowInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> removeGroup(RemoveGroupInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> removeMeter(RemoveMeterInput input) {
        // TODO Auto-generated method stub
        return null;
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

    private Match toMatch(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match match) {
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> updateGroup(UpdateGroupInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> updateMeter(UpdateMeterInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    public NodeId getNodeId() {
        return nodeId;
    }
}
