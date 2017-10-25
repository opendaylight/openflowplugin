/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.datastore.multipart;

import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsAndPortNumberMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatisticsBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PortStatsMultipartWriter extends AbstractMultipartWriter<NodeConnectorStatisticsAndPortNumberMap> {

    private final FeaturesReply features;

    public PortStatsMultipartWriter(final TxFacade txFacade,
                                    final InstanceIdentifier<Node> instanceIdentifier,
                                    final FeaturesReply features) {
        super(txFacade, instanceIdentifier);
        this.features = features;
    }

    @Override
    protected Class<NodeConnectorStatisticsAndPortNumberMap> getType() {
        return NodeConnectorStatisticsAndPortNumberMap.class;
    }

    @Override
    public void storeStatistics(final NodeConnectorStatisticsAndPortNumberMap statistics, final boolean withParents) {
        statistics.getNodeConnectorStatisticsAndPortNumberMap()
            .forEach(stat -> {
                final OpenflowVersion openflowVersion = OpenflowVersion.get(features.getVersion());
                final Long port = InventoryDataServiceUtil
                    .portNumberfromNodeConnectorId(openflowVersion, stat.getNodeConnectorId());

                final NodeConnectorId id = InventoryDataServiceUtil
                    .nodeConnectorIdfromDatapathPortNo(
                        features.getDatapathId(),
                        port,
                        OpenflowVersion.get(features.getVersion()));

                writeToTransaction(
                    getInstanceIdentifier()
                        .child(NodeConnector.class, new NodeConnectorKey(id))
                        .augmentation(FlowCapableNodeConnectorStatisticsData.class)
                        .child(FlowCapableNodeConnectorStatistics.class),
                    new FlowCapableNodeConnectorStatisticsBuilder(stat)
                        .build(),
                        OFConstants.OFP_VERSION_1_0 == features.getVersion() || withParents);
            });
    }

}
