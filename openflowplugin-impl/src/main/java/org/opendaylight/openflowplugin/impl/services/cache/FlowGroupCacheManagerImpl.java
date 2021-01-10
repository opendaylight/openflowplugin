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
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupInfo;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupStatus;
import org.opendaylight.openflowplugin.api.openflow.ReconciliationState;

@Singleton
@Service(classes = FlowGroupCacheManager.class)
public class FlowGroupCacheManagerImpl implements FlowGroupCacheManager {
    // FIXME: make this configurable and expose a different implementation at least for OSGi when this is switched off
    private static final int FLOWGROUP_CACHE_SIZE = 10000;

    private final Map<String, ReconciliationState> reconciliationStates = new ConcurrentHashMap<>();
    private final Map<String, Queue<FlowGroupInfo>> flowGroups = new ConcurrentHashMap<>();

    @Override
    public Map<String, Collection<FlowGroupInfo>> getAllNodesFlowGroupCache() {
        return Collections.unmodifiableMap(Maps.transformValues(flowGroups, Collections::unmodifiableCollection));
    }

    @Override
    public Map<String, ReconciliationState> getReconciliationStates() {
        return Collections.unmodifiableMap(reconciliationStates);
    }

    @Override
    public void appendFlowGroup(final String nodeId, final String id, final String description,
            final FlowGroupStatus status) {
        final Queue<FlowGroupInfo> queue = flowGroups.computeIfAbsent(nodeId,
            key -> Queues.synchronizedQueue(EvictingQueue.create(FLOWGROUP_CACHE_SIZE)));
        queue.add(new FlowGroupInfo(id, description, status));
    }
}