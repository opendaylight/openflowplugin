/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.group;

import com.google.common.annotations.VisibleForTesting;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import javax.annotation.concurrent.ThreadSafe;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;

@ThreadSafe
public class DeviceGroupRegistryImpl implements DeviceGroupRegistry {

    private final Queue<GroupId> groupIds = new ConcurrentLinkedQueue<>();
    private final Queue<GroupId> marks = new ConcurrentLinkedQueue<>();

    @Override
    public void store(final GroupId groupId) {
        marks.remove(groupId);
        groupIds.add(groupId);
    }

    @Override
    public void addMark(final GroupId groupId) {
        marks.add(groupId);
    }

    @Override
    public void processMarks() {
        groupIds.removeAll(marks);
        marks.clear();
    }

    @Override
    public void forEach(final Consumer<GroupId> consumer) {
        groupIds.forEach(consumer);
    }

    @Override
    public int size() {
        return groupIds.size();
    }

    @Override
    public void close() {
        groupIds.clear();
        marks.clear();
    }

    @VisibleForTesting
    Queue<GroupId> getAllGroupIds() {
        return groupIds;
    }
}
