/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.datastore.multipart;

import java.util.Collections;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.impl.util.DeviceInitializationUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.Desc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DescMultipartWriter extends AbstractMultipartWriter<Desc> {

    private final ConnectionContext connectionContext;

    public DescMultipartWriter(final TxFacade txFacade,
                               final InstanceIdentifier<Node> instanceIdentifier,
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
        writeToTransaction(getInstanceIdentifier()
                .augmentation(FlowCapableNode.class),
            new FlowCapableNodeBuilder(statistics)
                .setTable(Collections.emptyList())
                .setMeter(Collections.emptyList())
                .setGroup(Collections.emptyList())
                .setIpAddress(DeviceInitializationUtil.getIpAddress(connectionContext, getInstanceIdentifier()))
                .setPortNumber(DeviceInitializationUtil.getPortNumber(connectionContext, getInstanceIdentifier()))
                .setSwitchFeatures(DeviceInitializationUtil.getSwitchFeatures(connectionContext))
                .build(),
            withParents);
    }
}
