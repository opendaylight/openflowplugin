/*
 * Copyright (c) 2013 Pantheon Technologies, s.r.o. and others. All rights reserved.
 * Copyright (c) 2024 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.channel.EventLoopGroup;
import java.net.InetSocketAddress;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Server facade interface.
 */
public abstract class ServerFacade {
    private final @NonNull SettableFuture<Void> shutdownFuture = SettableFuture.create();
    private final @NonNull InetSocketAddress localAddress;

    @GuardedBy("this")
    private EventLoopGroup group;

    ServerFacade(final EventLoopGroup group, final InetSocketAddress localAddress) {
        this.localAddress = requireNonNull(localAddress);
        this.group = requireNonNull(group);

        // Hook onto group shutting down -- that's when we know shutdownFuture is completed
        group.terminationFuture().addListener(downResult -> {
            final var cause = downResult.cause();
            if (cause != null) {
                shutdownFuture.setException(cause);
            } else {
                shutdownFuture.set(null);
            }
        });
    }

    /**
     * Returns the local address.
     *
     * @return the local address
     */
    public final @NonNull InetSocketAddress localAddress() {
        return localAddress;
    }

    /**
     * Shuts down this facade. If this facade was already shut down, this method does nothing.
     *
     * @return a future completing when the facade has been shut down
     */
    synchronized @NonNull ListenableFuture<Void> shutdown() {
        final var local = group;
        if (local != null) {
            group = null;
            local.shutdownGracefully();
        }
        return shutdownFuture;
    }
}
