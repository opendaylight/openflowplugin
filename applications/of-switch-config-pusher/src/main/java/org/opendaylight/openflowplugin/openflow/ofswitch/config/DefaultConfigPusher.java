/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.ofswitch.config;

import static java.util.Objects.requireNonNull;

import java.util.List;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.utils.concurrent.LoggingFutures;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.applications.deviceownershipservice.DeviceOwnershipService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.SwitchConfigFlag;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = { })
public final class DefaultConfigPusher implements AutoCloseable, DataTreeChangeListener<FlowCapableNode> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultConfigPusher.class);

    private final DeviceOwnershipService deviceOwnershipService;
    private final SetConfig setConfig;
    private final Registration reg;

    @Inject
    @Activate
    public DefaultConfigPusher(@Reference final DataBroker dataBroker, @Reference final RpcService rpcService,
            @Reference final DeviceOwnershipService deviceOwnershipService) {
        this.deviceOwnershipService = requireNonNull(deviceOwnershipService);
        setConfig = rpcService.getRpc(SetConfig.class);
        reg = dataBroker.registerTreeChangeListener(LogicalDatastoreType.OPERATIONAL,
            DataObjectReference.builder(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class).build(),
            this);
        LOG.info("DefaultConfigPusher has started.");
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        reg.close();
    }

    @Override
    public void onDataTreeChanged(final List<DataTreeModification<FlowCapableNode>> modifications) {
        for (var modification : modifications) {
            if (modification.getRootNode().modificationType() == ModificationType.WRITE) {
                final var nodeId = modification.path().getFirstKeyOf(Node.class).getId().getValue();
                if (deviceOwnershipService.isEntityOwned(nodeId)) {
                    LoggingFutures.addErrorLogging(setConfig.invoke(new SetConfigInputBuilder()
                        .setFlag(SwitchConfigFlag.FRAGNORMAL.toString())
                        .setMissSearchLength(OFConstants.OFPCML_NO_BUFFER)
                        .setNode(new NodeRef(((DataObjectIdentifier<?>) modification.path()).trimTo(Node.class)))
                        .build()), LOG, "addFlow");
                } else {
                    LOG.debug("Node {} is not owned by this controller, so skip setting config", nodeId);
                }
            }
        }
    }
}
