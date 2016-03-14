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

import java.util.Collection;
import java.util.concurrent.Semaphore;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.applications.frsync.SemaphoreKeeper;

/**
 * key-based semaphore provider
 * 
 * @author Michal Rehak
 */
public class SemaphoreKeeperGuavaImpl<K> implements SemaphoreKeeper<K> {

    private LoadingCache<K, Semaphore> semaphoreCache;

    public SemaphoreKeeperGuavaImpl(final int permits, final boolean fair) {
        semaphoreCache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .weakValues()
                .build(new CacheLoader<K, Semaphore>() {
                    @Override
                    public Semaphore load(final K key) throws Exception {
                        return new Semaphore(permits, fair) {
                            private static final long serialVersionUID = 1L;

                            /*@Override
                            public String toString() {
                                final StringBuilder str = new StringBuilder();
                                str.append(super.toString());
                                str.append("[");
                                final Collection<Thread> queuedThreads = getQueuedThreads();
                                boolean qtdelim = false;
                                for (Thread thread : queuedThreads) {
                                    if(qtdelim) {
                                        str.append(", ");
                                    } else {
                                        qtdelim = true;
                                        str.append(thread.getName());
                                    }
                                }
                                str.append("]");
                                str.append(queuedThreads);
                                return str.toString();
                            }*/
                        };
                    }
                });
    }

    @Override
    public Semaphore summonGuard(final @Nonnull K key) {
        return semaphoreCache.getUnchecked(key);
    }
}
