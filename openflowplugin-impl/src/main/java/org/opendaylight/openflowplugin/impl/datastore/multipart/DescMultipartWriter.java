/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.datastore.multipart;

import java.util.Map;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.impl.util.DeviceInitializationUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.Desc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;

public class DescMultipartWriter extends AbstractMultipartWriter<Desc> {
    private final ConnectionContext connectionContext;

    public DescMultipartWriter(final TxFacade txFacade,
                               final WithKey<Node, NodeKey> instanceIdentifier,
                               final ConnectionContext connectionContext) {
        super(txFacade, instanceIdentifier);
        this.connectionContext = connectionContext;
    }

    @Override
    protected Class<Desc> getType() {
        return Desc.class;
    }

    @Override
    public void storeStatistics(final Desc statistics, final boolean withParents) {
        writeToTransaction(getInstanceIdentifier().toBuilder().augmentation(FlowCapableNode.class).build(),
            new FlowCapableNodeBuilder(statistics)
                .setTable(Map.of())
                .setMeter(Map.of())
                .setGroup(Map.of())
                .setIpAddress(DeviceInitializationUtil.getIpAddress(connectionContext, getInstanceIdentifier()))
                .setPortNumber(DeviceInitializationUtil.getPortNumber(connectionContext, getInstanceIdentifier()))
                .setSwitchFeatures(DeviceInitializationUtil.getSwitchFeatures(connectionContext))
                .build(),
            withParents);
    }
}
