/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

public class DevicesGroupRegistry {
    private final Map<NodeId, List<Long>> deviceGroupMapping = new ConcurrentHashMap<>();

    public boolean isGroupPresent(NodeId nodeId, Long groupId) {
        return deviceGroupMapping.get(nodeId) != null ? deviceGroupMapping.get(nodeId).contains(groupId) : false;
    }

    public void storeGroup(NodeId nodeId, Long groupId) {
        deviceGroupMapping.computeIfAbsent(nodeId, groupIdList -> new ArrayList<>()).add(groupId);
    }

    public void removeGroup(NodeId nodeId, Long groupId) {
        deviceGroupMapping.computeIfPresent(nodeId, (node, groupIds) -> groupIds).remove(groupId);
    }

    public void clearNodeGroups(NodeId nodeId) {
        deviceGroupMapping.remove(nodeId);
    }

}
