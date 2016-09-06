/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.registry.flow;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceFlowRegistryImpl implements DeviceFlowRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceFlowRegistryImpl.class);
    private static final String ALIEN_SYSTEM_FLOW_ID = "#UF$TABLE*";
    private static final AtomicInteger UNACCOUNTED_FLOWS_COUNTER = new AtomicInteger(0);

    private final BiMap<FlowRegistryKey, FlowDescriptor> flowRegistry = Maps.synchronizedBiMap(HashBiMap.create());
    private final DataBroker dataBroker;
    private final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier;
    private final List<ListenableFuture<List<Optional<FlowCapableNode>>>> lastFillFutures = new ArrayList<>();

    // Specifies what to do with flow read from datastore
    private final Consumer<Flow> flowConsumer = flow -> {
        // Create flow registry key from flow
        final FlowRegistryKey key = FlowRegistryKeyFactory.create(flow);

        // Now, we will update the registry, but we will also try to prevent duplicate entries
        if (!flowRegistry.containsKey(key)) {
            LOG.trace("Found flow with table ID : {} and flow ID : {}", flow.getTableId(), flow.getId().getValue());
            final FlowDescriptor descriptor = FlowDescriptorFactory.create(flow.getTableId(), flow.getId());
            store(key, descriptor);
        }
    };

    public DeviceFlowRegistryImpl(final DataBroker dataBroker, final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier) {
        this.dataBroker = dataBroker;
        this.instanceIdentifier = instanceIdentifier;
    }

    @Override
    public ListenableFuture<List<Optional<FlowCapableNode>>> fill() {
        LOG.debug("Filling flow registry with flows for node: {}", instanceIdentifier.getKey().getId().getValue());

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
        final ListenableFuture<List<Optional<FlowCapableNode>>> lastFillFuture = Futures.allAsList(Arrays.asList(configFuture, operationalFuture));
        lastFillFutures.add(lastFillFuture);
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
                result.asSet().stream()
                        .filter(Objects::nonNull)
                        .filter(flowCapableNode -> Objects.nonNull(flowCapableNode.getTable()))
                        .flatMap(flowCapableNode -> flowCapableNode.getTable().stream())
                        .filter(Objects::nonNull)
                        .filter(table -> Objects.nonNull(table.getFlow()))
                        .flatMap(table -> table.getFlow().stream())
                        .filter(Objects::nonNull)
                        .filter(flow -> Objects.nonNull(flow.getId()))
                        .forEach(flowConsumer);

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
        LOG.trace("Retrieving flow descriptor for flow hash : {}", flowRegistryKey.hashCode());
        FlowDescriptor flowDescriptor = flowRegistry.get(flowRegistryKey);
        // Get FlowDescriptor from flow registry
        if(flowDescriptor == null){
            if (LOG.isTraceEnabled()) {
                LOG.trace("Failed to retrieve flow descriptor for flow hash : {}, trying with custom equals method", flowRegistryKey.hashCode());
            }
            for(Map.Entry<FlowRegistryKey, FlowDescriptor> fd : flowRegistry.entrySet()) {
                if (fd.getKey().equals(flowRegistryKey)) {
                    flowDescriptor = fd.getValue();
                    break;
                }
            }
        }
        return flowDescriptor;
    }

    @Override
    public void store(final FlowRegistryKey flowRegistryKey, final FlowDescriptor flowDescriptor) {
        try {
          LOG.trace("Storing flowDescriptor with table ID : {} and flow ID : {} for flow hash : {}",
                    flowDescriptor.getTableKey().getId(), flowDescriptor.getFlowId().getValue(), flowRegistryKey.hashCode());
          flowRegistry.put(flowRegistryKey, flowDescriptor);
        } catch (IllegalArgumentException ex) {
          LOG.error("Flow with flowId {} already exists in table {}", flowDescriptor.getFlowId().getValue(),
                    flowDescriptor.getTableKey().getId());
          final FlowId newFlowId = createAlienFlowId(flowDescriptor.getTableKey().getId());
          final FlowDescriptor newFlowDescriptor = FlowDescriptorFactory.
            create(flowDescriptor.getTableKey().getId(), newFlowId);
          flowRegistry.put(flowRegistryKey, newFlowDescriptor);
        }
    }

    @Override
    public void update(final FlowRegistryKey newFlowRegistryKey, final FlowDescriptor flowDescriptor) {
        LOG.trace("Updating the entry with hash: {}", newFlowRegistryKey.hashCode());
        flowRegistry.forcePut(newFlowRegistryKey, flowDescriptor);
    }

    @Override
    public FlowId storeIfNecessary(final FlowRegistryKey flowRegistryKey) {
        LOG.trace("Trying to retrieve flow ID for flow hash : {}", flowRegistryKey.hashCode());

        // First, try to get FlowDescriptor from flow registry
        FlowDescriptor flowDescriptor = retrieveIdForFlow(flowRegistryKey);

        // We was not able to retrieve FlowDescriptor, so we will at least try to generate it
        if (flowDescriptor == null) {
            LOG.trace("Flow descriptor for flow hash : {} not found, generating alien flow ID", flowRegistryKey.hashCode());
            final short tableId = flowRegistryKey.getTableId();
            final FlowId alienFlowId = createAlienFlowId(tableId);
            flowDescriptor = FlowDescriptorFactory.create(tableId, alienFlowId);

            // Finally we got flowDescriptor, so now we will store it to registry,
            // so next time we won't need to generate it again
            store(flowRegistryKey, flowDescriptor);
        }

        return flowDescriptor.getFlowId();
    }

    @Override
    public void removeDescriptor(final FlowRegistryKey flowRegistryKey) {
        LOG.trace("Removing flow descriptor for flow hash : {}", flowRegistryKey.hashCode());
        flowRegistry.remove(flowRegistryKey);
    }

    @Override
    public Map<FlowRegistryKey, FlowDescriptor> getAllFlowDescriptors() {
        return Collections.unmodifiableMap(flowRegistry);
    }

    @Override
    public void close() {
        final Iterator<ListenableFuture<List<Optional<FlowCapableNode>>>> iterator = lastFillFutures.iterator();

        while(iterator.hasNext()) {
            final ListenableFuture<List<Optional<FlowCapableNode>>> next = iterator.next();
            boolean success = next.cancel(true);
            LOG.trace("Cancelling filling flow registry with flows job {} with result: {}", next, success);
            iterator.remove();
        }

        flowRegistry.clear();
    }

    @VisibleForTesting
    static FlowId createAlienFlowId(final short tableId) {
        final String alienId = ALIEN_SYSTEM_FLOW_ID + tableId + '-' + UNACCOUNTED_FLOWS_COUNTER.incrementAndGet();
        return new FlowId(alienId);
    }
}