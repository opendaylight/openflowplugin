/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.registry.group;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupInfo;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupStatus;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.impl.services.cache.FlowGroupInfoHistoryAppender;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;

public class DeviceGroupRegistryImpl implements DeviceGroupRegistry {
    // FIXME: clean up locking here
    private final List<GroupId> groupIds = Collections.synchronizedList(new ArrayList<>());
    private final List<GroupId> marks = Collections.synchronizedList(new ArrayList<>());
    private final FlowGroupInfoHistoryAppender history;

    public DeviceGroupRegistryImpl(final FlowGroupInfoHistoryAppender history) {
        this.history = requireNonNull(history);
    }

    @Override
    public void store(final GroupId groupId) {
        if (!groupIds.contains(groupId)) {
            marks.remove(groupId);
            groupIds.add(groupId);
        }
    }

    @Override
    public void addMark(final GroupId groupId) {
        if (!marks.contains(groupId)) {
            marks.add(groupId);
        }
    }

    @Override
    public void processMarks() {
        groupIds.removeAll(marks);
        marks.clear();
    }

    @Override
    public void forEach(final Consumer<GroupId> consumer) {
        synchronized (groupIds) {
            groupIds.forEach(consumer);
        }
    }

    @Override
    public int size() {
        return groupIds.size();
    }

    @Override
    public void appendHistoryGroup(final GroupId id, final GroupTypes type, final FlowGroupStatus status) {
        history.appendFlowGroupInfo(FlowGroupInfo.ofGroup(id, type, status));
    }

    @Override
    public void close() {
        groupIds.clear();
        marks.clear();
    }

    @VisibleForTesting
    List<GroupId> getAllGroupIds() {
        return groupIds;
    }
}
