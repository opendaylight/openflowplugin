/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.Semaphore;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.applications.frsync.SemaphoreKeeper;

/**
 * key-based semaphore provider
 */
public class SemaphoreKeeperImpl<K> implements SemaphoreKeeper<K> {

    private LoadingCache<K, Semaphore> semaphoreCache;

    public SemaphoreKeeperImpl(final int permits, final boolean fair) {
        semaphoreCache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .weakValues()
                .build(new CacheLoader<K, Semaphore>() {
                    @Override
                    public Semaphore load(final K key) throws Exception {
                        return new Semaphore(permits, fair);
                    }
                });
    }

    @Override
    public Semaphore summonGuard(final @Nonnull K key) {
        return semaphoreCache.getUnchecked(key);
    }
}
