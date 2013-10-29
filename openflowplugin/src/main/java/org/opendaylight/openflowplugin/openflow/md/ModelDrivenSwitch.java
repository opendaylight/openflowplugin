package org.opendaylight.openflowplugin.openflow.md;

import java.util.concurrent.Future;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

public interface ModelDrivenSwitch extends //
        SalGroupService, //
        SalFlowService, //
        SalMeterService, //
        PacketProcessingService, //
        Identifiable<InstanceIdentifier<Node>> {

    CompositeObjectRegistration<ModelDrivenSwitch> register(ProviderContext ctx);

    @Override
    public InstanceIdentifier<Node> getIdentifier();

    public NodeId getNodeId();

    @Override
    public Future<RpcResult<Void>> addFlow(AddFlowInput input);

    @Override
    public Future<RpcResult<Void>> removeFlow(RemoveFlowInput input);

    @Override
    public Future<RpcResult<Void>> transmitPacket(TransmitPacketInput input);

    @Override
    public Future<RpcResult<Void>> updateFlow(UpdateFlowInput input);

    @Override
    public Future<RpcResult<Void>> addGroup(AddGroupInput input);

    @Override
    public Future<RpcResult<Void>> addMeter(AddMeterInput input);

    @Override
    public Future<RpcResult<Void>> removeGroup(RemoveGroupInput input);

    @Override
    public Future<RpcResult<Void>> removeMeter(RemoveMeterInput input);

    @Override
    public Future<RpcResult<Void>> updateGroup(UpdateGroupInput input);

    @Override
    public Future<RpcResult<Void>> updateMeter(UpdateMeterInput input);
}
