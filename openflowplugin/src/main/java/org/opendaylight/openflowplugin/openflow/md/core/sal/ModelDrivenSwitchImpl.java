/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;

/**
 * RPC implementation of MD-switch
 */
public class ModelDrivenSwitchImpl extends AbstractModelDrivenSwitch {

    private static final Logger LOG = org.slf4j.LoggerFactory
            .getLogger(ModelDrivenSwitchImpl.class);
    private final NodeId nodeId;

    protected ModelDrivenSwitchImpl(NodeId nodeId,
            InstanceIdentifier<Node> identifier, SessionContext context) {
        super(identifier, context);
        this.nodeId = nodeId;
    }

    @Override
    public Future<RpcResult<Void>> addFlow(AddFlowInput input) {
        // TODO Auto-generated method stub

        return null;
    }

    @Override
    public Future<RpcResult<AddGroupOutput>> addGroup(AddGroupInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<AddMeterOutput>> addMeter(AddMeterInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> removeFlow(RemoveFlowInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<RemoveGroupOutput>> removeGroup(
            RemoveGroupInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<RemoveMeterOutput>> removeMeter(
            RemoveMeterInput input) {
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<UpdateGroupOutput>> updateGroup(
            UpdateGroupInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<UpdateMeterOutput>> updateMeter(
            UpdateMeterInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }
}
