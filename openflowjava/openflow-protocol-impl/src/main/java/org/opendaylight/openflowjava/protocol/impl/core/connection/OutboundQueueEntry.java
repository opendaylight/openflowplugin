/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.function.Function;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class OutboundQueueEntry {
    private static final Logger LOG = LoggerFactory.getLogger(OutboundQueueEntry.class);
    public static final Function<OfHeader, Boolean> DEFAULT_IS_COMPLETE = message -> {
        if (message instanceof MultipartReplyMessage) {
            return !((MultipartReplyMessage) message).getFlags().getOFPMPFREQMORE();
        }

        return true;
    };

    private FutureCallback<OfHeader> callback;
    private OfHeader message;
    private boolean completed;
    private boolean barrier;
    private volatile boolean committed;
    private Function<OfHeader, Boolean> isCompletedFunction = DEFAULT_IS_COMPLETE;

    void commit(final OfHeader messageToCommit, final FutureCallback<OfHeader> commitCallback) {
        commit(messageToCommit, commitCallback, DEFAULT_IS_COMPLETE);
    }

    void commit(final OfHeader messageToCommit, final FutureCallback<OfHeader> commitCallback,
            final Function<OfHeader, Boolean> isCommitCompletedFunction) {
        if (this.completed) {
            LOG.warn("Can't commit a completed message.");
            if (commitCallback != null) {
                commitCallback.onFailure(new OutboundQueueException("Can't commit a completed message."));
            }
        } else {
            this.message = messageToCommit;
            this.callback = commitCallback;
            this.barrier = messageToCommit instanceof BarrierInput;
            this.isCompletedFunction = isCommitCompletedFunction;

            // Volatile write, needs to be last
            this.committed = true;
        }
    }

    void reset() {
        barrier = false;
        callback = null;
        completed = false;
        message = null;

        // Volatile write, needs to be last
        committed = false;
    }

    boolean isBarrier() {
        return barrier;
    }

    boolean isCommitted() {
        return committed;
    }

    boolean isCompleted() {
        return completed;
    }

    OfHeader takeMessage() {
        final OfHeader ret = message;
        if (!barrier) {
            checkCompletionNeed();
        }
        message = null;
        return ret;
    }

    private void checkCompletionNeed() {
        if (callback == null || message instanceof PacketOutInput) {
            completed = true;
            if (callback != null) {
                callback.onSuccess(null);
                callback = null;
            }
            committed = false;
        }
    }

    boolean complete(@Nullable final OfHeader response) {
        Preconditions.checkState(!completed, "Attempted to complete a completed message with response %s", response);

        // Multipart requests are special, we have to look at them to see
        // if there is something outstanding and adjust ourselves accordingly
        final boolean reallyComplete = isCompletedFunction.apply(response);

        completed = reallyComplete;
        if (callback != null) {
            callback.onSuccess(response);
            if (reallyComplete) {
                // We will not need the callback anymore, make sure it can be GC'd
                callback = null;
            }
        }
        LOG.debug("Entry {} completed {} with response {}", this, completed, response);
        return reallyComplete;
    }

    void fail(final OutboundQueueException cause) {
        if (!completed) {
            completed = true;
            if (callback != null) {
                callback.onFailure(cause);
                callback = null;
            }
        } else {
            LOG.warn("Ignoring failure for completed message", cause);
        }
    }

    @VisibleForTesting
    /** This method is only for testing to prove that after queue entry is completed there is not callback future */
    boolean hasCallback() {
        return callback != null;
    }
}
