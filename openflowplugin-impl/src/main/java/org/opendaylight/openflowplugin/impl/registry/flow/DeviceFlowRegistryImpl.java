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
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
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

@ThreadSafe
public class DeviceFlowRegistryImpl implements DeviceFlowRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceFlowRegistryImpl.class);
    private static final String ALIEN_SYSTEM_FLOW_ID = "#UF$TABLE*";
    private static final AtomicInteger UNACCOUNTED_FLOWS_COUNTER = new AtomicInteger(0);

    private final BiMap<FlowRegistryKey, FlowDescriptor> flowRegistry = HashBiMap.create();
    private final List<FlowRegistryKey> marks = new ArrayList<>();
    private final DataBroker dataBroker;
    private final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier;
    private final List<ListenableFuture<List<Optional<FlowCapableNode>>>> lastFillFutures = new ArrayList<>();
    private final Consumer<Flow> flowConsumer;

    public DeviceFlowRegistryImpl(final short version, final DataBroker dataBroker, final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier) {
        this.dataBroker = dataBroker;
        this.instanceIdentifier = instanceIdentifier;

        // Specifies what to do with flow read from datastore
        flowConsumer = flow -> {
            // Create flow registry key from flow
            final FlowRegistryKey key = FlowRegistryKeyFactory.create(version, flow);

            // Now, we will update the registry, but we will also try to prevent duplicate entries
            if (!flowRegistry.containsKey(key)) {
                LOG.trace("Found flow with table ID : {} and flow ID : {}", flow.getTableId(), flow.getId().getValue());
                final FlowDescriptor descriptor = FlowDescriptorFactory.create(flow.getTableId(), flow.getId());
                storeDescriptor(key, descriptor);
            }
        };
    }

    @Override
    @GuardedBy("this")
    public synchronized ListenableFuture<List<Optional<FlowCapableNode>>> fill() {
        LOG.debug("Filling flow registry with flows for node: {}", instanceIdentifier.getKey().getId().getValue());

        // Prepare path for read transaction
        // TODO: Read only Tables, and not entire FlowCapableNode (fix Yang model)
        final InstanceIdentifier<FlowCapableNode> path = instanceIdentifier.augmentation(FlowCapableNode.class);

        // First, try to fill registry with flows from DS/Configuration
        final CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> configFuture = fillFromDatastore(LogicalDatastoreType.CONFIGURATION, path);

        // Now, try to fill registry with flows from DS/Operational
        // in case of cluster fail over, when clients are not using DS/Configuration
        // for adding flows, but only RPCs
        final CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> operationalFuture = fillFromDatastore(LogicalDatastoreType.OPERATIONAL, path);

        // And at last, chain and return futures created above.
        // Also, cache this future, so call to DeviceFlowRegistry.close() will be able
        // to cancel this future immediately if it will be still in progress
        final ListenableFuture<List<Optional<FlowCapableNode>>> lastFillFuture = Futures.allAsList(Arrays.asList(configFuture, operationalFuture));
        lastFillFutures.add(lastFillFuture);
        return lastFillFuture;
    }

    @GuardedBy("this")
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
    @GuardedBy("this")
    public synchronized FlowDescriptor retrieveDescriptor(final FlowRegistryKey flowRegistryKey) {
        LOG.trace("Retrieving flow descriptor for flow hash : {}", flowRegistryKey.hashCode());
        return flowRegistry.get(correctFlowRegistryKey(flowRegistry.keySet(), flowRegistryKey));
    }

    @Override
    @GuardedBy("this")
    public synchronized void storeDescriptor(final FlowRegistryKey flowRegistryKey, final FlowDescriptor flowDescriptor) {
        final FlowRegistryKey correctFlowRegistryKey = correctFlowRegistryKey(flowRegistry.keySet(), flowRegistryKey);

        try {
            if (hasMark(correctFlowRegistryKey)) {
                // We are probably doing update of flow ID or table ID, so remove mark for removal of this flow
                // and replace it with new value
                marks.remove(correctFlowRegistryKey(marks, correctFlowRegistryKey));
                flowRegistry.forcePut(correctFlowRegistryKey, flowDescriptor);
                return;
            }

            LOG.trace("Storing flowDescriptor with table ID : {} and flow ID : {} for flow hash : {}",
                flowDescriptor.getTableKey().getId(), flowDescriptor.getFlowId().getValue(), correctFlowRegistryKey.hashCode());

            flowRegistry.put(correctFlowRegistryKey, flowDescriptor);
        } catch (IllegalArgumentException ex) {
            if (hasMark(flowRegistry.inverse().get(flowDescriptor))) {
                // We are probably doing update of flow, but without changing flow ID or table ID, so we need to replace
                // old value with new value, but keep the old value marked for removal
                flowRegistry.forcePut(correctFlowRegistryKey, flowDescriptor);
                return;
            }

            // We are trying to store new flow to flow registry, but we already have different flow with same flow ID
            // stored in registry, so we need to create alien ID for this new flow here.
            LOG.warn("Flow with flow ID {} already exists in table {}, generating alien flow ID", flowDescriptor.getFlowId().getValue(),
                flowDescriptor.getTableKey().getId());

            flowRegistry.put(
                correctFlowRegistryKey,
                FlowDescriptorFactory.create(
                    flowDescriptor.getTableKey().getId(),
                    createAlienFlowId(flowDescriptor.getTableKey().getId())));
        }
    }

    @Override
    @GuardedBy("this")
    public synchronized void forEachEntry(final BiConsumer<FlowRegistryKey, FlowDescriptor> consumer) {
        flowRegistry.forEach(consumer);
    }

    @Override
    @GuardedBy("this")
    public synchronized void store(final FlowRegistryKey flowRegistryKey) {
        if (Objects.isNull(retrieveDescriptor(flowRegistryKey))) {
            // We do not found flow in flow registry, that means it do not have any ID already assigned, so we need
            // to generate new alien flow ID here.
            LOG.debug("Flow descriptor for flow hash : {} not found, generating alien flow ID", flowRegistryKey.hashCode());
            final short tableId = flowRegistryKey.getTableId();
            final FlowId alienFlowId = createAlienFlowId(tableId);
            final FlowDescriptor flowDescriptor = FlowDescriptorFactory.create(tableId, alienFlowId);

            // Finally we got flowDescriptor, so now we will store it to registry,
            // so next time we won't need to generate it again
            storeDescriptor(flowRegistryKey, flowDescriptor);
        }
    }

    @Override
    @GuardedBy("this")
    public synchronized void addMark(final FlowRegistryKey flowRegistryKey) {
        LOG.trace("Removing flow descriptor for flow hash : {}", flowRegistryKey.hashCode());
        marks.add(flowRegistryKey);
    }

    @Override
    @GuardedBy("this")
    public synchronized boolean hasMark(final FlowRegistryKey flowRegistryKey) {
        return Objects.nonNull(flowRegistryKey) && marks.contains(correctFlowRegistryKey(marks, flowRegistryKey));

    }

    @Override
    @GuardedBy("this")
    public synchronized void processMarks() {
        // Remove all flows that was marked for removal from flow registry and clear all marks.
        marks.forEach(flowRegistry::remove);
        marks.clear();
    }

    @Override
    @GuardedBy("this")
    public synchronized void forEach(final Consumer<FlowRegistryKey> consumer) {
        flowRegistry.keySet().forEach(consumer);
    }

    @Override
    @GuardedBy("this")
    public synchronized int size() {
        return flowRegistry.size();
    }

    @Override
    @GuardedBy("this")
    public synchronized void close() {
        final Iterator<ListenableFuture<List<Optional<FlowCapableNode>>>> iterator = lastFillFutures.iterator();

        // We need to force interrupt and clear all running futures that are trying to read flow IDs from datastore
        while (iterator.hasNext()) {
            final ListenableFuture<List<Optional<FlowCapableNode>>> next = iterator.next();
            boolean success = next.cancel(true);
            LOG.trace("Cancelling filling flow registry with flows job {} with result: {}", next, success);
            iterator.remove();
        }

        flowRegistry.clear();
        marks.clear();
    }

    @GuardedBy("this")
    private FlowRegistryKey correctFlowRegistryKey(final Collection<FlowRegistryKey> flowRegistryKeys, final FlowRegistryKey key) {
        if (Objects.isNull(key)) {
            return null;
        }

        if (!flowRegistryKeys.contains(key)) {
            // If we failed to compare FlowRegistryKey by hashCode, try to retrieve correct FlowRegistryKey
            // from set of keys using custom comparator method for Match. This case can occur when we have different
            // augmentations on extensions, or switch returned things like IP address or port in different format that
            // we sent.
            if (LOG.isTraceEnabled()) {
                LOG.trace("Failed to retrieve flow descriptor for flow hash : {}, trying with custom equals method", key.hashCode());
            }

            for (final FlowRegistryKey flowRegistryKey : flowRegistryKeys) {
                if (key.equals(flowRegistryKey)) {
                    return flowRegistryKey;
                }
            }
        }

        // If we failed to find key at all or key is already present in set of keys, just return original key
        return key;
    }

    @VisibleForTesting
    static FlowId createAlienFlowId(final short tableId) {
        final String alienId = ALIEN_SYSTEM_FLOW_ID + tableId + '-' + UNACCOUNTED_FLOWS_COUNTER.incrementAndGet();
        LOG.debug("Created alien flow id {} for table id {}", alienId, tableId);
        return new FlowId(alienId);
    }

    @VisibleForTesting
    Map<FlowRegistryKey, FlowDescriptor> getAllFlowDescriptors() {
        return flowRegistry;
    }
}
