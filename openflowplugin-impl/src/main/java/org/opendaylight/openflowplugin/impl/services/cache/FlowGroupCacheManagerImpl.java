/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.cache;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCache;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;

public class FlowGroupCacheManagerImpl implements FlowGroupCacheManager {

    private Map<String, Queue<FlowGroupCache>> flowGroupCacheListForAllNodes = new ConcurrentHashMap<>();

    public Map<String, Queue<FlowGroupCache>> getFlowGroupCacheListForAllNodes() {
        return flowGroupCacheListForAllNodes;
    }
}