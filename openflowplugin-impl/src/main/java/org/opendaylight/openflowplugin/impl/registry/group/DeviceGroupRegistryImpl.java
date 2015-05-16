/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.group;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 15.4.2015.
 */
public class DeviceGroupRegistryImpl implements DeviceGroupRegistry {

    private final List<GroupId> groupIdList = new ArrayList<>();
    private final List<GroupId> marks = new ArrayList<>();

    @Override
    public void store(final GroupId groupId) {
        groupIdList.add(groupId);
    }

    @Override
    public void markToBeremoved(final GroupId groupId) {
        marks.add(groupId);
    }

    @Override
    public void removeMarked() {
        synchronized (groupIdList) {
            groupIdList.removeAll(marks);
        }
        marks.clear();
    }

    @Override
    public List<GroupId> getAllGroupIds() {
        return groupIdList;
    }

    @Override
    public void close() {
        synchronized (groupIdList) {
            groupIdList.clear();
        }
        synchronized (marks) {
            marks.clear();
        }
    }
}
