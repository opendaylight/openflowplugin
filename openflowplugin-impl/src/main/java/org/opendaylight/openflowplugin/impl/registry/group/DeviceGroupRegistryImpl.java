/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.group;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;

public class DeviceGroupRegistryImpl implements DeviceGroupRegistry {

    private final List<GroupId> groupIdList = new ArrayList<>();
    private final List<GroupId> marks = new ArrayList<>();

    @Override
    public void store(final GroupId groupId) {
        groupIdList.add(groupId);
    }

    @Override
    public void addMark(final GroupId groupId) {
        marks.add(groupId);
    }

    @Override
    public boolean hasMark(final GroupId groupId) {
        synchronized (marks) {
            return marks.contains(groupId);
        }
    }

    @Override
    public void processMarks() {
        synchronized (groupIdList) {
            groupIdList.removeAll(marks);
        }

        synchronized (marks) {
            marks.clear();
        }
    }

    @Override
    public void forEach(final Consumer<GroupId> consumer) {
        groupIdList.forEach(consumer);
    }

    @Override
    public int size() {
        return groupIdList.size();
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

    @VisibleForTesting
    List<GroupId> getAllGroupIds() {
        return groupIdList;
    }
}
