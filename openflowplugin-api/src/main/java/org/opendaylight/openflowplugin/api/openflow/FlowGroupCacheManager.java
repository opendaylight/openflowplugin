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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.common.Uint8;

public interface FlowGroupCacheManager {

    Map<String, ReconciliationState> getReconciliationStates();

    // FIXME: this quite unrelated to getReconciliationStates() to the point
    Map<NodeId, Collection<FlowGroupInfo>> getAllNodesFlowGroupCache();

    // FIXME: these two methods should live in a separate interface
    void appendFlow(@NonNull NodeId nodeId, @NonNull FlowId id, Uint8 tableId, @NonNull FlowGroupStatus status);

    void appendGroup(@NonNull NodeId nodeId, @NonNull GroupId id, @NonNull GroupTypes type,
        @NonNull FlowGroupStatus status);
}
