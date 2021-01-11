/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device.history;

import com.google.common.base.Stopwatch;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupInfo;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupInfoHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public final class FlowGroupInfoHistoryImpl implements FlowGroupInfoHistory, FlowGroupInfoHistoryAppender {
    private static final Logger LOG = LoggerFactory.getLogger(FlowGroupInfoHistoryImpl.class);

    private final EvictingQueue<FlowGroupInfo> entries;

    public FlowGroupInfoHistoryImpl(final int size) {
        entries = EvictingQueue.create(size);
    }

    @Override
    public void appendFlowGroupInfo(final FlowGroupInfo info) {
        // Always called from a specific device's context, no locking necessary
        entries.add(info);
    }

    @Override
    public Collection<FlowGroupInfo> readEntries() {
        // All of this is very optimistic and is a violation of synchronization. Since this is just a debug tool, we
        // care about footprint the most -- and hence the appender is taking no locks whatsoever.
        //
        // Now if a different thread attempts to read, there is nothing to synchronize on, really, so we'll try to get a
        // copy, potentially multiple times and afterwards we give up.
        final Stopwatch sw = Stopwatch.createStarted();
        do {
            try {
                return ImmutableList.copyOf(entries);
            } catch (ConcurrentModificationException e) {
                LOG.debug("Entries have been modified while we were acquiring them, retry", e);
            }
        } while (sw.elapsed(TimeUnit.SECONDS) < 3);

        // We have failed to converge in three seconds, let's try again and fail if we fail again. That is deemed good
        // enough for now.
        // TODO: If this is not sufficient, we really need to have a Netty context here somehow and then offload this
        //       access to run on that thread. Since a particular channel (and therefore DeviceContext, etc.) executes
        //       *at most* on one thread, we'd end up either running normal operation, possibly updating entries here
        //       *or* copy them over.
        return ImmutableList.copyOf(entries);
    }
}
