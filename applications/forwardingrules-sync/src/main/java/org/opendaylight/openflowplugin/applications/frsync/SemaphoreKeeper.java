/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync;

import java.util.concurrent.Semaphore;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Key based semaphore provider.
 * For the same key there is always only one semaphore available. Unused semaphores are garbage-collect.
 * @param <K> key type
 */
public interface SemaphoreKeeper<K> {
    /**
     * Create or load semaphore for key from cache.
     * @param key semaphore identifier
     * @return new or existing semaphore for given key, for one key there is always only one semaphore available
     */
    Semaphore summonGuard(@NonNull K key);

    /**
     * Get guard and lock for key.
     * @param key for which guard should be created and acquired
     * @return semaphore guard
     */
    Semaphore summonGuardAndAcquire(@NonNull K key);

    /**
     * Unlock and release guard.
     * @param guard semaphore guard which should be released
     */
    void releaseGuard(@Nullable Semaphore guard);
}
