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
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.util.FlowUtil;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowHashIdMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.nodes.node.table.FlowHashIdMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.nodes.node.table.FlowHashIdMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.GuardedBy;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.4.2015.
 */
public class DeviceFlowRegistryImpl implements DeviceFlowRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceFlowRegistryImpl.class);

    private final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier;
    private final ConcurrentMap<FlowRegistryKey, FlowDescriptor> flowRegistry = new TrieMap<>();
    @GuardedBy("marks")
    private final Collection<FlowRegistryKey> marks = new HashSet<>();

    public DeviceFlowRegistryImpl(final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier) {
        this.instanceIdentifier = instanceIdentifier;
    }

    @Override
    public FlowDescriptor retrieveIdForFlow(final FlowRegistryKey flowRegistryKey) {
        LOG.trace("Trying to retrieve flowDescriptor for key: {}", flowRegistryKey);

        // First, try to get FlowDescriptor from flow registry
        FlowDescriptor flowDescriptor = flowRegistry.get(flowRegistryKey);

        if (flowDescriptor != null) {
            return flowDescriptor;
        }

        final short tableId = flowRegistryKey.getTableId();

        // We do not found any FlowDescriptor for our FlowRegistryKey in registry,
        // so now we will try to retrieve FlowId from Datastore/CONFIG

        // First, we will create hash from FlowRegistryKey match, priority and cookie
        final FlowHashIdMapKey hashKey = FlowUtil.buildFlowHashKey(
                flowRegistryKey.getMatch(),
                flowRegistryKey.getPriority(),
                flowRegistryKey.getCookie());

        // Now we will define path to FlowId in datastore using previously created hash key
        final KeyedInstanceIdentifier<FlowHashIdMap, FlowHashIdMapKey> flowPath = instanceIdentifier
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(tableId))
                .augmentation(FlowHashIdMapping.class)
                .child(FlowHashIdMap.class, hashKey);

        final DataBroker dataBroker = OFSessionUtil.getSessionManager().getDataBroker();

        if (dataBroker != null) {
            // Create new read-only transaction
            final ReadTransaction transaction = dataBroker.newReadOnlyTransaction();

            // Now, we will try to actually read in datastore and retrieve FlowId, and then convert it to FlowDescriptor
            try {
                final Optional<FlowHashIdMap> flowHashIdMapOptional = transaction.read(LogicalDatastoreType.CONFIGURATION, flowPath).get();

                if (flowHashIdMapOptional.isPresent()) {
                    flowDescriptor = FlowDescriptorFactory.create(tableId, flowHashIdMapOptional.get().getFlowId());
                }
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Read transaction for identifier {} failed with exception: {}", instanceIdentifier, e);
            }
        }

        // We was still not able to retrieve FlowDescriptor, so we will at least try to generate it
        if (flowDescriptor == null) {
            final FlowId alienFlowId = FlowUtil.createAlienFlowId(tableId);
            flowDescriptor = FlowDescriptorFactory.create(tableId, alienFlowId);
        }

        // Finally we got flowDescriptor, so now we will store it to registry,
        // so next time we won't need to repeat this process again
        store(flowRegistryKey, flowDescriptor);

        return flowDescriptor;
    }

    @Override
    public void store(final FlowRegistryKey flowRegistryKey, final FlowDescriptor flowDescriptor) {
        LOG.trace("Storing flowDescriptor with table ID : {} and flow ID : {} for flow hash : {}", flowDescriptor.getTableKey().getId(), flowDescriptor.getFlowId().getValue(), flowRegistryKey.hashCode());
        flowRegistry.put(flowRegistryKey, flowDescriptor);
    }

    @Override
    public FlowId storeIfNecessary(final FlowRegistryKey flowRegistryKey) {
        // We will simply reuse retrieveIdForFlow to get or generate FlowDescriptor and store it if needed
        final FlowDescriptor flowDescriptor = retrieveIdForFlow(flowRegistryKey);
        return flowDescriptor.getFlowId();
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
