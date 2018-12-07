/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Future;
import org.opendaylight.infrautils.utils.concurrent.LoggingFutures;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
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

    public MeterForwarder(final ForwardingRulesManager manager, final DataBroker db,
                          final ListenerRegistrationHelper listenerRegistrationHelper) {
        super(manager, db, listenerRegistrationHelper);
    }

    @Override
    public void deregisterListener() {
        close();
    }

    @Override
    public void close() {
        if (listenerRegistration != null) {
            listenerRegistration.close();
            listenerRegistration = null;
        }
    }

    @Override
    protected InstanceIdentifier<Meter> getWildCardPath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class)
                .child(Meter.class);
    }

    @Override
    public void remove(final InstanceIdentifier<Meter> identifier, final Meter removeDataObj,
            final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final RemoveMeterInputBuilder builder = new RemoveMeterInputBuilder(removeDataObj);

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setMeterRef(new MeterRef(identifier));
        builder.setTransactionUri(new Uri(provider.getNewTransactionId()));

        LoggingFutures.addErrorLogging(this.provider.getSalMeterService().removeMeter(builder.build()), LOG,
            "removeMeter");
    }

    @Override
    public Future<RpcResult<RemoveMeterOutput>> removeWithResult(final InstanceIdentifier<Meter> identifier,
            final Meter removeDataObj, final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final RemoveMeterInputBuilder builder = new RemoveMeterInputBuilder(removeDataObj);

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setMeterRef(new MeterRef(identifier));
        builder.setTransactionUri(new Uri(provider.getNewTransactionId()));
        return this.provider.getSalMeterService().removeMeter(builder.build());
    }

    @Override
    public void update(final InstanceIdentifier<Meter> identifier, final Meter original, final Meter update,
            final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final UpdateMeterInputBuilder builder = new UpdateMeterInputBuilder();

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setMeterRef(new MeterRef(identifier));
        builder.setTransactionUri(new Uri(provider.getNewTransactionId()));
        builder.setUpdatedMeter(new UpdatedMeterBuilder(update).build());
        builder.setOriginalMeter(new OriginalMeterBuilder(original).build());

        LoggingFutures.addErrorLogging(this.provider.getSalMeterService().updateMeter(builder.build()), LOG,
            "updateMeter");
    }

    @Override
    public Future<RpcResult<AddMeterOutput>> add(final InstanceIdentifier<Meter> identifier, final Meter addDataObj,
            final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final AddMeterInputBuilder builder = new AddMeterInputBuilder(addDataObj);

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setMeterRef(new MeterRef(identifier));
        builder.setTransactionUri(new Uri(provider.getNewTransactionId()));
        return this.provider.getSalMeterService().addMeter(builder.build());
    }

    @Override
    public void createStaleMarkEntity(InstanceIdentifier<Meter> identifier, Meter del,
            InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.debug("Creating Stale-Mark entry for the switch {} for meter {} ", nodeIdent, del);
        StaleMeter staleMeter = makeStaleMeter(del);
        persistStaleMeter(staleMeter, nodeIdent);
    }

    private static StaleMeter makeStaleMeter(Meter del) {
        StaleMeterBuilder staleMeterBuilder = new StaleMeterBuilder(del);
        return staleMeterBuilder.setMeterId(del.getMeterId()).build();
    }

    private void persistStaleMeter(StaleMeter staleMeter, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, getStaleMeterInstanceIdentifier(staleMeter, nodeIdent),
                staleMeter);

        FluentFuture<?> submitFuture = writeTransaction.commit();
        handleStaleMeterResultFuture(submitFuture);
    }

    private static void handleStaleMeterResultFuture(FluentFuture<?> submitFuture) {
        submitFuture.addCallback(new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                LOG.debug("Stale Meter creation success");
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.error("Stale Meter creation failed", throwable);
            }
        }, MoreExecutors.directExecutor());
    }

    private static InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819
        .meters.StaleMeter> getStaleMeterInstanceIdentifier(
            StaleMeter staleMeter, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return nodeIdent.child(StaleMeter.class, new StaleMeterKey(new MeterId(staleMeter.getMeterId())));
    }
}
