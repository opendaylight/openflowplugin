/**
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.ofswitch.config;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.NodeConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.SwitchConfigFlag;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DefaultConfigPusher implements AutoCloseable, DataTreeChangeListener<FlowCapableNode> {

    private final NodeConfigService nodeConfigService;
    private final DataBroker dataBroker;
    private ListenerRegistration<DataTreeChangeListener> listenerRegistration;

    public DefaultConfigPusher(NodeConfigService nodeConfigService, DataBroker dataBroker) {
        this.nodeConfigService = nodeConfigService;
        this.dataBroker = dataBroker;
    }

    public void start() {
        InstanceIdentifier<FlowCapableNode> path = InstanceIdentifier.create(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class);
        DataTreeIdentifier<FlowCapableNode> identifier = new DataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, path);
        listenerRegistration = dataBroker.registerDataTreeChangeListener(identifier, this);
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
