/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.connection;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FutureCallback;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.Uint32;

@Beta
public interface OutboundQueue {
    /**
     * Reserve an entry in the outbound queue.
     * @return XID for the new message, or null if the queue is full
     */
    Uint32 reserveEntry();

    /**
     * Commit the specified offset using a message. Specified callback will
     * be invoked once we know how it has resolved, either with a normal response,
     * implied completion via a barrier, or failure (such as connection drop). For
     * multipart responses, {@link FutureCallback#onSuccess(Object)} will be invoked
     * multiple times as the corresponding responses arrive. If the request is completed
     * with a response, the object reported will be non-null. If the request's completion
     * is implied by a barrier, the object reported will be null.
     *
     * <p>If this request fails on the remote device, {@link FutureCallback#onFailure(Throwable)}
     * will be called with an instance of {@link DeviceRequestFailedException}.
     *
     * <p>If the request fails due to local reasons, {@link FutureCallback#onFailure(Throwable)}
     * will be called with an instance of {@link OutboundQueueException}. In particular, if
     * this request failed because the device disconnected, {@link OutboundQueueException#DEVICE_DISCONNECTED}
     * will be reported.
     *
     * @param xid Previously-reserved XID
     * @param message Message which should be sent out, or null if the reservation
     *                should be cancelled.
     * @param callback Callback to be invoked, or null if no callback should be invoked.
     * @throws IllegalArgumentException if the slot is already committed or was never reserved.
     */
    void commitEntry(
            @NonNull Uint32 xid,
            @Nullable OfHeader message,
            @Nullable FutureCallback<OfHeader> callback);

    /**
     * Commit the specified offset using a message. Specified callback will
     * be invoked once we know how it has resolved, either with a normal response,
     * implied completion via a barrier, or failure (such as connection drop). For
     * multipart responses, {@link FutureCallback#onSuccess(Object)} will be invoked
     * multiple times as the corresponding responses arrive. If the request is completed
     * with a response, the object reported will be non-null. If the request's completion
     * is implied by a barrier, the object reported will be null.
     *
     * <p>If this request fails on the remote device, {@link FutureCallback#onFailure(Throwable)}
     * will be called with an instance of {@link DeviceRequestFailedException}.
     *
     * <p>If the request fails due to local reasons, {@link FutureCallback#onFailure(Throwable)}
     * will be called with an instance of {@link OutboundQueueException}. In particular, if
     * this request failed because the device disconnected, {@link OutboundQueueException#DEVICE_DISCONNECTED}
     * will be reported.
     *
     * @param xid Previously-reserved XID
     * @param message Message which should be sent out, or null if the reservation
     *                should be cancelled.
     * @param callback Callback to be invoked, or null if no callback should be invoked.
     * @param isComplete Function to determine if OfHeader is processing is complete
     * @throws IllegalArgumentException if the slot is already committed or was never reserved.
     */
    void commitEntry(
            @NonNull Uint32 xid,
            @Nullable OfHeader message,
            @Nullable FutureCallback<OfHeader> callback,
            @Nullable Function<OfHeader, Boolean> isComplete);
}
