/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.openflowplugin.applications.frsync.ForwardingRulesCommitter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.OriginalMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.UpdatedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterRef;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link ForwardingRulesCommitter} methods for processing add, update and remove of {@link Meter}.
 */
public class MeterForwarder
        implements ForwardingRulesCommitter<Meter, AddMeterOutput, RemoveMeterOutput, UpdateMeterOutput> {
    private static final Logger LOG = LoggerFactory.getLogger(MeterForwarder.class);

    private final RemoveMeter removeMeter;
    private final AddMeter addMeter;
    private final UpdateMeter updateMeter;

    public MeterForwarder(final RpcService rpcConsumerRegistry) {
        addMeter = rpcConsumerRegistry.getRpc(AddMeter.class);
        removeMeter = rpcConsumerRegistry.getRpc(RemoveMeter.class);
        updateMeter = rpcConsumerRegistry.getRpc(UpdateMeter.class);
    }

    @Override
    public ListenableFuture<RpcResult<RemoveMeterOutput>> remove(final InstanceIdentifier<Meter> identifier,
            final Meter removeDataObj, final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.trace("Received the Meter REMOVE request [Tbl id, node Id {} {}", identifier, nodeIdent);
        return removeMeter.invoke(new RemoveMeterInputBuilder(removeDataObj)
            .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class).toIdentifier()))
            .setMeterRef(new MeterRef(identifier.toIdentifier()))
            .build());
    }

    @Override
    public ListenableFuture<RpcResult<UpdateMeterOutput>> update(final InstanceIdentifier<Meter> identifier,
            final Meter original, final Meter update, final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.trace("Received the Meter UPDATE request [Tbl id, node Id {} {} {}", identifier, nodeIdent, update);
        return updateMeter.invoke(new UpdateMeterInputBuilder()
            .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class).toIdentifier()))
            .setMeterRef(new MeterRef(identifier.toIdentifier()))
            .setUpdatedMeter(new UpdatedMeterBuilder(update).build())
            .setOriginalMeter(new OriginalMeterBuilder(original).build())
            .build());
    }

    @Override
    public ListenableFuture<RpcResult<AddMeterOutput>> add(final InstanceIdentifier<Meter> identifier,
            final Meter addDataObj, final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.trace("Received the Meter ADD request [Tbl id, node Id {} {} {}", identifier, nodeIdent, addDataObj);
        return addMeter.invoke(new AddMeterInputBuilder(addDataObj)
            .setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class).toIdentifier()))
            .setMeterRef(new MeterRef(identifier.toIdentifier()))
            .build());
    }
}
