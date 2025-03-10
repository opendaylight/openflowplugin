/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.registry.flow;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupInfo;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupStatus;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.device.history.FlowGroupInfoHistoryAppender;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlow;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceFlowRegistryImpl implements DeviceFlowRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceFlowRegistryImpl.class);
    private static final String ALIEN_SYSTEM_FLOW_ID = "#UF$TABLE*";
    private static final AtomicInteger UNACCOUNTED_FLOWS_COUNTER = new AtomicInteger(0);

    // FIXME: improve locking here
    private final List<ListenableFuture<List<Optional<FlowCapableNode>>>> lastFillFutures = new ArrayList<>();
    private final BiMap<FlowRegistryKey, FlowDescriptor> flowRegistry = Maps.synchronizedBiMap(HashBiMap.create());
    private final DataObjectIdentifier.WithKey<Node, NodeKey> instanceIdentifier;
    private final FlowGroupInfoHistoryAppender history;
    private final FlowRegistryKeyFactory keyFactory;
    private final DataBroker dataBroker;

    public DeviceFlowRegistryImpl(final Uint8 version, final DataBroker dataBroker,
            final DataObjectIdentifier.WithKey<Node, NodeKey> instanceIdentifier,
            final FlowGroupInfoHistoryAppender history) {
        this.dataBroker = requireNonNull(dataBroker);
        this.instanceIdentifier = requireNonNull(instanceIdentifier);
        this.history = requireNonNull(history);
        keyFactory = FlowRegistryKeyFactory.ofVersion(version);
    }

    @Override
    public FlowRegistryKey createKey(
        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow flow) {
        return keyFactory.create(flow);
    }

    @Override
    public ListenableFuture<List<Optional<FlowCapableNode>>> fill() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Filling flow registry with flows for node: {}", instanceIdentifier.key().getId().getValue());
        }

        // Prepare path for read transaction
        // TODO: Read only Tables, and not entire FlowCapableNode (fix Yang model)
        final var path = instanceIdentifier.toBuilder().augmentation(FlowCapableNode.class).build();

        // First, try to fill registry with flows from DS/Configuration
        final FluentFuture<Optional<FlowCapableNode>> configFuture =
                fillFromDatastore(LogicalDatastoreType.CONFIGURATION, path);

        // Now, try to fill registry with flows from DS/Operational
        // in case of cluster fail over, when clients are not using DS/Configuration
        // for adding flows, but only RPCs
        final FluentFuture<Optional<FlowCapableNode>> operationalFuture =
                fillFromDatastore(LogicalDatastoreType.OPERATIONAL, path);

        // And at last, chain and return futures created above.
        // Also, cache this future, so call to DeviceFlowRegistry.close() will be able
        // to cancel this future immediately if it will be still in progress
        final ListenableFuture<List<Optional<FlowCapableNode>>> lastFillFuture =
                Futures.allAsList(Arrays.asList(configFuture, operationalFuture));
        lastFillFutures.add(lastFillFuture);
        return lastFillFuture;
    }

    private FluentFuture<Optional<FlowCapableNode>> fillFromDatastore(final LogicalDatastoreType logicalDatastoreType,
                              final DataObjectIdentifier<FlowCapableNode> path) {
        // Prepare read operation from datastore for path
        final FluentFuture<Optional<FlowCapableNode>> future;
        try (ReadTransaction transaction = dataBroker.newReadOnlyTransaction()) {
            future = transaction.read(logicalDatastoreType, path);
        }

        future.addCallback(new FutureCallback<Optional<FlowCapableNode>>() {
            @Override
            public void onSuccess(final Optional<FlowCapableNode> result) {
                result.ifPresent(flowCapableNode -> {
                    flowCapableNode.nonnullTable().values().stream()
                        .filter(Objects::nonNull)
                        .flatMap(table -> table.nonnullFlow().values().stream())
                        .filter(Objects::nonNull)
                        .filter(flow -> flow.getId() != null)
                        .forEach(flow -> {
                            final var flowRegistryKey = createKey(flow);
                            if (getExistingKey(flowRegistryKey) == null) {
                                // Now, we will update the registry
                                storeDescriptor(flowRegistryKey, FlowDescriptorFactory.create(flow.getTableId(),
                                    flow.getId()));
                            }
                        });
                });
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.debug("Failed to read {} path {}", logicalDatastoreType, path, throwable);
            }
        }, MoreExecutors.directExecutor());

        return future;
    }

    @Override
    public FlowDescriptor retrieveDescriptor(@NonNull final FlowRegistryKey flowRegistryKey) {
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
    public void storeDescriptor(@NonNull final FlowRegistryKey flowRegistryKey,
                                @NonNull final FlowDescriptor flowDescriptor) {
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
                LOG.warn("Flow with flow ID {} already exists in table {}, generating alien flow ID",
                        flowDescriptor.getFlowId().getValue(),
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
        if (retrieveDescriptor(flowRegistryKey) == null) {
            LOG.debug("Flow descriptor for flow hash : {} not found, generating alien flow ID", flowRegistryKey);

            // We do not found flow in flow registry, that means it do not have any ID already assigned, so we need
            // to generate new alien flow ID here.
            final Uint8 tableId = Uint8.valueOf(flowRegistryKey.getTableId());
            storeDescriptor(flowRegistryKey, FlowDescriptorFactory.create(tableId, createAlienFlowId(tableId)));
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
    public void appendHistoryFlow(final FlowId id, final Uint8 tableId, final FlowGroupStatus status) {
        history.appendFlowGroupInfo(new FlowInfo(status, id, tableId));
    }

    @Override
    public void clearFlowRegistry() {
        flowRegistry.clear();
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
    static FlowId createAlienFlowId(final Uint8 tableId) {
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
        if (flowRegistryKey.getMatch().augmentation(GeneralAugMatchNodesNodeTableFlow.class) == null) {
            if (flowRegistry.containsKey(flowRegistryKey)) {
                return flowRegistryKey;
            }
        } else {
            synchronized (flowRegistry) {
                for (Map.Entry<FlowRegistryKey, FlowDescriptor> keyValueSet : flowRegistry.entrySet()) {
                    if (keyValueSet.getKey().equals(flowRegistryKey)) {
                        return keyValueSet.getKey();
                    }
                }
            }
        }
        return null;
    }

    @VisibleForTesting
    Map<FlowRegistryKey, FlowDescriptor> getAllFlowDescriptors() {
        return flowRegistry;
    }

    private static final class FlowInfo extends FlowGroupInfo {
        private final Uint8 tableId;
        private final FlowId id;

        FlowInfo(final FlowGroupStatus status, final FlowId id, final Uint8 tableId) {
            super(status);
            this.id = requireNonNull(id);
            this.tableId = requireNonNull(tableId);
        }

        @Override
        public String getId() {
            return id.getValue();
        }

        @Override
        public String getDescription() {
            return tableId.toString();
        }
    }
}
