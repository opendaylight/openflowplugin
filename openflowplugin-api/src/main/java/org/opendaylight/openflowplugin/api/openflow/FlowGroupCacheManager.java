/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow;

import java.util.Collection;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;

public interface FlowGroupCacheManager {

    Map<String, ReconciliationState> getReconciliationStates();

    Map<String, Collection<FlowGroupInfo>> getAllNodesFlowGroupCache();

    void appendFlowGroup(@NonNull String nodeId, @NonNull String id, @NonNull String description,
        @NonNull FlowGroupStatus status);
}
