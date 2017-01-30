/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.datastore.multipart;

import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.multipart.reply.multipart.reply.body.MultipartReplyPortDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsDataBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PortDescMultipartWriter extends AbstractMultipartWriter<MultipartReplyPortDesc> {

    private final FeaturesReply features;

    public PortDescMultipartWriter(final TxFacade txFacade,
                                   final InstanceIdentifier<Node> instanceIdentifier,
                                   final FeaturesReply features) {
        super(txFacade, instanceIdentifier);
        this.features = features;
    }

    @Override
    protected Class<MultipartReplyPortDesc> getType() {
        return MultipartReplyPortDesc.class;
    }

    @Override
    public void storeStatistics(final MultipartReplyPortDesc statistics, final boolean withParents) {
        statistics.getPorts()
            .forEach(stat -> {
                final NodeConnectorId id = InventoryDataServiceUtil
                    .nodeConnectorIdfromDatapathPortNo(
                        features.getDatapathId(),
                        OpenflowPortsUtil.getProtocolPortNumber(
                            OpenflowVersion.get(features.getVersion()),
                            stat.getPortNumber()),
                        OpenflowVersion.get(features.getVersion()));

                writeToTransaction(
                    getInstanceIdentifier()
                        .child(NodeConnector.class, new NodeConnectorKey(id)),
                    new NodeConnectorBuilder()
                        .setId(id)
                        .addAugmentation(
                            FlowCapableNodeConnector.class,
                            new FlowCapableNodeConnectorBuilder(stat).build())
                        .addAugmentation(
                            FlowCapableNodeConnectorStatisticsData.class,
                            new FlowCapableNodeConnectorStatisticsDataBuilder().build())
                        .build(),
                    withParents);
            });
    }

}
