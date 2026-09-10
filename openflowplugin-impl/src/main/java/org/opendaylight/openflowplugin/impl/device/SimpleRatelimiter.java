/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.util.concurrent.atomic.AtomicInteger;

abstract class SimpleRatelimiter {
    private final AtomicInteger counter = new AtomicInteger();
    private final Object counterLock = new Object();
    @GuardedBy("counterLock")
    private int lowWatermark;
    private volatile int lowWatermarkEffective;
    private volatile int highWatermark;
    @GuardedBy("counterLock")
    private volatile boolean limited;

    SimpleRatelimiter(final int lowWatermark, final int highWatermark) {
        checkArgument(lowWatermark >= 0);
        checkArgument(highWatermark >= 0);
        checkArgument(lowWatermark <= highWatermark);

        this.lowWatermark = lowWatermark;
        this.highWatermark = highWatermark;
        lowWatermarkEffective = lowWatermark;
    }

    protected final boolean isLimited() {
        return limited;
    }

    protected abstract void disableFlow();

    protected abstract void enableFlow();

    boolean acquirePermit() {
        final int cnt = counter.incrementAndGet();
        if (cnt > highWatermark) {
            synchronized (counterLock) {
                final int recheck = counter.decrementAndGet();
                if (recheck >= highWatermark && !limited) {
                    disableFlow();
                    limited = true;
                }
            }
            return false;
        }

        return true;
    }

    void releasePermit() {
        final int cnt = counter.decrementAndGet();
        if (cnt <= lowWatermarkEffective) {
            synchronized (counterLock) {
                final int recheck = counter.get();
                if (recheck <= lowWatermarkEffective && limited) {
                    enableFlow();
                    limited = false;
                    resetLowWaterMark();
                }
            }
        }
    }

    @GuardedBy("counterLock")
    private void resetLowWaterMark() {
        lowWatermarkEffective = lowWatermark;
    }

    void adaptLowWaterMarkAndDisableFlow(final int temporaryLowWaterMark) {
        if (temporaryLowWaterMark < highWatermark) {
            synchronized (counterLock) {
                lowWatermarkEffective = temporaryLowWaterMark;
                if (!limited) {
                    disableFlow();
                    limited = true;
                }
            }
        }
    }

    int getOccupiedPermits() {
        return counter.get();
    }

    void changeWaterMarks(final int newLowWatermark, final int newHighWatermark) {
        synchronized (counterLock) {
            lowWatermark = newLowWatermark;
            highWatermark = newHighWatermark;
            resetLowWaterMark();
        }
    }
}
