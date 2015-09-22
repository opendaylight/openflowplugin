/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.registry.flow;

import com.romix.scala.collection.concurrent.TrieMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.util.FlowUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
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
    public FlowDescriptor retrieveIdForFlow(final FlowRegistryKey flowRegistryKey) {
        FlowDescriptor flowDescriptor = flowRegistry.get(flowRegistryKey);
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
