/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.inventory.manager;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.TableUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Class receives and processes table feature updates. It augment table feature on table node
 * in the inventory tree (node/table/{table-id}).
 * Created by vishnoianil on 1/21/16.
 */
public class NodeTablesFeatureCommitter implements SalTableListener {

    private static final Logger LOG = LoggerFactory.getLogger(NodeChangeCommiter.class);

    private final FlowCapableInventoryProvider manager;

    public NodeTablesFeatureCommitter(final FlowCapableInventoryProvider manager) {
        this.manager = Preconditions.checkNotNull(manager);
    }

    @Override
    public void onTableUpdated(final TableUpdated notification) {
        final NodeId nodeId = notification.getNode().getValue().firstKeyOf(Node.class).getId();
        LOG.info("Table feature notification received from {}", nodeId.getValue());
        manager.enqueue(new InventoryOperation() {
            @Override
            public void applyOperation(final ReadWriteTransaction tx) {
                List<TableFeatures> swTablesFeatures = notification.getTableFeatures();
                final InstanceIdentifier<FlowCapableNode> flowCapableNodeII = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, new NodeKey(nodeId)).augmentation(FlowCapableNode.class);

                LOG.debug("Table feature update message contains feature data for {} tables from node {}",
                        swTablesFeatures != null?swTablesFeatures.size():0, nodeId.getValue());

                for (final TableFeatures tableFeatureData : swTablesFeatures) {
                    final Short tableId = tableFeatureData.getTableId();
                    final KeyedInstanceIdentifier<TableFeatures, TableFeaturesKey> tableFeaturesII = flowCapableNodeII
                            .child(Table.class, new TableKey(tableId))
                            .child(TableFeatures.class,new TableFeaturesKey(tableId));

                    LOG.trace("Updating table feature for table {} of node {}", tableId, nodeId.getValue());
                    tx.put(LogicalDatastoreType.OPERATIONAL, tableFeaturesII, tableFeatureData, true);
                }
            }
        });
    }
}
