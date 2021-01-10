/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.cache;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Service;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupInfoHistories;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupInfoHistory;
import org.opendaylight.openflowplugin.api.openflow.ReconciliationState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

@Singleton
@Service(classes = { FlowGroupCacheManager.class, FlowGroupInfoHistories.class, FlowGroupInfoHistoryRegistry.class })
public class FlowGroupCacheManagerImpl
        implements FlowGroupCacheManager, FlowGroupInfoHistories, FlowGroupInfoHistoryRegistry {
    // FIXME: make this configurable and expose a different implementation at least for OSGi when this is switched off
    private static final int FLOWGROUP_CACHE_SIZE = 10000;

    private final Map<String, ReconciliationState> reconciliationStates = new ConcurrentHashMap<>();
    private final Map<NodeId, FlowGroupInfoHistoryImpl> flowGroups = new ConcurrentHashMap<>();

    @Override
    public Map<String, ReconciliationState> getReconciliationStates() {
        return reconciliationStates;
    }

    @Override
    public Map<NodeId, FlowGroupInfoHistory> getAllFlowGroupHistories() {
        return Collections.unmodifiableMap(flowGroups);
    }

    @Override
    public FlowGroupInfoHistory getFlowGroupHistory(final NodeId nodeId) {
        return flowGroups.get(nodeId);
    }

    @Override
    public FlowGroupInfoHistoryAppender getAppender(final NodeId nodeId) {
        return flowGroups.computeIfAbsent(nodeId, key -> new FlowGroupInfoHistoryImpl(FLOWGROUP_CACHE_SIZE));
    }
}