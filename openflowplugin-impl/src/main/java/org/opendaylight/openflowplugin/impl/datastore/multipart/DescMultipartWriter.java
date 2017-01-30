/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.datastore.multipart;

import java.net.InetSocketAddress;
import java.util.Collections;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.openflow.md.core.sal.SwitchFeaturesUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.Desc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DescMultipartWriter extends AbstractMultipartWriter<Desc> {

    private static final Logger LOG = LoggerFactory.getLogger(DescMultipartWriter.class);
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
                .setIpAddress(getIpAddress())
                .setSwitchFeatures(SwitchFeaturesUtil
                    .getInstance()
                    .buildSwitchFeatures(new GetFeaturesOutputBuilder(connectionContext
                        .getFeatures())
                        .build()))
                .build(),
            withParents);
    }

    private IpAddress getIpAddress() {
        final InetSocketAddress remoteAddress = connectionContext
            .getConnectionAdapter()
            .getRemoteAddress();

        if (remoteAddress == null) {
            LOG.warn("IP address of the node {} cannot be obtained. No connection with switch.", getInstanceIdentifier());
            return null;
        }

        LOG.info("IP address of switch is: {}", remoteAddress);
        return IetfInetUtil.INSTANCE.ipAddressFor(remoteAddress.getAddress());
    }
}
