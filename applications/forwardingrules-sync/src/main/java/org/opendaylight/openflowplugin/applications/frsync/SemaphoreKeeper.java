/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync;

import java.util.concurrent.Semaphore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Proposal for how a key based semaphore provider should look like.
 * <ul>
 * <li>thread safe</li>
 * <li>garbage-collect unused semaphores</li>
 * <li>for the same key there must be always only one semaphore available</li>
 * </ul>
 *
 *
 * usage:
 * <pre>
 * final Semaphore guard = semaphoreKeeper.summonGuard(key);
 * guard.acquire();
 * // guard protected logic ...
 * guard.release();
 * </pre>
 *
 * @param <K> key type
 */

public interface SemaphoreKeeper<K> {
    /**
     * @param key semaphore identifier
     * @return new or existing semaphore for given key, for one key there is always only one semaphore available
     */
    Semaphore summonGuard(@Nonnull final K key);

    /**
     * Get guard and lock for key.
     * @param key for which guard should be created and acquired
     * @return semaphore guard
     */
    Semaphore summonGuardAndAcquire(@Nonnull final K key);

    /**
     * Unlock and release guard.
     * @param guard semaphore guard which should be released
     */
    void releaseGuard(@Nullable final Semaphore guard);
}
