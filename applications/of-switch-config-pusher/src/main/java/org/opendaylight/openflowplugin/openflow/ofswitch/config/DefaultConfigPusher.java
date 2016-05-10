/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.ofswitch.config;

import java.util.Set;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
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
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by Martin Bobak mbobak@cisco.com on 10/14/14.
 */
public class DefaultConfigPusher implements AutoCloseable, DataChangeListener {

    private final NodeConfigService nodeConfigService;
    private final DataBroker dataBroker;
    private ListenerRegistration<DataChangeListener> dataChangeListenerRegistration;

    public DefaultConfigPusher(NodeConfigService nodeConfigService, DataBroker dataBroker) {
        this.nodeConfigService = nodeConfigService;
        this.dataBroker = dataBroker;
    }

    public void start() {
        InstanceIdentifier<FlowCapableNode> path = InstanceIdentifier.create(Nodes.class).child(Node.class).
                augmentation(FlowCapableNode.class);
        dataChangeListenerRegistration = dataBroker.registerDataChangeListener(
                LogicalDatastoreType.OPERATIONAL,
                path, this, AsyncDataBroker.DataChangeScope.BASE);
    }

    @Override
    public void close() {
        if(dataChangeListenerRegistration != null) {
            dataChangeListenerRegistration.close();
        }
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        final Set<InstanceIdentifier<?>> changedDataKeys = change.getCreatedData().keySet();

        if (changedDataKeys != null) {
            for (InstanceIdentifier<?> key : changedDataKeys) {
                SetConfigInputBuilder setConfigInputBuilder = new SetConfigInputBuilder();
                setConfigInputBuilder.setFlag(SwitchConfigFlag.FRAGNORMAL.toString());
                setConfigInputBuilder.setMissSearchLength(OFConstants.OFPCML_NO_BUFFER);
                setConfigInputBuilder.setNode(new NodeRef(key.firstIdentifierOf(Node.class)));
                nodeConfigService.setConfig(setConfigInputBuilder.build());
            }
        }

    }
}
