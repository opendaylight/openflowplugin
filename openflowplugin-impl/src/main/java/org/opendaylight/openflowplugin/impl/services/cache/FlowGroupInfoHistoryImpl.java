/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.cache;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Queue;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupInfo;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupInfoHistory;

@NonNullByDefault
final class FlowGroupInfoHistoryImpl implements FlowGroupInfoHistory, FlowGroupInfoHistoryAppender {
    // FIXME: most of the time all we access it from the same(-ish) context. We should be able to do better. To do do
    //        that we need an Executor which will run exclusively with the associated device
    //        (i.e. in its Netty context) ...
    @GuardedBy("this")
    private final Queue<FlowGroupInfo> entries;

    FlowGroupInfoHistoryImpl(final int size) {
        entries = EvictingQueue.create(size);
    }

    // FIXME: ... in which case this method will not be synchronized ...
    @Override
    public synchronized void appendFlowGroupInfo(final FlowGroupInfo info) {
        entries.add(info);
    }

    // FIXME: ... and this method will submit a request on that executor, producing a copy of the history
    @Override
    public synchronized Collection<FlowGroupInfo> readEntries() {
        return ImmutableList.copyOf(entries);
    }
}
