/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.cache;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Queues;
import java.util.Collection;
import java.util.Queue;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupInfo;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupInfoHistory;
import org.opendaylight.yangtools.util.UnmodifiableCollection;

final class FlowGroupInfoHistoryImpl implements FlowGroupInfoHistory, FlowGroupInfoHistoryAppender {
    private final Queue<FlowGroupInfo> entries;

    FlowGroupInfoHistoryImpl(final int size) {
        // FIXME: synchronized queue relies on locking -- and most of the time all we access it from the same(-ish)
        //        context. We should be able to do better. To do do that we need an Executor which will run exclusively
        //        with the associated device (i.e. in its Netty context) ...
        entries = Queues.synchronizedQueue(EvictingQueue.create(size));
    }

    @Override
    public Collection<FlowGroupInfo> entries() {
        // FIXME: ... and then this method will submit a request on that executor, producing a copy of the history
        return UnmodifiableCollection.create(entries);
    }

    @Override
    public void appendFlowGroupInfo(final @NonNull FlowGroupInfo info) {
        entries.add(info);
    }
}
