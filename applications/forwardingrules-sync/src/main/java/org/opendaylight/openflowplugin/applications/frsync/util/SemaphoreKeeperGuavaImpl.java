/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.util;

import static java.util.Objects.requireNonNull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.Semaphore;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.openflowplugin.applications.frsync.SemaphoreKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemaphoreKeeperGuavaImpl<K> implements SemaphoreKeeper<K> {

    private static final Logger LOG = LoggerFactory.getLogger(SemaphoreKeeperGuavaImpl.class);
    private final LoadingCache<K, Semaphore> semaphoreCache;

    public SemaphoreKeeperGuavaImpl(final int permits, final boolean fair) {
        semaphoreCache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .weakValues()
                .build(new CacheLoader<K, Semaphore>() {
                    @Override
                    public Semaphore load(final K key) {
                        return new Semaphore(permits, fair);
                    }
                });
    }

    @Override
    public Semaphore summonGuard(@NonNull final K key) {
        return semaphoreCache.getUnchecked(key);
    }

    @Override
    public Semaphore summonGuardAndAcquire(@NonNull final K key) {
        final Semaphore guard = requireNonNull(summonGuard(key), "Guard not available for " + key);
        try {
            guard.acquire();
        } catch (InterruptedException e) {
            LOG.warn("Could not acquire guard for {}", key, e);
            return null;
        }
        return guard;
    }

    @Override
    public void releaseGuard(@Nullable final Semaphore guard) {
        if (guard != null) {
            guard.release();
        }
    }
}
