/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.datastore;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsAndPortNumberMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatisticsBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PortStatisticsWriter extends AbstractStatisticsWriter<NodeConnectorStatisticsAndPortNumberMap> {

    public PortStatisticsWriter(final TxFacade txFacade, final InstanceIdentifier<Node> instanceIdentifier) {
        super(txFacade, instanceIdentifier);
    }

    @Override
    protected ListenableFuture<Boolean> storeStatistics(final List<NodeConnectorStatisticsAndPortNumberMap> statistics, final boolean withParents) {
        statistics.stream()
            .flatMap(stats -> stats.getNodeConnectorStatisticsAndPortNumberMap().stream())
            .forEach(stat -> writeToTransaction(
                getNodeInstanceIdentifier()
                    .child(NodeConnector.class, new NodeConnectorKey(stat.getNodeConnectorId()))
                    .augmentation(FlowCapableNodeConnectorStatisticsData.class)
                    .child(FlowCapableNodeConnectorStatistics.class),
                new FlowCapableNodeConnectorStatisticsBuilder(stat).build(),
                withParents));

        return submitTransaction();
    }

}
