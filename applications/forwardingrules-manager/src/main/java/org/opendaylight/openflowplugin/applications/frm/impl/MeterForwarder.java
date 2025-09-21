/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.opendaylight.infrautils.utils.concurrent.LoggingFutures;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.StaleMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.StaleMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.StaleMeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.OriginalMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.UpdatedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterRef;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MeterForwarder It implements
 * {@link org.opendaylight.mdsal.binding.api.DataTreeChangeListener}
 * for WildCardedPath to {@link Meter} and ForwardingRulesCommiter interface for
 * methods: add, update and remove {@link Meter} processing for
 * {@link org.opendaylight.mdsal.binding.api.DataTreeModification}.
 *
 */
public class MeterForwarder extends AbstractListeningCommiter<Meter> {
    private static final Logger LOG = LoggerFactory.getLogger(MeterForwarder.class);

    public MeterForwarder(final ForwardingRulesManager manager, final DataBroker dataBroker) {
        super(manager, dataBroker);
    }

    @Override
    protected DataObjectReference<Meter> getWildCardPath() {
        return DataObjectReference.builder(Nodes.class)
            .child(Node.class)
            .augmentation(FlowCapableNode.class)
            .child(Meter.class)
            .build();
    }

    @Override
    public void remove(final DataObjectIdentifier<Meter> identifier, final Meter removeDataObj,
            final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        LoggingFutures.addErrorLogging(provider.removeMeter().invoke(new RemoveMeterInputBuilder(removeDataObj)
                .setNode(new NodeRef(nodeIdent.trimTo(Node.class)))
                .setMeterRef(new MeterRef(identifier))
                .setTransactionUri(new Uri(provider.getNewTransactionId()))
                .build()), LOG, "removeMeter");
    }

    @Override
    public ListenableFuture<RpcResult<RemoveMeterOutput>> removeWithResult(final DataObjectIdentifier<Meter> identifier,
            final Meter removeDataObj, final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        return provider.removeMeter().invoke(new RemoveMeterInputBuilder(removeDataObj)
            .setNode(new NodeRef(nodeIdent.trimTo(Node.class)))
            .setMeterRef(new MeterRef(identifier))
            .setTransactionUri(new Uri(provider.getNewTransactionId()))
            .build());
    }

    @Override
    public void update(final DataObjectIdentifier<Meter> identifier, final Meter original, final Meter update,
            final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        LoggingFutures.addErrorLogging(provider.updateMeter().invoke(new UpdateMeterInputBuilder()
            .setNode(new NodeRef(nodeIdent.trimTo(Node.class)))
            .setMeterRef(new MeterRef(identifier))
            .setTransactionUri(new Uri(provider.getNewTransactionId()))
            .setUpdatedMeter(new UpdatedMeterBuilder(update).build())
            .setOriginalMeter(new OriginalMeterBuilder(original).build())
            .build()), LOG, "updateMeter");
    }

    @Override
    public ListenableFuture<RpcResult<AddMeterOutput>> add(final DataObjectIdentifier<Meter> identifier,
            final Meter addDataObj, final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        return provider.addMeter().invoke(new AddMeterInputBuilder(addDataObj)
            .setNode(new NodeRef(nodeIdent.trimTo(Node.class)))
            .setMeterRef(new MeterRef(identifier))
            .setTransactionUri(new Uri(provider.getNewTransactionId()))
            .build());
    }

    @Override
    public void createStaleMarkEntity(final DataObjectIdentifier<Meter> identifier, final Meter del,
            final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        LOG.debug("Creating Stale-Mark entry for the switch {} for meter {} ", nodeIdent, del);

        final var staleMeter = new StaleMeterBuilder(del).setMeterId(del.getMeterId()).build();
        final var writeTransaction = dataBroker.newWriteOnlyTransaction();
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, nodeIdent.toBuilder()
            .child(StaleMeter.class, new StaleMeterKey(new MeterId(staleMeter.getMeterId())))
            .build(), staleMeter);
        writeTransaction.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(final CommitInfo result) {
                LOG.debug("Stale Meter creation success");
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("Stale Meter creation failed", throwable);
            }
        }, MoreExecutors.directExecutor());
    }
}
