/**
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.ofswitch.config;

import java.util.Collection;
import java.util.concurrent.Callable;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.NodeConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.SwitchConfigFlag;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConfigPusher implements AutoCloseable, DataTreeChangeListener<FlowCapableNode> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultConfigPusher.class);
    private static final long STARTUP_LOOP_TICK = 500L;
    private static final int STARTUP_LOOP_MAX_RETRIES = 8;
    private final NodeConfigService nodeConfigService;
    private final DataBroker dataBroker;
    private ListenerRegistration<DataTreeChangeListener> listenerRegistration;

    public DefaultConfigPusher(NodeConfigService nodeConfigService, DataBroker dataBroker) {
        this.nodeConfigService = nodeConfigService;
        this.dataBroker = dataBroker;
    }

    public void start() {
        try {
            final InstanceIdentifier<FlowCapableNode> path = InstanceIdentifier.create(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class);
            final DataTreeIdentifier<FlowCapableNode> identifier = new DataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, path);
            final SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(STARTUP_LOOP_TICK, STARTUP_LOOP_MAX_RETRIES);
            listenerRegistration = looper.loopUntilNoException(new Callable<ListenerRegistration<DataTreeChangeListener>>() {
                @Override
                public ListenerRegistration<DataTreeChangeListener> call() throws Exception {
                    return dataBroker.registerDataTreeChangeListener(identifier, DefaultConfigPusher.this);
                }
            });
        } catch (Exception e) {
            LOG.error("DataTreeChangeListener registration failed: {}", e);
            throw new IllegalStateException("DefaultConfigPusher startup failed!", e);
        }
        LOG.info("DefaultConfigPusher has started.");
    }

    @Override
    public void close() {
        if(listenerRegistration != null) {
            listenerRegistration.close();
        }
    }

    @Override
    public void onDataTreeChanged(@Nonnull final Collection<DataTreeModification<FlowCapableNode>> modifications) {
        for (DataTreeModification modification : modifications) {
            if (modification.getRootNode().getModificationType() == ModificationType.WRITE) {
                SetConfigInputBuilder setConfigInputBuilder = new SetConfigInputBuilder();
                setConfigInputBuilder.setFlag(SwitchConfigFlag.FRAGNORMAL.toString());
                setConfigInputBuilder.setMissSearchLength(OFConstants.OFPCML_NO_BUFFER);
                setConfigInputBuilder.setNode(new NodeRef(modification.getRootPath().getRootIdentifier().firstIdentifierOf(Node.class)));
                nodeConfigService.setConfig(setConfigInputBuilder.build());
            }
        }
    }

}
