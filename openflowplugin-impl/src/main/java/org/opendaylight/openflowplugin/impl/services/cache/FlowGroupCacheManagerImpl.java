/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.cache;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Service;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupInfo;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupStatus;
import org.opendaylight.openflowplugin.api.openflow.ReconciliationState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.common.Uint8;

@Singleton
@Service(classes = FlowGroupCacheManager.class)
public class FlowGroupCacheManagerImpl implements FlowGroupCacheManager {
    // FIXME: make this configurable and expose a different implementation at least for OSGi when this is switched off
    private static final int FLOWGROUP_CACHE_SIZE = 10000;

    private final Map<String, ReconciliationState> reconciliationStates = new ConcurrentHashMap<>();
    private final Map<NodeId, Queue<FlowGroupInfo>> flowGroups = new ConcurrentHashMap<>();

    @Override
    public Map<String, ReconciliationState> getReconciliationStates() {
        return reconciliationStates;
    }

    @Override
    public Map<NodeId, Collection<FlowGroupInfo>> getAllNodesFlowGroupCache() {
        return Collections.unmodifiableMap(Maps.transformValues(flowGroups, Collections::unmodifiableCollection));
    }

    @Override
    public void appendFlow(final NodeId nodeId, final FlowId id, final Uint8 tableId, final FlowGroupStatus status) {
        flowGroup(nodeId).add(FlowGroupInfo.ofFlow(id, tableId, status));
    }

    @Override
    public void appendGroup(final NodeId nodeId, final GroupId id, final GroupTypes type,
            final FlowGroupStatus status) {
        flowGroup(nodeId).add(FlowGroupInfo.ofGroup(id, type, status));
    }

    // FIXME: we really want to split out the 'nodeId' lookup and provide an internal interface for the plugin to
    //        contribute directly to the queue.
    private @NonNull Queue<FlowGroupInfo> flowGroup(final NodeId nodeId) {
        return flowGroups.computeIfAbsent(nodeId,
            // FIXME: synchronized queue relies on locking -- and most of the time all we access it from the same(-ish)
            //        context. We should be able to do better.
            key -> Queues.synchronizedQueue(EvictingQueue.create(FLOWGROUP_CACHE_SIZE)));
    }
}