/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.base.Preconditions;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.OriginalMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.UpdatedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterRef;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MeterForwarder
 * It implements {@link org.opendaylight.controller.md.sal.binding.api.DataChangeListener}}
 * for WildCardedPath to {@link Meter} and ForwardingRulesCommiter interface for methods:
 *  add, update and remove {@link Meter} processing for
 *  {@link org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent}.
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 */
public class MeterForwarder extends AbstractListeningCommiter<Meter, AddMeterOutput, RemoveMeterOutput, UpdateMeterOutput> {

    private static final Logger LOG = LoggerFactory.getLogger(MeterForwarder.class);

    private ListenerRegistration<MeterForwarder> listenerRegistration;

    public MeterForwarder (final ForwardingRulesManager manager, final DataBroker db) {
        super(manager, Meter.class);
        Preconditions.checkNotNull(db, "DataBroker can not be null!");
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
            LOG.warn("FRM Meter DataChange listener registration fail!");
            LOG.debug("FRM Meter DataChange listener registration fail ..", e);
            throw new IllegalStateException("FlowForwarder startup fail! System needs restart.", e);
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
    public Future<RpcResult<RemoveMeterOutput>> remove(final InstanceIdentifier<Meter> identifier, final Meter removeDataObj,
                                                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final RemoveMeterInputBuilder builder = new RemoveMeterInputBuilder(removeDataObj);

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setMeterRef(new MeterRef(identifier));
        builder.setTransactionUri(new Uri(provider.getNewTransactionId()));
        return this.provider.getSalMeterService().removeMeter(builder.build());
    }

    @Override
    public Future<RpcResult<UpdateMeterOutput>> update(final InstanceIdentifier<Meter> identifier,
                                                       final Meter original, final Meter update,
                                                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final UpdateMeterInputBuilder builder = new UpdateMeterInputBuilder();

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setMeterRef(new MeterRef(identifier));
        builder.setTransactionUri(new Uri(provider.getNewTransactionId()));
        builder.setUpdatedMeter((new UpdatedMeterBuilder(update)).build());
        builder.setOriginalMeter((new OriginalMeterBuilder(original)).build());

        return this.provider.getSalMeterService().updateMeter(builder.build());
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
}

