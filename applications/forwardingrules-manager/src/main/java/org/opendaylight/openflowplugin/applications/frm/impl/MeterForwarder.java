/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
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
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MeterForwarder
 * It implements {@link org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener}
 * for WildCardedPath to {@link Meter} and ForwardingRulesCommiter interface for methods:
 * add, update and remove {@link Meter} processing for
 * {@link org.opendaylight.controller.md.sal.binding.api.DataTreeModification}.
 *
 */
public class MeterForwarder extends AbstractListeningCommiter<Meter> {

    private static final Logger LOG = LoggerFactory.getLogger(MeterForwarder.class);
    private final DataBroker dataBroker;
    private ListenerRegistration<MeterForwarder> listenerRegistration;

    public MeterForwarder (final ForwardingRulesManager manager, final DataBroker db) {
        super(manager, Meter.class);
        dataBroker = Preconditions.checkNotNull(db, "DataBroker can not be null!");
        final DataTreeIdentifier<Meter> treeId = new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, getWildCardPath());

        try {
            SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(ForwardingRulesManagerImpl.STARTUP_LOOP_TICK,
                    ForwardingRulesManagerImpl.STARTUP_LOOP_MAX_RETRIES);
            listenerRegistration = looper.loopUntilNoException(new Callable<ListenerRegistration<MeterForwarder>>() {
                @Override
                public ListenerRegistration<MeterForwarder> call() throws Exception {
                    return db.registerDataTreeChangeListener(treeId, MeterForwarder.this);
                }
            });
        } catch (final Exception e) {
            LOG.warn("FRM Meter DataTreeChange listener registration fail!");
            LOG.debug("FRM Meter DataTreeChange listener registration fail ..", e);
            throw new IllegalStateException("MeterForwarder startup fail! System needs restart.", e);
        }
    }

    @Override
    public void close() {
        if (listenerRegistration != null) {
            try {
                listenerRegistration.close();
            } catch (Exception e) {
                LOG.warn("Error by stop FRM MeterChangeListener.{}", e.getMessage());
                LOG.debug("Error by stop FRM MeterChangeListener..", e);
            }
            listenerRegistration = null;
        }
    }

    @Override
    protected InstanceIdentifier<Meter> getWildCardPath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class)
                .augmentation(FlowCapableNode.class).child(Meter.class);
    }

    @Override
    public void remove(final InstanceIdentifier<Meter> identifier, final Meter removeDataObj,
                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final RemoveMeterInputBuilder builder = new RemoveMeterInputBuilder(removeDataObj);

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setMeterRef(new MeterRef(identifier));
        builder.setTransactionUri(new Uri(provider.getNewTransactionId()));
        this.provider.getSalMeterService().removeMeter(builder.build());
    }


    @Override
    public Future<RpcResult<RemoveMeterOutput>> removeWithResult(final InstanceIdentifier<Meter> identifier, final Meter removeDataObj,
                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final RemoveMeterInputBuilder builder = new RemoveMeterInputBuilder(removeDataObj);

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setMeterRef(new MeterRef(identifier));
        builder.setTransactionUri(new Uri(provider.getNewTransactionId()));
        return this.provider.getSalMeterService().removeMeter(builder.build());
    }

    @Override
    public void update(final InstanceIdentifier<Meter> identifier,
                       final Meter original, final Meter update,
                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final UpdateMeterInputBuilder builder = new UpdateMeterInputBuilder();

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setMeterRef(new MeterRef(identifier));
        builder.setTransactionUri(new Uri(provider.getNewTransactionId()));
        builder.setUpdatedMeter((new UpdatedMeterBuilder(update)).build());
        builder.setOriginalMeter((new OriginalMeterBuilder(original)).build());

        this.provider.getSalMeterService().updateMeter(builder.build());
    }

    @Override
    public Future<RpcResult<AddMeterOutput>> add(
        final InstanceIdentifier<Meter> identifier, final Meter addDataObj,
        final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final AddMeterInputBuilder builder = new AddMeterInputBuilder(addDataObj);

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setMeterRef(new MeterRef(identifier));
        builder.setTransactionUri(new Uri(provider.getNewTransactionId()));
        return this.provider.getSalMeterService().addMeter(builder.build());
    }

    @Override
    public void createStaleMarkEntity(InstanceIdentifier<Meter> identifier, Meter del, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.debug("Creating Stale-Mark entry for the switch {} for meter {} ", nodeIdent.toString(), del.toString());
        StaleMeter staleMeter = makeStaleMeter(identifier, del, nodeIdent);
        persistStaleMeter(staleMeter, nodeIdent);
    }

    private StaleMeter makeStaleMeter(InstanceIdentifier<Meter> identifier, Meter del, InstanceIdentifier<FlowCapableNode> nodeIdent){
        StaleMeterBuilder staleMeterBuilder = new StaleMeterBuilder(del);
        return staleMeterBuilder.setMeterId(del.getMeterId()).build();

    }

    private void persistStaleMeter(StaleMeter staleMeter, InstanceIdentifier<FlowCapableNode> nodeIdent){
        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, getStaleMeterInstanceIdentifier(staleMeter, nodeIdent), staleMeter, false);

        CheckedFuture<Void, TransactionCommitFailedException> submitFuture = writeTransaction.submit();
        handleStaleMeterResultFuture(submitFuture);


    }

    private void handleStaleMeterResultFuture(CheckedFuture<Void, TransactionCommitFailedException> submitFuture){
        Futures.addCallback(submitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                LOG.debug("Stale Meter creation success");
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.error("Stale Meter creation failed {}", t);
            }
        });


    }


    private InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.StaleMeter> getStaleMeterInstanceIdentifier(StaleMeter staleMeter, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return nodeIdent
                .child(StaleMeter.class, new StaleMeterKey(new MeterId(staleMeter.getMeterId())));
    }

}

