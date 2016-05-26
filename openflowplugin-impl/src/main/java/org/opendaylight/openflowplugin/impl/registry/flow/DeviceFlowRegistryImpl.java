/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.registry.flow;

import com.google.common.base.Optional;
import com.romix.scala.collection.concurrent.TrieMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import javax.annotation.concurrent.GuardedBy;

import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.util.FlowUtil;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowHashIdMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.nodes.node.table.FlowHashIdMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.nodes.node.table.FlowHashIdMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.4.2015.
 */
public class DeviceFlowRegistryImpl implements DeviceFlowRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceFlowRegistryImpl.class);

    private final ConcurrentMap<FlowRegistryKey, FlowDescriptor> flowRegistry = new TrieMap<>();
    @GuardedBy("marks")
    private final Collection<FlowRegistryKey> marks = new HashSet<>();

    @Override
    public FlowDescriptor retrieveIdForFlow(final FlowRegistryKey flowRegistryKey, final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier) {
        if (flowRegistry.containsKey(flowRegistryKey)) {
            return flowRegistry.get(flowRegistryKey);
        }

        final short tableId = flowRegistryKey.getTableId();
        final String hash = String.valueOf(flowRegistryKey.getMatch()) +
                flowRegistryKey.getPriority() +
                flowRegistryKey.getCookie();

        final KeyedInstanceIdentifier<FlowHashIdMap, FlowHashIdMapKey> flowPath = instanceIdentifier
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(tableId))
                .augmentation(FlowHashIdMapping.class)
                .child(FlowHashIdMap.class, new FlowHashIdMapKey(hash));

        final ReadTransaction transaction = OFSessionUtil.getSessionManager().getDataBroker().newReadOnlyTransaction();
        FlowDescriptor flowDescriptor = null;

        try {
            Optional<FlowHashIdMap> flowHashIdMapOptional = transaction.read(LogicalDatastoreType.OPERATIONAL, flowPath).get();

            if (flowHashIdMapOptional.isPresent()) {
                flowDescriptor = FlowDescriptorFactory.create(tableId, flowHashIdMapOptional.get().getFlowId());
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Read transaction for identifier {} failed with exception: {}", instanceIdentifier, e);
        }

        if (flowDescriptor == null) {
            flowDescriptor = FlowDescriptorFactory.create(tableId, storeIfNecessary(flowRegistryKey, tableId));
        }

        store(flowRegistryKey, flowDescriptor);
        return flowDescriptor;
    }

    @Override
    public void store(final FlowRegistryKey flowRegistryKey, final FlowDescriptor flowDescriptor) {
        LOG.trace("Storing flowDescriptor with table ID : {} and flow ID : {} for flow hash : {}", flowDescriptor.getTableKey().getId(), flowDescriptor.getFlowId().getValue(), flowRegistryKey.hashCode());
        flowRegistry.put(flowRegistryKey, flowDescriptor);
    }

    @Override
    public FlowId storeIfNecessary(final FlowRegistryKey flowRegistryKey, @Deprecated final short tableId) {
        //TODO: remove tableId parameter - it is contained in the first one
        final FlowId alienFlowId = FlowUtil.createAlienFlowId(tableId);
        final FlowDescriptor alienFlowDescriptor = FlowDescriptorFactory.create(tableId, alienFlowId);

        final FlowDescriptor previous = flowRegistry.putIfAbsent(flowRegistryKey, alienFlowDescriptor);
        if (previous == null) {
            LOG.trace("Flow descriptor for flow hash {} wasn't found.", flowRegistryKey.hashCode());
            return alienFlowId;
        } else {
            return previous.getFlowId();
        }
    }

    @Override
    public void markToBeremoved(final FlowRegistryKey flowRegistryKey) {
        synchronized (marks) {
            marks.add(flowRegistryKey);
        }
        LOG.trace("Flow hash {} was marked for removal.", flowRegistryKey.hashCode());
    }

    @Override
    public void removeMarked() {
        synchronized (marks) {
            for (FlowRegistryKey flowRegistryKey : marks) {
                LOG.trace("Removing flowDescriptor for flow hash : {}", flowRegistryKey.hashCode());
                flowRegistry.remove(flowRegistryKey);
            }

            marks.clear();
        }
    }

    @Override
    public Map<FlowRegistryKey, FlowDescriptor> getAllFlowDescriptors() {
        return Collections.unmodifiableMap(flowRegistry);
    }

    @Override
    public void close() {
        flowRegistry.clear();
        marks.clear();
    }
}
