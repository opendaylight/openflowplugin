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
import org.opendaylight.yangtools.yang.common.Uint32;

public class DevicesGroupRegistry {
    private final Map<String, List<Uint32>> deviceGroupMapping = new ConcurrentHashMap<>();

    public boolean isGroupPresent(final String nodeId, final Uint32 groupId) {
        final List<Uint32> groups = deviceGroupMapping.get(nodeId);
        return groups != null && groups.contains(groupId);
    }

    public void storeGroup(final String nodeId, final Uint32 groupId) {
        deviceGroupMapping.computeIfAbsent(nodeId, groupIdList -> new ArrayList<>()).add(groupId);
    }

    public void removeGroup(final String nodeId, final Uint32 groupId) {
        deviceGroupMapping.computeIfPresent(nodeId, (node, groupIds) -> groupIds).remove(groupId);
    }

    public void clearNodeGroups(final String nodeId) {
        deviceGroupMapping.remove(nodeId);
    }
}