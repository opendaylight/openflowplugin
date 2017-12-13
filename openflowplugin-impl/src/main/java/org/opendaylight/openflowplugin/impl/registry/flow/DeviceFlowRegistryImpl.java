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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlow;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ThreadSafe
public class DeviceFlowRegistryImpl implements DeviceFlowRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceFlowRegistryImpl.class);
    private static final String ALIEN_SYSTEM_FLOW_ID = "#UF$TABLE*";
    private static final AtomicInteger UNACCOUNTED_FLOWS_COUNTER = new AtomicInteger(0);

    private final BiMap<FlowRegistryKey, FlowDescriptor> flowRegistry = Maps.synchronizedBiMap(HashBiMap.create());
    private final DataBroker dataBroker;
    private final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier;
    private final List<ListenableFuture<List<Optional<FlowCapableNode>>>> lastFillFutures = new ArrayList<>();
    private final Consumer<Flow> flowConsumer;

    public DeviceFlowRegistryImpl(final short version, final DataBroker dataBroker,
                                  final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier) {
        this.dataBroker = dataBroker;
        this.instanceIdentifier = instanceIdentifier;

        // Specifies what to do with flow read from data store
        flowConsumer = flow -> {
            final FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(version, flow);

            if (getExistingKey(flowRegistryKey) == null) {
                // Now, we will update the registry
                storeDescriptor(flowRegistryKey, FlowDescriptorFactory.create(flow.getTableId(), flow.getId()));
            }
        };
    }

    @Override
    public ListenableFuture<List<Optional<FlowCapableNode>>> fill() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Filling flow registry with flows for node: {}", instanceIdentifier.getKey().getId().getValue());
        }

        // Prepare path for read transaction
        // TODO: Read only Tables, and not entire FlowCapableNode (fix Yang model)
        final InstanceIdentifier<FlowCapableNode> path = instanceIdentifier.augmentation(FlowCapableNode.class);

        // First, try to fill registry with flows from DS/Configuration
        final CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> configFuture =
                fillFromDatastore(LogicalDatastoreType.CONFIGURATION, path);

        // Now, try to fill registry with flows from DS/Operational
        // in case of cluster fail over, when clients are not using DS/Configuration
        // for adding flows, but only RPCs
        final CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> operationalFuture =
                fillFromDatastore(LogicalDatastoreType.OPERATIONAL, path);

        // And at last, chain and return futures created above.
        // Also, cache this future, so call to DeviceFlowRegistry.close() will be able
        // to cancel this future immediately if it will be still in progress
        final ListenableFuture<List<Optional<FlowCapableNode>>> lastFillFuture = Futures
                .allAsList(Arrays.asList(configFuture, operationalFuture));
        lastFillFutures.add(lastFillFuture);
        return lastFillFuture;
    }

    private CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> fillFromDatastore(
            final LogicalDatastoreType logicalDatastoreType, final InstanceIdentifier<FlowCapableNode> path) {
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
            public void onFailure(Throwable throwable) {
                // Even when read operation failed, close the transaction
                transaction.close();
            }
        });

        return future;
    }

    @Override
    public FlowDescriptor retrieveDescriptor(@Nonnull final FlowRegistryKey flowRegistryKey) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Retrieving flow descriptor for flow registry : {}", flowRegistryKey.toString());
        }

        FlowRegistryKey existingFlowRegistryKey = getExistingKey(flowRegistryKey);
        if (existingFlowRegistryKey != null) {
            return flowRegistry.get(existingFlowRegistryKey);
        }
        return null;
    }

    @Override
    public void storeDescriptor(@Nonnull final FlowRegistryKey flowRegistryKey,
                                @Nonnull final FlowDescriptor flowDescriptor) {
        try {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Storing flowDescriptor with table ID : {} and flow ID : {} for flow hash : {}",
                        flowDescriptor.getTableKey().getId(),
                        flowDescriptor.getFlowId().getValue(),
                        flowRegistryKey.toString());
            }

            addToFlowRegistry(flowRegistryKey, flowDescriptor);
        } catch (IllegalArgumentException ex) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Flow with flow ID {} already exists in table {}, "
                                + "generating alien flow ID", flowDescriptor.getFlowId().getValue(),
                        flowDescriptor.getTableKey().getId());
            }

            // We are trying to store new flow to flow registry, but we already have different flow with same flow ID
            // stored in registry, so we need to create alien ID for this new flow here.
            addToFlowRegistry(
                    flowRegistryKey,
                    FlowDescriptorFactory.create(
                            flowDescriptor.getTableKey().getId(),
                            createAlienFlowId(flowDescriptor.getTableKey().getId())));
        }
    }

    @Override
    public void store(final FlowRegistryKey flowRegistryKey) {
        if (Objects.isNull(retrieveDescriptor(flowRegistryKey))) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Flow descriptor for flow hash : {} not found, generating alien flow ID",
                        flowRegistryKey.toString());
            }

            // We do not found flow in flow registry, that means it do not have any ID already assigned, so we need
            // to generate new alien flow ID here.
            storeDescriptor(
                    flowRegistryKey,
                    FlowDescriptorFactory.create(
                            flowRegistryKey.getTableId(),
                            createAlienFlowId(flowRegistryKey.getTableId())));
        }
    }

    @Override
    public void addMark(final FlowRegistryKey flowRegistryKey) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Removing flow descriptor for flow hash : {}", flowRegistryKey.toString());
        }

        removeFromFlowRegistry(flowRegistryKey);
    }

    @Override
    public void processMarks() {
        // Do nothing
    }

    @Override
    public void forEach(final Consumer<FlowRegistryKey> consumer) {
        synchronized (flowRegistry) {
            flowRegistry.keySet().forEach(consumer);
        }
    }

    @Override
    public int size() {
        return flowRegistry.size();
    }

    @Override
    public void close() {
        final Iterator<ListenableFuture<List<Optional<FlowCapableNode>>>> iterator = lastFillFutures.iterator();

        // We need to force interrupt and clear all running futures that are trying to read flow IDs from data store
        while (iterator.hasNext()) {
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
        LOG.debug("Created alien flow id {} for table id {}", alienId, tableId);
        return new FlowId(alienId);
    }

    //Hashcode generation of the extension augmentation can differ for the same object received from the datastore and
    // the one received after deserialization of switch message. OpenFlowplugin extensions are list, and the order in
    // which it can receive the extensions back from switch can differ and that lead to a different hashcode. In that
    // scenario, hashcode won't match and flowRegistry return the  related key. To overcome this issue, these methods
    // make sure that key is stored only if it doesn't equals to any existing key.
    private void addToFlowRegistry(final FlowRegistryKey flowRegistryKey, final FlowDescriptor flowDescriptor) {
        FlowRegistryKey existingFlowRegistryKey = getExistingKey(flowRegistryKey);
        if (existingFlowRegistryKey == null) {
            flowRegistry.put(flowRegistryKey, flowDescriptor);
        } else {
            flowRegistry.put(existingFlowRegistryKey, flowDescriptor);
        }
    }

    private void removeFromFlowRegistry(final FlowRegistryKey flowRegistryKey) {
        FlowRegistryKey existingFlowRegistryKey = getExistingKey(flowRegistryKey);
        if (existingFlowRegistryKey != null) {
            flowRegistry.remove(existingFlowRegistryKey);
        } else {
            flowRegistry.remove(flowRegistryKey);
        }
    }

    private FlowRegistryKey getExistingKey(final FlowRegistryKey flowRegistryKey) {
        synchronized (flowRegistry) {
            if (flowRegistryKey.getMatch().getAugmentation(GeneralAugMatchNodesNodeTableFlow.class) == null) {
                if (flowRegistry.containsKey(flowRegistryKey)) {
                    return flowRegistryKey;
                }
            } else {
                Iterator flowRegistryKeyIterator = flowRegistry.keySet().iterator();
                while (flowRegistryKeyIterator.hasNext()) {
                    FlowRegistryKey flowRegistryKeyValue = (FlowRegistryKey) flowRegistryKeyIterator.next();
                    if (flowRegistryKeyValue.equals(flowRegistryKey)) {
                        return flowRegistryKeyValue;
                    }
                }
            }
            return null;
        }
    }

    @VisibleForTesting
    Map<FlowRegistryKey, FlowDescriptor> getAllFlowDescriptors() {
        return flowRegistry;
    }
}
