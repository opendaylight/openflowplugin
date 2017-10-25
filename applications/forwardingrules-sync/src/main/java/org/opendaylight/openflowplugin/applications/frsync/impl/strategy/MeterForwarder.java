/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.applications.frsync.ForwardingRulesCommitter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
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
public class MeterForwarder implements ForwardingRulesCommitter<Meter, AddMeterOutput, RemoveMeterOutput, UpdateMeterOutput> {

    private static final Logger LOG = LoggerFactory.getLogger(MeterForwarder.class);
    private final SalMeterService salMeterService;

    public MeterForwarder(SalMeterService salMeterService) {
        this.salMeterService = salMeterService;
    }

    @Override
    public Future<RpcResult<RemoveMeterOutput>> remove(final InstanceIdentifier<Meter> identifier, final Meter removeDataObj,
                                                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        LOG.trace("Received the Meter REMOVE request [Tbl id, node Id {} {}",
                identifier, nodeIdent);

        final RemoveMeterInputBuilder builder = new RemoveMeterInputBuilder(removeDataObj);

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setMeterRef(new MeterRef(identifier));
        return salMeterService.removeMeter(builder.build());
    }

    @Override
    public Future<RpcResult<UpdateMeterOutput>> update(final InstanceIdentifier<Meter> identifier,
                                                       final Meter original, final Meter update,
                                                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.trace("Received the Meter UPDATE request [Tbl id, node Id {} {} {}",
                identifier, nodeIdent, update);

        final UpdateMeterInputBuilder builder = new UpdateMeterInputBuilder();

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setMeterRef(new MeterRef(identifier));
        builder.setUpdatedMeter((new UpdatedMeterBuilder(update)).build());
        builder.setOriginalMeter((new OriginalMeterBuilder(original)).build());

        return salMeterService.updateMeter(builder.build());
    }

    @Override
    public Future<RpcResult<AddMeterOutput>> add(final InstanceIdentifier<Meter> identifier, final Meter addDataObj,
                                                 final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.trace("Received the Meter ADD request [Tbl id, node Id {} {} {}",
                identifier, nodeIdent, addDataObj);

        final AddMeterInputBuilder builder = new AddMeterInputBuilder(addDataObj);

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setMeterRef(new MeterRef(identifier));
        return salMeterService.addMeter(builder.build());
    }

}
