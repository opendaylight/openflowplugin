/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.registry.flow;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.romix.scala.collection.concurrent.TrieMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.util.FlowUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
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
    private final DataBroker dataBroker;
    private ListenableFuture<List<Optional<FlowCapableNode>>> lastFillFuture;

    public DeviceFlowRegistryImpl(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public ListenableFuture<List<Optional<FlowCapableNode>>> fill(final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier) {
        LOG.debug("Filling flow registry with flows for node: {}", instanceIdentifier);

        // Prepare path for read transaction
        // TODO: Read only Tables, and not entire FlowCapableNode (fix Yang model)
        final InstanceIdentifier<FlowCapableNode> path = instanceIdentifier.augmentation(FlowCapableNode.class);

        // First, try to fill registry with flows from DS/Configuration
        CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> configFuture = fillFromDatastore(LogicalDatastoreType.CONFIGURATION, path);

        // Now, try to fill registry with flows from DS/Operational
        // in case of cluster fail over, when clients are not using DS/Configuration
        // for adding flows, but only RPCs
        CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> operationalFuture = fillFromDatastore(LogicalDatastoreType.OPERATIONAL, path);

        // And at last, chain and return futures created above.
        // Also, cache this future, so call to DeviceFlowRegistry.close() will be able
        // to cancel this future immediately if it will be still in progress
        lastFillFuture = Futures.allAsList(Arrays.asList(configFuture, operationalFuture));
        return lastFillFuture;
    }

    private CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> fillFromDatastore(final LogicalDatastoreType logicalDatastoreType, final InstanceIdentifier<FlowCapableNode> path) {
        // Create new read-only transaction
        final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();

        // Bail out early if transaction is null
        if (transaction == null) {
            return Futures.immediateFailedCheckedFuture(
                    new ReadFailedException("Read transaction is null"));
        }

        // Prepare read operation from datastore for path
        final CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> future =
                transaction.read(logicalDatastoreType, path);

        // Bail out early if future is null
        if (future == null) {
            return Futures.immediateFailedCheckedFuture(
                    new ReadFailedException("Future from read transaction is null"));
        }

        Futures.addCallback(future, new FutureCallback<Optional<FlowCapableNode>>() {
            @Override
            public void onSuccess(Optional<FlowCapableNode> result) {
                if (result.isPresent()) {
                    final List<Table> tables = result.get().getTable();

                    if (tables != null) {
                        for (Table table : tables) {
                            final List<Flow> flows = table.getFlow();

                            if (flows != null) {
                                // If we finally got some flows, store each of them in registry if needed
                                for (Flow flow : table.getFlow()) {
                                    final FlowRegistryKey key = FlowRegistryKeyFactory.create(flow);

                                    // Now, we will update the registry, but we will also try to prevent duplicate entries
                                    if (!flowRegistry.containsKey(key)) {
                                        LOG.trace("Found flow with table ID : {} and flow ID : {}",
                                                flow.getTableId(),
                                                flow.getId().getValue());

                                        final FlowDescriptor descriptor = FlowDescriptorFactory.create(
                                                flow.getTableId(),
                                                flow.getId());

                                        store(key, descriptor);
                                    }
                                }
                            }
                        }
                    }
                }

                // After we are done with reading from datastore, close the transaction
                transaction.close();
            }

            @Override
            public void onFailure(Throwable t) {
                // Even when read operation failed, close the transaction
                transaction.close();
            }
        });

        return future;
    }

    @Override
    public FlowDescriptor retrieveIdForFlow(final FlowRegistryKey flowRegistryKey) {
        LOG.trace("Retrieving flowDescriptor for flow hash: {}", flowRegistryKey.hashCode());

        // Get FlowDescriptor from flow registry
        return flowRegistry.get(flowRegistryKey);
    }

    @Override
    public void store(final FlowRegistryKey flowRegistryKey, final FlowDescriptor flowDescriptor) {
        LOG.trace("Storing flowDescriptor with table ID : {} and flow ID : {} for flow hash : {}", flowDescriptor.getTableKey().getId(), flowDescriptor.getFlowId().getValue(), flowRegistryKey.hashCode());
        flowRegistry.put(flowRegistryKey, flowDescriptor);
    }

    @Override
    public FlowId storeIfNecessary(final FlowRegistryKey flowRegistryKey) {
        LOG.trace("Trying to retrieve flowDescriptor for flow hash: {}", flowRegistryKey.hashCode());

        // First, try to get FlowDescriptor from flow registry
        FlowDescriptor flowDescriptor = flowRegistry.get(flowRegistryKey);

        // We was not able to retrieve FlowDescriptor, so we will at least try to generate it
        if (flowDescriptor == null) {
            final short tableId = flowRegistryKey.getTableId();
            final FlowId alienFlowId = FlowUtil.createAlienFlowId(tableId);
            flowDescriptor = FlowDescriptorFactory.create(tableId, alienFlowId);

            // Finally we got flowDescriptor, so now we will store it to registry,
            // so next time we won't need to generate it again
            store(flowRegistryKey, flowDescriptor);
        }

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
        if (lastFillFuture != null) {
            lastFillFuture.cancel(true);
        }

        flowRegistry.clear();
        marks.clear();
    }
}
