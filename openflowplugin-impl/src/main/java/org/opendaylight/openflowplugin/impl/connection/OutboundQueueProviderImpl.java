/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection;

import com.google.common.util.concurrent.FutureCallback;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutboundQueueProviderImpl implements OutboundQueueProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OutboundQueueProviderImpl.class);
    private final short ofVersion;
    private volatile OutboundQueue outboundQueue;

    public OutboundQueueProviderImpl(final short ofVersion) {
        this.ofVersion = ofVersion;
    }

    @Nonnull
    @Override
    public BarrierInput createBarrierRequest(@Nonnull final Long xid) {
        final BarrierInputBuilder biBuilder = new BarrierInputBuilder();
        biBuilder.setVersion(ofVersion);
        biBuilder.setXid(xid);
        return biBuilder.build();

    }

    @Override
    public synchronized void onConnectionQueueChanged(final OutboundQueue queue) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Replacing queue {} with {}", outboundQueue, queue);
        }
        outboundQueue = queue;
        notifyAll();
    }

    @Override
    public Long reserveEntry() {
        for (;;) {
            OutboundQueue queue = outboundQueue;
            if (queue == null) {
                LOG.error("No queue present, failing request");
                return null;
            }

            final Long ret = queue.reserveEntry();
            if (ret != null) {
                return ret;
            }

            LOG.debug("Reservation failed, trying to recover");
            synchronized (this) {
                while (queue.equals(outboundQueue)) {
                    LOG.debug("Queue {} is not replaced yet, going to sleep", queue);
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        LOG.error("Interrupted while waiting for entry", e);
                        return null;
                    }
                }
            }
        }
    }

    @Override
    public void commitEntry(final Long xid, final OfHeader message, final FutureCallback<OfHeader> callback) {
        outboundQueue.commitEntry(xid, message, callback);
    }

    @Override
    public void commitEntry(final Long xid, final OfHeader message, final FutureCallback<OfHeader> callback,
            final Function<OfHeader, Boolean> isComplete) {
        outboundQueue.commitEntry(xid, message, callback, isComplete);
    }
}
