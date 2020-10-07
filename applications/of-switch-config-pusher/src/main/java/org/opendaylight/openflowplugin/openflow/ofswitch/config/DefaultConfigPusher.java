/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.ofswitch.config;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.opendaylight.infrautils.utils.concurrent.LoggingFutures;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.applications.deviceownershipservice.DeviceOwnershipService;
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

@Singleton
public class DefaultConfigPusher implements AutoCloseable, ClusteredDataTreeChangeListener<FlowCapableNode> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultConfigPusher.class);
    private static final long STARTUP_LOOP_TICK = 500L;
    private static final int STARTUP_LOOP_MAX_RETRIES = 8;
    private final NodeConfigService nodeConfigService;
    private final DataBroker dataBroker;
    private final DeviceOwnershipService deviceOwnershipService;
    private ListenerRegistration<?> listenerRegistration;

    @Inject
    public DefaultConfigPusher(final NodeConfigService nodeConfigService, @Reference final DataBroker dataBroker,
            @Reference final DeviceOwnershipService deviceOwnershipService) {
        this.nodeConfigService = nodeConfigService;
        this.dataBroker = dataBroker;
        this.deviceOwnershipService = requireNonNull(deviceOwnershipService, "DeviceOwnershipService can not be null");
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @PostConstruct
    public void start() {
        try {
            final InstanceIdentifier<FlowCapableNode> path = InstanceIdentifier.create(Nodes.class).child(Node.class)
                    .augmentation(FlowCapableNode.class);
            final DataTreeIdentifier<FlowCapableNode> identifier = DataTreeIdentifier.create(
                    LogicalDatastoreType.OPERATIONAL, path);
            final SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(STARTUP_LOOP_TICK, STARTUP_LOOP_MAX_RETRIES);
            listenerRegistration = looper.loopUntilNoException(
                () -> dataBroker.registerDataTreeChangeListener(identifier, DefaultConfigPusher.this));
        } catch (Exception e) {
            LOG.error("DataTreeChangeListener registration failed", e);
            throw new IllegalStateException("DefaultConfigPusher startup failed!", e);
        }
        LOG.info("DefaultConfigPusher has started.");
    }

    @Override
    @PreDestroy
    public void close() {
        if (listenerRegistration != null) {
            listenerRegistration.close();
        }
    }

    @Override
    public void onDataTreeChanged(final Collection<DataTreeModification<FlowCapableNode>> modifications) {
        for (DataTreeModification<FlowCapableNode> modification : modifications) {
            if (modification.getRootNode().getModificationType() == ModificationType.WRITE) {
                String nodeId = modification.getRootPath().getRootIdentifier()
                        .firstKeyOf(Node.class).getId().getValue();
                if (deviceOwnershipService.isEntityOwned(nodeId)) {
                    SetConfigInputBuilder setConfigInputBuilder = new SetConfigInputBuilder();
                    setConfigInputBuilder.setFlag(SwitchConfigFlag.FRAGNORMAL.toString());
                    setConfigInputBuilder.setMissSearchLength(OFConstants.OFPCML_NO_BUFFER);
                    setConfigInputBuilder.setNode(new NodeRef(modification.getRootPath()
                            .getRootIdentifier().firstIdentifierOf(Node.class)));
                    LoggingFutures.addErrorLogging(nodeConfigService.setConfig(setConfigInputBuilder.build()),
                            LOG, "addFlow");
                } else {
                    LOG.debug("Node {} is not owned by this controller, so skip setting config", nodeId);
                }
            }
        }
    }

}
