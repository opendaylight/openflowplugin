/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.SnapshotElicitRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.OpendaylightDirectStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adding retry mechanism in case of unsuccessful syncup.
 */
public class SyncReactorRetryDecorator implements SyncReactor{

    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorRetryDecorator.class);

    private final SyncReactor delegate;
    private final OpendaylightDirectStatisticsService directStatisticsService;
    private SnapshotElicitRegistry snapshotElicitRegistry;
    private boolean retry;


    public SyncReactorRetryDecorator (final SyncReactor delegate, SnapshotElicitRegistry snapshotElicitRegistry,
                                      final OpendaylightDirectStatisticsService directStatisticsService) {
        this.delegate = delegate;
        this.retry = false;
        this.snapshotElicitRegistry = snapshotElicitRegistry;
        this.directStatisticsService = directStatisticsService;
    }

    public ListenableFuture<Boolean> syncup (final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                             final FlowCapableNode configTree,
                                             final FlowCapableNode operationalTree) throws InterruptedException {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        LOG.trace("syncup {}", nodeId.getValue());

        ListenableFuture<Boolean> syncupResult = delegate.syncup(flowcapableNodePath, configTree, operationalTree);

        return Futures.transform(syncupResult, new Function<Boolean, Boolean>() {
            @Override
            public Boolean apply(Boolean result) {
                if (result)
                    return true;
                else {
                    // TODO - allow compression among config states, prevent config from being propagated to reactor
                    // register for operational
                    snapshotElicitRegistry.registerForNextConsistentOperationalSnapshot(nodeId);
                    // elicit statistics
                    triggerStatisticsGathering(nodeId);
                    return false;
                }
            }
        });
    }

    public void triggerStatisticsGathering(NodeId nodeId) {
        NodeRef nodeRef = new NodeRef(InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId)));
        directStatisticsService.getFlowStatistics(new GetFlowStatisticsInputBuilder().setNode(nodeRef).build());
        directStatisticsService.getGroupStatistics(new GetGroupStatisticsInputBuilder().setNode(nodeRef).build());
        directStatisticsService.getMeterStatistics(new GetMeterStatisticsInputBuilder().setNode(nodeRef).build());
        directStatisticsService.getNodeConnectorStatistics(new GetNodeConnectorStatisticsInputBuilder().setNode(nodeRef).build());
        directStatisticsService.getQueueStatistics(new GetQueueStatisticsInputBuilder().setNode(nodeRef).build());
    }

    public boolean inRetry() {
        return this.retry;
    }

}
